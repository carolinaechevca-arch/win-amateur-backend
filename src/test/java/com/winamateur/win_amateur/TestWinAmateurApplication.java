package com.winamateur.win_amateur;

import org.springframework.boot.SpringApplication;

public class TestWinAmateurApplication {

	public static void main(String[] args) {
		SpringApplication.from(WinAmateurApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
