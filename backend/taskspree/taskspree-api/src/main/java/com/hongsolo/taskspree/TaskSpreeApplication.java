package com.hongsolo.taskspree;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaskSpreeApplication {
	public static void main(String[] args) {

        SpringApplication.run(TaskSpreeApplication.class, args);
	}
}
