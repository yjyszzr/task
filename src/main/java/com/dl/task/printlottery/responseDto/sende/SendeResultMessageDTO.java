package com.dl.task.printlottery.responseDto.sende;

import java.util.List;
import java.util.Map;

 

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class SendeResultMessageDTO {

	@ApiModelProperty(value = "返回结果", required = true)
    public String result;
	
	@ApiModelProperty(value = "出票公司票号", required = true)
    public String orderNumber;
	
	@ApiModelProperty(value = "出票id", required = true)
	public String ticketId;
	
	@ApiModelProperty(value = "出票成功时间", required = true)
	public String successTime;
	
	@ApiModelProperty(value = "出票时赔率", required = true)
	public SpMap odds;
	
	@ApiModelProperty(value = "出票成功时间", required = true)
	public String playType;
	
	@Data
	public static class SpMap{
		@ApiModelProperty(value = "", required = true)
		public OddsDTO spMap;
	}
	@Data
	public static class OddsDTO{
		@ApiModelProperty(value = "", required = true)
		public List<MatchNumber> matchNumber;
	}
	
	@Data
	public static class MatchNumber{
		@ApiModelProperty(value = "", required = true)
		public String matchNumber;
		@ApiModelProperty(value = "", required = true)
		public Map<String,String> value;
		
	}
	
}
