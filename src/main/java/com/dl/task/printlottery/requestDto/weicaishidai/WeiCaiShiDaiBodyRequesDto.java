package com.dl.task.printlottery.requestDto.weicaishidai;

import java.util.List;

import lombok.Data;

@Data
public class WeiCaiShiDaiBodyRequesDto {
	private List<WeiCaiShiDaiBodyTicketRequesDto> tickets;
	private String uuid;
	@Data
	public static class WeiCaiShiDaiBodyTicketRequesDto {
		private String game_id;
		private String play_type;
		private String bet_type;
		private String out_id;
		private String multiple;
		private String number;
		private String icount;
		private String amount;
		private String term_code;
//		private String end_time;
	}
}
