package com.dl.task.model;

import java.util.List;

import lombok.Data;

@Data
public class ReqOrdeEntity {

	private String orderSn;
	private double reward;
	private int userId;
	private double userMoney;
	private double betMoney;
	private String betTime;
	private String note;
	private List<ReqOrdeEntity> userIdAndRewardList;
}
