package com.example.demo.iface.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.account.command.CreateMoneyAccountCommand;
import com.example.demo.domain.account.command.DepositMoneyCommand;
import com.example.demo.iface.dto.CreateMoneyAccountResource;
import com.example.demo.iface.dto.DepositMoneyResource;
import com.example.demo.iface.dto.MoneyAccountQueriedResource;
import com.example.demo.iface.dto.MoneyAccountRegisteredResource;
import com.example.demo.iface.dto.MoneyDepositedResource;
import com.example.demo.service.MoneyAccountCommandService;
import com.example.demo.service.MoneyAccountQueryService;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/account")
@Tag(name = "Money Account API", description = "進行與儲值帳戶領域相關動作")
public class MoneyAccountController {

	private MoneyAccountQueryService moneyAccountQueryService;
	private MoneyAccountCommandService moneyAccountCommandService;

	/**
	 * 註冊使用者儲值帳號
	 * 
	 * @param resource
	 * @return 成功訊息
	 */
	@PostMapping("/register")
	@Operation(summary = "API - 註冊使用者儲值帳號", description = "註冊使用者儲值帳號。")
	public ResponseEntity<MoneyAccountRegisteredResource> register(
			@Parameter(description = "使用者儲值帳號資訊") @RequestBody CreateMoneyAccountResource resource) {
		// DTO 轉換
		CreateMoneyAccountCommand command = BaseDataTransformer.transformData(resource,
				CreateMoneyAccountCommand.class);

		return new ResponseEntity<MoneyAccountRegisteredResource>(BaseDataTransformer.transformData(
				moneyAccountCommandService.register(command), MoneyAccountRegisteredResource.class), HttpStatus.OK);
	}

	/**
	 * 進行儲值動作
	 * 
	 * @param resource
	 * @return 成功訊息
	 */
	@PostMapping("/deposit")
	@Operation(summary = "API - 進行儲值動作", description = "進行儲值動作。")
	public ResponseEntity<MoneyDepositedResource> deposit(
			@Parameter(description = "儲值資訊") @RequestBody DepositMoneyResource resource) {
		DepositMoneyCommand command = BaseDataTransformer.transformData(resource, DepositMoneyCommand.class);
		return new ResponseEntity<>(BaseDataTransformer.transformData(
				moneyAccountCommandService.publishDepositEvent(command), MoneyDepositedResource.class), HttpStatus.OK);
	}

	/**
	 * 透過使用者帳號查詢儲值帳號資訊
	 * 
	 * @param username
	 * @return 帳戶資訊
	 */
	@GetMapping("")
	@Operation(summary = "API - 透過使用者帳號查詢儲值帳號資訊", description = "透過使用者帳號查詢儲值帳號資訊。")
	public ResponseEntity<MoneyAccountQueriedResource> queryAccount(@RequestParam String username) {
		return new ResponseEntity<>(BaseDataTransformer.transformData(moneyAccountQueryService.queryAccount(username),
				MoneyAccountQueriedResource.class), HttpStatus.OK);
	}

}
