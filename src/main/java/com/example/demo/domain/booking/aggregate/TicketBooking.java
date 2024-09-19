package com.example.demo.domain.booking.aggregate;

import java.util.UUID;

import com.example.demo.base.context.ContextHolder;
import com.example.demo.base.entity.BaseEntity;
import com.example.demo.base.enums.YesNo;
import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.booking.outbound.BookTicketEvent;
import com.example.demo.util.DateTransformUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "TICKET_BOOKING")
public class TicketBooking extends BaseEntity {

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

	@Enumerated(EnumType.STRING)
	@Column(name = "ACTIVE_FLAG")
	private YesNo activeFlag; // 是否失效 (過期、取消訂位)

	public void create(BookTicketCommand command) {
		this.uuid = UUID.randomUUID().toString();
		this.trainUuid = command.getTrainUuid();
		this.ticketUuid = command.getTicketUuid();
		this.username = ContextHolder.getUsername();
		this.email = ContextHolder.getUsername();
		this.activeFlag = YesNo.Y;

		// 建立一個 Event
		BookTicketEvent event = BookTicketEvent.builder().eventLogUuid(UUID.randomUUID().toString()).targetId(this.uuid)
				.takeDate(DateTransformUtil.transformLocalDateToString(command.getTakeDate()))
				.seatNo(command.getSeatNo()).build();
		// 設置進 Context 上下文
		ContextHolder.setBaseEvent(event);
	}
}
