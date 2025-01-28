package com.example.demo.domain.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.base.enums.YesNo;
import com.example.demo.domain.share.OptionQueried;
import com.example.demo.domain.train.aggregate.Train;
import com.example.demo.domain.train.aggregate.vo.TrainKind;
import com.example.demo.infra.repository.SettingRepository;
import com.example.demo.infra.repository.TrainRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OptionService {

	private SettingRepository settingRepository;
	private TrainRepository trainRepository;

	/**
	 * 查詢相關的設定 (dropdown)
	 * 
	 * @param type 設定種類
	 * @return List<OptionQueried>
	 */
	public List<OptionQueried> getSettingTypes(String dataType) {
		return settingRepository.findByDataTypeAndActiveFlag(dataType, YesNo.Y).stream().map(setting -> {
			return new OptionQueried(setting.getId(), setting.getType(), setting.getType());
		}).collect(Collectors.toList());
	}

	/**
	 * 取得火車類別
	 * 
	 * @return List<OptionQueried>
	 */
	public List<OptionQueried> getTrainKinds() {
		Map<String, TrainKind> kindMap = TrainKind.getMap();
		return kindMap.values().stream()
				.map(v -> new OptionQueried(null, v.getCode(), v.getLabel())).collect(Collectors.toList());
	}

	/**
	 * 取得火車車次資料 (下拉式選單)
	 */
	public List<OptionQueried> getTrainNoList() {
		return trainRepository.findAll().stream().map(Train::getNumber).distinct().map(e -> {
			return new OptionQueried(null, String.valueOf(e) ,String.valueOf(e));
		}).distinct().collect(Collectors.toList());
		
	}
}
