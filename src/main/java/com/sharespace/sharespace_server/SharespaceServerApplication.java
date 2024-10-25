package com.sharespace.sharespace_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SharespaceServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SharespaceServerApplication.class, args);
	}

}
