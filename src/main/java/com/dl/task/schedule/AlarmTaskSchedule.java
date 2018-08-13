package com.dl.task.schedule;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.alibaba.fastjson.JSON;
import com.dl.base.util.JSONHelper;
import com.dl.task.model.DLSysAlarmTask;
import com.dl.task.service.DLSysAlarmTaskService;

@Slf4j
@Configuration
@EnableScheduling
public class AlarmTaskSchedule {
	@Autowired
	private DLSysAlarmTaskService dLSysAlarmTaskService;
	/**
	 * 系统报警任务
	 */
	@Scheduled(cron = "${task.schedule.alarm.task.system}")
	public void systemAlarm() {
		log.info("系统报警任务开始");
		List<DLSysAlarmTask> isOpenAlarms = dLSysAlarmTaskService.selectOpenAlarmTask();
		for(DLSysAlarmTask alarmTask:isOpenAlarms){
			try{
				dLSysAlarmTaskService.alarmTask(alarmTask);
			}catch(Exception e){
				log.error("报警出错，dl_sys_alarm_task={}",JSONHelper.bean2json(alarmTask));
				continue;
			}
		}
		log.info("系统报警任务结束");
	}
}
