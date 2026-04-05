package com.example.demo.domain.account.aggregate;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.demo.base.domain.aggregate.BaseAggreagteRoot;
import com.example.demo.base.infra.context.ContextHolder;
import com.example.demo.base.shared.event.BaseEvent;
import com.example.demo.domain.account.command.CreateMoneyAccountCommand;
import com.example.demo.domain.account.outbound.AccountTxEvent;
import com.example.demo.domain.account.outbound.RegisterUserEvent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "MONEY_ACCOUNT")
public class MoneyAccount extends BaseAggreagteRoot {

	@Id
	@Column(name = "UUID")
	private String uuid;

	@Column(name = "NAME")
	private String name; // 人名

	@Column(name = "USERNAME")
	private String username; // 帳號

	@Column(name = "EMAIL")
	private String email; // email

	@Column(name = "BALANCE")
	private BigDecimal balance = new BigDecimal("0"); // 餘額

	/**
	 * 新增帳戶訊息
	 * 
	 * @param command
	 */
	public void create(CreateMoneyAccountCommand command) {
		this.uuid = UUID.randomUUID().toString();
		this.username = command.getUsername();
		this.name = command.getName();
		this.email = command.getEmail();
		this.balance = this.balance.add(command.getMoney()); // 加上去

		// 建立 Event
		BaseEvent event = RegisterUserEvent.builder().name(this.name).targetId(this.uuid)
				.eventLogUuid(UUID.randomUUID().toString()).username(this.username).email(this.email).build();
		ContextHolder.setBaseEvent(event);
	}

	/**
	 * 儲值
	 * 
	 * @param balance 加總後的總合
	 */
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	/**
	 * 為預訂座位建立扣款事件
	 */
	public void chargeForBooking(BigDecimal balance, String eventId) {
		// 進行扣款
		BigDecimal result = this.balance.subtract(balance);

		// 賦予 Event Transaction ID 標註此次事件在同一次業務行為
		this.assignEventTxId(eventId);
		// 發布扣款事件
		AccountTxEvent event = AccountTxEvent.builder().targetId(this.uuid).money(result).build();

		// 設置 Domain Event
		this.raiseEvent(event);
	}

	/**
	 * 針對取消座位建立退款事件
	 */
	public void refundForSeatCancelling(BigDecimal refund) {
		// 將退款加總回去
		BigDecimal result = this.balance.add(refund);

		// 建立退款事件
		AccountTxEvent event = AccountTxEvent.builder().money(result).eventLogUuid(UUID.randomUUID().toString())
				.targetId(this.uuid).build();

		// 設置 Domain Event
		this.raiseEvent(event);

	}
}
