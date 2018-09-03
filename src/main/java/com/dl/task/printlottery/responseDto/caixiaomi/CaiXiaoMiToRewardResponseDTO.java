package com.dl.task.printlottery.responseDto.caixiaomi;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

import com.dl.task.enums.ThirdRewardStatusEnum;

import lombok.Data;

@Data
public class CaiXiaoMiToRewardResponseDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private Boolean querySuccess;

	@ApiModelProperty(value = "返回码", required = true)
	private String retCode;

	@ApiModelProperty(value = "返回码描述信息", required = true)
	private String retDesc;

	@ApiModelProperty(value = "返回订单详情", required = true)
	private List<CaiXiaoMiToRewardOrderResponse> orders;

	@Data
	public static class CaiXiaoMiToRewardOrderResponse {
		private Boolean querySuccess = Boolean.FALSE;

		@ApiModelProperty(value = "商户订单号", required = true)
		private String ticketId;
		
		@ApiModelProperty(value = "中奖状态 0: 成功 8:已兑奖", required = true)
		private Integer errorCode;
	}
}
