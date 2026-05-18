package com.chandan.payments.util;

import com.chandan.payments.paypal.res.error.PaypalErrorDetail;
import com.chandan.payments.paypal.res.error.PaypalErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaypalOrderUtil {
	PaypalOrderUtil() {}
	
	public static String getPaypalErrorSummary(PaypalErrorResponse res) {
		log.info("Generating Paypal error summary from response: {}", res);
		
		if (res == null) {
			return "Unknown Paypal error.";
		}
		
		StringBuilder summary = new StringBuilder();
		
		//Append if not null
		appendIfNotNull(summary, res.getName());
		appendIfNotNull(summary, res.getMessage());
		appendIfNotNull(summary, res.getError());
		appendIfNotNull(summary, res.getErrorDescription());
		
		if (res.getDetails() != null && !res.getDetails().isEmpty()) {
			PaypalErrorDetail detail = res.getDetails().get(0);
			if (detail != null) {
				appendIfNotNull(summary, detail.getField());
				appendIfNotNull(summary, detail.getIssue());
				appendIfNotNull(summary, detail.getDescription());
			}
		}
		log.info("Generated Paypal error summary: {}", summary.toString());
		return summary.length() > 0 ? summary.toString() : "Unknown Paypal error.";
	}
	
	private static void appendIfNotNull(StringBuilder sb, String value) {
		if (value != null && !value.isBlank()) {
			if (sb.length() > 0) { sb.append(" | "); }
			sb.append(value.trim());
		}
		
	}

}
