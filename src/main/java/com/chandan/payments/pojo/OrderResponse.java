package com.chandan.payments.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private String orderId;
    private String paypalStatus;
    private String redirectUrl;
}
