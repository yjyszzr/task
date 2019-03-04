package com.dl.task.param;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

 
@Data
public class SupperLottoOrderDetailParam {
	
	@ApiModelProperty(value = "详情Id")
	private Integer orderDetailId;
	
    @ApiModelProperty(value = "中奖奖金")
    private BigDecimal moneyPrize;
    
    @ApiModelProperty(value = "中奖状态;1-未中奖,2-已中奖")
    private Integer ticketStatus;
    
    @ApiModelProperty(value = "中奖状态;0-未猜中 1-已猜中")
    private Integer isGuess;
    
    @ApiModelProperty(value = "中奖级别")
    private String levelPrize;
}
