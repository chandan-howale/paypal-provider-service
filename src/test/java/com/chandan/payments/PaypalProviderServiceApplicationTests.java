package com.chandan.payments;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "paypal.client.id=test",
    "paypal.client.secret=test"
})
class PaypalProviderServiceApplicationTests {
	
    @Test
    void contextLoads() {
    }
}
