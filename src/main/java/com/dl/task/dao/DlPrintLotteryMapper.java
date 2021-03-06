package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.LotteryThirdApiLog;

public interface DlPrintLotteryMapper extends Mapper<DlPrintLottery> {
	
	//获取待出票的记录
	public List<DlPrintLottery> lotteryPrintsByUnPrint();
	//更新为出票中
	public int updatePrintIngStatusByTicketId(DlPrintLottery printLottery);
	//更新状态为出票失败，同时维护error及printtime
	public int updatePrintErrorStatusByTicketId(DlPrintLottery printLottery);
	//添加日志
	public int saveLotteryThirdApiLog(LotteryThirdApiLog thirdApiLog);
	//获取正在出票的记录
	public List<DlPrintLottery> getPrintIngLotterys();
	//更新出票信息
	public int updateLotteryPrintByCallBack(DlPrintLottery print);
	public int beatchUpdateLotteryPrintByCallBack(@Param("lotteryPrints")List<DlPrintLottery> lotteryPrints);
	//获取已出票还没有兑奖比对的记录
	public List<DlPrintLottery> lotteryPrintsByUnCompareJz();
	//获取篮彩已出票还没有兑奖比对的记录
	public List<DlPrintLottery> lotteryPrintsByUnCompareJl();
	//获取已出票还没有兑奖比对的记录
	public List<DlPrintLottery> lotteryPrintsByUnCompare();
	//更新彩票兑奖结果
	public int updatePrintLotteryCompareInfo(DlPrintLottery lotteryPrint);
	//通过订单获取对应的彩票
	public List<DlPrintLottery> getPrintLotteryListByGoOpenRewardOrderSns(@Param("orderSns")List<String> orderSns);
	//更新printstatus=17的订单staus=2
	public void updatePrintLotteryFailStatus(DlPrintLottery lotteryPrint);
	//获取订单下的所有标
	public List<DlPrintLottery> printLotterysByOrderSn(@Param("orderSn")String orderSn);
	public DlPrintLottery selectDlPrintLotteryByTicketId(String ticketId);
	public int updatePrintStatusByTicketId(DlPrintLottery lotteryPrint);
	public int beatchUpdatePrintStatusByTicketId(@Param("lotteryPrints")List<DlPrintLottery> lotteryPrints);
	public List<DlPrintLottery> lotteryPrintsHenanByUnPrint();
	
	public List<DlPrintLottery> lotteryPrintsXianByUnPrint();

	public List<DlPrintLottery> getPrintIngLotterysHenan();
	
	public List<DlPrintLottery> getPrintIngLotterysXian();
	public void insertDlPrintLottery(@Param("dlPrintLottery") DlPrintLottery dlPrintLottery);
	public void batchInsertDlPrintLottery(@Param("dlPrintLotterys") List<DlPrintLottery> dlPrintLotterys);
	public Double printLotteryRoutAmount();
	public List<DlPrintLottery> selectFinishPrintLotteryButNotRewardXian();
	public int updatePrintThirdRewardRewardStatus1To3(DlPrintLottery updateDlPrint);
	public int updatePrintThirdRewardRewardStatus2To3(DlPrintLottery updateDlPrint);
	public List<String> selectIssuesNotUpdateReward();
	public List<DlPrintLottery> selectFinishPrintLotteryButNotRewardHeNan();
	public List<DlPrintLottery> lotteryPrintsByUnPrintByChannelId(@Param("printChannelId") Integer printChannelId,@Param("status") Integer status);
	public List<DlPrintLottery> selectRewardLotterys(@Param("printChannelId") Integer printChannelId,@Param("thirdRewardStatus") Integer thirdRewardStatus);
	public int beatchUpdateComparedStakes(@Param("isSsue") String isSsue);
	public int updatePrintThirdRewardRewardStatus1To3AndPrintLottery(DlPrintLottery updateDlPrint);
	public int updatePrintThirdRewardRewardStatus2To3AndPrintLottery(DlPrintLottery updateDlPrint);
	public int batchUpdateDlPrintLotteryTowardDoing(@Param("dlPrintLotterys") List<DlPrintLottery> lotteryPrints);
}