package com.dl.task.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderInfoDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("投注倍数")
	private Integer cathectic;
	
	@ApiModelProperty("过关方式")
	private String passType;
	
	@ApiModelProperty("彩票分类")
	private Integer lotteryClassifyId;
	
	@ApiModelProperty("彩票子分类")
	private Integer lotteryPlayClassifyId;
	
	@ApiModelProperty("玩法")
	private String playType;
}
