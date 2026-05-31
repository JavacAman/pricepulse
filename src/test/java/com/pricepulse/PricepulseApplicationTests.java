package com.pricepulse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class PricepulseApplicationTests {

	@MockBean
	JavaMailSender mailSender;

	@Test
	void contextLoads() {
	}

}
