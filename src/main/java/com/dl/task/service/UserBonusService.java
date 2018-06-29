package com.dl.task.service;

import java.util.List;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
import com.dl.task.dao.UserBonusMapper;
import com.dl.task.model.UserBonus;
import com.dl.task.param.UserBonusParam;

@Service
@Transactional(value="transactionManager1")
@Slf4j
public class UserBonusService{
	@Resource
	private UserBonusMapper userBonusMapper;
	
	/**
	 * 更新红包为已过期
	 * 
	 * @param userBonusIdList
	 */
	public void updateBonusExpire() {
		log.info("更新过期的红包定时任务开始");
		Integer now = DateUtil.getCurrentTimeLong();
		List<Integer> userBonusIdList = userBonusMapper.queryUserBonusIdsExpire(now);
		if (CollectionUtils.isEmpty(userBonusIdList)) {
			log.info("没有过期的红包，定时任务结束");
			return;
		}

		int rst = userBonusMapper.updateBatchUserBonusExpire(userBonusIdList);
		log.info("本次更新过期的红包" + rst + "个");
		log.info("更新过期的红包的定时任务结束");
	}
	
	/**
	 * 回滚下单时的账户变动：目前仅红包置为未使用
	 * 
	 * @param userBonusParam
	 * @return
	 */
	public BaseResult<String> rollbackChangeUserAccountByCreateOrder(UserBonusParam userBonusParam) {
		this.updateUserBonusStatusUnused(userBonusParam.getUserBonusId());
		return ResultGenerator.genSuccessResult("回滚红包状态成功");
	}
	
	/**
	 * 更新红包状态为未使用
	 *
	 * @param userBonusParam
	 * @return
	 */
	public void updateUserBonusStatusUnused(Integer userBonusId) {
		UserBonus userBonus = userBonusMapper.selectUserBonuByBonusId(userBonusId);
		if(userBonus!=null){
			UserBonus usedUserBonus = new UserBonus();
			usedUserBonus.setUserBonusId(userBonusId);
			usedUserBonus.setUsedTime(DateUtil.getCurrentTimeLong());
			userBonusMapper.updateBonusUnuseByUserBonusId(usedUserBonus);
		}else{			
			log.error("用户红包编号为" + userBonusId + "不存在");
		}
	}
}
