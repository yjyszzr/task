package com.dl.task.printlottery.requestDto.sende;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class QueryStakeSDParam {
	
	@ApiModelProperty(value = "内容", required = true)
	public String message;
}
