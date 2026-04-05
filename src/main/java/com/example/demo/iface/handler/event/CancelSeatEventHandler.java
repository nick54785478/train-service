package com.example.demo.iface.handler.event;

import java.io.IOException;
import java.util.Objects;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.base.application.port.EventIdempotenceHandlerPort;
import com.example.demo.base.application.port.EventPublishPort;
import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.base.infra.persistence.EventLogRepository;
import com.example.demo.base.infra.persistence.EventSourceRepository;
import com.example.demo.domain.booking.outbound.CancelSeatEvent;
import com.example.demo.domain.seat.command.CancelSeatCommand;
import com.example.demo.service.SeatCommandService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.checkin-seat-topic-queue.name}")
public class CancelSeatEventHandler extends BaseEventHandler {

	private SeatCommandService seatCommandService;

	public CancelSeatEventHandler(EventIdempotenceHandlerPort eventIdempotentLogService,
			EventPublishPort rabbitmqService, EventLogRepository eventLogRepository,
			EventSourceRepository eventSourceRepository, SeatCommandService seatCommandService) {
		super(eventIdempotentLogService, rabbitmqService, eventLogRepository, eventSourceRepository);
		this.seatCommandService = seatCommandService;
	}

	@RabbitHandler
	public void handle(CancelSeatEvent event, Channel channel, Message message) throws IOException {
		log.info("Cancel Seat Topic Queue -- 接收到消息： {}", event);

		if (Objects.isNull(event)) {
			log.error("Consumer 接收到的 Message 有問題, 內容:{}", event);
			return;
		}

		// 冪等機制，防止重覆消費所帶來的副作用
		if (!this.checkEventIdempotency(event)) {
			log.warn("Consume repeated: {}", event);
			return;
		}

		// 執行對座位的 Cancel 動作
		CancelSeatCommand command = CancelSeatCommand.builder().uuid(event.getTargetId()).carNo(event.getCarNo())
				.seatNo(event.getSeatNo()).takeDate(event.getTakeDate()).build();
		seatCommandService.cancelSeat(command);
	}
}