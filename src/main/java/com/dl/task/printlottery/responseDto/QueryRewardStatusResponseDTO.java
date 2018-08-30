package com.dl.task.printlottery.responseDto;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class QueryRewardStatusResponseDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private Boolean querySuccess;

	@ApiModelProperty(value = "返回码", required = true)
	private String retCode;

	@ApiModelProperty(value = "返回码描述信息", required = true)
	private String retDesc;

	@ApiModelProperty(value = "返回订单详情", required = true)
	private List<QueryRewardStatusOrderResponse> orders;

	@Data
	public static class QueryRewardStatusOrderResponse {
		private Boolean querySuccess = Boolean.FALSE;

		@ApiModelProperty(value = "商户订单号", required = true)
		private String ticketId;
		@ApiModelProperty(value = "中奖状态 0: 成功 8:已兑奖", required = true)
		private Integer errorCode;
	}
}
