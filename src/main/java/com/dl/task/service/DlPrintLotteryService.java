package com.dl.task.service;

import io.jsonwebtoken.lang.Collections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.dl.base.configurer.RestTemplateConfig;
import com.dl.base.enums.MatchPlayTypeEnum;
import com.dl.base.enums.MatchResultCrsEnum;
import com.dl.base.enums.MatchResultHadEnum;
import com.dl.base.enums.MatchResultHafuEnum;
import com.dl.base.enums.SNBusinessCodeEnum;
import com.dl.base.enums.ThirdApiEnum;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.MD5Utils;
import com.dl.base.util.SNGenerator;
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.dao.PeriodRewardDetailMapper;
import com.dl.task.dao2.DlLeagueMatchResultMapper;
import com.dl.task.dao2.LotteryMatchMapper;
import com.dl.task.dto.DlJcZqMatchCellDTO;
import com.dl.task.dto.DlQueryPrizeFileDTO;
import com.dl.task.dto.DlQueryStakeDTO;
import com.dl.task.dto.DlQueryStakeDTO.BackQueryStake;
import com.dl.task.dto.DlToStakeDTO;
import com.dl.task.dto.DlToStakeDTO.BackOrderDetail;
import com.dl.task.dto.LotteryPrintDTO;
import com.dl.task.dto.MatchBetCellDTO;
import com.dl.task.dto.MatchBetPlayCellDTO;
import com.dl.task.dto.MatchBetPlayDTO;
import com.dl.task.dto.OrderDetailDataDTO;
import com.dl.task.dto.OrderInfoAndDetailDTO;
import com.dl.task.dto.OrderInfoDTO;
import com.dl.task.dto.XianDlQueryStakeDTO;
import com.dl.task.dto.XianDlQueryStakeDTO.XianBackQueryStake;
import com.dl.task.dto.XianDlToStakeDTO;
import com.dl.task.dto.XianDlToStakeDTO.XianBackOrderDetail;
import com.dl.task.model.BetResultInfo;
import com.dl.task.model.DlLeagueMatchResult;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.LotteryThirdApiLog;
import com.dl.task.param.DlJcZqMatchBetParam;
import com.dl.task.param.DlQueryPrizeFileParam;
import com.dl.task.param.DlQueryStakeParam;
import com.dl.task.param.DlToStakeParam;
import com.dl.task.param.DlToStakeParam.PrintTicketOrderParam;
import com.google.common.collect.Lists;

@Service
@Slf4j
public class DlPrintLotteryService {

	@Resource
	private DlPrintLotteryMapper dlPrintLotteryMapper;

	@Resource
	private DlLeagueMatchResultMapper dlLeagueMatchResultMapper;

	@Resource
	private PeriodRewardDetailMapper periodRewardDetailMapper;

	@Resource
	private DlLeagueMatchResultService matchResultService;

	@Resource
	private LotteryMatchMapper lotteryMatchMapper;
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
	
	@Value("${print.ticket.xian.url}")
	private String printTicketXianUrl;
	
	@Value("${print.ticket.xian.merchant}")
	private String xianMerchant;
	
	@Value("${print.ticket.xian.merchantPassword}")
	private String xianMerchantPassword;

	public void goQueryStake() {
		goQueryStakeHenan();
		goQueryStakeXian();
	}
	
	/**
	 * 定时任务去主动查询发票状态
	 */
	public void goQueryStakeXian() {
		List<DlPrintLottery> prints = dlPrintLotteryMapper.getPrintIngLotterysXian();
		log.info("西安 彩票出票状态查询数据："+prints.size());
		while(prints.size() > 0) {
			log.info("西安 彩票出票状态查询数据还有："+prints.size());
			int endIndex = prints.size()>20?20:prints.size();
			List<DlPrintLottery> subList = prints.subList(0, endIndex);
			List<String> collect = subList.stream().map(print-> print.getTicketId()).collect(Collectors.toList());
			String[] orders = collect.toArray(new String[collect.size()]);
			this.goQueryStakeXian(orders);
			prints.removeAll(subList);
		}
	}
	private void goQueryStakeXian(String[] orders) {
		DlQueryStakeParam queryStakeParam = new DlQueryStakeParam();
		queryStakeParam.setMerchant(xianMerchant);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		queryStakeParam.setTimestamp(sdf.format(new Date()));
		queryStakeParam.setVersion("1.0");
		queryStakeParam.setOrders(orders);
		XianDlQueryStakeDTO dlQueryStakeDTO = this.queryStakeXian(queryStakeParam);
		String retCode = dlQueryStakeDTO.getRetCode();
		if("0".equals(retCode)) {
			List<XianBackQueryStake> queryStakes = dlQueryStakeDTO.getOrders();
			log.info("西安  查询返回结果数据：size="+queryStakes.size());
			List<DlPrintLottery> lotteryPrints = new ArrayList<>(queryStakes.size());
			for(XianBackQueryStake stake: queryStakes) {
				String ticketId = stake.getTicketId();
				DlPrintLottery lotteryPrint = dlPrintLotteryMapper.selectDlPrintLotteryByTicketId(ticketId);
				if(null != lotteryPrint) {
					Integer printStatus = stake.getPrintStatus();
					if(printStatus.equals(ProjectConstant.PRINT_STATUS_FAIL)) {
						lotteryPrint.setStatus(2);
					}else if(printStatus.equals(ProjectConstant.PRINT_STATUS_SUCCESS)) {
						lotteryPrint.setStatus(1);
					}else if(printStatus.equals(ProjectConstant.PRINT_STATUS_PRINT)) {
						lotteryPrint.setStatus(3);
					} else {
						continue;
					}
					String sp = stake.getSp();
					lotteryPrint.setPlatformId(stake.getPlatformId());
					lotteryPrint.setPrintNo(stake.getPrintNo());
					lotteryPrint.setPrintSp(sp);
					lotteryPrint.setPrintStatus(printStatus);
					Date printTime = null;
					String printTimeStr = stake.getPrintTime();
					if(StringUtils.isNotBlank(printTimeStr)) {
						try {
							printTimeStr = printTimeStr.replaceAll("/", "-");
							printTime = sdf.parse(printTimeStr);
							lotteryPrint.setPrintTime(printTime);
						} catch (ParseException e) {
							log.error("订单编号：" + stake.getTicketId() + "，出票回调，时间转换异常", e);
							continue;
						}
					}
					lotteryPrints.add(lotteryPrint);
				}
			}
			log.info("goQueryStake orders size=" + orders.length + " -> updateLotteryPrintByCallBack size:" + lotteryPrints.size());
			if (CollectionUtils.isNotEmpty(lotteryPrints)) {
				for (DlPrintLottery print : lotteryPrints) {
					dlPrintLotteryMapper.updateLotteryPrintByCallBack(print);
				}
			}
		}
	}
	/**
	 * 投注结果查询
	 * @return
	 */
	public XianDlQueryStakeDTO queryStakeXian(DlQueryStakeParam param) {
		param.setTimestamp(DateUtil.getCurrentTimeString(DateUtil.getCurrentTimeLong().longValue(), DateUtil.datetimeFormat));
		JSONObject jo = JSONObject.fromObject(param);
		String backStr = getBackDateByJsonDataXian(jo, "/order/query");
		log.info("西安出票查询参数={},响应信息={}",jo,backStr);
		JSONObject backJo = JSONObject.fromObject(backStr);
		@SuppressWarnings("rawtypes")
		Map<String,Class> mapClass = new HashMap<String,Class>();
		mapClass.put("orders", XianBackQueryStake.class);
		XianDlQueryStakeDTO dlQueryStakeDTO = (XianDlQueryStakeDTO) JSONObject.toBean(backJo, XianDlQueryStakeDTO.class, mapClass); 
		if(dlQueryStakeDTO.getOrders()==null){
			log.error("西安出票查询失败，retCode={},retDesc={}",dlQueryStakeDTO.getRetCode(),dlQueryStakeDTO.getRetDesc());
		}
//		转化 一些特定参数例如利率，票号
		for(XianBackQueryStake stake : dlQueryStakeDTO.getOrders()){
			praseXianPrintSpToOurSp(stake);
		}
		return dlQueryStakeDTO;
	}
	/**
	 * 对赔率字段特殊处理
	 * @param xianStake
	 */
	private void praseXianPrintSpToOurSp(XianBackQueryStake xianStake) {
		log.info("西安赔率处理 ticketId={},sp={}",xianStake.getTicketId(),xianStake.getSp());
		String ourSp = parseStakeIssues(xianStake.getSp(),Boolean.TRUE);
		log.info("转化后的赔率格式是={}",ourSp);
		xianStake.setSp(ourSp);
	}
	/**
	 * 处理投注信息转化
	 * @param stakeIssues
	 * @param removePlayCode
	 * @return
	 */
	private static String parseStakeIssues(String stakeIssues,Boolean removePlayCode){
		StringBuffer ourStakeSpResult = new StringBuffer();
		if(StringUtils.isNotEmpty(stakeIssues)){
			TreeMap<String, String> ourStakes = new TreeMap<String, String>();
			stakeIssues = stakeIssues.replace(":", "");//替换掉冒号
			String[] xianSpList = stakeIssues.split(";");
			for(int i=0;i<xianSpList.length;i++){
				String one = xianSpList[i];
				String[] issueStakes = one.split("\\|");
				if(issueStakes.length==3){// 玩法|场次|赔率					
//					统一场次只存一个
					String playAndIssue = issueStakes[0]+"|"+issueStakes[1];
					if(ourStakes.containsKey(playAndIssue)){
						String result = ourStakes.get(playAndIssue);
						ourStakes.put(playAndIssue, result+","+issueStakes[2]);
					}else{
						ourStakes.put(playAndIssue, issueStakes[2]);
					}
				}else{
					log.info("西安返回的赔率格式异常sp={}",stakeIssues);
				}
			}
			log.info("prase keymap={}",ourStakes.toString());
			for(String key : ourStakes.keySet()){
				if(ourStakeSpResult.length()>0){//除第一条数据外，其他的都要加上分号
					ourStakeSpResult.append(";");
				}
				if(removePlayCode){
					String[] playCodeAndeIssueArr = key.split("\\|");
					ourStakeSpResult.append(playCodeAndeIssueArr[1]);
				}else{					
					ourStakeSpResult.append(key);
				}
				ourStakeSpResult.append("|");
				ourStakeSpResult.append(ourStakes.get(key));
			}
		}
		return ourStakeSpResult.length()==0?"":ourStakeSpResult.toString();
	}
	/**
	 * 定时任务去主动查询发票状态
	 */
	public void goQueryStakeHenan() {
		List<DlPrintLottery> prints = dlPrintLotteryMapper.getPrintIngLotterysHenan();
		log.info("彩票出票状态查询数据："+prints.size());
		while(prints.size() > 0) {
			log.info("彩票出票状态查询数据还有："+prints.size());
			int endIndex = prints.size()>20?20:prints.size();
			List<DlPrintLottery> subList = prints.subList(0, endIndex);
			List<String> collect = subList.stream().map(print-> print.getTicketId()).collect(Collectors.toList());
			String[] orders = collect.toArray(new String[collect.size()]);
			this.goQueryStakeHenan(orders);
			prints.removeAll(subList);
		}
	}
	private void goQueryStakeHenan(String[] orders) {
		DlQueryStakeParam queryStakeParam = new DlQueryStakeParam();
		queryStakeParam.setMerchant(merchant);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		queryStakeParam.setTimestamp(sdf.format(new Date()));
		queryStakeParam.setVersion("1.0");
		queryStakeParam.setOrders(orders);
		DlQueryStakeDTO dlQueryStakeDTO = this.queryStakeHenan(queryStakeParam);
		String retCode = dlQueryStakeDTO.getRetCode();
		if("0".equals(retCode)) {
			List<BackQueryStake> queryStakes = dlQueryStakeDTO.getOrders();
			log.info("查询返回结果数据：size="+queryStakes.size());
			List<DlPrintLottery> lotteryPrints = new ArrayList<>(queryStakes.size());
			for(BackQueryStake stake: queryStakes) {
				String ticketId = stake.getTicketId();
				DlPrintLottery lotteryPrint = dlPrintLotteryMapper.selectDlPrintLotteryByTicketId(ticketId);
				if(null != lotteryPrint) {
					Integer printStatus = stake.getPrintStatus();
					if(printStatus.equals(ProjectConstant.PRINT_STATUS_FAIL)) {
						lotteryPrint.setStatus(2);
					}else if(printStatus.equals(ProjectConstant.PRINT_STATUS_SUCCESS)) {
						lotteryPrint.setStatus(1);
					}else if(printStatus.equals(ProjectConstant.PRINT_STATUS_PRINT)) {
						lotteryPrint.setStatus(3);
					} else {
						continue;
					}
//					String stakes = lotteryPrint.getStakes();
					String sp = stake.getSp();
//					String comparePrintSp = getComparePrintSpHenan(sp, stake.getTicketId());
//					comparePrintSp = StringUtils.isBlank(comparePrintSp)?sp:comparePrintSp;

//					String game = lotteryPrint.getGame();
//					String printSp = null;
//					if("T51".equals(game) && StringUtils.isNotBlank(comparePrintSp)) {
//						printSp = this.getPrintSp(stakes, comparePrintSp);
//					} else if("T56".equals(game)) {
//						printSp = comparePrintSp;
//					}
					lotteryPrint.setPlatformId(stake.getPlatformId());
					lotteryPrint.setPrintNo(stake.getPrintNo());
					lotteryPrint.setPrintSp(sp);
					lotteryPrint.setPrintStatus(printStatus);
					Date printTime = null;
					String printTimeStr = stake.getPrintTime();
					if(StringUtils.isNotBlank(printTimeStr)) {
						try {
							printTimeStr = printTimeStr.replaceAll("/", "-");
							printTime = sdf.parse(printTimeStr);
							lotteryPrint.setPrintTime(printTime);
						} catch (ParseException e) {
							log.error("订单编号：" + stake.getTicketId() + "，出票回调，时间转换异常", e);
							continue;
						}
					}
					lotteryPrints.add(lotteryPrint);
				}
			}
			log.info("goQueryStake orders size=" + orders.length + " -> updateLotteryPrintByCallBack size:" + lotteryPrints.size());
			if (CollectionUtils.isNotEmpty(lotteryPrints)) {
				for (DlPrintLottery print : lotteryPrints) {
					dlPrintLotteryMapper.updateLotteryPrintByCallBack(print);
				}
			}
		}
	}
	/**
	 * 获取我们需要的带玩法的赔率,供订单修改详情赔率计算奖金使用
	 */
	private String getPrintSp(String stakes, String spStr) {
		String[] stakesList = stakes.split(";");
		Map<String, String> codeTypeMap = new HashMap<String, String>();
		for(int i=0; i<stakesList.length; i++) {
			String stake = stakesList[i];
			String playType = stake.substring(0, stake.indexOf("|"));
			String playCode = stake.substring(stake.indexOf("|") + 1, stake.lastIndexOf("|"));
			codeTypeMap.put(playCode, playType);
		}
		String[] spArr = spStr.split(";");
		StringBuffer sbuf = new StringBuffer();
		for(String sp: spArr) {
			String[] split = sp.split("\\|");
			String playCode = split[0];
			String playType = codeTypeMap.get(playCode);
			String nsp = playType+"|"+sp;
			sbuf.append(nsp).append(";");
		}
		String printSp = sbuf.substring(0, sbuf.length()-1);
		return printSp;
		
	}
	/**
	 * 比较回调和主动查询的赔率是否一致，如果不一致，以主动查询成功的结果为准
	 * @param callBackSp
	 * @param issue
	 * @return
	 */
	private String getComparePrintSpHenan(String callBackSp, String ticketId) {
		DlQueryStakeParam param = new DlQueryStakeParam();
		param.setMerchant(merchant);
		String[] orders = new String[1];
		orders[0] = ticketId;
		param.setOrders(orders);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		param.setTimestamp(sdf.format(new Date()));
		param.setVersion("1.0");
		DlQueryStakeDTO dlQueryStakeDTO = queryStakeHenan(param);
		if(!dlQueryStakeDTO.getRetCode().equals("0")) {
			return callBackSp;
		}
		List<BackQueryStake> backQueryStakes = dlQueryStakeDTO.getOrders();
		if(CollectionUtils.isEmpty(backQueryStakes)) {
			return callBackSp;
		}
		BackQueryStake backQueryStake = backQueryStakes.get(0);
		if(null == backQueryStake || backQueryStake.getPrintStatus() != 16) {
			return callBackSp;
		}
		if(StringUtils.isNotEmpty(callBackSp) && StringUtils.isNotEmpty(backQueryStake.getSp())) {
			if(callBackSp.equals(backQueryStake.getSp())) {
				return callBackSp;
			} else {
				return backQueryStake.getSp();
			}
		} else if(StringUtils.isNotEmpty(callBackSp)) {
			return callBackSp;
		}
		
		return backQueryStake.getSp();
	}
	/**
	 * 比较回调和主动查询的赔率是否一致，如果不一致，以主动查询成功的结果为准
	 * @param callBackSp
	 * @param issue
	 * @return
	 */
	private String getComparePrintSpXian(String callBackSp, String ticketId) {
		DlQueryStakeParam param = new DlQueryStakeParam();
		param.setMerchant(xianMerchant);
		String[] orders = new String[1];
		orders[0] = ticketId;
		param.setOrders(orders);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		param.setTimestamp(sdf.format(new Date()));
		param.setVersion("1.0");
		XianDlQueryStakeDTO dlQueryStakeDTO = queryStakeXian(param);
		if(!dlQueryStakeDTO.getRetCode().equals("0")) {
			return callBackSp;
		}
		List<XianBackQueryStake> backQueryStakes = dlQueryStakeDTO.getOrders();
		if(CollectionUtils.isEmpty(backQueryStakes)) {
			return callBackSp;
		}
		XianBackQueryStake backQueryStake = backQueryStakes.get(0);
		if(null == backQueryStake || backQueryStake.getPrintStatus() != 16) {
			return callBackSp;
		}
		if(StringUtils.isNotEmpty(callBackSp) && StringUtils.isNotEmpty(backQueryStake.getSp())) {
			if(callBackSp.equals(backQueryStake.getSp())) {
				return callBackSp;
			} else {
				return backQueryStake.getSp();
			}
		} else if(StringUtils.isNotEmpty(callBackSp)) {
			return callBackSp;
		}
		
		return backQueryStake.getSp();
	}
	/**
	 * 投注结果查询
	 * @return
	 */
	public DlQueryStakeDTO queryStakeHenan(DlQueryStakeParam param) {
		param.setTimestamp(DateUtil.getCurrentTimeString(DateUtil.getCurrentTimeLong().longValue(), DateUtil.datetimeFormat));
		JSONObject jo = JSONObject.fromObject(param);
		String backStr = getBackDateByJsonDataHenan(jo, "/stake_query");
		JSONObject backJo = JSONObject.fromObject(backStr);
		@SuppressWarnings("rawtypes")
		Map<String,Class> mapClass = new HashMap<String,Class>();
		mapClass.put("orders", BackQueryStake.class);
		DlQueryStakeDTO dlQueryStakeDTO = (DlQueryStakeDTO) JSONObject.toBean(backJo, DlQueryStakeDTO.class, mapClass); 
		return dlQueryStakeDTO;
	}
	/**
	 * 出票定时任务
	 */
	public void goPrintLottery() {
		doHenanPrintLotterys();
        doXianPrintLotterys();
	}
	/**
	 * 西安出票处理
	 */
	private void doXianPrintLotterys() {
//      西安
      List<DlPrintLottery> lotteryPrintXianList = dlPrintLotteryMapper.lotteryPrintsXianByUnPrint();
      log.info("西安 goPrintLottery 未出票数："+lotteryPrintXianList.size());
      if(CollectionUtils.isNotEmpty(lotteryPrintXianList)) {
      	log.info("西安 lotteryPrintList size="+lotteryPrintXianList.size());
      	while(lotteryPrintXianList.size() > 0) {
      		int toIndex = lotteryPrintXianList.size() > 50?50:lotteryPrintXianList.size();
      		List<DlPrintLottery> lotteryPrints = lotteryPrintXianList.subList(0, toIndex);
      		log.info("西安 go tostake size="+lotteryPrints.size());
      		Set<String> errOrderSns = this.gotoStakXian(lotteryPrints);
      		log.info("西安出票失败订单数："+errOrderSns.size());
      		lotteryPrintXianList.removeAll(lotteryPrints);
      	}
      }
	}
	/**
	 * 调用第三方出票
	 * @param successOrderSn
	 * @param lotteryPrintList
	 * @return 返回
	 */
	private Set<String> gotoStakXian(List<DlPrintLottery> lotteryPrints) {
		DlToStakeParam dlToStakeParam = new DlToStakeParam();
		dlToStakeParam.setMerchant(xianMerchant);
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
		XianDlToStakeDTO dlToStakeDTO = this.toStakeXian(dlToStakeParam);
		if(null != dlToStakeDTO && CollectionUtils.isNotEmpty(dlToStakeDTO.getOrders())) {
			log.info("inf tostake orders");
			List<DlPrintLottery> lotteryPrintErrors = new LinkedList<DlPrintLottery>();
			List<DlPrintLottery> lotteryPrintSuccess = new LinkedList<DlPrintLottery>();
			for(XianBackOrderDetail backOrderDetail : dlToStakeDTO.getOrders()) {
				DlPrintLottery lotteryPrint = new DlPrintLottery();
				lotteryPrint.setTicketId(backOrderDetail.getTicketId());
				Integer errorCode = backOrderDetail.getErrorCode();
				if(errorCode != 0) {
					if(3002 == errorCode) {
						lotteryPrint.setStatus(3);
						lotteryPrintSuccess.add(lotteryPrint);
						successOrderSn.add(ticketIdOrderSnMap.get(backOrderDetail.getTicketId()));
					}else {
						lotteryPrint.setErrorCode(errorCode);
						//出票失败
						lotteryPrint.setStatus(2);
						lotteryPrint.setPrintTime(new Date());
						lotteryPrintErrors.add(lotteryPrint);
					}
				} else {
					//出票中
					successOrderSn.add(ticketIdOrderSnMap.get(backOrderDetail.getTicketId()));
					lotteryPrint.setStatus(3);
					lotteryPrintSuccess.add(lotteryPrint);
				}
			}
			if(CollectionUtils.isNotEmpty(lotteryPrintErrors)) {
				log.info("lotteryPrintErrors size = "+lotteryPrintErrors.size());
				long start = System.currentTimeMillis();
				int num = 0;
				for(DlPrintLottery lotteryPrint:lotteryPrintErrors) {
					int rst = this.updatePrintStatusByTicketId(lotteryPrint);
					num+=rst<0?0:rst;
				}
				long end = System.currentTimeMillis();
				log.info("lotteryPrintErrors size = "+lotteryPrintErrors.size() +" rst size="+ num+ "  times=" + (end-start));
			}
			if(CollectionUtils.isNotEmpty(lotteryPrintSuccess)) {
				log.info("lotteryPrintSuccess size="+lotteryPrintSuccess.size());
				long start = System.currentTimeMillis();
				int num = 0;
				for(DlPrintLottery lotteryPrint:lotteryPrintSuccess) {
					int rst = this.updatePrintStatusByTicketId(lotteryPrint);
					num+=rst<0?0:rst;
				}
				long end = System.currentTimeMillis();
				log.info("lotteryPrintSuccess size="+lotteryPrintSuccess.size()+" rst size="+ num + "  times=" + (end-start));
			}
		}
		allOrderSns.removeAll(successOrderSn);
		return allOrderSns;
	}
	/**
	 * 投注接口（竞彩足球，game参数都是T51）
	 * @return
	 */
	public XianDlToStakeDTO toStakeXian(DlToStakeParam param) {
		param.setTimestamp(DateUtil.getCurrentTimeString(DateUtil.getCurrentTimeLong().longValue(), DateUtil.datetimeFormat));
		JSONObject jo = JSONObject.fromObject(param);
		String backStr = getBackDateByJsonDataXian(jo, "/order/create");
		JSONObject backJo = JSONObject.fromObject(backStr);
		@SuppressWarnings("rawtypes")
		Map<String,Class> mapClass = new HashMap<String,Class>();
		mapClass.put("orders", XianBackOrderDetail.class);
		XianDlToStakeDTO dlToStakeDTO = (XianDlToStakeDTO) JSONObject.toBean(backJo, XianDlToStakeDTO.class, mapClass); 
		return dlToStakeDTO;
	}
	/**
	 * 获取返回信息
	 * @param jo
	 * @return
	 */
	private String getBackDateByJsonDataXian(JSONObject jo, String inter) {
		String authStr = xianMerchant + xianMerchantPassword + jo.toString();
		ClientHttpRequestFactory clientFactory = restTemplateConfig.simpleClientHttpRequestFactory();
		RestTemplate rest = restTemplateConfig.restTemplate(clientFactory);
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json;charset=UTF-8");
		headers.setContentType(type);
		String authorization = MD5Utils.MD5(authStr);
		headers.add("Authorization", authorization);
		HttpEntity<JSONObject> requestEntity = new HttpEntity<JSONObject>(jo, headers);
		String requestUrl = printTicketXianUrl + inter;
		String response = rest.postForObject(requestUrl, requestEntity, String.class);
		String requestParam = JSONHelper.bean2json(requestEntity);
		LotteryThirdApiLog thirdApiLog = new LotteryThirdApiLog(requestUrl, ThirdApiEnum.HE_NAN_LOTTERY.getCode(), requestParam, response);
        dlPrintLotteryMapper.saveLotteryThirdApiLog(thirdApiLog);
		return response;
	}
//	FIXME 用于调试河南期次兑奖文件main方法
	public static void main(String[] args) {
//		String heNanUrl="http://capi.bjzhongteng.com";
//		String heNanMerchant="caixiaomi_pro";
//		String heNanPwd="zoo3ReabDeGo6Ao4";
		String heNanUrl="devcapi.bjzhongteng.com";
		String heNanMerchant="caixiaomi_dev";
		String heNanPwd="Udohdup9shoh0Pee";
		queryOrder(heNanUrl,heNanMerchant,heNanPwd);
//		queryPrizeFile(heNanUrl,heNanMerchant,heNanPwd);
	}
	private static void queryOrder(String heNanUrl, String heNanMerchant,
			String heNanPwd) {
//		String[] orders=new String[]{"2018070220336551340874","2018070220343831340875","2018070220344721290876","2018070220342271280877"};
//		2018070220336551340874
//		2018070220343831340875
//		2018070220344721290876
//		2018070220342271280877		
//		String[] orders=new String[]{"2018070220344071350880","2018070220343531290879","2018070220347661330878"};
//		2018070220344071350880
//		2018070220343531290879
//		2018070220347661330878
		String[] orders=new String[]{"2018070220348623260972","2018070220341183790973","2018070220343653130975"};
//		2018070220348623260972
//		2018070220341183790973
//		2018070220343653130975
//		2018070220345593790970
//		2018070220347713260976
//		2018070220346653250971
//		2018070220347413880974
		Map<String,Object> param = new HashMap<String, Object>();
		param.put("orders", orders);
		param.put("merchant", heNanMerchant);
		param.put("version", "1.0");
		param.put("timestamp", DateUtil.getCurrentTimeString(DateUtil.getCurrentTimeLong().longValue(), DateUtil.datetimeFormat));
		JSONObject jo = JSONObject.fromObject(param);
		String authStr = heNanMerchant + heNanPwd + jo.toString();
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json;charset=UTF-8");
		headers.setContentType(type);
		String authorization = MD5Utils.MD5(authStr);
		headers.add("Authorization", authorization);
		HttpEntity<JSONObject> requestEntity = new HttpEntity<JSONObject>(jo, headers);
		String requestUrl = heNanUrl + "/order/query";
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
		String response = restTemplate.postForObject(requestUrl, requestEntity, String.class);
		log.info(response);
	}

	private static void queryPrizeFile(String heNanUrl,String heNanMerchant,String heNanPwd){
//		String heNanUrl="http://1.192.90.178:9085";
//		String heNanMerchant="180326";
//		String heNanPwd="0FC67A15";
		Map<String,Object> param = new HashMap<String, Object>();
		param.put("game", "T51");
		param.put("issue", "201806306110");
		param.put("merchant", heNanMerchant);
		param.put("version", "1.0");
		param.put("timestamp", DateUtil.getCurrentTimeString(DateUtil.getCurrentTimeLong().longValue(), DateUtil.datetimeFormat));
		JSONObject jo = JSONObject.fromObject(param);
		String authStr = heNanMerchant + heNanPwd + jo.toString();
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json;charset=UTF-8");
		headers.setContentType(type);
		String authorization = MD5Utils.MD5(authStr);
		headers.add("Authorization", authorization);
		HttpEntity<JSONObject> requestEntity = new HttpEntity<JSONObject>(jo, headers);
		String requestUrl = heNanUrl + "/prize_file";
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
		String response = restTemplate.postForObject(requestUrl, requestEntity, String.class);
		log.info(response);
	}
	/**
	 * 河南出票处理
	 */
	private void doHenanPrintLotterys() {
		List<DlPrintLottery> lotteryPrintList = dlPrintLotteryMapper.lotteryPrintsHenanByUnPrint();
        log.info("goPrintLottery 未出票数："+lotteryPrintList.size());
        if(CollectionUtils.isNotEmpty(lotteryPrintList)) {
        	log.info("lotteryPrintList size="+lotteryPrintList.size());
        	while(lotteryPrintList.size() > 0) {
        		int toIndex = lotteryPrintList.size() > 50?50:lotteryPrintList.size();
        		List<DlPrintLottery> lotteryPrints = lotteryPrintList.subList(0, toIndex);
        		log.info(" go tostake size="+lotteryPrints.size());
        		Set<String> errOrderSns = this.gotoStak(lotteryPrints);
        		log.info("出票失败订单数："+errOrderSns.size());
        		lotteryPrintList.removeAll(lotteryPrints);
        	}
        }
	}
	/**
	 * 调用第三方出票
	 * @param successOrderSn
	 * @param lotteryPrintList
	 * @return 返回
	 */
	private Set<String> gotoStak(List<DlPrintLottery> lotteryPrints) {
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
		DlToStakeDTO dlToStakeDTO = this.toStakeHenan(dlToStakeParam);
		if(null != dlToStakeDTO && CollectionUtils.isNotEmpty(dlToStakeDTO.getOrders())) {
			log.info("inf tostake orders");
			List<DlPrintLottery> lotteryPrintErrors = new LinkedList<DlPrintLottery>();
			List<DlPrintLottery> lotteryPrintSuccess = new LinkedList<DlPrintLottery>();
			for(BackOrderDetail backOrderDetail : dlToStakeDTO.getOrders()) {
				DlPrintLottery lotteryPrint = new DlPrintLottery();
				lotteryPrint.setTicketId(backOrderDetail.getTicketId());
				Integer errorCode = backOrderDetail.getErrorCode();
				if(errorCode != 0) {
					if(3002 == errorCode) {
						successOrderSn.add(ticketIdOrderSnMap.get(backOrderDetail.getTicketId()));
					}else {
						lotteryPrint.setErrorCode(errorCode);
						//出票失败
						lotteryPrint.setStatus(2);
						lotteryPrint.setPrintTime(new Date());
						lotteryPrintErrors.add(lotteryPrint);
					}
				} else {
					//出票中
					successOrderSn.add(ticketIdOrderSnMap.get(backOrderDetail.getTicketId()));
					lotteryPrint.setStatus(3);
					lotteryPrintSuccess.add(lotteryPrint);
				}
			}
			if(CollectionUtils.isNotEmpty(lotteryPrintErrors)) {
				log.info("lotteryPrintErrors size = "+lotteryPrintErrors.size());
				long start = System.currentTimeMillis();
				int num = 0;
				for(DlPrintLottery lotteryPrint:lotteryPrintErrors) {
					int rst = this.updatePrintStatusByTicketId(lotteryPrint);
					num+=rst<0?0:rst;
				}
				long end = System.currentTimeMillis();
				log.info("lotteryPrintErrors size = "+lotteryPrintErrors.size() +" rst size="+ num+ "  times=" + (end-start));
			}
			if(CollectionUtils.isNotEmpty(lotteryPrintSuccess)) {
				log.info("lotteryPrintSuccess size="+lotteryPrintSuccess.size());
				long start = System.currentTimeMillis();
				int num = 0;
				for(DlPrintLottery lotteryPrint:lotteryPrintSuccess) {
					int rst = this.updatePrintStatusByTicketId(lotteryPrint);
					num+=rst<0?0:rst;
				}
				long end = System.currentTimeMillis();
				log.info("lotteryPrintSuccess size="+lotteryPrintSuccess.size()+" rst size="+ num + "  times=" + (end-start));
			}
		}
		allOrderSns.removeAll(successOrderSn);
		return allOrderSns;
	}
	public int updatePrintStatusByTicketId(DlPrintLottery lotteryPrint) {
		return dlPrintLotteryMapper.updatePrintStatusByTicketId(lotteryPrint);
	}
	/**
	 * 投注接口（竞彩足球，game参数都是T51）
	 * @return
	 */
	public DlToStakeDTO toStakeHenan(DlToStakeParam param) {
		param.setTimestamp(DateUtil.getCurrentTimeString(DateUtil.getCurrentTimeLong().longValue(), DateUtil.datetimeFormat));
		JSONObject jo = JSONObject.fromObject(param);
		String backStr = getBackDateByJsonDataHenan(jo, "/stake");
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
	private String getBackDateByJsonDataHenan(JSONObject jo, String inter) {
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
		log.info("河南出票调用requestUrl={},响应={}",requestUrl,response);
		String requestParam = JSONHelper.bean2json(requestEntity);
		LotteryThirdApiLog thirdApiLog = new LotteryThirdApiLog(requestUrl, ThirdApiEnum.HE_NAN_LOTTERY.getCode(), requestParam, response);
		dlPrintLotteryMapper.saveLotteryThirdApiLog(thirdApiLog);
		return response;
	}

	// ※※※※※※※※※※※※※※※※※
	// ※※※※※※※※※※※※※※※※※
	// 定时任务：更新彩票信息
	public void updatePrintLotteryCompareStatus() {
		List<DlPrintLottery> lotteryPrints = dlPrintLotteryMapper.lotteryPrintsByUnCompare();
		if (lotteryPrints == null) {
			log.info("updatePrintLotteryCompareStatus 没有获取到需要更新状态的彩票数据");
			return;
		}
		log.info("updatePrintLotteryCompareStatus 获取到需要更新状态的彩票数据，size=" + lotteryPrints.size());
		// 获取没有赛事结果比较的playcodes
		Set<String> unPlayCodes = new HashSet<String>();
		List<DlPrintLottery> endPrints = new ArrayList<DlPrintLottery>(lotteryPrints.size());
		for (DlPrintLottery print : lotteryPrints) {
			List<String> playCodes = this.printStakePlayCodes(print);
			String comparedStakes = print.getComparedStakes();
			List<String> comparedPlayCodes = null;
			if (StringUtils.isNotEmpty(comparedStakes)) {
				comparedPlayCodes = Arrays.asList(comparedStakes.split(","));
			}
			if (comparedPlayCodes != null) {
				playCodes.removeAll(comparedPlayCodes);
			}
			if (playCodes.size() == 0) {
				print.setCompareStatus(ProjectConstant.FINISH_COMPARE);
				endPrints.add(print);
			} else {
				unPlayCodes.addAll(playCodes);
			}
		}
		log.info("updatePrintLotteryCompareStatus 未更新状态彩票对应其次数，size=" + unPlayCodes.size());
		if (CollectionUtils.isEmpty(unPlayCodes)) {
			return;
		}
		// 获取赛事结果
		List<String> playCodes = new ArrayList<String>(unPlayCodes.size());
		playCodes.addAll(unPlayCodes);
		List<String> canCelPlayCodes = lotteryMatchMapper.getCancelMatches(playCodes);
		List<DlLeagueMatchResult> matchResults = dlLeagueMatchResultMapper.queryMatchResultsByPlayCodes(playCodes);
		if (CollectionUtils.isEmpty(matchResults) && Collections.isEmpty(canCelPlayCodes)) {
			log.info("updatePrintLotteryCompareStatus 准备获取赛事结果的场次数：" + playCodes.size() + " 没有获取到相应的赛事结果信息也没有取消的赛事");
			return;
		}
		log.info("updatePrintLotteryCompareStatus 准备获取赛事结果的场次数：" + playCodes.size() + " 获取到相应的赛事结果信息数：" + matchResults.size() + "  已取消赛事" + canCelPlayCodes.size());

		Map<String, List<DlLeagueMatchResult>> resultMap = new HashMap<String, List<DlLeagueMatchResult>>();
		if (!CollectionUtils.isEmpty(matchResults)) {
			for (DlLeagueMatchResult dto : matchResults) {
				String playCode = dto.getPlayCode();
				List<DlLeagueMatchResult> list = resultMap.get(playCode);
				if (list == null) {
					list = new ArrayList<DlLeagueMatchResult>(5);
					resultMap.put(playCode, list);
				}
				list.add(dto);
			}
		}
		//
		List<DlPrintLottery> updates = new ArrayList<DlPrintLottery>(lotteryPrints.size());
		for (String playCode : playCodes) {
			boolean isCancel = false;
			if (canCelPlayCodes.contains(playCode)) {
				isCancel = true;
			}
			List<DlLeagueMatchResult> matchResultList = resultMap.get(playCode);
			if (!isCancel && CollectionUtils.isEmpty(matchResultList)) {
				continue;
			}
			for (DlPrintLottery print : lotteryPrints) {
				Boolean playTypehaveResult = Boolean.FALSE;
				String stakes = print.getStakes();
				String comparedStakes = print.getComparedStakes() == null ? "" : print.getComparedStakes();
				// 判断是否对比过
				if (stakes.contains(playCode) && !comparedStakes.contains(playCode)) {
					if (comparedStakes.length() > 0) {
						comparedStakes += ",";
					}
					comparedStakes += playCode;
					DlPrintLottery updatePrint = new DlPrintLottery();
					updatePrint.setPrintLotteryId(print.getPrintLotteryId());
					updatePrint.setComparedStakes(comparedStakes);
					String[] stakesarr = stakes.split(";");
					StringBuffer sbuf = new StringBuffer();
					Set<String> stakePlayCodes = new HashSet<String>(stakesarr.length);
					// 彩票的每一场次分析
					for (String stake : stakesarr) {
						String[] split = stake.split("\\|");
						stakePlayCodes.add(split[1]);
						if (stake.contains(playCode)) {
							String playTypeStr = split[0];
							List<String> cellCodes = Arrays.asList(split[2].split(","));
							if (isCancel) {
								playTypehaveResult = Boolean.TRUE;
								sbuf.append(";").append(playTypeStr).append("|").append(playCode).append("|");
								for (int i = 0; i < cellCodes.size(); i++) {
									if (i > 0) {
										sbuf.append(",");
									}
									String cellCode = cellCodes.get(i);
									sbuf.append(cellCode).append("@").append("1.00");
								}
							} else {
								// 比赛结果获取中奖信息
								for (DlLeagueMatchResult rst : matchResultList) {
									if (rst.getPlayType().equals(Integer.valueOf(playTypeStr))) {
										playTypehaveResult = Boolean.TRUE;
										String cellCode = rst.getCellCode();
										if (cellCodes.contains(cellCode)) {
											Map<String, String> aa = this.aa(print.getPrintSp());
											String key = rst.getPlayCode() + "|" + rst.getCellCode();
											String odds = aa.get(key);
											if (StringUtils.isNotBlank(odds)) {
												sbuf.append(";").append("0").append(rst.getPlayType()).append("|").append(key).append("@").append(odds);
												break;
											}
										}
									}
								}
							}
						}
					}
					if(!playTypehaveResult){//没有赛果时，跳出该处理等待下次循环
						continue;
					}
					// 中奖记录
					String reward = print.getRewardStakes();
					if (sbuf.length() > 0) {
						reward = StringUtils.isBlank(reward) ? sbuf.substring(1, sbuf.length()) : (reward + sbuf.toString());
					}
					updatePrint.setRewardStakes(reward);
					updatePrint.setRealRewardMoney(print.getRealRewardMoney());
					updatePrint.setCompareStatus(print.getCompareStatus());

					// 彩票对票结束
					if (stakePlayCodes.size() == comparedStakes.split(",").length) {
						updatePrint.setCompareStatus(ProjectConstant.FINISH_COMPARE);
						if (StringUtils.isNotBlank(reward)) {
							// 彩票中奖金额
							// log.info(reward);
							List<String> spList = Arrays.asList(reward.split(";"));
							List<List<Double>> winSPList = spList.stream().map(s -> {
								String cells = s.split("\\|")[2];
								String[] split = cells.split(",");
								List<Double> list = new ArrayList<Double>(split.length);
								for (String str : split) {
									list.add(Double.valueOf(str.substring(str.indexOf("@") + 1)));
								}
								return list;
							}).collect(Collectors.toList());
							List<Double> rewardList = new ArrayList<Double>();
							// 2018-06-04计算税
							this.groupByRewardList(2.0, Integer.valueOf(print.getBetType()) / 10, winSPList, rewardList);
							double rewardSum = rewardList.stream().reduce(0.00, Double::sum) * print.getTimes();
							updatePrint.setRealRewardMoney(BigDecimal.valueOf(rewardSum));
							// 保存第三方给计算的单张彩票的价格
							/*
							 * PeriodRewardDetail periodRewardDetail = new
							 * PeriodRewardDetail();
							 * periodRewardDetail.setTicketId
							 * (print.getTicketId()); List<PeriodRewardDetail>
							 * tickets = periodRewardDetailMapper.
							 * queryPeriodRewardDetailBySelective
							 * (periodRewardDetail); if
							 * (!CollectionUtils.isEmpty(tickets)) { BigDecimal
							 * thirdPartRewardMoney =
							 * BigDecimal.valueOf(tickets.get(0).getReward());
							 * updatePrint
							 * .setThirdPartRewardMoney(thirdPartRewardMoney); }
							 */
						}
					}
					// 添加
					updates.add(updatePrint);
				}// 判断是否对比过over
			}// over prints for
		}// over playcode for
		log.info("updateBatchLotteryPrint 准备更新彩票信息到数据库：size" + updates.size());
		if(updates.size() > 0) {
			int num = 0;
			for (DlPrintLottery print : updates) {
				if (null == print.getRealRewardMoney()) {
					print.setRealRewardMoney(BigDecimal.ZERO);
				}
				int n = dlPrintLotteryMapper.updatePrintLotteryCompareInfo(print);
				if (n > 0) {
					num += n;
				}
			}
			log.info("updateBatchLotteryPrint 更新彩票信息到数据库：size" + updates.size() + "  入库返回：size=" + num);
		}
	}

	/**
	 * 获取playcode
	 * 
	 * @param print
	 * @return
	 */
	private List<String> printStakePlayCodes(DlPrintLottery print) {
		String stakes = print.getStakes();
		String[] split = stakes.split(";");
		List<String> playCodes = new ArrayList<String>(split.length);
		for (String str : split) {
			String[] split2 = str.split("\\|");
			String playCode = split2[1];
			playCodes.add(playCode);
		}
		return playCodes;
	}


	/**
	 * 组合中奖集合
	 * 
	 * @param amount
	 *            :初始值2*times
	 * @param num
	 *            :几串几
	 * @param list
	 *            :赔率
	 * @param rewardList
	 *            :组合后的中奖金额list
	 */
	private void groupByRewardList(Double amount, int num, List<List<Double>> list, List<Double> rewardList) {
		LinkedList<List<Double>> link = new LinkedList<List<Double>>(list);
		while (link.size() > 0) {
			List<Double> removes = link.remove(0);
			for (Double remove : removes) {
				Double item = amount * remove;
				if (num == 1) {
					// start对大于等于10000的单注奖金进行20%税收，：单注彩票奖金大于或者等于1万元时，扣除20%的偶然所得税后再派奖
					if (item.doubleValue() >= 10000) {
						item = item * 0.8;
					}
					// end
					rewardList.add(item);
				} else {
					groupByRewardList(item, num - 1, link, rewardList);
				}
			}
		}
	}

	private Map<String, String> aa(String printSp) {
		List<String> spList = Arrays.asList(printSp.split(";"));
		Map<String, String> spMap = new HashMap<String, String>();
		for (String temp : spList) {
			if (temp.contains(",")) {
				String playCode = temp.substring(0, temp.lastIndexOf("|"));
				String temp2 = temp.substring(temp.lastIndexOf("|") + 1);
				String[] tempArr = temp2.split(",");
				for (int j = 0; j < tempArr.length; j++) {
					String temp3 = playCode + "|" + tempArr[j];
					spMap.put(temp3.substring(0, temp3.indexOf("@")), temp3.substring(temp3.indexOf("@") + 1));
				}
			} else {
				spMap.put(temp.substring(0, temp.indexOf("@")), temp.substring(temp.indexOf("@") + 1));
			}
		}
		return spMap;
	}
	/**
	 * 保存预出票信息
	 * @param list
	 * @return
	 */
	@Transactional(value="transactionManager1")
	public BaseResult<String> saveLotteryPrintInfo(List<LotteryPrintDTO> list, String orderSn,int printLotteryCom) {
		List<DlPrintLottery> printLotterysByOrderSn = dlPrintLotteryMapper.printLotterysByOrderSn(orderSn);
		if(CollectionUtils.isNotEmpty(printLotterysByOrderSn)) {
			return ResultGenerator.genSuccessResult("已创建");
		}
		List<DlPrintLottery> models = list.stream().map(dto->{
			DlPrintLottery lotteryPrint = new DlPrintLottery();
			lotteryPrint.setGame("T51");
			lotteryPrint.setMerchant(printLotteryCom==1?merchant:xianMerchant);
			lotteryPrint.setTicketId(dto.getTicketId());
			lotteryPrint.setAcceptTime(DateUtil.getCurrentTimeLong());
			lotteryPrint.setBetType(dto.getBetType());
			lotteryPrint.setMoney(BigDecimal.valueOf(dto.getMoney()*100));
			lotteryPrint.setIssue(dto.getIssue());
			lotteryPrint.setPlayType(dto.getPlayType());
			lotteryPrint.setTimes(dto.getTimes());
			lotteryPrint.setStakes(dto.getStakes());
			lotteryPrint.setOrderSn(orderSn);
			lotteryPrint.setRealRewardMoney(BigDecimal.valueOf(0.00));
			lotteryPrint.setThirdPartRewardMoney(BigDecimal.valueOf(0.00));
			lotteryPrint.setCompareStatus("0");
			lotteryPrint.setComparedStakes("");
			lotteryPrint.setRewardStakes("");
			lotteryPrint.setStatus(0);
			lotteryPrint.setPrintLotteryCom(printLotteryCom);
			return lotteryPrint;
		}).collect(Collectors.toList());
		dlPrintLotteryMapper.batchInsertDlPrintLottery(models);
		return ResultGenerator.genSuccessResult();
	}
	@Transactional(value="transactionManager1")
	public BaseResult<String> saveLotteryPrintInfo(OrderInfoAndDetailDTO data, String orderSn) {
		List<OrderDetailDataDTO> orderDetailDataDTOs = data.getOrderDetailDataDTOs();
		OrderInfoDTO orderInfoDTO = data.getOrderInfoDTO();
		String betType = orderInfoDTO.getPassType();
		String playType = orderInfoDTO.getPlayType();
		Integer times = orderInfoDTO.getCathectic();
		OrderDetailDataDTO orderDetailDataDTO = orderDetailDataDTOs.get(0);
		Double money = orderDetailDataDTOs.size()*2.0*times;
		String stakes = orderDetailDataDTOs.stream().map(item->item.getTicketData().split("@")[0]).collect(Collectors.joining(","));
		String game = orderDetailDataDTO.getChangci();
		String issue = orderDetailDataDTO.getIssue();
		//orderDetailDataDTO.get
		String ticketId = SNGenerator.nextSN(SNBusinessCodeEnum.TICKET_SN.getCode());
		DlPrintLottery lotteryPrint = new DlPrintLottery();
		lotteryPrint.setGame(game);
		lotteryPrint.setMerchant(merchant);
		lotteryPrint.setTicketId(ticketId);
		lotteryPrint.setAcceptTime(DateUtil.getCurrentTimeLong());
		lotteryPrint.setBetType(betType);
		lotteryPrint.setMoney(BigDecimal.valueOf(money*100));
		lotteryPrint.setIssue(issue);
		lotteryPrint.setPlayType(playType);
		lotteryPrint.setTimes(times);
		lotteryPrint.setStakes(stakes);
		lotteryPrint.setOrderSn(orderSn);
		lotteryPrint.setRealRewardMoney(BigDecimal.valueOf(0.00));
		lotteryPrint.setThirdPartRewardMoney(BigDecimal.valueOf(0.00));
		lotteryPrint.setCompareStatus("0");
		lotteryPrint.setComparedStakes("");
		lotteryPrint.setRewardStakes("");
		lotteryPrint.setStatus(0);
		dlPrintLotteryMapper.insertDlPrintLottery(lotteryPrint);
		return ResultGenerator.genSuccessResult();
	}

	public List<LotteryPrintDTO> getPrintLotteryListByOrderInfo(OrderInfoAndDetailDTO  orderInfo, String orderSn) {
		OrderInfoDTO order = orderInfo.getOrderInfoDTO();
		List<OrderDetailDataDTO> selectByOrderId = orderInfo.getOrderDetailDataDTOs();
		List<MatchBetPlayDTO> matchBetPlays = selectByOrderId.stream().map(detail->{
			String ticketData = detail.getTicketData();
			String[] tickets = ticketData.split(";");
			String playCode = null;
			List<MatchBetCellDTO> matchBetCells = new ArrayList<MatchBetCellDTO>(tickets.length);
			for(String tikcket: tickets) {
				String[] split = tikcket.split("\\|");
				if(split.length != 3) {
					log.error("getBetInfoByOrderInfo ticket has error, orderSn="+orderSn+ " ticket="+tikcket);
					continue;
				}
				String playType = split[0];
				if(null == playCode) {
					playCode = split[1];
				}
				String[] split2 = split[2].split(",");
				List<DlJcZqMatchCellDTO> betCells = Arrays.asList(split2).stream().map(str->{
					String[] split3 = str.split("@");
					String matchResult = getCathecticData(split[0], split3[0]);
					DlJcZqMatchCellDTO dto = new DlJcZqMatchCellDTO(split3[0], matchResult, split3[1]);
					return dto;
				}).collect(Collectors.toList());
				MatchBetCellDTO matchBetCell = new MatchBetCellDTO();
				matchBetCell.setPlayType(playType);
				matchBetCell.setBetCells(betCells);
				matchBetCells.add(matchBetCell);
			}
			MatchBetPlayDTO dto = new MatchBetPlayDTO();
			dto.setChangci(detail.getChangci());
			dto.setIsDan(detail.getIsDan());
			dto.setLotteryClassifyId(detail.getLotteryClassifyId());
			dto.setLotteryPlayClassifyId(detail.getLotteryPlayClassifyId());
			dto.setMatchId(detail.getMatchId());
			dto.setMatchTeam(detail.getMatchTeam());
			Date matchTime = detail.getMatchTime();
			dto.setMatchTime((int)matchTime.toInstant().getEpochSecond());
			dto.setPlayCode(playCode);
			dto.setMatchBetCells(matchBetCells);
			return dto;
		}).collect(Collectors.toList());
		Integer times = order.getCathectic();
		String betType = order.getPassType();
		Integer lotteryClassifyId = order.getLotteryClassifyId();
		Integer lotteryPlayClassifyId = order.getLotteryPlayClassifyId();
		DlJcZqMatchBetParam param = new DlJcZqMatchBetParam();
		param.setBetType(betType);
		param.setLotteryClassifyId(lotteryClassifyId);
		param.setLotteryPlayClassifyId(lotteryPlayClassifyId);
		param.setPlayType(order.getPlayType());
		param.setTimes(times);
		param.setMatchBetPlays(matchBetPlays);
		List<LotteryPrintDTO> printLotteryList = this.getPrintLotteryList(param);
		return printLotteryList;
	}
	/**
	 * 获取预出票信息
	 * @param param
	 * @return
	 */
	public List<LotteryPrintDTO> getPrintLotteryList(DlJcZqMatchBetParam param) {
		long start = System.currentTimeMillis();
		List<MatchBetPlayDTO> matchBellCellList = param.getMatchBetPlays();
		String betTypes = param.getBetType();
		Map<String, List<String>> indexMap = this.getBetIndexList(matchBellCellList, betTypes);
		long end1 = System.currentTimeMillis();
		log.info("1计算预出票投注排列用时：" + (end1-start)+ " - "+start);
		Map<String, List<MatchBetPlayCellDTO>> playCellMap = this.getMatchBetPlayMap(matchBellCellList);
		long end2 = System.currentTimeMillis();
		log.info("2计算预出票获取不同投注的赛事信息用时：" + (end2-end1)+ " - "+start);
		//计算核心
		List<LotteryPrintDTO> lotteryPrints = new ArrayList<LotteryPrintDTO>();
		double srcMoney = 2.0*param.getTimes();
		BetResultInfo betResult = new BetResultInfo();
		for(String betType: indexMap.keySet()) {
			char[] charArray = betType.toCharArray();
			int num = Integer.valueOf(String.valueOf(charArray[0]));
			List<String> betIndexList = indexMap.get(betType);
			List<List<MatchBetPlayCellDTO>> result = new ArrayList<List<MatchBetPlayCellDTO>>(betIndexList.size());
			for(String str: betIndexList) {
				String[] strArr = str.split(",");
				List<String> playCodes = new ArrayList<String>(strArr.length);
				for(String item: strArr) {
					MatchBetPlayDTO betPlayDto = matchBellCellList.get(Integer.valueOf(item));
					String playCode = betPlayDto.getPlayCode();
					playCodes.add(playCode);
				}
				List<MatchBetPlayCellDTO> dtos = new ArrayList<MatchBetPlayCellDTO>(0);
				this.matchBetPlayCellsForLottery(playCodes.size(), playCellMap, playCodes, dtos, result);
			}
			//出票信息
			for(List<MatchBetPlayCellDTO> subList: result) {
				Integer oldBetNum = betResult.getBetNum();
				this.betNumtemp(srcMoney, num, subList, subList.size(), betResult);
				String stakes = subList.stream().map(cdto->{
					String playCode = cdto.getPlayCode();
					String playType1 = cdto.getPlayType();
					String cellCodes = cdto.getBetCells().stream().map(cell->{
						return cell.getCellCode();
					}).collect(Collectors.joining(","));
					return playType1 + "|" + playCode + "|" + cellCodes;
				}).collect(Collectors.joining(";"));
				Set<Integer> collect = subList.stream().map(cdto->Integer.parseInt(cdto.getPlayType())).collect(Collectors.toSet());
				String playType = param.getPlayType();
				if(Integer.parseInt(playType) == 6 && collect.size() == 1) {
					playType = "0"+collect.toArray(new Integer[1])[0].toString();
				}
				String issue = subList.get(0).getPlayCode();
				if(subList.size() > 1) {
					issue = subList.stream().max((item1,item2)->item1.getPlayCode().compareTo(item2.getPlayCode())).get().getPlayCode();
				}
				int betNum = betResult.getBetNum() - oldBetNum;
				int maxTime = 10000/betNum > 99? 99:10000/betNum;
				int times = param.getTimes();
				int n = times/maxTime;
				int m = times%maxTime;
				if(n > 0) {
					for(int i=0; i< n; i++) {
						Double money = betNum*maxTime*2.0;
						LotteryPrintDTO lotteryPrintDTO = new LotteryPrintDTO();
						lotteryPrintDTO.setBetType(betType);
						lotteryPrintDTO.setIssue(issue);
						lotteryPrintDTO.setMoney(money);
						lotteryPrintDTO.setPlayType(playType);
						lotteryPrintDTO.setStakes(stakes);
						String ticketId = SNGenerator.nextSN(SNBusinessCodeEnum.TICKET_SN.getCode());
						lotteryPrintDTO.setTicketId(ticketId);
						lotteryPrintDTO.setTimes(maxTime);
						lotteryPrints.add(lotteryPrintDTO);
					}
				}
				if(m > 0) {
					Double money = betNum*m*2.0;
//					String playType = param.getPlayType();
					LotteryPrintDTO lotteryPrintDTO = new LotteryPrintDTO();
					lotteryPrintDTO.setBetType(betType);
					lotteryPrintDTO.setIssue(issue);
					lotteryPrintDTO.setMoney(money);
					lotteryPrintDTO.setPlayType(playType);
					lotteryPrintDTO.setStakes(stakes);
					String ticketId = SNGenerator.nextSN(SNBusinessCodeEnum.TICKET_SN.getCode());
					lotteryPrintDTO.setTicketId(ticketId);
					lotteryPrintDTO.setTimes(m);
					lotteryPrints.add(lotteryPrintDTO);
				}
			}
		}
		long end3 = System.currentTimeMillis();
		log.info("3计算预出票基础信息用时：" + (end3-end2)+ " - "+start);
		log.info("5计算预出票信息用时：" + (end3-start)+ " - "+start);
		return lotteryPrints;
	}
	
	private Map<String, List<MatchBetPlayCellDTO>> getMatchBetPlayMap(List<MatchBetPlayDTO> matchBellCellList) {
		//整理投注对象
//		TMatchBetInfoWithMinOddsList tbml = new TMatchBetInfoWithMinOddsList();
//		List<Double> minList = new ArrayList<Double>(matchBellCellList.size());
		Map<String, List<MatchBetPlayCellDTO>> playCellMap = new HashMap<String, List<MatchBetPlayCellDTO>>(matchBellCellList.size());
//		Map<String, Double> minCellOddsMap = new HashMap<String, Double>(matchBellCellList.size());
//		Map<String, Integer> cellNumsMap = new HashMap<String, Integer>(matchBellCellList.size());
		matchBellCellList.forEach(betPlayDto->{
			String playCode = betPlayDto.getPlayCode();
			List<MatchBetCellDTO> matchBetCells = betPlayDto.getMatchBetCells();
			List<MatchBetPlayCellDTO> list = playCellMap.get(playCode);
//			Double minCellOdds = minCellOddsMap.get(playCode);
//			Integer cellNums = cellNumsMap.get(playCode);
			if(list == null) {
				list = new ArrayList<MatchBetPlayCellDTO>(matchBetCells.size());
				playCellMap.put(playCode, list);
//				minCellOdds = Double.MAX_VALUE;
//				cellNums = 0;
			}
			
			for(MatchBetCellDTO cell: matchBetCells) {
				MatchBetPlayCellDTO playCellDto = new MatchBetPlayCellDTO(betPlayDto);
				playCellDto.setPlayType(cell.getPlayType());
				List<DlJcZqMatchCellDTO> betCells = cell.getBetCells();
				playCellDto.setBetCells(betCells);
//				logger.info("=====cell.getFixedOdds()============  " + cell.getFixedOdds());
				playCellDto.setFixedodds(cell.getFixedOdds());
				list.add(playCellDto);
				/*if(betCells.size() == 1) {
					String cellOdds = betCells.get(0).getCellOdds();
					minCellOdds = Double.min(minCellOdds, Double.valueOf(cellOdds));
				}else {
					String cellOdds = betCells.stream().min((item1,item2)->Double.valueOf(item1.getCellOdds()).compareTo(Double.valueOf(item2.getCellOdds()))).get().getCellOdds();
					minCellOdds = Double.min(minCellOdds, Double.valueOf(cellOdds));
				}*/
			}
//			cellNums += matchBetCells.size();
//			minCellOddsMap.put(playCode, minCellOdds);
//			cellNumsMap.put(playCode, cellNums);
		});
		/*minList.addAll(minCellOddsMap.values());
		tbml.setMinOddsList(minList);
		tbml.setPlayCellMap(playCellMap);*/
//		tbml.setCellNumsMap(cellNumsMap);
		return playCellMap;
	}
	/**
	 * 计算投注组合
	 * @param matchBellCellList
	 * @param betTypes
	 * @return
	 */
	private Map<String, List<String>> getBetIndexList(List<MatchBetPlayDTO> matchBellCellList, String betTypes) {
		//读取设胆的索引
		List<String> indexList = new ArrayList<String>(matchBellCellList.size());
		List<String> danIndexList = new ArrayList<String>(3);
		for(int i=0; i< matchBellCellList.size(); i++) {
			indexList.add(i+"");
			int isDan = matchBellCellList.get(i).getIsDan();
			if(isDan != 0) {
				danIndexList.add(i+"");
			}
		}
		String[] split = betTypes.split(",");
		Map<String, List<String>> indexMap = new HashMap<String, List<String>>();
		for(String betType: split) {
			char[] charArray = betType.toCharArray();
			if(charArray.length == 2 && charArray[1] == '1') {
				int num = Integer.valueOf(String.valueOf(charArray[0]));
				//计算场次组合
				List<String> betIndexList = new ArrayList<String>();
				betNum1("", num, indexList, betIndexList);
				if(danIndexList.size() > 0) {
					betIndexList = betIndexList.stream().filter(item->{
						for(String danIndex: danIndexList) {
							if(!item.contains(danIndex)) {
								return false;
							}
						}
						return true;
					}).collect(Collectors.toList());
				}
				indexMap.put(betType, betIndexList);
			}
		}
		return indexMap;
	}
	/**
     * 通过玩法code与投注内容，进行转换
     * @param playCode
     * @param cathecticStr
     * @return
     */
    private String getCathecticData(String playType, String cathecticStr) {
    	int playCode = Integer.parseInt(playType);
    	String cathecticData = "";
    	if(MatchPlayTypeEnum.PLAY_TYPE_HHAD.getcode() == playCode
    		|| MatchPlayTypeEnum.PLAY_TYPE_HAD.getcode() == playCode) {
    		cathecticData = MatchResultHadEnum.getName(Integer.valueOf(cathecticStr));
    	} else if(MatchPlayTypeEnum.PLAY_TYPE_CRS.getcode() == playCode) {
    		cathecticData = MatchResultCrsEnum.getName(cathecticStr);
    	} else if(MatchPlayTypeEnum.PLAY_TYPE_TTG.getcode() == playCode) {
    		cathecticData = cathecticStr;
    	} else if(MatchPlayTypeEnum.PLAY_TYPE_HAFU.getcode() == playCode) {
    		cathecticData = MatchResultHafuEnum.getName(cathecticStr);
    	}
    	return cathecticData;
    }
    /**
	 * 
	 * @param playCellMap
	 * @param list
	 * @param dtos
	 * @param result
	 */
	private void matchBetPlayCellsForLottery(int num, Map<String, List<MatchBetPlayCellDTO>> playCellMap, List<String> list, List<MatchBetPlayCellDTO> dtos, List<List<MatchBetPlayCellDTO>> result) {
		LinkedList<String> link = new LinkedList<String>(list);
		while(link.size() > 0) {
			String key = link.remove(0);
			List<MatchBetPlayCellDTO> playCellDTOs = playCellMap.get(key);
			for(MatchBetPlayCellDTO dto: playCellDTOs) {
				List<MatchBetPlayCellDTO> playCells = new ArrayList<MatchBetPlayCellDTO>();
				playCells.addAll(dtos);
				playCells.add(dto);
				if(num == 1) {
					playCells.sort((item1,item2)->item1.getPlayCode().compareTo(item2.getPlayCode()));
					result.add(playCells);
				}else {
					matchBetPlayCellsForLottery(num-1, playCellMap, link, playCells, result);
				}
			}
		}
	}
	private void betNumtemp(Double srcAmount, int num, List<MatchBetPlayCellDTO> subList, int indexSize, BetResultInfo betResult) {
//		LinkedList<Integer> link = new LinkedList<Integer>(subListIndex);
		while(indexSize > 0) {
			Integer index = subList.size() - indexSize;
			indexSize--;
			MatchBetPlayCellDTO remove = subList.get(index);
			List<DlJcZqMatchCellDTO> betCells = remove.getBetCells();
			for(DlJcZqMatchCellDTO betCell: betCells) {
				Double amount = srcAmount*Double.valueOf(betCell.getCellOdds());
				if(num == 1) {
					betResult.setBetNum(betResult.getBetNum()+1);
					Double minBonus = betResult.getMinBonus();
					if(Double.compare(minBonus, amount) > 0 || minBonus.equals(0.0)) {
						betResult.setMinBonus(amount);
					}
				}else {
					betNumtemp(amount,num-1,subList, indexSize, betResult);
				}
			}
		}
	}
	/**
	 * 计算组合
	 * @param str
	 * @param num
	 * @param list
	 * @param betList
	 */
	private static void betNum1(String str, int num, List<String> list, List<String> betList) {
		LinkedList<String> link = new LinkedList<String>(list);
		while(link.size() > 0) {
			String remove = link.remove(0);
			String item = str+remove+",";
			if(num == 1) {
				betList.add(item.substring(0,item.length()-1));
			} else {
				betNum1(item,num-1,link, betList);
			}
		}
	}

	/**
	 * 获取河南已出票未获取第三方出奖信息的进行出奖
	 */
	public void updatePrintLotteryThirdRewardHeNan() {
//		每一期次进行处理
		List<DlPrintLottery> dlPrintLotterys = dlPrintLotteryMapper.selectFinishPrintLotteryButNotRewardHeNan();
		if(CollectionUtils.isEmpty(dlPrintLotterys)){
			return ;
		}
		List<String> issueAndGameList = new ArrayList<String>();
		dlPrintLotterys.forEach(print->{
			String issueAndGame = print.getGame()+";"+print.getIssue();
			if(!issueAndGameList.contains(issueAndGame)){
				issueAndGameList.add(issueAndGame);
			}
		});
		log.info("河南期次查询集合={}",issueAndGameList);
//		获取每一期次的逻辑处理
		for(String issueAndGame : issueAndGameList){
			String[] issueAndGameArr = issueAndGame.split(";");
			DlQueryPrizeFileParam param = new DlQueryPrizeFileParam();
			param.setMerchant(merchant);
			param.setVersion("1.0");
			param.setGame(issueAndGameArr[0]);
			param.setIssue(issueAndGameArr[1]);
			DlQueryPrizeFileDTO dto = queryPrizeFile(param);
			if(dto!=null&&StringUtils.isNotEmpty(dto.getUrl())){
//				读取文件内容
				try {
					List<DlPrintLottery> dlPrintList = readFileFromUrl(dto.getUrl());
					for(DlPrintLottery updateDlPrint : dlPrintList){//FIXME 后面有时间改为批量更新
						log.info("河南更新第三方奖金 ticketId={},thirdRewardMoney={}",updateDlPrint.getTicketId(),updateDlPrint.getThirdPartRewardMoney());
						dlPrintLotteryMapper.updatePrintThirdRewardRewardStatus1To3(updateDlPrint);
					}
				} catch (IOException e) {
					log.info("解析河南出票奖金文件失败",e);
				}
				
			}
		}
	}
	
	

	/**
	 * 读取文件封装为对应参数
	 * @param printMap 
	 * @param url
	 * @return
	 * @throws IOException 
	 */
	private List<DlPrintLottery> readFileFromUrl(String urlStr) throws IOException {
		List<DlPrintLottery> issueList = new ArrayList<DlPrintLottery>();
		log.info("解析河南出票兑奖信息 文件地址={}",urlStr);
		 URL url = new URL(urlStr);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String s;
        int row=0;
        while ((s = reader.readLine()) != null) {
        	log.info("查询河南出奖信息内容为={}",s);
        	row++;
        	if(row==1){
        		continue;
        	}
        	String[] printPrizeInfoArr = s.split("\t");
        	log.info("出奖信息={}",printPrizeInfoArr);
        	String ticketId = printPrizeInfoArr[1];
        	String thirdReward = printPrizeInfoArr[3];
			DlPrintLottery updateDlPrint = new DlPrintLottery();
			updateDlPrint.setThirdPartRewardMoney(BigDecimal.valueOf(Integer.parseInt(thirdReward)));
			updateDlPrint.setTicketId(ticketId);
			issueList.add(updateDlPrint);
        }
        reader.close();
		return issueList;
	}

	public void updatePrintLotterysThirdRewardXian() {
		List<DlPrintLottery> dlPrintLotterys = dlPrintLotteryMapper.selectFinishPrintLotteryButNotRewardXian();
		if(CollectionUtils.isEmpty(dlPrintLotterys)){
			return ;
		}
//		获取第三方奖金信息
		List<String> tickets = dlPrintLotterys.stream().map(print->print.getTicketId()).collect(Collectors.toList());
		String[] ticketsArr = tickets.toArray(new String[tickets.size()]);
		DlQueryStakeParam queryStakeParam = new DlQueryStakeParam();
		queryStakeParam.setMerchant(xianMerchant);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		queryStakeParam.setTimestamp(sdf.format(new Date()));
		queryStakeParam.setVersion("1.0");
		queryStakeParam.setOrders(ticketsArr);
		XianDlQueryStakeDTO dlQueryStakeDTO = this.queryStakeXian(queryStakeParam);
		String retCode = dlQueryStakeDTO.getRetCode();
		if("0".equals(retCode)) {
			List<XianBackQueryStake> queryStakes = dlQueryStakeDTO.getOrders();
			for(XianBackQueryStake stake: queryStakes) {
				Integer prizeStatus = stake.getPrizeStatus();
				if(Integer.valueOf(1).equals(prizeStatus) || Integer.valueOf(2).equals(prizeStatus)){
					String ticketId = stake.getTicketId();
					Integer money = stake.getPrizeMoney();
					if(money==null){
						money=Integer.valueOf(0);
					}
					BigDecimal thirdRewardMoney = BigDecimal.valueOf(money);
					DlPrintLottery updateDlPrint = new DlPrintLottery();
					updateDlPrint.setThirdPartRewardMoney(thirdRewardMoney);
					updateDlPrint.setTicketId(ticketId);
					log.info("西安 更新第三方奖金信息 ticketId={},thirdRewardMoney={}",updateDlPrint.getTicketId(),updateDlPrint.getThirdPartRewardMoney());
					dlPrintLotteryMapper.updatePrintThirdRewardRewardStatus1To3(updateDlPrint);
				}
			}
		}
	}
	/**
	 * 期次中奖文件查询
	 * @return
	 */
	private DlQueryPrizeFileDTO queryPrizeFile(DlQueryPrizeFileParam param) {
		param.setTimestamp(DateUtil.getCurrentTimeString(DateUtil.getCurrentTimeLong().longValue(), DateUtil.datetimeFormat));
		JSONObject jo = JSONObject.fromObject(param);
		String backStr = getBackDateByJsonDataHenan(jo, "/prize_file");
		JSONObject backJo = JSONObject.fromObject(backStr);
		DlQueryPrizeFileDTO dlQueryPrizeFileDTO = (DlQueryPrizeFileDTO) JSONObject.toBean(backJo, DlQueryPrizeFileDTO.class); 
		return dlQueryPrizeFileDTO;
	}
}
