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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSON;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.task.dao.OrderDetailMapper;
import com.dl.task.dao.OrderMapper;
import com.dl.task.model.Order;
import com.dl.task.param.UpdateOrderInfoParam;

@Slf4j
@Service
public class OrderService extends AbstractService<Order> {

	@Resource
	private OrderMapper orderMapper;

	@Resource
	private OrderDetailMapper orderDetailMapper;

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


}
