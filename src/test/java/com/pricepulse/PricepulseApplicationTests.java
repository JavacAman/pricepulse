package com.pricepulse;

import com.pricepulse.config.TestMailConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestMailConfig.class)
class PricepulseApplicationTests {

    @Test
    void contextLoads() {
    }
}