package com.example.demo.service;

import java.util.Objects;

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
import com.example.demo.base.repository.EventLogRepository;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.account.command.CreateMoneyAccountCommand;
import com.example.demo.domain.account.command.DepositMoneyCommand;
import com.example.demo.domain.account.service.MoneyAccountService;
import com.example.demo.domain.share.dto.MoneyAccountRegisteredData;
import com.example.demo.domain.share.dto.MoneyDepositedRegisteredData;
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

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	private final EventLogRepository eventLogRepository;
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
		EventLog eventLog = this.generateEventLog(registerQueueName, saved.getUuid(), event);
		// 發布註冊使用者事件 (到 AuthService 進行註冊)
		this.publishEvent(exchangeName, registerQueueName, event);
		eventLog.publish(JsonParseUtil.serialize(event)); // 更改狀態為:已發布
		eventLogRepository.save(eventLog);

		return saved;
	}

	/**
	 * 帳戶儲值
	 * 
	 * @param command
	 * @return MoneyDepositedRegisteredData
	 */
	public MoneyDepositedRegisteredData deposit(DepositMoneyCommand command) {
		return moneyAccountService.deposit(command);
	}

}
