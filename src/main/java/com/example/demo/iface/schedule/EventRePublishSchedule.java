package com.example.demo.iface.schedule;

import java.util.Date;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.base.application.port.EventPublishPort;
import com.example.demo.base.infra.persistence.EventLogRepository;
import com.example.demo.base.shared.command.PublishEventCommand;
import com.example.demo.base.shared.entity.EventLog;
import com.example.demo.base.shared.enums.EventLogSendQueueStatus;
import com.example.demo.base.shared.event.BaseEvent;
import com.example.demo.util.JsonParseUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Event Republish 排程
 */
@Slf4j
@Component
public class EventRePublishSchedule {

	private EventLogRepository eventLogRepository;
	private EventPublishPort rabbitmqService;

	public EventRePublishSchedule(EventLogRepository eventLogRepository, EventPublishPort rabbitmqService) {
		this.eventLogRepository = eventLogRepository;
		this.rabbitmqService = rabbitmqService;
	}

	@Scheduled(fixedDelayString = "60000", initialDelay = 1000L)
	public void republishEvent() {
		// 往前 3 分鐘
		Date time = new Date((new Date()).getTime() - 300000L);
		List<EventLog> eventLogList = eventLogRepository.findByStatusAndOccuredAtBefore(EventLogSendQueueStatus.INITIAL,
				time);

		if (!eventLogList.isEmpty()) {
			eventLogList.stream().forEach(eventLog -> {
				try {
					// 將字串轉為 Class<T>
					Class<?> clazz = Class.forName(eventLog.getClassName());
					BaseEvent event = (BaseEvent) JsonParseUtil.unserialize(eventLog.getBody(), clazz);
					log.debug("Event Data: {}", event);

					// Event 重發佈
					PublishEventCommand<BaseEvent> publishCommand = PublishEventCommand.<BaseEvent>builder()
							.event(event).topic(eventLog.getTopic()).build();
					rabbitmqService.publish(publishCommand);
					eventLog.publish(eventLog.getBody()); // 變更狀態為: 已發布

				} catch (ClassNotFoundException e) {
					log.error("Class 轉換發生錯誤 ", e);
				}
			});
			eventLogRepository.saveAll(eventLogList);
		}

	}

}
