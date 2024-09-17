package com.example.demo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.config.AuthFeignConfiguration;
import com.example.demo.iface.dto.RegisterUserResource;

@FeignClient(value = "AuthFeignClient", url = "${auth.endpoint.service}", configuration = AuthFeignConfiguration.class)
public interface AuthFeignClient {

	/**
	 * 註冊使用者資料
	 *
	 * @param resource
	 * @return 更新成功訊息
	 */
	@PostMapping(value = "/api/v1/users/register")
	public String register(@RequestBody RegisterUserResource resource);
	
	
}
