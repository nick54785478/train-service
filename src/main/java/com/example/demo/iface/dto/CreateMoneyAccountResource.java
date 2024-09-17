package com.example.demo.iface.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMoneyAccountResource {

	private String name;	// 使用者名稱

	private String email; // 信箱

	private String username; // 帳號

	private String password; // 密碼

	private String address;	// 地址
	
	private BigDecimal money;
}
