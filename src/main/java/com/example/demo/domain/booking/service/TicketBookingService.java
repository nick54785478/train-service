package com.example.demo.domain.booking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.base.BaseDomainService;
import com.example.demo.base.context.ContextHolder;
import com.example.demo.base.event.EventLog;
import com.example.demo.domain.booking.aggregate.TicketBooking;
import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.share.dto.TicketBookedData;
import com.example.demo.infra.repository.EventLogRepository;
import com.example.demo.infra.repository.TicketBookingRepository;
import com.example.demo.util.JsonParseUtil;

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

	private final EventLogRepository eventLogRepository;
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

		// 從上下文取得使用者帳號
		String username = ContextHolder.getUsername();

		var event = ContextHolder.getEvent();

		// Event 類型
		String className = event.getClass().getName();

		// 寫入 EventLog
		var eventLog = EventLog.builder().uuid(event.getEventLogUuid()).userId(username).topic(bookingQueueName)
				.className(className).body(JsonParseUtil.serialize(event)).targetId(saved.getUuid()).build();
		eventLogRepository.save(eventLog); // 儲存 eventLog
		log.info("EventLog:{}", eventLog);

		return new TicketBookedData(saved.getUuid());
	}
}
