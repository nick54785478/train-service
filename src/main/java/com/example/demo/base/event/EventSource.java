package com.example.demo.base.event;

import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "EVENT_SOURCE", uniqueConstraints = {
		@jakarta.persistence.UniqueConstraint(columnNames = { "targetId", "version" }) })
@EntityListeners(AuditingEntityListener.class)
public class EventSource {

	@Id
	@Column(name = "UUID")
	private String uuid;

	@Column(name = "AGGREGATE_ID")
	private String targetId; // 目標 UUID

	@Column(name = "USER_ID")
	private String userId; // 使用者帳號

	@Column(name = "EVENT_CLASS_NAME")
	private String className; // Event 類型

	@CreatedDate
	@Column(name = "OCCURED_AT")
	private Date occuredAt;

	@Column(name = "BODY")
	private String body; // Aggregate Root 實體 JSON

	@Column(name = "VERSION")
	private Long version;

}
