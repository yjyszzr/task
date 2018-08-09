package com.dl.task.printlottery.responseDto;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

import com.dl.task.enums.ThirdRewardStatusEnum;

@Data
public class QueryRewardResponseDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Boolean querySuccess;

	@ApiModelProperty(value = "返回码", required = true)
    private String retCode;
	
	@ApiModelProperty(value = "返回码描述信息", required = true)
    private String retDesc;
	
	@ApiModelProperty(value = "返回订单详情", required = true)
    private List<QueryRewardOrderResponse> orders;
	
	@Data
	public static class QueryRewardOrderResponse {
		private Boolean querySuccess = Boolean.FALSE;
		@ApiModelProperty(value = "第三方出奖状态 1:第三方未进行出奖 2:第三方出奖中 3:第三方已出奖 4:第三方已结算", required = true)
		private ThirdRewardStatusEnum thirdRewardStatusEnum;
		
		@ApiModelProperty(value = "商户订单号", required = true)
	    private String ticketId;
		@ApiModelProperty(value = "中奖金额 分为单位", required = true)
	    private Integer prizeMoney;
	}
}
