package com.dl.task.model;

import javax.persistence.*;

@Table(name = "dl_ticket_channel")
public class DlTicketChannel {
    /**
     * 更改是几十年
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 出票公司名称
     */
    @Column(name = "channel_name")
    private String channelName;

    /**
     * 出票公司编码
     */
    @Column(name = "channel_code")
    private String channelCode;

    /**
     * 出票公司Url
     */
    @Column(name = "ticket_url")
    private String ticketUrl;

    /**
     * 出票公司账号
     */
    @Column(name = "ticket_merchant")
    private String ticketMerchant;

    /**
     * 出票公司密码
     */
    @Column(name = "ticket_merchant_password")
    private String ticketMerchantPassword;

    /**
     * 出票公司回调URL
     */
    @Column(name = "ticket_notify_utl")
    private String ticketNotifyUtl;

    /**
     * 状态0启用1关闭
     */
    @Column(name = "channel_status")
    private Integer channelStatus;

    /**
     * 添加时间
     */
    @Column(name = "add_time")
    private Integer addTime;

    /**
     * 更改时间
     */
    @Column(name = "update_time")
    private Integer updateTime;

    /**
     * 获取更改是几十年
     *
     * @return id - 更改是几十年
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置更改是几十年
     *
     * @param id 更改是几十年
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取出票公司名称
     *
     * @return channel_name - 出票公司名称
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * 设置出票公司名称
     *
     * @param channelName 出票公司名称
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    /**
     * 获取出票公司编码
     *
     * @return channel_code - 出票公司编码
     */
    public String getChannelCode() {
        return channelCode;
    }

    /**
     * 设置出票公司编码
     *
     * @param channelCode 出票公司编码
     */
    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    /**
     * 获取出票公司Url
     *
     * @return ticket_url - 出票公司Url
     */
    public String getTicketUrl() {
        return ticketUrl;
    }

    /**
     * 设置出票公司Url
     *
     * @param ticketUrl 出票公司Url
     */
    public void setTicketUrl(String ticketUrl) {
        this.ticketUrl = ticketUrl;
    }

    /**
     * 获取出票公司账号
     *
     * @return ticket_merchant - 出票公司账号
     */
    public String getTicketMerchant() {
        return ticketMerchant;
    }

    /**
     * 设置出票公司账号
     *
     * @param ticketMerchant 出票公司账号
     */
    public void setTicketMerchant(String ticketMerchant) {
        this.ticketMerchant = ticketMerchant;
    }

    /**
     * 获取出票公司密码
     *
     * @return ticket_merchant_password - 出票公司密码
     */
    public String getTicketMerchantPassword() {
        return ticketMerchantPassword;
    }

    /**
     * 设置出票公司密码
     *
     * @param ticketMerchantPassword 出票公司密码
     */
    public void setTicketMerchantPassword(String ticketMerchantPassword) {
        this.ticketMerchantPassword = ticketMerchantPassword;
    }

    /**
     * 获取出票公司回调URL
     *
     * @return ticket_notify_utl - 出票公司回调URL
     */
    public String getTicketNotifyUtl() {
        return ticketNotifyUtl;
    }

    /**
     * 设置出票公司回调URL
     *
     * @param ticketNotifyUtl 出票公司回调URL
     */
    public void setTicketNotifyUtl(String ticketNotifyUtl) {
        this.ticketNotifyUtl = ticketNotifyUtl;
    }

    /**
     * 获取状态0启用1关闭
     *
     * @return channel_status - 状态0启用1关闭
     */
    public Integer getChannelStatus() {
        return channelStatus;
    }

    /**
     * 设置状态0启用1关闭
     *
     * @param channelStatus 状态0启用1关闭
     */
    public void setChannelStatus(Integer channelStatus) {
        this.channelStatus = channelStatus;
    }

    /**
     * 获取添加时间
     *
     * @return add_time - 添加时间
     */
    public Integer getAddTime() {
        return addTime;
    }

    /**
     * 设置添加时间
     *
     * @param addTime 添加时间
     */
    public void setAddTime(Integer addTime) {
        this.addTime = addTime;
    }

    /**
     * 获取更改时间
     *
     * @return update_time - 更改时间
     */
    public Integer getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更改时间
     *
     * @param updateTime 更改时间
     */
    public void setUpdateTime(Integer updateTime) {
        this.updateTime = updateTime;
    }
}