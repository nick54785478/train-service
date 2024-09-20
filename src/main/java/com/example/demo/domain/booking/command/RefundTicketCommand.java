package com.example.demo.domain.booking.command;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundTicketCommand {

	private String uuid; // booking uuid
	
	private String seatNo; // 座號
	
	private LocalDate takeDate;	// 搭乘日期

}
