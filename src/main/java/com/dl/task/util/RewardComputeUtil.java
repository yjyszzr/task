package com.dl.task.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.tools.classfile.StackMapTable_attribute.append_frame;

public class RewardComputeUtil {
	public static void main(String[] args) {
		List<String> tickesList = new ArrayList<String>();
		tickesList.add("1=02|201808212017|1@4.100;02|201808212010|1@3.120;02|201808212007|3@2.300;02|201808212001|3@1.590");
		tickesList.add("1=02|201808212017|1@4.100;05|201808212010|11@4.400;02|201808212007|3@2.300;02|201808212001|3@1.590");
		tickesList.add("1=03|201808212017|11@7.200;02|201808212010|1@3.120;02|201808212007|3@2.300;02|201808212001|3@1.590");
		tickesList.add("1=03|201808212017|11@7.200;05|201808212010|11@4.400;02|201808212007|3@2.300;02|201808212001|3@1.590");
		tickesList.add("1=04|201808212017|2@3.200;02|201808212010|1@3.120;02|201808212007|3@2.300;02|201808212001|3@1.590");
		tickesList.add("1=04|201808212017|2@3.200;05|201808212010|11@4.400;02|201808212007|3@2.300;02|201808212001|3@1.590");
		tickesList.add("1=02|201808212017|1@4.100;02|201808212010|1@3.120;02|201808212007|3@2.300;04|201808212001|2@3.050");
		tickesList.add("1=02|201808212017|1@4.100;05|201808212010|11@4.400;02|201808212007|3@2.300;04|201808212001|2@3.050");
		tickesList.add("1=03|201808212017|11@7.200;02|201808212010|1@3.120;02|201808212007|3@2.300;04|201808212001|2@3.050");
		tickesList.add("1=03|201808212017|11@7.200;05|201808212010|11@4.400;02|201808212007|3@2.300;04|201808212001|2@3.050");
		tickesList.add("1=04|201808212017|2@3.200;02|201808212010|1@3.120;02|201808212007|3@2.300;04|201808212001|2@3.050");
		tickesList.add("1=04|201808212017|2@3.200;05|201808212010|11@4.400;02|201808212007|3@2.300;04|201808212001|2@3.050");
		BigDecimal two = new BigDecimal(2.0);
		for(String temp:tickesList){
			String[] timeArr = temp.split("=");
			StringBuffer oneReward = new StringBuffer();
			BigDecimal times = new BigDecimal(timeArr[0]);
			oneReward.append(times);
			BigDecimal reward = two.multiply(times);
			String[] cellSpArr = timeArr[1].split(";");
			for(String cellSp:cellSpArr){
				String[] spArr = cellSp.split("@");
				BigDecimal sp = new BigDecimal(spArr[1]);
				oneReward.append("*");
				oneReward.append(sp);
				reward = reward.multiply(sp);
			}
			oneReward.append("*");
			oneReward.append(times);
			oneReward.append("*");
			oneReward.append(two);
			oneReward.append("=");
			oneReward.append(reward.setScale(2,RoundingMode.HALF_EVEN));
			System.out.println(oneReward.toString());
		}
		
	}
}
