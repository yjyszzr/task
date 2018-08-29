package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DLSysAlarmTask;

public interface DLSysAlarmTaskMapper extends Mapper<DLSysAlarmTask> {

	List<DLSysAlarmTask> selectSysAlarmOpenTask();

	int updateSmsCountPlusOne(DLSysAlarmTask update);
	int updateDingDingCountPlusOne(DLSysAlarmTask update);

	int reSetAlarmCode(@Param("alarmCode") String alarmCode);
}