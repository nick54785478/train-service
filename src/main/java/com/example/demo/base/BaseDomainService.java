package com.example.demo.base;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.base.context.ContextHolder;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.event.EventSource;
import com.example.demo.base.repository.EventSourceRepository;
import com.example.demo.util.BaseDataTransformer;
import com.example.demo.util.JsonParseUtil;

/**
 * Base Domain Service
 */
@Service
public abstract class BaseDomainService {

	@Autowired
	EventSourceRepository eventSourceRepository;

	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * 
	 * @param <T>
	 * 
	 * @param target 目標物件
	 * @param clazz  欲轉換的型別
	 * @return 轉換後的物件
	 */
	public <T> T transformEntityToData(Object target, Class<T> clazz) {
		return BaseDataTransformer.transformData(target, clazz);
	}

	/**
	 * 呼叫 BaseDataTransformer 進行資料轉換
	 * 
	 * @param <S,    T>
	 * 
	 * @param target 目標物件列表
	 * @param clazz  欲轉換的型別
	 * @return 轉換後的物件列表
	 */
	public <S, T> List<T> transformEntityToData(List<S> target, Class<T> clazz) {
		return BaseDataTransformer.transformData(target, clazz);
	}

	/**
	 * 紀錄 Event Source
	 * 
	 * @param eventStreamId Event Stream Id (格式: Aggregate Type-Aggregate ID)
	 * @param body Aggregate 資料
	 */
	public void addEventSource(String eventStreamId, String body) {
		// 取得 Event
		BaseEvent event = ContextHolder.getEvent();

		// 紀錄 EventSourcing
		EventSource eventSource = EventSource.builder().uuid(eventStreamId).targetId(event.getTargetId())
				.className(event.getClass().getName()).body(body).userId("SYSTEM").version(1L)
				.build();
		eventSourceRepository.save(eventSource);
	}
}
