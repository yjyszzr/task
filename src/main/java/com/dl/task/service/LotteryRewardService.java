package com.dl.task.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.dao.LotteryMatchMapper;
import com.dl.task.dao.LotteryPrintMapper;
import com.dl.task.dao.LotteryRewardMapper;
import com.dl.task.dao.OrderMapper;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.LotteryPrint;
import com.dl.task.model.LotteryReward;
import com.dl.task.model.Order;
import com.dl.task.param.LotteryPrintMoneyParam;
import com.dl.task.param.OrderDataParam;

import lombok.extern.slf4j.Slf4j;

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

	
	/**
	 * 根据开奖期次更新订单的状态，中奖金额 等
	 * @param issue
	 */
	public String updateOrderAfterOpenReward() {
		//查询订单状态是待开奖的，查询是否每笔订单锁包含的彩票都已经比对完成
		List<String> orderSnList = orderMapper.queryOrderSnListUnOpenReward();
		
		log.info("待开奖数据： size="+orderSnList.size());
		if(CollectionUtils.isEmpty(orderSnList)) {
			return "待开奖数据： size="+orderSnList.size();
		}
		
		while(orderSnList.size() > 0) {
			int num = orderSnList.size()>20?20:orderSnList.size();
			List<String> subList = orderSnList.subList(0, num);
			List<DlPrintLottery> dlOrderDataDTOs = dlPrintLotteryMapper.getPrintLotteryListByOrderSns(subList);
			log.info("获取可开奖彩票信息："+dlOrderDataDTOs.size());
			if(CollectionUtils.isNotEmpty(dlOrderDataDTOs)) {
				Map<String, Double> map = new HashMap<String, Double>();
				Set<String> unOrderSns = new HashSet<String>();
				List<DlPrintLottery> errorPrints = new ArrayList<DlPrintLottery>(0);
				for(DlPrintLottery dto: dlOrderDataDTOs) {
					Integer status = dto.getStatus();
					if(2==status) {
						continue;
					}
					int printStatus = dto.getPrintStatus();
					String printSp = dto.getPrintSp();
					if(printStatus == ProjectConstant.PRINT_STATUS_SUCCESS || StringUtils.isNotBlank(printSp)) {//出票成功
						String orderSn = dto.getOrderSn();
						String compareStatus = dto.getCompareStatus();
						if(StringUtils.isBlank(compareStatus) || !"1".equals(compareStatus)) {
							unOrderSns.add(orderSn);
						}
						if(unOrderSns.contains(orderSn)) {
							map.remove(orderSn);
							continue;
						}
						Double double1 = map.get(orderSn);
						BigDecimal realRewardMoney = dto.getRealRewardMoney();
						double realReward = realRewardMoney == null?0:realRewardMoney.doubleValue();
						double1 = double1==null?realReward:(double1+realReward);
						map.put(orderSn, double1);
					}else if(1==status && printStatus == ProjectConstant.PRINT_STATUS_FAIL){
						errorPrints.add(dto);
					}
				}
				
				log.info("*********8可开奖订单及资金数："+map.size());
//				LotteryPrintMoneyParam lotteryPrintMoneyDTO = new LotteryPrintMoneyParam();
				List<OrderDataParam> dtos = new ArrayList<OrderDataParam>(map.size());
				for(String orderSn: map.keySet()) {
					OrderDataParam dlOrderDataDTO = new OrderDataParam();
					dlOrderDataDTO.setOrderSn(orderSn);
					BigDecimal realReward = BigDecimal.valueOf(map.get(orderSn));
					dlOrderDataDTO.setRealRewardMoney(realReward);
					
					if(realReward.compareTo(BigDecimal.ZERO) == 0) {//未中奖
						dlOrderDataDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_NOT);
					}else if(realReward.compareTo(BigDecimal.ZERO) > 0) {//已中奖
						dlOrderDataDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_REWARDING);
					}
					
					if(realReward.compareTo(BigDecimal.ZERO) < 0) {//中奖金额为负数，过滤掉
						continue;
					}
					
					dtos.add(dlOrderDataDTO);
				}
				log.info("%%%%%%准备执行开奖订单数："+dtos.size());
				if(dtos.size() > 0) {
					int n = 0;
					for (OrderDataParam orderDataParam : dtos) {
						Order updateOrder = new Order();
						updateOrder.setOrderSn(orderDataParam.getOrderSn());
						updateOrder.setWinningMoney(orderDataParam.getRealRewardMoney());
						updateOrder.setOrderStatus(orderDataParam.getOrderStatus());
						updateOrder.setAwardTime(DateUtil.getCurrentTimeLong());
						n += orderMapper.updateWiningMoney(updateOrder);
					}
					log.info("更新订单中奖状态和中奖金额updateOrderInfoByExchangeReward param size="+ n);

				}
				if(errorPrints.size() > 0) {
					for(DlPrintLottery lotteryPrint:errorPrints) {
						dlPrintLotteryMapper.updatePrintLotteryFailStatus(lotteryPrint);
					}
				}
			}
			orderSnList.removeAll(subList);
		}
		return "success";
	}

}