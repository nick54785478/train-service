package com.example.demo.schedule;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.base.enums.YesNo;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.infra.repository.TrainSeatRepository;
import com.example.demo.util.DateTransformUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 檢查失效票券排程
 * */
@Slf4j
@Component
public class TicketCheckingSchedule {
	
	@Autowired
	TrainSeatRepository trainSeatRepository;
	
	// 每天晚上 12 點
	@Scheduled(cron = "0 0 0 * * ?")
	public void checkTickBooking() {
		Date now = new Date(); // 現在時間
		LocalDate localDate = DateTransformUtil.transformDateToLocalDate(now);
		log.info("現在時間: {}", now);
		
		List<TrainSeat> trainSeatList = trainSeatRepository.findByActiveFlagAndTakeDateBefore(YesNo.Y, localDate);
		log.debug("座位清單: {}", trainSeatList);
		trainSeatList.stream().forEach(e -> {
			e.inactive();
		});
		
		trainSeatRepository.saveAll(trainSeatList);
	}

}
