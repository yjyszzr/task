package com.dl.task.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DlOldBeltNew;
import com.dl.task.model.ExtraBonus;
import com.dl.task.model.InvitationsNum;
import com.dl.task.model.ReqOrdeEntityForUserAccount;

public interface DlOldBeltNewMapper extends Mapper<DlOldBeltNew> {

	public List<DlOldBeltNew> findInvitationsByUserId(Integer status);

	public List<InvitationsNum> findAllInvitationsNum();

	public void updateConformingUser(@Param("userIdsArr") List<Integer> userIdsArr, @Param("status") Integer status);

	public List<Integer> findToBeConfirmedUserIds();

	public List<ExtraBonus> findExtraBonus();

	public void insertExtraBonus(ExtraBonus extraBonusInsert);

	public void insertUserAccount(List<ReqOrdeEntityForUserAccount> userAccountList);

	public void updateUserAccountStatus(List<ReqOrdeEntityForUserAccount> userAccountList);
}