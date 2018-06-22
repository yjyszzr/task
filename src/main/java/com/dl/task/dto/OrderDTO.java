package com.dl.task.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

    @ApiModelProperty("订单id")
    private Integer orderId;

    @ApiModelProperty("订单编号")
    private String orderSn;

    @ApiModelProperty("买家id")
    private Integer userId;

    @ApiModelProperty("订单状态")
    private Integer orderStatus;

    @ApiModelProperty("支付状态")
    private Integer payStatus;

    @ApiModelProperty("支付id")
    private Integer payId;

    @ApiModelProperty("支付代码")
    private String payCode;

    @ApiModelProperty("支付名称")
    private String payName;

    @ApiModelProperty("支付编码")
    private String paySn;

    @ApiModelProperty("订单实付金额")
    private BigDecimal moneyPaid;

    @ApiModelProperty("彩票总金额")
    private BigDecimal ticketAmount;

    @ApiModelProperty("余额支付")
    private BigDecimal surplus;

    @ApiModelProperty("可提现余额支付")
    private BigDecimal userSurplus;

    @ApiModelProperty("不可提现余额支付")
    private BigDecimal userSurplusLimit;
    
    @ApiModelProperty("第三方支付金额")
    private BigDecimal thirdPartyPaid;

    @ApiModelProperty("用户红包id")
    private Integer userBonusId;

    @ApiModelProperty("用户红包金额")
    private BigDecimal bonus;

    @ApiModelProperty("订单来源")
    private String orderFrom;

    @ApiModelProperty("订单生成时间")
    private Integer addTime;

    @ApiModelProperty("订单支付时间")
    private Integer payTime;
}
