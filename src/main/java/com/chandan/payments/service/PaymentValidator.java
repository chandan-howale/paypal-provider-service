package com.chandan.payments.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.chandan.payments.constant.ErrorCodeEnum;
import com.chandan.payments.exception.PaypalProviderException;
import com.chandan.payments.pojo.CreateOrderReq;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentValidator {


	/*
	 * This method checks incoming create order request for null and invalid values.
	 * If there is any validation error, it throws PaypalProviderException with appropriate error code and message.
	 * If no error, then just runs till end and returns void.
	 * @param createOrderReq
	 */
	public void validateCreateOrderRequest(CreateOrderReq createOrderReq) {
		log.info("Validating create order request | CreateOrderReq: {}", createOrderReq);
		
		// if conditions to validate createOrderReq and their fields
		if (createOrderReq == null) {
			log.error("Create order request is null.");
			
			throw new PaypalProviderException(
					ErrorCodeEnum.INVALID_REQUEST.getErrorCode(),
					ErrorCodeEnum.INVALID_REQUEST.getErrorMessage(),
					HttpStatus.BAD_REQUEST);
		}
		
		if (createOrderReq.getAmount() == null ||  createOrderReq.getAmount() <= 0) {
			log.error("Invalid amount in create order request: {}", createOrderReq.getAmount());
			
			throw new PaypalProviderException(
					ErrorCodeEnum.INVALID_AMOUNT.getErrorCode(),
					ErrorCodeEnum.INVALID_AMOUNT.getErrorMessage(),
					HttpStatus.BAD_REQUEST);
		}
		
		if (createOrderReq.getCurrencyCode() == null || createOrderReq.getCurrencyCode().isBlank()) {
			log.error("Currency code is null or blank in create order request.");
			
			throw new PaypalProviderException(
					ErrorCodeEnum.CURRENCY_CODE_REQUIRED.getErrorCode(),
					ErrorCodeEnum.CURRENCY_CODE_REQUIRED.getErrorMessage(),
					HttpStatus.BAD_REQUEST);
		}
		
		//check returnUrl
		if (createOrderReq.getReturnUrl() == null || createOrderReq.getReturnUrl().isBlank()) {
			log.error("Return URL is null or blank in create order request.");
			
			throw new PaypalProviderException(
					ErrorCodeEnum.RETURN_URL_REQUIRED.getErrorCode(),
					ErrorCodeEnum.RETURN_URL_REQUIRED.getErrorMessage(),
					HttpStatus.BAD_REQUEST);
		}	
		
		//check cancelUrl
		if (createOrderReq.getCancelUrl() == null || createOrderReq.getCancelUrl().isBlank()) {
			log.error("Cancel URL is null or blank in create order request.");
			
			throw new PaypalProviderException(
					ErrorCodeEnum.CANCEL_URL_REQUIRED.getErrorCode(),
					ErrorCodeEnum.CANCEL_URL_REQUIRED.getErrorMessage(),
					HttpStatus.BAD_REQUEST);
		}
		log.info("Create order request validation passed.");
	}
	
}
