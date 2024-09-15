package com.example.demo.iface.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketResource {

	private Integer trainNo;

	private String fromStop;

	private String toStop;

	private BigDecimal price;
}
