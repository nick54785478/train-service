package com.example.demo.iface.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStopResource {

	private Integer seq;  // 停站順序
	
	private String stopName;	// 站名
	
	private String stopTime;	// 停靠時間
}
