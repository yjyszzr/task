package com.dl.task.param;


import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderQueryParam {
	
	@ApiModelProperty("订单状态 -1-所有订单 3-待开奖 5-已中奖")
	@NotNull(message ="订单状态 不能为空")
	private Integer orderStatus;
	
	@ApiModelProperty("支付状态  0待支付1已支付")
	@NotNull(message ="支付状态 不能为空")
	private Integer payStatus;
	
}
