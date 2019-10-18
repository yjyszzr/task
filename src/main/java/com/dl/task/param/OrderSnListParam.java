package com.dl.task.param;

import java.util.List;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderSnListParam {
	@ApiModelProperty("已派奖订单号集合")
	private List<String> orderSnlist;
}
