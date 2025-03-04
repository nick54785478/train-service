package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.base.service.BaseApplicationService;
import com.example.demo.domain.service.OptionService;
import com.example.demo.domain.share.OptionQueriedData;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OptionQueryService extends BaseApplicationService {

	private OptionService optionService;

	/**
	 * 透過 DataType 查詢相關的設定
	 * 
	 * @param dataType 設定種類
	 * @return List<OptionQueried>
	 */
	public List<OptionQueriedData> getSettingsByDataType(String dataType) {
		return optionService.getSettingsByDataType(dataType);
	}

	/**
	 * 取得火車類別
	 * 
	 * @return List<OptionQueried>
	 */
	public List<OptionQueriedData> getTrainKinds() {
		return optionService.getTrainKinds();
	}

	/**
	 * 取得車票種類
	 * 
	 * @return List<OptionQueried>
	 */
	public List<OptionQueriedData> getTicketTypes() {
		return optionService.getTicketTypes();
	}

	/**
	 * 取得火車車次資料 (下拉式選單)
	 * 
	 * @return List<OptionQueried>
	 */
	public List<OptionQueriedData> getTrainNoList() {
		return optionService.getTrainNoList();
	}

	/**
	 * 透過 Type 查詢相關的設定
	 * 
	 * @param dataType
	 * @param type
	 * @return List<OptionQueried>
	 */
	public List<OptionQueriedData> getSettingsByDataTypeAndType(String dataType, String type) {
		return optionService.getSettingsByDataTypeAndType(dataType, type);
	}

}
