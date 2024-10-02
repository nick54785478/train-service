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

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/booking")
public class BookingController {

	BookQueryService bookQueryService;

	/**
	 * 查詢該使用者的訂票資訊
	 * 
	 * @param username
	 */
	@GetMapping("/{username}")
	public ResponseEntity<BookingQueriedResource> queryBook(@PathVariable String username) {
		BookingQueriedData bookQueriedData = bookQueryService.queryBooking(username);
		return new ResponseEntity<BookingQueriedResource>(
				BaseDataTransformer.transformData(bookQueriedData, BookingQueriedResource.class), HttpStatus.OK);
	}
}
