package com.dl.task.printlottery.responseDto.caixiaomi;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class CaiXiaoMiQueryRewardDTO implements Serializable {

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
	private List<CaixiaoMiQueryRewardOrderResponse> orders;

	@Data
	public static class CaixiaoMiQueryRewardOrderResponse {
		@ApiModelProperty(value = "中心平台订单编号", required = true)
		private String platformId;

		@ApiModelProperty(value = "商户订单号", required = true)
		private String ticketId;

		@ApiModelProperty(value = "票面上的票号 ", required = true)
		private String printNo;

		@ApiModelProperty(value = "返回码 0:成功兑奖8:已兑奖9:票不存在10:兑奖中 ", required = true)
		private Integer awardCode;

		@ApiModelProperty(value = "中奖时间", required = true)
		private String awardTime;

		@ApiModelProperty(value = "中奖奖金(按照分来计算)", required = true)
		private Integer awardMoney;

	}
}
