package com.sumit.flashsale.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WarehouseSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(WarehouseSimulatorApplication.class, args);
	}

}
