package com.example.demo.iface.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.iface.dto.OptionQueriedResource;
import com.example.demo.service.OptionQueryService;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/options")
@Tag(name = "Option API", description = "下拉式選單 API")
public class OptionController {

	private OptionQueryService optionQueryService;

	/**
	 * 取得火車種類的下拉式選單資料
	 * 
	 * @return List<OptionResource>
	 */
	@GetMapping("/trainKinds")
	@Operation(summary = "API - 取得火車種類的下拉式選單資料", description = "取得火車種類的下拉式選單資料。")
	public ResponseEntity<List<OptionQueriedResource>> getTrainKinds() {
		return new ResponseEntity<>(
				BaseDataTransformer.transformData(optionQueryService.getTrainKinds(), OptionQueriedResource.class),
				HttpStatus.OK);
	}
	
	/**
	 * 取得車票種類的下拉式選單資料
	 * 
	 * @return List<OptionResource>
	 */
	@GetMapping("/ticketTypes")
	@Operation(summary = "API - 取得車票種類的下拉式選單資料", description = "取得車票種類的下拉式選單資料。")
	public ResponseEntity<List<OptionQueriedResource>> getTicketTypes() {
		return new ResponseEntity<>(
				BaseDataTransformer.transformData(optionQueryService.getTicketTypes(), OptionQueriedResource.class),
				HttpStatus.OK);
	}

	/**
	 * 查詢相關 DataType 的設定 (下拉式選單)
	 * 
	 * @param type 設定種類
	 * @return ResponseEntity<List<OptionQueriedResource>>
	 */
	@GetMapping("/query")
	public ResponseEntity<List<OptionQueriedResource>> query(@RequestParam String type) {
		return new ResponseEntity<>(BaseDataTransformer.transformData(optionQueryService.getSettingTypes(type),
				OptionQueriedResource.class), HttpStatus.OK);
	}

	/**
	 * 查詢車次的下拉式選單資料
	 * 
	 * @return ResponseEntity<List<OptionQueriedResource>>
	 */
	@GetMapping("/trainNoList")
	public ResponseEntity<List<OptionQueriedResource>> getTrainNoList() {
		return new ResponseEntity<>(
				BaseDataTransformer.transformData(optionQueryService.getTrainNoList(), OptionQueriedResource.class),
				HttpStatus.OK);
	}
}
