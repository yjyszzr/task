package com.dl.task.dto;

import lombok.Data;

import com.dl.task.model.DlTicketChannel;
import com.dl.task.model.DlTicketChannelLotteryClassify;
@Data
public class PrintChannelInfo {
	private DlTicketChannel channel;
	private DlTicketChannelLotteryClassify classify;
}
