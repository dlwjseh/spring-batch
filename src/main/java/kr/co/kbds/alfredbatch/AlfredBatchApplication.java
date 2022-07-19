package kr.co.kbds.alfredbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing // 배치기능 활성화
@SpringBootApplication
public class AlfredBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlfredBatchApplication.class, args);
	}

}
