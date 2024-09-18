package com.example.demo.domain.share.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoneyAccountRegisteredData {
	
	private String uuid;

	private String username; // 使用者名稱
	
	private String email;
	
	private BigDecimal balance;	// 餘額
	
}
