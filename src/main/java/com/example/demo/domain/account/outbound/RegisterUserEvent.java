package com.example.demo.domain.account.outbound;

import com.example.demo.base.event.BaseEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RegisterUserEvent extends BaseEvent {

	private String name; // 使用者名稱

	private String email; // 信箱

	private String username; // 帳號

	private String password; // 密碼

	private String address;	// 地址
}
