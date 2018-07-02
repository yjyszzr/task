package com.dl.task.service;

import io.jsonwebtoken.lang.Collections;

import java.math.BigDecimal;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.dl.base.configurer.RestTemplateConfig;
import com.dl.base.enums.ThirdApiEnum;
import com.dl.base.util.DateUtil;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.MD5Utils;
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.dao.PeriodRewardDetailMapper;
import com.dl.task.dao2.DlLeagueMatchResultMapper;
import com.dl.task.dao2.LotteryMatchMapper;
import com.dl.task.dto.DlQueryStakeDTO;
import com.dl.task.dto.DlQueryStakeDTO.BackQueryStake;
import com.dl.task.dto.DlToStakeDTO;
import com.dl.task.dto.DlToStakeDTO.BackOrderDetail;
import com.dl.task.dto.XianDlQueryStakeDTO;
import com.dl.task.dto.XianDlQueryStakeDTO.XianBackQueryStake;
import com.dl.task.dto.XianDlToStakeDTO;
import com.dl.task.dto.XianDlToStakeDTO.XianBackOrderDetail;
import com.dl.task.model.DlLeagueMatchResult;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.LotteryThirdApiLog;
import com.dl.task.param.DlQueryStakeParam;
import com.dl.task.param.DlToStakeParam;
import com.dl.task.param.DlToStakeParam.PrintTicketOrderParam;

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
					String stakes = lotteryPrint.getStakes();
					String sp = stake.getSp();
					String comparePrintSp = getComparePrintSpXian(sp, stake.getTicketId());
					comparePrintSp = StringUtils.isBlank(comparePrintSp)?sp:comparePrintSp;

					String game = lotteryPrint.getGame();
					String printSp = null;
					if("T51".equals(game) && StringUtils.isNotBlank(comparePrintSp)) {
						printSp = this.getPrintSp(stakes, comparePrintSp);
					} else if("T56".equals(game)) {
						printSp = comparePrintSp;
					}
					lotteryPrint.setPlatformId(stake.getPlatformId());
					lotteryPrint.setPrintNo(stake.getPrintNo());
					lotteryPrint.setPrintSp(printSp);
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
		JSONObject backJo = JSONObject.fromObject(backStr);
		@SuppressWarnings("rawtypes")
		Map<String,Class> mapClass = new HashMap<String,Class>();
		mapClass.put("orders", XianBackQueryStake.class);
		XianDlQueryStakeDTO dlQueryStakeDTO = (XianDlQueryStakeDTO) JSONObject.toBean(backJo, XianDlQueryStakeDTO.class, mapClass); 
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
					String stakes = lotteryPrint.getStakes();
					String sp = stake.getSp();
					String comparePrintSp = getComparePrintSpHenan(sp, stake.getTicketId());
					comparePrintSp = StringUtils.isBlank(comparePrintSp)?sp:comparePrintSp;

					String game = lotteryPrint.getGame();
					String printSp = null;
					if("T51".equals(game) && StringUtils.isNotBlank(comparePrintSp)) {
						printSp = this.getPrintSp(stakes, comparePrintSp);
					} else if("T56".equals(game)) {
						printSp = comparePrintSp;
					}
					lotteryPrint.setPlatformId(stake.getPlatformId());
					lotteryPrint.setPrintNo(stake.getPrintNo());
					lotteryPrint.setPrintSp(printSp);
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
		param.setMerchant(merchant);
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
//	public static void main(String[] args) {
//		String heNanUrl="http://1.192.90.178:9085";
//		String heNanMerchant="180326";
//		String heNanPwd="0FC67A15";
//		Map<String,Object> param = new HashMap<String, Object>();
//		param.put("game", "T51");
//		param.put("issue", "201806306110");
//		param.put("merchant", heNanMerchant);
//		param.put("version", "1.0");
//		param.put("timestamp", DateUtil.getCurrentTimeString(DateUtil.getCurrentTimeLong().longValue(), DateUtil.datetimeFormat));
//		JSONObject jo = JSONObject.fromObject(param);
//		String authStr = heNanMerchant + heNanPwd + jo.toString();
//		HttpHeaders headers = new HttpHeaders();
//		MediaType type = MediaType.parseMediaType("application/json;charset=UTF-8");
//		headers.setContentType(type);
//		String authorization = MD5Utils.MD5(authStr);
//		headers.add("Authorization", authorization);
//		HttpEntity<JSONObject> requestEntity = new HttpEntity<JSONObject>(jo, headers);
//		String requestUrl = heNanUrl + "/prize_file";
//		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//        //ms
//        factory.setReadTimeout(5000);
//        //ms
//        factory.setConnectTimeout(15000);
//        RestTemplate restTemplate = new RestTemplate(factory);
//        List<HttpMessageConverter<?>> messageConverters = Lists.newArrayList();
//        for (HttpMessageConverter httpMessageConverter : restTemplate.getMessageConverters()) {
//            if (httpMessageConverter instanceof StringHttpMessageConverter) {
//                messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
//                continue;
//            }
//            messageConverters.add(httpMessageConverter);
//        }
//        restTemplate.setMessageConverters(messageConverters);
//		String response = restTemplate.postForObject(requestUrl, requestEntity, String.class);
//		log.info(response);
//	}
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

}
