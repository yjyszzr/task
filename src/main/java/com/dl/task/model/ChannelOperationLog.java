package com.dl.task.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Table(name = "dl_channel_option_log")
@Data
public class ChannelOperationLog {
	@Id
	@Column(name = "option_id")
	private Integer optionId;
	/**
	 * 用户Id(与用户表做关联)
	 */
	@Column(name = "user_id")
	private Integer userId;

	/**
	 * 渠道店员Id
	 */
	@Column(name = "distributor_id")
	private Integer distributorId;
	/**
	 * 渠道Id
	 */
	@Column(name = "channel_id")
	private Integer channelId;
	/**
	 * 操作时间
	 */
	@Column(name = "option_time")
	private Integer optionTime;
	/**
	 * 操作节点
	 */
	@Column(name = "operation_node")
	private Integer operationNode;
	/**
	 * 状态
	 */
	@Column(name = "status")
	private Integer status;
	/**
	 * 用户名称
	 */
	@Column(name = "user_name")
	private String userName;
	/**
	 * 电话
	 */
	@Column(name = "mobile")
	private String mobile;
	/**
	 * 订单来源
	 */
	@Column(name = "source")
	private String source;

	/**
	 * 订单实付金额
	 */
	@Column(name = "option_amount")
	private BigDecimal optionAmount;
	
	/**
	 * 订单号 
	 */
	@Column(name = "order_sn")
	private String orderSn;
}
