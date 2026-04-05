package com.example.demo.domain.seat.aggregate;

import java.time.LocalDate;

import com.example.demo.base.domain.aggregate.BaseAggreagteRoot;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.booking.outbound.CancelSeatEvent;
import com.example.demo.domain.seat.command.CreateSeatCommand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "TRAIN_SEAT")
public class TrainSeat extends BaseAggreagteRoot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "TICKET_UUID")
	private String ticketUuid; // Ticket 對應的 UUID

	@Column(name = "TRAIN_UUID")
	private String trainUuid; // 火車對應的 UUID

	@Column(name = "BOOK_UUID")
	private String bookUuid; // 預訂座位的 UUID

	@Column(name = "TAKE_DATE")
	private LocalDate takeDate; // 乘車日期

	@Column(name = "SEAT_NO")
	private String seatNo; // 座號

	@Column(name = "CAR_NO")
	private Long carNo; // 車廂編號

	@Column(name = "BOOKED")
	@Enumerated(EnumType.STRING)
	private YesNo booked; // 是否已預定

	@Column(name = "ACTIVE_FLAG")
	@Enumerated(EnumType.STRING)
	private YesNo activeFlag; // 是否已失效

	/**
	 * create seat
	 * 
	 * @param ticketUuid Ticket UUID
	 * @param trainUuid  Train UUID
	 * @param bookUuid   Booking UUID
	 * @param takeDate   搭乘日期
	 * @param seatNo     座位
	 * @param carNo      車廂編號
	 * @return TrainSeat
	 */
	public static TrainSeat create(CreateSeatCommand command) {
		TrainSeat seat = new TrainSeat();
		seat.ticketUuid = command.getTicketUuid();
		seat.trainUuid = command.getTrainUuid();
		seat.bookUuid = command.getBookingUuid();
		seat.takeDate = command.getTakeDate();
		seat.carNo = command.getCarNo();
		seat.seatNo = command.getSeatNo();
		seat.booked = YesNo.Y;
		seat.activeFlag = YesNo.Y;
		return seat;
	}

	/**
	 * check in 座位
	 */
	public void checkIn() {
		this.activeFlag = YesNo.N;
	}

	/**
	 * 取消座位
	 */
	public void cancel() {
		this.activeFlag = YesNo.N;
		this.booked = YesNo.N;
	}

	/**
	 * 使票券失效
	 */
	public void inactive() {
		this.activeFlag = YesNo.N;
	}

}
