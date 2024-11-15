package com.example.demo.iface.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoneyAccountQueriedResource {

	private String uuid; // 使用者 uuid

	private String name; // 人名

	private String username; // 帳號

	private String email; // email

	private BigDecimal balance = new BigDecimal("0"); // 餘額
}
