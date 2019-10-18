package com.dl.task.dto;

import java.util.List;

import lombok.Data;

@Data
public class TicketPlayInfo {

	private Integer playType;
	private String fixedodds;
	private List<CellInfo> cellInfos;
}
