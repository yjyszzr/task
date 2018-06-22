package com.dl.task.service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.shop.payment.dto.PaymentDTO;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.param.RollbackOrderAmountParam;
import com.dl.shop.payment.param.RollbackThirdOrderAmountParam;
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.OrderMapper;
import com.dl.task.dao.PayMentMapper;
import com.dl.task.dto.SurplusPaymentCallbackDTO;
import com.dl.task.model.Order;
import com.dl.task.model.PayLog;
import com.dl.task.model.PayMent;
import com.dl.task.param.SurplusPayParam;
import com.dl.task.param.UpdateOrderInfoParam;
import com.dl.task.param.UserBonusParam;

import lombok.extern.slf4j.Slf4j;

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
    @Transactional
    public void dealBeyondPayTimeOrder(Order or) {
    	if(or.getSurplus().compareTo(BigDecimal.ZERO) > 0) {
	    	SurplusPayParam surplusPayParam = new SurplusPayParam();
	    	surplusPayParam.setOrderSn(or.getOrderSn());
	    	BaseResult<SurplusPaymentCallbackDTO> rollRst = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
	    	if(rollRst.getCode() != 0) {
	    		log.error(rollRst.getMsg());
	    		return;
	    	}
	    	
	    	if(rollRst.getCode() != 0) {
	    		log.error("支付超时订单回滚用户余额异常,code="+rollRst.getCode()+"  msg:"+rollRst.getMsg()+" 订单号："+or.getOrderSn());
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
   	
    	UpdateOrderInfoParam updateOrderInfoParam = new UpdateOrderInfoParam();
    	updateOrderInfoParam.setOrderSn(or.getOrderSn());
    	updateOrderInfoParam.setOrderStatus(8);//订单失败
    	updateOrderInfoParam.setPayStatus(2);//支付失败
    	updateOrderInfoParam.setPayTime(DateUtil.getCurrentTimeLong());
    	BaseResult<String> updateRst = orderService.updateOrderInfoStatus(updateOrderInfoParam);
    	if(updateRst.getCode() != 0) {
    		log.error("支付超时订单更新订单为出票失败 异常，返回，code="+updateRst.getCode()+"  msg:"+updateRst.getMsg()+" 订单号："+or.getOrderSn());
    		return;
    	}
    	
    	PayLog updatepayLog = new PayLog();
    	updatepayLog.setIsPaid(ProjectConstant.IS_PAID_FAILURE);
    	updatepayLog.setOrderSn(or.getOrderSn());
    	payLogService.updatePayLogByOrderSn(updatepayLog);
    }
    	

}