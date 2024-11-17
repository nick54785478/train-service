package com.example.demo.domain.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.base.exception.ValidationException;
import com.example.demo.base.service.BaseDomainService;
import com.example.demo.domain.share.StopQueriedData;
import com.example.demo.domain.share.TrainCreatedData;
import com.example.demo.domain.share.TrainDetailQueriedData;
import com.example.demo.domain.share.TrainQueriedData;
import com.example.demo.domain.ticket.aggregate.Ticket;
import com.example.demo.domain.train.aggregate.Train;
import com.example.demo.domain.train.aggregate.entity.TrainStop;
import com.example.demo.domain.train.aggregate.vo.TrainKind;
import com.example.demo.domain.train.command.CreateTrainCommand;
import com.example.demo.domain.train.command.QueryTrainCommand;
import com.example.demo.infra.repository.TicketRepository;
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
	private TicketRepository ticketRepository;

	/**
	 * 新增火車資料
	 * 
	 * @param command
	 * @return uuid
	 */
	public TrainCreatedData create(CreateTrainCommand command) {
		this.checkBeforeCreate(command);
		Train train = new Train();
		train.create(command);
		Train savedEntity = trainRepository.save(train);
		return new TrainCreatedData(savedEntity.getUuid());
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

	@Transactional // 確保在整個方法執行期間 Session 是打開的，保持懶加載(否則會報錯)
	public List<TrainDetailQueriedData> filterTrainData(QueryTrainCommand command) {
		List<TrainDetailQueriedData> resList = new ArrayList<>();
		List<Train> trainList = trainRepository.findByCondition(command.getTrainNo(), command.getTime(),				command.getFromStop(), command.getToStop());

		// 透過起迄站取得 Ticket 資料
		List<Ticket> ticketList = ticketRepository.findByFromStopAndToStop(command.getFromStop(), command.getToStop());
		Map<String, Ticket> ticketMap = ticketList.stream()
				.collect(Collectors.toMap(Ticket::getTrainUuid, Function.identity()));

		trainList.stream().forEach(e -> {
			TrainDetailQueriedData trainData = new TrainDetailQueriedData();
			trainData.setUuid(e.getUuid());
			trainData.setTrainNo(e.getNumber());
			trainData.setFromStop(command.getFromStop());
			trainData.setToStop(command.getToStop());
			trainData.setKind(e.getKind().getLabel());

			if (!Objects.isNull(ticketMap.get(e.getUuid()))) {
				var ticket = ticketMap.get(e.getUuid());
				trainData.setPrice(ticket.getPrice());
				trainData.setTicketUuid(ticket.getTicketNo());
			}

			// 將停靠站清單轉為 Map <站名, Entity>
			Map<String, TrainStop> stopMap = e.getStops().stream()
					.collect(Collectors.toMap(TrainStop::getName, Function.identity()));
			// 起站時間
			if (!Objects.isNull(stopMap.get(trainData.getFromStop()))) {
				trainData.setFromStopTime(stopMap.get(trainData.getFromStop()).getTime());
			}
			// 迄站時間
			if (!Objects.isNull(stopMap.get(trainData.getToStop()))) {
				trainData.setToStopTime(stopMap.get(trainData.getToStop()).getTime());
			}

			List<StopQueriedData> stopResource = this.transformEntityToData(e.getStops(), StopQueriedData.class);

			// 依 SEQ 升序排序
			stopResource.sort(Comparator.comparingInt(StopQueriedData::getSeq));
			trainData.setStops(stopResource);
			resList.add(trainData);
		});

		return resList;
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

}
