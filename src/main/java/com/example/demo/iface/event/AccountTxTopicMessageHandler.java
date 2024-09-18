package com.example.demo.iface.event;

import java.io.IOException;
import java.util.Objects;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.base.BaseEventHandler;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.event.EventLog;
import com.example.demo.base.event.EventSource;
import com.example.demo.base.repository.EventLogRepository;
import com.example.demo.base.repository.EventSourceRepository;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component 
@RabbitListener(queues = "${rabbitmq.acount-tx-topic-queue.name}")
public class AccountTxTopicMessageHandler extends BaseEventHandler {

	@Autowired
	private EventSourceRepository eventSourceRepository;
	@Autowired
	private EventLogRepository eventLogRepository;

	@RabbitHandler
	public void handle(BaseEvent event, Channel channel, Message message) throws IOException {
		log.info("Account Transaction Topic Queue -- 接收到消息： {}", event);

		if (Objects.isNull(event)) {
			log.error("Consumer 接收到的 Message 有問題, 內容:{}", event);
			return;
		}

		// 冪等機制，防止重覆消費所帶來的副作用
		if (!this.checkEventIdempotency(event)) {
			log.warn("Consume repeated: {}", event);
			return;
		}

		EventSource eventSource = new EventSource();

		// 查詢 EventLog
		EventLog eventLog = eventLogRepository.findByUuid(event.getEventLogUuid());
		eventLog.consume(); // 更改狀態為: 已消費
		eventLogRepository.save(eventLog);

	}
}
