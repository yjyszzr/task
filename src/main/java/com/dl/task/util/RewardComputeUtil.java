package com.dl.task.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RewardComputeUtil {
	public static void main(String[] args) {
//		select concat('tickesList.add("',times,'=',reward_stakes,'");') from dl_print_lottery where order_sn='2018090709180471620363' and real_reward_money>0;
		List<String> tickesList = new ArrayList<String>();
		tickesList.add("2=01|201809167053|0@2.260;01|201809167048|3@2.630;01|201809167047|3@1.460");
		tickesList.add("2=02|201809167054|3@1.160;01|201809167048|3@2.630;01|201809167047|3@1.460");
		tickesList.add("2=02|201809167054|3@1.160;01|201809167053|0@2.260;01|201809167047|3@1.460");
		tickesList.add("2=02|201809167054|3@1.160;01|201809167053|0@2.260;01|201809167048|3@2.630");
		StringBuffer orderRewardStr = new StringBuffer();
		BigDecimal orderReward = new BigDecimal("0.0");
		BigDecimal two = new BigDecimal(2.0);
		for(String temp:tickesList){
			String[] timeArr = temp.split("=");
			StringBuffer oneReward = new StringBuffer();
			BigDecimal times = new BigDecimal(timeArr[0]);
			BigDecimal reward = new BigDecimal("1.0").multiply(two);
			oneReward.append(two);
			String[] cellSpArr = timeArr[1].split(";");
			for(String cellSp:cellSpArr){
				String[] spArr = cellSp.split("@");
				BigDecimal sp = new BigDecimal(spArr[1]);
				oneReward.append("*");
				oneReward.append(sp);
				reward = reward.multiply(sp);
			}
			reward = reward.setScale(3,RoundingMode.DOWN);
			System.out.println(reward);
	    	reward=reward.setScale(2,RoundingMode.HALF_EVEN);
	    	System.out.println(reward);
			reward=reward.multiply(times);
			oneReward.append("*");
			oneReward.append(times);
			oneReward.append("=");
			oneReward.append(reward);
			orderRewardStr.append(reward);
			orderRewardStr.append("+");
			orderReward=orderReward.add(reward);
			System.out.println(oneReward.toString());
		}
		orderRewardStr.deleteCharAt(orderRewardStr.length()-1);
		orderRewardStr.append("=");
		orderRewardStr.append(orderReward);
		System.out.println("订单总金额:"+orderRewardStr.toString());
	}
}
