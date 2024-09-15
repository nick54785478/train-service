package com.example.demo.base.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.base.service.EventIdempotentLogService;

@Component
public class BaseEventHandler {

	@Autowired
	EventIdempotentLogService eventIdempotentLogService;

	public boolean checkEventIdempotency(BaseEvent event) {
		return eventIdempotentLogService.handleIdempotency(event);
	}
}
