package com.example.demo.iface.event;

import java.io.IOException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.base.BaseEventHandler;
import com.example.demo.base.event.EventLog;
import com.example.demo.base.repository.EventLogRepository;
import com.example.demo.domain.booking.aggregate.vo.TicketStatus;
import com.example.demo.domain.booking.outbound.TicketBookingEvent;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.share.enums.TicketAction;
import com.example.demo.infra.repository.TicketBookingRepository;
import com.example.demo.infra.repository.TrainSeatRepository;
import com.example.demo.util.DateTransformUtil;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.book-topic-queue.name}")
public class BookTopicMessageHandler extends BaseEventHandler {

	@Autowired
	private TicketBookingRepository ticketBookingRepository;
	@Autowired
	private TrainSeatRepository trainSeatRepository;
	@Autowired
	private EventLogRepository eventLogRepository;

	@RabbitHandler
	public void handle(TicketBookingEvent event, Channel channel, Message message) throws IOException {
		log.info("Book Topic Queue -- 接收到消息： {}", event);

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
			// Book
			if (StringUtils.equals(event.getAction(), TicketAction.BOOK.getName())) {
				TrainSeat trainSeat = new TrainSeat();
				trainSeat.create(booking.getTicketUuid(), booking.getTrainUuid(), booking.getUuid(),
						DateTransformUtil.transformStringToLocalDate(event.getTakeDate()), event.getSeatNo());
				trainSeatRepository.save(trainSeat);

			// Check In
			} else if (StringUtils.equals(event.getAction(), TicketAction.CHECK_IN.getName())) {
				TrainSeat trainSeat = trainSeatRepository.findByBookUuidAndTakeDateAndSeatNo(event.getTargetId(),
						DateTransformUtil.transformStringToLocalDate(event.getTakeDate()), event.getSeatNo());
				trainSeat.checkIn();
				trainSeatRepository.save(trainSeat);
			}
		});

		// 查詢 EventLog
		EventLog eventLog = eventLogRepository.findByUuid(event.getEventLogUuid());
		eventLog.consume(); // 更改狀態為: 已消費
		eventLogRepository.save(eventLog);

	}
}
