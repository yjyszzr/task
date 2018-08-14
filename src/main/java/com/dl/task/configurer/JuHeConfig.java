package com.dl.task.configurer;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class JuHeConfig {
	
	@Value("${juhe.sms.api.url}")
	private String smsApiUrl;
	
	@Value("${juhe.sms.key}")
	private String smsKey;
}
