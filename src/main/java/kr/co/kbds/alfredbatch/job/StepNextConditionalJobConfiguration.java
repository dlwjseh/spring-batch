package kr.co.kbds.alfredbatch.job;

import org.springframework.batch.core.ExitStatus;
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
@Configuration
public class StepNextConditionalJobConfiguration {
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job stepNextConditionalJob() {
		return jobBuilderFactory.get("stepNextConditionalJob")
				.start(conditionJobStep1())
					.on("FAILED") // FAILED일 경우 (※ on이 캐치하는 상태값은 BatchStatus가 아닌 ExitStatus임)
					.to(conditionJobStep3()) // step3으로 이동한다.
					.on("*") // step3결과와 상관없이
					.end() // step3으로 이동하면 Flow가 종료한다.
				.from(conditionJobStep1()) // step1로부터
					.on("*") // FAILED 외에 모든 경우
					.to(conditionJobStep2()) // step2로 이동한다.
					.next(conditionJobStep3()) // step2가 정상 종료되면 step3으로 이동한다.
					.on("*") // step3결과와 상관없이
					.end() // step3으로 이동하면 Flow가 종료한다.
				.end() // Job 종료
				.build();
	}

	@Bean
	public Step conditionJobStep1() {
		return stepBuilderFactory.get("step1")
				.tasklet((contribution, chunkContext) -> {
					log.info(">>>>> This is stepNextConditionJob Step1");

					/*
					  ExitStatus를 FAILD로 지정한다.
					  해당 status를 보고 flow가 진행된다.
					 */
					contribution.setExitStatus(ExitStatus.FAILED);

					return RepeatStatus.FINISHED;
				})
				.build();
	}

	@Bean
	public Step conditionJobStep2() {
		return stepBuilderFactory.get("step2")
				.tasklet((contribution, chunkContext) -> {
					log.info(">>>>> This is stepNextConditionJob Step2");
					return RepeatStatus.FINISHED;
				})
				.build();
	}

	@Bean
	public Step conditionJobStep3() {
		return stepBuilderFactory.get("step3")
				.tasklet((contribution, chunkContext) -> {
					log.info(">>>>> This is stepNextConditionJob Step3");
					return RepeatStatus.FINISHED;
				})
				.build();
	}

}
