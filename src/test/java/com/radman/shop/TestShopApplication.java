package com.radman.shop;

import org.springframework.boot.SpringApplication;

public class TestShopApplication {

	static void main(String[] args) {
		SpringApplication.from(ShopApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
