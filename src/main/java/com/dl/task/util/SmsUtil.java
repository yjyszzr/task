package com.dl.task.util;

import java.net.URLEncoder;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
@Slf4j
public class SmsUtil {
	/**
	 * 发短信
	 * @param mobile
	 * @param tplId
	 * @param tplValue
	 */
	public static void send(String mobile,String tplId,String tplValue){
		String smsUrl="";
		String smsKey="";
		RestTemplate rest = RestTemplateFactory.getRestTemplate();
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json;charset=UTF-8");
		headers.setContentType(type);
		try {
			tplValue = URLEncoder.encode(tplValue, "UTF-8");
		} catch (Exception e1) {
			log.error(e1.getMessage());
		}
		StringBuffer url = new StringBuffer(smsUrl);
		url.append("?mobile=" + mobile);
		url.append("&tpl_id=" + tplId);
		url.append("&tpl_value=" + tplValue);
		url.append("&key=" + smsKey);
		log.info("短信发送报警信息mobile={},url={}",mobile,url.toString());
		String response = rest.getForObject(url.toString(), String.class);
		log.info("短信发送报警信息mobile={},response={}",mobile,response);
	}
}
