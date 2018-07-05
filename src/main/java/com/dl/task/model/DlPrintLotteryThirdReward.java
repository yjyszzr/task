package com.dl.task.model;

import java.math.BigDecimal;
import javax.persistence.*;

@Table(name = "dl_print_lottery_third_reward")
public class DlPrintLotteryThirdReward {
    /**
     * 出票流水id
     */
    @Id
    @Column(name = "print_lottery_result_id")
    private Integer printLotteryResultId;

    /**
     * 订单编号
     */
    @Column(name = "order_sn")
    private String orderSn;

    /**
     * 出票编号
     */
    @Column(name = "ticket_id")
    private String ticketId;

    /**
     * 中心平台订单编号
     */
    @Column(name = "platform_id")
    private String platformId;

    /**
     * 第三方给出的中奖金额
     */
    @Column(name = "third_part_reward_money")
    private BigDecimal thirdPartRewardMoney;

    /**
     * 出票状态 8-小奖 9-大奖 10-未中奖
     */
    @Column(name = "reward_status")
    private Integer rewardStatus;

    /**
     * 结算状态 1-未结算 2-已结算
     */
    @Column(name = "balance_status")
    private Integer balanceStatus;

    /**
     * 获取出票流水id
     *
     * @return print_lottery_result_id - 出票流水id
     */
    public Integer getPrintLotteryResultId() {
        return printLotteryResultId;
    }

    /**
     * 设置出票流水id
     *
     * @param printLotteryResultId 出票流水id
     */
    public void setPrintLotteryResultId(Integer printLotteryResultId) {
        this.printLotteryResultId = printLotteryResultId;
    }

    /**
     * 获取订单编号
     *
     * @return order_sn - 订单编号
     */
    public String getOrderSn() {
        return orderSn;
    }

    /**
     * 设置订单编号
     *
     * @param orderSn 订单编号
     */
    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    /**
     * 获取出票编号
     *
     * @return ticket_id - 出票编号
     */
    public String getTicketId() {
        return ticketId;
    }

    /**
     * 设置出票编号
     *
     * @param ticketId 出票编号
     */
    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    /**
     * 获取中心平台订单编号
     *
     * @return platform_id - 中心平台订单编号
     */
    public String getPlatformId() {
        return platformId;
    }

    /**
     * 设置中心平台订单编号
     *
     * @param platformId 中心平台订单编号
     */
    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    /**
     * 获取第三方给出的中奖金额
     *
     * @return third_part_reward_money - 第三方给出的中奖金额
     */
    public BigDecimal getThirdPartRewardMoney() {
        return thirdPartRewardMoney;
    }

    /**
     * 设置第三方给出的中奖金额
     *
     * @param thirdPartRewardMoney 第三方给出的中奖金额
     */
    public void setThirdPartRewardMoney(BigDecimal thirdPartRewardMoney) {
        this.thirdPartRewardMoney = thirdPartRewardMoney;
    }

    /**
     * 获取出票状态 8-小奖 9-大奖 10-未中奖
     *
     * @return reward_status - 出票状态 8-小奖 9-大奖 10-未中奖
     */
    public Integer getRewardStatus() {
        return rewardStatus;
    }

    /**
     * 设置出票状态 8-小奖 9-大奖 10-未中奖
     *
     * @param rewardStatus 出票状态 8-小奖 9-大奖 10-未中奖
     */
    public void setRewardStatus(Integer rewardStatus) {
        this.rewardStatus = rewardStatus;
    }

    /**
     * 获取结算状态 1-未结算 2-已结算
     *
     * @return balance_status - 结算状态 1-未结算 2-已结算
     */
    public Integer getBalanceStatus() {
        return balanceStatus;
    }

    /**
     * 设置结算状态 1-未结算 2-已结算
     *
     * @param balanceStatus 结算状态 1-未结算 2-已结算
     */
    public void setBalanceStatus(Integer balanceStatus) {
        this.balanceStatus = balanceStatus;
    }
}