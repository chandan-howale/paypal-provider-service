package com.chandan.payments.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.chandan.payments.http.HttpRequest;
import com.chandan.payments.http.HttpServiceEngine;
import com.chandan.payments.pojo.CreateOrderReq;
import com.chandan.payments.pojo.OrderResponse;
import com.chandan.payments.service.PaymentValidator;
import com.chandan.payments.service.TokenService;
import com.chandan.payments.service.helper.CreateOrderHelper;
import com.chandan.payments.service.interfaces.PaymentService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {


	private final TokenService tokenService;
		
	private final CreateOrderHelper createOrderHelper;
	
	private final HttpServiceEngine httpServiceEngine;
	
	private final PaymentValidator paymentValidator;
	
	@Override
	public OrderResponse createOrder(CreateOrderReq createOrderReq) {

		log.info("Creating order in PaymentServiceImpl | createOrderReq: {}", createOrderReq);
		
		paymentValidator.validateCreateOrderRequest(createOrderReq);
		log.info("Create order request validated successfully.");

		String accessToken = tokenService.getAccessToken();
		log.info("Access Token retrieved: {}", accessToken);


		HttpRequest httpRequest = createOrderHelper.prepareCreateOrderHttpRequest(createOrderReq, accessToken);
		log.info("Prepared HttpRequest for Create Order call in createOrderHelper: {}", httpRequest);
		
		ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("HTTP call response from HttpServiceEngine in TokenService: {}", httpResponse);

		OrderResponse orderResponce = createOrderHelper.handlePaypalResponse(httpResponse);
		
		return orderResponce;
	}

	

	@PostConstruct
	public void init() {
		log.info("PaymentServiceImpl initialized");
	}

}
