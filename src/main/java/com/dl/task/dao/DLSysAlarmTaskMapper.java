package com.dl.task.dao;

import java.util.List;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DLSysAlarmTask;

public interface DLSysAlarmTaskMapper extends Mapper<DLSysAlarmTask> {

	List<DLSysAlarmTask> selectSysAlarmOpenTask();

	int updateSmsCountPlusOne(DLSysAlarmTask update);
	int updateDingDingCountPlusOne(DLSysAlarmTask update);
}