package com.dl.task.param;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

 
@Data
public class SupperLottoOrderParam {
	
    @ApiModelProperty(value = "奖金")
    private BigDecimal winningMoney;
    
    @ApiModelProperty(value = "中奖状态;4-未中奖5-已中奖")
    private Integer orderStatus;
    
    @ApiModelProperty(value = "订单编号")
    private String orderSn;
    
    @ApiModelProperty(value = "超级大奖级别")
    private Integer maxLevel;
    
}
