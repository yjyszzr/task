package com.dl.task.enums;

public enum ThirdRewardStatusEnum {
	REWARD_INIT(1,"未出奖"),
	DOING(2,"出奖中"),
	REWARD_OVER(3,"已出奖"),
	BALANCE_OVER(4,"已结算");
	private Integer status;
	private String desc;
	private ThirdRewardStatusEnum(Integer status,String desc){
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
