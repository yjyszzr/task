package com.dl.task.service;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dl.base.enums.MatchResultCrsEnum;
import com.dl.base.enums.MatchResultHadEnum;
import com.dl.base.service.AbstractService;
import com.dl.task.dao2.DlMatchResultMapper;
import com.dl.task.model.DlMatchResult;

@Service
@Transactional(value="transactionManager2")
public class DlMatchResultService extends AbstractService<DlMatchResult> {
    @Resource
    private DlMatchResultMapper dlMatchResultMapper;

    //定时刷新计算比赛结果
    public void refreshMatchResult() {
    	List<DlMatchResult> goGetMatchResult = dlMatchResultMapper.goGetMatchResult();
    	if(CollectionUtils.isNotEmpty(goGetMatchResult)) {
    		List<DlMatchResult> updateResults = new ArrayList<DlMatchResult>(goGetMatchResult.size());
    		for(DlMatchResult matchResult: goGetMatchResult) {
    			String firstHalf = matchResult.getFirstHalf();
    			String whole = matchResult.getWhole();
    			String goalline = matchResult.getGoalline();
    			if(StringUtils.isBlank(firstHalf) || StringUtils.isBlank(whole) || StringUtils.isBlank(goalline)) {
    				continue;
    			}
    			String[] split = firstHalf.split(":");
    			Integer p = Integer.parseInt(split[0]);
    			Integer q = Integer.parseInt(split[1]);
    			String[] split2 = whole.split(":");
    			Integer m = Integer.parseInt(split2[0]);
    			Integer n = Integer.parseInt(split2[1]);
    			Integer a = m - n;
    			Integer b = p - q;
    			Integer s = Integer.parseInt(goalline);
    			Integer sum = m + n;
    			//胜平负
    			String hadCodeStr = "";
    			if(a > 0) {
    				hadCodeStr = MatchResultHadEnum.HAD_H.getCode().toString();
    			} else if(a < 0) {
    				hadCodeStr = MatchResultHadEnum.HAD_A.getCode().toString();
    			} else {
    				hadCodeStr = MatchResultHadEnum.HAD_D.getCode().toString();
    			}
    			matchResult.setHad(hadCodeStr);
    			//让球
    			if(a+s > 0) {
    				matchResult.setHhad(MatchResultHadEnum.HAD_H.getCode().toString());
    			} else if(a+s < 0) {
    				matchResult.setHhad(MatchResultHadEnum.HAD_A.getCode().toString());
    			} else {
    				matchResult.setHhad(MatchResultHadEnum.HAD_D.getCode().toString());
    			}
    			//半全场
    			String halfCodeStr = "";
    			if(b > 0) {
    				halfCodeStr = MatchResultHadEnum.HAD_H.getCode().toString();
    			} else if(b < 0) {
    				halfCodeStr = MatchResultHadEnum.HAD_A.getCode().toString();
    			} else {
    				halfCodeStr = MatchResultHadEnum.HAD_D.getCode().toString();
    			}
    			matchResult.setHafu(halfCodeStr + hadCodeStr);
    			//总进球
    			if(sum > 6) {
    				matchResult.setTtg("7");
    			} else {
    				matchResult.setTtg(sum.toString());
    			}
    			//比分
    			if(sum > 7) {
    				if(m > n) {
    					matchResult.setCrs(MatchResultCrsEnum.CRS_90.getCode());
    				} else if(m < n) {
    					matchResult.setCrs(MatchResultCrsEnum.CRS_09.getCode());
    				} else {
    					matchResult.setCrs(MatchResultCrsEnum.CRS_99.getCode());
    				}
    			} else {
    				matchResult.setCrs(whole);
    			}
    			updateResults.add(matchResult);
    		}
    		if(CollectionUtils.isNotEmpty(updateResults)) {
    			for(DlMatchResult matchResult: updateResults) {
    				int rst = dlMatchResultMapper.updateMatchResult(matchResult);
    			}
    		}
    	}
    }
}
