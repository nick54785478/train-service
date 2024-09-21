package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.base.service.BaseApplicationService;
import com.example.demo.domain.share.TrainCreatedData;
import com.example.demo.domain.train.command.CreateTrainCommand;
import com.example.demo.domain.train.service.TrainService;

import lombok.AllArgsConstructor;

@Service
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
@AllArgsConstructor
public class TrainCommandService extends BaseApplicationService {

	private TrainService trainService;

	/**
	 * 新增火車資訊
	 * 
	 * @param command
	 * @return UUID
	 */
	public TrainCreatedData createTrain(CreateTrainCommand command) {
		return trainService.create(command);
	}

}
