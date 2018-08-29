package com.dl.task.printlottery;
/**
 * 出票公司枚举
 * @author Administrator
 *
 */
public enum PrintComEnums {
	
	HENAN(1,"河南"),
	XIAN(2,"西安"),
	CAIXIAOMI(3,"彩小秘"),
	WEICAISHIDAI(4,"微彩时代");
	
	private Integer printChannelId;
	private Integer rewardType;//1批量按票出奖2 按照期次出奖
	private String printChannelName;
	
	private PrintComEnums(Integer printChannelId,String printChannelName){
		this.printChannelId=printChannelId;
		this.printChannelName=printChannelName;
		this.rewardType=1;
	}
	private PrintComEnums(Integer printChannelId,String printChannelName,Integer rewardType){
		this.printChannelId=printChannelId;
		this.printChannelName=printChannelName;
		this.rewardType=rewardType;
	}
	public Integer getPrintChannelId(){
		return this.printChannelId;
	}
	public String getPrintChannelName(){
		return this.printChannelName;
	}
	public Integer getRewardType() {
		return rewardType;
	}
}
