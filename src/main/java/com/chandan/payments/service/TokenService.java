package com.chandan.payments.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.chandan.payments.constant.Constant;
import com.chandan.payments.http.HttpRequest;
import com.chandan.payments.http.HttpServiceEngine;
import com.chandan.payments.paypal.res.PaypalOAuthToken;
import com.chandan.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

	private static final int REDIS_ACCESS_TOKEN_EXPIRY_DIFF = 300;

	private static final String PAYPAL_ACCESS_TOKEN = "PAYPAL_ACCESS_TOKEN";

	private final HttpServiceEngine httpServiceEngine;

	@Value("${paypal.client.id}")
	String clientId;

	@Value("${paypal.client.secret}")
	String clientSecret;

	@Value("${paypal.oath.url}")
	String oathUrl;

	private final JsonUtil jsonUtil;

	private final RedisService redisService;
	
	/*
	 * 
	 * @return access token string
	 */
	public String getAccessToken() {

		log.info("Retrieving access token from TokenService");
		
		//redis based caching
		String accessToken = redisService.getValue(PAYPAL_ACCESS_TOKEN);
		
		log.info("Access token retrieved from Redis cache: {}", accessToken);

		if (accessToken != null) {
			log.info("Access token found in cache: {}", accessToken);
			return accessToken;
		}

		log.info("No cached accessToken found, Calling OAuth service to get new token.");

		// setting up header section of OAuth call --->
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(clientId, clientSecret);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// setting up body section of OAuth call --->
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add(Constant.GRANT_TYPE, Constant.CLIENT_CREDENTIALS);

		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl(oathUrl);
		httpRequest.setHttpHeaders(headers);
		httpRequest.setBody(formData);

		log.info("Prepared HttpRequest for OAuth call in TokenService: {}", httpRequest);

		ResponseEntity<String> response = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("HTTP call response from HttpServiceEngine in TokenService: {}", response);

		String tokenBody = response.getBody();
		log.info("Access token retrieved from OAuth service: {}", tokenBody);

		PaypalOAuthToken token = jsonUtil.fromJson(tokenBody, PaypalOAuthToken.class);
		log.info("Parsed OAuth token response using JsonUtil: {}", token);
		
		accessToken = token.getAccessToken();
		
		// Cache the access token in Redis with an expiry time (fetch from token response - 5 mins)
		
		redisService.setValueWithExpiry(
				PAYPAL_ACCESS_TOKEN,
				token.getAccessToken(), 
				token.getExpiresIn() - REDIS_ACCESS_TOKEN_EXPIRY_DIFF);
		
		
		log.info("Caching access token for future use..!");

		return accessToken;

	}

}
