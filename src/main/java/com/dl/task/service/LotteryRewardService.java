package com.dl.task.service;

import com.dl.base.util.DateUtil;
import com.dl.lottery.api.ILotteryPrintService;
import com.dl.lottery.param.NotifyParam;
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.dao.LotteryRewardMapper;
import com.dl.task.dao.OrderMapper;
import com.dl.task.dao2.LotteryMatchMapper;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.Order;
import com.dl.task.param.OrderDataParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class LotteryRewardService {
	
	@Resource
	private LotteryMatchMapper lotteryMatchMapper;
	
	@Resource
	private LotteryRewardMapper lotteryRewardMapper;
	
	@Resource
	private DlPrintLotteryMapper dlPrintLotteryMapper;
	
	@Resource
	private	OrderMapper orderMapper;

	@Resource
	private ILotteryPrintService iLotteryPrintService;

	
	/**
	 * 更新待开奖的订单状态及中奖金额
	 * @param issue
	 */
	public String updateOrderAfterOpenReward() {
		//查询订单状态是待开奖的，查询是否每笔订单锁包含的彩票都已经比对完成
		List<String> orderSnList = orderMapper.queryOrderSnListUnOpenReward();
		
		log.info("updateOrderAfterOpenReward待开奖数据： size="+orderSnList.size());
		if(CollectionUtils.isEmpty(orderSnList)) {
			return "待开奖数据： size="+orderSnList.size();
		}
		while(orderSnList.size() > 0) {
			int num = orderSnList.size()>20?20:orderSnList.size();
			List<String> subList = orderSnList.subList(0, num);
			List<DlPrintLottery> dlOrderDataDTOs = dlPrintLotteryMapper.getPrintLotteryListByGoOpenRewardOrderSns(subList);
//			log.info("获取可开奖彩票信息："+dlOrderDataDTOs.size());
			if(CollectionUtils.isNotEmpty(dlOrderDataDTOs)) {
				log.info("updateOrderAfterOpenReward--1");
				Map<String, Double> map = new HashMap<String, Double>();
				Set<String> unOrderSns = new HashSet<String>();
				for(DlPrintLottery dto: dlOrderDataDTOs) {
					log.info("updateOrderAfterOpenReward--2");
					String orderSn = dto.getOrderSn();
					String compareStatus = dto.getCompareStatus();
					Integer thirdRewardStatus = dto.getThirdRewardStatus();
					Boolean caiXiaoMiIsNotRewardEnd = StringUtils.isBlank(compareStatus) || !"1".equals(compareStatus);
					String game = dto.getGame();
					if("T01".equals(game)){
						caiXiaoMiIsNotRewardEnd = caiXiaoMiIsNotRewardEnd||!Integer.valueOf(3).equals(thirdRewardStatus);
					}
					if(caiXiaoMiIsNotRewardEnd) {
						unOrderSns.add(orderSn);
					}
					if(unOrderSns.contains(orderSn)) {
						map.remove(orderSn);
						continue;
					}
					Double double1 = map.get(orderSn);
					BigDecimal realRewardMoney = dto.getRealRewardMoney();
					double realReward = 0;
					if(realRewardMoney!=null){
						realReward = realRewardMoney.doubleValue();
					}
					double1 = double1==null?realReward:(double1+realReward);
					map.put(orderSn, double1);
				}
				
//				log.info("*********8可开奖订单及资金数："+map.size());
				List<OrderDataParam> dtos = new ArrayList<OrderDataParam>(map.size());
				for(String orderSn: map.keySet()) {
					log.info("updateOrderAfterOpenReward--3");
					OrderDataParam dlOrderDataDTO = new OrderDataParam();
					dlOrderDataDTO.setOrderSn(orderSn);
					BigDecimal realReward = BigDecimal.valueOf(map.get(orderSn));
					dlOrderDataDTO.setRealRewardMoney(realReward);
					
					if(realReward.compareTo(BigDecimal.ZERO) == 0) {//未中奖
						dlOrderDataDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_NOT);
					}else if(realReward.compareTo(BigDecimal.ZERO) > 0) {//已中奖
						dlOrderDataDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_ALREADY);
					}
					
					if(realReward.compareTo(BigDecimal.ZERO) < 0) {//中奖金额为负数，过滤掉
						continue;
					}
					
					dtos.add(dlOrderDataDTO);

				}
//				log.info("%%%%%%准备执行开奖订单数："+dtos.size());
				if(dtos.size() > 0) {
					log.info("updateOrderAfterOpenReward--4");
					int n = 0;
					for (OrderDataParam orderDataParam : dtos) {
						Order updateOrder = new Order();
						updateOrder.setOrderSn(orderDataParam.getOrderSn());
						updateOrder.setWinningMoney(orderDataParam.getRealRewardMoney());
						updateOrder.setOrderStatus(orderDataParam.getOrderStatus());
						updateOrder.setAwardTime(DateUtil.getCurrentTimeLong());
						n += orderMapper.updateWiningMoney(updateOrder);

						//若是商户订单,主动通知商户中奖信息
						Order order = orderMapper.getOrderInfoByOrderSn(orderDataParam.getOrderSn());
						log.info("updateOrderAfterOpenReward:order="+order.getMerchantOrderSn());
						if(!StringUtils.isEmpty(order.getMerchantOrderSn())){
							log.info("updateOrderAfterOpenReward&&&&&&商户订单,开始通知商户是否中奖&&&&&&&&&");
							String merchantOrderSn = order.getMerchantOrderSn();
							NotifyParam qParam = new NotifyParam();
							qParam.setMerchantOrderSn(merchantOrderSn);
							qParam.setNotifyUrl("http://app.shoumiba.cn/api/callback/ticket/status");
							iLotteryPrintService.notifyPrintResultToMerchant(qParam);
						}
					}
					log.info("更新订单中奖状态和中奖金额updateOrderInfoByExchangeReward param size="+ n);

				}
			}
			orderSnList.removeAll(subList);
		}
		return "success";
	}

}
