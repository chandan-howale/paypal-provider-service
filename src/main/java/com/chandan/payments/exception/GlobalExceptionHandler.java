package com.chandan.payments.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.chandan.payments.constant.ErrorCodeEnum;
import com.chandan.payments.pojo.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(PaypalProviderException.class)
    public ResponseEntity<ErrorResponse> handlePaypalProviderException(PaypalProviderException ex) {
        log.error("Handling PaypalProviderException: {}", ex.getErrorMessage(), ex);
		
		ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode(), ex.getErrorMessage());

        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex) {
		log.error("Handling generic exception: {}", ex.getMessage(), ex);
		
		ErrorResponse errorResponse = new ErrorResponse(
				ErrorCodeEnum.GENERIC_ERROR.getErrorCode(),
				ErrorCodeEnum.GENERIC_ERROR.getErrorMessage());
		
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
