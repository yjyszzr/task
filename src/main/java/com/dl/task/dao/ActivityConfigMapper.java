package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.ActivityConfig;

public interface ActivityConfigMapper extends Mapper<ActivityConfig> {

	List<String> queryActivityConfigList(@Param("act_id") Integer act_id);
	
	int deleteConfigRecByConfigId(List<String> configId);
}