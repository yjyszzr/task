package com.dl.task.dao;

import java.util.List;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.UserWithdraw;

public interface UserWithdrawMapper extends Mapper<UserWithdraw> {
	
	
	List<UserWithdraw> queryUserWithdrawRefunding();
	
	int updateUserWithdraw4To2(UserWithdraw userWithdraw); 
}