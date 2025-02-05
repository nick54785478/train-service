package com.example.demo.service;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.base.enums.YesNo;
import com.example.demo.base.service.BaseApplicationService;
import com.example.demo.domain.service.TicketService;
import com.example.demo.domain.service.TrainService;
import com.example.demo.domain.setting.aggregate.ConfigurableSetting;
import com.example.demo.domain.share.enums.TrainSheetMapping;
import com.example.demo.domain.ticket.command.CreateTicketCommand;
import com.example.demo.domain.train.command.CreateStopCommand;
import com.example.demo.domain.train.command.CreateTrainCommand;
import com.example.demo.domain.train.command.UpdateTrainCommand;
import com.example.demo.infra.repository.SettingRepository;
import com.example.demo.util.ExcelUtil;
import com.example.demo.util.ParameterMappingUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
@AllArgsConstructor
public class TrainCommandService extends BaseApplicationService {

	private TrainService trainService;
	private TicketService ticketService;
	private SettingRepository settingRepository;

	/**
	 * 新增火車資訊
	 * 
	 * @param command
	 * @return UUID
	 */
	public void createTrain(CreateTrainCommand command) {
		trainService.create(command);
	}

	/**
	 * 更新火車資訊
	 * 
	 * @param command
	 * @return UUID
	 */
	public void updateTrain(UpdateTrainCommand command) {
		trainService.update(command);
	}

	/**
	 * 上傳 Excel 資料，並更新或新增車次資料
	 * 
	 * @param mapping
	 * @param sheetMapping
	 * @param file
	 * @throws IOException
	 */
	public void upload(String mapping, String sheetMapping, MultipartFile file) throws IOException {
		// 取得 SheetName 清單
		List<String> sheetNameList = settingRepository.findByTypeAndActiveFlag(sheetMapping, YesNo.Y).stream()
				.map(ConfigurableSetting::getValue).collect(Collectors.toList());
		// 將 Excel 資料進行轉換
		Map<String, List<Map<String, String>>> excelData = ExcelUtil.readExcelData(file.getInputStream(),
				sheetNameList);
		// 取得 DataMapping 資料
		List<ConfigurableSetting> mappings = settingRepository.findByTypeAndActiveFlag(mapping, YesNo.Y);

		// 處理資料為 Command
		Map<String, Object> processUploadData = this.processUploadData(excelData, mappings);
		log.info("processUploadData: {}", processUploadData);
		// 儲存處理後的資料
		this.saveProcessedData(processUploadData);
	}

	/**
	 * 儲存處理後的資料
	 * 
	 * @param 處理後的上傳資料
	 */
	public void saveProcessedData(Map<String, Object> processedData) {

		CreateTrainCommand trainCommand = (CreateTrainCommand) processedData.get(TrainSheetMapping.TRAIN.getName());

		List<CreateStopCommand> stopCommands = (List<CreateStopCommand>) processedData
				.get(TrainSheetMapping.STOPS.getName());
		trainCommand.setStops(stopCommands);

		List<CreateTicketCommand> ticketCommands = (List<CreateTicketCommand>) processedData
				.get(TrainSheetMapping.TICKETS.getName());

		ticketCommands = ticketCommands.stream().map(e -> {
			e.setTrainNo(trainCommand.getTrainNo());
			return e;
		}).toList();

		// 呼叫 Domain Service 將資料存入 Aggregate
		this.createTrainAndTickets(trainCommand, ticketCommands);
	}

	/**
	 * 呼叫 Domain Service 將資料存入 Aggregate
	 * 
	 * @param createTrainCommand
	 * @param createTicketCommands
	 */
	public void createTrainAndTickets(CreateTrainCommand createTrainCommand,
			List<CreateTicketCommand> createTicketCommands) {
		trainService.create(createTrainCommand);
		ticketService.create(createTicketCommands);
	}

	/**
	 * 處理上傳資料
	 * 
	 * @param excelData   經處理後的 Excel 資料 Map<SheetName, List<Map<Header, Value>>>
	 * @param dataMapping DataMapping
	 * @return Map<String, Object> 整理後的資料
	 */
	public Map<String, Object> processUploadData(Map<String, List<Map<String, String>>> excelData,
			List<ConfigurableSetting> dataMapping) {
		Map<String, Object> resMap = new HashMap<>();

		// 將 Data Mapping 轉為 Map<欄位名, field>
		Map<String, String> mapping = dataMapping.stream()
				.collect(Collectors.toMap(ConfigurableSetting::getName, ConfigurableSetting::getValue));

		log.info("excelData:{}", excelData);
		// 遍歷 Map<SheetName, List<Map<Header, Value>>>
		excelData.forEach((k, v) -> {

			Map<String, String> processMap = new HashedMap<>();
			// 車次資料處理
			if (StringUtils.equals(k, TrainSheetMapping.TRAIN.getName())) {
				// Map<field, 值>

				// 遍歷 List<Map<Header, Value>>
				v.stream().forEach(map -> {
					// 遍歷 Map<Header, Value>
					map.forEach((header, value) -> {
						if (!Objects.isNull(mapping.get(header))) {
							// 取得 Field ( Entity 的 Field)
							var field = mapping.get(header);
							processMap.put(field, value);
						}
					});
				});
				CreateTrainCommand train = new CreateTrainCommand();
				ParameterMappingUtil.setFieldsFromMap(train, processMap);
				resMap.put(k, train);
			}

			// 車站資料處理
			if (StringUtils.equals(k, TrainSheetMapping.STOPS.getName())) {
				List<CreateStopCommand> stops = new ArrayList<>();

				// 遍歷 List<Map<Header, Value>>
				v.stream().forEach(map -> {
					// 遍歷 Map<Header, Value>
					map.forEach((header, value) -> {
						// 裝 Stop 資料
						if (!Objects.isNull(mapping.get(header))) {
							// 取得 Field ( Entity 的 Field)
							var field = mapping.get(header);
							processMap.put(field, value);
						}
					});
					CreateStopCommand stop = new CreateStopCommand();
					ParameterMappingUtil.setFieldsFromMap(stop, processMap);
					stop.setStopTime(this.processLocalTime(stop.getStopTime()));
					stops.add(stop);
				});
				resMap.put(k, stops);
			}

			// 車票資料處理
			if (StringUtils.equals(k, TrainSheetMapping.TICKETS.getName())) {
				List<CreateTicketCommand> tickets = new ArrayList<>();

				// 遍歷 List<Map<Header, Value>>
				v.stream().forEach(map -> {
					// 遍歷 Map<Header, Value>
					map.forEach((header, value) -> {
						if (!Objects.isNull(mapping.get(header))) {
							// 取得 Field ( Entity 的 Field)
							var field = mapping.get(header);
							processMap.put(field, value);
						}
					});
					CreateTicketCommand ticket = new CreateTicketCommand();
					ParameterMappingUtil.setFieldsFromMap(ticket, processMap);
					tickets.add(ticket);
				});
				resMap.put(k, tickets);
			}
		});
		return resMap;
	}

	/**
	 * 從 Excel 轉換後的 LocalTime 為毫秒格式，需再轉換
	 * 
	 * @param stopTime
	 */
	public String processLocalTime(String stopTime) {
		// LocalTime 需額外處理
		double fraction = Double.parseDouble(stopTime);
		LocalTime parsedTime = LocalTime.ofNanoOfDay((long) (fraction * 24 * 60 * 60 * 1_000_000_000L));
		// 轉換 LocalTime 為 "HH:mm:ss" 格式的字串
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		return parsedTime.format(formatter);
	}
}
