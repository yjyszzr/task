package com.dl.task.dao;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DlTicketChannelLotteryClassify;

public interface DlTicketChannelLotteryClassifyMapper extends Mapper<DlTicketChannelLotteryClassify> {
	List<DlTicketChannelLotteryClassify> selectOpenPrintChanel(@Param("lotteryClassifyId") Integer lotteryClassifyId,@Param("minMatchStartTimeSeconds") Integer minMatchStartTimeSeconds,@Param("ticketMoney") BigDecimal ticketMoney, @Param("excludePlayType") String excludePlayType, @Param("minBetAmountLottery") BigDecimal minBetAmountLottery, @Param("maxBetAmountLottery") BigDecimal maxBetAmountLottery);
}