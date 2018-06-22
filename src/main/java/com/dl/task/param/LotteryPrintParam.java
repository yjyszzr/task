package com.dl.task.param;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LotteryPrintParam implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty("订单编号")
	private String orderSn;

	@ApiModelProperty("接单时间")
	private int acceptTime;
	
	@ApiModelProperty("出票时间")
	private int ticketTime;
	
	@ApiModelProperty("出票返回的赔率")
	private String printSp;
}
