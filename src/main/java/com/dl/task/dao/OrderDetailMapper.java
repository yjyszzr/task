package com.dl.task.dao;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.OrderDetail;
import com.dl.task.param.SupperLottoOrderDetailParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderDetailMapper extends Mapper<OrderDetail> {
	
	//获取 没有赛事结果 的订单详情
	public List<OrderDetail> unMatchResultOrderDetails();
	
	//获取 没有赛事结果 的篮彩订单详情
	public List<OrderDetail> unBasketMatchResultOrderDetails();

    //根据多个订单号获取 的多个篮彩订单详情
    public List<OrderDetail> getOrderDetailsByOrderSns(@Param("orderSns") List<String> orderSnList);
	
	//更新订单详情的比赛结果
	public int updateMatchResult(OrderDetail detail);
	//获取订单下的所有详情记录
	public List<OrderDetail> queryListByOrderSn(@Param("orderSn")String orderSn);
	//更新赔率信息
	public int updateTicketData(OrderDetail orderDetail);
	public int beatchUpdateMatchResult(@Param("issue") String t01Issue, @Param("matchResult") String matchResult);

	public void updateOrderDetailInfoForSupperLotto(@Param("supperLottoOrderDetailParam")  SupperLottoOrderDetailParam supperLottoOrderDetailParam);
}