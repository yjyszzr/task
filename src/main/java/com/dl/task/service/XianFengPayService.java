package com.dl.task.service;

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.dl.task.configurer.XianFengPayConfig;
import com.dl.task.dto.XianfengQueryBalanceDto;
import com.dl.task.util.HttpUtil;
import com.dl.task.xianfeng.XianfengQueryBalanceEntity;
import com.ucf.sdk.util.AESCoder;

@Service
@Slf4j
public class XianFengPayService {
	@Resource
	private XianFengPayConfig xianFengPayConfig;
	
	/**
	 * 先锋支付查询用户余额
	 * @return
	 */
	public XianfengQueryBalanceDto queryBalance(){
		String reqStr = null;
		try{
			reqStr = XianfengQueryBalanceEntity.buildQueryBalanceEntity(xianFengPayConfig).buildReqStr();
		}catch(Exception e){
			log.error("先锋支付查询余额封装参数异常",e);
		}
		if(StringUtils.isEmpty(reqStr)){
			return null;
		}
		//生成data
		String url = xianFengPayConfig.getUCF_GATEWAY_URL() + "?" + reqStr;
		log.info("先锋支付查询用户余额,请求参数={}",url);
		String response = HttpUtil.sendMsg(null, url, Boolean.FALSE);
		log.info("先锋支付查询用户余额,response={}",response);
		if(StringUtils.isEmpty(response)){
			return null;
		}
		String dataResult=null;
		try {
			dataResult = AESCoder.decrypt(response,xianFengPayConfig.getMER_RSAKEY());
		} catch (Exception e) {
			log.error("先锋支付查询用户余额解析响应异常,response={},rasKey={}",response,xianFengPayConfig.getMER_RSAKEY(),e);
			return null;
		}
		XianfengQueryBalanceDto queryBalance = JSON.parseObject(dataResult,XianfengQueryBalanceDto.class);
		return queryBalance;
	}
}
