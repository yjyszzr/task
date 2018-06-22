package com.dl.task.dto;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderInfoAndDetailDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("订单")
    private OrderInfoDTO orderInfoDTO;

    @ApiModelProperty("订单详情")
    private List<OrderDetailDataDTO> orderDetailDataDTOs;
}
