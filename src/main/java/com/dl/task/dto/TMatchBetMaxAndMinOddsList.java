package com.dl.task.dto;

import java.util.List;

import lombok.Data;

@Data
public class TMatchBetMaxAndMinOddsList {

	List<Double> minOddsList;//投注每场最小赔率列表
	List<Double> maxOddsList;//投注每场最小赔率列表
}
