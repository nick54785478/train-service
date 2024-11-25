package com.example.demo.domain.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.base.exception.ValidationException;
import com.example.demo.base.service.BaseDomainService;
import com.example.demo.domain.share.TicketCreatedData;
import com.example.demo.domain.ticket.aggregate.Ticket;
import com.example.demo.domain.ticket.command.CreateTicketCommand;
import com.example.demo.domain.train.aggregate.Train;
import com.example.demo.infra.repository.TicketRepository;
import com.example.demo.infra.repository.TrainRepository;

import lombok.AllArgsConstructor;

/**
 * Domain Service
 */
@Service
@AllArgsConstructor
public class TicketService extends BaseDomainService {

	private TrainRepository trainRepository;
	private TicketRepository ticketRepository;

	/**
	 * 新增車票資料
	 * 
	 * @param command
	 * @return TicketCreatedData
	 */
	public TicketCreatedData create(CreateTicketCommand command) {
		Train train = trainRepository.findByNumber(command.getTrainNo());

		// 領域檢核 檢查車次是否存在(車次存在才可以新增)
		if (Objects.isNull(train)) {
			throw new ValidationException("VALIDATE_FAILED", "該車次不存在");
		}
		Ticket ticket = new Ticket();
		ticket.create(command, train);
		Ticket savedEntity = ticketRepository.save(ticket); // 將ticket資料存入資料庫

		return new TicketCreatedData(savedEntity.getTicketNo());
	}

	/**
	 * 批次新增車票資料
	 * 
	 * @param trainNo
	 * @param commands
	 * @return TicketCreatedData
	 */
	public TicketCreatedData create(Integer trainNo, List<CreateTicketCommand> commands) {
		Train train = trainRepository.findByNumber(trainNo);

		// 領域檢核 檢查車次是否存在(車次存在才可以新增)
		if (Objects.isNull(train)) {
			throw new ValidationException("VALIDATE_FAILED", "該車次不存在");
		}
		List<Ticket> ticketList = commands.stream().map(command -> {
			Ticket ticket = new Ticket();
			ticket.create(command, train);
			return ticket;
		}).collect(Collectors.toList());
		ticketRepository.saveAll(ticketList); // 將ticket資料存入資料庫

		return new TicketCreatedData(train.getUuid());

	}

}
