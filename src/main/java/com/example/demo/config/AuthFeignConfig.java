package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class AuthFeignConfig {

	/**
	 * 定義一個 Feign 的請求攔截器，用於在每次發送請求時添加訂閱金鑰 Header。
	 *
	 * @return 請求攔截器
	 */
	@Bean
	public RequestInterceptor requestCpqCommonTokenInterceptor() {
		// 可以將匿名內部類轉換為 Lambda 表達式，但為了好閱讀沒有進行調整
		return new RequestInterceptor() {
			@Override
			public void apply(RequestTemplate requestTemplate) {
				// requestTemplate.header(APIM_SUBSCRIPTION_HEADER, subcriptionKey);
			}
		};
	}
}
