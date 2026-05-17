package com.chandan.payments.paypal.req;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OrderRequest {

	private String intent;
	
	@JsonProperty("purchase_units")
	private List<PurchaseUnit> purchaseUnits;
	
	@JsonProperty("payment_source")
	private PaymentSource paymentSource;
	
}
