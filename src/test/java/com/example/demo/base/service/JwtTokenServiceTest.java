package com.example.demo.base.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.jsonwebtoken.Claims;

@SpringBootTest
class JwtTokenServiceTest {

	@Autowired
	JwtTokenService jwtTokenService;
	private String jwToken = "";

	@BeforeEach
	void setUp() throws Exception {
		this.jwToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJEQVRBX09XTkVSIl0sImlzcyI6IlNZU1RFTSIsInN1YiI6Im5pY2sxMjNAZXhhbXBsZS5jb20iLCJpYXQiOjE3MjYxMzcyNDQsImV4cCI6MjA0MTQ5NzI0NH0.kxzrgtEifDx7u-IFAl9lHQshyjUYOaHkRfCi7ZWFooY";
	}

	@Test
	void test() {
		Claims tokenBody = jwtTokenService.getTokenBody(jwToken);
		System.out.println(tokenBody);
		assertNotNull(tokenBody);
	}

}
