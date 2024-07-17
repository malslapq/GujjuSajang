package com.GujjuSajang.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = {"com.GujjuSajang.orders", "com.GujjuSajang.core"})
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class OrdersApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrdersApplication.class, args);
	}

}
