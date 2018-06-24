package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.LotteryClassifyTemp;
import com.dl.task.model.LotteryPlayClassifyTemp;
import com.dl.task.model.OrderDetail;
import com.dl.task.model.PlayTypeName;

public interface OrderDetailMapper extends Mapper<OrderDetail> {
	
	//获取 没有赛事结果 的订单详情
	public List<OrderDetail> unMatchResultOrderDetails();
	//更新订单详情的比赛结果
	public int updateMatchResult(OrderDetail detail);
	//获取订单下的所有详情记录
	public List<OrderDetail> queryListByOrderSn(@Param("orderSn")String orderSn);
	//更新赔率信息
	public int updateTicketData(OrderDetail orderDetail);
}