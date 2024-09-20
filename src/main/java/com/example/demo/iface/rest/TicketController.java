package com.example.demo.iface.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.booking.command.CheckInTicketCommand;
import com.example.demo.domain.ticket.command.CreateTicketCommand;
import com.example.demo.iface.dto.BookTicketResource;
import com.example.demo.iface.dto.CheckInTicketResource;
import com.example.demo.iface.dto.CreateTicketResource;
import com.example.demo.iface.dto.TicketBookedResource;
import com.example.demo.iface.dto.TicketCheckedInResource;
import com.example.demo.iface.dto.TicketCreatedResource;
import com.example.demo.service.TicketCommandService;
import com.example.demo.util.BaseDataTransformer;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Validated
@RequestMapping("/api/v1/ticket")
@RestController
@AllArgsConstructor
public class TicketController {

	private TicketCommandService ticketCommandService;

	/**
	 * 建立 車票資料
	 * 
	 * @param resource
	 * @return ResponseEntity
	 */
	@PostMapping("")
	public ResponseEntity<TicketCreatedResource> createTicket(@Valid @RequestBody CreateTicketResource resource) {
		CreateTicketCommand command = BaseDataTransformer.transformData(resource, CreateTicketCommand.class);
		return new ResponseEntity<>(BaseDataTransformer.transformData(ticketCommandService.createTicket(command),
				TicketCreatedResource.class), HttpStatus.OK);
	}

	/**
	 * 預定 車票資料
	 * 
	 * @param resource
	 * @return ResponseEntity
	 */
	@PostMapping("/booking")
	public ResponseEntity<TicketBookedResource> bookTicket(@RequestBody BookTicketResource resource) {
		BookTicketCommand command = BaseDataTransformer.transformData(resource, BookTicketCommand.class);
		return new ResponseEntity<>(
				BaseDataTransformer.transformData(ticketCommandService.bookTicket(command), TicketBookedResource.class),
				HttpStatus.OK);
	}

	/**
	 * Check in 車票
	 * 
	 * @param resource
	 * @return ResponseEntity
	 */
	@PostMapping("/checkIn")
	public ResponseEntity<TicketCheckedInResource> bookTicket(@RequestBody CheckInTicketResource resource) {
		CheckInTicketCommand command = BaseDataTransformer.transformData(resource, CheckInTicketCommand.class);
		return new ResponseEntity<>(BaseDataTransformer.transformData(ticketCommandService.checkInTicket(command),
				TicketCheckedInResource.class), HttpStatus.OK);
	}

}
