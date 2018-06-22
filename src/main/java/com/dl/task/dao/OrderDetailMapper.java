package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.LotteryClassifyTemp;
import com.dl.task.model.LotteryPlayClassifyTemp;
import com.dl.task.model.OrderDetail;
import com.dl.task.model.PlayTypeName;

public interface OrderDetailMapper extends Mapper<OrderDetail> {
	
	/**
	 * 
	 * @param orderId
	 * @param userId
	 * @return
	 */
	public List<OrderDetail> selectByOrderId(@Param("orderId")Integer orderId, @Param("userId")Integer userId);
	
	
	/**
	 * 查询用户某天所下的订单中包含比赛id集合
	 * @param orderId
	 * @param userId
	 * @return
	 */
	public List<String> selectMatchIdsInSomeDayOrder(@Param("dateStr") String dateStr, @Param("userId")Integer userId);
	


	/**
	 * 获取 没有赛事结果 的订单详情
	 * @return
	 */
	public List<OrderDetail> unMatchResultOrderDetails();
	
	/**
	 * 获取玩法名称
	 * @param lotteryClassifyId
	 * @return
	 */
	public List<PlayTypeName> getPlayTypes(@Param("lotteryClassifyId")Integer lotteryClassifyId);
	/**
	 * 获取玩法内容
	 * @param playCode
	 * @param playType
	 * @return
	 */
//	public String getPlayContent(@Param("playCode")String playCode, @Param("playType")Integer playType);


	public void updateTicketData(OrderDetail orderDetail);
	/**
	 * 
	 * @param classifyId
	 * @param playClassifyId
	 * @return  status, redirectUrl
	 */
	public LotteryPlayClassifyTemp lotteryPlayClassifyStatusAndUrl(@Param("classifyId") int classifyId, @Param("playClassifyId") int playClassifyId);


	public LotteryClassifyTemp lotteryClassify(@Param("classifyId")Integer lotteryClassifyId);
	
}