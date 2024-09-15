package com.example.demo.iface.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StopQueriedResource {
	
	private Integer seq;

	private String stopName;
	
	private String stopTime;
}
