package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DlMessage;
import com.dl.task.param.UserMessageListParam;

public interface DlMessageMapper extends Mapper<DlMessage> {

	List<DlMessage> findUserMessageList(UserMessageListParam param);

	int getUnReadMessageNum(@Param("userId")Integer userId);
	
	int updateUnReadMessage(@Param("userId")Integer userId);
}