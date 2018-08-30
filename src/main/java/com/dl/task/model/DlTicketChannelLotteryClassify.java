package com.dl.task.model;

import java.math.BigDecimal;

import javax.persistence.*;

@Table(name = "dl_ticket_channel_lottery_classify")
public class DlTicketChannelLotteryClassify {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 出票公司Id
     */
    @Column(name = "ticket_channel_id")
    private Integer ticketChannelId;

    /**
     * 出票公司名称
     */
    @Column(name = "ticket_channel_name")
    private String ticketChannelName;

    /**
     * 彩种Id
     */
    @Column(name = "lottery_classify_id")
    private Integer lotteryClassifyId;

    /**
     * 彩种名称
     */
    @Column(name = "lottery_classify_name")
    private String lotteryClassifyName;

    /**
     * 彩种编号
     */
    private String game;

    /**
     * 最低投注金额
     */
    @Column(name = "min_bet_amount")
    private BigDecimal minBetAmount;

    /**
     * 最大投注金额
     */
    @Column(name = "max_bet_amount")
    private BigDecimal maxBetAmount;

    /**
     * 开赛前几分钟停止售票
     */
    @Column(name = "sale_end_time")
    private String saleEndTime;

    /**
     * 开机时间
     */
    @Column(name = "matchine_open_time")
    private String matchineOpenTime;

    /**
     * 关机时间
     */
    @Column(name = "matchine_close_time")
    private String matchineCloseTime;

    /**
     * 状态0启用1关闭
     */
    private Integer status;

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
     * 单张票最低投注金额
     */
    @Column(name = "min_bet_amount_lottery")
    private BigDecimal minBetAmountLottery;
    /**
     * 单张票最大投注金额
     */
    @Column(name = "max_bet_amount_lottery")
    private BigDecimal maxBetAmountLottery;
    /**
     * 单张票最大投注倍数
     */
    @Column(name = "max_times_lottery")
    private Integer maxTimesLottery;
    /**
     * 排除的玩法多个玩法逗号分隔
     */
    @Column(name = "exclude_play_type")
    private String excludePlayType;
    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取出票公司Id
     *
     * @return ticket_channel_id - 出票公司Id
     */
    public Integer getTicketChannelId() {
        return ticketChannelId;
    }

    /**
     * 设置出票公司Id
     *
     * @param ticketChannelId 出票公司Id
     */
    public void setTicketChannelId(Integer ticketChannelId) {
        this.ticketChannelId = ticketChannelId;
    }

    /**
     * 获取出票公司名称
     *
     * @return ticket_channel_name - 出票公司名称
     */
    public String getTicketChannelName() {
        return ticketChannelName;
    }

    /**
     * 设置出票公司名称
     *
     * @param ticketChannelName 出票公司名称
     */
    public void setTicketChannelName(String ticketChannelName) {
        this.ticketChannelName = ticketChannelName;
    }

    /**
     * 获取彩种Id
     *
     * @return lottery_classify_id - 彩种Id
     */
    public Integer getLotteryClassifyId() {
        return lotteryClassifyId;
    }

    /**
     * 设置彩种Id
     *
     * @param lotteryClassifyId 彩种Id
     */
    public void setLotteryClassifyId(Integer lotteryClassifyId) {
        this.lotteryClassifyId = lotteryClassifyId;
    }

    /**
     * 获取彩种名称
     *
     * @return lottery_classify_name - 彩种名称
     */
    public String getLotteryClassifyName() {
        return lotteryClassifyName;
    }

    /**
     * 设置彩种名称
     *
     * @param lotteryClassifyName 彩种名称
     */
    public void setLotteryClassifyName(String lotteryClassifyName) {
        this.lotteryClassifyName = lotteryClassifyName;
    }

    /**
     * 获取彩种编号
     *
     * @return game - 彩种编号
     */
    public String getGame() {
        return game;
    }

    /**
     * 设置彩种编号
     *
     * @param game 彩种编号
     */
    public void setGame(String game) {
        this.game = game;
    }

    /**
     * 获取最低投注金额
     *
     * @return min_bet_amount - 最低投注金额
     */
    public BigDecimal getMinBetAmount() {
        return minBetAmount;
    }

    /**
     * 设置最低投注金额
     *
     * @param minBetAmount 最低投注金额
     */
    public void setMinBetAmount(BigDecimal minBetAmount) {
        this.minBetAmount = minBetAmount;
    }

    /**
     * 获取最大投注金额
     *
     * @return max_bet_amount - 最大投注金额
     */
    public BigDecimal getMaxBetAmount() {
        return maxBetAmount;
    }

    /**
     * 设置最大投注金额
     *
     * @param maxBetAmount 最大投注金额
     */
    public void setMaxBetAmount(BigDecimal maxBetAmount) {
        this.maxBetAmount = maxBetAmount;
    }

    /**
     * 获取开赛前几分钟停止售票
     *
     * @return sale_end_time - 开赛前几分钟停止售票
     */
    public String getSaleEndTime() {
        return saleEndTime;
    }

    /**
     * 设置开赛前几分钟停止售票
     *
     * @param saleEndTime 开赛前几分钟停止售票
     */
    public void setSaleEndTime(String saleEndTime) {
        this.saleEndTime = saleEndTime;
    }

    /**
     * 获取开机时间
     *
     * @return matchine_open_time - 开机时间
     */
    public String getMatchineOpenTime() {
        return matchineOpenTime;
    }

    /**
     * 设置开机时间
     *
     * @param matchineOpenTime 开机时间
     */
    public void setMatchineOpenTime(String matchineOpenTime) {
        this.matchineOpenTime = matchineOpenTime;
    }

    /**
     * 获取关机时间
     *
     * @return matchine_close_time - 关机时间
     */
    public String getMatchineCloseTime() {
        return matchineCloseTime;
    }

    /**
     * 设置关机时间
     *
     * @param matchineCloseTime 关机时间
     */
    public void setMatchineCloseTime(String matchineCloseTime) {
        this.matchineCloseTime = matchineCloseTime;
    }

    /**
     * 获取状态0启用1关闭
     *
     * @return status - 状态0启用1关闭
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置状态0启用1关闭
     *
     * @param status 状态0启用1关闭
     */
    public void setStatus(Integer status) {
        this.status = status;
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