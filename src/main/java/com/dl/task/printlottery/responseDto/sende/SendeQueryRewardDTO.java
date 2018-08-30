package com.dl.task.printlottery.responseDto.sende;

import java.util.List;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class SendeQueryRewardDTO {
	
	 

	@ApiModelProperty(value = "彩种", required = true)
	public String lotteryCode;
	
	@ApiModelProperty(value = "返回结果", required = true)
	public String resultCode;
	
	@ApiModelProperty(value = "", required = true)
	public String pwd;
	
	@ApiModelProperty(value = "", required = true)
	public String key;
	
	@ApiModelProperty(value = "内容", required = true)
	public List<SendeBonusMessageDTO> message;
	
	@Data
	public static class SendeBonusMessageDTO {
		
		@ApiModelProperty(value = "税后金额", required = true)
		public Double afterTaxBonus;
		
		@ApiModelProperty(value = "税前金额", required = true)
		public Double bonus;
		
		@ApiModelProperty(value = "系统订单号", required = true)
		public String orderNumber;
		
		@ApiModelProperty(value = "返回结果DISTRIBUTE已派奖,NOT_DISTRIBUTE等待派奖, ORDER_NOT_EXIT_ERROR 订单不存在", required = true)
		public String result;
		
		@ApiModelProperty(value = "", required = true)
		public String ticketId;
	} 
}
