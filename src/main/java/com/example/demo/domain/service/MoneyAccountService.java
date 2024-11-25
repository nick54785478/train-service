package com.example.demo.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.base.config.context.ContextHolder;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.exception.ValidationException;
import com.example.demo.base.service.BaseDomainService;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.account.command.CreateMoneyAccountCommand;
import com.example.demo.domain.account.command.DepositMoneyCommand;
import com.example.demo.domain.share.MoneyAccountQueriedData;
import com.example.demo.domain.share.MoneyAccountRegisteredData;
import com.example.demo.infra.repository.MoneyAccountRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MoneyAccountService extends BaseDomainService {

	@Autowired
	private MoneyAccountRepository moneyAccountRepository;

	/**
	 * 註冊儲值帳號
	 * 
	 * @param command
	 * @return MoneyAccountRegisteredData
	 */
	public MoneyAccountRegisteredData register(CreateMoneyAccountCommand command) {
		MoneyAccount moneyAccount = new MoneyAccount();
		moneyAccount.create(command);
		MoneyAccount saved = moneyAccountRepository.save(moneyAccount);
		return this.transformEntityToData(saved, MoneyAccountRegisteredData.class);
	}

	/**
	 * 儲值金額
	 * 
	 * @param command
	 */
	public void deposit(DepositMoneyCommand command) {
		moneyAccountRepository.findById(command.getUuid()).ifPresent(moneyAccount -> {
			BaseEvent event = ContextHolder.getEvent();
			log.debug("Event : {}", event);
			moneyAccount.setBalance(command.getMoney());
			moneyAccountRepository.save(moneyAccount);

		});

	}

	/**
	 * 透過使用者帳號查詢儲值帳號資訊
	 * 
	 * @param username 使用者帳號
	 * @return 儲值帳號資訊
	 */
	public MoneyAccountQueriedData queryAccount(String username) {
		MoneyAccount account = moneyAccountRepository.findByUsername(username);
		
		if (account == null) {
			throw new ValidationException("VALIDATION_EXCEPTION", "查無此使用者資料");
		}
		
		return this.transformEntityToData(account, MoneyAccountQueriedData.class);
	}
}
