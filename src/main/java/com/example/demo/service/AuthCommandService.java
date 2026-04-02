package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.domain.share.UserLoginCommand;
import com.example.demo.iface.dto.res.JwtTokenGettenResource;
import com.example.demo.infra.client.AuthFeignClient;

@Service
public class AuthCommandService {

	@Autowired
	private AuthFeignClient client;

	/**
	 * 對 Auth Service 索取 Token
	 * 
	 * @param command
	 */
	public JwtTokenGettenResource getToken(UserLoginCommand command) {
		JwtTokenGettenResource resource = client.login(command);
		return resource;
	}

}



