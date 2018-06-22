package com.dl.task.service;
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
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.dao.LotteryMatchMapper;
import com.dl.task.dao.PeriodRewardDetailMapper;
import com.dl.task.dto.DlQueryStakeDTO;
import com.dl.task.dto.DlQueryStakeDTO.BackQueryStake;
import com.dl.task.dto.DlToStakeDTO;
import com.dl.task.dto.DlToStakeDTO.BackOrderDetail;
import com.dl.task.model.DlLeagueMatchResult;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.LotteryPrint;
import com.dl.task.model.LotteryThirdApiLog;
import com.dl.task.model.PeriodRewardDetail;
import com.dl.task.param.DlQueryStakeParam;
import com.dl.task.param.DlToStakeParam;
import com.dl.task.param.DlToStakeParam.PrintTicketOrderParam;

import io.jsonwebtoken.lang.Collections;
import net.sf.json.JSONObject;

@Service
@Transactional
public class DlPrintLotteryService {
	private final static Logger log = Logger.getLogger(DlPrintLotteryService.class);
	
    @Resource
    private DlPrintLotteryMapper dlPrintLotteryMapper;
    
    @Resource
    private PeriodRewardDetailMapper  periodRewardDetailMapper;
    
    @Resource
    private DlLeagueMatchResultService matchResultService;
    
    @Resource
    private LotteryMatchMapper   lotteryMatchMapper;
    
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
	
	
	/**
	 * 定时任务：更新彩票信息
	 */
	public void updatePrintLotteryCompareStatus() {
		List<LotteryPrint> lotteryPrints = dlPrintLotteryMapper.lotteryPrintsByUnCompare();
		if(lotteryPrints == null) {
			log.info("updatePrintLotteryCompareStatus 没有获取到需要更新状态的彩票数据");
			return;
		}
		log.info("updatePrintLotteryCompareStatus 获取到需要更新状态的彩票数据，size="+lotteryPrints.size());
		//获取没有赛事结果比较的playcodes
		Set<String> unPlayCodes = new HashSet<String>();
		List<LotteryPrint> endPrints = new ArrayList<LotteryPrint>(lotteryPrints.size());
		for(LotteryPrint print: lotteryPrints) {
			List<String> playCodes = this.printStakePlayCodes(print);
			String comparedStakes = print.getComparedStakes();
			List<String> comparedPlayCodes = null;
			if(StringUtils.isNotEmpty(comparedStakes)) {
				comparedPlayCodes = Arrays.asList(comparedStakes.split(","));
			}
			if(comparedPlayCodes != null) {
				playCodes.removeAll(comparedPlayCodes);
			}
			if(playCodes.size() == 0) {
				print.setCompareStatus(ProjectConstant.FINISH_COMPARE);
				endPrints.add(print);
			}else {
				unPlayCodes.addAll(playCodes);
			}
		}
		log.info("updatePrintLotteryCompareStatus 未更新状态彩票对应其次数，size="+unPlayCodes.size());
		if(CollectionUtils.isEmpty(unPlayCodes)) {
			return;
		}
		//获取赛事结果
		List<String> playCodes = new ArrayList<String>(unPlayCodes.size());
		playCodes.addAll(unPlayCodes);
		List<String> canCelPlayCodes = lotteryMatchMapper.getCancelMatches(playCodes);
		List<DlLeagueMatchResult> matchResults = matchResultService.queryMatchResultsByPlayCodes(playCodes);
		if(CollectionUtils.isEmpty(matchResults) && Collections.isEmpty(canCelPlayCodes)) {
			log.info("updatePrintLotteryCompareStatus 准备获取赛事结果的场次数："+playCodes.size() +" 没有获取到相应的赛事结果信息也没有取消的赛事");
			return;
		}
		log.info("updatePrintLotteryCompareStatus 准备获取赛事结果的场次数："+playCodes.size() +" 获取到相应的赛事结果信息数："+matchResults.size() + "  已取消赛事"+canCelPlayCodes.size());
		
		Map<String, List<DlLeagueMatchResult>> resultMap = new HashMap<String, List<DlLeagueMatchResult>>();
		if(!CollectionUtils.isEmpty(matchResults)) {
			for(DlLeagueMatchResult dto: matchResults) {
				String playCode = dto.getPlayCode();
				List<DlLeagueMatchResult> list = resultMap.get(playCode);
				if(list == null) {
					list = new ArrayList<DlLeagueMatchResult>(5);
					resultMap.put(playCode, list);
				}
				list.add(dto);
			}
		}
		//
		List<LotteryPrint> updates = new ArrayList<LotteryPrint>(lotteryPrints.size());
		for(String playCode: playCodes) {
			boolean isCancel = false;
			if(canCelPlayCodes.contains(playCode)) {
				isCancel = true;
			}
			List<DlLeagueMatchResult> matchResultList = resultMap.get(playCode);
			if(!isCancel && CollectionUtils.isEmpty(matchResultList)) {
				continue;
			}
			for(LotteryPrint print: lotteryPrints) {
				String stakes = print.getStakes();
				String comparedStakes = print.getComparedStakes()==null?"":print.getComparedStakes();
				//判断是否对比过
				if(stakes.contains(playCode) && !comparedStakes.contains(playCode)) {
					if(comparedStakes.length() > 0) {
						comparedStakes +=",";
					}
					comparedStakes += playCode;
					LotteryPrint updatePrint = new LotteryPrint();
					updatePrint.setPrintLotteryId(print.getPrintLotteryId());
					updatePrint.setComparedStakes(comparedStakes);
					String[] stakesarr = stakes.split(";");
					StringBuffer sbuf = new StringBuffer();
					Set<String> stakePlayCodes = new HashSet<String>(stakesarr.length);
					//彩票的每一场次分析
					for(String stake: stakesarr) {
						String[] split = stake.split("\\|");
						stakePlayCodes.add(split[1]);
						if(stake.contains(playCode)) {
							String playTypeStr = split[0];
							List<String> cellCodes = Arrays.asList(split[2].split(","));
							if(isCancel) {
								sbuf.append(";").append(playTypeStr).append("|")
								.append(playCode).append("|");
								for(int i=0; i< cellCodes.size(); i++) {
									if(i > 0) {
										sbuf.append(",");
									}
									String cellCode = cellCodes.get(i);
									sbuf.append(cellCode).append("@").append("1.00");
								}
							}else {
								//比赛结果获取中奖信息
								for(DlLeagueMatchResult rst : matchResultList) {
									if(rst.getPlayType().equals(Integer.valueOf(playTypeStr))) {
										String cellCode = rst.getCellCode();
										if(cellCodes.contains(cellCode)) {
											Map<String, String> aa = this.aa(print.getPrintSp());
											String key = rst.getPlayCode() + "|" + rst.getCellCode();
											String odds = aa.get(key);
											if(StringUtils.isNotBlank(odds)) {
												sbuf.append(";").append("0").append(rst.getPlayType()).append("|")
												.append(key)
												.append("@").append(odds);
												break;
											}
										}
									}
								}
							}
						}
					}
					//中奖记录
					String reward = print.getRewardStakes();
					if(sbuf.length() > 0) {
						reward = StringUtils.isBlank(reward)?sbuf.substring(1, sbuf.length()):(reward+sbuf.toString());
						updatePrint.setRewardStakes(reward);
					}
					
					//彩票对票结束 
					if(stakePlayCodes.size() == comparedStakes.split(",").length) {
						updatePrint.setCompareStatus(ProjectConstant.FINISH_COMPARE);
						if(StringUtils.isNotBlank(reward)) {
							//彩票中奖金额
							//log.info(reward);
							List<String> spList = Arrays.asList(reward.split(";"));
							List<List<Double>> winSPList = spList.stream().map(s -> {
								String cells = s.split("\\|")[2];
								String[] split = cells.split(",");
								List<Double> list = new ArrayList<Double>(split.length);
								for(String str: split) {
									list.add(Double.valueOf(str.substring(str.indexOf("@")+1)));
								}
								return list;
							}).collect(Collectors.toList());
							List<Double> rewardList = new ArrayList<Double>();
							/*this.groupByRewardList(Double.valueOf(2 * print.getTimes()), Integer.valueOf(print.getBetType()) / 10,winSPList, rewardList);
							double rewardSum = rewardList.stream().reduce(0.00, Double::sum);*/
							//2018-06-04计算税
							this.groupByRewardList(2.0, Integer.valueOf(print.getBetType()) / 10,winSPList, rewardList);
							double rewardSum = rewardList.stream().reduce(0.00, Double::sum)*print.getTimes();
							updatePrint.setRealRewardMoney(BigDecimal.valueOf(rewardSum));
							// 保存第三方给计算的单张彩票的价格
							PeriodRewardDetail periodRewardDetail = new PeriodRewardDetail();
							periodRewardDetail.setTicketId(print.getTicketId());
							List<PeriodRewardDetail> tickets = periodRewardDetailMapper.queryPeriodRewardDetailBySelective(periodRewardDetail);
							if (!CollectionUtils.isEmpty(tickets)) {
								BigDecimal thirdPartRewardMoney = BigDecimal.valueOf(tickets.get(0).getReward());
								updatePrint.setThirdPartRewardMoney(thirdPartRewardMoney);
							}
						}
					}
					//添加
					updates.add(updatePrint);
				}//判断是否对比过over
			}//over prints for
		}//over playcode for
		this.updateBatchLotteryPrint(updates);
		this.updateBatchLotteryPrint(endPrints);
	}
	
	/**
	 * 获取playcode
	 * @param print
	 * @return
	 */
	private List<String> printStakePlayCodes(LotteryPrint print) {
		String stakes = print.getStakes();
		String[] split = stakes.split(";");
		List<String> playCodes = new ArrayList<String>(split.length);
		for(String str: split) {
			String[] split2 = str.split("\\|");
			String playCode = split2[1];
			playCodes.add(playCode);
		}
		return playCodes;
	}	
	
	/**
	 * 高速批量更新LotteryPrint 10万条数据 18s
	 * @param list
	 */
	public void updateBatchLotteryPrint(List<LotteryPrint> list) {
		log.info("updateBatchLotteryPrint 准备更新彩票信息到数据库：size" + list.size());
		int num = 0;
		for(LotteryPrint print: list) {
			if(null == print.getRealRewardMoney()) {
				print.setRealRewardMoney(BigDecimal.ZERO);
			}
			int n = dlPrintLotteryMapper.updateBatchLotteryPrint(print);
			if(n > 0) {
				num += n;
			}
		}
		log.info("updateBatchLotteryPrint 更新彩票信息到数据库：size" + list.size() + "  入库返回：size=" + num);
	}
	
	/**
	 * 组合中奖集合
	 * @param amount:初始值2*times
	 * @param num:几串几
	 * @param list:赔率
	 * @param rewardList:组合后的中奖金额list
	 */
	private void groupByRewardList(Double amount, int num, List<List<Double>> list, List<Double> rewardList) {
		LinkedList<List<Double>> link = new LinkedList<List<Double>>(list);
		while(link.size() > 0) {
			List<Double> removes = link.remove(0);
			for(Double remove: removes) {
				Double item = amount*remove;
				if(num == 1) {
					//start对大于等于10000的单注奖金进行20%税收，：单注彩票奖金大于或者等于1万元时，扣除20%的偶然所得税后再派奖
					if(item.doubleValue() >= 10000) {
						item = item*0.8;
					}
					//end
					rewardList.add(item);
				} else {
					groupByRewardList(item,num-1,link, rewardList);
				}
			}
		}		
	}
	
	private Map<String,String> aa(String printSp) {
		List<String> spList = Arrays.asList(printSp.split(";"));
		Map<String,String> spMap = new HashMap<String,String>();
		for(String temp:spList) {
			if(temp.contains(",")) {
				String playCode = temp.substring(0, temp.lastIndexOf("|"));
				String temp2 =  temp.substring(temp.lastIndexOf("|")+1);
				String[] tempArr = temp2.split(",");
				for(int j = 0;j < tempArr.length;j++) {
					String temp3 = playCode + "|" + tempArr[j];
					spMap.put(temp3.substring(0,temp3.indexOf("@")), temp3.substring(temp3.indexOf("@")+1));
				}
			}else {
				spMap.put(temp.substring(0,temp.indexOf("@")), temp.substring(temp.indexOf("@")+1));
			}
		}
		return spMap;
	}
	
}
