package com.example.demo.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.base.event.BaseEvent;
import com.example.demo.infra.event.RabbitmqService;

/**
 * Base Application Service
 */
@Service
public abstract class BaseApplicationService {

	@Autowired
	private RabbitmqService rabbitmqService;

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
