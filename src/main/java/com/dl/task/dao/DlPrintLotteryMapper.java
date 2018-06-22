package com.dl.task.dao;

import java.util.List;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.LotteryPrint;
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
	
	public List<LotteryPrint> lotteryPrintsByUnCompare();
	
	public int updateBatchLotteryPrint(LotteryPrint lotteryPrint);
}