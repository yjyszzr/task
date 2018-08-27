package com.dl.task.printlottery.responseDto.sende;

import java.util.List;
import java.util.Map;

import com.dl.task.printlottery.requestDto.sende.QueryStakeSDParam;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class SendeResultToStakeDTO {
	
	 

	@ApiModelProperty(value = "彩种", required = true)
	public String lotteryCode;
	
	@ApiModelProperty(value = "返回结果", required = true)
	public String resultCode;
	
	@ApiModelProperty(value = "返回结果", required = true)
	public String pwd;
	
	@ApiModelProperty(value = "返回结果", required = true)
	public String key;
	
	@ApiModelProperty(value = "内容", required = true)
	public List<SendeResultMessageDTO> message;
	
	 
}
