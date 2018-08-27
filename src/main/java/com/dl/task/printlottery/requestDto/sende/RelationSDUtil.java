package com.dl.task.printlottery.requestDto.sende;

import java.util.HashMap;
import java.util.Map;

public class RelationSDUtil {
	
	private static Map<String,String> lotteryTypeMap = new HashMap<String, String>();
	private static Map<String,String> playTypeRelationMap = new HashMap<String, String>();
	private static Map<String,String> betTypeRelationMap = new HashMap<String, String>();
	private static Map<String,Map<String,String>> playTypeMap = new HashMap<String, Map<String,String>>();
	private static Map<String,String> RQSPFMap = new HashMap<String, String>();
	private static Map<String,String> SPFMap = new HashMap<String, String>();
	private static Map<String,String> BFMap = new HashMap<String, String>();
	private static Map<String,String> JQSMap = new HashMap<String, String>();
	private static Map<String,String> BQCMap = new HashMap<String, String>();
	static{
		lotteryTypeMap.put("T51", "JCZQ");
		
		betTypeRelationMap.put("11", "P1");
		betTypeRelationMap.put("21", "P2_1");
		betTypeRelationMap.put("31", "P3_1");
		betTypeRelationMap.put("41", "P4_1");
		betTypeRelationMap.put("51", "P5_1");
		betTypeRelationMap.put("61", "P6_1");
		betTypeRelationMap.put("71", "P7_1");
		betTypeRelationMap.put("81", "P8_1");
//		竞彩投注彩小秘对微彩对应关系
		playTypeRelationMap.put("01", "RQSPF");//让球胜平负
		playTypeRelationMap.put("02", "SPF");//胜平负
		playTypeRelationMap.put("03", "BF");//比分
		playTypeRelationMap.put("04", "JQS");//总进球
		playTypeRelationMap.put("05", "BQC");//半全场
		playTypeRelationMap.put("06", "HHGG");//混合投注
		
		SPFMap.put("0", "LOSE");
		SPFMap.put("1", "DRAW");
		SPFMap.put("3", "WIN");
		playTypeMap.put("SPF", SPFMap);
		
		RQSPFMap.put("0", "RQLOSE");
		RQSPFMap.put("1", "RQDRAW");
		RQSPFMap.put("3", "RQWIN");
		playTypeMap.put("RQSPF", RQSPFMap);
		
		JQSMap.put("0", "S0");
		JQSMap.put("1", "S1");
		JQSMap.put("2", "S2");
		JQSMap.put("3", "S3");
		JQSMap.put("4", "S4");
		JQSMap.put("5", "S5");
		JQSMap.put("6", "S6");
		JQSMap.put("7", "S7");
		playTypeMap.put("JQS", JQSMap);
		
		BQCMap.put("33", "WIN_WIN");
		BQCMap.put("31", "WIN_DRAW");
		BQCMap.put("30", "WIN_LOSE");
		BQCMap.put("13", "DRAW_WIN");
		BQCMap.put("11", "DRAW_DRAW");
		BQCMap.put("10", "DRAW_LOSE");
		BQCMap.put("03", "LOSE_WIN");
		BQCMap.put("01", "LOSE_DRAW");
		BQCMap.put("00", "LOSE_LOSE");
		playTypeMap.put("BQC", BQCMap);
		
		BFMap.put("10", "WIN10");		BFMap.put("01", "LOSE01");
		BFMap.put("20", "WIN20");		BFMap.put("02", "LOSE02");
		BFMap.put("21", "WIN21");		BFMap.put("12", "LOSE12");
		BFMap.put("30", "WIN30");		BFMap.put("03", "LOSE03");
		BFMap.put("31", "WIN31");		BFMap.put("13", "LOSE13");
		BFMap.put("32", "WIN32");		BFMap.put("23", "LOSE23");
		BFMap.put("40", "WIN40");		BFMap.put("04", "LOSE04");
		BFMap.put("41", "WIN41");		BFMap.put("14", "LOSE14");
		BFMap.put("42", "WIN42");		BFMap.put("24", "LOSE24");
		BFMap.put("50", "WIN50");		BFMap.put("05", "LOSE05");
		BFMap.put("51", "WIN51");		BFMap.put("15", "LOSE15");
		BFMap.put("52", "WIN52");		BFMap.put("25", "LOSE25");
		BFMap.put("90", "WIN_OTHER");	BFMap.put("09", "LOSE_OTHER");
		BFMap.put("00", "DRAW00");
		BFMap.put("11", "DRAW11");
		BFMap.put("22", "DRAW22");
		BFMap.put("33", "DRAW33");
		BFMap.put("99", "DRAW_OTHER");
		playTypeMap.put("BF", BFMap);

	}
	/**
	 * 返回玩法
	 */
	public static String getPlayType(String paly) {
		return playTypeRelationMap.get(paly);
	}
	/**
	 * 过关方式
	 * @param bet
	 * @return
	 */
	public static String getBetType(String bet) {
		return betTypeRelationMap.get(bet);
	}
	
	public static Map<String,String> getValueMap(String play,String val){
		Map<String,String> map = new HashMap<>();
		String playType = getPlayType(play);
		String[] nums = val.split(",");
		String playText = "";
		for(String num: nums) {
			playText = playText +playTypeMap.get(playType).get(num)+",";
		}
		map.put(playType, playText.substring(0,playText.length()-1));
		return map;
	}
}
