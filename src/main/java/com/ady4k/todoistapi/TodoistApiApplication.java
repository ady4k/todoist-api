package com.ady4k.todoistapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
public class TodoistApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoistApiApplication.class, args);
	}
}
