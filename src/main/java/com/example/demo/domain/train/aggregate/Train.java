package com.example.demo.domain.train.aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.example.demo.base.entity.BaseEntity;
import com.example.demo.domain.train.aggregate.entity.TrainStop;
import com.example.demo.domain.train.aggregate.vo.TrainKind;
import com.example.demo.domain.train.command.CreateStopCommand;
import com.example.demo.domain.train.command.CreateTrainCommand;
import com.example.demo.domain.train.command.UpdateTrainCommand;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "TRAIN_UUID", updatable = false)
	private List<TrainStop> stops = new ArrayList<>();

	/**
	 * 在持久化之前執行的方法，用於設置 Train UUID。
	 */
	@PrePersist
	public void prePersist() {
		if (Objects.isNull(this.uuid)) {
			this.uuid = this.u;
			// 將 Train UUID 設置進 Stop
			for (TrainStop stop : stops) {
				stop.setTrainUuid(this.uuid);
				System.out.println("prePersist:" + stop);
			}
		}
	}

	/**
	 * Command Handler 用途: 新增車次
	 * 
	 * @param command
	 */
	public void create(CreateTrainCommand command) {
		this.u = UUID.randomUUID().toString();
		this.number = command.getTrainNo();
		this.kind = TrainKind.fromLabel(command.getTrainKind());
		// 將 Stop 資訊進行設置
		for (CreateStopCommand stop : command.getStops()) {
			TrainStop trainStop = new TrainStop();
			trainStop.create("", stop.getSeq(), stop.getStopName(), stop.getStopTime());
			stops.add(trainStop);
		}

	}

	/**
	 * Command Handler 用途: 更新車次資料
	 * 
	 * @param command
	 */
	public void update(UpdateTrainCommand command, String trainUuid) {
//		// 將 Stop 資料進行重設置(註.不能使用清空後重新新增，因為 update 比 create 更快)
//		Map<String, TrainStop> existMap = this.stops.stream()
//				.collect(Collectors.toMap(TrainStop::getUuid, Function.identity()));
//
//		// 新資料沒有但舊資料有 => 刪除
//		List<TrainStop> result = this.stops.stream()
//				.filter(existStop -> command.getStops().stream()
//						.noneMatch(newStop -> existStop.getUuid().equals(newStop.getUuid())))
//				.peek(TrainStop::delete) // peek 在收集到清單之前執行
//				.collect(Collectors.toList());
//
//		// 遍歷使用者的角色資料蒐集
//		command.getStops().stream().forEach(e -> {
//			// uuid 對不到 --> 新資料中有但舊資料沒有的資料 => 新增
//			if (Objects.isNull(existMap.get(e.getUuid()))) {
//				TrainStop trainStop = new TrainStop();
//				trainStop.create(this.uuid, e.getSeq(), e.getStopName(), e.getStopTime());
//				result.add(trainStop);
//			} else {
//				// 有對到 --> 新蓋舊
//				TrainStop old = existMap.get(e.getUuid());
//				old.update(e.getSeq(), e.getStopName(), e.getStopTime(), YesNo.valueOf(e.getDeleteFlag()));
//				result.add(old);
//			}
//		});

		this.stops.clear();

		List<String> uuids = this.stops.stream().map(TrainStop::getUuid).collect(Collectors.toList());
		// 移除 functions 中不存在於 Role Functions 的項目
		this.stops.removeIf(existingStop -> command.getStops().stream()
				.noneMatch(newStop -> StringUtils.equals(newStop.getUuid(), existingStop.getUuid())));
		System.out.println("sss:" + this.stops.size());
		// 增加新的 Role Function
		command.getStops().stream().forEach(newStop -> {
			if (!uuids.contains(newStop.getUuid())) {
				TrainStop trainStop = new TrainStop();
				trainStop.create(trainUuid, newStop.getSeq(), newStop.getStopName(), newStop.getStopTime());
				this.stops.add(trainStop);
			}
		});
//		this.stops = new ArrayList<>();
		System.out.println("result size:" + stops.size());
	}

}
