package com.example.demo.domain.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.base.config.context.ContextHolder;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.exception.ValidationException;
import com.example.demo.base.service.BaseDomainService;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.booking.aggregate.TicketBooking;
import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.booking.command.CheckInTicketCommand;
import com.example.demo.domain.booking.command.RefundTicketCommand;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.share.BookingQueriedData;
import com.example.demo.domain.share.TicketBookedData;
import com.example.demo.domain.share.TicketCheckedInData;
import com.example.demo.domain.share.TicketRefundedData;
import com.example.demo.domain.share.TrainSeatBookedData;
import com.example.demo.domain.ticket.aggregate.Ticket;
import com.example.demo.domain.train.aggregate.Train;
import com.example.demo.domain.train.aggregate.entity.TrainStop;
import com.example.demo.domain.train.aggregate.vo.TrainKind;
import com.example.demo.infra.repository.TicketBookingRepository;
import com.example.demo.infra.repository.TicketRepository;
import com.example.demo.infra.repository.TrainRepository;
import com.example.demo.infra.repository.TrainSeatRepository;

import jakarta.transaction.Transactional;
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
	private final TrainSeatRepository trainSeatRepository;
	private final TrainRepository trainRepository;
	private final TicketRepository ticketReposiotry;

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
	 */
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
	 */
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

	/**
	 * 查詢個人訂票資訊
	 * 
	 * @param username 使用者名稱
	 * @return BookingQueriedData
	 */
	@Transactional
	public BookingQueriedData queryBooking(String username) {
		List<TicketBooking> bookingList = ticketBookingRepository.findByUsername(username);
		List<String> bookingUuidList = bookingList.stream().map(TicketBooking::getUuid).collect(Collectors.toList());

		// 座位資料
		List<TrainSeat> trainSeatList = trainSeatRepository.findByBookUuidIn(bookingUuidList);
		// 座位 Map <book Uuid, Entity>
		Map<String, List<TrainSeat>> seatMap = trainSeatList.stream()
				.collect(Collectors.groupingBy(TrainSeat::getBookUuid));

		// 透過火車座位查出對應火車的 Uuid 清單
		List<String> trainUuidList = trainSeatList.stream().map(TrainSeat::getTrainUuid).collect(Collectors.toList());
		// 透過 火車 Uuid 清單查出火車資訊
		List<Train> trainList = trainRepository.findByUuidIn(trainUuidList);
		// 火車 Map<火車 Uuid, Train>
		Map<String, Train> trainMap = trainList.stream().collect(Collectors.toMap(Train::getUuid, Function.identity()));

		// 透過火車座位查出對應車票的 Uuid 清單
		List<String> ticketUuidList = trainSeatList.stream().map(TrainSeat::getTicketUuid).collect(Collectors.toList());
		// 車票 Map<車票 Uuid, Train>
		Map<String, Ticket> ticketMap = ticketReposiotry.findByTicketNoIn(ticketUuidList).stream()
				.collect(Collectors.toMap(Ticket::getTicketNo, Function.identity()));

		BookingQueriedData bookingQueriedData = new BookingQueriedData();
		bookingQueriedData.setUsername(username);

		List<TrainSeatBookedData> bookedDatas = new ArrayList<>();

		bookingList.stream().forEach(book -> {
			TrainSeatBookedData bookedData = new TrainSeatBookedData();
			this.setTrainSeatBookedData(bookedData, book, ticketMap, trainMap, seatMap);
			bookedDatas.add(bookedData);
		});

		bookingQueriedData.setBookedDatas(bookedDatas);
		return bookingQueriedData;
	}

	/**
	 * 設置 Train Seat Booking 資料
	 * 
	 * @param bookedData 被設置的 Booking 資料
	 * @param book       Booking 領域資料
	 * @param ticketMap  車票 Map<車票 Uuid, Train> 資料
	 * @param trainMap   火車 Map<火車Uuid, Train> 資料
	 * @param seatMap    座位 Map<bookUuid, Entity> 資料
	 */
	private void setTrainSeatBookedData(TrainSeatBookedData bookedData, TicketBooking book,
			Map<String, Ticket> ticketMap, Map<String, Train> trainMap, Map<String, List<TrainSeat>> seatMap) {
		if (!Objects.isNull(ticketMap.get(book.getTicketUuid()))) {
			var ticketData = ticketMap.get(book.getTicketUuid());
			bookedData.setFrom(ticketData.getFromStop()); // 起站
			bookedData.setTo(ticketData.getToStop()); // 迄站
		}

		if (!Objects.isNull(trainMap.get(book.getTrainUuid()))) {
			var trainData = trainMap.get(book.getTrainUuid());
			bookedData.setNumber(trainData.getNumber()); // 火車車次
			bookedData.setKind(TrainKind.fromLabel(trainData.getKind().getLabel()).getLabel()); // 火車名稱

			// Map <站名, 發車時間>
			Map<String, LocalTime> stopMap = trainData.getStops().stream()
					.collect(Collectors.toMap(TrainStop::getName, TrainStop::getTime));
			// 設置發車時間
			bookedData.setStartTime(
					!Objects.isNull(stopMap.get(bookedData.getFrom())) ? stopMap.get(bookedData.getFrom()) : null);
			// 設置抵達時間
			bookedData.setArriveTime(
					!Objects.isNull(stopMap.get(bookedData.getTo())) ? stopMap.get(bookedData.getTo()) : null);
			log.debug("起站:{}, 迄站:{}, 發車時間:{}, 抵達時間:{}", bookedData.getFrom(), bookedData.getTo(),
					bookedData.getStartTime(), bookedData.getArriveTime());
		}

		if (!Objects.isNull(seatMap.get(book.getUuid()))) {
			var seatList = seatMap.get(book.getUuid());
			Map<String, TrainSeat> collectMap = seatList.stream()
					.collect(Collectors.toMap(TrainSeat::getBookUuid, Function.identity()));

			if (!Objects.isNull(collectMap.get(book.getUuid()))) {
				var trainSeat = collectMap.get(book.getUuid());
				bookedData.setSeatNo(trainSeat.getSeatNo());
				bookedData.setTakeDate(trainSeat.getTakeDate());
				bookedData.setActiveFlag(trainSeat.getActiveFlag());
				bookedData.setBooked(trainSeat.getBooked());
			}
		}
	}

}
