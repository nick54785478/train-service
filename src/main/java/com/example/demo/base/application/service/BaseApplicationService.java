package com.example.demo.base.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.base.application.port.EventPublishPort;
import com.example.demo.base.infra.persistence.EventLogRepository;
import com.example.demo.base.shared.command.PublishEventCommand;
import com.example.demo.base.shared.entity.EventLog;
import com.example.demo.base.shared.enums.EventLogSendQueueStatus;
import com.example.demo.base.shared.event.BaseEvent;
import com.example.demo.util.BaseDataTransformer;
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
	 * @param exchange 交換機
	 * @param topic    Topic 通道
	 * @param event    事件
	 */
	public void publishEvent(String topic, BaseEvent event) {
		PublishEventCommand<BaseEvent> publishCommand = PublishEventCommand.<BaseEvent>builder().event(event)
				.topic(topic).build();
		rabbitmqService.publish(publishCommand);
	}

	/**
	 * 建立 EventLog
	 * 
	 * @param topic Topic 通道
	 * @param event 事件
	 * @param body  事件發生變動內容
	 */
	public EventLog generateEventLog(String topic, BaseEvent event) {

		// 建立 EventLog
		return EventLog.builder().uuid(UUID.randomUUID().toString()) // 不再依賴 event
				.topic(topic).userId("SYSTEM").className(event.getClass().getName())
				.targetId(event.getTargetId()).txId(event.getEventTxId()) // 關聯 transaction
				.body(JsonParseUtil.serialize(event)).status(EventLogSendQueueStatus.INITIAL).build();
	}

}
