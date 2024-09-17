package com.example.demo.base;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.base.event.BaseEvent;
import com.example.demo.infra.event.RabbitmqService;
import com.example.demo.util.BaseDataTransformer;

/**
 * Base Application Service
 */
@Service
public abstract class BaseApplicationService {

	@Autowired
	private RabbitmqService rabbitmqService;
	
	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * @param <T>
	 * @param target 目標物件
	 * @param clazz  欲轉換的型別
	 * @return 轉換後的物件
	 */
	public <T> T transformData(Object target, Class<T> clazz) {
		return BaseDataTransformer.transformData(target, clazz);
	}

	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * @param <S,    T>
	 * @param target 目標物件列表
	 * @param clazz  欲轉換的型別
	 * @return 轉換後的物件列表
	 */
	public <S, T> List<T> transformData(List<S> target, Class<T> clazz) {
		return BaseDataTransformer.transformData(target, clazz);
	}

	/**
	 * 發布事件 (Event)
	 * 
	 * @param exchange   交換機
	 * @param topicQueue Topic 通道
	 * @param event      事件
	 */
	public void publishEvent(String exchangeName, String topicQueue, BaseEvent event) {
		rabbitmqService.publish(exchangeName, topicQueue, event);
	}
}
