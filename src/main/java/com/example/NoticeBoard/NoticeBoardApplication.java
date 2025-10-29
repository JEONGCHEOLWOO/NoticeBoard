package com.example.NoticeBoard;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NoticeBoardApplication {

	public static void main(String[] args) {

		// .env 파일 읽기 (dotenv 라이브러리)
		Dotenv dotenv = Dotenv.load();

		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		System.setProperty("DB_URL", dotenv.get("DB_URL"));

		SpringApplication.run(NoticeBoardApplication.class, args);
	}

}
