package com.dl.task.dao;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DlLotteryClassify;

public interface DlLotteryClassifyMapper extends Mapper<DlLotteryClassify> {

	DlLotteryClassify selectOneByLotteryClassifyId(@Param("lotteryClassifyId") Integer lotteryClassifyId);
}