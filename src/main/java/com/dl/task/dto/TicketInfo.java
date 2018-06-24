package com.dl.task.dto;

import java.util.List;

import lombok.Data;

@Data
public class TicketInfo {

	private String playCode;
	private int isDan;
	private List<TicketPlayInfo> ticketPlayInfos;
}
