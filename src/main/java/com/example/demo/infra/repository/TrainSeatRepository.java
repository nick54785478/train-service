package com.example.demo.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.base.enums.YesNo;
import com.example.demo.domain.seat.aggregate.TrainSeat;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface TrainSeatRepository extends JpaRepository<TrainSeat, Long> {
	
	List<TrainSeat> findByActiveFlagAndTakeDateBefore(YesNo activeFlag, LocalDate takeDate);

	TrainSeat findByBookUuidAndSeatNoAndTakeDateAndActiveFlag(String uuid, String seatNo, LocalDate takeDate,
			YesNo activeFlag);

	List<TrainSeat> findBySeatNoAndTakeDate(String seatNo, LocalDate takeDate);

	TrainSeat findByBookUuidAndTakeDateAndSeatNo(String bookUuid, LocalDate takeDate, String seatNo);

	TrainSeat findByTakeDateAndSeatNoAndTrainUuidAndBooked(LocalDate takeDate, String seatNo, String trainUuid,
			YesNo yesNo);
	
	List<TrainSeat> findByTakeDateAndTrainUuidAndBookedAndActiveFlag(LocalDate takeDate, String trainUuid,
			YesNo booked, YesNo activeFlag);
	
	List<TrainSeat> findByBookUuidIn(List<String> bookingList);
}
