package com.example.demo.domain.train.aggregate.entity;

import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

import com.example.demo.base.entity.BaseEntity;
import com.example.demo.base.enums.YesNo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "TRAIN_STOP")
public class TrainStop extends BaseEntity {

	@Id
	@Column(name = "UUID")
	private String uuid;

	@Transient
	private String u;

	@Column(name = "TRAIN_UUID")
	private String trainUuid;

	@Column(name = "SEQ")
	private Integer seq; // 停站順序

	@Column(name = "NAME")
	private String name; // 站名

	@Column(columnDefinition = "TIME")
	private LocalTime time; // 停站時間

	@Enumerated(EnumType.STRING)
	@Column(name = "DELETE_FLAG")
	private YesNo deleteFlag; // 是否失效

	/**
	 * 在持久化之前執行的方法。
	 */
	@PrePersist
	public void prePersist() {
		// 新增時沒有 UUID，設置 UUID
		if (Objects.isNull(this.uuid)) {
			this.uuid = this.u;
		}
	}

	/**
	 * 設置火車代碼
	 * 
	 * @param trainUuid
	 */
	public void setTrainUuid(String trainUuid) {
		this.trainUuid = trainUuid;
	}

	/**
	 * 建立停靠站資料
	 * 
	 * @param trainUuid
	 * @param seq
	 * @param name
	 * @param time
	 */
	public void create(String trainUuid, Integer seq, String name, String time) {
		this.u = UUID.randomUUID().toString();
		this.trainUuid = trainUuid;
		this.seq = seq;
		this.name = name;
		this.time = LocalTime.parse(time);
		this.deleteFlag = YesNo.N;
	}

}
