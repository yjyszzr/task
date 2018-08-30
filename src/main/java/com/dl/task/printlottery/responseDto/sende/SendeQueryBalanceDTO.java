package com.dl.task.printlottery.responseDto.sende;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class SendeQueryBalanceDTO {

	@ApiModelProperty(value = " ", required = true)
	public String lotteryCode;
	
	@ApiModelProperty(value = "", required = true)
	public String pwd;
	
	@ApiModelProperty(value = "", required = true)
	public String betSoruce;
	
	@ApiModelProperty(value = "卡号", required = true)
	public String cardCode;
	
	@ApiModelProperty(value = "结果", required = true)
	public String resultCode;
	
	@ApiModelProperty(value = "签名", required = true)
	public String key;
	
	@ApiModelProperty(value = "内容", required = true)
	public	SendeBalanceMessageDTO message;
	
	@Data
	public static class SendeBalanceMessageDTO {
		
		@ApiModelProperty(value = "充值金额", required = true)
		public Double prestore;
		
		@ApiModelProperty(value = "销量", required = true)
		public Double amount;
		
		@ApiModelProperty(value = "余额", required = true)
		public Double balance;
		
		@ApiModelProperty(value = "派奖金额", required = true)
		public Double bonus;
		
	} 
}
