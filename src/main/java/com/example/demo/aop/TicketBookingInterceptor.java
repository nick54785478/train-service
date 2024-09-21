package com.example.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.base.config.context.ContextHolder;
import com.example.demo.base.event.BaseEvent;
import com.example.demo.domain.booking.aggregate.TicketBooking;
import com.example.demo.domain.booking.service.TicketBookingService;
import com.example.demo.util.JsonParseUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 針對 TicketBooking 領域的攔截器，用於紀錄 Event Source
 */
@Aspect
@Component
@Slf4j
public class TicketBookingInterceptor {

	@Autowired
	private TicketBookingService ticketBookingService;

	/**
	 * 定義切入點，針對 MoneyAccountRepository 的 save 方法進行切入。
	 */
	@Pointcut("execution(* com.example.demo.infra.repository.TicketBookingRepository.save(..))")
	public void pointCut() {

	}

	/**
	 * 對 TicketBooking 領域資料 執行 EventSourcing
	 * 
	 * @param joinPoint 切入點
	 * @return 方法執行結果
	 * @throws Throwable 例外
	 */
	@Around("pointCut()")
	public Object executeEventSourcing(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();

		TicketBooking entity = (TicketBooking) args[0]; // 獲取 MoneyAccount
		log.info("[execute EventSourcing] Entity -> uuid:{}, username:{}, email:{}, status:{}", entity.getUuid(),
				entity.getUsername(), entity.getEmail(), entity.getStatus());

		BaseEvent event = ContextHolder.getEvent();
		log.info("eventLog: {}, targetId:{}", event.getEventLogUuid(), event.getTargetId());

		// 紀錄 EventSource
		String eventStreamId = entity.getClass().getSimpleName() + "-" + entity.getUuid();
		ticketBookingService.addEventSource(eventStreamId, JsonParseUtil.serialize(entity));

		// 執行後續流程
		return joinPoint.proceed();

	}

}
