package com.example.demo.iface.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.share.BookingQueriedData;
import com.example.demo.iface.dto.BookingQueriedResource;
import com.example.demo.service.BookQueryService;
import com.example.demo.util.BaseDataTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/booking")
@Tag(name = "Booking API", description = "進行票券預訂領域相關動作")
public class BookingController {

	BookQueryService bookQueryService;

	/**
	 * 查詢該使用者的訂票資訊
	 * 
	 * @param username
	 */
	@GetMapping("/{username}")
	@Operation(summary = "API - 查詢該使用者的訂票資訊", description = "查詢該使用者的訂票資訊。")
	public ResponseEntity<BookingQueriedResource> queryBook(
			@Parameter(description = "使用者帳號")
			@PathVariable String username) {
		BookingQueriedData bookQueriedData = bookQueryService.queryBooking(username);
		return new ResponseEntity<BookingQueriedResource>(
				BaseDataTransformer.transformData(bookQueriedData, BookingQueriedResource.class), HttpStatus.OK);
	}
}
