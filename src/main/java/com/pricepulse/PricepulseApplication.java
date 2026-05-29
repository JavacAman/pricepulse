package com.pricepulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PricepulseApplication {

	public static void main(String[] args) {
		SpringApplication.run(PricepulseApplication.class, args);
	}

}
