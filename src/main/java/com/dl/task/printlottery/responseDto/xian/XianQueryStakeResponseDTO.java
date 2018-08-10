package com.dl.task.printlottery.responseDto.xian;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class XianQueryStakeResponseDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "代理商编号", required = true)
    private String merchant;
	
	@ApiModelProperty(value = "版本号", required = true)
    private String version;
	
	@ApiModelProperty(value = "时间戳", required = true)
    private String timestamp;
	
	@ApiModelProperty(value = "返回码", required = true)
    private String retCode;
	
	@ApiModelProperty(value = "返回码描述信息", required = true)
    private String retDesc;
	
	
	@ApiModelProperty(value = "返回订单详情", required = true)
    private List<XianQueryStakeOrderResponse> orders;
	
	@Data
	public static class XianQueryStakeOrderResponse {
		
		@ApiModelProperty(value = "返回码 0-成功 4-票不存在", required = true)
	    private Integer status;
		
		@ApiModelProperty(value = "订单id", required = true)
	    private String orderId;
		
		@ApiModelProperty(value = "商户订单号", required = true)
	    private String ticketId;
		
		@ApiModelProperty(value = "中心平台订单编号", required = true)
	    private String platformId;
		
		@ApiModelProperty(value = "订单状态 8-出票中 16-成功 17-失败", required = true)
	    private Integer printStatus;
		
		@ApiModelProperty(value = "赔率数据", required = true)
	    private String sp;
		
		@ApiModelProperty(value = "票面上的票号", required = true)
	    private String printNo;
		
		@ApiModelProperty(value = "票面上的出票时间", required = true)
	    private String printTime;

		@ApiModelProperty(value = "中奖状态0未开奖,1中奖,2未中奖", required = true)
	    private Integer prizeStatus;
		@ApiModelProperty(value = "格式为:玩法|场次|结果@赔率.多个结果间;分隔.串关的结果,分隔", required = true)
	    private String prizeNo;
		@ApiModelProperty(value = "中奖的注数", required = true)
	    private Integer prizeCount;
		@ApiModelProperty(value = "中奖金额 分为单位", required = true)
	    private Integer prizeMoney;
		@ApiModelProperty(value = "结算状态", required = true)
	    private Integer settleStatus;
	}
}
