package com.dl.task.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.task.dao.ActivityConfigMapper;
import com.dl.task.dao.ActivityMapper;
import com.dl.task.dao.ActivityUserInfoMapper;
import com.dl.task.model.Activity;
import com.dl.task.model.ActivityConfig;
import com.dl.task.model.ActivityUserInfo;

@Service
public class ActivityUserInfoService extends AbstractService<ActivityUserInfo> {
	@Resource
	private ActivityUserInfoMapper activityUserInfoMapper;
	@Resource
	private ActivityMapper activityMapper;
	@Resource
	private ActivityConfigMapper activityConfigMapper;
	
	public String timeUpdateActityUserInfoBl() {
		List<Activity> activityList = activityMapper.queryActivityList(3);//伯乐奖
		Integer copynum=0;
		Integer thisnum=0;
		Integer delnum=0;
		for (Activity activity : activityList) {
			if(this.getDay(DateUtil.getCurrentTimeLong())==this.getDay(activity.getEnd_time())) {
				copynum = activityUserInfoMapper.insertHisToUserInfo();//将现有数据备份到历史表
				thisnum = activityUserInfoMapper.updateActivityUserInfoByBl();//清楚此次活动数据
				List<String> aclist = activityConfigMapper.queryActivityConfigList(activity.getAct_id());//查询当前活动档位
				if(aclist!=null && aclist.size()>0) {
					delnum = activityConfigMapper.deleteConfigRecByConfigId(aclist);//删除挡位领取记录
				}
				break;
			}
		}
		return "伯乐奖备份"+copynum+"条数据，修改"+thisnum+"条数据，清除出档位领取数据"+delnum+"条";
	}
	
	public String timeUpdateActityUserInfoRy() {
		List<Activity> activityList = activityMapper.queryActivityList(4);//荣耀奖
		Integer copynum=0;
		Integer thisnum=0;
		Integer delnum=0;
		for (Activity activity : activityList) {
			if(this.getDay(DateUtil.getCurrentTimeLong())==this.getDay(activity.getEnd_time())) {
				copynum = activityUserInfoMapper.insertHisToUserInfo();//将现有数据备份到历史表
				thisnum = activityUserInfoMapper.updateActivityUserInfoByRy();//清楚此次活动数据
				List<String> aclist = activityConfigMapper.queryActivityConfigList(activity.getAct_id());//查询当前活动档位
				if(aclist!=null && aclist.size()>0) {
					delnum = activityConfigMapper.deleteConfigRecByConfigId(aclist);//删除挡位领取记录
				}
				break;
			}
		}
		return "荣耀奖备份"+copynum+"条数据，修改"+thisnum+"条数据，清除出档位领取数据"+delnum+"条";
	}
	
	public int getDay(Integer time) {
		String day = DateUtil.getTimeString(DateUtil.getCurrentTimeLong(), DateTimeFormatter.ofPattern("dd"));
		return day==null?0:Integer.valueOf(day);
	}
}
