package com.dl.task.service;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dl.base.service.AbstractService;
import com.dl.task.dao.DlDonationRechargeCardMapper;
import com.dl.task.model.DlDonationRechargeCard;
import com.dl.task.util.DateUtils;

import lombok.extern.slf4j.Slf4j;
import tk.mybatis.mapper.entity.Condition;
import tk.mybatis.mapper.entity.Example.Criteria;
@Slf4j
@Service
@Transactional(value = "transactionManager1")
public class DlDonationRechargeCardService extends AbstractService<DlDonationRechargeCard> {
	@Resource
	private DlDonationRechargeCardMapper dlDonationRechargeCardMapper;
 
	public void updateRechargeCardExpire() {
		Condition condition = new Condition(DlDonationRechargeCard.class);
		Criteria criteria = condition.createCriteria();
		criteria.andCondition("status=", 0);
		List<DlDonationRechargeCard> donationRechargeCardList = dlDonationRechargeCardMapper.selectByCondition(condition);
		log.info("*****可用的大礼包列表" + donationRechargeCardList);
		List<Integer> userBonusIdList =new ArrayList<Integer>();
		for (int i = 0; i < donationRechargeCardList.size(); i++) {
			DlDonationRechargeCard donationRechargeCard =new DlDonationRechargeCard();
			donationRechargeCard = donationRechargeCardList.get(i);
			Integer addTime = donationRechargeCard.getAddTime();
			Integer effectiveDay = donationRechargeCard.getEffectiveDay();
			Integer currentTime = DateUtils.getCurrentTimeLong();
			//有效期 + 开始时间 = 结束时间
			Integer endTime = effectiveDay * 24*60 * 60 * 1000 + addTime;
			if (currentTime > endTime ) {
				userBonusIdList.add(donationRechargeCard.getRechargeCardId());
			}
		}
		log.info("过期的大礼包" + userBonusIdList);
		if (userBonusIdList.size()>0) {
			dlDonationRechargeCardMapper.updateRechargeCardExpire(userBonusIdList);
		}
	}
}
