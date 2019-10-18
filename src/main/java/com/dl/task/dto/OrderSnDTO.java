package com.dl.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderSnDTO {
	@ApiModelProperty(value = "订单号")
    private String orderSn;
}
