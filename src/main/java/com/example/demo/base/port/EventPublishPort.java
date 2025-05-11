package com.example.demo.base.port;

import com.example.demo.base.event.BaseEvent;

public interface EventPublishPort {

	/**
	 * 發布事件
	 * 
	 * @param exchange   交換機
	 * @param topicQueue Topic 通道
	 * @param event      事件
	 */
	void publish(String exchangeName, String topicQueue, BaseEvent event);

}
