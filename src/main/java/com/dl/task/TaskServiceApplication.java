package com.dl.task;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.dl.base.configurer.FeignConfiguration;
import com.dl.base.configurer.RestTemplateConfig;
import com.dl.base.configurer.WebMvcConfigurer;
import com.dl.task.configurer.Swagger2;
import com.dl.task.core.ProjectConstant;

@SpringBootApplication
@Import({ RestTemplateConfig.class, Swagger2.class, WebMvcConfigurer.class, FeignConfiguration.class })
@MapperScan(basePackages = { ProjectConstant.MAPPER_PACKAGE, "com.dl.task.dao" })
@EnableTransactionManagement
public class TaskServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskServiceApplication.class, args);
	}

}
