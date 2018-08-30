package com.dl.task.enums;
/**
 * 出票状态 0-待出票 1-已出票 2-出票失败 3-出票中
 * @author Administrator
 *
 */
public enum PrintLotteryStatusEnum {
	INIT(0,"待出票"),
	SUCCESS(1,"出票失败"),
	FAIL(2,"出票失败"),
	DOING(3,"出票中");
	private Integer status;
	private String desc;
	private PrintLotteryStatusEnum(Integer status,String desc){
		this.status=status;
		this.desc=desc;
	}
	public Integer getStatus() {
		return status;
	}
	public String getDesc() {
		return desc;
	}
}
