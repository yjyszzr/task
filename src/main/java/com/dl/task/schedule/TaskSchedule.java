package com.dl.task.schedule;

import java.time.LocalTime;
import java.time.ZoneId;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.dl.task.service.DlPrintLotteryService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
public class TaskSchedule {

	@Resource
	private DlPrintLotteryService dlPrintLotteryService;
	/**
	 * 出票任务 （每5分钟执行一次）
	 * 调用第三方接口出票定时任务
	 */
	@Scheduled(cron = "0 0/1 * * * ?")
    public void printLottery() {
        log.info("出票定时任务启动");
        dlPrintLotteryService.goPrintLottery();
        log.info("出票定时任务结束");
        //每天9点前不作查询处理，只作出票处理
        LocalTime localTime = LocalTime.now(ZoneId.systemDefault());
        int hour = localTime.getHour();
        if(hour >= 9) {
        	log.info("彩票出票状态查询定时任务启动");
        	dlPrintLotteryService.goQueryStake();
        	log.info("彩票出票状态查询定时任务结束");
        }
    }
	
	/**
	 * 更新彩票信息
	 */
	@Scheduled(cron = "0 0/5 * * * ?")
	public void updatePrintLotteryCompareStatus() {
		log.info("更新彩票信息，彩票对奖开始");
//		lotteryPrintService.updatePrintLotteryCompareStatus();
		log.info("更新彩票信息，彩票对奖结束");
		
	}
	
	/**
	 * 更新待开奖的订单
	 * 
	 */
	@Scheduled(cron = "0 0/5 * * * ?")
	public void updateOrderAfterOpenReward() {
		log.info("更新待开奖的订单开始");
//		lotteryRewardService.updateOrderAfterOpenReward();
		log.info("更新待开奖的订单结束");
	}
	
}
