package com.aperture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ApertureApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApertureApplication.class, args);
	}

}
