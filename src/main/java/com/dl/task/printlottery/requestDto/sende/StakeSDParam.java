package com.dl.task.printlottery.requestDto.sende;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StakeSDParam implements Serializable{
	
	 
	@ApiModelProperty(value = "订单详情", required = true)
    public List<BetContentSDParam> betContent;
	
	
}
