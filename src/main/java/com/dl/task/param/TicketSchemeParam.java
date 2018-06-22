package com.dl.task.param;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TicketSchemeParam implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "方案编号", required = true)
    private String programmeSn;
	
	@ApiModelProperty(value = "订单编号", required = true)
    private String OrderSn;
}
