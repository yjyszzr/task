package com.dl.task.enums;
/**
 * 报警选项
 * @author Administrator
 *
 */
public enum AlarmTaskEnum {
	ALARM_TX_BALLANCE_XIANFENG("10000","报警提现先锋支付余额"),
	ALARM_LOTTERY_BALLANCE_HENAN("10001","报警出票河南余额"),
	ALARM_LOTTERY_BALLANCE_XIAN("10002",Boolean.FALSE,"报警出票西安余额"),
	ALARM_LOTTERY_BALLANCE_CAIXIAOMI("10003",Boolean.FALSE,"报警出票彩小秘余额"),
	ALARM_LOTTERY_BALLANCE_WEICAISHIDAI("10004","报警出票微彩时代余额"),
	ALARM_LOTTERY_BALLANCE_SENDE("10005","报警出票森德余额");
	
	private String alarmCode;
	
	private Boolean isOk;
	
	private String alarmDesc;
	
	private AlarmTaskEnum(String alarmCode,Boolean isOk,String alarmDesc){
		this.alarmCode=alarmCode;
		this.isOk =isOk;
		this.alarmDesc=alarmDesc;
	}
	private AlarmTaskEnum(String alarmCode,String alarmDesc){
		this.alarmCode=alarmCode;
		this.isOk =Boolean.TRUE;
		this.alarmDesc=alarmDesc;
	}
	public static AlarmTaskEnum getAlarmTaskEnum(String alarmCode){
		for(AlarmTaskEnum alarm:AlarmTaskEnum.values()){
			if(alarm.getAlarmCode().equals(alarmCode)){
				return alarm;
			}
		}
		return null;
	}
	public String getAlarmCode() {
		return alarmCode;
	}
	public Boolean getIsOk() {
		return isOk;
	}
	public String getAlarmDesc() {
		return alarmDesc;
	}
}
