package com.chandan.payments.pojo;

import lombok.Data;

@Data
public class CreateOrderReq {
	private String currencyCode;
	private Double amount;
	private String returnUrl;
	private String cancelUrl;
}
