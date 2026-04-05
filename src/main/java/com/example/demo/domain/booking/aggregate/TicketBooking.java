package com.example.demo.domain.booking.aggregate;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import com.example.demo.base.domain.aggregate.BaseAggreagteRoot;
import com.example.demo.base.shared.enums.YesNo;
import com.example.demo.domain.account.aggregate.MoneyAccount;
import com.example.demo.domain.booking.aggregate.vo.TicketStatus;
import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.booking.command.RefundTicketCommand;
import com.example.demo.domain.booking.outbound.BookSeatEvent;
import com.example.demo.domain.booking.outbound.CheckInSeatEvent;
import com.example.demo.domain.share.enums.TicketAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Table(name = "TICKET_BOOKING")
public class TicketBooking extends BaseAggreagteRoot {

	@Id
	@Column(name = "UUID")
	private String uuid;

	@Column(name = "USERNAME")
	private String username; // 使用者帳號

	@Column(name = "EMAIL")
	private String email; // Email

	@Column(name = "TICKET_UUID")
	private String ticketUuid; // 對應車票班次

	@Column(name = "TRAIN_UUID")
	private String trainUuid; // 該車次對應的 UUID

	@Column(name = "ACCOUNT_UUID")
	private String accountUuid; // 帳號 UUID

	@Column(name = "STATUS")
	@Enumerated(EnumType.STRING)
	private TicketStatus status; // 座位狀態

	@Enumerated(EnumType.STRING)
	@Column(name = "ACTIVE_FLAG")
	private YesNo activeFlag; // 是否失效 (過期、取消訂位)

	/**
	 * book
	 * 
	 * @param command      預訂車票命令
	 * @param MoneyAccount 訂票帳戶相關資訊
	 * @param username     使用者名稱
	 * @param email        信箱
	 */
	public static TicketBooking create(BookTicketCommand command, MoneyAccount account, String username, String email) {
		TicketBooking ticketBooking = new TicketBooking();
		ticketBooking.uuid = UUID.randomUUID().toString();
		ticketBooking.trainUuid = command.getTrainUuid();
		ticketBooking.ticketUuid = command.getTicketUuid();
		ticketBooking.username = username;
		ticketBooking.email = email;
		ticketBooking.status = TicketStatus.UNTAKEN;
		ticketBooking.accountUuid = (Objects.isNull(account)) ? null : account.getUuid();
		ticketBooking.activeFlag = YesNo.Y;

		// 建立一個 Domain Event
		BookSeatEvent event = BookSeatEvent.builder().targetId(ticketBooking.uuid).takeDate(command.getTakeDate())
				.action(TicketAction.BOOK.getName()).seatNo(command.getSeatNo()).carNo(command.getCarNo()).build();

		// 設置 Domain Event
		ticketBooking.raiseEvent(event);
		return ticketBooking;
	}

	/**
	 * 進行 Check in 動作
	 * 
	 * @param takeDate 搭乘日期
	 * @param seatNo   乘坐位置
	 * @param carNo    車廂編號
	 */
	public void checkIn(LocalDate takeDate, String seatNo, Long carNo) {
		this.status = TicketStatus.TAKEN;
		this.activeFlag = YesNo.N;

		// 建立 Domain Event : 車票 Check in 事件
		CheckInSeatEvent event = CheckInSeatEvent.builder().targetId(this.uuid).takeDate(takeDate)
				.action(TicketAction.CHECK_IN.getName()).seatNo(seatNo).carNo(carNo).build();
		// 設置 Domain Event
		this.raiseEvent(event);

//		BookSeatEvent event = BookSeatEvent.builder().targetId(ticketBooking.uuid)
//				.takeDate(DateTransformUtil.transformLocalDateToString(command.getTakeDate()))
//				.action(TicketAction.BOOK.getName()).seatNo(command.getSeatNo()).carNo(command.getCarNo()).build();
//
//		
//		// 建立一個 Event
//		CheckInSeatEvent event = CheckInSeatEvent.builder().eventLogUuid(UUID.randomUUID().toString())
//				.targetId(this.uuid).takeDate(DateTransformUtil.transformLocalDateToString(command.getTakeDate()))
//				.action(TicketAction.CHECK_IN.getName()).seatNo(command.getSeatNo()).carNo(command.getCarNo()).build();
//		// 設置進 Context 上下文
//		ContextHolder.setBaseEvent(event);
	}

	/**
	 * refund
	 * 
	 * @param command
	 */
	public void refund(RefundTicketCommand command) {
		this.status = TicketStatus.REFUNDED;
		this.activeFlag = YesNo.N;

//		// 建立一個 Event
//		BookSeatEvent event = BookSeatEvent.builder().eventLogUuid(UUID.randomUUID().toString()).targetId(this.uuid)
//				.takeDate(DateTransformUtil.transformLocalDateToString(command.getTakeDate()))
//				.action(TicketAction.REFUNDED.getName()).seatNo(command.getSeatNo()).carNo(command.getCarNo()).build();
//		// 設置進 Context 上下文
//		ContextHolder.setBaseEvent(event);

	}
}
