package com.dl.task.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderDetailDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "彩种id")
    private String lotteryClassifyId;
	
	@ApiModelProperty(value = "彩种分类id")
    private String lotteryPlayClassifyId;
	
	@ApiModelProperty(value = "彩种名称")
    private String lotteryClassifyName;
	
	@ApiModelProperty(value = "彩种图片")
    private String lotteryClassifyImg;
	
	@ApiModelProperty(value = "实际消费金额")
    private String moneyPaid;
	
	@ApiModelProperty(value = "处理结果")
    private String processResult;
	
	@ApiModelProperty(value = "处理状态描述")
    private String processStatusDesc;
	
	@ApiModelProperty(value = "订单状态")
    private String orderStatus;
	
	@ApiModelProperty(value = "订单状态描述")
    private String orderStatusDesc;
	
	@ApiModelProperty(value = "赛事列表")
    private List<MatchInfo> matchInfos;
	
	@ApiModelProperty(value = "过关方式")
    private String passType;
	
	@ApiModelProperty(value = "投注倍数")
    private Integer cathectic;
	
	@ApiModelProperty(value = "投注注数")
	private Integer betNum;
	
	@ApiModelProperty(value = "方案编号")
    private String programmeSn;
	
	@ApiModelProperty(value = "创建时间")
    private String createTime;
	
	@ApiModelProperty(value = "接单时间")
    private String acceptTime;
	
	@ApiModelProperty(value = "出票时间")
    private String ticketTime;
	
	@ApiModelProperty(value = "预测奖金")
    private String forecastMoney;
	
	@ApiModelProperty(value = "玩法")
    private String playType;
	
	@ApiModelProperty(value = "订单编号")
    private String orderSn;
	
	@ApiModelProperty(value="支付名称")
	private String payName;
	
	@ApiModelProperty(value="彩票购买总金额")
	private String ticketAmount;
	
	@ApiModelProperty(value="余额支付总金额")
	private String surplus;

	@ApiModelProperty(value="第三方支付金额")
	private String thirdPartyPaid;

	@ApiModelProperty(value="红包金额")
	private String bonus;

	@ApiModelProperty(value="可提现金额")
	private String userSurplus;

	@ApiModelProperty(value="不可提现金额")
	private String userSurplusLimit;
	
	@ApiModelProperty(value="0足彩七种玩法1冠军2冠亚军")
	private int detailType=0;
	
	@ApiModelProperty("跳转地址")
	private String redirectUrl;
	
	@Data
	public static class MatchInfo {
		
		@ApiModelProperty(value = "场次")
	    private String changci;
		
		@ApiModelProperty(value = "赛事")
	    private String match;
		
		@ApiModelProperty(value = "玩法")
	    private String playType;
		
		@ApiModelProperty(value = "是否加胆")
		private String isDan;
		
		@ApiModelProperty(value = "投注和赛果列表")
	    private List<CathecticResult> cathecticResults;
	}
	
	@Data
	public static class CathecticResult {
		
		@ApiModelProperty(value = "投注列表")
	    private List<Cathectic> cathectics;
		
		@ApiModelProperty(value = "比赛结果")
	    private String matchResult;
		
		@ApiModelProperty(value = "玩法")
	    private String playType;
	}
	
	@Data
	public static class Cathectic {
		
		@ApiModelProperty(value = "投注")
	    private String cathectic;
		
		@ApiModelProperty(value = "是否猜中 0-没猜中 1-猜中")
	    private String isGuess;
	}
}
