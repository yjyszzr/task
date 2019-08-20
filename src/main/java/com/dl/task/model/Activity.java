package com.dl.task.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "dl_activity")
public class Activity {
    /**
     * id
     */
    @Id
    @Column(name = "act_id")
    private Integer act_id;
    /**
     * 活动名称
     */
    @Column(name = "act_name")
    private String act_name;
    
    /**
     * 活动标题
     */
    @Column(name = "act_title")
    private String act_title;
    
    /**
     * 活动类型0 - 注册活动 1- 充值活动 2 - app 推广活动
     */
    @Column(name = "act_type")
    private Integer act_type;
    
    /**
     * 活动图片
     */
    @Column(name = "act_img")
    private String act_img;

    /**
     * 活动跳转url
     */
    @Column(name = "act_url")
    private String act_url;
    
    /**
     * 活动开始时间
     */
    @Column(name = "start_time")
    private Integer start_time;
    
    /**
     * 活动结束时间
     */
    @Column(name = "end_time")
    private Integer end_time;
    
    /**
     * 活动状态 0-有效 1-无效	
     */
    @Column(name = "is_finish")
    private Integer is_finish;
    
    /**
     * 限购数量
     */
    @Column(name = "purchase_num")
    private Integer purchase_num;
    
    /**
     * 审核状态 0- 未通过 1-通过	
     */
    @Column(name = "status")
    private Integer status;
    
    /**
     * 票种范围：0-全部 1-部分	
     */
    @Column(name = "use_range")
    private Integer use_range;
    
    /**
     * 奖励金额
     */
    @Column(name = "reward_money")
    private Double reward_money;
    
    /**
     * 邀请人数
     */
    @Column(name = "number")
    private Integer number;
    
    /**
     * 是否删除 1:删除0:未删除
     */
    @Column(name = "is_del")
    private String is_del;

	public Integer getAct_id() {
		return act_id;
	}

	public void setAct_id(Integer act_id) {
		this.act_id = act_id;
	}

	public String getAct_name() {
		return act_name;
	}

	public void setAct_name(String act_name) {
		this.act_name = act_name;
	}

	public String getAct_title() {
		return act_title;
	}

	public void setAct_title(String act_title) {
		this.act_title = act_title;
	}

	public Integer getAct_type() {
		return act_type;
	}

	public void setAct_type(Integer act_type) {
		this.act_type = act_type;
	}

	public String getAct_img() {
		return act_img;
	}

	public void setAct_img(String act_img) {
		this.act_img = act_img;
	}

	public String getAct_url() {
		return act_url;
	}

	public void setAct_url(String act_url) {
		this.act_url = act_url;
	}

	public Integer getStart_time() {
		return start_time;
	}

	public void setStart_time(Integer start_time) {
		this.start_time = start_time;
	}

	public Integer getEnd_time() {
		return end_time;
	}

	public void setEnd_time(Integer end_time) {
		this.end_time = end_time;
	}

	public Integer getIs_finish() {
		return is_finish;
	}

	public void setIs_finish(Integer is_finish) {
		this.is_finish = is_finish;
	}

	public Integer getPurchase_num() {
		return purchase_num;
	}

	public void setPurchase_num(Integer purchase_num) {
		this.purchase_num = purchase_num;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getUse_range() {
		return use_range;
	}

	public void setUse_range(Integer use_range) {
		this.use_range = use_range;
	}

	public Double getReward_money() {
		return reward_money;
	}

	public void setReward_money(Double reward_money) {
		this.reward_money = reward_money;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getIs_del() {
		return is_del;
	}

	public void setIs_del(String is_del) {
		this.is_del = is_del;
	}


}