package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DlArtifiPrintLottery;

public interface DlArtifiPrintLotteryMapper extends Mapper<DlArtifiPrintLottery> {
	
	int batchInsert(@Param("dlArtifiLotterys") List<DlArtifiPrintLottery> dlArtifiLotterys);
	
}