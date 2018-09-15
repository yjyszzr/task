package com.dl.task.splittable;

import java.util.Date;

import lombok.Data;

@Data
public class SplitTableCommonDto {
	private String splitTableTimeUnit;//month or day
	private Integer splitTableTimeLimit;
	private Date splitTableTaskExecTime;
	private String execTableDateMonth;
	private String splitNewTableSuffix;//yyyyMM or yyyyMMdd
	private String execTableDateDay;
}
