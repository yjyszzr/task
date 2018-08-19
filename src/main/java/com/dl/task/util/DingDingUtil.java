package com.dl.task.util;

import java.util.Arrays;
import java.util.List;

import lombok.Data;

import com.dl.base.util.JSONHelper;

public class DingDingUtil {
	
	public static String sendDingDingMsg(String url,String content,String[] mobiles){
		String response="";
		DingDingRequestDto dto = new DingDingRequestDto();
		dto.setMsgtype("text");
		DingDingTextRequestDto text = new DingDingTextRequestDto();
		text.setContent(content);
		dto.setText(text);
		DingDingAtRequestDto at  = new DingDingAtRequestDto();
		if(mobiles==null||mobiles.length==0){
			at.setIsAtAll(Boolean.TRUE);
		}else{
			at.setAtMobiles(Arrays.asList(mobiles));
		    at.setIsAtAll(Boolean.FALSE);
		}
		dto.setAt(at);
		response = HttpUtil.sendMsg(JSONHelper.bean2json(dto), url, Boolean.TRUE);
		return response;
	}
	@Data
	public static class DingDingRequestDto{
		private String msgtype;
		private DingDingTextRequestDto text;
		private DingDingAtRequestDto at;
	}

	@Data
	public static class DingDingTextRequestDto{
		private String content;
	}
	@Data
	public static class DingDingAtRequestDto{
		private List<String> atMobiles;
		private Boolean isAtAll;
	}
}
