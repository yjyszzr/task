package com.dl.task.dao;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DlSysSplitTableTask;

public interface DlSysSplitTableTaskMapper extends Mapper<DlSysSplitTableTask> {

	DlSysSplitTableTask selectSplitTaskByTaskCode(@Param("taskCode") String taskCode);
}