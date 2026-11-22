package com.example.hospitalmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HospitalmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HospitalmanagerApplication.class, args);
	}

}
