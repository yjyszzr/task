package com.dl.task.dao;

import java.util.List;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.UserAccount;

public interface UserAccountMapper extends Mapper<UserAccount> {

	List<UserAccount> queryUserAccountBySelective(UserAccount userAccount);

	int insertUserAccount(UserAccount userAccount);

}