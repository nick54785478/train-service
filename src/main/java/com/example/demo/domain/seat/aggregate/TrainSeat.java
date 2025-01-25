package com.example.demo.domain.seat.aggregate;

import java.time.LocalDate;

import com.example.demo.base.entity.BaseEntity;
import com.example.demo.base.enums.YesNo;

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
public class TrainSeat extends BaseEntity {

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
	 * @param ticketUuid
	 * @param trainUuid
	 * @param bookUuid
	 * @param takeDate
	 * @param seatNo
	 * @param carNo
	 */
	public void create(String ticketUuid, String trainUuid, String bookUuid, LocalDate takeDate, String seatNo, Long carNo) {
		this.ticketUuid = ticketUuid;
		this.trainUuid = trainUuid;
		this.bookUuid = bookUuid;
		this.takeDate = takeDate;
		this.carNo = carNo;
		this.seatNo = seatNo;
		this.booked = YesNo.Y;
		this.activeFlag = YesNo.Y;
	}

	/**
	 * check in
	 */
	public void checkIn() {
		this.activeFlag = YesNo.N;
	}

	/**
	 * refund
	 */
	public void refund() {
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
