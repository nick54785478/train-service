package com.example.demo.iface.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookTicketResource {

	private String trainUuid; // 火車 UUID

	private String ticketUuid; // 車票 UUID

	private Integer trainNo; // 火車號次

	private String seatNo; // 座號

	private String takeDate; // 乘車日期
}
