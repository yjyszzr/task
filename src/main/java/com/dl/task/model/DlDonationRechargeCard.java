package com.dl.task.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
@Data
@Table(name = "dl_donation_recharge_card")
public class DlDonationRechargeCard {
	/**
     * 充值卡id
     */
    @Id
    @Column(name = "recharge_card_id")
    private Integer rechargeCardId;
 
    /**
     * 充值卡名称
     */
    private String name;

    /**
     * 充值卡图片路径
     */
    @Column(name = "img_url")
    private String imgUrl;

    /**
     * 0代表赠品可用,1代表赠品不可用
     */
    private Integer status;

    @Column(name = "add_user")
    private String addUser;

    /**
     * 创建时间
     */
    @Column(name = "add_time")
    private Integer addTime;

    private String description;

    /**
     * 0代表未被删除,1代表被删除
     */
    @Column(name = "is_delete")
    private Integer isDelete;
    
    /**
     * type
     */
    @Column(name = "type")
    private Integer type;
    
    /**
     * max_donation
     */
    @Column(name = "max_donation")
    private Integer maxDonation;
    
    /**
     * limit_recharge_money
     */
    @Column(name = "limit_recharge_money")
    private Integer limitRechargeMoney;
    
    /**
     * effective_day
     */
    @Column(name = "effective_day")
    private Integer effectiveDay;
    
    /**
     * real_value
     */
    @Column(name = "real_value")
    private BigDecimal real_value;
}