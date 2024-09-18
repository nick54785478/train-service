package com.example.demo.iface.event;

import java.io.IOException;
import java.util.Objects;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.base.event.BaseEventHandler;
import com.example.demo.base.event.EventLog;
import com.example.demo.base.repository.EventLogRepository;
import com.example.demo.client.AuthFeignClient;
import com.example.demo.domain.account.outbound.RegisterUserEvent;
import com.example.demo.iface.dto.RegisterUserResource;
import com.example.demo.iface.dto.UserInfoResource;
import com.example.demo.iface.dto.UserRegisteredResource;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.register-topic-queue.name}")
public class RegisterUserMessageHandler extends BaseEventHandler {

	@Autowired
	private EventLogRepository eventLogRepository;
	@Autowired
	private AuthFeignClient authFeignClient;

	@RabbitHandler
	public void handle(RegisterUserEvent event, Channel channel, Message message) throws IOException {
		log.info("Register User Topic Queue -- 接收到消息： {}", event);

		if (Objects.isNull(event)) {
			log.error("Consumer 接收到的 Message 有問題, 內容:{}", event);
			return;
		}

		// 冪等機制，防止重覆消費所帶來的副作用
		if (!this.checkEventIdempotency(event)) {
			log.warn("Consume repeated: {}", event);
			return;
		}

		RegisterUserResource resource = this.transformData(event, RegisterUserResource.class);

		// 透過 Email 查詢此用戶是否已註冊在 AuthService
		UserInfoResource responseData = authFeignClient.getUserByEmail(event.getEmail());
		if (Objects.isNull(responseData)) {
			// 呼叫外部 API，註冊使用者
			UserRegisteredResource response = authFeignClient.register(resource);
			log.debug("code:{}, message:{}", response.getCode(), response.getMessage());
		}

		// 查詢 EventLog
		EventLog eventLog = eventLogRepository.findByUuid(event.getEventLogUuid());
		eventLog.consume(); // 更改狀態為: 已消費
		eventLogRepository.save(eventLog);

		// TODO 若發生錯誤處理情況

	}

}
