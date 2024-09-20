package com.example.demo.infra.event;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.base.repository.EventLogRepository;
import com.example.demo.domain.booking.outbound.TicketBookingEvent;
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
	RabbitmqService rabbitmqService;

	@Test
	void testPublish() {
		TicketBookingEvent event = TicketBookingEvent.builder().eventLogUuid("EventLogUuid").targetId("Booking UUID")
				.seatNo("4A").takeDate(DateTransformUtil.transformLocalDateToString(LocalDate.now())).build();

//		EventLog eventLog = EventLog.builder().uuid(uuid).targetId("火車UUID").className(testQueueName).userId("Nick123")
//				.body("Request Body").build();
//		

		// 儲存 EventLog
		rabbitmqService.publish(exchangeName, testQueueName, event);

	}

}
