package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.LotteryReward;

public interface LotteryRewardMapper extends Mapper<LotteryReward> {
	
	List<LotteryReward> queryRewardToday();
	
	List<LotteryReward> queryRewardByIssueBySelective(@Param("issue") String issue);
	
	LotteryReward queryRewardByChangciId(@Param("changciId") Integer changciId);
	
}