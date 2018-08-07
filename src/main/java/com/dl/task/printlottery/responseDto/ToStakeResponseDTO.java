package com.dl.task.printlottery.responseDto;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ToStakeResponseDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	/**
	 * 请求是否成功
	 */
	private Boolean retSucc=Boolean.FALSE;
	
	@ApiModelProperty(value = "返回码", required = true)
    private String retCode;
	
	@ApiModelProperty(value = "返回码描述信息", required = true)
    private String retDesc;
	
	@ApiModelProperty(value = "返回订单详情", required = true)
    private List<ToStakeBackOrderDetail> orders;
	
	@Data
	public static class ToStakeBackOrderDetail {
		
		@ApiModelProperty(value = "商户订单号", required = true)
	    private String ticketId;
		
		@ApiModelProperty(value = "中心平台订单编号", required = true)
	    private String platformId;
		
		@ApiModelProperty(value = "处理结果", required = true)
	    private Integer errorCode;
		
		@ApiModelProperty(value = "处理结果", required = true)
		private Integer errorDesc;
		private Boolean printLotteryDoing;
	}
}
