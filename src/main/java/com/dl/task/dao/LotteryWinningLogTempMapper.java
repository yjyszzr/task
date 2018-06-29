package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.LotteryWinningLogTemp;

public interface LotteryWinningLogTempMapper extends Mapper<LotteryWinningLogTemp> {

	void deleteByLogIds(@Param("ids") List<Integer> ids);

	List<LotteryWinningLogTemp> selectIsShowList();

	int insertlotteryWinningTemp(LotteryWinningLogTemp temp);
}