package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.domain.service.OptionService;
import com.example.demo.domain.share.OptionQueried;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OptionQueryService {

	private OptionService optionService;
	
	/**
	 * 查詢相關的設定
	 * 
	 * @param type 設定種類
	 * @return List<OptionQueried>
	 */
	public List<OptionQueried> getSettingTypes(String type) {
		return optionService.getSettingTypes(type);
	}

	/**
	 * 取得火車類別
	 * 
	 * @return List<OptionQueried>
	 */
	public List<OptionQueried> getTrainKinds() {
		return optionService.getTrainKinds();
	}
	
	/**
	 * 取得火車車次資料 (下拉式選單)
	 */
	public List<OptionQueried> getTrainNoList() {
		return optionService.getTrainNoList();
	}

}
