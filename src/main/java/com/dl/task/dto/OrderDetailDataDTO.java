package com.dl.task.dto;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderDetailDataDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("彩票信息（投注内容）")
    private String ticketData;
	
	@ApiModelProperty("场次")
    private String changci;
	
	@ApiModelProperty("是否有胆 0-否 1-是")
    private Integer isDan;
	
	@ApiModelProperty("彩票种类")
    private Integer lotteryClassifyId;
	
	@ApiModelProperty("彩票子分类")
    private Integer lotteryPlayClassifyId;
	
	@ApiModelProperty("赛事id")
	private Integer matchId;
	
	@ApiModelProperty("比赛双方球队")
	private String matchTeam;
	
	@ApiModelProperty("比赛时间")
	private Date matchTime;
	
	@ApiModelProperty("期次")
	private String issue;
}
