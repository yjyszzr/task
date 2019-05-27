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
@Slf4j
@Service
@Transactional(value = "transactionManager1")
public class DlDonationRechargeCardService extends AbstractService<DlDonationRechargeCard> {
	@Resource
	private DlDonationRechargeCardMapper dlDonationRechargeCardMapper;
 
	public void updateRechargeCardExpire() {
		Integer status = 0;
		List<DlDonationRechargeCard> donationRechargeCardList = dlDonationRechargeCardMapper.selectByRechargeCardStatus(status);
		log.info("*****可用的大礼包列表" + donationRechargeCardList);
		List<Integer> userBonusIdList =new ArrayList<Integer>();
		for (int i = 0; i < donationRechargeCardList.size(); i++) {
			DlDonationRechargeCard donationRechargeCard =new DlDonationRechargeCard();
			donationRechargeCard = donationRechargeCardList.get(i);
			Integer addTime = donationRechargeCard.getAddTime();
			Integer effectiveDay = donationRechargeCard.getEffectiveDay();
			Integer currentTime = DateUtils.getCurrentTimeLong();
			//有效期 + 开始时间 = 结束时间
			log.info("***大礼包创建时间{}======有效期***{}",addTime,effectiveDay);
			Integer endTime = effectiveDay * 24 * 60 * 60 + addTime;
			log.info("当前时间："+currentTime+"-结束时间："+endTime+"="+(currentTime-endTime));
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
