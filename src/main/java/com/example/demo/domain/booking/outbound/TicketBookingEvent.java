package com.example.demo.domain.booking.outbound;

import com.example.demo.base.event.BaseEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TicketBookingEvent extends BaseEvent {
	
	private String action; // 動作

	private String seatNo; // 座號

	private String takeDate; // 乘車日期
	
}
