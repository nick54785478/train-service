package com.example.demo.iface.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.booking.command.BookTicketCommand;
import com.example.demo.domain.booking.command.CheckInTicketCommand;
import com.example.demo.domain.booking.command.RefundTicketCommand;
import com.example.demo.domain.share.BookingQueriedData;
import com.example.demo.iface.dto.BookTicketResource;
import com.example.demo.iface.dto.BookingQueriedResource;
import com.example.demo.iface.dto.CheckInTicketResource;
import com.example.demo.iface.dto.RefundTicketResource;
import com.example.demo.iface.dto.TicketBookedResource;
import com.example.demo.iface.dto.TicketCheckedInResource;
import com.example.demo.service.BookQueryService;
import com.example.demo.service.TicketCommandService;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/booking")
@Tag(name = "Booking API", description = "進行車票預訂領域相關動作")
public class BookingController {

	private BookQueryService bookQueryService;
	private TicketCommandService ticketCommandService;


	/**
	 * 查詢該使用者的訂票資訊
	 * 
	 * @param username
	 */
	@GetMapping("/{username}")
	@Operation(summary = "API - 查詢該使用者的訂票資訊", description = "查詢該使用者的訂票資訊。")
	public ResponseEntity<BookingQueriedResource> queryBooking(
			@Parameter(description = "使用者帳號")
			@PathVariable String username) {
		BookingQueriedData bookQueriedData = bookQueryService.queryBooking(username);
		return new ResponseEntity<>(
				BaseDataTransformer.transformData(bookQueriedData, BookingQueriedResource.class), HttpStatus.OK);
	}
	
	/**
	 * 預定車票資料
	 * 
	 * @param resource
	 * @return ResponseEntity
	 */
	@PostMapping("")
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
