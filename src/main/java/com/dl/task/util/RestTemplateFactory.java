package com.dl.task.util;

import java.nio.charset.Charset;
import java.util.List;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;


public class RestTemplateFactory {
	public static RestTemplate getRestTemplate(){
		 SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
	        //ms
	        factory.setReadTimeout(5000);
	        //ms
	        factory.setConnectTimeout(15000);
	        RestTemplate restTemplate = new RestTemplate(factory);
	        List<HttpMessageConverter<?>> messageConverters = Lists.newArrayList();
	        for (HttpMessageConverter httpMessageConverter : restTemplate.getMessageConverters()) {
	            if (httpMessageConverter instanceof StringHttpMessageConverter) {
	                messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
	                continue;
	            }
	            messageConverters.add(httpMessageConverter);
	        }
	        restTemplate.setMessageConverters(messageConverters);
	        return restTemplate;
	}
}
