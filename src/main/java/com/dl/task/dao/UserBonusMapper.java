package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.UserBonus;

public interface UserBonusMapper extends Mapper<UserBonus> {
	
	 UserBonus selectUserBonuByBonusId(Integer userBonusId);
	 
	 List<Integer> queryUserBonusIdsExpire(@Param("now") Integer now);
	 
	 int updateBatchUserBonusExpire(@Param("list") List<Integer> userBonusIdList);
	 
     int updateBonusUnuseByUserBonusId(UserBonus userBonus);
}