package com.example.demo.domain.account.aggregate;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.demo.base.config.context.ContextHolder;
import com.example.demo.base.entity.BaseEntity;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.domain.account.command.CreateMoneyAccountCommand;
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
public class MoneyAccount extends BaseEntity {

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
		BaseEvent event = RegisterUserEvent.builder().name(this.name).targetId(this.uuid).eventLogUuid(UUID.randomUUID().toString())
				.username(this.username).email(this.email).build();
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
	

}
