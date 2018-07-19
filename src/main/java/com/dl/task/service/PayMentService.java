package com.dl.task.service;
import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.dl.base.result.BaseResult;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.OrderMapper;
import com.dl.task.dao.PayMentMapper;
import com.dl.task.dto.SurplusPaymentCallbackDTO;
import com.dl.task.model.Order;
import com.dl.task.model.PayLog;
import com.dl.task.model.PayMent;
import com.dl.task.param.SurplusPayParam;
import com.dl.task.param.UserBonusParam;

@Service
@Slf4j
public class PayMentService extends AbstractService<PayMent> {
	private final static Logger logger = LoggerFactory.getLogger(PayMentService.class);
	
    @Resource
    private PayMentMapper payMentMapper;
    
    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private PayLogService payLogService;
    
    @Resource
    private UserBonusService userBonusService;
    
    @Resource
    private UserAccountService userAccountService;
    
    @Resource
    private OrderService  orderService;

    /**
     * 处理支付超时订单
     */
    public void dealBeyondPayTimeOrderOut() {
		logger.info("开始执行混合支付超时订单任务");
		List<Order> orderList = orderMapper.queryOrderListBySelective(DateUtil.getCurrentTimeLong());
    	
    	logger.info("混合支付超时订单数："+orderList.size());
    	if(orderList.size() == 0) {
    		logger.info("没有混合支付超时订单,定时任务结束");
    		return;
    	}
    	
    	for(Order or:orderList) {
    		this.dealBeyondPayTimeOrder(or);
    	}
		log.info("结束执行支混合付超时订单任务");
    }
    
    
    /**
     * 处理支付超时订单
     */
    @Transactional(value="transactionManager1")
    public void dealBeyondPayTimeOrder(Order or) {
    	if(or.getSurplus().compareTo(BigDecimal.ZERO) > 0) {
	    	SurplusPayParam surplusPayParam = new SurplusPayParam();
	    	surplusPayParam.setOrderSn(or.getOrderSn());
	    	BaseResult<SurplusPaymentCallbackDTO> rollRst = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
	    	if(rollRst.getCode() != 0) {
	    		log.error("支付超时订单回滚用户余额异常,code="+rollRst.getCode()+"  msg:"+rollRst.getMsg()+" 订单号："+or.getOrderSn());
	    		return;
	    	}else {
	    		log.info(JSON.toJSONString("用户"+or.getUserId()+"超时支付订单"+or.getOrderSn()+"已回滚账户余额"));
	    	} 
    	}
    	
    	Integer userBonusId = or.getUserBonusId();
    	if(null != userBonusId) {
    		UserBonusParam userbonusParam = new UserBonusParam();
    		userbonusParam.setUserBonusId(userBonusId);
    		userbonusParam.setOrderSn(or.getOrderSn());
    		userBonusService.rollbackChangeUserAccountByCreateOrder(userbonusParam);
    	}
   	
    	int rst = orderMapper.updateOrderStatus0To8(or.getOrderSn(), DateUtil.getCurrentTimeLong());
    	if(rst <= 0) {
    		log.error("支付超时订单更新订单为出票失败 异常，订单号："+or.getOrderSn());
    		return;
    	}
    	
    	PayLog updatepayLog = new PayLog();
    	updatepayLog.setIsPaid(ProjectConstant.IS_PAID_FAILURE);
    	updatepayLog.setOrderSn(or.getOrderSn());
    	payLogService.updatePayLogByOrderSn(updatepayLog);
    }
}
