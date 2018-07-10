package com.dl.task.service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.dl.base.constant.CommonConstants;
import com.dl.base.enums.SNBusinessCodeEnum;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.SNGenerator;
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.DlMessageMapper;
import com.dl.task.dao.LotteryWinningLogTempMapper;
import com.dl.task.dao.OrderMapper;
import com.dl.task.dao.UserAccountMapper;
import com.dl.task.dao.UserMapper;
import com.dl.task.dto.SurplusPaymentCallbackDTO;
import com.dl.task.dto.SysConfigDTO;
import com.dl.task.dto.UserIdAndRewardDTO;
import com.dl.task.model.DlMessage;
import com.dl.task.model.LotteryWinningLogTemp;
import com.dl.task.model.Order;
import com.dl.task.model.User;
import com.dl.task.model.UserAccount;
import com.dl.task.param.SurplusPayParam;
import com.dl.task.util.GeTuiMessage;
import com.dl.task.util.GeTuiUtil;
import com.google.common.base.Joiner;

@Service
@Slf4j
@Transactional(value="transactionManager1")
public class UserAccountService extends AbstractService<UserAccount> {
	@Resource
	private UserAccountMapper userAccountMapper;

	@Resource
	private UserMapper userMapper;
	@Resource
	private OrderMapper orderMapper;

	@Resource
	private SysConfigService sysConfigService;

	@Resource
	private DlMessageMapper dlMessageMapper;
	@Resource
	private GeTuiUtil geTuiUtil;
	
	@Resource
	private	LotteryWinningLogTempMapper lotteryWinningLogTempMapper;

	/**
	 * 更新用户账户信息
	 * 
	 * @param payId
	 * @param status
	 * @param accountSn
	 * @return
	 */
	public BaseResult<String> updateUserAccount(String payId, Integer status, String accountSn) {
		UserAccount userAccount = new UserAccount();
		userAccount.setPayId(payId);
		userAccount.setStatus(status);
		userAccount.setAccountSn(accountSn);
		int rst = userAccountMapper.updateUserAccountBySelective(userAccount);
		if (1 != rst) {
			return ResultGenerator.genFailResult("余额支付后余额扣减失败");
		}
		return ResultGenerator.genSuccessResult("余额支付后余额扣减成功", "success");
	}

	/**
	 * 中奖后批量更新用户账户的可提现余额,dealType = 1,自动；dealType = 2,手动
	 * 
	 * @param userIdAndRewardList
	 */
	public BaseResult<String> batchUpdateUserAccount(List<UserIdAndRewardDTO> dtos, Integer dealType) {
		List<UserIdAndRewardDTO> oldUserIdAndRewardDtos = new ArrayList<UserIdAndRewardDTO>(dtos);
		List<UserIdAndRewardDTO> userIdAndRewardList = new ArrayList<UserIdAndRewardDTO>(dtos);
		BigDecimal limitValue = BigDecimal.ZERO;
		if (1 == dealType) {
			limitValue = this.queryBusinessLimit(CommonConstants.BUSINESS_ID_REWARD);
			if (limitValue.compareTo(BigDecimal.ZERO) <= 0) {
				limitValue = BigDecimal.ZERO;
			}

			Double limitValueDouble = limitValue.doubleValue();
			this.dealBeyondLimitOrder(userIdAndRewardList, limitValueDouble);
			userIdAndRewardList.removeIf(s -> s.getReward().doubleValue() >= limitValueDouble);
		}
		if (userIdAndRewardList.size() == 0) {
			log.info("没有要自动开奖的订单");
			return ResultGenerator.genSuccessResult("没有要自动开奖的订单");
		}

		log.info("=^_^= =^_^= =^_^= =^_^= 派奖开始,派奖数据包括:" + JSON.toJSONString(userIdAndRewardList));

		// 查询是否已经派发奖金,并过滤掉
		List<String> orderSnList = userIdAndRewardList.stream().map(s -> s.getOrderSn()).collect(Collectors.toList());
		List<String> rewardOrderSnList = userAccountMapper.queryUserAccountRewardByOrdersn(orderSnList);
		if (rewardOrderSnList.size() > 0) {
			log.error("含有已派发过奖金的订单号，已被过滤,订单号包括：" + Joiner.on(",").join(rewardOrderSnList));
			for(String s: rewardOrderSnList){
				orderMapper.updateOrderStatus6To5(s);
			}
			userIdAndRewardList.removeIf(s -> rewardOrderSnList.contains(s.getOrderSn()));
		}

		Integer accountTime = DateUtil.getCurrentTimeLong();
		for (UserIdAndRewardDTO uDTO : userIdAndRewardList) {
			User updateUserMoney = new User();
			updateUserMoney.setUserId(uDTO.getUserId());
			updateUserMoney.setUserMoney(uDTO.getReward());
			userMapper.updateInDBUserMoney(updateUserMoney);
			UserAccount userAccountParam = new UserAccount();
			String accountSn = SNGenerator.nextSN(SNBusinessCodeEnum.ACCOUNT_SN.getCode());
			userAccountParam.setAccountSn(accountSn);
			userAccountParam.setOrderSn(uDTO.getOrderSn());
			userAccountParam.setUserId(uDTO.getUserId());
			userAccountParam.setAmount(uDTO.getReward());
			userAccountParam.setProcessType(ProjectConstant.REWARD);
			userAccountParam.setLastTime(DateUtil.getCurrentTimeLong());
			userAccountParam.setAddTime(accountTime);
			userAccountParam.setStatus(Integer.valueOf(ProjectConstant.FINISH));
			int insertRst = userAccountMapper.insertUserAccountBySelective(userAccountParam);
			if (1 != insertRst) {
				log.error("中奖订单号为" + uDTO.getOrderSn() + "生成中奖流水失败");
			} else {
				log.error("用户" + uDTO.getUserId() + "中奖订单号为" + uDTO.getOrderSn() + "奖金派发完成");
			}
		}
		log.info("更新用户中奖订单为已派奖开始");
		for(UserIdAndRewardDTO s: userIdAndRewardList){
			orderMapper.updateOrderStatus6To5(s.getOrderSn());
		}
		log.info("更新用户中奖订单为已派奖成功");
		//推送消息
		saveRewardMessageAsync(userIdAndRewardList, accountTime);

		//记录中奖信息
		this.updateLotteryWinning(oldUserIdAndRewardDtos);
		log.info("=^_^= =^_^= =^_^= =^_^= " + DateUtil.getCurrentDateTime() + "用户派发奖金完成" + "=^_^= =^_^= =^_^= =^_^= ");

		return ResultGenerator.genSuccessResult("用户派发奖金完成");
	}
	/**
	 * 异步保存中奖消息
	 * 
	 * @param list
	 */
	@Async
	public void saveRewardMessageAsync(List<UserIdAndRewardDTO> list, Integer accountTime) {
		List<Integer> userIdList = list.stream().map(s -> s.getUserId()).collect(Collectors.toList());
		if(CollectionUtils.isEmpty(userIdList)) {
			return;
		}
		log.info(" 00000   " + JSONHelper.bean2json(userIdList));
		List<User> userList = userMapper.queryUserByUserIds(userIdList);
		if (CollectionUtils.isEmpty(userList)) {
			return;
		}

		for (UserIdAndRewardDTO u : list) {
			DlMessage messageAddParam = new DlMessage();
			messageAddParam.setTitle(CommonConstants.FORMAT_REWARD_TITLE);
			messageAddParam.setContent(MessageFormat.format(CommonConstants.FORMAT_REWARD_CONTENT, u.getReward()));
			messageAddParam.setContentDesc(CommonConstants.FORMAT_REWARD_CONTENT_DESC);
			// messageAddParam.setContentUrl("www.baidu.com");//通知暂时不需要
			messageAddParam.setSender(u.getUserId());
			messageAddParam.setMsgType(0);
			messageAddParam.setReceiver(u.getUserId());
			String clientId = null;
			for (User user : userList) {
				if (user.getUserId().equals(u.getUserId())) {
					messageAddParam.setReceiverMobile(user.getMobile());
					clientId = user.getPushKey();
				}
				continue;
			}
			messageAddParam.setObjectType(1);
			messageAddParam.setMsgUrl("");// 通知暂时不需要
			messageAddParam.setSendTime(accountTime);
			messageAddParam.setMsgDesc(MessageFormat.format(CommonConstants.FORMAT_REWARD_MSG_DESC, u.getBetMoney(), u.getBetTime()));
			dlMessageMapper.insertInDbSelective(messageAddParam);
			//push
			if(StringUtils.isNotBlank(clientId)) {
				String content = MessageFormat.format(CommonConstants.FORMAT_REWARD_PUSH_DESC, u.getReward());
				GeTuiMessage getuiMessage = new GeTuiMessage(CommonConstants.FORMAT_REWARD_PUSH_TITLE, content, DateUtil.getCurrentTimeLong());
				geTuiUtil.pushMessage(clientId, getuiMessage);
			}
		}
	}

	/**
	 * 更新超过阈值的订单状态为派奖审核中
	 * @param userIdAndRewardList
	 * @param limitValueDouble
	 */
	public void dealBeyondLimitOrder(List<UserIdAndRewardDTO> userIdAndRewardList,Double limitValueDouble) {
		List<UserIdAndRewardDTO> beyondLimitList = userIdAndRewardList.stream().filter(s -> s.getReward().doubleValue() >= limitValueDouble).collect(Collectors.toList());
		if(beyondLimitList.size() == 0) {
			return;
		}
		for(UserIdAndRewardDTO s: beyondLimitList){
			orderMapper.updateOrderStatus6To7(s.getOrderSn());
		}
	}
	
	
	/**
	 * 查询业务值得限制：CommonConstants 中9-派奖限制 8-提现限制
	 * 
	 * @return
	 */
	public BigDecimal queryBusinessLimit(Integer businessId) {
		// 检查是否设置了派奖阈值
		SysConfigDTO sysDTO = sysConfigService.querySysConfig(businessId);
		if (sysDTO == null) {
			log.warn("派奖前，请前往后台管理设置派奖的奖金阈值");
			return BigDecimal.ZERO;
		}
		BigDecimal limitValue = sysDTO.getValue();
		return limitValue;
	}

	/**
	 * 更新跑马灯中奖信息
	 * @param userIdAndRewardList
	 */
	public void updateLotteryWinning(List<UserIdAndRewardDTO> userIdAndRewardList) {
		List<UserIdAndRewardDTO> collect = userIdAndRewardList.stream().filter(u->u.getReward().doubleValue() > 500).sorted((item1,item2)->item1.getReward().compareTo(item2.getReward())).collect(Collectors.toList());
    	if(collect.size() > 10) {
    		collect = collect.subList(0, 10);
    	}
    	log.info(JSONHelper.bean2json(collect));
    	Set<Integer> set = collect.stream().map(dto->dto.getUserId()).collect(Collectors.toSet());
    	log.info("111111111111    " + JSONHelper.bean2json(set));
    	List<Integer> userIds = new ArrayList<Integer>(set);
    	if(null == userIds ||  userIds.size() == 0) {
    		return;
    	}
    	
    	List<User> users = userMapper.queryUserByUserIds(userIds);
    	Map<Integer, String> map = new HashMap<Integer, String>(users.size());
    	for(User user: users) {
    		map.put(user.getUserId(), user.getMobile());
    	}
		List<LotteryWinningLogTemp> olds = lotteryWinningLogTempMapper.selectIsShowList();
    	for(UserIdAndRewardDTO dto: collect) {
    		BigDecimal reward = dto.getReward();
    		String mobile = map.get(dto.getUserId());
    		LotteryWinningLogTemp temp = new LotteryWinningLogTemp();
    		temp.setWinningMoney(reward);
    		temp.setPhone(mobile==null?"":mobile);
    		temp.setIsShow(1);
    		lotteryWinningLogTempMapper.insertlotteryWinningTemp(temp);
    	}
    	int num = olds.size() + collect.size() - 10;
    	if(num > 0) {
    		List<Integer> ids = olds.stream().sorted((item1,item2)-> item1.getWinningLogId().compareTo(item2.getWinningLogId()))
    		.limit(num).map(dto->dto.getWinningLogId()).collect(Collectors.toList());
    		lotteryWinningLogTempMapper.deleteByLogIds(ids);
    	}
	}
	
	/**
	 * 回滚含有余额部分的付款所用的钱
	 * 
	 * @param String
	 *            orderSn,String surplus
	 */
	public BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay(SurplusPayParam surplusPayParam) {
		String inPrams = JSON.toJSONString(surplusPayParam);
		log.info(DateUtil.getCurrentDateTime() + "使用到了部分或全部余额时候回滚支付传递的参数:" + inPrams);
		Order order = orderMapper.getOrderInfoByOrderSn(surplusPayParam.getOrderSn());
		if (null == order) {
			return ResultGenerator.genFailResult("没有该笔订单" + surplusPayParam.getOrderSn() + "，无法回滚账户");
		}

		Integer userId = order.getUserId();
		if (null == userId) {
			return ResultGenerator.genFailResult("该笔订单" + surplusPayParam.getOrderSn() + "userId为空，无法回滚账户");
		}
		User user = userMapper.queryUserByUserId(userId);
		if (null == user) {
			return ResultGenerator.genFailResult("没有这个用户的用户id，无法回滚账户");
		}

		UserAccount userAccount = new UserAccount();
		userAccount.setUserId(userId);
		userAccount.setOrderSn(surplusPayParam.getOrderSn());
		userAccount.setProcessType(ProjectConstant.BUY);
		List<UserAccount> userAccountList = userAccountMapper.queryUserAccountBySelective(userAccount);
		if (CollectionUtils.isEmpty(userAccountList)) {
			return ResultGenerator.genFailResult("订单号为" + surplusPayParam.getOrderSn() + "没有账户记录，无法回滚");
		}

		UserAccount userAccountRoll = new UserAccount();
		userAccountRoll.setUserId(userId);
		userAccountRoll.setThirdPartPaid(surplusPayParam.getThirdPartPaid());
		userAccountRoll.setOrderSn(surplusPayParam.getOrderSn());
		userAccountRoll.setProcessType(ProjectConstant.ACCOUNT_ROLLBACK);
		List<UserAccount> userAccountListRoll = userAccountMapper.queryUserAccountBySelective(userAccountRoll);
		if (!CollectionUtils.isEmpty(userAccountListRoll)) {
			return ResultGenerator.genFailResult("订单号为" + surplusPayParam.getOrderSn() + "已经回滚，无法再次回滚");
		}

		User updateUser = new User();
		BigDecimal userSurplus = order.getUserSurplus();
		BigDecimal userSurplusLimit = order.getUserSurplusLimit();
		updateUser.setUserId(user.getUserId());
		if (userSurplus != null && userSurplus.doubleValue() > 0) {
			log.info("user money: " + user.getUserMoney());
			updateUser.setUserMoney(userSurplus);
			userMapper.updateInDBUserMoney(updateUser);
		}

		if (userSurplusLimit != null && userSurplusLimit.doubleValue() > 0) {
			updateUser.setUserMoneyLimit(userSurplusLimit);
			userMapper.updateInDBUserMoneyLimit(updateUser);
		}
		UserAccount userAccountParam = new UserAccount();
		String accountSn = SNGenerator.nextSN(SNBusinessCodeEnum.ACCOUNT_SN.getCode());
		userAccountParam.setAccountSn(accountSn);
		userAccountParam.setUserId(userId);
		userAccountParam.setAmount(order.getUserSurplus().add(order.getUserSurplusLimit()));
		User curUser = userMapper.queryUserByUserId(userId);
		BigDecimal curBalance = curUser.getUserMoney().add(user.getUserMoneyLimit());
		userAccountParam.setCurBalance(curBalance);
		userAccountParam.setProcessType(ProjectConstant.ACCOUNT_ROLLBACK);
		userAccountParam.setOrderSn(order.getOrderSn());
		userAccountParam.setPaymentName(order.getPayName());
		userAccountParam.setThirdPartName(StringUtils.isEmpty(order.getPayName()) ? "" : order.getPayName());
		userAccountParam.setAddTime(DateUtil.getCurrentTimeLong());
		userAccountParam.setLastTime(DateUtil.getCurrentTimeLong());
		userAccountParam.setPayId(String.valueOf(order.getPayId()));
		int insertRst = userAccountMapper.insertUserAccountBySelective(userAccountParam);
		SurplusPaymentCallbackDTO surplusPaymentCallbackDTO = new SurplusPaymentCallbackDTO();
		surplusPaymentCallbackDTO.setSurplus(BigDecimal.ZERO);
		surplusPaymentCallbackDTO.setUserSurplus(BigDecimal.ZERO);
		surplusPaymentCallbackDTO.setUserSurplusLimit(BigDecimal.ZERO);
		surplusPaymentCallbackDTO.setCurBalance(curBalance);
		return ResultGenerator.genSuccessResult("success", surplusPaymentCallbackDTO);
	}
}
