package com.dl.task.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.alibaba.fastjson.JSON;
import com.dl.base.param.EmptyParam;
import com.dl.base.util.DateUtilNew;
import com.dl.base.util.SNGenerator;
import com.dl.lottery.api.IArtifiPrintLotteryService;
import com.dl.shop.payment.api.IpaymentService;
import com.dl.task.configurer.URLConfig;
import com.dl.task.model.DlOldBeltNew;
import com.dl.task.model.ExtraBonus;
import com.dl.task.model.InvitationsNum;
import com.dl.task.model.Order;
import com.dl.task.model.ReqOrdeEntity;
import com.dl.task.model.ReqOrdeEntityForUserAccount;
import com.dl.task.model.UserWithdraw;
import com.dl.task.service.DlMatchResultService;
import com.dl.task.service.DlOldBeltNewService;
import com.dl.task.service.DlPrintLotteryService;
import com.dl.task.service.LotteryRewardService;
import com.dl.task.service.OrderService;
import com.dl.task.service.PayMentService;
import com.dl.task.service.UserBonusService;
import com.dl.task.service.WithdrawService;
import com.dl.task.util.ManualAuditUtil;

import lombok.extern.slf4j.Slf4j;

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

	@Resource
	private DlOldBeltNewService dlOldBeltNewService;

	@Resource
	private URLConfig urlConfig;
	@Resource
	private DlMatchResultService dlMatchResultService;
	@Resource
	private IArtifiPrintLotteryService iArtifiPrintLotteryService;
	
	
//	/**
//	 * 第一步： 出票任务 （每5分钟执行一次） 调用第三方接口出票定时任务 定时的对出票中的进行查询结果
//	 */
//	@Scheduled(cron = "${task.schedule.match.score.refreshMatchResult}")
//	public void refreshMatchResult() {
//		log.info("比分计算赛果定时任务启动");
//		dlMatchResultService.refreshMatchResult();
//		log.info("比分计算赛果定时任务结束");
//	}

	/**
	 * 第一步： 出票任务 （每5分钟执行一次） 调用第三方接口出票定时任务 定时的对出票中的进行查询结果
	 * 2018-10-12 暂停对接第三方出票公司
	 */
//	@Scheduled(cron = "${task.schedule.lottery.print.lottery}")
//	public void printLottery() {
//		dlPrintLotteryService.goPrintLotteryVersion2();
//	}

	/**
	 * 查询出票信息任务 （每12分钟执行一次） 调用第三方接口出票定时任务 定时的对出票中的进行查询结果
	 * 2018-10-12 暂停对接第三方出票公司
	 */
//	@Scheduled(cron = "${task.schedule.lottery.print.querylottery}")
//	public void quereyPrintLottery() {
//		LocalTime localTime = LocalTime.now(ZoneId.systemDefault());
//		int hour = localTime.getHour();
//		if (hour < 1 || hour >= 9) {
//			log.info("彩票出票状态查询定时任务启动");
//			for(PrintComEnums printComEnums:PrintComEnums.values()){
//				if(PrintComEnums.WEICAISHIDAI==printComEnums){
//					continue;
//				}
//				dlPrintLotteryService.queryPrintLotteryVersion2(printComEnums);
//			}
//			log.info("彩票出票状态查询定时任务结束");
//		}
//	}

	/**
	 *查询出票信息任务 （每12分钟执行一次） 调用第三方接口出票定时任务 定时的对出票中的进行查询结果
	 *2018-10-12 暂停对接第三方出票公司
	 */
//	@Scheduled(cron = "${task.schedule.lottery.print.querylottery.weicai}")
//	public void quereyPrintLotteryWeiCai() {
//		LocalTime localTime = LocalTime.now(ZoneId.systemDefault());
//		int hour = localTime.getHour();
//		if (hour < 1 || hour >= 9) {
//			log.info("彩票出票状态微彩时代查询定时任务启动");
//			dlPrintLotteryService.queryPrintLotteryVersion2(PrintComEnums.WEICAISHIDAI);
//			log.info("彩票出票状态微彩时代查询定时任务结束");
//		}
//	}
	
	/**
	 * 去兑奖
	 */
/*	@Scheduled(cron = "${task.schedule.lottery.print.caixaiomi.toreward}")
	public void updatePrintLotteryCaiXiaoMiToRewardInfo() {
		dlPrintLotteryService.toRewardPrintLotteryVersion2(PrintComEnums.CAIXIAOMI);
	}*/
	/**
	 * 更新出票的中奖信息
	 */
/*	@Scheduled(cron = "${task.schedule.lottery.print.third.reward}")
	public void updatePrintLotteryThirdRewardInfo() {
		dlPrintLotteryService.rewardPrintLotteryVersion2();
	}*/

	/**
	 * 第二步： 对出票数据进行兑奖，更新彩票信息
	 */
	@Scheduled(cron = "${task.schedule.lottery.print.comparestatus}")
	public void updatePrintLotteryCompareStatus() {
		log.info("更新彩票信息，彩票对奖开始");
//		dlPrintLotteryService.updatePrintLotteryCompareStatus();
		dlPrintLotteryService.updatePrintLotteryCompareStatusJz();
/*		dlPrintLotteryService.updatePrintLotteryCompareStatusJl();*/
		log.info("更新彩票信息，彩票对奖结束");

	}

	/**
	 * 第三步： 订单出票结果更新 将出票信息回写到订单
	 * 
	 */
//	@Scheduled(cron = "${task.schedule.order.print.lottery.status}")
//	public void refreshOrderPrintStatus() {
//		log.info("开始执行更新订单出票结果任务");
//		orderService.refreshOrderPrintStatus();
//		log.info("结束执行更新订单出票结果任务");
//	}

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
//		orderService.updateOrderBasketMatchResult();
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
	 * 第三方支付的query 订单
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
	@Scheduled(cron = "${task.schedule.order.pay.success}")
	public void orderPaySuccessScheduled() {
		log.info("订单支付完成后的逻辑处理");
		List<Order> orderList = orderService.getPaySuccessOrdersList();
		for (Order order : orderList) {
			try {
				orderService.doPaySuccessOrder(order);
			} catch (Exception e) {
				log.error("处理订单支付成功order_sn={}", order.getOrderSn(), e);
				log.error("处理订单支付成功",e);
			}
		}
	}

	/**
	 * 订单支付失败逻辑处理
	 */
	@Scheduled(cron = "${task.schedule.order.pay.fail}")
	public void orderPayFailScheduled() {
		log.info("订单支付失败后的逻辑处理");
		List<Order> orderList = orderService.getPayFailOrdersList();
		for (Order order : orderList) {
			try {
				paymentService.dealBeyondPayTimeOrder(order);
			} catch (Exception e) {
				log.error("处理订单支付失败order_sn={}", order.getOrderSn(), e);
				log.error("处理订单支付失败order_sn=",e);
			}
		}
	}

	/**
	 * 第三方支付的query 充值
	 */
	@Scheduled(cron = "${task.schedule.recharge.pay.timeout}")
	public void timerRechargeQueryScheduled() {
		log.info("第三方支付定时任务开始");
		EmptyParam emptyParam = new EmptyParam();
		ipaymentService.timerRechargeQueryScheduled(emptyParam);
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
		for (UserWithdraw userWithdraw : userWithdrawFailRefundigList) {
			try {
				withdrawService.userWithdrawFailRefund(userWithdraw);
			} catch (Exception e) {
				log.error("withdrawsn={},提现失败回滚用户账户金额异常", userWithdraw.getWithdrawalSn(), e);
				log.error("提现失败回滚用户账户金额异常",e);
			}
		}
	}

	/**
	 * 老带新活动 新用户更改状态
	 */
	@Scheduled(cron = "${task.schedule.activity.oldBeltNew.updateUserStatus}")
	public void oldBeltNewUpdateUserStatus() {
		log.info("老带新活动定时开始=======================================");
		dlOldBeltNewService.updateConformingUser();// 更新符合条件的用户
		Integer status = 1;
		// 查询出当前status = 1 的人
		List<DlOldBeltNew> toBeConfirmedInvitationsList = dlOldBeltNewService.findInvitationsByUserId(status);
		// 查询出来每个用户下邀请了多少人 status != 0
		List<InvitationsNum> invitationsNumList = dlOldBeltNewService.findAllInvitationsNum();
		// 获取额外奖励列表
		List<ExtraBonus> extraBonusList = dlOldBeltNewService.findExtraBonus();
		Map<Integer, ExtraBonus> extraBonusMap = new HashMap<Integer, ExtraBonus>();
		extraBonusList.forEach(item -> extraBonusMap.put(item.getUserId(), item));
		List<ReqOrdeEntity> userIdAndRewardList = new ArrayList<ReqOrdeEntity>();

		for (int i = 0; i < invitationsNumList.size(); i++) {
			Integer userId = invitationsNumList.get(i).getUserId();
			Integer num = 0;
			List<Integer> userIds = new ArrayList<Integer>();
			ReqOrdeEntity reqOrdeEntity = new ReqOrdeEntity();
			for (int j = 0; j < toBeConfirmedInvitationsList.size(); j++) {
				Integer invitationsUserId = toBeConfirmedInvitationsList.get(j).getInviterUserId();
				if (userId.equals(invitationsUserId)) {
					num++;
					userIds.add(toBeConfirmedInvitationsList.get(j).getRegisterUserId());
					log.info("userId----------------------------------------------------------------------" + userId);
				}
			}
			if (num > 0) {
				dlOldBeltNewService.updateConformingUserToAward(userIds);
				// 更新该用户的邀请奖励
				String sn = SNGenerator.nextSN(9);// 生成订单号
				reqOrdeEntity.setOrderSn(sn);
				Integer amount = num * 20;
				reqOrdeEntity.setReward(Double.parseDouble(amount.toString()));
				reqOrdeEntity.setUserId(userId);
				reqOrdeEntity.setUserMoney(0);
				reqOrdeEntity.setBetMoney(0);
				reqOrdeEntity.setBetTime(DateUtilNew.getCurrentTimeString(Long.valueOf(DateUtilNew.getCurrentTimeLong()), DateUtilNew.datetimeFormat));
				reqOrdeEntity.setNote("邀请" + num + "个用户奖励" + amount + "元!");
				userIdAndRewardList.add(reqOrdeEntity);
			}
			// 组装额外奖励
			Integer extraBonus = 0;
			ExtraBonus extraBonusForMap = extraBonusMap.get(userId);
			if (invitationsNumList.get(i).getUserNum() >= 10 && invitationsNumList.get(i).getUserNum() < 20) { // 邀请人数-当前人数>10
				extraBonus = 15;
				if (extraBonusForMap == null) {
					// 第一次操作为插入 其余为修改
					ExtraBonus extraBonusInsert = new ExtraBonus();
					extraBonusInsert.setId(0);
					extraBonusInsert.setUserId(userId);
					extraBonusInsert.setExtraBonus(extraBonus);
					dlOldBeltNewService.insertExtraBonus(extraBonusInsert);
				} else {
					extraBonus -= extraBonusForMap.getExtraBonus();
				}
			} else if (invitationsNumList.get(i).getUserNum() >= 20 && invitationsNumList.get(i).getUserNum() < 30) {
				extraBonus = 15 + 30;
				if (extraBonusForMap == null) {
					// 第一次操作为插入 其余为修改
					ExtraBonus extraBonusInsert = new ExtraBonus();
					extraBonusInsert.setId(0);
					extraBonusInsert.setUserId(userId);
					extraBonusInsert.setExtraBonus(extraBonus);
					dlOldBeltNewService.insertExtraBonus(extraBonusInsert);
				} else {
					extraBonus -= extraBonusForMap.getExtraBonus();
				}
			} else if (invitationsNumList.get(i).getUserNum() >= 30 && invitationsNumList.get(i).getUserNum() < 40) {
				extraBonus = 15 + 30 + 50;
				if (extraBonusForMap == null) {
					// 第一次操作为插入 其余为修改
					ExtraBonus extraBonusInsert = new ExtraBonus();
					extraBonusInsert.setId(0);
					extraBonusInsert.setUserId(userId);
					extraBonusInsert.setExtraBonus(extraBonus);
					dlOldBeltNewService.insertExtraBonus(extraBonusInsert);
				} else {
					extraBonus -= extraBonusForMap.getExtraBonus();
				}
			} else if (invitationsNumList.get(i).getUserNum() >= 40 && invitationsNumList.get(i).getUserNum() < 50) {
				extraBonus = 15 + 30 + 50 + 70;
				if (extraBonusForMap == null) {
					// 第一次操作为插入 其余为修改
					ExtraBonus extraBonusInsert = new ExtraBonus();
					extraBonusInsert.setId(0);
					extraBonusInsert.setUserId(userId);
					extraBonusInsert.setExtraBonus(extraBonus);
					dlOldBeltNewService.insertExtraBonus(extraBonusInsert);
				} else {
					extraBonus -= extraBonusForMap.getExtraBonus();
				}
			} else if (invitationsNumList.get(i).getUserNum() >= 50 && invitationsNumList.get(i).getUserNum() < 100) {
				extraBonus = 15 + 30 + 50 + 70 + 80;
				if (extraBonusForMap == null) {
					// 第一次操作为插入 其余为修改
					ExtraBonus extraBonusInsert = new ExtraBonus();
					extraBonusInsert.setId(0);
					extraBonusInsert.setUserId(userId);
					extraBonusInsert.setExtraBonus(extraBonus);
					dlOldBeltNewService.insertExtraBonus(extraBonusInsert);
				} else {
					extraBonus -= extraBonusForMap.getExtraBonus();
				}
			} else if (invitationsNumList.get(i).getUserNum() >= 100) {
				extraBonus = 15 + 30 + 50 + 70 + 80 + 200;
				if (extraBonusForMap == null) {
					// 第一次操作为插入 其余为修改
					ExtraBonus extraBonusInsert = new ExtraBonus();
					extraBonusInsert.setId(0);
					extraBonusInsert.setUserId(userId);
					extraBonusInsert.setExtraBonus(extraBonus);
					dlOldBeltNewService.insertExtraBonus(extraBonusInsert);
				} else {
					extraBonus -= extraBonusForMap.getExtraBonus();
				}
			}
			// 如果额外奖励>0 进行一下操作
			if (extraBonus > 0) {
				ReqOrdeEntity reqOrdeEntityExtra = new ReqOrdeEntity();
				reqOrdeEntityExtra.setOrderSn(SNGenerator.nextSN(9));
				reqOrdeEntityExtra.setReward(Double.parseDouble(extraBonus.toString()));
				reqOrdeEntityExtra.setUserId(userId);
				reqOrdeEntityExtra.setUserMoney(0);
				reqOrdeEntityExtra.setBetMoney(0);
				reqOrdeEntityExtra.setBetTime(DateUtilNew.getCurrentTimeString(Long.valueOf(DateUtilNew.getCurrentTimeLong()), DateUtilNew.datetimeFormat));
				reqOrdeEntityExtra.setNote("邀请到" + invitationsNumList.get(i).getUserNum() + "个用户,额外奖励" + extraBonus + "元!");
				userIdAndRewardList.add(reqOrdeEntityExtra);

			}
		}
		if (userIdAndRewardList.size() > 0) {
			String reqStr = "{'userIdAndRewardList':" + JSON.toJSONString(userIdAndRewardList) + "}";
			log.info("请求reqStr===========================" + reqStr);
			// 保存奖励记录
			List<ReqOrdeEntityForUserAccount> userAccountList = new ArrayList<ReqOrdeEntityForUserAccount>(userIdAndRewardList.size());
			for (int j = 0; j < userIdAndRewardList.size(); j++) {
				ReqOrdeEntityForUserAccount userAccount = new ReqOrdeEntityForUserAccount();
				userAccount.setId(0);
				userAccount.setCreateTime(userIdAndRewardList.get(j).getBetTime());
				userAccount.setNote(userIdAndRewardList.get(j).getNote());
				userAccount.setOrderSn(userIdAndRewardList.get(j).getOrderSn());
				userAccount.setReward(userIdAndRewardList.get(j).getReward());
				userAccount.setUserId(userIdAndRewardList.get(j).getUserId());
				userAccount.setStatus(0);
				userAccountList.add(userAccount);
			}
			dlOldBeltNewService.insertUserAccount(userAccountList);
			ManualAuditUtil.ManualAuditUtil(reqStr, urlConfig.getManualRewardToUserMoneyLimitUrl(), true);
			// 更改奖励状态
			dlOldBeltNewService.updateUserAccount(userAccountList);
		}
		log.info("老带新活动定时结束=======================================");
	}

	/**
	 * 人工出票分单系统timer轮询
	 */
	@Scheduled(cron = "${task.schedule.lottery.artifi.schedualed}")
	public void artifiPrintLotteryTaskScheduled() {
		log.info("======================^_^===============================");
		log.info("[artifiPrintLotteryTaskScheduled]" + "人工出票分单系统");
		log.info("=====================================================");
		log.info("^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^");
		log.info("^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^");
		log.info("^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^");
		log.info("^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^");
		log.info("^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^");
		log.info("^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^");
		log.info("^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^^_^");
		EmptyParam emptyParam = new EmptyParam();
		iArtifiPrintLotteryService.artifiTaskTimer(emptyParam);
	}
}
