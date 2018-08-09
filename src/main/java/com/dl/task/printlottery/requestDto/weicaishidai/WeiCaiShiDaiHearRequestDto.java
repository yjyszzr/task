package com.dl.task.printlottery.requestDto.weicaishidai;

import lombok.Data;

@Data
public class WeiCaiShiDaiHearRequestDto {
	String cmd;
	String digest;
	String digestType;
	String userId;
	String timeStamp;
	String userType;
}
