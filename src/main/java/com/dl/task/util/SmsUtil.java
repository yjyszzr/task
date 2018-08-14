package com.dl.task.util;

import java.net.URLEncoder;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.dl.task.configurer.JuHeConfig;
@Slf4j
public class SmsUtil {
	/**
	 * 发短信
	 * @param mobile
	 * @param tplId
	 * @param tplValue
	 */
	public static void send(JuHeConfig juHeConfig,String mobile,String tplId,String tplValue){
		String smsUrl=juHeConfig.getSmsApiUrl();
		String smsKey=juHeConfig.getSmsKey();
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
	public  static String getTplValue(Map<String,String> tplValueMaps){
		StringBuffer tplValue=new StringBuffer();
		if(CollectionUtils.isEmpty(tplValueMaps)){
			return tplValue.toString();
		}
		for(String key:tplValueMaps.keySet()){
			tplValue.append("#"+key+"#=");
			tplValue.append(tplValueMaps.get(key));
			tplValue.append("&");
		}
		tplValue.deleteCharAt(tplValue.length()-1);
		return tplValue.toString();
	}
}
