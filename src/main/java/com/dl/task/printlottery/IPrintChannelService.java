package com.dl.task.printlottery;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.json.JSONObject;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.dl.base.enums.ThirdApiEnum;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.MD5Utils;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.DlTicketChannel;
import com.dl.task.model.LotteryThirdApiLog;
import com.dl.task.param.DlQueryStakeParam;
import com.dl.task.printlottery.requestDto.CommonQueryStakeParam;
import com.dl.task.printlottery.requestDto.CommonToQueryBanlanceParam;
import com.dl.task.printlottery.requestDto.CommonToStakeParam;
import com.dl.task.printlottery.requestDto.CommonToStakeParam.CommonPrintTicketOrderParam;
import com.dl.task.printlottery.responseDto.QueryPrintBalanceDTO;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO;
import com.google.common.collect.Lists;

public interface IPrintChannelService {
	 static final org.slf4j.Logger parentLog = org.slf4j.LoggerFactory.getLogger(IPrintChannelService.class);
	
	ToStakeResponseDTO toStake(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper);
	QueryStakeResponseDTO queryStake(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper);
	
	default CommonQueryStakeParam defaultCommonQueryStakeParam(List<DlPrintLottery> dlPrintLotterys,String merchant,String version){
		CommonQueryStakeParam queryStakeParam = new CommonQueryStakeParam();
		queryStakeParam.setMerchant(merchant);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		queryStakeParam.setTimestamp(sdf.format(new Date()));
		queryStakeParam.setVersion(version);
		List<String> collect = dlPrintLotterys.stream().map(print-> print.getTicketId()).collect(Collectors.toList());
		String[] orders = collect.toArray(new String[collect.size()]);
		queryStakeParam.setOrders(orders);
		return queryStakeParam;
	}
	/**
	 * 
	 * @param dlPrintLotterys 投注出票集合
	 * @param merchant 商户号
	 * @param version 版本号
	 * @return
	 */
	default CommonToStakeParam defaultCommonToStakeParam(List<DlPrintLottery> dlPrintLotterys,String merchant,String version){
		CommonToStakeParam commonToStakeParam = new CommonToStakeParam();
		commonToStakeParam.setMerchant(merchant);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		commonToStakeParam.setTimestamp(sdf.format(new Date()));
		commonToStakeParam.setVersion(version);
		List<CommonPrintTicketOrderParam> commonPrintTicketOrderParams = new LinkedList<CommonPrintTicketOrderParam>();
		dlPrintLotterys.forEach(lp->{
			CommonPrintTicketOrderParam commonPrintTicketOrderParam = new CommonPrintTicketOrderParam();
			commonPrintTicketOrderParam.setTicketId(lp.getTicketId());
			commonPrintTicketOrderParam.setGame(lp.getGame());
			commonPrintTicketOrderParam.setIssue(lp.getIssue());
			commonPrintTicketOrderParam.setPlayType(lp.getPlayType());
			commonPrintTicketOrderParam.setBetType(lp.getBetType());
			commonPrintTicketOrderParam.setTimes(lp.getTimes());
			commonPrintTicketOrderParam.setMoney(lp.getMoney().intValue());
			commonPrintTicketOrderParam.setStakes(lp.getStakes());
			commonPrintTicketOrderParams.add(commonPrintTicketOrderParam);
		});
		commonToStakeParam.setOrders(commonPrintTicketOrderParams);
		return commonToStakeParam; 
    }
	/**
	 * 
	 * @param dlPrintLotterys 投注出票集合
	 * @param merchant 商户号
	 * @param version 版本号
	 * @return
	 */
	default CommonToQueryBanlanceParam defaultCommonToQueryBanlanceParam(String merchant,String version){
		CommonToQueryBanlanceParam commonToStakeParam = new CommonToQueryBanlanceParam();
		commonToStakeParam.setMerchant(merchant);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		commonToStakeParam.setTimestamp(sdf.format(new Date()));
		commonToStakeParam.setVersion(version);
		return commonToStakeParam; 
    }
	default RestTemplate getRestTemplate(){
		 SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        //ms
        factory.setReadTimeout(50000);
        //ms
        factory.setConnectTimeout(60000);
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
	
	/**
	 * 通用的出票公司请求
	 * @param channelInfo 出票通道信息 账户、密码、出票地址不能为空
	 * @param dlPrintLotteryMapper 用来保存请求到数据库
	 * @param jo json参数
	 * @param inter 请求耳机地址
	 * @param thirdApiEnum 接口所属类型
	 * @return
	 */
	default String defaultCommonRestRequest(DlTicketChannel channelInfo,DlPrintLotteryMapper dlPrintLotteryMapper,JSONObject jo, String inter,ThirdApiEnum thirdApiEnum){
		RestTemplate rest = getRestTemplate();
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json;charset=UTF-8");
		headers.setContentType(type);
		String authStr = channelInfo.getTicketMerchant() + channelInfo.getTicketMerchantPassword() + jo.toString();
		String authorization = MD5Utils.MD5(authStr);
		headers.add("Authorization", authorization);
		String requestUrl = channelInfo.getTicketUrl() + inter;
		HttpEntity<JSONObject> requestEntity = new HttpEntity<JSONObject>(jo, headers);
		parentLog.info("通用的访问第三方请求reqTime={},url={},header={},requestParams={},",System.currentTimeMillis(),requestUrl,JSONHelper.bean2json(headers),JSONHelper.bean2json(requestEntity));
		String response = rest.postForObject(requestUrl, requestEntity, String.class);
		parentLog.info("restreqTime={}, response={}",System.currentTimeMillis(),response);
		String requestParam = JSONHelper.bean2json(requestEntity);
		LotteryThirdApiLog thirdApiLog = new LotteryThirdApiLog(requestUrl, thirdApiEnum.getCode(), requestParam, response);
		dlPrintLotteryMapper.saveLotteryThirdApiLog(thirdApiLog);
		return response;
	}
	QueryRewardResponseDTO queryRewardByLottery(List<DlPrintLottery> dlPrintLotterys,DlTicketChannel dlTicketChannel,DlPrintLotteryMapper dlPrintLotteryMapper);
	
	QueryRewardResponseDTO queryRewardByIssue(String issue,DlTicketChannel dlTicketChannel,DlPrintLotteryMapper dlPrintLotteryMapper);
	QueryPrintBalanceDTO queryBalance(DlTicketChannel channel,DlPrintLotteryMapper dlPrintLotteryMapper);
}
