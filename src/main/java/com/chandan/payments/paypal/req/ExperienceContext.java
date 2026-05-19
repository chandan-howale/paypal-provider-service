package com.chandan.payments.paypal.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ExperienceContext {
	
	@JsonProperty("payment_method_preference")
	private String paymentMethodPreference;
	
	@JsonProperty("landing_page")
	private String landingPage;
	
	@JsonProperty("shipping_preference")
	private String shippingPreference;
	
	@JsonProperty("user_action")
	private String userAction;
	
	@JsonProperty("return_url")
	private String returnUrl;
	
	@JsonProperty("cancel_url")
	private String cancelUrl;
	
}
