package com.example.demo.iface.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "車票退票資訊")
public class RefundTicketResource {

	@Schema(description = "預定代號 uuid")
	private String uuid; // booking uuid

	@Schema(description = "座號")
	private String seatNo; // 座號

	@Schema(description = "搭乘日期")
	private String takeDate; // 搭乘日期

}
