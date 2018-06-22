package com.dl.task.model;

import java.math.BigDecimal;
import javax.persistence.*;

@Table(name = "dl_user_account")
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 流水号
     */
    @Column(name = "account_sn")
    private String accountSn;

    /**
     * 变动用户
     */
    @Column(name = "user_id")
    private Integer userId;

    /**
     * 操作人
     */
    @Column(name = "admin_user")
    private String adminUser;

    /**
     * 操作金额
     */
    private BigDecimal amount;

    /**
     * 当前变动后的总余额
     */
    @Column(name = "cur_balance")
    private BigDecimal curBalance;


    /**
     * 添加时间
     */
    @Column(name = "add_time")
    private Integer addTime;

    /**
     * 更新时间
     */
    @Column(name = "last_time")
    private Integer lastTime;

    /**
     * 操作类型
     */
    @Column(name = "process_type")
    private Integer processType;


    /**
     * 支付方式名称
     */
    @Column(name = "payment_name")
    private String paymentName;

    @Column(name = "order_sn")
    private String orderSn;

    @Column(name = "parent_sn")
    private String parentSn;

    /**
     * 操作备注
     */
    private String note;
    
    @Column(name = "third_part_name")
    private String thirdPartName;
    
    @Column(name = "user_surplus")
    private BigDecimal userSurplus;
    
    @Column(name = "user_surplus_limit")
    private BigDecimal userSurplusLimit;
    
    @Column(name = "third_part_paid")
    private BigDecimal thirdPartPaid;
    
    @Column(name = "pay_id")
    private String payId;
    
    @Column(name = "status")
    private Integer status;
    
    @Column(name = "bonus_price")
    private BigDecimal bonusPrice;
    
    public BigDecimal getBonusPrice() {
		return bonusPrice;
	}

	public void setBonusPrice(BigDecimal bonusPrice) {
		this.bonusPrice = bonusPrice;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getThirdPartName() {
		return thirdPartName;
	}

	public void setThirdPartName(String thirdPartName) {
		this.thirdPartName = thirdPartName;
	}

	public BigDecimal getUserSurplus() {
		return userSurplus;
	}

	public void setUserSurplus(BigDecimal userSurplus) {
		this.userSurplus = userSurplus;
	}

	public BigDecimal getUserSurplusLimit() {
		return userSurplusLimit;
	}

	public void setUserSurplusLimit(BigDecimal userSurplusLimit) {
		this.userSurplusLimit = userSurplusLimit;
	}

	public BigDecimal getThirdPartPaid() {
		return thirdPartPaid;
	}

	public void setThirdPartPaid(BigDecimal thirdPartPaid) {
		this.thirdPartPaid = thirdPartPaid;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

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
     * 获取流水号
     *
     * @return account_sn - 流水号
     */
    public String getAccountSn() {
        return accountSn;
    }

    /**
     * 设置流水号
     *
     * @param accountSn 流水号
     */
    public void setAccountSn(String accountSn) {
        this.accountSn = accountSn;
    }

    /**
     * 获取变动用户
     *
     * @return user_id - 变动用户
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * 设置变动用户
     *
     * @param userId 变动用户
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * 获取操作人
     *
     * @return admin_user - 操作人
     */
    public String getAdminUser() {
        return adminUser;
    }

    /**
     * 设置操作人
     *
     * @param adminUser 操作人
     */
    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    /**
     * 获取操作金额
     *
     * @return amount - 操作金额
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * 设置操作金额
     *
     * @param amount 操作金额
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * 获取当前变动后的总余额
     *
     * @return cur_balance - 当前变动后的总余额
     */
    public BigDecimal getCurBalance() {
        return curBalance;
    }

    /**
     * 设置当前变动后的总余额
     *
     * @param curBalance 当前变动后的总余额
     */
    public void setCurBalance(BigDecimal curBalance) {
        this.curBalance = curBalance;
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
     * 获取更新时间
     *
     * @return last_time - 更新时间
     */
    public Integer getLastTime() {
        return lastTime;
    }

    /**
     * 设置更新时间
     *
     * @param lastTime 更新时间
     */
    public void setLastTime(Integer lastTime) {
        this.lastTime = lastTime;
    }

    /**
     * 获取操作类型
     *
     * @return process_type - 操作类型
     */
    public Integer getProcessType() {
        return processType;
    }

    /**
     * 设置操作类型
     *
     * @param processType 操作类型
     */
    public void setProcessType(Integer processType) {
        this.processType = processType;
    }

    /**
     * 获取支付方式名称
     *
     * @return payment_name - 支付方式名称
     */
    public String getPaymentName() {
        return paymentName;
    }

    /**
     * 设置支付方式名称
     *
     * @param paymentName 支付方式名称
     */
    public void setPaymentName(String paymentName) {
        this.paymentName = paymentName;
    }

    /**
     * @return order_sn
     */
    public String getOrderSn() {
        return orderSn;
    }

    /**
     * @param orderSn
     */
    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    /**
     * @return parent_sn
     */
    public String getParentSn() {
        return parentSn;
    }

    /**
     * @param parentSn
     */
    public void setParentSn(String parentSn) {
        this.parentSn = parentSn;
    }

    /**
     * 获取操作备注
     *
     * @return note - 操作备注
     */
    public String getNote() {
        return note;
    }

    /**
     * 设置操作备注
     *
     * @param note 操作备注
     */
    public void setNote(String note) {
        this.note = note;
    }
}