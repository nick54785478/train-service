package com.example.demo.iface.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoneyDepositedResource {
	
	private String uuid;

	private String username; // 使用者名稱
	
	private BigDecimal balance;	// 餘額
}
