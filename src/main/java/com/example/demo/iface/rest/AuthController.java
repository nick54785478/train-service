package com.example.demo.iface.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.share.UserLoginCommand;
import com.example.demo.iface.dto.JwtTokenGettenResource;
import com.example.demo.iface.dto.UserLoginResource;
import com.example.demo.service.outbound.AuthCommandService;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@Tag(name = "Login API", description = "進行會員登入")
@RequestMapping("/login")
public class AuthController {

	private AuthCommandService authCommandService;

	/**
	 * 進行會員登入
	 * 
	 * @param resource
	 * @return JWT Token
	 */
	@PostMapping("")
	@Operation(summary = "API - 進行會員登入", description = "進行會員登入。")
	public ResponseEntity<JwtTokenGettenResource> login(
			@Parameter(description = "登入資訊(帳號、密碼)", required = true) @RequestBody UserLoginResource resource) {
		UserLoginCommand command = BaseDataTransformer.transformData(resource, UserLoginCommand.class);
		return new ResponseEntity<>(
				BaseDataTransformer.transformData(authCommandService.getToken(command), JwtTokenGettenResource.class),
				HttpStatus.OK);
	}
}
