package com.dl.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IssueDTO {
	
	@ApiModelProperty(value = "期次")
    private String issue;

}
