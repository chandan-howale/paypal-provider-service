package com.chandan.payments.http;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpServiceEngine {
	
	private final RestClient restClient;

	public ResponseEntity<String> makeHttpCall(HttpRequest httpRequest) {
		log.info("Making HTTP call in HttpServiceEngine");
		

		// Making the actual HTTP call --->
		try {
			ResponseEntity<String> httpResponse = restClient
					.method(httpRequest.getHttpMethod())
					.uri(httpRequest.getUrl())
					.headers(
							restClientHeaders -> 
							restClientHeaders.addAll(
									httpRequest.getHttpHeaders()))
					.body(httpRequest.getBody())
					.retrieve()
					.toEntity(String.class);
			
			log.info("HTTP call completed in HttpServiceEngine with httpResponse: {}", httpResponse);
			
			return httpResponse;
			
		} catch (Exception e) {
			log.error("Exception occurred while making HTTP call in HttpServiceEngine: {}", e.getMessage(), e);
			
			throw new RuntimeException("Failed to make HTTP call in HttpServiceEngine" 
					+ " :" + e.getMessage());
		}
		
	}

}
