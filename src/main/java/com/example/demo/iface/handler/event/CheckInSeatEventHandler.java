package com.example.demo.iface.handler.event;

import java.io.IOException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.base.application.port.EventIdempotenceHandlerPort;
import com.example.demo.base.application.port.EventPublishPort;
import com.example.demo.base.iface.handler.BaseEventHandler;
import com.example.demo.base.infra.persistence.EventLogRepository;
import com.example.demo.base.infra.persistence.EventSourceRepository;
import com.example.demo.domain.booking.command.CheckInSeatCommand;
import com.example.demo.domain.booking.outbound.CheckInSeatEvent;
import com.example.demo.domain.share.enums.TicketAction;
import com.example.demo.infra.repository.MoneyAccountRepository;
import com.example.demo.infra.repository.TicketBookingRepository;
import com.example.demo.service.SeatCommandService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.checkin-seat-topic-queue.name}")
public class CheckInSeatEventHandler extends BaseEventHandler {

	private SeatCommandService seatCommandService;
	private TicketBookingRepository ticketBookingRepository;

	public CheckInSeatEventHandler(EventIdempotenceHandlerPort eventIdempotentLogService,
			EventPublishPort rabbitmqService, EventLogRepository eventLogRepository,
			EventSourceRepository eventSourceRepository, TicketBookingRepository ticketBookingRepository,
			MoneyAccountRepository moneyAccountRepository, SeatCommandService seatCommandService) {
		super(eventIdempotentLogService, rabbitmqService, eventLogRepository, eventSourceRepository);
		this.ticketBookingRepository = ticketBookingRepository;
		this.seatCommandService = seatCommandService;
	}

	@RabbitHandler
	public void handle(CheckInSeatEvent event, Channel channel, Message message) throws IOException {
		log.info("Check in Seat Topic Queue -- 接收到消息： {}", event);

		if (Objects.isNull(event)) {
			log.error("Consumer 接收到的 Message 有問題, 內容:{}", event);
			return;
		}

		// 冪等機制，防止重覆消費所帶來的副作用
		if (!this.checkEventIdempotency(event)) {
			log.warn("Consume repeated: {}", event);
			return;
		}

		ticketBookingRepository.findById(event.getTargetId()).ifPresent(booking -> {
			if (StringUtils.equals(event.getAction(), TicketAction.CHECK_IN.getName())) {

				// 執行對座位的 Check In
				CheckInSeatCommand command = CheckInSeatCommand.builder().uuid(event.getTargetId())
						.carNo(event.getCarNo()).seatNo(event.getSeatNo()).takeDate(event.getTakeDate()).build();
				seatCommandService.checkInSeat(command);
			}
		});
	}
}
