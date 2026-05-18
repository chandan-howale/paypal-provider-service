package com.chandan.payments.paypal.res.error;

import lombok.Data;

@Data
public class PaypalErrorDetail {

    private String field;
    private String value;
    private String location;
    private String issue;
    private String description;
}
