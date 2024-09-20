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
import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.event.EventLog;
import com.example.demo.base.exception.ValidationException;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.booking.command.CheckInTicketCommand;
import com.example.demo.domain.booking.service.TicketBookingService;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.share.TicketCheckedInData;
import com.example.demo.domain.share.dto.TicketBookedData;
import com.example.demo.domain.share.dto.TicketCreatedData;
import com.example.demo.domain.ticket.command.CreateTicketCommand;
import com.example.demo.domain.ticket.service.TicketService;
import com.example.demo.infra.repository.MoneyAccountRepository;
import com.example.demo.infra.repository.TrainSeatRepository;
import com.example.demo.util.JsonParseUtil;

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

	private final TicketBookingService ticketBookingService;

	private final MoneyAccountRepository moneyAccountRepository;

	@Value("${rabbitmq.book-topic-queue.name}")
	private String bookingQueueName;

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	/**
	 * 新增車票資訊
	 * 
	 * @param command
	 * @return TicketCreatedData
	 */
	public TicketCreatedData createTicket(CreateTicketCommand command) {
		return ticketService.create(command);
	}

	/**
	 * 劃位訂票
	 * 
	 * @param command
	 * @return TicketBookedData
	 */
	public TicketBookedData bookTicket(BookTicketCommand command) {

		TrainSeat seat = trainSeatRepository.findByTakeDateAndSeatNoAndTrainUuidAndBooked(command.getTakeDate(),
				command.getSeatNo(), command.getTrainUuid(), YesNo.Y);
		// 不能重複劃位
		if (!Objects.isNull(seat)) {
			throw new ValidationException("VALIDATE_FAILED", "該位子已被預定，劃位失敗");
		}

		// 查詢 儲值帳號 資訊
		MoneyAccount account = moneyAccountRepository.findByEmail(ContextHolder.getUserEmail());

		TicketBookedData resource = ticketBookingService.book(command, account);

		// 發布事件
		var event = ContextHolder.getEvent();
		this.publishEvent(exchangeName, bookingQueueName, event);

		// 查詢 EventLog
		EventLog eventLog = eventLogRepository.findByUuid(event.getEventLogUuid());
		eventLog.publish(eventLog.getBody()); // 更改狀態為: 已發布
		eventLogRepository.save(eventLog);

		return resource;
	}

	/**
	 * Check in Ticket
	 * 
	 * @param command
	 * @param TicketCheckedInResource
	 */
	public TicketCheckedInData checkInTicket(CheckInTicketCommand command) {
		TicketCheckedInData checkIn = ticketBookingService.checkIn(command);

		// 發布事件
		BaseEvent event = ContextHolder.getEvent();
		this.publishEvent(exchangeName, bookingQueueName, event);

		EventLog eventLog = eventLogRepository.findByUuid(event.getEventLogUuid());
		eventLog.publish(JsonParseUtil.serialize(event));
		eventLogRepository.save(eventLog);
		return checkIn;
	}
}
