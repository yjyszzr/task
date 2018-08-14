package com.dl.task.util;

import java.util.HashMap;
import java.util.Map;

import com.dl.base.util.JSONHelper;

import lombok.Data;

public class DingDingUtil {
	
	public static String sendDingDingMsg(String url,String content,String[] mobiles){
		String response="";
		DingDingRequestDto dto = new DingDingRequestDto();
		dto.setMsgtype("text");
		Map<String,String> contentMap = new HashMap<String, String>();
		contentMap.put("content", content);
		dto.setText(contentMap);
		Map<String,String> atMap = new HashMap<String, String>();
		dto.setAt(atMap);
		HttpUtil.sendMsg(JSONHelper.bean2json(dto), url, Boolean.TRUE);
		return response;
	}
	@Data
	public static class DingDingRequestDto{
		private String msgtype;
		private Map<String,String> text;
		private Map<String,String> at;
	}
}
