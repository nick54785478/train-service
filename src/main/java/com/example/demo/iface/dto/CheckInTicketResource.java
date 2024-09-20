package com.example.demo.iface.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInTicketResource {

	private String uuid; // booking uuid

	private String seatNo; // 座號

	private String takeDate; // 搭乘日期

}
