package com.example.demo.base;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.event.EventLog;
import com.example.demo.base.event.EventSource;
import com.example.demo.base.repository.EventLogRepository;
import com.example.demo.base.repository.EventSourceRepository;
import com.example.demo.infra.event.RabbitmqService;
import com.example.demo.util.BaseDataTransformer;
import com.example.demo.util.JsonParseUtil;

/**
 * Base Application Service
 */
@Service
public abstract class BaseApplicationService {

	@Autowired
	private RabbitmqService rabbitmqService;
	@Autowired
	private EventLogRepository eventLogRepository;
	@Autowired
	private EventSourceRepository eventSourceRepository;

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
	 * @param topicQueue Topic 通道
	 * @param targetId   目標物 UUID
	 * @param event      事件
	 * @param body       事件發生變動內容
	 */
	public EventLog generateEventLog(String topicQueue, String targetId, BaseEvent event) {
		// 建立 EventLog UUID
		String eventLogUuid = UUID.randomUUID().toString();

		// 建立 EventLog
		EventLog eventLog = EventLog.builder().uuid(eventLogUuid).topic(topicQueue).targetId(targetId)
				.className(event.getClass().getName()).body(JsonParseUtil.serialize(event)).userId("SYSTEM").build();
		event.setEventLogUuid(eventLogUuid);

		return eventLogRepository.save(eventLog);
	}

	/**
	 * 紀錄 EventSource
	 * 
	 * @param aggregateId 聚合ID
	 * @param event       事件
	 * @param body        事件發生變動內容
	 */
	public void saveEventSource(String aggregateId, BaseEvent event, String body) {
		// 紀錄 EventSourcing
		EventSource eventSource = EventSource.builder().uuid(aggregateId).className(event.getClass().getName())
				.body(body).userId("SYSTEM").version(1L).build();
		eventSourceRepository.save(eventSource);
	}
}
