package com.dl.task.param;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderWithUserParam implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "空字符串")
	public String str;
}
