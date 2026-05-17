package com.chandan.payments.paypal.res;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PaypalOrder {

	private String id;
	private String status;

	@JsonProperty("payment_source")
	private PaymentSource paymentSource;

	private List<PaypalLink> links;

}
