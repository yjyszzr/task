package com.dl.task.dao2;

import java.util.List;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DlMatchResult;

public interface DlMatchResultMapper extends Mapper<DlMatchResult> {
	
	//获取需要计算彩果的记录
	List<DlMatchResult> goGetMatchResult();
	//更新彩果信息到库
	int updateMatchResult(DlMatchResult matchResult);
}