package com.sumit.flashsale.flash_sale_storefront;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class FlashSaleStorefrontApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashSaleStorefrontApplication.class, args);
	}

}
