package com.dl.task.printlottery.responseDto.weicaishidai;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WeiCaiShiDaiQueryStakeDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "返回订单详情", required = true)
	private WeiCaiShiDaiToStakeRetCode err;
	
	@ApiModelProperty(value = "返回订单详情", required = true)
    private List<WeiCaiShiDaiQueryStakeResponse> tickets;
	
	@Data
	public static class WeiCaiShiDaiQueryStakeResponse {
		
		@ApiModelProperty(value = "#订单编号", required = true)
	    private String orderId;
		
		@ApiModelProperty(value = "#出票的内部流水编号", required = true)
	    private String ticketId;
		
		@ApiModelProperty(value = "#彩票中心的出票票号", required = true)
	    private String ticketNumber;
		@ApiModelProperty(value = "#投注单，如果是竞彩会返回赔率", required = true)
		private String number;
		@ApiModelProperty(value = "#出票状态: 0 出票中 1出票成功 3出票失败", required = true)
		private String orderStatus;
		@ApiModelProperty(value = "#出票金额 单位为分", required = true)
		private String price;
		@ApiModelProperty(value = "#出票时间2016-01-01 00:01:02", required = true)
		private String printTime;
		@ApiModelProperty(value = "#商户号,", required = true)
		private String vendorId;
		@ApiModelProperty(value = "#中奖状态, 0未开奖 1未中奖 2已算奖", required = true)
		private String winStatus;
		@ApiModelProperty(value = "#税后中奖金额 单位元", required = true)
		private String actualPrize;
	}
}
