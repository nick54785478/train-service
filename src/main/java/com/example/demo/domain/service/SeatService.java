package com.example.demo.domain.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.command.BaseIdempotentCommand;
import com.example.demo.base.enums.YesNo;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.service.BaseDomainService;
import com.example.demo.base.service.EventIdempotentLogService;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.setting.aggregate.ConfigurableSetting;
import com.example.demo.domain.share.SeatQueriedData;
import com.example.demo.domain.share.UnbookedSeatGottenData;
import com.example.demo.infra.repository.SettingRepository;
import com.example.demo.infra.repository.TrainSeatRepository;
import com.example.demo.util.SequenceGenerator;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SeatService extends BaseDomainService {

	private SettingRepository settingRepository;
	private TrainSeatRepository trainSeatRepository;
	private EventIdempotentLogService eventIdempotentLogService;

	/**
	 * 查詢該乘車時段已被預訂的車位
	 * 
	 * @param trainUuid
	 * @param takeDate
	 * @return 車位資料
	 */
	public List<SeatQueriedData> queryBookedSeats(String trainUuid, LocalDate takeDate) {
		List<TrainSeat> trainSeats = trainSeatRepository.findByTakeDateAndTrainUuidAndBookedAndActiveFlag(takeDate,
				trainUuid, YesNo.Y, YesNo.Y);
		return this.transformEntityToData(trainSeats, SeatQueriedData.class);
	}

	/**
	 * 取得座位代號及車廂編號
	 * 
	 * @param trainUuid
	 * @param takeDate
	 * @return TrainSeatGottenData
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
	public UnbookedSeatGottenData getUnbookedSeat(String trainUuid, LocalDate takeDate) {
		UnbookedSeatGottenData trainSeatGottenData = new UnbookedSeatGottenData();
		trainSeatGottenData.setTrainUuid(trainUuid);

		List<ConfigurableSetting> setting = settingRepository.findByDataTypeAndActiveFlag("SEAT_NO_LIST", YesNo.Y);
		List<String> seatNoList = setting.stream().findFirst().stream().map(ConfigurableSetting::getDescription)
				.collect(Collectors.toList());

		List<TrainSeat> trainSeats = trainSeatRepository.findByTrainUuidAndTakeDateAndActiveFlag(trainUuid, takeDate,
				YesNo.Y);

		// 若沒有任何預定資料
		if (trainSeats.isEmpty()) {
			this.getSeatNoAndCarNoRecursively(trainSeatGottenData);
			return trainSeatGottenData;
		}

		// 將 TrainSeat 依車廂編號分組，並提取各車廂有被預定的 seatNo
		Map<Long, List<String>> group = trainSeats.stream().collect(Collectors.groupingBy(TrainSeat::getCarNo,
				// 提取 seatNo 並收集為 List
				Collectors.mapping(TrainSeat::getSeatNo, Collectors.toList())));

		for (Map.Entry<Long, List<String>> entry : group.entrySet()) {
			Long key = entry.getKey();
			List<String> bookedSeatList = entry.getValue();
			// 兩個數量相等，代表位置已訂滿
			if (bookedSeatList.size() == seatNoList.size()) {
				continue;
			}

			// 過濾出該車廂未被預訂的 SeatNo
			List<String> unbooked = seatNoList.stream().filter(item -> !bookedSeatList.contains(item))
					.collect(Collectors.toList());
			// 遞迴取得 SeatNo
			this.getSeatNoAndCarNoRecursively(trainSeatGottenData, unbooked, key);

			// 所有位置都被取完了且尚未取得座位編號 SeatNo 及 車廂編號 CarNo，跳過重取
			if (unbooked.isEmpty() && Objects.isNull(trainSeatGottenData.getCarNo())
					&& StringUtils.isBlank(trainSeatGottenData.getSeatNo())) {
				continue;
			}
		}
		return trainSeatGottenData;
	}

	/**
	 * 遞迴取得 SeatNo(座位編號) 及 CarNo(車廂編號)，用於沒有任何被預訂的情況，避免在分布式環境中衝突取號
	 * 
	 * @param trainNoGottenData 回傳結果
	 */
	private void getSeatNoAndCarNoRecursively(UnbookedSeatGottenData trainSeatGottenData) {
		if (Objects.isNull(trainSeatGottenData.getCarNo()) && StringUtils.isBlank(trainSeatGottenData.getSeatNo())) {
			// 隨機取得座位號
			String seatNo = SequenceGenerator.generateSeatNumber();
			// 使用冪等機制
			BaseIdempotentCommand command = BaseIdempotentCommand.builder().eventLogUuid(UUID.randomUUID().toString())
					.targetId(seatNo).build();
			if (eventIdempotentLogService.handleIdempotency(command)) {
				// 直接從第一列列車去取
				trainSeatGottenData.setCarNo(1L);
				trainSeatGottenData.setSeatNo(seatNo);
			}
		} else {
			this.getSeatNoAndCarNoRecursively(trainSeatGottenData);
		}
	}

	/**
	 * 遞迴取得 SeatNo(座位編號) 及 CarNo(車廂編號)，避免在分布式環境中衝突取號
	 * 
	 * @param trainNoGottenData 回傳結果
	 * @param unbooked          未被預訂的位置清單
	 * @param key               車廂編號
	 */
	private void getSeatNoAndCarNoRecursively(UnbookedSeatGottenData trainSeatGottenData, List<String> unbooked,
			Long key) {
		if (Objects.isNull(trainSeatGottenData.getCarNo()) && StringUtils.isBlank(trainSeatGottenData.getSeatNo())) {

			if (unbooked.isEmpty()) {
				return;
			}

			unbooked.stream().findAny().ifPresent(seatNo -> {
				// 使用冪等機制
				BaseIdempotentCommand command = BaseIdempotentCommand.builder()
						.eventLogUuid(UUID.randomUUID().toString()).targetId(seatNo).build();
				if (eventIdempotentLogService.handleIdempotency(command)) {
					trainSeatGottenData.setCarNo(key); // 根據目前被遍歷的車廂編號設置
					trainSeatGottenData.setSeatNo(seatNo); // 設置隨機一筆 SeatNo
					// 設置完移除該 SeatNo
					unbooked.remove(seatNo);
				}
			});
		} else {
			// 沒取到再繼續執行
			this.getSeatNoAndCarNoRecursively(trainSeatGottenData, unbooked, key);
		}

	}
}
