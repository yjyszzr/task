package com.dl.task.dao;


import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DlDonationRechargeCard;

public interface DlDonationRechargeCardMapper extends Mapper<DlDonationRechargeCard> {
	

	void updateRechargeCardExpire(@Param("list") List<Integer> rechargeCardIdList);
	
}