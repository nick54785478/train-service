package com.example.demo.domain.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.example.demo.base.exception.ValidationException;
import com.example.demo.base.service.BaseDomainService;
import com.example.demo.domain.share.StopSummaryQueriedData;
import com.example.demo.domain.share.TrainQueriedData;
import com.example.demo.domain.share.TrainSummaryQueriedData;
import com.example.demo.domain.train.aggregate.Train;
import com.example.demo.domain.train.aggregate.entity.TrainStop;
import com.example.demo.domain.train.aggregate.vo.TrainKind;
import com.example.demo.domain.train.command.CreateTrainCommand;
import com.example.demo.domain.train.command.QueryTrainCommand;
import com.example.demo.infra.repository.TrainRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Domain Service
 */
@Slf4j
@Service
@AllArgsConstructor
public class TrainService extends BaseDomainService {

	private TrainRepository trainRepository;

	/**
	 * 新增火車資料
	 * 
	 * @param command
	 * @return uuid
	 */
	public void create(CreateTrainCommand command) {
		this.checkBeforeCreate(command);
		Train train = new Train();
		train.create(command);
		trainRepository.save(train);
	}

	/**
	 * 透過號次查詢該火車資訊
	 * 
	 * @param trainNo
	 * @return 火車資訊
	 */
	@Transactional // 確保在整個方法執行期間 Session 是打開的，保持懶加載(否則會報錯)
	public TrainQueriedData query(Integer trainNo) {
		Train train = trainRepository.findByNumber(trainNo);
		if (Objects.isNull(train)) {
			throw new ValidationException("VALIDATE_FAILED", "查無此車次 " + trainNo);
		}
		return this.transformEntityToData(train, TrainQueriedData.class);
	}

	/**
	 * 透過條件過濾並查詢火車資訊
	 * 
	 * @param command
	 * @return 火車資訊
	 */
	@Transactional // 確保在整個方法執行期間 Session 是打開的，保持懶加載(否則會報錯)
	public List<TrainSummaryQueriedData> filterTrainData(QueryTrainCommand command) {
		List<TrainSummaryQueriedData> resList = new ArrayList<>();
		List<Train> trainList = trainRepository.findByCondition(command.getTrainNo(),
				StringUtils.isNotBlank(command.getTrainKind()) ? TrainKind.fromLabel(command.getTrainKind()).toString()
						: null,
				command.getTime(), command.getFromStop(), command.getToStop());

		trainList.stream().forEach(e -> {
			TrainSummaryQueriedData trainData = new TrainSummaryQueriedData();
			trainData.setUuid(e.getUuid());
			trainData.setTrainNo(e.getNumber());
			trainData.setKind(e.getKind().getLabel());

			// 取得起點站與終點站
			TrainStop[] station = getFirstAndTerminatedStation(e.getStops());
			this.setStopData(station, trainData);

			List<StopSummaryQueriedData> stopResource = this.transformEntityToData(e.getStops(),
					StopSummaryQueriedData.class);

			// 依 SEQ 升序排序
			stopResource.sort(Comparator.comparingInt(StopSummaryQueriedData::getSeq));
			trainData.setStops(stopResource);
			resList.add(trainData);
		});

		return resList;
	}

	/**
	 * 設置 Stop 資料
	 * 
	 * @param stationData 起始站與終點站資料
	 * @param trainData   火車查詢資料
	 */
	private void setStopData(TrainStop[] stationData, TrainSummaryQueriedData trainData) {
		TrainStop firstStop = stationData[0]; // 起點站
		TrainStop terminatedStop = stationData[1]; // 終點站
		trainData.setFromStop(firstStop.getName());
		trainData.setToStop(terminatedStop.getName());
		trainData.setFromStopTime(stationData[0].getTime()); // 起站時間
		trainData.setToStopTime(stationData[1].getTime());// 迄站時間

	}

	/**
	 * 透過停靠站查詢該火車資訊
	 * 
	 * @param fromStop 起站
	 * @param toStop   迄站
	 * @return 火車資訊
	 */
	@Transactional // 確保在整個方法執行期間 Session 是打開的，保持懶加載(否則會報錯)
	public List<TrainQueriedData> getTrainListBetweenStopSection(String fromStop, String toStop) {
		List<Train> trainList = trainRepository.findAll();

		List<TrainQueriedData> dataList = trainList.stream().filter(e -> {
			List<String> stopList = e.getStops().stream().sorted(Comparator.comparingInt(TrainStop::getSeq)) // 根據 SEQ
																												// 排序
					.map(TrainStop::getName).collect(Collectors.toList());
			// 起站 index
			int fromIndex = stopList.indexOf(fromStop);
			// 迄站 index
			int toIndex = stopList.indexOf(toStop);

			// 確保起站存在，迄站存在，且 起站不能在迄站後面
			return fromIndex >= 0 && toIndex >= 0 && fromIndex < toIndex;
		}).map(e -> this.transformEntityToData(e, TrainQueriedData.class)).collect(Collectors.toList());
		return this.transformEntityToData(dataList, TrainQueriedData.class);
	}

	/**
	 * 組合式檢核 i. 檢核車次是否已存在 ii. 檢核車種是否合法 iii. 檢核停靠站名是否重覆（相同站名不可重覆出現）
	 * 
	 * @param command
	 * @param true/false
	 */
	public Boolean checkBeforeCreate(CreateTrainCommand command) {

		// 檢核車次是否已存在 (車次不存在才能新增)
		Train train = trainRepository.findByNumber(command.getTrainNo());
		if (!Objects.isNull(train)) {
			log.error("火車車次:{} 已存在，新增失敗", command.getTrainNo());
			throw new ValidationException("VALIDATE_FAILED", "火車車次已存在，新增失敗");
		}

		// 檢核車種是否合法
		if (!TrainKind.checkTrainKind(command.getTrainKind())) {
			log.error("火車車種:{} 不合法，新增失敗", command.getTrainKind());
			throw new ValidationException("VALIDATE_FAILED", "車種不合法，新增失敗");
		}

		// 檢核停靠站名是否重覆（相同站名不可重覆出現）
		long count = command.getStops().stream().distinct().count();
		if (command.getStops().size() != count) {
			log.error("火車停靠站重複，新增失敗，車次列表:{}", command.getStops());
			throw new ValidationException("VALIDATE_FAILED", "火車停靠站重複，新增失敗");
		}

		return true;
	}

	/**
	 * 找出起始站與終點站
	 * 
	 * @param trainStops 車站列表
	 * @return TrainStop[]
	 */
	private TrainStop[] getFirstAndTerminatedStation(List<TrainStop> trainStops) {
		TrainStop[] result = trainStops.stream().collect(() -> new TrainStop[] { null, null }, (res, stop) -> {
			if (res[0] == null || stop.getSeq() < res[0].getSeq())
				res[0] = stop; // 最小
			if (res[1] == null || stop.getSeq() > res[1].getSeq())
				res[1] = stop; // 最大
		}, (res1, res2) -> {
			if (res1[0] == null || (res2[0] != null && res2[0].getSeq() < res1[0].getSeq()))
				res1[0] = res2[0];
			if (res1[1] == null || (res2[1] != null && res2[1].getSeq() > res1[1].getSeq()))
				res1[1] = res2[1];
		});
		return result;
	}
}
