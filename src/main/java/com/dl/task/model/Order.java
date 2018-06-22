package com.dl.task.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;

@Table(name = "dl_order")
public class Order {
    /**
     * 订单id
     */
    @Id
    @Column(name = "order_id")
    private Integer orderId;

    /**
     * 订单号
     */
    @Column(name = "order_sn")
    private String orderSn;

    /**
     * 父订单号
     */
    @Column(name = "parent_sn")
    private String parentSn;

    /**
     * 买家id
     */
    @Column(name = "user_id")
    private Integer userId;

    /**
     * 订单状态:0-待开奖,1-未中奖,2-已中奖
     */
    @Column(name = "order_status")
    private Integer orderStatus;

    /**
     * 支付状态
     */
    @Column(name = "pay_status")
    private Integer payStatus;

    /**
     * 支付id
     */
    @Column(name = "pay_id")
    private Integer payId;

    /**
     * 支付代码
     */
    @Column(name = "pay_code")
    private String payCode;

    /**
     * 支付名称
     */
    @Column(name = "pay_name")
    private String payName;

    @Column(name = "pay_sn")
    private String paySn;

    /**
     * 订单实付金额
     */
    @Column(name = "money_paid")
    private BigDecimal moneyPaid;

    /**
     * 彩票总金额
     */
    @Column(name = "ticket_amount")
    private BigDecimal ticketAmount;

    /**
     * 余额支付
     */
    private BigDecimal surplus;

    /**
     * 可提现余额支付
     */
    @Column(name = "user_surplus")
    private BigDecimal userSurplus;

    /**
     * 不可提现余额支付
     */
    @Column(name = "user_surplus_limit")
    private BigDecimal userSurplusLimit;
    
    /**
     * 第三方支付金额
     */
    @Column(name = "third_party_paid")
    private BigDecimal thirdPartyPaid;

    /**
     * 用户红包id
     */
    @Column(name = "user_bonus_id")
    private Integer userBonusId;

    /**
     * 用户红包金额
     */
    private BigDecimal bonus;

    /**
     * 订单赠送的积分
     */
    @Column(name = "give_integral")
    private Integer giveIntegral;

    /**
     * 订单来源
     */
    @Column(name = "order_from")
    private String orderFrom;

    /**
     * 订单生成时间
     */
    @Column(name = "add_time")
    private Integer addTime;

    /**
     * 订单支付时间
     */
    @Column(name = "pay_time")
    private Integer payTime;

    /**
     * 订单类型
     */
    @Column(name = "order_type")
    private Integer orderType;
    
    /**
     * 彩票种类id
     */
    @Column(name = "lottery_classify_id")
    private Integer lotteryClassifyId;
    
    
    @Column(name = "device_channel")
    private String deviceChannel;
    
    /**
     * 彩票子分类
     */
    @Column(name = "lottery_play_classify_id")
    private Integer lotteryPlayClassifyId;
    
    
    @Column(name = "award_time")
    private Integer awardTime;
    
    public Integer getAwardTime() {
		return awardTime;
	}

	public void setAwardTime(Integer awardTime) {
		this.awardTime = awardTime;
	}

	public String getDeviceChannel() {
		return deviceChannel;
	}

	public void setDeviceChannel(String deviceChannel) {
		this.deviceChannel = deviceChannel;
	}

	public Integer getLotteryPlayClassifyId() {
		return lotteryPlayClassifyId;
	}

	public void setLotteryPlayClassifyId(Integer lotteryPlayClassifyId) {
		this.lotteryPlayClassifyId = lotteryPlayClassifyId;
	}

	/**
     * 比赛时间
     */
    @Column(name = "match_time")
    private Date matchTime;
    
    /**
     * 中奖金额
     */
    @Column(name = "winning_money")
    private BigDecimal winningMoney;
    
    /**
     * 过关方式
     */
    @Column(name = "pass_type")
    private String passType;
    
    /**
     * 玩法
     */
    @Column(name = "play_type")
    private String playType;
    
    /**
     * 投注倍数
     */
    @Column(name = "cathectic")
    private Integer cathectic; 
    
    /**
     * 投注倍数
     */
    @Column(name = "bet_num")
    private Integer betNum;
    
    
    
    /**
     * 店铺接单时间（出票开始时间）
     */
    @Column(name = "accept_time")
    private Integer acceptTime;
    
    /**
     * 出票时间
     */
    @Column(name = "ticket_time")
    private Integer ticketTime;
    
    /**
     * 预测奖金
     */
    @Column(name = "forecast_money")
    private String forecastMoney;
    
    /**
     * 投注最后一场比赛期次
     */
    @Column(name = "issue")
    private String issue;

    /**
     * 是否彻底删除
     */
    @Column(name = "is_delete")
    private Integer isDelete;
    @Column(name = "ticket_num")
    private Integer ticketNum;

    /**
     * 获取订单id
     *
     * @return order_id - 订单id
     */
    public Integer getOrderId() {
        return orderId;
    }

    /**
     * 设置订单id
     *
     * @param orderId 订单id
     */
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    /**
     * 获取订单号
     *
     * @return order_sn - 订单号
     */
    public String getOrderSn() {
        return orderSn;
    }

    /**
     * 设置订单号
     *
     * @param orderSn 订单号
     */
    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    /**
     * 获取父订单号
     *
     * @return parent_sn - 父订单号
     */
    public String getParentSn() {
        return parentSn;
    }

    /**
     * 设置父订单号
     *
     * @param parentSn 父订单号
     */
    public void setParentSn(String parentSn) {
        this.parentSn = parentSn;
    }

    /**
     * 获取买家id
     *
     * @return user_id - 买家id
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * 设置买家id
     *
     * @param userId 买家id
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * 获取订单状态:0-待开奖,1-未中奖,2-已中奖
     *
     * @return order_status - 订单状态:0-待开奖,1-未中奖,2-已中奖
     */
    public Integer getOrderStatus() {
        return orderStatus;
    }

    /**
     * 设置订单状态:0-待开奖,1-未中奖,2-已中奖
     *
     * @param orderStatus 订单状态:0-待开奖,1-未中奖,2-已中奖
     */
    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    /**
     * 获取支付状态
     *
     * @return pay_status - 支付状态
     */
    public Integer getPayStatus() {
        return payStatus;
    }

    /**
     * 设置支付状态
     *
     * @param payStatus 支付状态
     */
    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    /**
     * 获取支付id
     *
     * @return pay_id - 支付id
     */
    public Integer getPayId() {
        return payId;
    }

    /**
     * 设置支付id
     *
     * @param payId 支付id
     */
    public void setPayId(Integer payId) {
        this.payId = payId;
    }

    /**
     * 获取支付代码
     *
     * @return pay_code - 支付代码
     */
    public String getPayCode() {
        return payCode;
    }

    /**
     * 设置支付代码
     *
     * @param payCode 支付代码
     */
    public void setPayCode(String payCode) {
        this.payCode = payCode;
    }

    /**
     * 获取支付名称
     *
     * @return pay_name - 支付名称
     */
    public String getPayName() {
        return payName;
    }

    /**
     * 设置支付名称
     *
     * @param payName 支付名称
     */
    public void setPayName(String payName) {
        this.payName = payName;
    }

    /**
     * @return pay_sn
     */
    public String getPaySn() {
        return paySn;
    }

    /**
     * @param paySn
     */
    public void setPaySn(String paySn) {
        this.paySn = paySn;
    }

    /**
     * 获取订单实付金额
     *
     * @return money_paid - 订单实付金额
     */
    public BigDecimal getMoneyPaid() {
        return moneyPaid;
    }

    /**
     * 设置订单实付金额
     *
     * @param moneyPaid 订单实付金额
     */
    public void setMoneyPaid(BigDecimal moneyPaid) {
        this.moneyPaid = moneyPaid;
    }

    /**
     * 获取彩票总金额
     *
     * @return ticket_amount - 彩票总金额
     */
    public BigDecimal getTicketAmount() {
        return ticketAmount;
    }

    /**
     * 设置彩票总金额
     *
     * @param ticketAmount 彩票总金额
     */
    public void setTicketAmount(BigDecimal ticketAmount) {
        this.ticketAmount = ticketAmount;
    }

    /**
     * 获取余额支付
     *
     * @return surplus - 余额支付
     */
    public BigDecimal getSurplus() {
        return surplus;
    }

    /**
     * 设置余额支付
     *
     * @param surplus 余额支付
     */
    public void setSurplus(BigDecimal surplus) {
        this.surplus = surplus;
    }

    /**
     * 获取可提现余额支付
     *
     * @return user_surplus - 可提现余额支付
     */
    public BigDecimal getUserSurplus() {
        return userSurplus;
    }

    /**
     * 设置可提现余额支付
     *
     * @param userSurplus 可提现余额支付
     */
    public void setUserSurplus(BigDecimal userSurplus) {
        this.userSurplus = userSurplus;
    }

    /**
     * 获取不可提现余额支付
     *
     * @return user_surplus_limit - 不可提现余额支付
     */
    public BigDecimal getUserSurplusLimit() {
        return userSurplusLimit;
    }

    /**
     * 设置不可提现余额支付
     *
     * @param userSurplusLimit 不可提现余额支付
     */
    public void setUserSurplusLimit(BigDecimal userSurplusLimit) {
        this.userSurplusLimit = userSurplusLimit;
    }
    
    /**
     * 获取第三方支付金额
     *
     * @return thirdPartyPaid - 第三方支付金额
     */
    public BigDecimal getThirdPartyPaid() {
        return thirdPartyPaid;
    }

    /**
     * 设置第三方支付金额
     *
     * @param thirdPartyPaid 第三方支付金额
     */
    public void setThirdPartyPaid(BigDecimal thirdPartyPaid) {
        this.thirdPartyPaid = thirdPartyPaid;
    }

    /**
     * 获取用户红包id
     *
     * @return user_bonus_id - 用户红包id
     */
    public Integer getUserBonusId() {
        return userBonusId;
    }

    /**
     * 设置用户红包id
     *
     * @param userBonusId 用户红包id
     */
    public void setUserBonusId(Integer userBonusId) {
        this.userBonusId = userBonusId;
    }

    /**
     * 获取用户红包金额
     *
     * @return bonus - 用户红包金额
     */
    public BigDecimal getBonus() {
        return bonus;
    }

    /**
     * 设置用户红包金额
     *
     * @param bonus 用户红包金额
     */
    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    /**
     * 获取订单赠送的积分
     *
     * @return give_integral - 订单赠送的积分
     */
    public Integer getGiveIntegral() {
        return giveIntegral;
    }

    /**
     * 设置订单赠送的积分
     *
     * @param giveIntegral 订单赠送的积分
     */
    public void setGiveIntegral(Integer giveIntegral) {
        this.giveIntegral = giveIntegral;
    }

    /**
     * 获取订单来源
     *
     * @return order_from - 订单来源
     */
    public String getOrderFrom() {
        return orderFrom;
    }

    /**
     * 设置订单来源
     *
     * @param orderFrom 订单来源
     */
    public void setOrderFrom(String orderFrom) {
        this.orderFrom = orderFrom;
    }

    /**
     * 获取订单生成时间
     *
     * @return add_time - 订单生成时间
     */
    public Integer getAddTime() {
        return addTime;
    }

    /**
     * 设置订单生成时间
     *
     * @param addTime 订单生成时间
     */
    public void setAddTime(Integer addTime) {
        this.addTime = addTime;
    }

    /**
     * 获取订单支付时间
     *
     * @return pay_time - 订单支付时间
     */
    public Integer getPayTime() {
        return payTime;
    }

    /**
     * 设置订单支付时间
     *
     * @param payTime 订单支付时间
     */
    public void setPayTime(Integer payTime) {
        this.payTime = payTime;
    }

    /**
     * 获取订单类型
     *
     * @return order_type - 订单类型
     */
    public Integer getOrderType() {
        return orderType;
    }

    /**
     * 设置订单类型
     *
     * @param orderType 订单类型
     */
    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }
    
    public Integer getLotteryClassifyId() {
		return lotteryClassifyId;
	}

	public void setLotteryClassifyId(Integer lotteryClassifyId) {
		this.lotteryClassifyId = lotteryClassifyId;
	}

	public Date getMatchTime() {
		return matchTime;
	}

	public void setMatchTime(Date matchTime) {
		this.matchTime = matchTime;
	}

	public BigDecimal getWinningMoney() {
		return winningMoney;
	}

	public void setWinningMoney(BigDecimal winningMoney) {
		this.winningMoney = winningMoney;
	}
	
	public String getPassType() {
		return passType;
	}

	public void setPassType(String passType) {
		this.passType = passType;
	}

	public Integer getCathectic() {
		return cathectic;
	}

	public void setCathectic(Integer cathectic) {
		this.cathectic = cathectic;
	}

	public Integer getBetNum() {
		return betNum;
	}

	public void setBetNum(Integer betNum) {
		this.betNum = betNum;
	}

	public Integer getAcceptTime() {
		return acceptTime;
	}

	public void setAcceptTime(Integer acceptTime) {
		this.acceptTime = acceptTime;
	}

	public Integer getTicketTime() {
		return ticketTime;
	}

	public void setTicketTime(Integer ticketTime) {
		this.ticketTime = ticketTime;
	}
	
	public String getForecastMoney() {
		return forecastMoney;
	}

	public void setForecastMoney(String forecastMoney) {
		this.forecastMoney = forecastMoney;
	}

	/**
     * 获取是否彻底删除
     *
     * @return is_delete - 是否彻底删除
     */
    public Integer getIsDelete() {
        return isDelete;
    }

    /**
     * 设置是否彻底删除
     *
     * @param isDelete 是否彻底删除
     */
    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

	public String getPlayType() {
		return playType;
	}

	public void setPlayType(String playType) {
		this.playType = playType;
	}

	public String getIssue() {
		return issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public Integer getTicketNum() {
		return ticketNum;
	}

	public void setTicketNum(Integer ticketNum) {
		this.ticketNum = ticketNum;
	}
    
}