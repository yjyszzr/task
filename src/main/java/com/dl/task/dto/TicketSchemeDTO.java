package com.dl.task.dto;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TicketSchemeDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "方案编号")
    private String programmeSn;
	
	@ApiModelProperty(value = "方案编号")
    private List<TicketSchemeDetailDTO> ticketSchemeDetailDTOs;
	
	@Data
	public static class TicketSchemeDetailDTO {
		
		@ApiModelProperty(value = "序号")
	    private String number;
		
		@ApiModelProperty(value = "投注内容")
	    private String tickeContent;
		
		@ApiModelProperty(value = "过关方式")
	    private String passType;
		
		@ApiModelProperty(value = "倍数")
	    private String multiple;
		
		@ApiModelProperty("出票状态， 0-待出票 1-已出票 2-出票失败 3-出票中")
		private Integer status;
	}
}
