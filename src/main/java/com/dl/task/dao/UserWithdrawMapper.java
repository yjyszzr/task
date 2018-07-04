package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.UserWithdraw;

public interface UserWithdrawMapper extends Mapper<UserWithdraw> {
	
	
	List<UserWithdraw> queryUserWithdrawRefunding();
	
	int updateUserWithdraw4To2(UserWithdraw userWithdraw); 
	
	/************下面的方法暂时无用，可能需要删除*****************/
	
	int insertUserWithdraw(UserWithdraw userWithdraw);
	
	int updateUserWithdrawBySelective(UserWithdraw userWithdraw);
	
	List<UserWithdraw> queryUserWithdrawBySelective(UserWithdraw userWithdraw);
	
	List<UserWithdraw> queryUserWithdrawByWithDrawSnAndUserId(UserWithdraw userWithdraw);
	
	int countUserWithdrawByUserId(@Param("userId") Integer userId);
	
}