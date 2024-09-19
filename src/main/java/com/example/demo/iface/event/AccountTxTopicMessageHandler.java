package com.example.demo.iface.event;

import java.io.IOException;
import java.util.Objects;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.base.BaseEventHandler;
import com.example.demo.base.context.ContextHolder;
import com.example.demo.domain.account.command.DepositMoneyCommand;
import com.example.demo.domain.account.outbound.AccountTxEvent;
import com.example.demo.service.MoneyAccountCommandService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.acount-tx-topic-queue.name}")
public class AccountTxTopicMessageHandler extends BaseEventHandler {

	@Autowired
	MoneyAccountCommandService moneyAccountCommandService;

	@RabbitHandler
	public void handle(AccountTxEvent event, Channel channel, Message message) throws IOException {
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
		
		ContextHolder.setBaseEvent(event); // 將 Event 存入上下文供取用。

		// 呼叫 Application Service 進行儲值處理
		DepositMoneyCommand command = new DepositMoneyCommand();
		command.setUuid(event.getTargetId());
		command.setMoney(event.getMoney());
		
		
		moneyAccountCommandService.deposit(command);

		// 進行消費
		this.consumeEvent(event.getEventLogUuid());

	}
}
