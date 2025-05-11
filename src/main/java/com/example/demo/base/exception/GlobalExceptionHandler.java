package com.example.demo.base.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.base.exception.response.BaseExceptionResponse;
import com.example.demo.base.util.BaseDataTransformer;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

/**
 * 全域例外處理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<BaseExceptionResponse> handleValidationException(ValidationException e) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseDataTransformer.transformData(e, BaseExceptionResponse.class));
	}

	@ResponseBody
	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<BaseExceptionResponse> handleExpiredJwtExceptionHandle(final ExpiredJwtException e) {
		log.error("Token已過期，請重新登入!", e);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new BaseExceptionResponse("401", "Token 已過期，請重新登入!"));
	}

	@ResponseBody
	@ExceptionHandler(SignatureException.class)
	public ResponseEntity<BaseExceptionResponse> SignatureExceptionHandler(final SignatureException e) {
		log.error("簽名有誤", e);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new BaseExceptionResponse("401", "Token 驗證不符，拒絕存取!"));
	}

}
