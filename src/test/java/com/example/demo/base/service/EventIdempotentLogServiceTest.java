package com.example.demo.base.service;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.domain.booking.outbound.TicketBookingEvent;
import com.example.demo.util.DateTransformUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class EventIdempotentLogServiceTest {

	@Autowired
	EventIdempotentLogService eventIdempotentLogService;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {

	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testHandleIdempotency() {
		TicketBookingEvent event = TicketBookingEvent.builder().eventLogUuid("EventLogUuid").targetId("Booking UUID")
				.seatNo("4A").takeDate(DateTransformUtil.transformLocalDateToString(LocalDate.now())).build();

		// 冪等機制，防止重覆消費所帶來的副作用
		if (!eventIdempotentLogService.handleIdempotency(event)) {
			log.warn("Consume repeated: {}", event);
			return;
		}
		log.info("執行後續流程");

	}

	@Test
	void testHandleIdempotency2() {
		TicketBookingEvent event = TicketBookingEvent.builder().eventLogUuid("EventLogUuid").targetId("Booking UUID")
				.seatNo("4A").takeDate(DateTransformUtil.transformLocalDateToString(LocalDate.now())).build();
		// 冪等機制，防止重覆消費所帶來的副作用
		if (!eventIdempotentLogService.handleIdempotency(event)) {
			log.warn("Consume repeated: {}", event);
			return;
		}
		log.info("執行後續流程");
	}

}
