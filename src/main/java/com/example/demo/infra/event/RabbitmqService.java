package com.example.demo.infra.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.base.event.BaseEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RabbitmqService {

	@Autowired
	RabbitTemplate rabbitTemplate;

	/**
	 * 發布事件到 Rabbit MQ
	 * 
	 * @param exchange   交換機
	 * @param topicQueue Topic 通道
	 * @param event      事件
	 */
	public void publish(String exchangeName, String topicQueue, BaseEvent event) {
		log.debug("Exchange:{}, Topic:{}, Event Body:{}", exchangeName, topicQueue, event);
		rabbitTemplate.convertAndSend(exchangeName, topicQueue, event);
	}
}
