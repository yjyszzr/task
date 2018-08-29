package com.dl.task.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderWithUserDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "用户id")
	public Integer userId;
	
	@ApiModelProperty(value = "中奖金额")
	public BigDecimal realRewardMoney;
	
	@ApiModelProperty(value = "订单号")
	public String orderSn;

	@ApiModelProperty(value = "投注金额")
	private String betMoney;

	@ApiModelProperty(value = "投注时间")
	private int betTime;
	@ApiModelProperty(value = "彩种类型")
	private Integer lotteryClassifyId;
}
