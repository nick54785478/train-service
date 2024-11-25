package com.example.demo.iface.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.ticket.command.CreateTicketCommand;
import com.example.demo.iface.dto.CreateTicketResource;
import com.example.demo.iface.dto.TicketCreatedResource;
import com.example.demo.service.TicketCommandService;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

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
			@Parameter(description = "車票資訊") @RequestBody CreateTicketResource resource) {
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
			@Parameter(description = "車票資訊") @RequestBody List<CreateTicketResource> resources) {

		// 將 train No 設置進該resource
		resources.stream().forEach(e -> e.setTrainNo(trainNo));
		List<CreateTicketCommand> commands = BaseDataTransformer.transformData(resources, CreateTicketCommand.class);

		return new ResponseEntity<>(BaseDataTransformer.transformData(
				ticketCommandService.createTickets(trainNo, commands), TicketCreatedResource.class), HttpStatus.OK);
	}

}
