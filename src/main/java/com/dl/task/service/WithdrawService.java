package com.dl.task.service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.dl.base.constant.CommonConstants;
import com.dl.base.enums.SNBusinessCodeEnum;
import com.dl.base.util.DateUtil;
import com.dl.base.util.SNGenerator;
import com.dl.task.dao.UserAccountMapper;
import com.dl.task.dao.UserMapper;
import com.dl.task.dao.UserWithdrawMapper;
import com.dl.task.model.User;
import com.dl.task.model.UserAccount;
import com.dl.task.model.UserWithdraw;
import com.dl.task.param.AddMessageParam;
import com.dl.task.param.MessageAddParam;

@Service
@Slf4j
public class WithdrawService {

	@Resource
	private UserWithdrawMapper userWithdrawMapper;
	
	@Resource
	private UserAccountMapper userAccountMapper;
	@Resource
	private UserMapper userMapper;
	@Resource 
	private DlMessageService dlMessageService;
	
	/**
	 * 获取提现失败退款中的数据
	 * @return
	 */
	public List<UserWithdraw> queryUserWithdrawRefundings(){
		List<UserWithdraw> userWithdrawFailRefundigList = userWithdrawMapper.queryUserWithdrawRefunding();
		return userWithdrawFailRefundigList;
	}
	
	
	@Transactional(value="transactionManager1")
	public void userWithdrawFailRefund(UserWithdraw userWithdraw) {
		String withdrawSn = userWithdraw.getWithdrawalSn();
		Integer userId = userWithdraw.getUserId();
		BigDecimal withdrawAmount = userWithdraw.getAmount();
		UserWithdraw updateWithdraw = new UserWithdraw();
		updateWithdraw.setPayTime(DateUtil.getCurrentTimeLong());
		updateWithdraw.setWithdrawalSn(withdrawSn);
//		更新提现状态为2失败
		int updateRow = userWithdrawMapper.updateUserWithdraw4To2(updateWithdraw);
		if(updateRow==1){			
//		修改用户余额，给用户约增加回去
			UserAccount queryUserAccount = new UserAccount();
			queryUserAccount.setUserId(userId);
			queryUserAccount.setPayId(withdrawSn);
			queryUserAccount.setProcessType(6);
			List<UserAccount> userThisWithdrawRollList = userAccountMapper.queryUserAccountBySelective(queryUserAccount);
			if(CollectionUtils.isEmpty(userThisWithdrawRollList)){
//				回滚用户可提现余额
				User updateUser = new User();
				updateUser.setUserId(userId);
				updateUser.setUserMoney(withdrawAmount);
				userMapper.updateInDBUserMoney(updateUser);
				User userUpdated = userMapper.queryUserByUserId(userId);
				log.info("回滚后" + userId + "账户值：" + userUpdated.getUserMoney().add(userUpdated.getUserMoneyLimit()));
				log.info("开始生成回滚账户流水");
//				生成账户流水
				UserAccount userAccount = new UserAccount();
				userAccount.setUserId(userId);
				String accountSn = SNGenerator.nextSN(SNBusinessCodeEnum.ACCOUNT_SN.getCode());
				userAccount.setAmount(withdrawAmount);
				userAccount.setAccountSn(accountSn);
				userAccount.setProcessType(6);
				userAccount.setThirdPartName("");
				userAccount.setThirdPartPaid(BigDecimal.ZERO);
				userAccount.setPayId(withdrawSn);
				userAccount.setAddTime(DateUtil.getCurrentTimeLong());
				userAccount.setLastTime(DateUtil.getCurrentTimeLong());
				userAccount.setCurBalance(userUpdated.getUserMoney().add(userUpdated.getUserMoneyLimit()));
				userAccount.setStatus(1);
				userAccount.setNote("提现回滚" + withdrawAmount);
				int rst = userAccountMapper.insertUserAccountBySelective(userAccount);
				log.info("生成回滚账户流水返回值" + rst);
//		发送提现失败通知
				sendWithdrawFailMessage(userWithdraw,userUpdated);
			}
		}
	}
	/**
	 * 发送提现失败消息通知
	 * @param userWithdraw
	 * @param user
	 */
	private void sendWithdrawFailMessage(UserWithdraw userWithdraw,User user) {
		AddMessageParam addParam = new AddMessageParam();
		List<MessageAddParam> params = new ArrayList<MessageAddParam>(1);
		MessageAddParam messageAddParam = new MessageAddParam();
		messageAddParam.setTitle(CommonConstants.FORMAT_WITHDRAW_FAIL_TITLE);
		messageAddParam.setContentDesc(CommonConstants.FORMAT_WITHDRAW_FAIL_CONTENT_DESC);
		BigDecimal amount = userWithdraw.getAmount();
		messageAddParam.setContent(MessageFormat.format(CommonConstants.FORMAT_WITHDRAW_CONTENT, amount.toString()));
		messageAddParam.setSender(-1);
		messageAddParam.setMsgType(0);
		messageAddParam.setReceiver(userWithdraw.getUserId());
		messageAddParam.setReceiveMobile(user.getMobile());
		messageAddParam.setObjectType(2);
		messageAddParam.setMsgUrl("");
		messageAddParam.setSendTime(DateUtil.getCurrentTimeLong());
		Integer addTime =userWithdraw.getAddTime();
		String addTimeStr = this.getTimeStr(addTime);
		Integer checkTime = DateUtil.getCurrentTimeLong();
		String checkTimeStr = this.getTimeStr(checkTime);
		Integer payTime = userWithdraw.getPayTime();
		String payTimeStr = this.getTimeStr(payTime);
		String strDesc = CommonConstants.FORMAT_WITHDRAW_MSG_FAIL_DESC;
		messageAddParam.setMsgDesc(MessageFormat.format(strDesc,addTimeStr, checkTimeStr, payTimeStr));
		params.add(messageAddParam);
		addParam.setParams(params);
		dlMessageService.add(addParam);
	}
	private String getTimeStr(Integer addTime) {
		if(addTime <= 0) {
			return "";
		}
		String addTimeStr = DateUtil.getCurrentTimeString(Long.valueOf(addTime), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:dd"));
		return addTimeStr;
	}
}
