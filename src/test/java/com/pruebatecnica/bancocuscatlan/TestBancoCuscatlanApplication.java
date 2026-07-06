package com.pruebatecnica.bancocuscatlan;

import org.springframework.boot.SpringApplication;

public class TestBancoCuscatlanApplication {

	public static void main(String[] args) {
		SpringApplication.from(BancoCuscatlanApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
