package com.dl.task;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.dl.base.configurer.FeignConfiguration;
import com.dl.base.configurer.RestTemplateConfig;
import com.dl.base.configurer.WebMvcConfigurer;
import com.dl.task.configurer.Swagger2;
import com.dl.task.core.ProjectConstant;

@SpringBootApplication
@Import({ RestTemplateConfig.class, Swagger2.class, WebMvcConfigurer.class, FeignConfiguration.class })
@MapperScan(basePackages = { ProjectConstant.MAPPER_PACKAGE, "com.dl.task.dao2" })
@EnableEurekaClient
@EnableFeignClients({"com.dl.shop.payment.api","com.dl.lottery.api","com.dl.storeH5.api"})
@EnableTransactionManagement
public class TaskServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskServiceApplication.class, args);
	}
    @Bean  
    public ScheduledThreadPoolExecutor scheduledExecutorService() {  
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);  
        return executor;  
    } 

}
