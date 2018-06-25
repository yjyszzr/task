package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.User;

public interface UserMapper extends Mapper<User> {

	//在数据库中更新用户不可提现金额
	int updateInDBUserMoneyLimit(User user);
	//在数据库中更新用户可提现金额
    int updateInDBUserMoney(User user);
	/**
	 * 在数据库中更新用户账户资金
	 * 
	 * @param user
	 * @return
	 */
	int updateInDBUserMoneyAndUserMoneyLimit(User user);
	/**
	 * 查询多个用户的当前余额
	 */
	List<User> queryUserByUserIds(@Param("userIds") List<Integer> userIds);
}