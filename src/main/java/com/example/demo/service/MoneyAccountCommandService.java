package com.example.demo.service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.BaseApplicationService;
import com.example.demo.base.context.ContextHolder;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.event.EventLog;
import com.example.demo.base.exception.ValidationException;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.account.command.CreateMoneyAccountCommand;
import com.example.demo.domain.account.command.DepositMoneyCommand;
import com.example.demo.domain.account.outbound.AccountTxEvent;
import com.example.demo.domain.account.service.MoneyAccountService;
import com.example.demo.domain.share.MoneyAccountRegisteredData;
import com.example.demo.domain.share.MoneyDepositedData;
import com.example.demo.infra.repository.MoneyAccountRepository;
import com.example.demo.util.JsonParseUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public class MoneyAccountCommandService extends BaseApplicationService {

	@Value("${rabbitmq.register-topic-queue.name}")
	private String registerQueueName;

	@Value("${rabbitmq.acount-tx-topic-queue.name}")
	private String txQueueName;

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	private final MoneyAccountService moneyAccountService;
	private final MoneyAccountRepository moneyAccountRepository;

	/**
	 * 註冊儲值帳戶
	 * 
	 * @param command
	 * @return MoneyAccountRegisteredData
	 */
	public MoneyAccountRegisteredData register(CreateMoneyAccountCommand command) {

		MoneyAccount account = moneyAccountRepository.findByEmail(command.getEmail());
		if (!Objects.isNull(account)) {
			log.error("該信箱已被註冊");
			throw new ValidationException("VALIDATE_FAILED", "該信箱已被註冊");
		}

		// 發布 Event 到 AuthService 進行註冊
		MoneyAccountRegisteredData saved = moneyAccountService.register(command);

		// 從 ContextHolder 取出 Event
		BaseEvent event = ContextHolder.getEvent();

		// 紀錄 Message 狀態
		EventLog eventLog = this.generateEventLog(registerQueueName, event.getEventLogUuid(), saved.getUuid(), event);
		// 發布註冊使用者事件 (到 AuthService 進行註冊)
		this.publishEvent(exchangeName, registerQueueName, event);
		eventLog.publish(JsonParseUtil.serialize(event)); // 更改狀態為:已發布
		eventLogRepository.save(eventLog);
		return saved;
	}

	/**
	 * 發布 Event 進行儲值
	 * 
	 * @param command
	 */
	public MoneyDepositedData publishDepositEvent(DepositMoneyCommand command) {

		Optional<MoneyAccount> op = moneyAccountRepository.findById(command.getUuid());
		if (op.isEmpty()) {
			log.error("查無此帳號，加值失敗。");
			throw new ValidationException("VALIDATE_FAILED", "加值失敗，拋出例外");
		} else {
			MoneyAccount moneyAccount = op.get();
			BigDecimal balance = moneyAccount.getBalance().add(command.getMoney());
			AccountTxEvent event = AccountTxEvent.builder().targetId(moneyAccount.getUuid())
					.eventLogUuid(UUID.randomUUID().toString()).money(balance).build();
			
			// 建立 EventLog
			EventLog eventLog = this.generateEventLog(txQueueName, event.getEventLogUuid(), event.getTargetId(), event);

			// 發布 Event 進行儲值
			this.publishEvent(exchangeName, txQueueName, event);
			eventLog.publish(eventLog.getBody());
			eventLogRepository.save(eventLog);

			return new MoneyDepositedData(moneyAccount.getUuid(), moneyAccount.getUsername(), balance);
		}
	}

	/**
	 * 進行帳戶儲值
	 * 
	 * @param command
	 * @return MoneyDepositedRegisteredData
	 */
	public void deposit(DepositMoneyCommand command) {
		moneyAccountService.deposit(command);
	}

}
