package com.dl.task.schedule;

import java.time.LocalTime;
import java.time.ZoneId;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.dl.base.param.EmptyParam;
import com.dl.shop.payment.api.IpaymentService;
import com.dl.task.service.DlPrintLotteryService;
import com.dl.task.service.LotteryRewardService;
import com.dl.task.service.OrderService;
import com.dl.task.service.PayMentService;
import com.dl.task.service.UserBonusService;

@Slf4j
@Configuration
@EnableScheduling
public class TaskSchedule {

	@Resource
	private DlPrintLotteryService dlPrintLotteryService;

	@Resource
	private LotteryRewardService lotteryRewardService;

	@Resource
	private UserBonusService userBonusService;

	@Resource
	private OrderService orderService;

	@Resource
	private PayMentService paymentService;

	@Resource
	private IpaymentService ipaymentService;

	/**
	 * 第一步： 出票任务 （每5分钟执行一次） 调用第三方接口出票定时任务 定时的对出票中的进行查询结果
	 */
	@Scheduled(cron = "0 0/1 * * * ?")
	public void printLottery() {
		log.info("出票定时任务启动");
		dlPrintLotteryService.goPrintLottery();
		log.info("出票定时任务结束");
		// 每天9点前不作查询处理，只作出票处理
		LocalTime localTime = LocalTime.now(ZoneId.systemDefault());
		int hour = localTime.getHour();
		if (hour >= 9) {
			log.info("彩票出票状态查询定时任务启动");
			dlPrintLotteryService.goQueryStake();
			log.info("彩票出票状态查询定时任务结束");
		}
	}

	/**
	 * 第二步： 对出票数据进行兑奖，更新彩票信息
	 */
	@Scheduled(cron = "0 0/5 * * * ?")
	public void updatePrintLotteryCompareStatus() {
		log.info("更新彩票信息，彩票对奖开始");
		dlPrintLotteryService.updatePrintLotteryCompareStatus();
		log.info("更新彩票信息，彩票对奖结束");

	}

	/**
	 * 第三步： 订单出票结果更新 将出票信息回写到订单
	 * 
	 */
	@Scheduled(cron = "0 0/1 * * * ?")
	public void refreshOrderPrintStatus() {
		log.info("开始执行更新订单出票结果任务");
		orderService.refreshOrderPrintStatus();
		log.info("结束执行更新订单出票结果任务");
	}

	/**
	 * 第四步： 更新待开奖的订单状态及中奖金额
	 * 
	 */
	@Scheduled(cron = "0 0/5 * * * ?")
	public void updateOrderAfterOpenReward() {
		log.info("更新待开奖的订单开始");
		lotteryRewardService.updateOrderAfterOpenReward();
		log.info("更新待开奖的订单结束");
	}

	/************************* 订单的定时任务 *****************/
	/**
	 * 订单详情赛果 （每5分钟执行一次）
	 */
	@Scheduled(cron = "0 0/2 * * * ?")
	public void updateOrderMatchResult() {
		log.info("开始执行更新订单详情赛果任务");
		orderService.updateOrderMatchResult();
		log.info("结束执行更新订单详情赛果任务");
	}

	/**
	 * 更新中奖用户的账户
	 */
	@Scheduled(cron = "0 0/5 * * * ?")
	public void addRewardMoneyToUsers() {
		log.info("更新中奖用户的账户，派奖开始");
		orderService.addRewardMoneyToUsers();
		log.info("更新中奖用户的账户，派奖结束");
	}

	/************************** 用户的定时任务 *****************/
	/**
	 * 更新过期的红包
	 */
	@Scheduled(cron = "0 0/1 * * * ?")
	public void updateBonusExpire() {
		log.info("更新过期的红包定时任务开始");
		userBonusService.updateBonusExpire();
		log.info("更新过期的红包的定时任务结束");
	}

	/**************** 支付的定时任务,调用支付模块 **************/
	@Scheduled(cron = "0 0/2 * * * ?")
	public void dealBeyondPayTimeOrderOut() {
		log.info("开始执行混合支付超时订单任务");
		paymentService.dealBeyondPayTimeOrderOut();
		log.info("结束执行支混合付超时订单任务");
	}

	/**
	 * 第三方支付的query
	 */
	@Scheduled(fixedRate = 1000 * 5)
	public void timerOrderQueryScheduled() {
		log.info("第三方支付定时任务开始");
		EmptyParam emptyParam = new EmptyParam();
		ipaymentService.timerOrderQueryScheduled(emptyParam);
		
	}

	/**
	 * 提现状态轮询
	 */
	@Scheduled(fixedRate = 1000 * 20)
	public void timerCheckCashReq() {
		log.info("体现状态轮询定时任务开始");
		EmptyParam emptyParam = new EmptyParam();
		ipaymentService.timerCheckCashReq(emptyParam);
	}
}
