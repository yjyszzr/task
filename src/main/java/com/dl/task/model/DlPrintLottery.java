package com.dl.task.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;

@Table(name = "dl_print_lottery")
public class DlPrintLottery {
    /**
     * 出票流水id
     */
    @Id
    @Column(name = "print_lottery_id")
    private Integer printLotteryId;

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
     * 请求时间
     */
    @Column(name = "accept_time")
    private Integer acceptTime;

    /**
     * 代理商编号
     */
    private String merchant;

    /**
     * 游戏编号
     */
    private String game;

    /**
     * 期次
     */
    private String issue;

    /**
     * 玩法
     */
    @Column(name = "play_type")
    private String playType;

    /**
     * 投注方式
     */
    @Column(name = "bet_type")
    private String betType;

    /**
     * 倍数
     */
    private Integer times;

    /**
     * 彩票金额
     */
    private BigDecimal money;

    /**
     * 投注号码
     */
    private String stakes;

    @Column(name = "reward_stakes")
    private String rewardStakes;

    /**
     * 出票返回错误码55555超时
     */
    @Column(name = "error_code")
    private Integer errorCode;

    /**
     * 出票状态 0-待出票 1-已出票 2-出票失败 3-出票中
     */
    private Integer status;

    /**
     * 中心平台订单编号
     */
    @Column(name = "platform_id")
    private String platformId;

    /**
     * 出票返回的状态8出票中16成功17失败
     */
    @Column(name = "print_status")
    private Integer printStatus;
    /**
     * 第三方出奖状态 1:第三方未进行出奖 2:第三方出奖中 3:第三方已出奖 4:第三方已结算
     */
    @Column(name = "third_reward_status")
    private Integer thirdRewardStatus;
    
    /**
     * 出票返回的赔率
     */
    @Column(name = "print_sp")
    private String printSp;

    /**
     * 出票返回的票号
     */
    @Column(name = "print_no")
    private String printNo;

    /**
     * 返回的出票时间
     */
    @Column(name = "print_time")
    private Date printTime;

    /**
     * 根据赔率和中奖结果系统计算出中奖金额
     */
    @Column(name = "real_reward_money")
    private BigDecimal realRewardMoney;

    /**
     * 第三方给出的中奖金额
     */
    @Column(name = "third_part_reward_money")
    private BigDecimal thirdPartRewardMoney;

    /**
     * 0-- 该张彩票的所有期次中有未开奖的比赛期次;
1-- 该张彩票的所有期次都与比赛结果比较,但还未得到第三方金额;
2-- 比对完成,计算出的金额等于第三方算出金额; 
3-- 比对完成,计算出的金额不等于第三方算出金额;
     */
    @Column(name = "compare_status")
    private String compareStatus;

    /**
     * 比较过的stakes,存储格式:期次|比较状态;如:201804135004|0,已对比较过-1,未比较过-0
     */
    @Column(name = "compared_stakes")
    private String comparedStakes;

    /**
     * print_lottery_com
     */
    @Column(name = "print_lottery_com")
    private Integer printLotteryCom;
    /**
     * 获取出票流水id
     *
     * @return print_lottery_id - 出票流水id
     */
    public Integer getPrintLotteryId() {
        return printLotteryId;
    }

    /**
     * 设置出票流水id
     *
     * @param printLotteryId 出票流水id
     */
    public void setPrintLotteryId(Integer printLotteryId) {
        this.printLotteryId = printLotteryId;
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
     * 获取请求时间
     *
     * @return accept_time - 请求时间
     */
    public Integer getAcceptTime() {
        return acceptTime;
    }

    /**
     * 设置请求时间
     *
     * @param acceptTime 请求时间
     */
    public void setAcceptTime(Integer acceptTime) {
        this.acceptTime = acceptTime;
    }

    /**
     * 获取代理商编号
     *
     * @return merchant - 代理商编号
     */
    public String getMerchant() {
        return merchant;
    }

    /**
     * 设置代理商编号
     *
     * @param merchant 代理商编号
     */
    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    /**
     * 获取游戏编号
     *
     * @return game - 游戏编号
     */
    public String getGame() {
        return game;
    }

    /**
     * 设置游戏编号
     *
     * @param game 游戏编号
     */
    public void setGame(String game) {
        this.game = game;
    }

    /**
     * 获取期次
     *
     * @return issue - 期次
     */
    public String getIssue() {
        return issue;
    }

    /**
     * 设置期次
     *
     * @param issue 期次
     */
    public void setIssue(String issue) {
        this.issue = issue;
    }

    /**
     * 获取玩法
     *
     * @return play_type - 玩法
     */
    public String getPlayType() {
        return playType;
    }

    /**
     * 设置玩法
     *
     * @param playType 玩法
     */
    public void setPlayType(String playType) {
        this.playType = playType;
    }

    /**
     * 获取投注方式
     *
     * @return bet_type - 投注方式
     */
    public String getBetType() {
        return betType;
    }

    /**
     * 设置投注方式
     *
     * @param betType 投注方式
     */
    public void setBetType(String betType) {
        this.betType = betType;
    }

    /**
     * 获取倍数
     *
     * @return times - 倍数
     */
    public Integer getTimes() {
        return times;
    }

    /**
     * 设置倍数
     *
     * @param times 倍数
     */
    public void setTimes(Integer times) {
        this.times = times;
    }

    /**
     * 获取彩票金额
     *
     * @return money - 彩票金额
     */
    public BigDecimal getMoney() {
        return money;
    }

    /**
     * 设置彩票金额
     *
     * @param money 彩票金额
     */
    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    /**
     * 获取投注号码
     *
     * @return stakes - 投注号码
     */
    public String getStakes() {
        return stakes;
    }

    /**
     * 设置投注号码
     *
     * @param stakes 投注号码
     */
    public void setStakes(String stakes) {
        this.stakes = stakes;
    }

    /**
     * @return reward_stakes
     */
    public String getRewardStakes() {
        return rewardStakes;
    }

    /**
     * @param rewardStakes
     */
    public void setRewardStakes(String rewardStakes) {
        this.rewardStakes = rewardStakes;
    }

    /**
     * 获取出票返回错误码55555超时
     *
     * @return error_code - 出票返回错误码55555超时
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    /**
     * 设置出票返回错误码55555超时
     *
     * @param errorCode 出票返回错误码55555超时
     */
    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 获取出票状态 0-待出票 1-已出票 2-出票失败 3-出票中
     *
     * @return status - 出票状态 0-待出票 1-已出票 2-出票失败 3-出票中
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置出票状态 0-待出票 1-已出票 2-出票失败 3-出票中
     *
     * @param status 出票状态 0-待出票 1-已出票 2-出票失败 3-出票中
     */
    public void setStatus(Integer status) {
        this.status = status;
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
     * 获取出票返回的状态8出票中16成功17失败
     *
     * @return print_status - 出票返回的状态8出票中16成功17失败
     */
    public Integer getPrintStatus() {
        return printStatus;
    }

    /**
     * 设置出票返回的状态8出票中16成功17失败
     *
     * @param printStatus 出票返回的状态8出票中16成功17失败
     */
    public void setPrintStatus(Integer printStatus) {
        this.printStatus = printStatus;
    }

    public Integer getThirdRewardStatus() {
		return thirdRewardStatus;
	}

	public void setThirdRewardStatus(Integer thirdRewardStatus) {
		this.thirdRewardStatus = thirdRewardStatus;
	}

	/**
     * 获取出票返回的赔率
     *
     * @return print_sp - 出票返回的赔率
     */
    public String getPrintSp() {
        return printSp;
    }

    /**
     * 设置出票返回的赔率
     *
     * @param printSp 出票返回的赔率
     */
    public void setPrintSp(String printSp) {
        this.printSp = printSp;
    }

    /**
     * 获取出票返回的票号
     *
     * @return print_no - 出票返回的票号
     */
    public String getPrintNo() {
        return printNo;
    }

    /**
     * 设置出票返回的票号
     *
     * @param printNo 出票返回的票号
     */
    public void setPrintNo(String printNo) {
        this.printNo = printNo;
    }

    /**
     * 获取返回的出票时间
     *
     * @return print_time - 返回的出票时间
     */
    public Date getPrintTime() {
        return printTime;
    }

    /**
     * 设置返回的出票时间
     *
     * @param printTime 返回的出票时间
     */
    public void setPrintTime(Date printTime) {
        this.printTime = printTime;
    }

    /**
     * 获取根据赔率和中奖结果系统计算出中奖金额
     *
     * @return real_reward_money - 根据赔率和中奖结果系统计算出中奖金额
     */
    public BigDecimal getRealRewardMoney() {
        return realRewardMoney;
    }

    /**
     * 设置根据赔率和中奖结果系统计算出中奖金额
     *
     * @param realRewardMoney 根据赔率和中奖结果系统计算出中奖金额
     */
    public void setRealRewardMoney(BigDecimal realRewardMoney) {
        this.realRewardMoney = realRewardMoney;
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
     * 获取0-- 该张彩票的所有期次中有未开奖的比赛期次;
1-- 该张彩票的所有期次都与比赛结果比较,但还未得到第三方金额;
2-- 比对完成,计算出的金额等于第三方算出金额; 
3-- 比对完成,计算出的金额不等于第三方算出金额;
     *
     * @return compare_status - 0-- 该张彩票的所有期次中有未开奖的比赛期次;
1-- 该张彩票的所有期次都与比赛结果比较,但还未得到第三方金额;
2-- 比对完成,计算出的金额等于第三方算出金额; 
3-- 比对完成,计算出的金额不等于第三方算出金额;
     */
    public String getCompareStatus() {
        return compareStatus;
    }

    /**
     * 设置0-- 该张彩票的所有期次中有未开奖的比赛期次;
1-- 该张彩票的所有期次都与比赛结果比较,但还未得到第三方金额;
2-- 比对完成,计算出的金额等于第三方算出金额; 
3-- 比对完成,计算出的金额不等于第三方算出金额;
     *
     * @param compareStatus 0-- 该张彩票的所有期次中有未开奖的比赛期次;
1-- 该张彩票的所有期次都与比赛结果比较,但还未得到第三方金额;
2-- 比对完成,计算出的金额等于第三方算出金额; 
3-- 比对完成,计算出的金额不等于第三方算出金额;
     */
    public void setCompareStatus(String compareStatus) {
        this.compareStatus = compareStatus;
    }

    /**
     * 获取比较过的stakes,存储格式:期次|比较状态;如:201804135004|0,已对比较过-1,未比较过-0
     *
     * @return compared_stakes - 比较过的stakes,存储格式:期次|比较状态;如:201804135004|0,已对比较过-1,未比较过-0
     */
    public String getComparedStakes() {
        return comparedStakes;
    }

    /**
     * 设置比较过的stakes,存储格式:期次|比较状态;如:201804135004|0,已对比较过-1,未比较过-0
     *
     * @param comparedStakes 比较过的stakes,存储格式:期次|比较状态;如:201804135004|0,已对比较过-1,未比较过-0
     */
    public void setComparedStakes(String comparedStakes) {
        this.comparedStakes = comparedStakes;
    }

	public Integer getPrintLotteryCom() {
		return printLotteryCom;
	}

	public void setPrintLotteryCom(Integer printLotteryCom) {
		this.printLotteryCom = printLotteryCom;
	}
}