package com.example.demo.iface.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.train.command.CreateTrainCommand;
import com.example.demo.domain.train.command.QueryTrainCommand;
import com.example.demo.iface.dto.CreateTrainResource;
import com.example.demo.iface.dto.TrainCreatedResource;
import com.example.demo.iface.dto.TrainDetailQueriedResource;
import com.example.demo.iface.dto.TrainQueriedResource;
import com.example.demo.iface.dto.TrainSummaryQueriedResource;
import com.example.demo.service.TrainCommandService;
import com.example.demo.service.TrainQueryService;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;

@Valid
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/train")
@Tag(name = "Train API", description = "進行與火車領域相關動作")
public class TrainController {

	private TrainQueryService trainQueryService;
	private TrainCommandService trainCommandService;

	/**
	 * 新增火車車次
	 * 
	 * @param resource
	 * @return 成功訊息
	 */
	@PostMapping("")
	@Operation(summary = "API - 新增火車車次", description = "新增火車車次。")
	public ResponseEntity<TrainCreatedResource> create(
			@Parameter(description = "火車車次資訊") @RequestBody CreateTrainResource resource) {
		// DTO 轉換
		CreateTrainCommand command = BaseDataTransformer.transformData(resource, CreateTrainCommand.class);
		trainCommandService.createTrain(command);
		return new ResponseEntity<TrainCreatedResource>(new TrainCreatedResource("201", "新增車次成功"), HttpStatus.OK);
	}

	/**
	 * 取得該火車車次的資訊
	 * 
	 * @param trainNo
	 * @return 該火車車次的停靠站資訊
	 */
	@Valid
	@GetMapping("/{trainNo}/stops")
	@Operation(summary = "API - 取得該火車車次的資訊", description = "取得該火車車次的資訊。")
	public ResponseEntity<TrainQueriedResource> query(
			@Parameter(description = "火車車次") @Valid @PathVariable @Min(value = 1, message = "車次必須為正整數") Integer trainNo) {
		return new ResponseEntity<>(BaseDataTransformer.transformData(trainQueryService.queryTrainData(trainNo),
				TrainQueriedResource.class), HttpStatus.OK);
	}

	/**
	 * 查詢符合條件的火車資訊(訂票查詢)
	 * 
	 * @param trainNo   車次
	 * @param trainKind 車種
	 * @param fromStop  起站
	 * @param toStop    迄站
	 * @param takeDate  出發日期
	 * @param time      出發時間
	 * @return 該火車車次的停靠站資訊
	 */
	@GetMapping("")
	@Operation(summary = "API - 查詢符合條件的火車資訊(訂票查詢)", description = "查詢符合條件的火車資訊(訂票查詢)。")
	public ResponseEntity<List<TrainDetailQueriedResource>> getTrainInfo(
			@Parameter(description = "車次") @RequestParam(required = false) Integer trainNo,
			@Parameter(description = "車種") @RequestParam(required = false) String trainKind,
			@Parameter(description = "起站") @RequestParam String fromStop,
			@Parameter(description = "迄站") @RequestParam String toStop,
			@Parameter(description = "搭乘日期 (yyyy-mm-dd)") @Valid @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String takeDate,
			@Parameter(description = "搭乘時間 (hh:mm)") @RequestParam String time) {
		QueryTrainCommand command = new QueryTrainCommand(trainNo, trainKind, fromStop, toStop, takeDate, time);
		return new ResponseEntity<>(BaseDataTransformer.transformData(trainQueryService.queryTrainInfo(command),
				TrainDetailQueriedResource.class), HttpStatus.OK);
	}

	/**
	 * 查詢符合條件的火車資訊
	 * 
	 * @param trainNo   車次
	 * @param trainKind 車種
	 * @param fromStop  起站
	 * @param toStop    迄站
	 * @param takeDate  出發日期
	 * @param time      出發時間
	 * @return 該火車車次的停靠站資訊
	 */
	@Valid
	@GetMapping("/summary")
	@Operation(summary = "API - 查詢符合條件的火車資訊", description = "查詢符合條件的火車資訊。")
	public ResponseEntity<List<TrainSummaryQueriedResource>> queryTrainSummary(
			@Parameter(description = "車次") @RequestParam(required = false) Integer trainNo,
			@Parameter(description = "車種") @RequestParam(required = false) String trainKind,
			@Parameter(description = "起站") @RequestParam String fromStop,
			@Parameter(description = "迄站") @RequestParam String toStop,
			@Parameter(description = "搭乘日期 (yyyy-mm-dd)") @Valid @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String takeDate,
			@Parameter(description = "搭乘時間 (hh:mm)") @RequestParam String time) {
		QueryTrainCommand command = new QueryTrainCommand(trainNo, trainKind, fromStop, toStop, takeDate, time);
		return new ResponseEntity<>(BaseDataTransformer.transformData(trainQueryService.queryTrainSummary(command),
				TrainSummaryQueriedResource.class), HttpStatus.OK);
	}

}
