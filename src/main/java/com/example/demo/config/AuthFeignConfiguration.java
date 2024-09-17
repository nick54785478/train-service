package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class AuthFeignConfiguration {

	/**
	 * 定義一個 Feign 的請求攔截器，用於在每次發送請求時增加 Header 資料。
	 *
	 * @return 請求攔截器
	 */
	@Bean
	public RequestInterceptor requestCpqCommonTokenInterceptor() {

		return new RequestInterceptor() {
			@Override
			public void apply(RequestTemplate requestTemplate) {
				// 可在此處新增 Request Header
			}
		};
	}
}
