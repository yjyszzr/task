package com.dl.task.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
public class ReqOrdeEntityForUserAccount {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "order_sn")
	private String orderSn;

	@Column(name = "reward")
	private double reward;

	@Column(name = "user_id")
	private int userId;

	@Column(name = "note")
	private String note;

	@Column(name = "create_time")
	private String createTime;

	@Column(name = "status")
	private int status;
}
