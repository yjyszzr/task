package com.dl.task.model;

import javax.persistence.*;

@Table(name = "dl_sys_alarm_task")
public class DLSysAlarmTask {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 报警编号:10000：先锋支付余额报警，10001：河南出票余额报警，10002:西安票余额报警，10003:彩小秘出票余额报警，10004:微彩时代出票余额报警
     */
    @Column(name = "alarm_code")
    private String alarmCode;
    /**
     * 值0 关闭 1打开
     */
    @Column(name = "alarm_name")
    private String alarmName;

    /**
     * 值0 关闭 1打开
     */
    @Column(name = "open_status")
    private Integer openStatus;

    /**
     * 配置的数字限制条件
     */
    @Column(name = "alarm_limit")
    private String alarmLimit;

    /**
     * 配置的运算限制条件:例如使用params_name字段中的一些组合运算条件，暂不实现
     */
    @Column(name = "alarm_condition")
    private String alarmCondition;

    /**
     * 发送方式默认SMS,配置项有 短信:SMS,钉钉:DINGDING,所有方式：ALL
     */
    @Column(name = "send_method")
    private String sendMethod;

    /**
     * 配置提供的参数名称列表
     */
    @Column(name = "params_name")
    private String paramsName;

    /**
     * 短信发送内容
     */
    @Column(name = "sms_send_content")
    private String smsSendContent;

    /**
     * 短信发送手机号
     */
    @Column(name = "sms_send_mobile")
    private String smsSendMobile;

    /**
     * 首次短信首次发送
     */
    @Column(name = "sms_first_alarm_time")
    private Integer smsFirstAlarmTime;

    /**
     * 间隔时间设置例如：600,600,600
     */
    @Column(name = "sms_alarm_time")
    private String smsAlarmTime;

    /**
     * 短信发送第几次 如果次数 大于配置间隔个数 则不发送
     */
    @Column(name = "sms_alarm_count")
    private Integer smsAlarmCount;

    /**
     * 钉钉发送地址
     */
    @Column(name = "dingding_url")
    private String dingdingUrl;

    /**
     * 钉钉发送@人手机号
     */
    @Column(name = "dingding_mobile")
    private String dingdingMobile;

    /**
     * 钉钉发送内容
     */
    @Column(name = "dingding_send_content")
    private String dingdingSendContent;

    /**
     * 首次短信首次发送
     */
    @Column(name = "dingding_first_alarm_time")
    private Integer dingdingFirstAlarmTime;

    /**
     * 间隔时间设置例如：600,600,600
     */
    @Column(name = "dingding_alarm_time")
    private String dingdingAlarmTime;

    /**
     * 钉钉发送第几次 如果次数 大于配置间隔个数 则不发送
     */
    @Column(name = "dingding_alarm_count")
    private Integer dingdingAlarmCount;

    /**
     * 获取ID
     *
     * @return id - ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置ID
     *
     * @param id ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

	/**
     * 获取报警编号:10000：先锋支付余额报警，10001：河南出票余额报警，10002:西安票余额报警，10003:彩小秘出票余额报警，10004:微彩时代出票余额报警
     *
     * @return alarm_code - 报警编号:10000：先锋支付余额报警，10001：河南出票余额报警，10002:西安票余额报警，10003:彩小秘出票余额报警，10004:微彩时代出票余额报警
     */
    public String getAlarmCode() {
        return alarmCode;
    }

    /**
     * 设置报警编号:10000：先锋支付余额报警，10001：河南出票余额报警，10002:西安票余额报警，10003:彩小秘出票余额报警，10004:微彩时代出票余额报警
     *
     * @param alarmCode 报警编号:10000：先锋支付余额报警，10001：河南出票余额报警，10002:西安票余额报警，10003:彩小秘出票余额报警，10004:微彩时代出票余额报警
     */
    public void setAlarmCode(String alarmCode) {
        this.alarmCode = alarmCode;
    }

    public String getAlarmName() {
		return alarmName;
	}

	public void setAlarmName(String alarmName) {
		this.alarmName = alarmName;
	}
    /**
     * 获取值0 关闭 1打开
     *
     * @return open_status - 值0 关闭 1打开
     */
    public Integer getOpenStatus() {
        return openStatus;
    }

    /**
     * 设置值0 关闭 1打开
     *
     * @param openStatus 值0 关闭 1打开
     */
    public void setOpenStatus(Integer openStatus) {
        this.openStatus = openStatus;
    }

    /**
     * 获取配置的数字限制条件
     *
     * @return alarm_limit - 配置的数字限制条件
     */
    public String getAlarmLimit() {
        return alarmLimit;
    }

    /**
     * 设置配置的数字限制条件
     *
     * @param alarmLimit 配置的数字限制条件
     */
    public void setAlarmLimit(String alarmLimit) {
        this.alarmLimit = alarmLimit;
    }

    /**
     * 获取配置的运算限制条件:例如使用params_name字段中的一些组合运算条件，暂不实现
     *
     * @return alarm_condition - 配置的运算限制条件:例如使用params_name字段中的一些组合运算条件，暂不实现
     */
    public String getAlarmCondition() {
        return alarmCondition;
    }

    /**
     * 设置配置的运算限制条件:例如使用params_name字段中的一些组合运算条件，暂不实现
     *
     * @param alarmCondition 配置的运算限制条件:例如使用params_name字段中的一些组合运算条件，暂不实现
     */
    public void setAlarmCondition(String alarmCondition) {
        this.alarmCondition = alarmCondition;
    }

    /**
     * 获取发送方式默认SMS,配置项有 短信:SMS,钉钉:DINGDING,所有方式：ALL
     *
     * @return send_method - 发送方式默认SMS,配置项有 短信:SMS,钉钉:DINGDING,所有方式：ALL
     */
    public String getSendMethod() {
        return sendMethod;
    }

    /**
     * 设置发送方式默认SMS,配置项有 短信:SMS,钉钉:DINGDING,所有方式：ALL
     *
     * @param sendMethod 发送方式默认SMS,配置项有 短信:SMS,钉钉:DINGDING,所有方式：ALL
     */
    public void setSendMethod(String sendMethod) {
        this.sendMethod = sendMethod;
    }

    /**
     * 获取配置提供的参数名称列表
     *
     * @return params_name - 配置提供的参数名称列表
     */
    public String getParamsName() {
        return paramsName;
    }

    /**
     * 设置配置提供的参数名称列表
     *
     * @param paramsName 配置提供的参数名称列表
     */
    public void setParamsName(String paramsName) {
        this.paramsName = paramsName;
    }

    /**
     * 获取短信发送内容
     *
     * @return sms_send_content - 短信发送内容
     */
    public String getSmsSendContent() {
        return smsSendContent;
    }

    /**
     * 设置短信发送内容
     *
     * @param smsSendContent 短信发送内容
     */
    public void setSmsSendContent(String smsSendContent) {
        this.smsSendContent = smsSendContent;
    }

    /**
     * 获取短信发送手机号
     *
     * @return sms_send_mobile - 短信发送手机号
     */
    public String getSmsSendMobile() {
        return smsSendMobile;
    }

    /**
     * 设置短信发送手机号
     *
     * @param smsSendMobile 短信发送手机号
     */
    public void setSmsSendMobile(String smsSendMobile) {
        this.smsSendMobile = smsSendMobile;
    }

    /**
     * 获取首次短信首次发送
     *
     * @return sms_first_alarm_time - 首次短信首次发送
     */
    public Integer getSmsFirstAlarmTime() {
        return smsFirstAlarmTime;
    }

    /**
     * 设置首次短信首次发送
     *
     * @param smsFirstAlarmTime 首次短信首次发送
     */
    public void setSmsFirstAlarmTime(Integer smsFirstAlarmTime) {
        this.smsFirstAlarmTime = smsFirstAlarmTime;
    }

    /**
     * 获取间隔时间设置例如：600,600,600
     *
     * @return sms_alarm_time - 间隔时间设置例如：600,600,600
     */
    public String getSmsAlarmTime() {
        return smsAlarmTime;
    }

    /**
     * 设置间隔时间设置例如：600,600,600
     *
     * @param smsAlarmTime 间隔时间设置例如：600,600,600
     */
    public void setSmsAlarmTime(String smsAlarmTime) {
        this.smsAlarmTime = smsAlarmTime;
    }

    /**
     * 获取短信发送第几次 如果次数 大于配置间隔个数 则不发送
     *
     * @return sms_alarm_acount - 短信发送第几次 如果次数 大于配置间隔个数 则不发送
     */
    public Integer getSmsAlarmCount() {
        return smsAlarmCount;
    }

    /**
     * 设置短信发送第几次 如果次数 大于配置间隔个数 则不发送
     *
     * @param smsAlarmAcount 短信发送第几次 如果次数 大于配置间隔个数 则不发送
     */
    public void setSmsAlarmCount(Integer smsAlarmCount) {
        this.smsAlarmCount = smsAlarmCount;
    }

    /**
     * 获取钉钉发送地址
     *
     * @return dingding_url - 钉钉发送地址
     */
    public String getDingdingUrl() {
        return dingdingUrl;
    }

    /**
     * 设置钉钉发送地址
     *
     * @param dingdingUrl 钉钉发送地址
     */
    public void setDingdingUrl(String dingdingUrl) {
        this.dingdingUrl = dingdingUrl;
    }

    /**
     * 获取钉钉发送@人手机号
     *
     * @return dingding_mobile - 钉钉发送@人手机号
     */
    public String getDingdingMobile() {
        return dingdingMobile;
    }

    /**
     * 设置钉钉发送@人手机号
     *
     * @param dingdingMobile 钉钉发送@人手机号
     */
    public void setDingdingMobile(String dingdingMobile) {
        this.dingdingMobile = dingdingMobile;
    }

    /**
     * 获取钉钉发送内容
     *
     * @return dingding_send_content - 钉钉发送内容
     */
    public String getDingdingSendContent() {
        return dingdingSendContent;
    }

    /**
     * 设置钉钉发送内容
     *
     * @param dingdingSendContent 钉钉发送内容
     */
    public void setDingdingSendContent(String dingdingSendContent) {
        this.dingdingSendContent = dingdingSendContent;
    }

    /**
     * 获取首次短信首次发送
     *
     * @return dingding_first_alarm_time - 首次短信首次发送
     */
    public Integer getDingdingFirstAlarmTime() {
        return dingdingFirstAlarmTime;
    }

    /**
     * 设置首次短信首次发送
     *
     * @param dingdingFirstAlarmTime 首次短信首次发送
     */
    public void setDingdingFirstAlarmTime(Integer dingdingFirstAlarmTime) {
        this.dingdingFirstAlarmTime = dingdingFirstAlarmTime;
    }

    /**
     * 获取间隔时间设置例如：600,600,600
     *
     * @return dingding_alarm_time - 间隔时间设置例如：600,600,600
     */
    public String getDingdingAlarmTime() {
        return dingdingAlarmTime;
    }

    /**
     * 设置间隔时间设置例如：600,600,600
     *
     * @param dingdingAlarmTime 间隔时间设置例如：600,600,600
     */
    public void setDingdingAlarmTime(String dingdingAlarmTime) {
        this.dingdingAlarmTime = dingdingAlarmTime;
    }

    /**
     * 获取钉钉发送第几次 如果次数 大于配置间隔个数 则不发送
     *
     * @return dingding_alarm_acount - 钉钉发送第几次 如果次数 大于配置间隔个数 则不发送
     */
    public Integer getDingdingAlarmCount() {
        return dingdingAlarmCount;
    }

    /**
     * 设置钉钉发送第几次 如果次数 大于配置间隔个数 则不发送
     *
     * @param dingdingAlarmAcount 钉钉发送第几次 如果次数 大于配置间隔个数 则不发送
     */
    public void setDingdingAlarmCount(Integer dingdingAlarmCount) {
        this.dingdingAlarmCount = dingdingAlarmCount;
    }
}