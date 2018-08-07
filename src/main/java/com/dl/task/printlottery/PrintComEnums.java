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
	private String printChannelName;
	
	private PrintComEnums(Integer printChannelId,String printChannelName){
		this.printChannelId=printChannelId;
		this.printChannelName=printChannelName;
	}
	public Integer getPrintChannelId(){
		return this.printChannelId;
	}
	public String getPrintChannelName(){
		return this.printChannelName;
	}
}
