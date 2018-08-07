package com.dl.task.printlottery.responseDto;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

import com.dl.task.enums.PrintLotteryStatusEnum;

@Data
public class QueryStakeResponseDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Boolean querySuccess;

	@ApiModelProperty(value = "返回码", required = true)
    private String retCode;
	
	@ApiModelProperty(value = "返回码描述信息", required = true)
    private String retDesc;
	
	@ApiModelProperty(value = "返回订单详情", required = true)
    private List<QueryStakeOrderResponse> orders;
	
	@Data
	public static class QueryStakeOrderResponse {
		private Boolean querySuccess = Boolean.FALSE;
		@ApiModelProperty(value = "彩小秘出票状态 0-待出票 1-已出票 2-出票失败 3-出票中", required = true)
		private PrintLotteryStatusEnum statusEnum;

		@ApiModelProperty(value = "订单id", required = true)
	    private String orderId;
		
		@ApiModelProperty(value = "商户订单号", required = true)
	    private String ticketId;
		
		@ApiModelProperty(value = "中心平台订单编号", required = true)
	    private String platformId;
		
		@ApiModelProperty(value = "第三方出票状态 8出票中16成功17失败", required = true)
	    private Integer printStatus;
		
		@ApiModelProperty(value = "赔率数据", required = true)
	    private String sp;
		
		@ApiModelProperty(value = "票面上的票号", required = true)
	    private String printNo;
		
		@ApiModelProperty(value = "票面上的出票时间", required = true)
	    private String printTime;
		@ApiModelProperty(value = "票面上的出票时间", required = true)
	    private Date printTimeDate;
	}
}
