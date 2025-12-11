package org.example.vladtech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VladTechApplication {

	public static void main(String[] args) {
		SpringApplication.run(VladTechApplication.class, args);
	}

}
