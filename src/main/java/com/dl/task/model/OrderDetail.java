package com.dl.task.model;

import java.util.Date;

import javax.persistence.*;

@Table(name = "dl_order_detail")
public class OrderDetail {
    /**
     * 记录编号
     */
    @Id
    @Column(name = "order_detail_id")
    private Integer orderDetailId;

    /**
     * 买家id
     */
    @Column(name = "user_id")
    private Integer userId;

    /**
     * 订单编号
     */
    @Column(name = "order_id")
    private Integer orderId;
    
    /**
     * 订单号
     */
    @Column(name = "order_sn")
    private String orderSn;

    /**
     * 赛事id
     */
    @Column(name = "match_id")
    private Integer matchId;

    /**
     * 场次
     */
    private String changci;
    
    /**
     * 比赛期次
     */
    private String issue;

    /**
     * 比赛双方球队
     */
    @Column(name = "match_team")
    private String matchTeam;

    /**
     * 比赛结果
     */
    @Column(name = "match_result")
    private String matchResult;

    /**
     * 彩票信息（投注内容）
     */
    @Column(name = "ticket_data")
    private String ticketData;

    /**
     * 中奖状态：0-未开奖,1-未中奖,2-已中奖
     */
    @Column(name = "ticket_status")
    private Integer ticketStatus;

    /**
     * 赠送积分
     */
    @Column(name = "give_integral")
    private Integer giveIntegral;

    /**
     * 彩票种类
     */
    @Column(name = "lottery_classify_id")
    private Integer lotteryClassifyId;

    /**
     * 彩票子分类
     */
    @Column(name = "lottery_play_classify_id")
    private Integer lotteryPlayClassifyId;

    /**
     * 是否猜中 0-未猜中 1-已猜中
     */
    @Column(name = "is_guess")
    private Integer isGuess;
    
    /**
     * 是否有胆 0-否 1-是
     */
    @Column(name = "is_dan")
    private Integer isDan;
    
    /**
     * 比赛时间
     */
    @Column(name = "match_time")
    private Date matchTime;
    
    @Transient
    private String playType;

    /**
     * 添加时间
     */
    @Column(name = "add_time")
    private Integer addTime;
    
    /**
     * 让球数
     */
    @Column(name = "fix_odds")
    private String fixedodds;

    public String getFixedodds() {
		return fixedodds;
	}

	public void setFixedodds(String fixedodds) {
		this.fixedodds = fixedodds;
	}

	/**
     * 获取记录编号
     *
     * @return order_detail_id - 记录编号
     */
    public Integer getOrderDetailId() {
        return orderDetailId;
    }

    /**
     * 设置记录编号
     *
     * @param orderDetailId 记录编号
     */
    public void setOrderDetailId(Integer orderDetailId) {
        this.orderDetailId = orderDetailId;
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
     * 获取订单编号
     *
     * @return order_id - 订单编号
     */
    public Integer getOrderId() {
        return orderId;
    }

    /**
     * 设置订单编号
     *
     * @param orderId 订单编号
     */
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    /**
     * 获取赛事id
     *
     * @return match_id - 赛事id
     */
    public Integer getMatchId() {
        return matchId;
    }

    /**
     * 设置赛事id
     *
     * @param matchId 赛事id
     */
    public void setMatchId(Integer matchId) {
        this.matchId = matchId;
    }

    /**
     * 获取场次
     *
     * @return changci - 场次
     */
    public String getChangci() {
        return changci;
    }

    /**
     * 设置场次
     *
     * @param changci 场次
     */
    public void setChangci(String changci) {
        this.changci = changci;
    }
    
    public String getIssue() {
		return issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	/**
     * 获取比赛双方球队
     *
     * @return match_team - 比赛双方球队
     */
    public String getMatchTeam() {
        return matchTeam;
    }

    /**
     * 设置比赛双方球队
     *
     * @param matchTeam 比赛双方球队
     */
    public void setMatchTeam(String matchTeam) {
        this.matchTeam = matchTeam;
    }

    /**
     * 获取比赛结果
     *
     * @return match_result - 比赛结果
     */
    public String getMatchResult() {
        return matchResult;
    }

    /**
     * 设置比赛结果
     *
     * @param matchResult 比赛结果
     */
    public void setMatchResult(String matchResult) {
        this.matchResult = matchResult;
    }

    /**
     * 获取彩票信息（投注内容）
     *
     * @return ticket_data - 彩票信息（投注内容）
     */
    public String getTicketData() {
        return ticketData;
    }

    /**
     * 设置彩票信息（投注内容）
     *
     * @param ticketData 彩票信息（投注内容）
     */
    public void setTicketData(String ticketData) {
        this.ticketData = ticketData;
    }

    /**
     * 获取中奖状态：0-未开奖,1-未中奖,2-已中奖
     *
     * @return ticket_status - 中奖状态：0-未开奖,1-未中奖,2-已中奖
     */
    public Integer getTicketStatus() {
        return ticketStatus;
    }

    /**
     * 设置中奖状态：0-未开奖,1-未中奖,2-已中奖
     *
     * @param ticketStatus 中奖状态：0-未开奖,1-未中奖,2-已中奖
     */
    public void setTicketStatus(Integer ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    /**
     * 获取赠送积分
     *
     * @return give_integral - 赠送积分
     */
    public Integer getGiveIntegral() {
        return giveIntegral;
    }

    /**
     * 设置赠送积分
     *
     * @param giveIntegral 赠送积分
     */
    public void setGiveIntegral(Integer giveIntegral) {
        this.giveIntegral = giveIntegral;
    }

    /**
     * 获取彩票种类
     *
     * @return lottery_classify_id - 彩票种类
     */
    public Integer getLotteryClassifyId() {
        return lotteryClassifyId;
    }

    /**
     * 设置彩票种类
     *
     * @param lotteryClassifyId 彩票种类
     */
    public void setLotteryClassifyId(Integer lotteryClassifyId) {
        this.lotteryClassifyId = lotteryClassifyId;
    }

    /**
     * 获取彩票子分类
     *
     * @return lottery_play_classify_id - 彩票子分类
     */
    public Integer getLotteryPlayClassifyId() {
        return lotteryPlayClassifyId;
    }

    /**
     * 设置彩票子分类
     *
     * @param lotteryPlayClassifyId 彩票子分类
     */
    public void setLotteryPlayClassifyId(Integer lotteryPlayClassifyId) {
        this.lotteryPlayClassifyId = lotteryPlayClassifyId;
    }

    /**
     * 获取是否猜中 0-未猜中 1-已猜中
     *
     * @return is_guess - 是否猜中 0-未猜中 1-已猜中
     */
    public Integer getIsGuess() {
        return isGuess;
    }

    /**
     * 设置是否猜中 0-未猜中 1-已猜中
     *
     * @param isGuess 是否猜中 0-未猜中 1-已猜中
     */
    public void setIsGuess(Integer isGuess) {
        this.isGuess = isGuess;
    }
    

    public String getPlayType() {
		return playType;
	}

	public void setPlayType(String playType) {
		this.playType = playType;
	}
	
	public Integer getIsDan() {
		return isDan;
	}

	public void setIsDan(Integer isDan) {
		this.isDan = isDan;
	}
	
	public Date getMatchTime() {
		return matchTime;
	}

	public void setMatchTime(Date matchTime) {
		this.matchTime = matchTime;
	}
	
	public String getOrderSn() {
		return orderSn;
	}

	public void setOrderSn(String orderSn) {
		this.orderSn = orderSn;
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
}