package com.dl.task.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dl.base.service.AbstractService;
import com.dl.task.dao.DlOldBeltNewMapper;
import com.dl.task.model.DlOldBeltNew;
import com.dl.task.model.ExtraBonus;
import com.dl.task.model.InvitationsNum;
import com.dl.task.model.ReqOrdeEntityForUserAccount;

@Service
@Transactional(value = "transactionManager1")
public class DlOldBeltNewService extends AbstractService<DlOldBeltNew> {
	@Resource
	private DlOldBeltNewMapper dlOldBeltNewMapper;

	public List<DlOldBeltNew> findInvitationsByUserId(Integer status) {
		return dlOldBeltNewMapper.findInvitationsByUserId(status);
	}

	public List<InvitationsNum> findAllInvitationsNum() {
		return dlOldBeltNewMapper.findAllInvitationsNum();
	}

	public void updateConformingUser() {
		List<Integer> userIds = dlOldBeltNewMapper.findToBeConfirmedUserIds();
		if (userIds.size() > 0) {
			Integer status = 1; // 状态为1 说明待确认
			dlOldBeltNewMapper.updateConformingUser(userIds, status);
		}
	}

	public void updateConformingUserToAward(List<Integer> userIds) {
		if (userIds.size() > 0) {
			Integer status = 2; // 状态为2 说明给用户发送过奖励
			dlOldBeltNewMapper.updateConformingUser(userIds, status);
		}
	}

	public List<ExtraBonus> findExtraBonus() {
		return dlOldBeltNewMapper.findExtraBonus();
	}

	public void insertExtraBonus(ExtraBonus extraBonusInsert) {
		dlOldBeltNewMapper.insertExtraBonus(extraBonusInsert);

	}

	public void insertUserAccount(List<ReqOrdeEntityForUserAccount> userAccountList) {
		dlOldBeltNewMapper.insertUserAccount(userAccountList);
	}

	public void updateUserAccount(List<ReqOrdeEntityForUserAccount> userAccountList) {
		dlOldBeltNewMapper.updateUserAccount(userAccountList);

	}

}
