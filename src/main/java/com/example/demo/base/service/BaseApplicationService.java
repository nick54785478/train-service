package com.example.demo.base.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.base.entity.EventLog;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.port.EventPublishPort;
import com.example.demo.base.repository.EventLogRepository;
import com.example.demo.base.util.BaseDataTransformer;
import com.example.demo.util.JsonParseUtil;

/**
 * Base Application Service
 */
@Service
public abstract class BaseApplicationService {

	@Autowired
	protected EventPublishPort rabbitmqService;
	@Autowired
	protected EventLogRepository eventLogRepository;

	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * 
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
	 * 
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

	/**
	 * 建立 EventLog
	 * 
	 * @param topicQueue   Topic 通道
	 * @param eventLogUuid EventLog 的 UUID
	 * @param targetId     目標物 UUID
	 * @param event        事件
	 * @param body         事件發生變動內容
	 */
	public EventLog generateEventLog(String topicQueue, String eventLogUuid, String targetId, BaseEvent event) {

		// 建立 EventLog
		EventLog eventLog = EventLog.builder().uuid(eventLogUuid).topic(topicQueue).targetId(targetId)
				.className(event.getClass().getName()).body(JsonParseUtil.serialize(event)).userId("SYSTEM").build();

		return eventLogRepository.save(eventLog);
	}

}
