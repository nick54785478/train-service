package com.example.demo.domain.account.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.base.BaseDomainService;
import com.example.demo.base.exception.ValidationException;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.account.command.CreateMoneyAccountCommand;
import com.example.demo.domain.account.command.DepositMoneyCommand;
import com.example.demo.domain.share.dto.MoneyAccountRegisteredData;
import com.example.demo.domain.share.dto.MoneyDepositedRegisteredData;
import com.example.demo.infra.repository.MoneyAccountRepository;

@Service
public class MoneyAccountService extends BaseDomainService {

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
		return this.transformEntityToData(saved, MoneyAccountRegisteredData.class);
	}
	
	/**
	 * 儲值金額
	 * 
	 * @param command
	 * */
	public MoneyDepositedRegisteredData deposit(DepositMoneyCommand command) {
		Optional<MoneyAccount> op = moneyAccountRepository.findById(command.getUuid());
		
		if (op.isPresent()) {
			MoneyAccount account = op.get();
			account.deposit(command.getMoney());
			MoneyAccount saved = moneyAccountRepository.save(account);
			return this.transformEntityToData(saved, MoneyDepositedRegisteredData.class);
		}
		
		throw new ValidationException("VALIDATE_FAILED", "加值失敗，拋出例外");		
	}
}
