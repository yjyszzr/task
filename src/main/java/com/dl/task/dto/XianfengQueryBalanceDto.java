package com.dl.task.dto;

import lombok.Data;

@Data
public class XianfengQueryBalanceDto {
	private String resCode;
	private String resMessage;
	private String balance;
	private String freezeAmount;
	private String available;
	private String nonAdvance;
	private String previousBalance;
}
