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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSON;
import com.dl.base.enums.RespStatusEnum;
import com.dl.base.exception.ServiceException;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.OrderDetailMapper;
import com.dl.task.dao.OrderMapper;
import com.dl.task.dto.OrderDTO;
import com.dl.task.dto.OrderWithUserDTO;
import com.dl.task.dto.UserIdAndRewardDTO;
import com.dl.task.model.Order;
import com.dl.task.param.OrderSnParam;
import com.dl.task.param.UpdateOrderInfoParam;
import com.dl.task.param.UserIdAndRewardListParam;

@Slf4j
@Service
public class OrderService extends AbstractService<Order> {

	@Resource
	private OrderMapper orderMapper;

	@Resource
	private OrderDetailMapper orderDetailMapper;
	
	@Resource
	private	UserAccountService userAccountService;

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
	
	/**
	 * 根据订单编号查询订单数据
	 * 
	 * @param snParam
	 * @return
	 */
	public OrderDTO getOrderInfoByOrderSn(OrderSnParam snParam) {
		Order order = new Order();
		order.setOrderSn(snParam.getOrderSn());
		order = orderMapper.selectOne(order);
		OrderDTO orderDTO = new OrderDTO();
		if (null == order) {
			return orderDTO;
		}
		try {
			BeanUtils.copyProperties(orderDTO, order);
		} catch (IllegalAccessException | InvocationTargetException e) {
			log.error("订单id：" + order.getOrderId() + "，查询订单失败");
			e.printStackTrace();
			throw new ServiceException(RespStatusEnum.FAIL.getCode(), "订单列表查询失败");
		}
		return orderDTO;
	}

	public void addRewardMoneyToUsers() {
		List<OrderWithUserDTO> orderWithUserDTOs = orderMapper.selectOpenedAllRewardOrderList();
		log.info("派奖已中奖的用户数据：code=" + orderWithUserDTOs.size());
		if (CollectionUtils.isNotEmpty(orderWithUserDTOs)) {
			log.info("需要派奖的数据:" + orderWithUserDTOs.size());
			List<UserIdAndRewardDTO> userIdAndRewardDTOs = new LinkedList<UserIdAndRewardDTO>();
			for (OrderWithUserDTO orderWithUserDTO : orderWithUserDTOs) {
				UserIdAndRewardDTO userIdAndRewardDTO = new UserIdAndRewardDTO();
				userIdAndRewardDTO.setUserId(orderWithUserDTO.getUserId());
				userIdAndRewardDTO.setOrderSn(orderWithUserDTO.getOrderSn());
				userIdAndRewardDTO.setReward(orderWithUserDTO.getRealRewardMoney());
				int betTime = orderWithUserDTO.getBetTime();
				userIdAndRewardDTO.setBetMoney(orderWithUserDTO.getBetMoney());
				userIdAndRewardDTO.setBetTime(DateUtil.getTimeString(betTime, DateUtil.datetimeFormat));
				userIdAndRewardDTOs.add(userIdAndRewardDTO);
			}
			userAccountService.batchUpdateUserAccount(userIdAndRewardDTOs,ProjectConstant.REWARD_AUTO);
		}
	}
	
}
