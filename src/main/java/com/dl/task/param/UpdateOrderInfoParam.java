package com.dl.task.param;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UpdateOrderInfoParam implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty("订单编码")
	private String orderSn;

	@ApiModelProperty("可提现余额支付金额")
	private BigDecimal userSurplus;
	
	@ApiModelProperty("不可提现余额支付金额")
	private BigDecimal userSurplusLimit;
	
	@ApiModelProperty("订单支付时间")
	private Integer payTime;
	
	@ApiModelProperty("支付状态 1-已支付")
	private Integer payStatus;
	
	@ApiModelProperty("订单状态 1-未出票")
	private Integer orderStatus;
	
	@ApiModelProperty("支付id")
	private Integer payId;
	
	@ApiModelProperty("支付代码")
	private String payCode;
	
	@ApiModelProperty("支付名称")
	private String payName;
	
	@ApiModelProperty("支付编码")
	private String paySn;
	
	@ApiModelProperty("接单时间")
	private Integer acceptTime;
	
	@ApiModelProperty("出票时间")
	private Integer ticketTime;
	
}
