package com.example.demo.infra.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.base.enums.EventLogSendQueueStatus;
import com.example.demo.base.event.EventLog;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

	EventLog findByUuid(String uuid);
	
	List<EventLog> findByStatusAndOccuredAtBefore(EventLogSendQueueStatus status, Date time);

}
