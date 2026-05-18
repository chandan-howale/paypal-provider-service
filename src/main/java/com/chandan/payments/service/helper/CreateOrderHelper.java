package com.chandan.payments.service.helper;

import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.chandan.payments.constant.Constant;
import com.chandan.payments.constant.ErrorCodeEnum;
import com.chandan.payments.exception.PaypalProviderException;
import com.chandan.payments.http.HttpRequest;
import com.chandan.payments.paypal.req.Amount;
import com.chandan.payments.paypal.req.ExperienceContext;
import com.chandan.payments.paypal.req.OrderRequest;
import com.chandan.payments.paypal.req.PaymentSource;
import com.chandan.payments.paypal.req.Paypal;
import com.chandan.payments.paypal.req.PurchaseUnit;
import com.chandan.payments.paypal.res.PaypalLink;
import com.chandan.payments.paypal.res.PaypalOrder;
import com.chandan.payments.paypal.res.error.PaypalErrorResponse;
import com.chandan.payments.pojo.CreateOrderReq;
import com.chandan.payments.pojo.OrderResponse;
import com.chandan.payments.util.JsonUtil;
import com.chandan.payments.util.PaypalOrderUtil;

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
		// Todo: commented for testing with paypal

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

	public OrderResponse handlePaypalResponse(ResponseEntity<String> httpResponse) {
		log.info("Handling PayPal response in PaymentServiceImpl | httpResponse: {}", httpResponse);

		if (httpResponse.getStatusCode().is2xxSuccessful()) { // success

			// use modelmapper to convert response body to PaypalOrder object
			PaypalOrder paypalOrder = jsonUtil.fromJson(httpResponse.getBody(), PaypalOrder.class);
			log.info("Parsed PaypalOrder from response: {}", paypalOrder);

			OrderResponse orderResponce = toOrderResponse(paypalOrder);
			log.info("Converted OrderResponse: {}", orderResponce);

			// if we get valid response with PAYER_ACTION_REQUIRED status & url & id, then
			// only it success and return it else it failed
			if (orderResponce != null && orderResponce.getOrderId() != null && !orderResponce.getOrderId().isEmpty()
					&& orderResponce.getPaypalStatus() != null
					&& orderResponce.getPaypalStatus().equalsIgnoreCase(Constant.PAYER_ACTION_REQUIRED)
					&& orderResponce.getRedirectUrl() != null && !orderResponce.getRedirectUrl().isEmpty()) {
				log.info("Order created successfully with PAYER_ACTION_REQUIRED status.");
				return orderResponce;
			}

			log.error("Order creation failed or incomplete. Invalid OrderResponse: {}", orderResponce);
		}

		if (httpResponse.getStatusCode().is4xxClientError() || httpResponse.getStatusCode().is5xxServerError()) {
			log.info("Received 4xx, 5xx error response from PayPal service..");

			PaypalErrorResponse paypalErrorRes = jsonUtil.fromJson(httpResponse.getBody(), PaypalErrorResponse.class);
			log.info("Parsed PaypalErrorResponse from httpResponse: {}", paypalErrorRes);

			String errorCode = ErrorCodeEnum.PAYPAL_ERROR.getErrorCode();
			String errorMessage = PaypalOrderUtil.getPaypalErrorSummary(paypalErrorRes);
			log.info("Generated PayPal error summary: {}", errorMessage);

			throw new PaypalProviderException(errorCode, errorMessage,
					HttpStatus.valueOf(httpResponse.getStatusCode().value()));

		}

		log.error("Unexpected response from PayPal service. httpResponse: {}", httpResponse);

		throw new PaypalProviderException(ErrorCodeEnum.PAYPAL_UNKNOWN_ERROR.getErrorCode(),
				ErrorCodeEnum.PAYPAL_UNKNOWN_ERROR.getErrorMessage(), HttpStatus.BAD_GATEWAY);

	}

}
