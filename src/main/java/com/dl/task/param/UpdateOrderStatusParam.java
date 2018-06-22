package com.dl.task.param;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UpdateOrderStatusParam implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty("订单编码")
	private List<String> orderSns;

	@ApiModelProperty("订单状态 ")
	private Integer orderStatus;
	
	@ApiModelProperty("接单时间")
	private Integer acceptTime;
	
	@ApiModelProperty("出票时间")
	private Integer ticketTime;
	
}
