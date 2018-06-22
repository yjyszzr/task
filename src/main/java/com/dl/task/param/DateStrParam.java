package com.dl.task.param;

import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 日期字符串
 *
 * @author zhangzirong
 */
@Data
public class DateStrParam {
	
    @ApiModelProperty(value = "当前日期字符串：格式2018-3-5")
    @NotBlank(message = "当前日期不能为空")
    private String dateStr;
}
