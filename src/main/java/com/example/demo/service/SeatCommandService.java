package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.domain.seat.aggregate.TrainSeat;
import com.example.demo.domain.seat.command.CreateSeatCommand;
import com.example.demo.infra.repository.TrainSeatRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class SeatCommandService {

	private TrainSeatRepository trainSeatRepository;

	/**
	 * 進行劃位動作
	 * 
	 * @param command {@link CreateSeatCommand}
	 */
	@Transactional
	public void bookSeat(CreateSeatCommand command) {
		TrainSeat trainSeat = TrainSeat.create(command);
		trainSeatRepository.save(trainSeat);
	}

}
