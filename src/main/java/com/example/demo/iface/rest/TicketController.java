package com.example.demo.iface.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.booking.command.CheckInTicketCommand;
import com.example.demo.domain.booking.command.RefundTicketCommand;
import com.example.demo.domain.ticket.command.CreateTicketCommand;
import com.example.demo.iface.dto.BookTicketResource;
import com.example.demo.iface.dto.CheckInTicketResource;
import com.example.demo.iface.dto.CreateTicketResource;
import com.example.demo.iface.dto.RefundTicketResource;
import com.example.demo.iface.dto.TicketBookedResource;
import com.example.demo.iface.dto.TicketCheckedInResource;
import com.example.demo.iface.dto.TicketCreatedResource;
import com.example.demo.service.TicketCommandService;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/ticket")
@Tag(name = "Ticket API", description = "進行與車票領域相關動作")
public class TicketController {

	private TicketCommandService ticketCommandService;

	/**
	 * 建立車票資料
	 * 
	 * @param resource
	 * @return ResponseEntity
	 */
	@PostMapping("")
	@Operation(summary = "API - 建立車票資料", description = "建立車票資料。")
	public ResponseEntity<TicketCreatedResource> createTicket(
			@Parameter(description = "車票資訊") @Valid @RequestBody CreateTicketResource resource) {
		CreateTicketCommand command = BaseDataTransformer.transformData(resource, CreateTicketCommand.class);
		return new ResponseEntity<>(BaseDataTransformer.transformData(ticketCommandService.createTicket(command),
				TicketCreatedResource.class), HttpStatus.OK);
	}

	/**
	 * 針對某車次批次建立車票資料
	 * 
	 * @param resource
	 * @return ResponseEntity
	 */
	@PostMapping("/{trainNo}")
	@Operation(summary = "API - 針對某車次批次建立車票資料", description = "針對某車次批次建立車票資料。")
	public ResponseEntity<TicketCreatedResource> createTicket(
			@Parameter(description = "車次") @PathVariable Integer trainNo,
			@Parameter(description = "車票資訊") @Valid @RequestBody List<CreateTicketResource> resources) {

		// 將 train No 設置進該resource
		resources.stream().forEach(e -> e.setTrainNo(trainNo));
		List<CreateTicketCommand> commands = BaseDataTransformer.transformData(resources, CreateTicketCommand.class);
		
		return new ResponseEntity<>(BaseDataTransformer.transformData(ticketCommandService.createTickets(trainNo, commands),
				TicketCreatedResource.class), HttpStatus.OK);
	}

	/**
	 * 預定車票資料
	 * 
	 * @param resource
	 * @return ResponseEntity
	 */
	@PostMapping("/booking")
	@Operation(summary = "API - 預定車票資料", description = "預定車票資料。")
	public ResponseEntity<TicketBookedResource> bookTicket(
			@Parameter(description = "車票預定資訊") @RequestBody BookTicketResource resource) {
		BookTicketCommand command = BaseDataTransformer.transformData(resource, BookTicketCommand.class);
		return new ResponseEntity<>(
				BaseDataTransformer.transformData(ticketCommandService.bookTicket(command), TicketBookedResource.class),
				HttpStatus.OK);
	}

	/**
	 * 進行車票 Check in 動作
	 * 
	 * @param resource
	 * @return ResponseEntity
	 */
	@PostMapping("/checkIn")
	@Operation(summary = "API - 進行車票 Check in 動作", description = "進行車票 Check in 動作。")
	public ResponseEntity<TicketCheckedInResource> bookTicket(
			@Parameter(description = "車票 Check IN 資訊") @RequestBody CheckInTicketResource resource) {
		CheckInTicketCommand command = BaseDataTransformer.transformData(resource, CheckInTicketCommand.class);
		return new ResponseEntity<>(BaseDataTransformer.transformData(ticketCommandService.checkInTicket(command),
				TicketCheckedInResource.class), HttpStatus.OK);
	}

	/**
	 * 進行車票退票動作
	 * 
	 * @param resource
	 * @return ResponseEntity
	 */
	@PostMapping("/refund")
	@Operation(summary = "API - 進行車票退票動作", description = "進行車票退票動作。")
	public ResponseEntity<TicketCheckedInResource> refund(
			@Parameter(description = "車票退票資訊") @RequestBody RefundTicketResource resource) {
		RefundTicketCommand command = BaseDataTransformer.transformData(resource, RefundTicketCommand.class);
		return new ResponseEntity<>(BaseDataTransformer.transformData(ticketCommandService.refundTicket(command),
				TicketCheckedInResource.class), HttpStatus.OK);
	}

}
