package com.dl.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LotteryPrintDTO {

	@ApiModelProperty("期次号")
	private String issue;
	@ApiModelProperty("期次号")
	private String playType;
	@ApiModelProperty("玩法")
	private String betType;
	@ApiModelProperty("倍数")
	private int times;
	@ApiModelProperty("金额")
	private double money;
	@ApiModelProperty("投注号码")
	private String stakes;
	@ApiModelProperty("彩票编号")
	private String ticketId;
}
