package com.dl.task.printlottery.responseDto.weicaishidai;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import lombok.Data;

@Data
public class WeiCaiShiDaiQueryBalanceDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "返回订单详情", required = true)
	private WeiCaiShiDaiToStakeRetCode err;
	private String uuid;
	@ApiModelProperty(value = "账户信息", required = true)
    private WeiCaiShiDaiQueryBalanceResponse account;
	
	@Data
	public static class WeiCaiShiDaiQueryBalanceResponse {
		
		@ApiModelProperty(value = "商户余额 单位元", required = true)
	    private String balance;
		
		private String uuid;
	}
}
