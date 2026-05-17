package com.chandan.payments.service.interfaces;

import com.chandan.payments.pojo.CreateOrderReq;
import com.chandan.payments.pojo.OrderResponse;

public interface PaymentService {
	
	public OrderResponse createOrder(CreateOrderReq createOrderReq);
}
