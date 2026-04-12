package com.buixuantruong.shopapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class ShopappApplication {

	public static void main(String[] args) throws Exception {
		System.out.println(InetAddress.getByName("generativelanguage.googleapis.com"));
		SpringApplication.run(ShopappApplication.class, args);
	}

}
