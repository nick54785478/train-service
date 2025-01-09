package com.example.demo.domain.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.base.enums.YesNo;
import com.example.demo.domain.share.OptionQueried;
import com.example.demo.domain.train.aggregate.vo.TrainKind;
import com.example.demo.infra.repository.SettingRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OptionService {

	private SettingRepository settingRepository;

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
		List<OptionQueried> result = kindMap.values().stream().map(v -> new OptionQueried(null, v.getCode(), v.getLabel()))
				.collect(Collectors.toList());
		return result;
	}
}
