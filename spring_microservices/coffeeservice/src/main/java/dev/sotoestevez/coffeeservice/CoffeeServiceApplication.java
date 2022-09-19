package dev.sotoestevez.coffeeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class CoffeeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoffeeServiceApplication.class, args);
	}

}
