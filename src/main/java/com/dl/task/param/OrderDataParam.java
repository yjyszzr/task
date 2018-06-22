package com.dl.task.param;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderDataParam implements Serializable{

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "订单编号")
	public String orderSn;
	
	@ApiModelProperty(value = "中奖金额")
	public BigDecimal realRewardMoney;
	
	@ApiModelProperty(value = "订单状态:4-未中将5-已中奖")
	public Integer orderStatus;
}
