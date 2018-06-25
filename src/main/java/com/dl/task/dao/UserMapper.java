package com.dl.task.dao;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.User;

public interface UserMapper extends Mapper<User> {

	//在数据库中更新用户不可提现金额
	int updateInDBUserMoneyLimit(User user);

}