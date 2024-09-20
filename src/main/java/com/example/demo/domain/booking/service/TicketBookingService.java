package com.example.demo.domain.booking.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.base.BaseDomainService;
import com.example.demo.base.context.ContextHolder;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.exception.ValidationException;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.booking.aggregate.TicketBooking;
import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.booking.command.CheckInTicketCommand;
import com.example.demo.domain.booking.command.RefundTicketCommand;
import com.example.demo.domain.share.TicketBookedData;
import com.example.demo.domain.share.TicketCheckedInData;
import com.example.demo.domain.share.TicketRefundedData;
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
	public TicketBookedData book(BookTicketCommand command, MoneyAccount account) {
		TicketBooking ticketBooking = new TicketBooking();
		ticketBooking.create(command, account);

		// 儲存 Ticket Booking 資訊
		TicketBooking saved = ticketBookingRepository.save(ticketBooking);

		// 從上下文取得 Event
		var event = ContextHolder.getEvent();
		// 寫入 EventLog
		this.generateEventLog(bookingQueueName, event.getEventLogUuid(), saved.getUuid(), event);
		return new TicketBookedData(saved.getUuid());
	}
	
	/**
	 * Check in 車票 
	 * 
	 * @param command
	 * @return 成功訊息
	 * */
	public TicketCheckedInData checkIn(CheckInTicketCommand command) {
		Optional<TicketBooking> option = ticketBookingRepository.findById(command.getUuid());
		if (option.isPresent()) {
			TicketBooking booking = option.get();
			booking.checkIn(command);
			TicketBooking saved = ticketBookingRepository.save(booking);
			
			BaseEvent event = ContextHolder.getEvent();
			this.generateEventLog(bookingQueueName, event.getEventLogUuid(), event.getTargetId(), event);
			return new TicketCheckedInData(saved.getTrainUuid(), saved.getCreatedDate(), "Checked In Successfully");
		}
		log.error("發生錯誤，查無此預約");
		throw new ValidationException("VALIDATION_EXCEPTION", "發生錯誤，查無此預約");
	}
	
	/**
	 * 退費取消訂票
	 * 
	 * */
	public TicketRefundedData refund(RefundTicketCommand command) {
		Optional<TicketBooking> option = ticketBookingRepository.findById(command.getUuid());
		if (option.isPresent()) {
			TicketBooking booking = option.get();
			booking.refund(command);
			TicketBooking saved = ticketBookingRepository.save(booking);
			
			BaseEvent event = ContextHolder.getEvent();
			this.generateEventLog(bookingQueueName, event.getEventLogUuid(), event.getTargetId(), event);
			return new TicketRefundedData(saved.getTrainUuid(), saved.getCreatedDate(), "Refunded Successfully");
		}
		log.error("發生錯誤，查無此預約");
		throw new ValidationException("VALIDATION_EXCEPTION", "發生錯誤，查無此預約");
		
	}
}
