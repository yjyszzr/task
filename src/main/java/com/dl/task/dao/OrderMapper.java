package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.dto.OrderInfoListDTO;
import com.dl.task.dto.OrderWithUserDTO;
import com.dl.task.model.ChannelOperationLog;
import com.dl.task.model.DlChannelConsumer;
import com.dl.task.model.DlChannelDistributor;
import com.dl.task.model.Order;
import com.dl.task.model.User;
import com.dl.task.param.UpdateOrderInfoParam;

public interface OrderMapper extends Mapper<Order> {
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
	 * 根据期次获取中奖用户及奖金
	 * 
	 * @param issue
	 * @return
	 */
	public List<OrderWithUserDTO> selectOpenedAllRewardOrderList();

	/**
	 * 待出票订单列表
	 * 
	 * @return
	 */
	public List<Order> ordersListGoPrintLottery();

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

	int updateOrderStatus(Order order);

	/**
	 * 更新出票相关信息
	 * 
	 * @param order
	 */
	public int updateOrderTicketInfo(Order order);

	/**
	 * 更新中奖金额
	 * 
	 * @param order
	 * @return
	 */
	public int updateWiningMoney(Order order);

	public List<DlChannelConsumer> selectConsumers(@Param("userIds") List<Integer> userIds);

	public List<User> findAllUser(@Param("userIds") List<Integer> userIds);

	public List<DlChannelDistributor> channelDistributorList(@Param("channelDistributorIds") List<Integer> channelDistributorIds);

	public void saveChannelOperation(ChannelOperationLog channelOperationLog);
}