package com.example.demo.service;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.BaseApplicationService;
import com.example.demo.base.context.ContextHolder;
import com.example.demo.base.enums.YesNo;
import com.example.demo.base.event.EventLog;
import com.example.demo.base.exception.ValidationException;
import com.example.demo.base.repository.EventLogRepository;
import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.booking.service.TicketBookingService;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.share.dto.TicketBookedData;
import com.example.demo.domain.share.dto.TicketCreatedData;
import com.example.demo.domain.ticket.command.CreateTicketCommand;
import com.example.demo.domain.ticket.service.TicketService;
import com.example.demo.infra.repository.TrainSeatRepository;

import lombok.RequiredArgsConstructor;

/**
 * Application Service
 * 
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public class TicketCommandService extends BaseApplicationService {

	private final TicketService ticketService;

	private final TrainSeatRepository trainSeatRepository;

	private final EventLogRepository eventLogRepository;

	private final TicketBookingService ticketBookingService;

	@Value("${rabbitmq.book-topic-queue.name}")
	private String bookingQueueName;

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	/**
	 * 新增車票資訊
	 * 
	 * @param command
	 * @return uuid
	 */
	public TicketCreatedData createTicket(CreateTicketCommand command) {
		return ticketService.create(command);
	}

	/**
	 * 劃位訂票
	 * 
	 * @param command
	 * @return uuid
	 */
	public TicketBookedData bookTicket(BookTicketCommand command) {

		TrainSeat seat = trainSeatRepository.findByTakeDateAndSeatNoAndTrainUuidAndBooked(command.getTakeDate(),
				command.getSeatNo(), command.getTrainUuid(), YesNo.Y);

		// 不能重複劃位
		if (!Objects.isNull(seat)) {
			throw new ValidationException("VALIDATE_FAILED", "該位子已被預定，劃位失敗");
		}

		TicketBookedData resource = ticketBookingService.book(command);

		// 發布事件
		var event = ContextHolder.getEvent();
		this.publishEvent(exchangeName, bookingQueueName, event);
		
		// 查詢 EventLog
		EventLog eventLog = eventLogRepository.findByUuid(event.getEventLogUuid());
		eventLog.publish(eventLog.getBody()); // 更改狀態為: 已發布
		eventLogRepository.save(eventLog);

		return resource;
	}
}
