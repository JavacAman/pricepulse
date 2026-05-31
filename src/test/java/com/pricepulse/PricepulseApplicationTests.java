package com.pricepulse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class PricepulseApplicationTests {

	@MockitoBean
	JavaMailSender mailSender;

	@Test
	void contextLoads() {
	}

}
