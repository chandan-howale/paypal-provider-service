package com.chandan.payments.service.helper;

import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.chandan.payments.constant.Constant;
import com.chandan.payments.http.HttpRequest;
import com.chandan.payments.paypal.req.Amount;
import com.chandan.payments.paypal.req.ExperienceContext;
import com.chandan.payments.paypal.req.OrderRequest;
import com.chandan.payments.paypal.req.PaymentSource;
import com.chandan.payments.paypal.req.Paypal;
import com.chandan.payments.paypal.req.PurchaseUnit;
import com.chandan.payments.paypal.res.PaypalLink;
import com.chandan.payments.paypal.res.PaypalOrder;
import com.chandan.payments.pojo.CreateOrderReq;
import com.chandan.payments.pojo.OrderResponse;
import com.chandan.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateOrderHelper {

	private final JsonUtil jsonUtil;

	@Value("${paypal.create.order.url}")
	String createOrderUrl;

	public HttpRequest prepareCreateOrderHttpRequest(CreateOrderReq createOrderReq, String accessToken) {
		HttpHeaders headers = prepareReqHeader(accessToken);

		String requestAsJson = prepareReqBodyAsJson(createOrderReq);

		// create HttpRequest object
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl(createOrderUrl);
		httpRequest.setHttpHeaders(headers);
		httpRequest.setBody(requestAsJson);

		return httpRequest;
	}

	private String prepareReqBodyAsJson(CreateOrderReq createOrderReq) {
		// Setting values to create body section to pass with createOrder request call
		// -->
		// Amount
		Amount amount = new Amount();
		amount.setCurrencyCode(createOrderReq.getCurrencyCode());

		// read the amount from CreateOrderReq and convert to 2 decimal places string
		String amtStr = String.format(Constant.TWO_DECIMAL_FORMAT, createOrderReq.getAmount());
		amount.setValue(amtStr);

		// Purchase Unit
		PurchaseUnit unit = new PurchaseUnit();
		unit.setAmount(amount);

		// Experience Context
		ExperienceContext experienceContext = new ExperienceContext();
		experienceContext.setPaymentMethodPreference(Constant.IMMEDIATE_PAYMENT_REQUIRED);
		experienceContext.setLandingPage(Constant.LANDINGPAGE_LOGIN);
		experienceContext.setShippingPreference(Constant.SHIPPING_PREF_NO_SHIPPING);
		experienceContext.setUserAction(Constant.USER_ACTION_PAY_NOW);
		experienceContext.setReturnUrl(createOrderReq.getReturnUrl());
		experienceContext.setCancelUrl(createOrderReq.getCancelUrl());

		// PayPal
		Paypal paypal = new Paypal();
		paypal.setExperienceContext(experienceContext);

		// Payment Source
		PaymentSource ps = new PaymentSource();
		ps.setPaypal(paypal);

		// Root Object
		OrderRequest order = new OrderRequest();
		order.setIntent(Constant.INTENT_CAPTURE);
		order.setPurchaseUnits(Collections.singletonList(unit));
		order.setPaymentSource(ps);

		log.info("Constructed OrderRequest object: {}", order);

		// Convert to JSON
		String requestAsJson = jsonUtil.toJson(order);
		return requestAsJson;
	}

	private HttpHeaders prepareReqHeader(String accessToken) {
		// setting up header section of createOrder call --->
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		// set Paypal-Request-Id ==> UUID
		String uuid = UUID.randomUUID().toString();
		log.info("Generated UUID for Paypal-Request-Id: {}", uuid);

		headers.add(Constant.PAYPAL_REQUEST_ID, uuid);
		return headers;
	}

	public OrderResponse toOrderResponse(PaypalOrder paypalOrder) {
		log.info("Converting PaypalOrder to OrderResponse: {}", paypalOrder);

		OrderResponse response = new OrderResponse();
		response.setOrderId(paypalOrder.getId());
		response.setPaypalStatus(paypalOrder.getStatus());

		String redirectLink = paypalOrder.getLinks().stream()
				.filter(link -> "payer-action".equalsIgnoreCase(link.getRel())).findFirst().map(PaypalLink::getHref)
				.orElse(null);

		response.setRedirectUrl(redirectLink);
		log.info("Converted PaypalOrder to OrderResponse: {}", response);

		return response;
	}

}
