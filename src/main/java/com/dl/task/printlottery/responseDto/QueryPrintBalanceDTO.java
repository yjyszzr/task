package com.dl.task.printlottery.responseDto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class QueryPrintBalanceDTO {
	private Boolean querySuccess;

	@ApiModelProperty(value = "返回码", required = true)
    private String retCode;
	
	@ApiModelProperty(value = "返回码描述信息", required = true)
    private String retDesc;
	private Long balance;
}
