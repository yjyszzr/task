package com.dl.task.printlottery.responseDto.henan;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class HenanQueryRewardDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "代理商编号", required = true)
    private String merchant;
	
	@ApiModelProperty(value = "版本号", required = true)
    private String version;
	
	@ApiModelProperty(value = "时间戳", required = true)
    private String timestamp;
	
	@ApiModelProperty(value = "返回码", required = true)
    private String retCode;
	
	@ApiModelProperty(value = "返回码描述信息", required = true)
    private String retDesc;
	
	
	@ApiModelProperty(value = "返回订单详情", required = true)
    private List<HenanQueryRewardOrderResponse> orders;
	
	@Data
	public static class HenanQueryRewardOrderResponse {
		@ApiModelProperty(value = "中心平台订单编号", required = true)
		private String platformId;
		
		@ApiModelProperty(value = "商户订单号", required = true)
		private String ticketId;
		
		@ApiModelProperty(value = "返回码 4:未开奖8:小奖 9:大奖 10:未中奖 32订单不存在 33订单出票失败，出票失败的不算奖 ", required = true)
	    private Integer status;
		
		@ApiModelProperty(value = "状态描述", required = true)
	    private String statusDesc;
		
		@ApiModelProperty(value = "税前奖金", required = true)
	    private Integer preTax;
		@ApiModelProperty(value = "税金", required = true)
	    private Integer tax;
	}
}
