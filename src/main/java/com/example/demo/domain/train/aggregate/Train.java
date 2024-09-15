package com.example.demo.domain.train.aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.example.demo.base.entity.BaseEntity;
import com.example.demo.domain.train.aggregate.entity.TrainStop;
import com.example.demo.domain.train.aggregate.vo.TrainKind;
import com.example.demo.domain.train.command.CreateStopCommand;
import com.example.demo.domain.train.command.CreateTrainCommand;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "TRAIN")
public class Train extends BaseEntity {

	@Id
	@Column(name = "UUID")
	private String uuid;

	@Transient
	private String u;

	@Column(name = "TRAIN_NO")
	private Integer number;

	@Column(name = "TRAIN_KIND")
	@Enumerated(EnumType.STRING)
	private TrainKind kind;

	// 使用懶加載，避免 N+1 query 效能問題
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "TRAIN_UUID", updatable = false)
	private List<TrainStop> stops = new ArrayList<>();

	/**
	 * 在持久化之前執行的方法，用於設置 Train UUID。
	 */
	@PrePersist
	public void prePersist() {
		if (Objects.isNull(this.uuid)) {
			this.uuid = UUID.randomUUID().toString();
			// 將 Train UUID 設置進 Stop
			for (TrainStop stop : stops) {
				stop.setTrainUuid(this.uuid);
			}
		}
	}

	/**
	 * Command Handler
	 * 
	 * 用途: 新增車次
	 */
	public void create(CreateTrainCommand command) {
		this.number = command.getTrainNo();
		this.kind = TrainKind.fromLabel(command.getTrainKind());
		// 將 Stop 資訊進行設置
		for (CreateStopCommand stop : command.getStops()) {
			TrainStop trainStop = new TrainStop();
			trainStop.create("", stop.getSeq(), stop.getStopName(), stop.getStopTime());
			stops.add(trainStop);
		}

	}

}
