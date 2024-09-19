package com.example.demo.domain.booking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.base.BaseDomainService;
import com.example.demo.base.context.ContextHolder;
import com.example.demo.domain.booking.aggregate.TicketBooking;
import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.share.dto.TicketBookedData;
import com.example.demo.infra.repository.TicketBookingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Domain Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketBookingService extends BaseDomainService {

	@Value("${rabbitmq.book-topic-queue.name}")
	private String bookingQueueName;

	private final TicketBookingRepository ticketBookingRepository;

	/**
	 * Book 車票
	 * 
	 * @param command
	 */
	public TicketBookedData book(BookTicketCommand command) {

		TicketBooking ticketBooking = new TicketBooking();
		ticketBooking.create(command);

		// 儲存 Ticket Booking 資訊
		TicketBooking saved = ticketBookingRepository.save(ticketBooking);

		// 從上下文取得 Event
		var event = ContextHolder.getEvent();
		// 寫入 EventLog
		this.generateEventLog(bookingQueueName, event.getEventLogUuid(), saved.getUuid(), event);
		return new TicketBookedData(saved.getUuid());
	}
}
