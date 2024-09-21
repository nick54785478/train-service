package com.example.demo.schedule;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.base.entity.EventLog;
import com.example.demo.base.enums.EventLogSendQueueStatus;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.base.repository.EventLogRepository;
import com.example.demo.infra.event.RabbitmqService;
import com.example.demo.util.JsonParseUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EventRePublishSchedule {

	@Autowired
	private EventLogRepository eventLogRepository;
	@Autowired
	private RabbitmqService rabbitmqService;

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	@Scheduled(fixedDelayString = "60000", initialDelay = 1000L)
	public void republishEvent() {
		// 往前 3 分鐘
		Date time = new Date((new Date()).getTime() - 300000L);
		List<EventLog> eventLogList = eventLogRepository
				.findByStatusAndOccuredAtBefore(EventLogSendQueueStatus.INITIAL, time);

		if (!eventLogList.isEmpty()) {
			eventLogList.stream().forEach(eventLog -> {
				try {
					// 將字串轉為 Class<T>
					Class<?> clazz = Class.forName(eventLog.getClassName());
					BaseEvent event = (BaseEvent) JsonParseUtil.unserialize(eventLog.getBody(), clazz);
					log.debug("Event Data: {}", event);
					
					// Event 重發佈
					rabbitmqService.publish(exchangeName, eventLog.getTopic(), event);
					eventLog.publish(eventLog.getBody()); // 變更狀態為: 已發布

				} catch (ClassNotFoundException e) {
					log.error("Class 轉換發生錯誤 ", e);
				}
			});
			eventLogRepository.saveAll(eventLogList);
		}

	}

}
