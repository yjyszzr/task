package com.dl.task.service;

import io.jsonwebtoken.lang.Collections;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
<<<<<<< HEAD
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
=======
>>>>>>> branch 'master' of http://39.107.121.76/back/task.git
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
<<<<<<< HEAD
import com.dl.base.enums.RespStatusEnum;
import com.dl.base.exception.ServiceException;
=======
import com.dl.base.enums.SNBusinessCodeEnum;
>>>>>>> branch 'master' of http://39.107.121.76/back/task.git
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
<<<<<<< HEAD
=======
import com.dl.base.util.SNGenerator;
>>>>>>> branch 'master' of http://39.107.121.76/back/task.git
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.OrderDetailMapper;
import com.dl.task.dao.OrderMapper;
<<<<<<< HEAD
import com.dl.task.dto.OrderDTO;
import com.dl.task.dto.OrderWithUserDTO;
import com.dl.task.dto.UserIdAndRewardDTO;
=======
import com.dl.task.dao.UserAccountMapper;
import com.dl.task.dao.UserBonusMapper;
import com.dl.task.dao.UserMapper;
import com.dl.task.model.DlPrintLottery;
>>>>>>> branch 'master' of http://39.107.121.76/back/task.git
import com.dl.task.model.Order;
<<<<<<< HEAD
import com.dl.task.param.OrderSnParam;
=======
import com.dl.task.model.User;
import com.dl.task.model.UserAccount;
import com.dl.task.model.UserBonus;
>>>>>>> branch 'master' of http://39.107.121.76/back/task.git
import com.dl.task.param.UpdateOrderInfoParam;
import com.dl.task.param.UserIdAndRewardListParam;

@Slf4j
@Service
public class OrderService extends AbstractService<Order> {

	@Resource
	private OrderMapper orderMapper;

	@Resource
	private OrderDetailMapper orderDetailMapper;
	
	@Resource
<<<<<<< HEAD
	private	UserAccountService userAccountService;

=======
	private DlPrintLotteryMapper dlPrintLotteryMapper;
//	TODO 胡贺东 消息
//	@Resource 
//	private IUserMessageService userMessageService;
	@Resource
	private UserBonusMapper userBonusMapper;

	@Resource
	private UserAccountMapper userAccountMapper;
	@Resource
	private UserMapper userMapper;
>>>>>>> branch 'master' of http://39.107.121.76/back/task.git
	/**
	 * 更新订单状态
	 * 
	 * @param param
	 * @return
	 */
	@Transactional
	public BaseResult<String> updateOrderInfoStatus(UpdateOrderInfoParam param) {
		log.info("-----------%%%--------更新订单状态:" + JSON.toJSONString(param));
		Order order = new Order();
		order.setOrderSn(param.getOrderSn());
		order.setOrderStatus(param.getOrderStatus());
		order.setPayStatus(param.getPayStatus());
		order.setPayTime(param.getPayTime());
		int rst = orderMapper.updateOrderStatus(order);
		log.info("-----------%%%%-----------更新订单状态结果:" + rst);
		return ResultGenerator.genSuccessResult("订单支付数据更新成功");
	}
<<<<<<< HEAD
	
	/**
	 * 根据订单编号查询订单数据
	 * 
	 * @param snParam
	 * @return
	 */
	public OrderDTO getOrderInfoByOrderSn(OrderSnParam snParam) {
		Order order = new Order();
		order.setOrderSn(snParam.getOrderSn());
		order = orderMapper.selectOne(order);
		OrderDTO orderDTO = new OrderDTO();
		if (null == order) {
			return orderDTO;
=======

	//更新待出票订单状态及出票赔率信息
	public void refreshOrderPrintStatus() {
		List<Order> orders = orderMapper.ordersListNoFinishAllPrintLottery();
		if (CollectionUtils.isNotEmpty(orders)) {
			log.info("refreshOrderPrintStatus需要获取出票状态的订单数：" + orders.size());
			int n = 0;
			List<Order> rollOrders = new ArrayList<Order>(orders.size());
			for (Order order : orders) {
				List<DlPrintLottery> printLotterys = dlPrintLotteryMapper.printLotterysByOrderSn(order.getOrderSn());
				if(Collections.isEmpty(printLotterys)) {
					continue;
				}
				Set<Integer> lotteryStatus = printLotterys.stream().map(item->item.getStatus()).collect(Collectors.toSet());
				if(lotteryStatus.contains(0) || lotteryStatus.contains(3)) {//存在有未出票或出票中的
					continue;
				}
				BigDecimal refundMoney = BigDecimal.ZERO;
				for(DlPrintLottery item: printLotterys) {
					if(2 == item.getStatus()) {
						refundMoney = refundMoney.add(item.getMoney().divide(new BigDecimal(100)));
					}
				}
				if(lotteryStatus.contains(1)) {//存在出票成功的
					order.setPrintLotteryStatus(4);
					if(refundMoney.setScale(2,RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO.setScale(2,RoundingMode.HALF_EVEN))>0) {
						order.setPrintLotteryStatus(2);
					}
					order.setPrintLotteryRefundAmount(refundMoney);
					int rst = orderMapper.updateOrderStatus1To3(order);
					n += rst;
				}else {//全为出票失败的
					order.setPrintLotteryStatus(3);
					order.setPrintLotteryRefundAmount(refundMoney);
					int rst = orderMapper.updateOrderStatus1To2(order);
					n += rst;
				}
				if(refundMoney.setScale(2,RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO.setScale(2,RoundingMode.HALF_EVEN))>0) {//退款
					BigDecimal bonusAmount = order.getBonus();
					Integer userBonusId = order.getUserBonusId();
					Integer userId = order.getUserId();
					if(userBonusId != null && userBonusId > 0 && 
							refundMoney.setScale(2,RoundingMode.HALF_EVEN).compareTo(bonusAmount.setScale(2,RoundingMode.HALF_EVEN)) > 0) {
						refundMoney = refundMoney.subtract(bonusAmount);
						UserBonus updateUserBonus = new UserBonus();
						updateUserBonus.setUserId(userId);
						updateUserBonus.setUserBonusId(userBonusId);
						updateUserBonus.setUsedTime(DateUtil.getCurrentTimeLong());
						int i = userBonusMapper.updateBonusUnuseByUserBonusId(updateUserBonus);
						log.info("出票失败退回优惠券，userid={},user_bonus_id={}",userId,userBonusId);
					}
					//账户流水查看
					UserAccount userAccountRoll = new UserAccount();
					userAccountRoll.setUserId(userId);
					userAccountRoll.setThirdPartPaid(refundMoney);
					userAccountRoll.setOrderSn(order.getOrderSn());
					userAccountRoll.setProcessType(ProjectConstant.ACCOUNT_ROLLBACK);
					List<UserAccount> userAccountListRoll = userAccountMapper.queryUserAccountBySelective(userAccountRoll);
					if (!CollectionUtils.isEmpty(userAccountListRoll)) {
						log.info("[rollbackUserMoneyOrderFailure]" +order.getOrderSn()+ " 订单已经回滚，无法再次回滚");
						return ;
					}
					User user = new User();
//					调整为不可提现余额
					user.setUserMoneyLimit(refundMoney);
					user.setUserId(userId);
					int cnt = userMapper.updateInDBUserMoneyAndUserMoneyLimit(user);
					log.info("[rollbackUserMoneyOrderFailure]" + " userId:" + userId + " amt:" + refundMoney +" result cnt:" + cnt);
					//===========记录退款流水====================
					UserAccount userAccountParamByType = new UserAccount();
					Integer accountType = ProjectConstant.ACCOUNT_ROLLBACK;
					log.info("===========更新用户流水表=======:" + accountType);
					userAccountParamByType.setProcessType(accountType);
					userAccountParamByType.setAmount(BigDecimal.ZERO.subtract(refundMoney));
					userAccountParamByType.setBonusPrice(BigDecimal.ZERO);//暂无红包金额
					userAccountParamByType.setOrderSn(order.getOrderSn());
					userAccountParamByType.setThirdPartPaid(refundMoney);
					userAccountParamByType.setUserId(userId);
					userAccountParamByType.setAddTime(DateUtil.getCurrentTimeLong());
					userAccountParamByType.setLastTime(DateUtil.getCurrentTimeLong());
					userAccountParamByType.setParentSn("");
					String accountSn = SNGenerator.nextSN(SNBusinessCodeEnum.ACCOUNT_SN.getCode());
					userAccountParamByType.setAccountSn(accountSn);
					userAccountParamByType.setPayId("0");
					userAccountParamByType.setPaymentName("");
					userAccountParamByType.setThirdPartName("");
					int count = userAccountMapper.insertUserAccount(userAccountParamByType);
					log.info("退款成功记录流水成功 cnt:" + count);
					rollOrders.add(order);
				}
			}
			log.info("refreshOrderPrintStatus实际更新状态的订单数：" + n);
//			this.goLotteryMessage(rollOrders);
>>>>>>> branch 'master' of http://39.107.121.76/back/task.git
		}
<<<<<<< HEAD
		try {
			BeanUtils.copyProperties(orderDTO, order);
		} catch (IllegalAccessException | InvocationTargetException e) {
			log.error("订单id：" + order.getOrderId() + "，查询订单失败");
			e.printStackTrace();
			throw new ServiceException(RespStatusEnum.FAIL.getCode(), "订单列表查询失败");
		}
		return orderDTO;
	}
=======
	}

//	TODO 胡贺东 暂时不考虑消息
//	@Async
//	private void goLotteryMessage(List<Order> orders) {
//		AddMessageParam addParam = new AddMessageParam();
//		List<MessageAddParam> params = new ArrayList<MessageAddParam>(orders.size());
//		for (Order order : orders) {
//			if (2 != order.getOrderStatus()) {
//				continue;
//			}
//			// 消息
//			MessageAddParam messageAddParam = new MessageAddParam();
//			messageAddParam.setTitle(CommonConstants.FORMAT_PRINTLOTTERY_TITLE);
//			messageAddParam.setContent(CommonConstants.FORMAT_PRINTLOTTERY_CONTENT);
//			messageAddParam.setContentDesc(CommonConstants.FORMAT_PRINTLOTTERY_CONTENT_DESC);
//			messageAddParam.setSender(-1);
//			messageAddParam.setMsgType(0);
//			messageAddParam.setReceiver(order.getUserId());
//			messageAddParam.setReceiveMobile("");
//			messageAddParam.setObjectType(3);
//			messageAddParam.setMsgUrl("");
//			messageAddParam.setSendTime(DateUtil.getCurrentTimeLong());
//			String ticketAmount = order.getTicketAmount().toString();
//			Integer addTime = order.getAddTime();
//			LocalDateTime loclaTime = LocalDateTime.ofEpochSecond(addTime, 0, ZoneOffset.of("+08:00"));
//			String format = loclaTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:dd"));
//			messageAddParam.setMsgDesc(MessageFormat.format(CommonConstants.FORMAT_PRINTLOTTERY_MSG_DESC, ticketAmount, format));
//			params.add(messageAddParam);
//		}
//		addParam.setParams(params);
//		userMessageService.add(addParam);
//	}
>>>>>>> branch 'master' of http://39.107.121.76/back/task.git

	public void addRewardMoneyToUsers() {
		List<OrderWithUserDTO> orderWithUserDTOs = orderMapper.selectOpenedAllRewardOrderList();
		log.info("派奖已中奖的用户数据：code=" + orderWithUserDTOs.size());
		if (CollectionUtils.isNotEmpty(orderWithUserDTOs)) {
			log.info("需要派奖的数据:" + orderWithUserDTOs.size());
			List<UserIdAndRewardDTO> userIdAndRewardDTOs = new LinkedList<UserIdAndRewardDTO>();
			for (OrderWithUserDTO orderWithUserDTO : orderWithUserDTOs) {
				UserIdAndRewardDTO userIdAndRewardDTO = new UserIdAndRewardDTO();
				userIdAndRewardDTO.setUserId(orderWithUserDTO.getUserId());
				userIdAndRewardDTO.setOrderSn(orderWithUserDTO.getOrderSn());
				userIdAndRewardDTO.setReward(orderWithUserDTO.getRealRewardMoney());
				int betTime = orderWithUserDTO.getBetTime();
				userIdAndRewardDTO.setBetMoney(orderWithUserDTO.getBetMoney());
				userIdAndRewardDTO.setBetTime(DateUtil.getTimeString(betTime, DateUtil.datetimeFormat));
				userIdAndRewardDTOs.add(userIdAndRewardDTO);
			}
			userAccountService.batchUpdateUserAccount(userIdAndRewardDTOs,ProjectConstant.REWARD_AUTO);
		}
	}
	
}
