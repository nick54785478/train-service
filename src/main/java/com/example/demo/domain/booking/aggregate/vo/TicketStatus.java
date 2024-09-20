package com.example.demo.domain.booking.aggregate.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum TicketStatus {

	UNTAKEN("UNTAKEN", "尚未搭乘"), TAKEN("TAKEN", "已搭乘"), REFUNDED("REFUNDED", "取消訂位");

	@Getter
	private final String name;
	
	@Getter
	private final String value;
}
