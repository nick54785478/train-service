package com.example.demo.domain.setting.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSettingCommand {
	
	private Long id;
	
	private String dataType; // 資料種類

	private String type; // 種類

	private String name; // 名稱

	private String description; // 敘述

	private String activeFlag;

}
