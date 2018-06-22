package com.dl.task.param;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LotteryPrintRewardParam implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("期次")
	private String issue;
}
