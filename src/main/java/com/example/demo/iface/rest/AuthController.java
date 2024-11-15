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

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/login")
public class AuthController {

	private AuthCommandService authCommandService;

	@PostMapping("")
	public ResponseEntity<JwtTokenGettenResource> login(@RequestBody UserLoginResource resource) {
		UserLoginCommand command = BaseDataTransformer.transformData(resource, UserLoginCommand.class);
		return new ResponseEntity<>(
				BaseDataTransformer.transformData(authCommandService.getToken(command), JwtTokenGettenResource.class),
				HttpStatus.OK);
	}
}
