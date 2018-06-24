package com.dl.task.service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.dl.base.constant.CommonConstants;
import com.dl.base.enums.MatchPlayTypeEnum;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.DlLeagueMatchResultMapper;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.dao.LotteryMatchMapper;
import com.dl.task.dao.OrderDetailMapper;
import com.dl.task.dao.OrderMapper;
import com.dl.task.dto.MatchResultDTO;
import com.dl.task.model.DlLeagueMatchResult;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.Order;
import com.dl.task.model.OrderDetail;
import com.dl.task.param.AddMessageParam;
import com.dl.task.param.GetCancelMatchesParam;
import com.dl.task.param.MessageAddParam;
import com.dl.task.param.QueryMatchResultsByPlayCodesParam;
import com.dl.task.param.UpdateOrderInfoParam;

import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService extends AbstractService<Order> {

	@Resource
	private OrderMapper orderMapper;

	@Resource
	private OrderDetailMapper orderDetailMapper;

	@Resource
	private DlPrintLotteryMapper dlPrintLotteryMapper;
	
	@Resource
	private LotteryMatchMapper lotteryMatchMapper;
	
	@Resource
	private DlLeagueMatchResultMapper dlLeagueMatchResultMapper;


	/**
	 * 更新订单状态
	 * 
	 * @param param
	 * @return
	 */
	@Transactional
	public BaseResult<String> updateOrderInfoStatus(UpdateOrderInfoParam param) {
		log.info("-----------%%%--------更新订单状态:" + JSON.toJSONString(param));
		Order order = new Order();
		order.setOrderSn(param.getOrderSn());
		order.setOrderStatus(param.getOrderStatus());
		order.setPayStatus(param.getPayStatus());
		order.setPayTime(param.getPayTime());
		int rst = orderMapper.updateOrderStatus(order);
		log.info("-----------%%%%-----------更新订单状态结果:" + rst);
		return ResultGenerator.genSuccessResult("订单支付数据更新成功");
	}

	//更新待出票订单状态及出票赔率信息
	public void refreshOrderPrintStatus() {
		List<Order> orders = orderMapper.ordersListGoPrintLottery();
		if (CollectionUtils.isNotEmpty(orders)) {
			log.info("refreshOrderPrintStatus需要获取出票状态的订单数：" + orders.size());
			int n = 0;
			List<Order> rollOrders = new ArrayList<Order>(orders.size());
			for (Order order : orders) {
				List<DlPrintLottery> printLotterys = dlPrintLotteryMapper.printLotterysByOrderSn(order.getOrderSn());
				if(Collections.isEmpty(printLotterys)) {
					continue;
				}
				Set<Integer> lotteryStatus = printLotterys.stream().map(item->item.getStatus()).collect(Collectors.toSet());
				if(lotteryStatus.contains(0) || lotteryStatus.contains(3)) {//存在有未出票或出票中的
					continue;
				}
				double refundMoney = 0.0;
				for(DlPrintLottery item: printLotterys) {
					if(2 == item.getStatus()) {
						refundMoney += item.getMoney().doubleValue();
					}
				}
				if(refundMoney > 0) {//退款
					
					rollOrders.add(order);
				}
				if(lotteryStatus.contains(1)) {//存在出票成功的
					order.setPrintLotteryStatus(4);
					if(refundMoney > 0) {
						order.setPrintLotteryStatus(2);
					}
					order.setPrintLotteryRefundAmount(BigDecimal.valueOf(refundMoney));
					int rst = orderMapper.updateOrderStatus1To3(order);
					n += rst;
				}else {//全为出票失败的
					order.setPrintLotteryStatus(3);
					order.setPrintLotteryRefundAmount(BigDecimal.valueOf(refundMoney));
					int rst = orderMapper.updateOrderStatus1To2(order);
					n += rst;
				}
			}
			log.info("refreshOrderPrintStatus实际更新状态的订单数：" + n);
			this.goLotteryMessage(rollOrders);
		}
	}
	@Async
	private void goLotteryMessage(List<Order> orders) {
		AddMessageParam addParam = new AddMessageParam();
		List<MessageAddParam> params = new ArrayList<MessageAddParam>(orders.size());
		for (Order order : orders) {
			if (2 != order.getOrderStatus()) {
				continue;
			}
			// 消息
			MessageAddParam messageAddParam = new MessageAddParam();
			messageAddParam.setTitle(CommonConstants.FORMAT_PRINTLOTTERY_TITLE);
			messageAddParam.setContent(CommonConstants.FORMAT_PRINTLOTTERY_CONTENT);
			messageAddParam.setContentDesc(CommonConstants.FORMAT_PRINTLOTTERY_CONTENT_DESC);
			messageAddParam.setSender(-1);
			messageAddParam.setMsgType(0);
			messageAddParam.setReceiver(order.getUserId());
			messageAddParam.setReceiveMobile("");
			messageAddParam.setObjectType(3);
			messageAddParam.setMsgUrl("");
			messageAddParam.setSendTime(DateUtil.getCurrentTimeLong());
			String ticketAmount = order.getTicketAmount().toString();
			Integer addTime = order.getAddTime();
			LocalDateTime loclaTime = LocalDateTime.ofEpochSecond(addTime, 0, ZoneOffset.of("+08:00"));
			String format = loclaTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:dd"));
			messageAddParam.setMsgDesc(MessageFormat.format(CommonConstants.FORMAT_PRINTLOTTERY_MSG_DESC, ticketAmount, format));
			params.add(messageAddParam);
		}
		addParam.setParams(params);
		//userMessageService.add(addParam);
	}
	//更新订单的比赛结果
	public void updateOrderMatchResult() {
		List<OrderDetail> orderDetails = orderDetailMapper.unMatchResultOrderDetails();
		if (CollectionUtils.isEmpty(orderDetails)) {
			return;
		}
		Set<String> playCodesSet = orderDetails.stream().map(detail -> detail.getIssue()).collect(Collectors.toSet());
		List<String> playCodes = new ArrayList<String>(playCodesSet.size());
		playCodes.addAll(playCodesSet);
		log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size());
		List<String> cancelMatches = lotteryMatchMapper.getCancelMatches(playCodes);
		List<DlLeagueMatchResult> matchResults = dlLeagueMatchResultMapper.queryMatchResultsByPlayCodes(playCodes);
		if (CollectionUtils.isEmpty(matchResults) && CollectionUtils.isEmpty(cancelMatches)) {
			log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size() + " 没有获取到相应的赛事结果信息及没有取消赛事");
			return;
		}
		log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size() + " 获取到相应的赛事结果信息数：" + matchResults.size() + " 取消赛事数：" + cancelMatches.size());
		Map<String, List<OrderDetail>> detailMap = new HashMap<String, List<OrderDetail>>();
		List<OrderDetail> cancelList = new ArrayList<OrderDetail>(orderDetails.size());
		for (OrderDetail orderDetail : orderDetails) {
			String playCode = orderDetail.getIssue();
			if (cancelMatches.contains(playCode)) {
				orderDetail.setMatchResult(ProjectConstant.ORDER_MATCH_RESULT_CANCEL);
				cancelList.add(orderDetail);
			} else {
				List<OrderDetail> list = detailMap.get(playCode);
				if (list == null) {
					list = new ArrayList<OrderDetail>();
					detailMap.put(playCode, list);
				}
				list.add(orderDetail);
			}
		}
		log.info("取消赛事对应订单详情数：cancelList。si'ze" + cancelList.size() + "  detailMap.size=" + detailMap.size());
		Map<String, List<DlLeagueMatchResult>> resultMap = new HashMap<String, List<DlLeagueMatchResult>>();
		if (CollectionUtils.isNotEmpty(matchResults)) {
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
		log.info("resultMap size=" + resultMap.size());
		List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>(orderDetails.size());
		for (String playCode : resultMap.keySet()) {
			List<DlLeagueMatchResult> resultDTOs = resultMap.get(playCode);
			List<OrderDetail> details = detailMap.get(playCode);
			for (OrderDetail orderDetail : details) {
				String ticketDataStr = orderDetail.getTicketData();
				String[] split = ticketDataStr.split(";");
				OrderDetail od = new OrderDetail();
				od.setOrderDetailId(orderDetail.getOrderDetailId());
				StringBuffer sbuf = new StringBuffer();
				for (String ticketData : split) {
					if (StringUtils.isBlank(ticketData) || !ticketData.contains("|")) {
						continue;
					}
					Integer playType = Integer.valueOf(ticketData.substring(0, ticketData.indexOf("|")));
					if (playType.equals(MatchPlayTypeEnum.PLAY_TYPE_TSO.getcode())) {
						String hhadRst = null;
						String hadRst = null;
						for (DlLeagueMatchResult dto : resultDTOs) {
							if (MatchPlayTypeEnum.PLAY_TYPE_HHAD.getcode() == dto.getPlayType().intValue()) {
								String cellCode = dto.getCellCode();
								if (cellCode.equals("3")) {
									hhadRst = "32";
								} else if (cellCode.equals("0")) {
									hhadRst = "33";
								} else if (cellCode.equals("0")) {
									hhadRst = "32,33";
								}
							} else if (MatchPlayTypeEnum.PLAY_TYPE_HAD.getcode() == dto.getPlayType().intValue()) {
								String cellCode = dto.getCellCode();
								if (cellCode.equals("3")) {
									hadRst = "31";
								} else if (cellCode.equals("0")) {
									hadRst = "30";
								}
							}
						}
						String cellCode = hhadRst;
						if (hadRst != null) {
							if (cellCode == null) {
								cellCode = hadRst;
							} else if (cellCode != null) {
								cellCode = cellCode + "," + hadRst;
							}
						}
						if (cellCode != null) {
							sbuf.append("07|").append(playCode).append("|").append(cellCode).append(";");
						}
					} else {
						for (DlLeagueMatchResult dto : resultDTOs) {
							if (playType.equals(dto.getPlayType())) {
								sbuf.append("0").append(dto.getPlayType()).append("|").append(playCode).append("|").append(dto.getCellCode()).append(";");
							}
						}
					}
				}
				if (sbuf.length() > 0) {
					od.setMatchResult(sbuf.substring(0, sbuf.length() - 1));
					orderDetailList.add(od);
				}
			}
		}
		log.info("updateOrderMatchResult 准备去执行数据库更新操作：size=" + orderDetailList.size());
		for(OrderDetail detail: orderDetailList) {
			orderDetailMapper.updateMatchResult(detail);
		}
		log.info("updateOrderMatchResult 准备去执行数据库更新取消赛事结果操作：size=" + cancelList.size());
		for(OrderDetail detail: cancelList) {
			orderDetailMapper.updateMatchResult(detail);
		}
	}

}
