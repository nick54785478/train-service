package com.example.demo.infra.event;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.base.application.port.EventPublishPort;
import com.example.demo.base.infra.persistence.EventLogRepository;
import com.example.demo.base.shared.command.PublishEventCommand;
import com.example.demo.domain.booking.outbound.BookSeatEvent;
import com.example.demo.util.DateTransformUtil;

@SpringBootTest
class RabbitmqServiceTest {

	@Value("${rabbitmq.test-topic-queue.name}")
	private String testQueueName;

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	@Autowired
	EventLogRepository eventLogRepository;

	@Autowired
	private EventPublishPort rabbitmqService;

	@Test
	void testPublish() {
		BookSeatEvent event = BookSeatEvent.builder().eventLogUuid("EventLogUuid").targetId("Booking UUID")
				.seatNo("4A").takeDate(LocalDate.now()).build();

//		EventLog eventLog = EventLog.builder().uuid(uuid).targetId("火車UUID").className(testQueueName).userId("Nick123")
//				.body("Request Body").build();
//		
		PublishEventCommand<BookSeatEvent> command = PublishEventCommand.<BookSeatEvent>builder().event(event)
				.topic(testQueueName).build();

		// 儲存 EventLog
		rabbitmqService.publish(command);

	}

}
