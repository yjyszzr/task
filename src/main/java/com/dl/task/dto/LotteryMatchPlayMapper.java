package com.dl.task.dto;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.enums.MatchPlayTypeEnum;
import com.dl.base.mapper.Mapper;
import com.dl.shop.lottery.model.LotteryMatchPlay;

public interface LotteryMatchPlayMapper extends Mapper<LotteryMatchPlay> {

	List<LotteryMatchPlay> matchPlayListByChangciIds(@Param("changciIds")Integer[] matchIds, @Param("playType")String playType);

	void updatePlayContent(LotteryMatchPlay play);

	LotteryMatchPlay lotteryMatchPlayByChangciIdAndPlayType(@Param("changciId")Integer matchId, @Param("playType")int playType);
}