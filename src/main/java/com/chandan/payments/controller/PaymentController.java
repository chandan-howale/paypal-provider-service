package com.chandan.payments.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.chandan.payments.pojo.CreateOrderReq;
import com.chandan.payments.pojo.OrderResponse;
import com.chandan.payments.service.interfaces.PaymentService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController {
	
	private final PaymentService paymentService;
	
	@PostMapping("/payments")
	public OrderResponse createOrder(@RequestBody CreateOrderReq createOrderReq) {
		log.info("Creating order in PayPal provider service | createOrderReq: {}", createOrderReq);
		
		OrderResponse response = paymentService.createOrder(createOrderReq);
		log.info("Order creation response from service: {}", response);
		
		return response;
	}
	
	@PostMapping("/payments/{orderId}/capture")
	public OrderResponse captureOrder(@PathVariable String orderId) {
	    log.info("Capture order in PayPal provider service | orderId: {}", orderId);

	    OrderResponse response = paymentService.captureOrder(orderId);

	    log.info("Capture order response from service: {}", response);
	    return response;
	}


	@PostConstruct
	void init() {
		log.info("PaymentController initialized"
				+ " | paymentService:{}", paymentService);
	}
	
}
