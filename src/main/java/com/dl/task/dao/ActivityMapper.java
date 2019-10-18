package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.Activity;

public interface ActivityMapper extends Mapper<Activity> {

	List<Activity> queryActivityList(@Param("act_type") Integer act_type);
}