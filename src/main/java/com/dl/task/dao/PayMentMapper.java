package com.dl.task.dao;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.PayMent;


public interface PayMentMapper extends Mapper<PayMent> {
	void updateReadMoneyByPayCode(@Param("readMoney") String read_money,@Param("payCode") String pay_code);
}