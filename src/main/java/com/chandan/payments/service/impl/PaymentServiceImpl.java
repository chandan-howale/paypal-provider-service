package com.chandan.payments.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.chandan.payments.http.HttpRequest;
import com.chandan.payments.http.HttpServiceEngine;
import com.chandan.payments.paypal.res.PaypalOrder;
import com.chandan.payments.pojo.CreateOrderReq;
import com.chandan.payments.pojo.OrderResponse;
import com.chandan.payments.service.TokenService;
import com.chandan.payments.service.helper.CreateOrderHelper;
import com.chandan.payments.service.interfaces.PaymentService;
import com.chandan.payments.util.JsonUtil;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final TokenService tokenService;
	
	private final JsonUtil jsonUtil;
	
	private final CreateOrderHelper createOrderHelper;
	
	private final HttpServiceEngine httpServiceEngine;
	
	@Override
	public OrderResponse createOrder(CreateOrderReq createOrderReq) {

		log.info("Creating order in PaymentServiceImpl | createOrderReq: {}", createOrderReq);

		String accessToken = tokenService.getAccessToken();
		log.info("Access Token retrieved: {}", accessToken);


		HttpRequest httpRequest = createOrderHelper.prepareCreateOrderHttpRequest(createOrderReq, accessToken);
		log.info("Prepared HttpRequest for OAuth call in TokenService: {}", httpRequest);
		
		ResponseEntity<String> successResponse = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("HTTP call response from HttpServiceEngine in TokenService: {}", successResponse);

		// use modelmapper to convert response body to PaypalOrder object
		PaypalOrder paypalOrder =  jsonUtil.fromJson(successResponse.getBody(), PaypalOrder.class);
		log.info("Parsed PaypalOrder from response: {}", paypalOrder);
		
		OrderResponse orderResponce = createOrderHelper.toOrderResponse(paypalOrder);
		log.info("Converted OrderResponse: {}", orderResponce);
		
		// TODO Failure/Timeout - proper response handling 
		
		return orderResponce;
	}

	@PostConstruct
	public void init() {
		log.info("PaymentServiceImpl initialized");
	}

}
