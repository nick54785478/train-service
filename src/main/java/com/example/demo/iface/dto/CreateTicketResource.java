package com.example.demo.iface.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "車票資訊")
public class CreateTicketResource {

	@Schema(description = "火車代號 uuid")
	private Integer trainNo;

	@Schema(description = "起站")
	private String fromStop;

	@Schema(description = "迄站")
	private String toStop;
	
	@Schema(description = "車票種類")
	private String ticketType;

	@Schema(description = "價格")
	private BigDecimal price;
}
