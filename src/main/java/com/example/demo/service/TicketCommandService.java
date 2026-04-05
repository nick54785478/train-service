package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.base.infra.context.ContextHolder;
import com.example.demo.base.infra.event.EventTopicResolver;
import com.example.demo.base.shared.entity.EventLog;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.base.shared.event.BaseEvent;
import com.example.demo.base.shared.exception.exception.ValidationException;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.booking.aggregate.TicketBooking;
import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.booking.command.CheckInTicketCommand;
import com.example.demo.domain.booking.command.RefundTicketCommand;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.service.TicketBookingService;
import com.example.demo.domain.service.TicketService;
import com.example.demo.domain.share.TicketBookedData;
import com.example.demo.domain.share.TicketCheckedInData;
import com.example.demo.domain.share.TicketRefundedData;
import com.example.demo.domain.ticket.command.CreateOrUpdateTicketCommand;
import com.example.demo.domain.ticket.command.CreateTicketCommand;
import com.example.demo.infra.repository.MoneyAccountRepository;
import com.example.demo.infra.repository.TicketBookingRepository;
import com.example.demo.infra.repository.TrainSeatRepository;
import com.example.demo.util.JsonParseUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
public class TicketCommandService extends BaseApplicationService {

	private final TicketService ticketService;
	private final EventTopicResolver eventTopicResolver;
	private final TrainSeatRepository trainSeatRepository;
	private final TicketBookingService ticketBookingService;
	private final MoneyAccountRepository moneyAccountRepository;
	private final TicketBookingRepository ticketBookingRepository;

	@Value("${rabbitmq.book-seat-topic-queue.name}")
	private String bookSeatQueueName;

	@Value("${rabbitmq.acount-tx-topic-queue.name}")
	private String txQueueName;

	/**
	 * 新增車票資訊
	 * 
	 * @param command
	 */
	public void createTicket(CreateTicketCommand command) {
		ticketService.create(command);
	}

	/**
	 * 批次新增車票資訊
	 * 
	 * @param trainNo  車次
	 * @param commands
	 */
	public void createOrUpdateTickets(Integer trainNo, List<CreateOrUpdateTicketCommand> commands) {
		ticketService.createOrUpdate(trainNo, commands);
	}

	/**
	 * 劃位訂票
	 * 
	 * @param command {@link BookTicketCommand}
	 * @return TicketBookedData
	 */
	public TicketBookedData bookTicket(BookTicketCommand command) {

		// 取得當前使用者資訊
		String username = ContextHolder.getUsername();
		String email = ContextHolder.getUserEmail();

		TrainSeat seat = trainSeatRepository.findByTakeDateAndSeatNoAndTrainUuidAndBooked(command.getTakeDate(),
				command.getSeatNo(), command.getTrainUuid(), YesNo.Y);
		// 不能重複劃位
		if (!Objects.isNull(seat)) {
			throw new ValidationException("VALIDATE_FAILED", "該位子已被預定，劃位失敗");
		}

		// 查詢 儲值帳號 資訊
		MoneyAccount account = moneyAccountRepository.findByUsername(username);

		// 呼叫 Domain Service 進行訂位
		TicketBooking ticketBooking = ticketBookingService.book(command, account, username, email);
		ticketBookingRepository.saveAndFlush(ticketBooking); // 這邊要 Save & Flush，否則 Event 可能會在 Commit 前觸發

		// 發布 Domain Event 進行扣款及訂位
		this.publishEventsForBooking(ticketBooking, account);
		return new TicketBookedData(ticketBooking.getUuid());
	}

	/**
	 * Check in Ticket
	 * 
	 * @param command             {@link CheckInTicketCommand}
	 * @param TicketCheckedInData
	 */
	public TicketCheckedInData checkInTicket(CheckInTicketCommand command) {
		TicketBooking booking = ticketBookingRepository.findById(command.getUuid())
				.orElseThrow(() -> new ValidationException("VALIDATION_EXCEPTION", "發生錯誤，查無此訂位"));

		// 進行 Ticket 的 Check-in 動作
		ticketBookingService.checkInTicket(command, booking);
		TicketBooking saved = ticketBookingRepository.save(booking);

		// 取出 Domain Events
		List<BaseEvent> domainEvents = booking.getDomainEvents();

		// 發布 Domain Event
		domainEvents.stream().forEach(event -> {
			// 透過 Event 類型取得特定 Topic
			String topic = eventTopicResolver.resolve(event);

			// EventLog 在這裡建立（不在 Domain）
			EventLog eventLog = this.generateEventLog(topic, event);

			// 發送 MQ
			this.publishEvent(topic, event);

			// 更新狀態
			eventLog.publish(eventLog.getBody());
			eventLogRepository.saveAndFlush(eventLog);
		});
		
		// 清除 Domain Events
		booking.clearDomainEvents();

		return new TicketCheckedInData(saved.getUuid(), saved.getLastUpdatedDate(), "Check in Successfully!");
	}

	/**
	 * Refund Ticket
	 * 
	 * @param command
	 * @param TicketCheckedInResource
	 */
	public TicketRefundedData refundTicket(RefundTicketCommand command) {
		TrainSeat trainSeat = trainSeatRepository.findByBookUuidAndSeatNoAndTakeDateAndActiveFlag(command.getUuid(),
				command.getSeatNo(), command.getTakeDate(), YesNo.N);
		if (!Objects.isNull(trainSeat)) {
			log.error("該票券已失效");
			throw new ValidationException("VALIDATION_EXCEPTION", "該票券已失效");
		}

		TicketRefundedData refundedData = ticketBookingService.refund(command);

		// 發布事件
		BaseEvent event = ContextHolder.getEvent();
		this.publishEvent(bookSeatQueueName, event);
		EventLog eventLog = eventLogRepository.findByUuid(event.getEventLogUuid());
		eventLog.publish(JsonParseUtil.serialize(event));
		eventLogRepository.save(eventLog);
		return refundedData;
	}

	/**
	 * 發布 Domain Event (扣款訂位)
	 * 
	 * @param ticketBooking 訂票資訊聚合根
	 * @param account       付款帳號
	 */
	private void publishEventsForBooking(TicketBooking ticketBooking, MoneyAccount account) {

		// 修改點：合併所有 Aggregate 的事件
		List<BaseEvent> allEvents = new ArrayList<>();
		allEvents.addAll(ticketBooking.getDomainEvents());
		allEvents.addAll(account.getDomainEvents());
		log.info("all events: {}", allEvents);

		// 統一發布
		allEvents.forEach(event -> {

			// 透過 Event 類型取得特定 Topic
			String topic = eventTopicResolver.resolve(event);

			// EventLog 在這裡建立（不在 Domain）
			EventLog eventLog = generateEventLog(topic, event);

			// 發送 MQ
			this.publishEvent(topic, event);

			// 更新狀態
			eventLog.publish(eventLog.getBody());
			eventLogRepository.saveAndFlush(eventLog);
		});

		// 清理
		ticketBooking.clearDomainEvents();
		account.clearDomainEvents();
	}
}
