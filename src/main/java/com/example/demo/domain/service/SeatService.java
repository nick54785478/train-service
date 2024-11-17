package com.example.demo.domain.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.base.enums.YesNo;
import com.example.demo.base.service.BaseDomainService;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.share.SeatQueriedData;
import com.example.demo.infra.repository.TrainSeatRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SeatService extends BaseDomainService {

	private TrainSeatRepository trainSeatRepository;

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
}
