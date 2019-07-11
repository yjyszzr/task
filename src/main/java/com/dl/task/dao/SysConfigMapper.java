package com.dl.task.dao;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.SysConfig;

public interface SysConfigMapper extends Mapper<SysConfig> {
	SysConfig selectConfigByBusinessId(Integer businessId);
	void updateConfigByBusinessId(Integer value);
}