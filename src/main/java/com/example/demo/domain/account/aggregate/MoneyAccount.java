package com.example.demo.domain.account.aggregate;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.example.demo.base.entity.BaseEntity;
import com.example.demo.domain.account.command.CreateMoneyAccountCommand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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

	@Column(name = "USERNAME")
	private String username; // 人名

	@Column(name = "EMAIL")
	private String email; // email

	@Column(name = "BALANCE")
	private BigDecimal balance = new BigDecimal("0"); // 餘額

	/**
	 * 在持久化之前執行的方法。
	 */
	@PrePersist
	public void prePersist() {
		// 新增時沒有 UUID，設置 UUID
		if (Objects.isNull(this.uuid)) {
			this.uuid = UUID.randomUUID().toString();
		}
	}

	/**
	 * 新增帳戶訊息
	 * 
	 * @param command
	 */
	public void create(CreateMoneyAccountCommand command) {
		this.username = command.getName();
		this.email = command.getEmail();
		this.balance = this.balance.add(command.getMoney()); // 加上去
	}

	/**
	 * 儲值
	 * 
	 * @param command
	 */
	public void deposit(BigDecimal money) {
		this.balance = this.balance.add(money);
	}

}
