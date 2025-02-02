package com.example.demo.iface.rest;

import java.util.UUID;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.base.config.context.ContextHolder;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.infra.blob.MinioService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RequestMapping("/api/v1/test")
@RestController
@AllArgsConstructor
public class TestController {

	private MinioService minoService;

	@PostMapping("/checkContextHolder")
	public String checkToken() {
		BaseEvent event = BaseEvent.builder().eventLogUuid(UUID.randomUUID().toString()).build();
		ContextHolder.setBaseEvent(event);

		log.info("Event: {}", ContextHolder.getEvent());
		log.info("Username:{} ", ContextHolder.getUsername());
		log.info("JwToken: {} ", ContextHolder.getJwtoken());
		return "End";
	}
	

}
