package com.dl.task.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.task.dao.UserBonusMapper;
import com.dl.task.model.UserBonus;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class UserBonusService extends AbstractService<UserBonus> {
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

}
