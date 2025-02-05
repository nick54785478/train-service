package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.base.service.BaseApplicationService;
import com.example.demo.domain.service.TemplateService;
import com.example.demo.domain.template.command.UploadTemplateCommand;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public class TemplateCommandService extends BaseApplicationService {

	private TemplateService templateService;

	/**
	 * 上傳範本資料
	 * 
	 * @param command
	 * @param file
	 * @throws Exception
	 */
	public void upload(UploadTemplateCommand command, MultipartFile file) throws Exception {
		templateService.upload(command, file);
	}
}
