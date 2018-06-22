package com.dl.task.param;

import java.io.Serializable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderCondtionParam implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("订单状态 -1-所有订单 3-待开奖 5-已中奖")
	private Integer orderStatus;
	
	@ApiModelProperty("支付状态  0待支付1已支付")
	private Integer payStatus;
	
	@ApiModelProperty("时间戳:s")
	private Integer time;
	
}
