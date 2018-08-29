package com.dl.task.xianfeng;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.task.configurer.XianFengPayConfig;
import com.ucf.sdk.UcfForOnline;
import com.ucf.sdk.util.UnRepeatCodeGenerator;

/**
 * 余额查询
 * @author Administrator
 *
 */
@Slf4j
public class XianfengQueryBalanceEntity {
	public String service;
	public String secId;
	public String version;
	public String reqSn;
	public String merchantId;
	public String sign;

	
	public static final XianfengQueryBalanceEntity buildQueryBalanceEntity(XianFengPayConfig config) throws Exception {
		XianfengQueryBalanceEntity reqEntity = new XianfengQueryBalanceEntity();
		reqEntity.service = "REQ_QUERY_BALANCE";
		reqEntity.secId = config.getSEC_ID();
		reqEntity.version = config.getVERSION();
		reqEntity.merchantId = config.getMER_ID();
		String reqSn = UnRepeatCodeGenerator.createUnRepeatCode(reqEntity.merchantId, reqEntity.service, new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date()));
		reqEntity.reqSn = reqSn;
		String jsonStr = JSON.toJSONString(reqEntity);
		log.info("xinfeng queryBalance json str={}" ,jsonStr);
		JSONObject jsonObj = JSON.parseObject(jsonStr,JSONObject.class);
		Set<java.util.Map.Entry<String, Object>> mSet = jsonObj.entrySet();
		Iterator<java.util.Map.Entry<String, Object>> iterator = mSet.iterator();
		Map<String,String> mMap = new TreeMap<>(new PayKeyComparator());
		while(iterator.hasNext()) {
			java.util.Map.Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();
			String val = jsonObj.get(key).toString();
			mMap.put(key,val);
		}
		log.info("xainfeng querey balance map params={}",mMap);
		String signValue = UcfForOnline.createSign(config.getMER_RSAKEY(),"sign", mMap, "RSA");
		reqEntity.sign = signValue;
		return reqEntity;
	}
	
	
	public String buildReqStr() throws UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder();
		builder.append("service="+service);
		builder.append("&");
		builder.append("secId="+secId);
		builder.append("&");
		builder.append("version="+version);
		builder.append("&");
		builder.append("reqSn="+reqSn);
		builder.append("&");
		builder.append("merchantId="+merchantId);
		builder.append("&");
		builder.append("sign="+URLEncoder.encode(sign,"UTF-8"));
		return builder.toString();
	}
}
