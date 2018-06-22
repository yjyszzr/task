package com.dl.task.service;

import io.jsonwebtoken.lang.Collections;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSON;
import com.dl.base.constant.CommonConstants;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.lottery.param.PrintLotteryStatusByOrderSnParam;
import com.dl.member.param.AddMessageParam;
import com.dl.member.param.MessageAddParam;
import com.dl.shop.payment.param.RollbackOrderAmountParam;
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.dao.OrderDetailMapper;
import com.dl.task.dao.OrderMapper;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.Order;
import com.dl.task.param.OrderDataParam;
import com.dl.task.param.UpdateOrderInfoParam;

@Slf4j
@Service
public class OrderService extends AbstractService<Order> {

	@Resource
	private OrderMapper orderMapper;

	@Resource
	private OrderDetailMapper orderDetailMapper;

	@Resource
	private DlPrintLotteryMapper dlPrintLotteryMapper;


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
		userMessageService.add(addParam);
	}

}
