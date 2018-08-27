package com.dl.task.printlottery.requestDto.sende;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BetContentSDParam {
	
	@ApiModelProperty(value = "投注内容", required = true)
	private List<MatchContentSDParam> matchContent;
	
	@ApiModelProperty(value = "倍投值", required = true)
    private Integer multiple;
	
	@ApiModelProperty(value = " ", required = true)
	private String orderStatus ;
	
	@ApiModelProperty(value = "订单时间yyyy-MM-dd HH:mm:ss", required = true)
	private String orderTime;
	
	@ApiModelProperty(value = "自由过关", required = true)
	private String passItem ;
	
	@ApiModelProperty(value = "单关SINGLE/过关PASS", required = true)
	private String passMode ;
	
	@ApiModelProperty(value = "过关方式", required = true)
	private String passType ;
	
	@ApiModelProperty(value = "玩法", required = true)
	private String playType; 
	
	@ApiModelProperty(value = "订单金额", required = true)
	private Double schemeCost;
	
	@ApiModelProperty(value = "第三方票唯一序列号", required = true)
	private String ticketId ;
	
	@ApiModelProperty(value = "注数", required = true)
	private Integer units ;
	
	
	
	
	@Data
	public static class MatchContentSDParam {

		@ApiModelProperty(value = "可为空。体彩中心赛事唯一标识", required = true)
		private String matchKey ;
		
		@ApiModelProperty(value = "赛事唯一标识", required = true)
		private String matchNumber ;
		
		@ApiModelProperty(value = "玩法描述", required = true)
		private Map<String,String> value ;
	}
}