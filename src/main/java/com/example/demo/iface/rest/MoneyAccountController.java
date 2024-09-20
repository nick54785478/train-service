package com.example.demo.iface.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.account.command.CreateMoneyAccountCommand;
import com.example.demo.domain.account.command.DepositMoneyCommand;
import com.example.demo.iface.dto.CreateMoneyAccountResource;
import com.example.demo.iface.dto.DepositMoneyResource;
import com.example.demo.iface.dto.MoneyAccountRegisteredResource;
import com.example.demo.iface.dto.MoneyDepositedResource;
import com.example.demo.service.MoneyAccountCommandService;
import com.example.demo.util.BaseDataTransformer;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/account")
public class MoneyAccountController {

	private MoneyAccountCommandService moneyAccountCommandService;

	/**
	 * 註冊使用者儲值帳號
	 * 
	 * @param resource
	 * @return 成功訊息
	 */
	@PostMapping("/register")
	public ResponseEntity<MoneyAccountRegisteredResource> register(@RequestBody CreateMoneyAccountResource resource) {
		// DTO 轉換
		CreateMoneyAccountCommand command = BaseDataTransformer.transformData(resource,
				CreateMoneyAccountCommand.class);

		return new ResponseEntity<MoneyAccountRegisteredResource>(BaseDataTransformer.transformData(
				moneyAccountCommandService.register(command), MoneyAccountRegisteredResource.class), HttpStatus.OK);
	}

	/**
	 * 儲值
	 * 
	 * @param resource
	 * @return 成功訊息
	 */
	@PostMapping("/deposit")
	public ResponseEntity<MoneyDepositedResource> deposit(@RequestBody DepositMoneyResource resource) {
		DepositMoneyCommand command = BaseDataTransformer.transformData(resource, DepositMoneyCommand.class);
		return new ResponseEntity<>(BaseDataTransformer.transformData(
				moneyAccountCommandService.publishDepositEvent(command), MoneyDepositedResource.class), HttpStatus.OK);
	}

}
