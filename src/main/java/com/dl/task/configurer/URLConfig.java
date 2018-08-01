package com.dl.task.configurer;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class URLConfig {
	@Value("${manualRewardToUserMoneyLimitUrl}")
	private String manualRewardToUserMoneyLimitUrl;
}
