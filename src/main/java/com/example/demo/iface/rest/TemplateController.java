package com.example.demo.iface.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.base.exception.ValidationException;
import com.example.demo.domain.template.command.UploadTemplateCommand;
import com.example.demo.iface.dto.TemplateUploadedResource;
import com.example.demo.iface.dto.UploadTemplateResource;
import com.example.demo.service.TemplateCommandService;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/template")
@Tag(name = "Template API", description = "進行與範本領域相關動作")
public class TemplateController {

	private TemplateCommandService templateCommandService;
	
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "API - 上傳範本資料", description = "上傳範本資料。")
	public ResponseEntity<TemplateUploadedResource> uploadTemplate(
			@RequestPart(name = "resource", required = true) UploadTemplateResource resource,
			@RequestPart(name = "file", required = true) MultipartFile file) {
		UploadTemplateCommand command = BaseDataTransformer.transformData(resource, UploadTemplateCommand.class);
		try {
			templateCommandService.upload(command, file);
		} catch (Exception e) {
			log.error("發生錯誤，上傳失敗",e);
			throw new ValidationException("VALIDATE_FAILED", "發生錯誤，上傳失敗");
		}
		return new ResponseEntity<>(new TemplateUploadedResource("201", "上傳成功"), HttpStatus.OK);
	}
}
