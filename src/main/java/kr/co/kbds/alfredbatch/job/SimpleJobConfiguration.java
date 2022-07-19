package kr.co.kbds.alfredbatch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration // Spring Batch의 모든 Job은 @Configuration으로 등록해서 사용
public class SimpleJobConfiguration {
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job simpleJob() {
		return jobBuilderFactory.get("simpleJob") // simpleJob 이란 이름의 Batch Job을 생성. job의 이름은 별도로 지정하지 않고, 이렇게 Builder를 통해 지정
				.start(simpleStep1())
				.build();
	}

	@Bean
	public Step simpleStep1() {
		return stepBuilderFactory.get("simpleStep1") // simpleStep1 이란 이름의 Batch Step을 생성. 마찬가지로 Builder를 통해 이름을 지정
				.tasklet((contribution, chunkContext) -> { // Step 안에서 수행될 기능들을 명시. Tasklet은 Step안에서 단일로 수행될 커스텀한 기능들을 선언할때 사용
					log.info(">>>>> This is Step1");
					return RepeatStatus.FINISHED;
				})
				.build();
	}
}
