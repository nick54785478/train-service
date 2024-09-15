package com.example.demo.schedule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.base.enums.EventLogSendQueueStatus;
import com.example.demo.base.event.EventLog;
import com.example.demo.domain.booking.outbound.BookTicketEvent;
import com.example.demo.infra.event.RabbitmqService;
import com.example.demo.infra.repository.EventLogRepository;
import com.example.demo.util.JsonParseUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EventRePublishSchedule {

	@Autowired
	private EventLogRepository eventLogRepository;
	@Autowired
	private RabbitmqService rabbitmqService;

	@Value("${rabbitmq.book-topic-queue.name}")
	private String bookingQueueName;

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	@Scheduled(fixedDelayString = "60000", initialDelay = 1000L)
	public void republishEvent() {
        
		List<EventLog> eventLogList = eventLogRepository.findByTopicAndStatus(bookingQueueName,
				EventLogSendQueueStatus.INITIAL);

		if (!eventLogList.isEmpty()) {
			eventLogList.stream().forEach(e -> {
				BookTicketEvent event = JsonParseUtil.unserialize(e.getBody(), BookTicketEvent.class);
				log.debug("Event Data: {}", event);
				rabbitmqService.publish(exchangeName, e.getTopic(), event);
				e.publish(); // 變更狀態為: 已發布
			});
			eventLogRepository.saveAll(eventLogList);
		}

	}

}
