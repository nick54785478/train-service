package com.example.demo.domain.share;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptionQueried {

	private Long id;

	private String label;

	private String value;

	private String labelTw;

	private String labelCn;

	private String labelUs;
	

	public OptionQueried(Long id, String label, String value) {
		this.id = id;
		this.label = label;
		this.value = value;
	}

}
