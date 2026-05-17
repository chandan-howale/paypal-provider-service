package com.chandan.payments.service.impl;

import org.springframework.stereotype.Service;

import com.chandan.payments.service.TokenService;
import com.chandan.payments.service.interfaces.PaymentService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements  PaymentService {
	
	private final TokenService tokenService;

	@Override
	public String createOrder() {
		
		log.info("Creating order in PaymentServiceImpl");
		
		String accessToken = tokenService.getAccessToken();
		log.info("Access Token retrieved: {}", accessToken);
		
		/* TODO once the request and response structure is finalize, update this logic
		 	1. getAccessToken (OAuth)
		 	2. call paypal createOredr()
		 	3. Success/Failure/Timeout - proper response handling
		 	4. What to return to your calling service (payment-processing-service)
		 */
		
		
		return "Order created in service - " + accessToken;
	}
	
	@PostConstruct
	public void init() {
		log.info("PaymentServiceImpl initialized");
	}

}
