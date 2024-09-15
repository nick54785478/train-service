package com.example.demo.iface.event;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.base.event.BaseEvent;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.test-topic-queue.name}")
public class TestTopicMessageConsumer {

	@RabbitHandler
	public void testMessage(BaseEvent event, Channel channel, Message message) throws IOException {
		log.info("Test Topic Queue --接收到消息：targetId:{}", event.getTargetId());

//		if (true) {
//			log.error("拋出例外，稍後重新再測試");
//			throw new ValidationException("VALIDATION_EXCEPTION","拋出例外，稍後重新再測試");
//		}
		
//		try {
//			log.info("Test Topic Queue --接收到消息：targetId:{}", event.getTargetId());
//			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//		} catch (Exception e) {
//			if (message.getMessageProperties().getRedelivered()) {
//				log.error("訊息已重複處理失敗,拒絕再次接收...");
//				// basicReject: 拒絕訊息，與basicNack區別在於不能進行批次操作，其他用法很相似 false表示訊息不再重新進入佇列
//				channel.basicReject(message.getMessageProperties().getDeliveryTag(), false); // 拒絕訊息
//			} else {
//				log.error("訊息即將再次返回隊列處理...");
//				// basicNack:表示失敗確認，一般在消費訊息業務異常時用到此方法，可以將訊息重新投遞入佇列
//				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
//			}
//		}
	}
}
