package com.dl.task.dao;

import com.dl.base.mapper.Mapper;
import com.dl.task.dto.OrderInfoListDTO;
import com.dl.task.dto.OrderWithUserDTO;
import com.dl.task.model.*;
import com.dl.task.param.UpdateOrderInfoParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper extends Mapper<Order> {
	
	//查询未开奖的订单
	public List<String> queryOrderSnListUnOpenReward();
	 //更新中奖金额
	public int updateWiningMoney(Order order);
	 //根据期次获取中奖用户及奖金
	public List<OrderWithUserDTO> selectOpenedAllRewardOrderList();
	//待出票订单列表
	public List<Order> ordersListNoFinishAllPrintLottery();
	//手工出票，支付完成后 订单状态为 待开奖
	public int updateOrderStatus0To3(@Param("orderSn")String orderSn);
	//订单更新为待开奖
	public int updateOrderStatus1To3(Order order);
	//订单更新为出票失败
	public int updateOrderStatus1To2(Order order);
	//
	public ChannelOperationLog getChannelOperationByOrderSn(@Param("orderSn") String orderSn);

	public List<Order> queryOrderListByOrderSns(@Param("orderSnList") List<String> orderSnList);

	
	//---------------------------------
	/**
	 * 保存订单数据
	 * 
	 * @param order
	 */
	public void insertOrder(Order order);

	/**
	 * 根据订单状态查询订单列表
	 * 
	 * @param statusList
	 * @return
	 */
	public List<OrderInfoListDTO> getOrderInfoList(@Param("statusList") List<Integer> statusList, @Param("userId") Integer userId, @Param("lotteryClassifyId") Integer lotteryClassifyId);

	/**
	 * 支付成功修改订单信息
	 * 
	 * @param param
	 */
	public int updateOrderInfo(UpdateOrderInfoParam param);

	/**
	 * 通过orderSn读取order
	 * 
	 * @param orderSn
	 * @return
	 */
	public Order getOrderInfoByOrderSn(@Param("orderSn") String orderSn);

	

	
	/**
	 * 查询符合条件的订单号
	 * 
	 * @return
	 */
	public List<String> queryOrderSnListByStatus(Order order);

	/**
	 * 查询符合条件的订单集合
	 * 
	 * @return
	 */
	public List<Order> queryOrderListBySelective(@Param("nowTime") Integer nowTime);
	
	/**
	 * 查询距离下单时间超过20min的订单集合
	 * 
	 * @return
	 */
	public List<Order> queryOrderListByOrder20minOut(@Param("nowTime") Integer nowTime,@Param("expireTime") Integer expireTime);

	int updateOrderStatus(Order order);
	
	int updateOrderStatusVerified(@Param("orderSn") String orderSn);

	/**
	 * 更新出票相关信息
	 * 
	 * @param order
	 */
	public int updateOrderTicketInfo(Order order);

	

	public List<DlChannelConsumer> selectConsumers(@Param("userIds") List<Integer> userIds);

	public List<User> findAllUser(@Param("userIds") List<Integer> userIds);

	public List<DlChannelDistributor> channelDistributorList(@Param("channelDistributorIds") List<Integer> channelDistributorIds);

	public void saveChannelOperation(ChannelOperationLog channelOperationLog);
	
	public int updateOrderStatus6To5(@Param("orderSn")String orderSn);
	
	public int updateOrderStatus6To7(@Param("orderSn")String orderSn);
	
	public int updateOrderStatus0To8(@Param("orderSn")String orderSn, @Param("payTime")Integer payTime);
	
	public int batchUpdateOrderStatus0To8(@Param("orderSnList")List<String> orderSnList);
	
	public List<Order> selectPaySuccessOrdersList();
	public int updateOrderStatus0To1(@Param("orderSn")String orderSn);
	public List<Order> selectPayFailOrdersList();
	
	public void updateStatisticsRewardStatusTo0(@Param("orderSn")String orderSn);
	
}