package com.dl.task.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.task.dao.ActivityMapper;
import com.dl.task.dao.ActivityUserInfoMapper;
import com.dl.task.model.Activity;
import com.dl.task.model.ActivityUserInfo;

@Service
@Transactional(value="transactionManager1")
public class ActivityUserInfoService extends AbstractService<ActivityUserInfo> {
	@Resource
	private ActivityUserInfoMapper activityUserInfoMapper;
	@Resource
	private ActivityMapper activityMapper;
	
	public String timeUpdateActityUserInfoBl() {
		List<Activity> activityList = activityMapper.queryActivityList(3);//伯乐奖
		Integer copynum=0;
		Integer thisnum=0;
		for (Activity activity : activityList) {
			if(this.getDay(DateUtil.getCurrentTimeLong())==this.getDay(activity.getEnd_time())) {
				copynum = activityUserInfoMapper.insertHisToUserInfo();//将现有数据备份到历史表
				thisnum = activityUserInfoMapper.updateActivityUserInfoByBl();//清楚此次活动数据
				break;
			}
		}
		return "伯乐奖备份"+copynum+"条数据，修改"+thisnum+"条数据";
	}
	
	public String timeUpdateActityUserInfoRy() {
		List<Activity> activityList = activityMapper.queryActivityList(4);//荣耀奖
		Integer copynum=0;
		Integer thisnum=0;
		for (Activity activity : activityList) {
			if(this.getDay(DateUtil.getCurrentTimeLong())==this.getDay(activity.getEnd_time())) {
				copynum = activityUserInfoMapper.insertHisToUserInfo();//将现有数据备份到历史表
				thisnum = activityUserInfoMapper.updateActivityUserInfoByRy();//清楚此次活动数据
				break;
			}
		}
		return "荣耀奖备份"+copynum+"条数据，修改"+thisnum+"条数据";
	}
	
	public int getDay(Integer time) {
		String day = DateUtil.getTimeString(DateUtil.getCurrentTimeLong(), DateTimeFormatter.ofPattern("dd"));
		return day==null?0:Integer.valueOf(day);
	}
}
