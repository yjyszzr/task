package com.dl.task.dao2;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DlLeagueMatchResult;

public interface DlLeagueMatchResultMapper extends Mapper<DlLeagueMatchResult> {

	int getCountByChangciId(@Param("changciId")Integer changciId);
	
	List<DlLeagueMatchResult> queryMatchResultByPlayCode(@Param("playCode")String playCode);

	List<DlLeagueMatchResult> queryMatchResultsByPlayCodes(@Param("playCodes")List<String> playCodes);
}