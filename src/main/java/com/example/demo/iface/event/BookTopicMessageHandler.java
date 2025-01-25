package com.example.demo.iface.event;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.demo.base.service.BaseEventHandler;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.account.outbound.AccountTxEvent;
import com.example.demo.domain.booking.aggregate.TicketBooking;
import com.example.demo.domain.booking.outbound.TicketBookingEvent;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.share.enums.TicketAction;
import com.example.demo.domain.ticket.aggregate.Ticket;
import com.example.demo.infra.repository.MoneyAccountRepository;
import com.example.demo.infra.repository.TicketBookingRepository;
import com.example.demo.infra.repository.TicketRepository;
import com.example.demo.infra.repository.TrainSeatRepository;
import com.example.demo.util.DateTransformUtil;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.book-topic-queue.name}")
public class BookTopicMessageHandler extends BaseEventHandler {

	@Value("${rabbitmq.acount-tx-topic-queue.name}")
	private String txQueueName;

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	@Autowired
	private TicketBookingRepository ticketBookingRepository;
	@Autowired
	private TrainSeatRepository trainSeatRepository;
	@Autowired
	private MoneyAccountRepository moneyAccountRepository;
	@Autowired
	private TicketRepository ticketRepository;

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
				this.book(booking, event);

				// Check In
			} else if (StringUtils.equals(event.getAction(), TicketAction.CHECK_IN.getName())) {
				this.checkIn(event);

				// Refund
			} else if (StringUtils.equals(event.getAction(), TicketAction.REFUNDED.getName())) {
				TrainSeat trainSeat = trainSeatRepository.findByBookUuidAndTakeDateAndSeatNoAndCarNo(
						event.getTargetId(), DateTransformUtil.transformStringToLocalDate(event.getTakeDate()),
						event.getSeatNo(), event.getCarNo());
				trainSeat.refund();
				trainSeatRepository.save(trainSeat);

				// 先查該票價
				Ticket ticket = ticketRepository.findByTicketNo(booking.getTicketUuid());

				Optional<MoneyAccount> option = moneyAccountRepository.findById(booking.getAccountUuid());

				if (option.isEmpty()) {
					log.error("發生錯誤，查詢帳號資訊失敗。");
					return;
				} else {

					MoneyAccount moneyAccount = option.get();
					// 取出票價加總
					BigDecimal balance = moneyAccount.getBalance().add(ticket.getPrice());

					// 建立 Event
					AccountTxEvent txEvent = AccountTxEvent.builder().money(balance)
							.eventLogUuid(UUID.randomUUID().toString()).targetId(booking.getAccountUuid()).build();

					// 建立 EventLog
					this.generateEventLog(txQueueName, txEvent);

					// 發布 Event 進行退費動作
					this.publishEvent(exchangeName, txQueueName, txEvent);
				}
			}
		});

		// 進行消費
		this.consumeEvent(event.getEventLogUuid());

	}

	/**
	 * 劃位
	 * 
	 * @param booking Booking 資訊
	 * @param event   Event 資料
	 */
	private void book(TicketBooking booking, TicketBookingEvent event) {
		TrainSeat trainSeat = new TrainSeat();
		trainSeat.create(booking.getTicketUuid(), booking.getTrainUuid(), booking.getUuid(),
				DateTransformUtil.transformStringToLocalDate(event.getTakeDate()), event.getSeatNo(), event.getCarNo());
		trainSeatRepository.save(trainSeat);
	}

	/**
	 * check in
	 * 
	 * @param booking Booking 資訊
	 * @param event   Event 資料
	 */
	private void checkIn(TicketBookingEvent event) {
		TrainSeat trainSeat = trainSeatRepository.findByBookUuidAndTakeDateAndSeatNoAndCarNo(event.getTargetId(),
				DateTransformUtil.transformStringToLocalDate(event.getTakeDate()), event.getSeatNo(), event.getCarNo());
		trainSeat.checkIn();
		trainSeatRepository.save(trainSeat);

	}

}
