package com.example.demo.domain.account.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.base.BaseDomainService;
import com.example.demo.base.repository.EventSourceRepository;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.account.command.CreateMoneyAccountCommand;
import com.example.demo.domain.account.command.DepositMoneyCommand;
import com.example.demo.domain.share.dto.MoneyAccountRegisteredData;
import com.example.demo.infra.repository.MoneyAccountRepository;
import com.example.demo.util.JsonParseUtil;

@Service
public class MoneyAccountService extends BaseDomainService {

	@Autowired
	EventSourceRepository eventSourceRepository;
	@Autowired
	MoneyAccountRepository moneyAccountRepository;

	/**
	 * 註冊儲值帳號
	 * 
	 * @param command
	 */
	public MoneyAccountRegisteredData register(CreateMoneyAccountCommand command) {
		MoneyAccount moneyAccount = new MoneyAccount();
		moneyAccount.create(command);
		MoneyAccount saved = moneyAccountRepository.save(moneyAccount);

		// event stream id
		String eventStreamId = saved.getClass().getSimpleName() + "-" + saved.getUuid();
		this.addEventSource(eventStreamId, JsonParseUtil.serialize(saved));

		return this.transformEntityToData(saved, MoneyAccountRegisteredData.class);
	}

	/**
	 * 儲值金額
	 * 
	 * @param command
	 */
	public void deposit(DepositMoneyCommand command) {
		System.out.println("command:" + command);

		moneyAccountRepository.findById(command.getUuid()).ifPresent(e -> {
			e.deposit(command.getMoney());

			// 紀錄 EventSource
			String eventStreamId = e.getClass().getSimpleName() + "-" + e.getUuid();
			this.addEventSource(eventStreamId, JsonParseUtil.serialize(e));

			moneyAccountRepository.save(e);
		});

	}
}
