package com.dl.task.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dl.base.service.AbstractService;
import com.dl.task.dao.ActivityUserInfoMapper;
import com.dl.task.model.ActivityUserInfo;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(value = "transactionManager1")
@Slf4j
public class ActivityUserInfoService extends AbstractService<ActivityUserInfo> {
	@Resource
	private ActivityUserInfoMapper activityUserInfoMapper;
	
}
