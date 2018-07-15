package com.dl.task.schedule;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.dl.base.param.EmptyParam;
import com.dl.shop.payment.api.IpaymentService;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.Order;
import com.dl.task.model.UserWithdraw;
import com.dl.task.service.DlPrintLotteryService;
import com.dl.task.service.LotteryRewardService;
import com.dl.task.service.OrderService;
import com.dl.task.service.PayMentService;
import com.dl.task.service.UserBonusService;
import com.dl.task.service.WithdrawService;

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
	private WithdrawService withdrawService;

	@Resource
	private IpaymentService ipaymentService;

	/**
	 * 第一步： 出票任务 （每5分钟执行一次） 调用第三方接口出票定时任务 定时的对出票中的进行查询结果
	 */
	@Scheduled(cron = "${task.schedule.lottery.print.lottery}")
	public void printLottery() {
		log.info("出票定时任务启动");
		dlPrintLotteryService.goPrintLottery();
		log.info("出票定时任务结束");
		// 每天9点前不作查询处理，只作出票处理
		LocalTime localTime = LocalTime.now(ZoneId.systemDefault());
		int hour = localTime.getHour();
		if (hour < 1 || hour >= 9) {
			log.info("彩票出票状态查询定时任务启动");
			dlPrintLotteryService.goQueryStake();
			log.info("彩票出票状态查询定时任务结束");
		}
	}
	/**
	 * 更新出票的中奖信息
	 */
	@Scheduled(cron = "${task.schedule.lottery.print.third.reward}")
	public void updatePrintLotteryThirdRewardInfo(){
		try{
	    dlPrintLotteryService.updatePrintLotterysThirdRewardXian();
		}catch(Exception e){
			log.error("定时更新西安获奖信息失败",e);
		}
		try{
		dlPrintLotteryService.updatePrintLotteryThirdRewardHeNan();
		}catch(Exception e){
			log.error("定时更新河南获奖信息失败",e);
		}
	}
	
	/**
	 * 第二步： 对出票数据进行兑奖，更新彩票信息
	 */
	@Scheduled(cron = "${task.schedule.lottery.print.comparestatus}")
	public void updatePrintLotteryCompareStatus() {
		log.info("更新彩票信息，彩票对奖开始");
		dlPrintLotteryService.updatePrintLotteryCompareStatus();
		log.info("更新彩票信息，彩票对奖结束");

	}

	/**
	 * 第三步： 订单出票结果更新 将出票信息回写到订单
	 * 
	 */
	@Scheduled(cron = "${task.schedule.order.print.lottery.status}")
	public void refreshOrderPrintStatus() {
		log.info("开始执行更新订单出票结果任务");
		orderService.refreshOrderPrintStatus();
		log.info("结束执行更新订单出票结果任务");
	}

	/**
	 * 第四步： 更新待开奖的订单状态及中奖金额
	 * 
	 */
	@Scheduled(cron = "${task.schedule.order.open.reward}")
	public void updateOrderAfterOpenReward() {
		log.info("更新待开奖的订单开始");
		lotteryRewardService.updateOrderAfterOpenReward();
		log.info("更新待开奖的订单结束");
	}

	/************************* 订单的定时任务 *****************/
	/**
	 * 订单详情赛果 （每5分钟执行一次）
	 */
	@Scheduled(cron = "${task.schedule.order.match.result}")
	public void updateOrderMatchResult() {
		log.info("开始执行更新订单详情赛果任务");
		orderService.updateOrderMatchResult();
		log.info("结束执行更新订单详情赛果任务");
	}

	/**
	 * 更新中奖用户的账户
	 */
	@Scheduled(cron = "${task.schedule.member.reward.money}")
	public void addRewardMoneyToUsers() {
		log.info("更新中奖用户的账户，派奖开始");
		orderService.addRewardMoneyToUsers();
		log.info("更新中奖用户的账户，派奖结束");
	}

	/************************** 用户的定时任务 *****************/
	/**
	 * 更新过期的红包
	 */
	@Scheduled(cron = "${task.schedule.member.bonus.expire}")
	public void updateBonusExpire() {
		log.info("更新过期的红包定时任务开始");
		userBonusService.updateBonusExpire();
		log.info("更新过期的红包的定时任务结束");
	}

	/**************** 支付的定时任务,调用支付模块 **************/
	@Scheduled(cron = "${task.schedule.payment.time.out}")
	public void dealBeyondPayTimeOrderOut() {
		log.info("开始执行混合支付超时订单任务");
		paymentService.dealBeyondPayTimeOrderOut();
		log.info("结束执行支混合付超时订单任务");
	}

	/**
	 * 第三方支付的query
	 */
	@Scheduled(cron = "${task.schedule.order.pay.timeout}")
	public void timerOrderQueryScheduled() {
		log.info("第三方支付定时任务开始");
		EmptyParam emptyParam = new EmptyParam();
		ipaymentService.timerOrderQueryScheduled(emptyParam);
	}
	/**
	 * 订单支付成功逻辑处理
	 */
	@Scheduled(cron = "${task.schedule.order.pay.timeout}")
	public void orderPaySuccessScheduled() {
		log.info("订单支付完成后的逻辑处理");
		List<Order> orderList = orderService.getPaySuccessOrdersList();
		for(Order order : orderList){
			try{
			orderService.doPaySuccessOrder(order);
			}catch(Exception e){
				log.error("处理订单支付order_sn={}",order.getOrderSn(),e);
			}
		}
	}

	/**
	 * 提现状态轮询
	 */
	@Scheduled(cron = "${task.schedule.payment.check.cash}")
	public void timerCheckCashReq() {
		log.info("提现状态轮询定时任务开始");
		EmptyParam emptyParam = new EmptyParam();
		ipaymentService.timerCheckCashReq(emptyParam);
	}
	/**
	 * 提现失败定时任务处理回退用户信息
	 */
	@Scheduled(cron = "${task.schedule.withdraw.fail}")
	public void withdrawFail() {
		log.info("提现失败定时处理订单");
		List<UserWithdraw> userWithdrawFailRefundigList = withdrawService.queryUserWithdrawRefundings();
		for(UserWithdraw userWithdraw:userWithdrawFailRefundigList){
			try{
				withdrawService.userWithdrawFailRefund(userWithdraw);
			}catch(Exception e){
				log.error("withdrawsn={},提现失败回滚用户账户金额异常",userWithdraw.getWithdrawalSn(),e);
			}
		}
	}
}
