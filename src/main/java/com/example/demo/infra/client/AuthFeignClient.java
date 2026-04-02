package com.example.demo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.config.AuthFeignConfiguration;
import com.example.demo.domain.share.UserLoginCommand;
import com.example.demo.iface.dto.JwtTokenGettenResource;
import com.example.demo.iface.dto.RegisterUserResource;
import com.example.demo.iface.dto.UserInfoResource;
import com.example.demo.iface.dto.UserRegisteredResource;

@FeignClient(value = "AuthFeignClient", url = "${auth.endpoint.service}", configuration = AuthFeignConfiguration.class)
public interface AuthFeignClient {

	/**
	 * 註冊使用者資料
	 *
	 * @param resource
	 * @return 更新成功訊息
	 */
	@PostMapping(value = "/api/v1/users/register")
	public UserRegisteredResource register(@RequestBody RegisterUserResource resource);
	
	/**
	 * 登入功能
	 * 
	 * @param resource
	 * @return JwToken
	 * */
	@PostMapping(value = "/api/v1/login")
	public JwtTokenGettenResource login(@RequestBody UserLoginCommand command);

	/**
	 * 透過 email 取得 User 資料
	 * 
	 * @param request
	 * @return 使用者資料
	 */
	@GetMapping(value = "/api/v1/users/queryByEmail")
	public UserInfoResource getUserByEmail(@RequestParam(value = "email", required = false) String email);

}
