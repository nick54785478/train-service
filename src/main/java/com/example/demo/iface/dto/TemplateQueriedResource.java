package com.example.demo.iface.dto;

import com.example.demo.base.enums.YesNo;
import com.example.demo.domain.template.aggregate.vo.FileType;
import com.example.demo.domain.template.aggregate.vo.TemplateType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateQueriedResource {

	private Long id;

	private String name;

	private TemplateType type; // 範本種類

	private FileType fileType; // 檔案種類

	private String filePath; // 檔案路徑

	private String fileName; // 檔案名稱

	private YesNo deleteFlag; // 是否刪除
}
