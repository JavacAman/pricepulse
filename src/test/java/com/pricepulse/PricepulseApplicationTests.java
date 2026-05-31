package com.pricepulse;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class PricepulseApplicationTests {

    @TestConfiguration
    static class MailMockConfig {
        @Bean
        @Primary
        public JavaMailSender javaMailSender() {
            return Mockito.mock(JavaMailSender.class);
        }
    }

    @Test
    void contextLoads() {
    }
}