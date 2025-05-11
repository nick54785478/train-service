package com.example.demo.iface.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.base.util.BaseDataTransformer;
import com.example.demo.domain.customisation.command.CreateCustomisationCommand;
import com.example.demo.domain.customisation.command.UpdateCustomisationCommand;
import com.example.demo.domain.customisation.command.UpdateCustomizedValueCommand;
import com.example.demo.iface.dto.CreateCustomisationResource;
import com.example.demo.iface.dto.CustomisationCreatedResource;
import com.example.demo.iface.dto.CustomisationQueriedResource;
import com.example.demo.iface.dto.CustomisationUpdatedResource;
import com.example.demo.iface.dto.CustomizedValueUpdatedResource;
import com.example.demo.iface.dto.UpdateCustomisationResource;
import com.example.demo.iface.dto.UpdateCustomizedValueResource;
import com.example.demo.service.CustomisationCommandService;
import com.example.demo.service.CustomisationQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/customisation")
@Tag(name = "Customission API", description = "進行個人化配置領域相關動作")
public class CustomisationController {

	private CustomisationQueryService customisationQueryService;
	private CustomisationCommandService customisationCommandService;

	/**
	 * 新增個人化配置
	 * 
	 * @param resource
	 * @return 成功訊息
	 */
	@PostMapping("")
	@Operation(summary = "API - 新增個人化配置", description = "新增個人化配置。")
	public ResponseEntity<CustomisationCreatedResource> create(
			@Parameter(description = "個人化配置資訊") @RequestBody CreateCustomisationResource resource) {
		// DTO 轉換
		CreateCustomisationCommand command = BaseDataTransformer.transformData(resource,
				CreateCustomisationCommand.class);
		customisationCommandService.create(command);
		return new ResponseEntity<>(new CustomisationCreatedResource("201", "新增設定成功"), HttpStatus.OK);
	}

	/**
	 * 更新個人化配置
	 * 
	 * @param resource
	 * @return 成功訊息
	 */
	@PutMapping("")
	@Operation(summary = "API - 更新個人化配置", description = "更新個人化配置。")
	public ResponseEntity<CustomisationUpdatedResource> update(
			@Parameter(description = "個人化配置資訊") @RequestBody UpdateCustomisationResource resource) {
		// DTO 轉換
		UpdateCustomisationCommand command = BaseDataTransformer.transformData(resource,
				UpdateCustomisationCommand.class);
		customisationCommandService.update(command);
		return new ResponseEntity<>(new CustomisationUpdatedResource("200", "更新設定成功"), HttpStatus.OK);
	}

	/**
	 * 透過使用者帳號查詢個人化表格參數配置
	 * 
	 * @param username
	 * @param dataType
	 * @param type
	 * @return ResponseEntity<CustomisationQueriedResource>
	 */
	@GetMapping("/{username}")
	@Operation(summary = "API - 透過使用者帳號查詢個人化表格參數配置", description = "透過使用者帳號查詢個人化表格參數配置。")
	public ResponseEntity<CustomisationQueriedResource> queryCustomisations(@PathVariable String username,
			@RequestParam(required = false) String dataType, @RequestParam String type) {
		return new ResponseEntity<>(
				BaseDataTransformer.transformData(customisationQueryService.query(username, dataType, type),
						CustomisationQueriedResource.class),
				HttpStatus.OK);
	}

	/**
	 * 更新該使用者帳號個人化設定的值配置
	 * 
	 * @param username
	 * @param dataType
	 * @param type
	 * @return ResponseEntity<CustomizedValueUpdatedResource>
	 */
	@PutMapping("/{username}")
	@Operation(summary = "API - 更新該使用者帳號個人化設定的值配置", description = "更新該使用者帳號個人化設定的值配置 。")
	public ResponseEntity<CustomizedValueUpdatedResource> updateCustomizedValue(@PathVariable String username,
			@RequestBody UpdateCustomizedValueResource resource) {
		resource.setUsername(username);
		UpdateCustomizedValueCommand command = BaseDataTransformer.transformData(resource,
				UpdateCustomizedValueCommand.class);
		customisationCommandService.updateCustomizedValue(command);
		return new ResponseEntity<>(new CustomizedValueUpdatedResource("200", "更新該使用者帳號的個人化表格參數配置"), HttpStatus.OK);
	}

}
