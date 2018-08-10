package com.dl.task.printlottery.responseDto.weicaishidai;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WeiCaiShiDaiDlToStakeDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "返回订单详情", required = true)
	private WeiCaiShiDaiToStakeRetCode err;
	
	@ApiModelProperty(value = "返回订单详情", required = true)
    private List<WeiCaiShiDaiBackOrderDetail> tickets;
	
	@Data
	public static class WeiCaiShiDaiBackOrderDetail {
		
		@ApiModelProperty(value = "商户订单号", required = true)
	    private String orderId;
		
		@ApiModelProperty(value = "中心平台订单编号", required = true)
	    private String code;
		
		@ApiModelProperty(value = "处理结果", required = true)
	    private String message;
	}
}
