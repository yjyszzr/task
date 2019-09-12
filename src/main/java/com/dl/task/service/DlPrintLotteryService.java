package com.dl.task.service;

import com.dl.base.enums.*;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.BetUtils;
import com.dl.base.util.DateUtil;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.SNGenerator;
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.*;
import com.dl.task.dao2.*;
import com.dl.task.dto.*;
import com.dl.task.enums.PrintLotteryStatusEnum;
import com.dl.task.enums.ThirdRewardStatusEnum;
import com.dl.task.model.*;
import com.dl.task.param.DlJcZqMatchBetParam;
import com.dl.task.printlottery.PrintComEnums;
import com.dl.task.printlottery.PrintLotteryAdapter;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO.QueryRewardOrderResponse;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO.QueryStakeOrderResponse;
import com.dl.task.printlottery.responseDto.ToRewardResponseDTO;
import com.dl.task.printlottery.responseDto.ToRewardResponseDTO.ToRewardOrderResponse;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO.ToStakeBackOrderDetail;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

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
	private DlSuperLottoMapper dlSuperLottoMapper;
	
	@Resource
	private OrderDetailMapper orderDetailMapper;

	@Resource
	private PrintLotteryAdapter printLotteryAdapter;
	
    @Resource
    private DlMatchBasketballMapper dlMatchBasketballMapper;
    
    @Resource
    private DlResultBasketballMapper dlResultBasketballMapper;
    
    @Resource
    private DlArtifiPrintLotteryMapper dlArtifiPrintLotteryMapper;

    @Resource
	private OrderMapper orderMapper;
    
    @Resource
    private OrderService orderService;

	@Value("${print.ticket.merchant}")
	private String merchant;
	
	
	/**
	 * 隐藏赛事信息
	 */
	public void updateMatchShowOrdel() {
		try {
			List<LotteryMatch> lotteryMatchs = lotteryMatchMapper.getMatchShowOrDel();//获取当天比赛
			if(lotteryMatchs!=null && lotteryMatchs.size()>0) {
				lotteryMatchMapper.updateMatchShowOrDel(lotteryMatchs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * 隐藏赛事信息
	 */
	public void updateMatchShowOrDelShow() {
		try {
			List<LotteryMatch> lotteryMatchs = lotteryMatchMapper.getMatchShowOrDelShow();//获取当天比赛
			if(lotteryMatchs!=null && lotteryMatchs.size()>0) {
				lotteryMatchMapper.updateMatchShowOrDelShow(lotteryMatchs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * 更新赛果信息
	 */
	public void updatePrintLotteryCompareStatus() {
		List<DlPrintLottery> lotteryPrints = dlPrintLotteryMapper.lotteryPrintsByUnCompare();
//		log.info("未更新赛果的出票列表======================{}",lotteryPrints);
		if (lotteryPrints == null) {
			log.info("updatePrintLotteryCompareStatus 没有获取到需要更新状态的彩票数据");
			return;
		}
		List<String> T01Game = new ArrayList<String>();
		lotteryPrints.forEach(print->{
			if("T01".equals(print.getGame())){
				T01Game.add(print.getIssue()); 
			}
		});
	   List<String> uniqueT01Game = T01Game.stream().distinct().collect(Collectors.toList());
	   log.info("游戏期次uniqueT01Game===================={}",uniqueT01Game);
		updateT01GameDetailAndLottery(uniqueT01Game);
	}
	
	private void updateT01GameDetailAndLottery(List<String> t01Game) {
		log.info("游戏期次==========================={}",t01Game);
		for(String t01Issue:t01Game){
			DlSuperLotto dlSuperLotto = dlSuperLottoMapper.selectPrizeResultByTermNum(t01Issue);
//			log.info("游戏期次详情==========================={}",dlSuperLotto);
			if(dlSuperLotto!=null&&!StringUtils.isEmpty(dlSuperLotto.getPrizeNum())){				
				orderDetailMapper.beatchUpdateMatchResult(t01Issue,dlSuperLotto.getPrizeNum());
				dlPrintLotteryMapper.beatchUpdateComparedStakes(t01Issue);
			}
		}
	}

    /**
     * 不包含差值得于0的情况
     * @param vnmScore
     * @return
     */
    public static String whichWNMPeriod(Integer vnmScore){
        if(vnmScore >= 1 && vnmScore <= 5){
            return "客胜1-5";
        }else if(vnmScore >= 6 && vnmScore <= 10){
            return "客胜6-10";
        }else if(vnmScore >= 11 && vnmScore <= 15){
            return "客胜11-15";
        }else if(vnmScore >= 16 && vnmScore <= 20){
            return "客胜16-20";
        }else if(vnmScore >= 21 && vnmScore <= 25){
            return "客胜21-25";
        }else if(vnmScore >= 26){
            return "客胜26+";
        }else if(vnmScore >= -5 && vnmScore <= -1){
            return "主胜1-5";
        }else if(vnmScore >= -10 && vnmScore <= -6){
            return "主胜6-10";
        }else if(vnmScore >= -15 && vnmScore <= -11){
            return "主胜11-15";
        }else if(vnmScore >= -20 && vnmScore <= -16){
            return "主胜16-20";
        }else if(vnmScore >= -25 && vnmScore <= -21){
            return "主胜21-25";
        }else if( vnmScore <= -26){
            return "主胜26+";
        }

        return "";
    }

    /**
     * 构造篮球的matchResult
     */
    public List<JsonResultBasketball> generateBasketResult(List<CountBasketBaseInfo> countBasketBaseInfoList){
        List<JsonResultBasketball> jsonResultBasketballList = new ArrayList<>();
        for(CountBasketBaseInfo s:countBasketBaseInfoList){
            JsonResultBasketball jsonResultBasketball = new JsonResultBasketball();
            jsonResultBasketball.setOrderDetailId(s.getOrderDetailId());
            jsonResultBasketball.setOrderSn(s.getOrderSn());
            jsonResultBasketball.setTicketData(s.getTicketData());
            Integer changCiId = s.getChangCiId();
            jsonResultBasketball.setChangciId(changCiId);
            jsonResultBasketball.setPlayCode(s.getPlayCode());
            String score = s.getScore();//客队：主队
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(score)) {
                String[] VHArr = score.split(":");
                if (Integer.valueOf(VHArr[0]) > Integer.valueOf(VHArr[1])) {
                    jsonResultBasketball.setMnlResult("客胜");
                } else if (Integer.valueOf(VHArr[0]) < Integer.valueOf(VHArr[1])) {
                    jsonResultBasketball.setMnlResult("主胜");
                }

                if (org.apache.commons.lang3.StringUtils.isNotEmpty(s.getRangFen())) {
                    if (Double.valueOf(VHArr[1]) + Double.valueOf(s.getRangFen()) > Double.valueOf(VHArr[0])) {
                        jsonResultBasketball.setHdcResult("主胜");
                    } else if (Double.valueOf(VHArr[1]) + Double.valueOf(s.getRangFen()) < Double.valueOf(VHArr[0])) {
                        jsonResultBasketball.setHdcResult("主负");
                    }
                } else {
                    continue;
                }

                Integer vnmScore = Integer.valueOf(VHArr[0]) - Integer.valueOf(VHArr[1]);
                jsonResultBasketball.setWnmResult(whichWNMPeriod(vnmScore));

                if (org.apache.commons.lang3.StringUtils.isNotEmpty(s.getForecastScore())) {
                    if (Double.valueOf(VHArr[0]) + Double.valueOf(VHArr[1]) > Double.valueOf(s.getForecastScore())) {
                        jsonResultBasketball.setHiloResult("大");
                    } else if (Double.valueOf(VHArr[0]) + Double.valueOf(VHArr[1]) < Double.valueOf(s.getForecastScore())) {
                        jsonResultBasketball.setHiloResult("小");
                    }
                } else {
                    continue;
                }

            }
            jsonResultBasketballList.add(jsonResultBasketball);
        }

        return jsonResultBasketballList;
    }

	/**
	 * 适用于竞彩篮球,原先是由赛果来开每张彩票的奖，现在由订单详情和比赛的比分计算是否中奖
	 */
	// ※※※※※※※※※※※※※※※※※
	// ※※※※※※※※※※※※※※※※※
	// 定时任务：更新彩票信息
	public void updatePrintLotteryCompareStatusJl() {
		List<DlPrintLottery> lotteryPrints = dlPrintLotteryMapper.lotteryPrintsByUnCompareJl();
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
		log.info("updatePrintLotteryCompareStatus 未更新状态彩票对应期次={}",unPlayCodes);
		if (CollectionUtils.isEmpty(unPlayCodes)) {
			return;
		}
		// 获取赛事结果
		List<String> playCodes = new ArrayList<String>(unPlayCodes.size());
		playCodes.addAll(unPlayCodes);
		List<String> canCelPlayCodes = dlMatchBasketballMapper.getCancelMatches(playCodes);
		List<DlMatchBasketball> matchBasketBallList = dlMatchBasketballMapper.getChangciIdsFromBasketMatchByPlayCodes(playCodes);
		Map<Integer,String> pcodeAndCIdMap = matchBasketBallList.stream().collect(Collectors.toMap(DlMatchBasketball::getChangciId,DlMatchBasketball::getMatchSn));
//		List<Integer> changciIds = matchBasketBallList.stream().map(s->s.getChangciId()).collect(Collectors.toList());
//		List<DlResultBasketball> matchResults = dlResultBasketballMapper.queryMatchResultsByChangciIds(changciIds);
		List<String> orderSnList = lotteryPrints.stream().map(s->s.getOrderSn()).collect(Collectors.toList());
        List<OrderDetail> orderDetails = orderDetailMapper.getOrderDetailsByOrderSns(orderSnList);

        //转换
        List<CountBasketBaseInfo> countBasketBaseInfoList = new ArrayList<CountBasketBaseInfo>();
        for(OrderDetail s:orderDetails) {
            String issue = s.getIssue();
            CountBasketBaseInfo countBasketBaseInfo = new CountBasketBaseInfo();
            for(DlMatchBasketball ss:matchBasketBallList) {
                if (s.getIssue().equals(ss.getMatchSn())) {
                    countBasketBaseInfo.setOrderDetailId(s.getOrderDetailId());
                    countBasketBaseInfo.setOrderSn(s.getOrderSn());
                    countBasketBaseInfo.setTicketData(s.getTicketData());
                    countBasketBaseInfo.setChangCiId(ss.getChangciId());
                    countBasketBaseInfo.setPlayCode(s.getIssue());//issue 就是playcode
                    countBasketBaseInfo.setScore(ss.getWhole());//比分
                    countBasketBaseInfo.setForecastScore(s.getForecastScore());//订单详情中预设总分
                    countBasketBaseInfo.setRangFen(s.getFixedodds());//让分
                    countBasketBaseInfoList.add(countBasketBaseInfo);
                    break;
                }
            }
        }
        List<JsonResultBasketball> jsonResultBasketballList = generateBasketResult(countBasketBaseInfoList);
        //转换

		if (CollectionUtils.isEmpty(jsonResultBasketballList) && Collections.isEmpty(canCelPlayCodes)) {
			log.info("updatePrintLotteryCompareStatus 准备获取赛事结果的场次数：" + playCodes.size() + " 没有获取到相应的赛事结果信息也没有取消的赛事");
			return;
		}

		Map<Integer,List<BasketMatchOneResultDTO>> resultMap = new HashMap<>();
		List<BasketMatchOneResultDTO> matchOneResult = new ArrayList<>();
		for(JsonResultBasketball basketBallResult:jsonResultBasketballList) {
			Integer changciId = basketBallResult.getChangciId();
			Integer orderDetailId = basketBallResult.getOrderDetailId();
			String playCode = pcodeAndCIdMap.get(changciId);
			String hdc_result = basketBallResult.getHdcResult();
			String hilo_result = basketBallResult.getHiloResult();
			String mnl_result = basketBallResult.getMnlResult();
			String wnm_result = basketBallResult.getWnmResult();
			BasketMatchOneResultDTO dto1 = new BasketMatchOneResultDTO();
            dto1.setPlayType(String.valueOf(MatchBasketPlayTypeEnum.PLAY_TYPE_MNL.getcode()));
            dto1.setPlayCode(playCode);
            mnl_result = mnl_result.replaceAll(" ", "");
            if(mnl_result.equals("主负")){
                mnl_result = "客胜";
            }
            dto1.setCellCode(String.valueOf(MatchBasketBallResultHDCEnum.getCode(mnl_result)));
            dto1.setCellName(mnl_result);

            BasketMatchOneResultDTO dto2 = new BasketMatchOneResultDTO();
            dto2.setPlayType(String.valueOf(MatchBasketPlayTypeEnum.PLAY_TYPE_HDC.getcode()));
            dto2.setPlayCode(playCode);
            if(hdc_result.equals("主胜")){
                hdc_result = "让分主胜";
            }else if(hdc_result.equals("主负") || hdc_result.equals("让分主负")){
                hdc_result = "让分客胜";
            }
            dto2.setCellCode(String.valueOf(MatchBasketResultHdEnum.getCode(hdc_result)));
            dto2.setCellName(hdc_result);
			
			BasketMatchOneResultDTO dto3 = new BasketMatchOneResultDTO();
			dto3.setPlayType(String.valueOf(MatchBasketPlayTypeEnum.PLAY_TYPE_WNM.getcode()));
			dto3.setPlayCode(playCode);
			dto3.setCellCode(reHVMNLCode(wnm_result));
			dto3.setCellName(wnm_result);			
			
			BasketMatchOneResultDTO dto4 = new BasketMatchOneResultDTO();
			dto4.setPlayType(String.valueOf(MatchBasketPlayTypeEnum.PLAY_TYPE_HILO.getcode()));
			dto4.setPlayCode(playCode);
			dto4.setCellCode(MatchBasketBallResultHILOEnum.getCode(hilo_result+"分"));
			dto4.setCellName(hilo_result);
			
			matchOneResult.add(dto1);
			matchOneResult.add(dto2);
			matchOneResult.add(dto3);
			matchOneResult.add(dto4);
			
			resultMap.put(orderDetailId, matchOneResult);
		}
	
		List<DlPrintLottery> updates = new ArrayList<DlPrintLottery>(lotteryPrints.size());
		for (JsonResultBasketball basketBallResult : jsonResultBasketballList) {//循环订单详情的赛果构成的集合
            Integer orderDetailId = basketBallResult.getOrderDetailId();
            String playCode = basketBallResult.getPlayCode();
            boolean isCancel = false;
            if (canCelPlayCodes.contains(playCode)) {
                isCancel = true;
            }
            List<BasketMatchOneResultDTO> matchResultList = resultMap.get(orderDetailId);
            if (!isCancel && CollectionUtils.isEmpty(matchResultList)) {
                continue;
            }
            for (DlPrintLottery print : lotteryPrints) {
                if (basketBallResult.getOrderSn().equals(print.getOrderSn()) && print.getStakes().contains(basketBallResult.getPlayCode())) {
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
                                    List<BasketMatchOneResultDTO> matchResultListNew = matchResultList.stream().filter(s -> s.getPlayCode().equals(split[1])).collect(Collectors.toList());
                                    for (BasketMatchOneResultDTO rst : matchResultListNew) {

                                        if (rst.getPlayType().equals(String.valueOf(Integer.valueOf(playTypeStr)))) {
                                            playTypehaveResult = Boolean.TRUE;
                                            String cellCode = rst.getCellCode();
                                            if (cellCodes.contains(cellCode)) {
                                                Map<String, String> aa = this.aa(print.getPrintSp());
                                                String key = rst.getPlayCode() + "|" + rst.getCellCode();
                                                String odds = aa.get(key);
                                                if (StringUtils.isNotBlank(odds)) {
                                                    sbuf.append(";").append("0").append(String.valueOf(Integer.valueOf(playTypeStr))).append("|").append(key).append("@").append(odds);
                                                    break;
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                        }
                        if (!playTypehaveResult) {//没有赛果时，跳出该处理等待下次循环
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
                                Double oneTimeReward = rewardList.stream().reduce(0.00, Double::sum);
                                BigDecimal allTimesReward = new BigDecimal(oneTimeReward).multiply(new BigDecimal(print.getTimes()));
                                updatePrint.setRealRewardMoney(allTimesReward.setScale(2, RoundingMode.HALF_EVEN));
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
                }else{
                    continue;
                }// over prints for
            }
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
     * 获取胜分差的code
     * @param wnm_result
     * @return
     */
    public String reHVMNLCode(String wnmResult){
        String hRst = BasketBallHILOLeverlEnum.getCode(wnmResult.substring(2)+"分");
        if(wnmResult.contains("客胜")){
            hRst = String.valueOf(Integer.valueOf(hRst) + 6);
        }
        return hRst;
    }


	/**
	 * 仅适用于竞彩足球
	 */
	// ※※※※※※※※※※※※※※※※※
	// ※※※※※※※※※※※※※※※※※
	// 定时任务：更新彩票信息
	public void updatePrintLotteryCompareStatusJz() {
		List<DlPrintLottery> lotteryPrints = dlPrintLotteryMapper.lotteryPrintsByUnCompareJz();
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
		log.info("updatePrintLotteryCompareStatus 未更新状态彩票对应期次={}",unPlayCodes);
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
							Double oneTimeReward = rewardList.stream().reduce(0.00, Double::sum);
							//BigDecimal allTimesReward = new BigDecimal(oneTimeReward).setScale(3,RoundingMode.DOWN).setScale(2, RoundingMode.HALF_EVEN).multiply(new BigDecimal(print.getTimes()));
                            BigDecimal allTimesReward = new BigDecimal(oneTimeReward).multiply(new BigDecimal(print.getTimes()));
                            updatePrint.setRealRewardMoney(allTimesReward.setScale(2, RoundingMode.HALF_EVEN));
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
	public static void groupByRewardList(Double amount, int num, List<List<Double>> list, List<Double> rewardList) {
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
			MatchBetPlayDTO matchBetDto = new MatchBetPlayDTO();
			matchBetDto.setChangci(detail.getChangci());
			matchBetDto.setIsDan(detail.getIsDan());
			matchBetDto.setLotteryClassifyId(detail.getLotteryClassifyId());
			matchBetDto.setLotteryPlayClassifyId(detail.getLotteryPlayClassifyId());
			matchBetDto.setMatchId(detail.getMatchId());
			matchBetDto.setMatchTeam(detail.getMatchTeam());
			matchBetDto.setBetType(detail.getBetType());
			matchBetDto.setTicketData(detail.getTicketData());
			Date matchTime = detail.getMatchTime();
			matchBetDto.setMatchTime((int)matchTime.toInstant().getEpochSecond());
			if(Integer.valueOf(2).equals(detail.getLotteryClassifyId())){//竞彩足球逻辑保存
				matchBetDto.setPlayCode(detail.getIssue());
				 return matchBetDto;
			}
			String ticketData = detail.getTicketData();
			String[] tickets = ticketData.split(";");
			String playCode = null;
			List<MatchBetCellDTO> matchBetCells = new ArrayList<MatchBetCellDTO>(tickets.length);
			Integer lotteryClassifyId = detail.getLotteryClassifyId();

			if(1 == lotteryClassifyId ){
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
            }else if(3 == lotteryClassifyId ){
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
                        String matchResult = getBasketCathecticData(split[0], split3[0]);
                        DlJcZqMatchCellDTO dto = new DlJcZqMatchCellDTO(split3[0], matchResult, split3[1]);
                        return dto;
                    }).collect(Collectors.toList());

                    MatchBetCellDTO matchBetCell = new MatchBetCellDTO();
                    matchBetCell.setPlayType(playType);
                    matchBetCell.setBetCells(betCells);
                    matchBetCells.add(matchBetCell);
                }
            }

			matchBetDto.setPlayCode(playCode);
			matchBetDto.setMatchBetCells(matchBetCells);
			return matchBetDto;
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
		if(Integer.valueOf(2).equals(param.getLotteryClassifyId())){
			return getDaLeTouLotteryPrintDTO(param);
		}
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
				
//				List<DlJcZqMatchCellDTO> betCells = (List<DlJcZqMatchCellDTO>) subList.stream().map(s->s.getBetCells());
//				Map<String,String> cellMap = betCells.stream().collect(Collectors.toMap(DlJcZqMatchCellDTO::getCellCode, DlJcZqMatchCellDTO::getCellOdds));
				
				String printSpWithData = subList.stream().map(cdto->{
					String playCode = cdto.getPlayCode();
					String playType1 = cdto.getPlayType();
					String cellCodes = cdto.getBetCells().stream().map(cell->{
						return cell.getCellCode();
					}).collect(Collectors.joining(","));
					return playType1 + "|" + playCode + "|" + cellCodes;
				}).collect(Collectors.joining(";"));
						
				String orderTimePrintSp = subList.stream().map(cdto->{
					String playCode = cdto.getPlayCode();
					String cellCodes = cdto.getBetCells().stream().map(cell->{
						return cell.getCellCode()+"@"+cell.getCellOdds();
					}).collect(Collectors.joining(","));
					return  playCode + "|" + cellCodes;
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
						lotteryPrintDTO.setPrintSp(orderTimePrintSp);
						lotteryPrintDTO.setStakes(stakes);
						String ticketId = SNGenerator.nextSN(SNBusinessCodeEnum.TICKET_SN.getCode());
						lotteryPrintDTO.setTicketId(ticketId);
						lotteryPrintDTO.setTimes(maxTime);
						lotteryPrintDTO.setPrintSp(orderTimePrintSp);
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
					lotteryPrintDTO.setPrintSp(orderTimePrintSp);
					String ticketId = SNGenerator.nextSN(SNBusinessCodeEnum.TICKET_SN.getCode());
					lotteryPrintDTO.setTicketId(ticketId);
					lotteryPrintDTO.setTimes(m);
					lotteryPrintDTO.setPrintSp(orderTimePrintSp);
					lotteryPrints.add(lotteryPrintDTO);
				}


			}
		}
		long end3 = System.currentTimeMillis();
		log.info("3计算预出票基础信息用时：" + (end3-end2)+ " - "+start);
		log.info("5计算预出票信息用时：" + (end3-start)+ " - "+start);
		return lotteryPrints;
	}
	
	/**
	 * 计算大乐透的拆票信息
	 * @param param
	 * @return
	 */
	private List<LotteryPrintDTO> getDaLeTouLotteryPrintDTO(
			DlJcZqMatchBetParam param) {
		List<LotteryPrintDTO> lotteryPrints = new ArrayList<LotteryPrintDTO>();
		String playType = param.getPlayType();
		int times = param.getTimes();
		BigDecimal oneTicketMoney = new BigDecimal("2.0");
		if("05".equals(playType)){
			oneTicketMoney = new BigDecimal("3.0");
		}
		for(MatchBetPlayDTO matchBetPlayDto:param.getMatchBetPlays()){
			String betType = matchBetPlayDto.getBetType();
			String ticketData = matchBetPlayDto.getTicketData();	
			int betNum = BetUtils.getLettoBetNum(ticketData);
			int maxTime = BetUtils.maxTcBetTime(oneTicketMoney,betNum);
			int n = times/maxTime;
			int m = times%maxTime;
			if(n > 0) {
				for(int i=0; i< n; i++) {
					BigDecimal money = oneTicketMoney.multiply(new BigDecimal(maxTime*betNum));
					LotteryPrintDTO lotteryPrintDTO = new LotteryPrintDTO();
					lotteryPrintDTO.setBetType(betType);
					lotteryPrintDTO.setIssue(matchBetPlayDto.getPlayCode());
					lotteryPrintDTO.setMoney(money.doubleValue());
					lotteryPrintDTO.setPlayType(playType);
					lotteryPrintDTO.setStakes(ticketData);
					String ticketId = SNGenerator.nextSN(SNBusinessCodeEnum.TICKET_SN.getCode());
					lotteryPrintDTO.setTicketId(ticketId);
					lotteryPrintDTO.setTimes(maxTime);
					lotteryPrints.add(lotteryPrintDTO);
				}
			}
			if(m > 0) {
				BigDecimal money = oneTicketMoney.multiply(new BigDecimal(m*betNum));
				LotteryPrintDTO lotteryPrintDTO = new LotteryPrintDTO();
				lotteryPrintDTO.setBetType(betType);
				lotteryPrintDTO.setIssue(matchBetPlayDto.getPlayCode());
				lotteryPrintDTO.setMoney(money.doubleValue());
				lotteryPrintDTO.setPlayType(playType);
				lotteryPrintDTO.setStakes(ticketData);
				String ticketId = SNGenerator.nextSN(SNBusinessCodeEnum.TICKET_SN.getCode());
				lotteryPrintDTO.setTicketId(ticketId);
				lotteryPrintDTO.setTimes(m);
				lotteryPrints.add(lotteryPrintDTO);
			}
		}
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
     * 足球通过玩法code与投注内容，进行转换
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
     * 篮球通过玩法code与投注内容，进行转换
     * @param playCode
     * @param cathecticStr
     * @return
     */
    private String getBasketCathecticData(String playTypeStr, String cathecticStr) {
        int playType = Integer.parseInt(playTypeStr);
        String cathecticData = "";
        if (MatchBasketPlayTypeEnum.PLAY_TYPE_MNL.getcode() == playType || MatchBasketPlayTypeEnum.PLAY_TYPE_HDC.getcode() == playType) {
            if (!cathecticStr.equals("null")) {
                cathecticData = MatchBasketBallResultHDCEnum.getName(Integer.valueOf(cathecticStr));
            }
        } else if (MatchBasketPlayTypeEnum.PLAY_TYPE_HDC.getcode() == playType) {
            if (!cathecticStr.equals("null")) {
                cathecticData = MatchBasketBallResultHDCEnum.getName(Integer.valueOf(cathecticStr));
            }
        } else if (MatchBasketPlayTypeEnum.PLAY_TYPE_HILO.getcode() == playType) {
            if (!cathecticStr.equals("null")) {
                cathecticData = MatchBasketBallResultHILOEnum.getName(cathecticStr);
            }
        } else if (MatchBasketPlayTypeEnum.PLAY_TYPE_WNM.getcode() == playType) {
            if (!cathecticStr.equals("null")) {
                cathecticData = BasketBallHILOLeverlEnum.getName(cathecticStr);
            }
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
	 * 出票 版本2.0
	 */
	public void goPrintLotteryVersion2() {
		log.info("出票版本2.0出票开始");
		for(PrintComEnums printComEnums:PrintComEnums.values()){
			try{
				DlTicketChannel dlTicketChannel = printLotteryAdapter.selectChannelByChannelId(printComEnums);
				List<DlPrintLottery> lotteryLists = printLotteryAdapter.getLotteryList(printComEnums,PrintLotteryStatusEnum.INIT);
				log.info("渠道channelId={},channelName={},查询待出票状态个数={}",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName(),lotteryLists.size());
				while(!CollectionUtils.isEmpty(lotteryLists)){
					int endIndex = lotteryLists.size()>dlTicketChannel.getMaxNumBatchRequest()?dlTicketChannel.getMaxNumBatchRequest():lotteryLists.size();
					List<DlPrintLottery> subList = lotteryLists.subList(0, endIndex);
					ToStakeResponseDTO stakeResponseDto = printLotteryAdapter.toStake(printComEnums,subList,dlTicketChannel);
					if(stakeResponseDto==null){
						log.error("出票返回空，channelId={},channelName={}",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName());
					}else if(!stakeResponseDto.getRetSucc()){
						log.error("出票失败，channelId={},channelName={},errorCode={},errorMsg={}",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName(),
								stakeResponseDto.getRetCode(),stakeResponseDto.getRetDesc());
					}else if(stakeResponseDto.getRetSucc()){
						updatePrintLottery(stakeResponseDto);	
					}
					lotteryLists.removeAll(subList);
				}
			}catch(Exception e){
				log.error("投注接口 printChannelId={},printChannelName={}投注异常",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName(),e);
				continue;
			}
			log.info("渠道channelId={},channelName={}出票结束",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName());
		}
	}
	
	public void queryPrintLotteryVersion2(PrintComEnums printComEnums) {
			try{
				DlTicketChannel dlTicketChannel = printLotteryAdapter.selectChannelByChannelId(printComEnums);
				List<DlPrintLottery> lotteryLists = printLotteryAdapter.getLotteryList(printComEnums,PrintLotteryStatusEnum.DOING);
				log.info("渠道channelId={},channelName={},查询出票状态个数={}",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName(),lotteryLists.size());
				while(!CollectionUtils.isEmpty(lotteryLists)){
					int endIndex = lotteryLists.size()>dlTicketChannel.getMaxNumBatchRequest()?dlTicketChannel.getMaxNumBatchRequest():lotteryLists.size();
					List<DlPrintLottery> subList = lotteryLists.subList(0, endIndex);
					QueryStakeResponseDTO queryStakeResponseDTO = printLotteryAdapter.queryStake(printComEnums,subList,dlTicketChannel);
					if(queryStakeResponseDTO==null){
						log.error("出票查询返回空，channelId={},channelName={}",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName());
					}else if(!queryStakeResponseDTO.getQuerySuccess()){
						log.error("出票查询失败，channelId={},channelName={},errorCode={},errorMsg={}",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName(),
								queryStakeResponseDTO.getRetCode(),queryStakeResponseDTO.getRetDesc());
					}else if(queryStakeResponseDTO.getQuerySuccess()){
						List<QueryStakeOrderResponse> queryStakes = queryStakeResponseDTO.getOrders();
						List<DlPrintLottery> lotteryPrints = new ArrayList<>();
						for(QueryStakeOrderResponse stake: queryStakes) {
							if(stake==null||!stake.getQuerySuccess()){
								continue;
							}
							PrintLotteryStatusEnum statusEnum = stake.getStatusEnum();
							if(PrintLotteryStatusEnum.INIT==statusEnum){
								continue;
							}
							DlPrintLottery lotteryPrint = new DlPrintLottery();
							lotteryPrint.setTicketId(stake.getTicketId());
							lotteryPrint.setStatus(statusEnum.getStatus());
							lotteryPrint.setPlatformId(stake.getPlatformId());
							lotteryPrint.setPrintNo(stake.getPrintNo());
							lotteryPrint.setPrintSp(stake.getSp());
							lotteryPrint.setPrintStatus(stake.getPrintStatus());
							lotteryPrint.setPrintTime(stake.getPrintTimeDate());
							lotteryPrints.add(lotteryPrint);
						}
						if(!CollectionUtils.isEmpty(lotteryPrints)){							
							long start = System.currentTimeMillis();
							log.info("query stake betch Update dl_print_lottery start ={}ms,listSize={}",start,lotteryPrints.size());
							int updateRow= dlPrintLotteryMapper.beatchUpdateLotteryPrintByCallBack(lotteryPrints);
							long end = System.currentTimeMillis();
							log.info("query stake betch Update dl_print_lottery useTime ={}ms,updateRow={}",(end-start),updateRow);
//						for (DlPrintLottery print : lotteryPrints) {
//							dlPrintLotteryMapper.updateLotteryPrintByCallBack(print);
//						}
						}
					}
					lotteryLists.removeAll(subList);
				}
			}catch(Exception e){
				log.error("投注查询接口 printChannelId={},printChannelName={}投注查询异常",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName(),e);
			}
			log.info("渠道channelId={},channelName={},查询出票状态结束",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName());
	}
	
	public void rewardPrintLotteryVersion2() {
		log.info("出奖奖金查询版本2.0奖金查询开始");
		for(PrintComEnums printComEnums:PrintComEnums.values()){
			try{
				DlTicketChannel dlTicketChannel = printLotteryAdapter.selectChannelByChannelId(printComEnums);
				ThirdRewardStatusEnum thirdRewardStatusEnum = ThirdRewardStatusEnum.REWARD_INIT;
				if(PrintComEnums.CAIXIAOMI==printComEnums){
					thirdRewardStatusEnum= ThirdRewardStatusEnum.DOING;
				}
				List<DlPrintLottery> lotteryLists = printLotteryAdapter.getReWardLotteryList(printComEnums,thirdRewardStatusEnum);
				log.info("渠道channelId={},channelName={},查询第三方奖金个数={}",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName(),lotteryLists.size());
				if(CollectionUtils.isEmpty(lotteryLists)){
					continue ;
				}
				Map<String,DlPrintLottery> ticketIdPrintLottery= new HashMap<String, DlPrintLottery>();
				List<String> issueAndGameList = new ArrayList<String>();
				lotteryLists.forEach(print->{
					ticketIdPrintLottery.put(print.getTicketId(), print);
					String issueAndGame = print.getGame()+";"+print.getIssue();
					if(!issueAndGameList.contains(issueAndGame)){
						issueAndGameList.add(issueAndGame);
					}
				});
				if(Integer.valueOf(1).equals(printComEnums.getRewardType())){//按票计算奖金
					while(!CollectionUtils.isEmpty(lotteryLists)){
						int endIndex = lotteryLists.size()>dlTicketChannel.getMaxNumBatchRequest()?dlTicketChannel.getMaxNumBatchRequest():lotteryLists.size();
						List<DlPrintLottery> subList = lotteryLists.subList(0, endIndex);
						QueryRewardResponseDTO queryRewardResponseDTO = printLotteryAdapter.queryLotterysReward(printComEnums,subList,dlTicketChannel);
						if(!queryRewardResponseDTO.getQuerySuccess()){
							log.error("出票查询第三方奖金失败，channelId={},channelName={},errorCode={},errorMsg={}",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName(),
									queryRewardResponseDTO.getRetCode(),queryRewardResponseDTO.getRetDesc());
						}else if(queryRewardResponseDTO.getQuerySuccess()){
							updateLotterysReward(queryRewardResponseDTO,ticketIdPrintLottery);
						}
						lotteryLists.removeAll(subList);
					}
				}else if(Integer.valueOf(2).equals(printComEnums.getRewardType())){//按期次计算奖金
					for(String issue:issueAndGameList){
						QueryRewardResponseDTO queryRewardResponseDTO = printLotteryAdapter.queryLotterysRewardByIssue(printComEnums,issue,dlTicketChannel);
						if(!queryRewardResponseDTO.getQuerySuccess()){
							log.error("出票奖金查询失败，channelId={},channelName={},errorCode={},errorMsg={}",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName(),
									queryRewardResponseDTO.getRetCode(),queryRewardResponseDTO.getRetDesc());
						}else if(queryRewardResponseDTO.getQuerySuccess()){
							updateLotterysReward(queryRewardResponseDTO,ticketIdPrintLottery);
						}
					}
				}
			}catch(Exception e){
				log.error("出票奖金查询接口 printChannelId={},printChannelName={}出票奖金查询异常",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName(),e);
				continue;
			}
			log.info("渠道channelId={},channelName={},查询出票奖金结束",printComEnums.getPrintChannelId(),printComEnums.getPrintChannelName());
		}
	}
	/**
	 * 兑奖
	 * @param printComEnum
	 */
	public void toRewardPrintLotteryVersion2(PrintComEnums printComEnum) {
		DlTicketChannel dlTicketChannel = printLotteryAdapter.selectChannelByChannelId(printComEnum);
		List<DlPrintLottery> lotteryLists = printLotteryAdapter.getReWardLotteryList(printComEnum,ThirdRewardStatusEnum.REWARD_INIT);
		log.info("渠道channelId={},channelName={},兑奖票个数={}",printComEnum.getPrintChannelId(),printComEnum.getPrintChannelName(),lotteryLists.size());
		while(!CollectionUtils.isEmpty(lotteryLists)){			
			int endIndex = lotteryLists.size()>dlTicketChannel.getMaxNumBatchRequest()?dlTicketChannel.getMaxNumBatchRequest():lotteryLists.size();
			List<DlPrintLottery> subList = lotteryLists.subList(0, endIndex);
			ToRewardResponseDTO toRewardResponseDTO = printLotteryAdapter.toRewardByLottery(printComEnum,subList,dlTicketChannel);
			updateLotteryToRewardDoing(toRewardResponseDTO);
			lotteryLists.removeAll(subList);
		}
		log.info("渠道channelId={},channelName={}兑奖结束",printComEnum.getPrintChannelId(),printComEnum.getPrintChannelName());
	}
	
	/**
	 * 查询出奖信息，更新第三方出奖信息
	 * @param queryRewardResponseDTO
	 * @param ticketIdPrintLottery 
	 */
	private void updateLotterysReward(QueryRewardResponseDTO queryRewardResponseDTO, Map<String, DlPrintLottery> ticketIdPrintLottery) {
			List<QueryRewardOrderResponse> queryRewardOrders = queryRewardResponseDTO.getOrders();
			if(CollectionUtils.isEmpty(queryRewardOrders)){
				log.error("查询第三方出奖信息，查询成功但是orders is null");
				return;
			}
			for(QueryRewardOrderResponse reward: queryRewardOrders) {
				if(!reward.getQuerySuccess()){
					continue;
				}
				ThirdRewardStatusEnum thirdRewardStatusEnum = reward.getThirdRewardStatusEnum();
				if(ThirdRewardStatusEnum.REWARD_OVER==thirdRewardStatusEnum){
					String ticektId = reward.getTicketId();
					DlPrintLottery dlPrintLottery = ticketIdPrintLottery.get(ticektId);
					if(dlPrintLottery==null){
						log.error("ticketId={},更新出奖信息，对应的票不存在",ticektId);
						continue;
					}
					String game = dlPrintLottery.getGame();
					Integer printLotteryCom = dlPrintLottery.getPrintLotteryCom();
					BigDecimal thirdRewardMoney = BigDecimal.valueOf(reward.getPrizeMoney());
					if("T01".equals(game)){//大乐透更新订单开奖信息
						DlPrintLottery updateDlPrint = new DlPrintLottery();
						updateDlPrint.setThirdPartRewardMoney(thirdRewardMoney);
						updateDlPrint.setCompareStatus(ProjectConstant.FINISH_COMPARE);
						updateDlPrint.setRealRewardMoney(thirdRewardMoney.divide(new BigDecimal("100")).setScale(2));
						updateDlPrint.setTicketId(reward.getTicketId());
						log.info("更新第三方奖金信息 ticketId={},thirdRewardMoney={}",updateDlPrint.getTicketId(),updateDlPrint.getThirdPartRewardMoney());
						if(PrintComEnums.CAIXIAOMI.getPrintChannelId().equals(printLotteryCom)){							
							dlPrintLotteryMapper.updatePrintThirdRewardRewardStatus2To3AndPrintLottery(updateDlPrint);
						}else{
							dlPrintLotteryMapper.updatePrintThirdRewardRewardStatus1To3AndPrintLottery(updateDlPrint);
						}
					}else{
						DlPrintLottery updateDlPrint = new DlPrintLottery();
						updateDlPrint.setThirdPartRewardMoney(thirdRewardMoney);
						updateDlPrint.setTicketId(reward.getTicketId());
						log.info("更新第三方奖金信息 ticketId={},thirdRewardMoney={}",updateDlPrint.getTicketId(),updateDlPrint.getThirdPartRewardMoney());
						if(PrintComEnums.CAIXIAOMI.getPrintChannelId().equals(printLotteryCom)){
						    dlPrintLotteryMapper.updatePrintThirdRewardRewardStatus2To3(updateDlPrint);
						}else{
							dlPrintLotteryMapper.updatePrintThirdRewardRewardStatus1To3(updateDlPrint);
						}
					}
				}
			}
	}

	/**
	 * 根据查询结果进行查询出票
	 * @param stakeResponseDto
	 */
	private void updatePrintLottery(ToStakeResponseDTO stakeResponseDto) {
		if(null == stakeResponseDto||CollectionUtils.isEmpty(stakeResponseDto.getOrders())){
			log.error("出票后，更新出票数据，没有对应的数据,stakeResponseDto={}",stakeResponseDto==null?"":JSONHelper.bean2json(stakeResponseDto));
			return ;
		}
		List<DlPrintLottery> lotteryPrints = new LinkedList<DlPrintLottery>();
		for(ToStakeBackOrderDetail backOrderDetail : stakeResponseDto.getOrders()) {
			DlPrintLottery lotteryPrint = new DlPrintLottery();
			lotteryPrint.setTicketId(backOrderDetail.getTicketId());
			Integer errorCode = backOrderDetail.getErrorCode();
			if(backOrderDetail.getPrintLotteryDoing()){
				//出票中
				lotteryPrint.setStatus(3);
				lotteryPrints.add(lotteryPrint);
			}else if(!backOrderDetail.getPrintLotteryDoing()){
				lotteryPrint.setErrorCode(errorCode);
				//出票失败
				lotteryPrint.setStatus(2);
				lotteryPrint.setPrintTime(new Date());
				lotteryPrints.add(lotteryPrint);
			}
		}
		if(!CollectionUtils.isEmpty(lotteryPrints)){							
			long start = System.currentTimeMillis();
			log.info("toStake betch Update dl_print_lottery start ={}ms,listSize={}",start,lotteryPrints.size());
			int updateRow= dlPrintLotteryMapper.beatchUpdatePrintStatusByTicketId(lotteryPrints);
			long end = System.currentTimeMillis();
			log.info("toStake betch Update dl_print_lottery useTime ={}ms,updateRow={}",(end-start),updateRow);
		}
	}

	/**
	 * 新的根据出票路由选择后出票信息
	 * @param lotteryPrints
	 * @param orderSn
	 * @param printChannelInfo
	 */
	public void saveLotteryPrintInfo(List<LotteryPrintDTO> lotteryPrints,String orderSn,Integer lotteryClassifyId) {
		List<DlPrintLottery> printLotterysByOrderSn = dlPrintLotteryMapper.printLotterysByOrderSn(orderSn);
		if(CollectionUtils.isNotEmpty(printLotterysByOrderSn)) {
			log.info("订单orderSn={},已经出票",orderSn);
			return ;
		}
		List<DlPrintLottery> models = lotteryPrints.stream().map(dto->{
			DlPrintLottery lotteryPrint = new DlPrintLottery();
			if (lotteryClassifyId == 1 ) {
				lotteryPrint.setGame("T51");//足彩
			}else	if (lotteryClassifyId == 2 ) {
				lotteryPrint.setGame("2");//大乐透
			}else if (lotteryClassifyId == 3 ){
				lotteryPrint.setGame("T52");//蓝彩
			}else{
                lotteryPrint.setGame("");//其他彩种先空着
            }
			lotteryPrint.setMerchant("");
			lotteryPrint.setTicketId(dto.getTicketId());
			lotteryPrint.setAcceptTime(DateUtil.getCurrentTimeLong());
			lotteryPrint.setBetType(dto.getBetType());
			lotteryPrint.setMoney(BigDecimal.valueOf(dto.getMoney()*100));
			lotteryPrint.setIssue(dto.getIssue());
			lotteryPrint.setPrintSp(dto.getPrintSp());
			lotteryPrint.setPlayType(dto.getPlayType());
			lotteryPrint.setTimes(dto.getTimes());
			lotteryPrint.setStakes(dto.getStakes());
			lotteryPrint.setOrderSn(orderSn);
			lotteryPrint.setRealRewardMoney(BigDecimal.valueOf(0.00));
			lotteryPrint.setThirdPartRewardMoney(BigDecimal.valueOf(0.00));
			lotteryPrint.setCompareStatus("0");
			lotteryPrint.setComparedStakes("");
			lotteryPrint.setRewardStakes("");
			lotteryPrint.setStatus(1);
			lotteryPrint.setPrintStatus(16);
			lotteryPrint.setPrintLotteryCom(0);
			return lotteryPrint;
		}).collect(Collectors.toList());
		dlPrintLotteryMapper.batchInsertDlPrintLottery(models);
			
		//保存手工出票的信息
		List<String> orderSnList = models.stream().map(s->s.getOrderSn()).distinct().collect(Collectors.toList());
		Double totalMoney = models.stream().mapToDouble(s-> s.getMoney().doubleValue()).sum();
		List<Order> orderList = orderMapper.queryOrderListByOrderSns(orderSnList);
		log.info("该订单彩票总金额:"+totalMoney);
		List<DlArtifiPrintLottery> artifiPrintLotterys = orderList.stream().map(s->{
			DlArtifiPrintLottery dlArtifiPrintLottery = new DlArtifiPrintLottery();
			dlArtifiPrintLottery.setOrderSn(s.getOrderSn());
			dlArtifiPrintLottery.setLotteryClassifyId(s.getLotteryClassifyId());
			dlArtifiPrintLottery.setAddTime(DateUtil.getCurrentTimeLong());
			dlArtifiPrintLottery.setAppCodeName(s.getAppCodeName());
			dlArtifiPrintLottery.setStatisticsPaid(0);
			dlArtifiPrintLottery.setMoneyPaid(new BigDecimal(totalMoney));
			return dlArtifiPrintLottery;
		}).collect(Collectors.toList());

		dlArtifiPrintLotteryMapper.batchInsert(artifiPrintLotterys);
		log.info("订单orderSn={},入总队列",orderSn);
		return;
	}
	
	/**
	 * 更新兑奖中
	 * @param toRewardResponseDTO
	 */
	private void updateLotteryToRewardDoing(ToRewardResponseDTO toRewardResponseDTO) {
		List<ToRewardOrderResponse> toRewardOrderResponses = toRewardResponseDTO.getOrders();
		if(CollectionUtils.isEmpty(toRewardOrderResponses)){
			log.error("第三方兑奖，兑奖成功数据orders is null");
			return;
		}
		List<DlPrintLottery> lotteryPrints = new LinkedList<DlPrintLottery>();
		for(ToRewardOrderResponse response:toRewardOrderResponses){
			if(!response.getQuerySuccess()){
				continue;
			}
			DlPrintLottery lottery = new DlPrintLottery();
			lottery.setTicketId(response.getTicketId());
			lotteryPrints.add(lottery);
		}
		if(!CollectionUtils.isEmpty(lotteryPrints)){							
			long start = System.currentTimeMillis();
			log.info("toReward betch Update dl_print_lottery start ={}ms,listSize={}",start,lotteryPrints.size());
			int updateRow= dlPrintLotteryMapper.batchUpdateDlPrintLotteryTowardDoing(lotteryPrints);
			long end = System.currentTimeMillis();
			log.info("toReward betch Update dl_print_lottery useTime ={}ms,updateRow={}",(end-start),updateRow);
		}
	}

	
}
