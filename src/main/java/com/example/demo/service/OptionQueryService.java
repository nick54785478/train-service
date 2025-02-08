package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.base.service.BaseApplicationService;
import com.example.demo.domain.service.OptionService;
import com.example.demo.domain.share.OptionQueriedData;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OptionQueryService extends BaseApplicationService{

	private OptionService optionService;
	
	/**
	 * 查詢相關的設定
	 * 
	 * @param type 設定種類
	 * @return List<OptionQueried>
	 */
	public List<OptionQueriedData> getSettingTypes(String type) {
		return optionService.getSettingTypes(type);
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
	 */
	public List<OptionQueriedData> getTrainNoList() {
		return optionService.getTrainNoList();
	}

}
