package com.example.demo.iface.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopSummaryQueriedResource {

	private Integer seq; // 停站順序

	private String name; // 站名

	private String time; // 停站時間
	
}
