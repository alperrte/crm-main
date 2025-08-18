package com.example.ticket_service;

import org.springframework.boot.SpringApplication;

public class TestTicketServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(TicketServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
