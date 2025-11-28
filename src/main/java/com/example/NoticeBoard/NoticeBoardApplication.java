package com.example.NoticeBoard;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NoticeBoardApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.configure()
				.directory("./")  // 프로젝트 루트에서 읽기
				.ignoreIfMissing() // 파일 없으면 무시
				.load();

		// .env 내용을 시스템 환경변수로 등록
		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue())
		);

		SpringApplication.run(NoticeBoardApplication.class, args);
	}

}
