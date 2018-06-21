package com.dl.task.service;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.dl.base.configurer.RestTemplateConfig;
import com.dl.base.enums.ThirdApiEnum;
import com.dl.base.util.DateUtil;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.MD5Utils;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.dto.DlQueryStakeDTO;
import com.dl.task.dto.DlQueryStakeDTO.BackQueryStake;
import com.dl.task.dto.DlToStakeDTO;
import com.dl.task.dto.DlToStakeDTO.BackOrderDetail;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.LotteryThirdApiLog;
import com.dl.task.param.DlQueryStakeParam;
import com.dl.task.param.DlToStakeParam;
import com.dl.task.param.DlToStakeParam.PrintTicketOrderParam;

import net.sf.json.JSONObject;

@Service
@Transactional
public class DlPrintLotteryService {
	private final static Logger log = Logger.getLogger(DlPrintLotteryService.class);
	
    @Resource
    private DlPrintLotteryMapper dlPrintLotteryMapper;
    
    @Resource
	private RestTemplateConfig restTemplateConfig;
    
    @Resource
	private RestTemplate restTemplate;
    
    @Value("${print.ticket.url}")
	private String printTicketUrl;
	
	@Value("${print.ticket.merchant}")
	private String merchant;
	
	@Value("${print.ticket.merchantPassword}")
	private String merchantPassword;
    

	/**
	 * 定时任务去主动查询发票状态
	 */
	public void goQueryStake() {
		List<DlPrintLottery> prints = dlPrintLotteryMapper.getPrintIngLotterys();
		log.info("彩票出票状态查询数据："+prints.size());
		while(prints.size() > 0) {
			log.info("彩票出票状态查询数据还有："+prints.size());
			int endIndex = prints.size()>20?20:prints.size();
			List<DlPrintLottery> subList = prints.subList(0, endIndex);
			this.goQueryStake(subList);
			prints.removeAll(subList);
		}
	}
	//出票
	public void goPrintLottery() {
		String tokenId = "goPrintLottery" + DateUtil.getCurrentTimeLong();
		List<DlPrintLottery> lotteryPrintList = dlPrintLotteryMapper.lotteryPrintsByUnPrint();
	    log.info("request token: " + tokenId + " goPrintLottery 未出票数："+lotteryPrintList.size());
	    if(CollectionUtils.isNotEmpty(lotteryPrintList)) {
	    	while(lotteryPrintList.size() > 0) {
	    		int toIndex = lotteryPrintList.size() > 50?50:lotteryPrintList.size();
	    		List<DlPrintLottery> lotteryPrints = lotteryPrintList.subList(0, toIndex);
	    		log.info("request token: " + tokenId + "  go tostake size="+lotteryPrints.size());
	    		Set<String> errOrderSns = this.gotoStak(lotteryPrints, tokenId);
	    		log.info("request token: " + tokenId + "出票失败订单数："+errOrderSns.size());
	    		lotteryPrintList.removeAll(lotteryPrints);
	    	}
	    }
	}
	/**
	 * 投注接口（竞彩足球，game参数都是T51）
	 * @return
	 */
	private DlToStakeDTO toStake(DlToStakeParam param) {
		param.setTimestamp(DateUtil.getCurrentTimeString(DateUtil.getCurrentTimeLong().longValue(), DateUtil.datetimeFormat));
		JSONObject jo = JSONObject.fromObject(param);
		String backStr = getBackDateByJsonData(jo, "/stake");
		JSONObject backJo = JSONObject.fromObject(backStr);
		@SuppressWarnings("rawtypes")
		Map<String,Class> mapClass = new HashMap<String,Class>();
		mapClass.put("orders", BackOrderDetail.class);
		DlToStakeDTO dlToStakeDTO = (DlToStakeDTO) JSONObject.toBean(backJo, DlToStakeDTO.class, mapClass); 
		return dlToStakeDTO;
	}
	/**
	 * 获取返回信息
	 * @param jo
	 * @return
	 */
	private String getBackDateByJsonData(JSONObject jo, String inter) {
		String authStr = merchant + merchantPassword + jo.toString();
		ClientHttpRequestFactory clientFactory = restTemplateConfig.simpleClientHttpRequestFactory();
		RestTemplate rest = restTemplateConfig.restTemplate(clientFactory);
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json;charset=UTF-8");
		headers.setContentType(type);
		String authorization = MD5Utils.MD5(authStr);
		headers.add("Authorization", authorization);
		HttpEntity<JSONObject> requestEntity = new HttpEntity<JSONObject>(jo, headers);
		String requestUrl = printTicketUrl + inter;
		String response = rest.postForObject(requestUrl, requestEntity, String.class);
		String requestParam = JSONHelper.bean2json(requestEntity);
		try {
			//记录请求日志
			LotteryThirdApiLog thirdApiLog = new LotteryThirdApiLog(requestUrl, ThirdApiEnum.HE_NAN_LOTTERY.getCode(), requestParam, response);
			dlPrintLotteryMapper.saveLotteryThirdApiLog(thirdApiLog);
		} catch (Exception e) {
			log.error("出票请求日志记录失败：requestUrl:" + requestUrl + " ,requestparam:"+requestParam + " , response:"+response, e);
		}
		return response;
	}
	/**
	 * 调用第三方出票=
	 * @param successOrderSn
	 * @param lotteryPrintList
	 * @return 返回
	 */
	private Set<String> gotoStak(List<DlPrintLottery> lotteryPrints, String tokenId) {
		DlToStakeParam dlToStakeParam = new DlToStakeParam();
		dlToStakeParam.setMerchant(lotteryPrints.get(0).getMerchant());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dlToStakeParam.setTimestamp(sdf.format(new Date()));
		dlToStakeParam.setVersion("1.0");
		List<PrintTicketOrderParam> printTicketOrderParams = new LinkedList<PrintTicketOrderParam>();
		Map<String, String> ticketIdOrderSnMap = new HashMap<String, String>();
		Set<String> allOrderSns = new HashSet<String>(lotteryPrints.size());
		Set<String> successOrderSn = new HashSet<String>(lotteryPrints.size());
		lotteryPrints.forEach(lp->{
			PrintTicketOrderParam printTicketOrderParam = new PrintTicketOrderParam();
			printTicketOrderParam.setTicketId(lp.getTicketId());
			printTicketOrderParam.setGame(lp.getGame());
			printTicketOrderParam.setIssue(lp.getIssue());
			printTicketOrderParam.setPlayType(lp.getPlayType());
			printTicketOrderParam.setBetType(lp.getBetType());
			printTicketOrderParam.setTimes(lp.getTimes());
			printTicketOrderParam.setMoney(lp.getMoney().intValue());
			printTicketOrderParam.setStakes(lp.getStakes());
			printTicketOrderParams.add(printTicketOrderParam);
			ticketIdOrderSnMap.put(lp.getTicketId(), lp.getOrderSn());
			allOrderSns.add(lp.getOrderSn());
		});
		dlToStakeParam.setOrders(printTicketOrderParams);
		DlToStakeDTO dlToStakeDTO = this.toStake(dlToStakeParam);
		if(null != dlToStakeDTO && CollectionUtils.isNotEmpty(dlToStakeDTO.getOrders())) {
			log.info("request token: " + tokenId + "inf tostake orders");
			List<DlPrintLottery> lotteryPrintErrors = new LinkedList<DlPrintLottery>();
			List<DlPrintLottery> lotteryPrintSuccess = new LinkedList<DlPrintLottery>();
			for(BackOrderDetail backOrderDetail : dlToStakeDTO.getOrders()) {
				DlPrintLottery lotteryPrint = new DlPrintLottery();
				lotteryPrint.setTicketId(backOrderDetail.getTicketId());
				Integer errorCode = backOrderDetail.getErrorCode();
				if(0 == errorCode) {
					//出票中
					lotteryPrint.setStatus(3);
					lotteryPrintSuccess.add(lotteryPrint);
					successOrderSn.add(ticketIdOrderSnMap.get(backOrderDetail.getTicketId()));
				} else if(3002 == errorCode) {
					successOrderSn.add(ticketIdOrderSnMap.get(backOrderDetail.getTicketId()));
				} else {
					lotteryPrint.setErrorCode(errorCode);
					//出票失败
					lotteryPrint.setStatus(2);
					lotteryPrint.setPrintTime(new Date());
					lotteryPrintErrors.add(lotteryPrint);
				}
			}
			if(CollectionUtils.isNotEmpty(lotteryPrintErrors)) {
				log.info("request token: " + tokenId + "lotteryPrintErrors size = "+lotteryPrintErrors.size());
				long start = System.currentTimeMillis();
				int num = 0;
				for(DlPrintLottery lotteryPrint:lotteryPrintErrors) {
					int rst = dlPrintLotteryMapper.updatePrintErrorStatusByTicketId(lotteryPrint);
					num+=rst<0?0:rst;
				}
				long end = System.currentTimeMillis();
				log.info("request token: " + tokenId + "lotteryPrintErrors size = "+lotteryPrintErrors.size() +" rst size="+ num+ "  times=" + (end-start));
			}
			if(CollectionUtils.isNotEmpty(lotteryPrintSuccess)) {
				log.info("request token: " + tokenId + "lotteryPrintSuccess size="+lotteryPrintSuccess.size());
				long start = System.currentTimeMillis();
				int num = 0;
				for(DlPrintLottery lotteryPrint:lotteryPrintSuccess) {
					int rst = dlPrintLotteryMapper.updatePrintIngStatusByTicketId(lotteryPrint);
					num+=rst<0?0:rst;
				}
				long end = System.currentTimeMillis();
				log.info("request token: " + tokenId + "lotteryPrintSuccess size="+lotteryPrintSuccess.size()+" rst size="+ num + "  times=" + (end-start));
			}
		}
		allOrderSns.removeAll(successOrderSn);
		return allOrderSns;
	}
	/**
	 * 投注结果查询
	 * @return
	 */
	public DlQueryStakeDTO queryStake(DlQueryStakeParam param) {
		param.setTimestamp(DateUtil.getCurrentTimeString(DateUtil.getCurrentTimeLong().longValue(), DateUtil.datetimeFormat));
		JSONObject jo = JSONObject.fromObject(param);
		String backStr = getBackDateByJsonData(jo, "/stake_query");
		JSONObject backJo = JSONObject.fromObject(backStr);
		@SuppressWarnings("rawtypes")
		Map<String,Class> mapClass = new HashMap<String,Class>();
		mapClass.put("orders", BackQueryStake.class);
		DlQueryStakeDTO dlQueryStakeDTO = (DlQueryStakeDTO) JSONObject.toBean(backJo, DlQueryStakeDTO.class, mapClass); 
		return dlQueryStakeDTO;
	}
	//查询出票
	private void goQueryStake(List<DlPrintLottery> subList) {
		List<String> collect = subList.stream().map(print-> print.getTicketId()).collect(Collectors.toList());
		String[] orders = collect.toArray(new String[collect.size()]);
		Map<String, DlPrintLottery> map = new HashMap<String, DlPrintLottery>(subList.size());
		subList.forEach(item->map.put(item.getTicketId(), item));
		DlQueryStakeParam queryStakeParam = new DlQueryStakeParam();
		queryStakeParam.setMerchant(merchant);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		queryStakeParam.setTimestamp(sdf.format(new Date()));
		queryStakeParam.setVersion("1.0");
		queryStakeParam.setOrders(orders);
		DlQueryStakeDTO dlQueryStakeDTO = this.queryStake(queryStakeParam);
		String retCode = dlQueryStakeDTO.getRetCode();
		if("0".equals(retCode)) {
			List<BackQueryStake> queryStakes = dlQueryStakeDTO.getOrders();
			log.info("查询返回结果数据：size="+queryStakes.size());
			List<DlPrintLottery> lotteryPrints = new ArrayList<>(queryStakes.size());
//			List<LotteryPrintParam> lotteryPrintParams = new ArrayList<LotteryPrintParam>(queryStakes.size());
			for(BackQueryStake stake: queryStakes) {
				String ticketId = stake.getTicketId();
				DlPrintLottery lotteryPrint = map.get(ticketId);
				if(null != lotteryPrint) {
					Integer printStatus = stake.getPrintStatus();
					if(printStatus.equals(17)) {
						lotteryPrint.setStatus(2);
					}else if(printStatus.equals(16)) {
						lotteryPrint.setStatus(1);
					}else if(printStatus.equals(8)) {
						lotteryPrint.setStatus(3);
					} else {
						continue;
					}
					String game = lotteryPrint.getGame();
					lotteryPrint.setPlatformId(stake.getPlatformId());
					lotteryPrint.setPrintNo(stake.getPrintNo());
					lotteryPrint.setPrintSp(stake.getSp());
					lotteryPrint.setPrintStatus(printStatus);
					String printTimeStr = stake.getPrintTime();
					if(StringUtils.isNotBlank(printTimeStr)) {
						try {
							printTimeStr = printTimeStr.replaceAll("/", "-");
							Date printTime = sdf.parse(printTimeStr);
							lotteryPrint.setPrintTime(printTime);
						} catch (ParseException e) {
							log.error("订单编号：" + stake.getTicketId() + "，出票回调，时间转换异常", e);
							continue;
						}
					}
					lotteryPrints.add(lotteryPrint);
					/*if(printSp != null) {
						LotteryPrintParam lotteryPrintParam = new LotteryPrintParam();
						lotteryPrintParam.setOrderSn(lotteryPrint.getOrderSn());
						lotteryPrintParam.setAcceptTime(lotteryPrint.getAcceptTime());
						if(printTime != null) {
							lotteryPrintParam.setTicketTime(DateUtil.getCurrentTimeLong(printTime.getTime()/1000));
						}
						lotteryPrintParam.setPrintSp(printSp);
						lotteryPrintParams.add(lotteryPrintParam);
					}*/
				}
			}
			log.info("goQueryStake orders size=" + orders.length +" -> updateLotteryPrintByCallBack size:"+lotteryPrints.size());
			if(CollectionUtils.isNotEmpty(lotteryPrints)) {
				for(DlPrintLottery print: lotteryPrints) {
					dlPrintLotteryMapper.updateLotteryPrintByCallBack(print);
				}
			}
			/*if(CollectionUtils.isNotEmpty(lotteryPrintParams)) {
				orderService.updateOrderInfoByPrint(lotteryPrintParams);
			}*/
		}
	}
}
