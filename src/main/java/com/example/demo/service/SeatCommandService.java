package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.base.application.service.BaseApplicationService;
import com.example.demo.base.infra.event.EventTopicResolver;
import com.example.demo.base.shared.entity.EventLog;
import com.example.demo.base.shared.event.BaseEvent;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.seat.command.CancelSeatCommand;
import com.example.demo.domain.seat.command.CheckInSeatCommand;
import com.example.demo.domain.seat.command.CreateSeatCommand;
import com.example.demo.domain.ticket.aggregate.Ticket;
import com.example.demo.infra.repository.MoneyAccountRepository;
import com.example.demo.infra.repository.TicketBookingRepository;
import com.example.demo.infra.repository.TicketRepository;
import com.example.demo.infra.repository.TrainSeatRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class SeatCommandService extends BaseApplicationService {

	private TicketRepository ticketRepository;
	private EventTopicResolver eventTopicResolver;
	private MoneyAccountRepository moneyAccountRepository;
	private TrainSeatRepository trainSeatRepository;
	private TicketBookingRepository ticketBookingRepository;

	/**
	 * 進行劃位動作
	 * 
	 * @param command {@link CreateSeatCommand}
	 */
	@Transactional
	public void bookSeat(CreateSeatCommand command) {
		TrainSeat trainSeat = TrainSeat.create(command);
		trainSeatRepository.save(trainSeat);
	}

	/**
	 * 進行座位 Check in 動作
	 * 
	 * @param command {@link CheckInTicketCommand}
	 */
	@Transactional
	public void checkInSeat(CheckInSeatCommand command) {
		TrainSeat trainSeat = trainSeatRepository.findByBookUuidAndTakeDateAndSeatNoAndCarNo(command.getUuid(),
				command.getTakeDate(), command.getSeatNo(), command.getCarNo());
		trainSeat.checkIn();
		trainSeatRepository.save(trainSeat);
	}

	/**
	 * 取消劃位
	 * 
	 * @param command {@link CheckInTicketCommand}
	 */
	@Transactional
	public void cancelSeat(CancelSeatCommand command) {

		TrainSeat trainSeat = trainSeatRepository.findByBookUuidAndTakeDateAndSeatNoAndCarNo(command.getUuid(),
				command.getTakeDate(), command.getSeatNo(), command.getCarNo());
		trainSeat.cancel(); // 取消座位
		trainSeatRepository.save(trainSeat);

		ticketBookingRepository.findById(command.getUuid()).ifPresentOrElse(booking -> {
			// 先查該票價
			Ticket ticket = ticketRepository.findByTicketNo(booking.getTicketUuid());

			// 透過 Booking 上的 Account UUID 找到對應的帳戶
			moneyAccountRepository.findById(booking.getAccountUuid()).ifPresentOrElse(account -> {
				account.refundForSeatCancelling(ticket.getPrice());

				// 處理 Domain Event
				List<BaseEvent> domainEvents = account.getDomainEvents();
				domainEvents.stream().forEach(event -> {
					// 透過 Event 類型取得特定 Topic
					String topic = eventTopicResolver.resolve(event);

					// EventLog 在這裡建立（不在 Domain 內）
					EventLog eventLog = this.generateEventLog(topic, event);

					// 發送 MQ
					this.publishEvent(topic, event);

					// 更新狀態
					eventLog.publish(eventLog.getBody());
					eventLogRepository.saveAndFlush(eventLog);
				});

			}, () -> log.error("查無該筆帳戶資訊: Account uuid:{}", booking.getAccountUuid()));

		}, () -> log.error("查無該筆 Ticket Booking 紀錄，Booking uuid:{}", command.getUuid()));

	}

}
