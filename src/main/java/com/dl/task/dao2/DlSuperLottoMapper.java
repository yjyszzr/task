package com.dl.task.dao2;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DlSuperLotto;

public interface DlSuperLottoMapper extends Mapper<DlSuperLotto> {
	public DlSuperLotto selectPrizeResultByTermNum(@Param("termNum")String termNum);
}