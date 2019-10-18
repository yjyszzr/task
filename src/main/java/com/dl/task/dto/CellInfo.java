package com.dl.task.dto;

import lombok.Data;

@Data
public class CellInfo {

	private String cellCode;
	private String cellOdds;
	private String cellName;
	
	public CellInfo(){}
	
	public CellInfo(String cellCode, String cellName){
		this.cellCode = cellCode;
		this.cellName = cellName;
	}
}
