package com.dl.task.dao2;

import com.dl.base.mapper.Mapper;
import com.dl.task.dto.BasketBallLeagueInfoDTO;
import com.dl.task.model.DlMatchBasketball;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DlMatchBasketballMapper extends Mapper<DlMatchBasketball> {
	/**
	 * 获取取消的赛事编码
	 * @param playCodes
	 * @return
	 */
	public List<String> getCancelMatches(@Param("playCodes") List<String> playCodes);
	
	/**
	 * 根据多个play_code查询DlMatchBasketball集合
	 * @param playCodes
	 * @return
	 */
	public List<DlMatchBasketball> getChangciIdsFromBasketMatchByPlayCodes(@Param("playCodes") List<String> playCodes);


    /**
     * 根据多个play_code查询结束的DlMatchBasketball集合
     * @param playCodes
     * @return
     */
    public List<DlMatchBasketball> getEndBasketMatchByPlayCodes(@Param("playCodes") List<String> playCodes);
	/**
	 * 
	 * @param leagueId 
	 * @param playType获取赛事列表
	 * @return
	 */
	public List<DlMatchBasketball> getMatchList(@Param("leagueIds")String leagueId);
	
	/**
	 * 获取赛事列表的联赛信息
	 * @return
	 */
	public List<BasketBallLeagueInfoDTO> getBasketBallFilterConditions();
	
	
	/**
	 * 根据查询条件查询赛事结果
	 * @param dateStr
	 * @return
	 */
	public List<DlMatchBasketball> queryMatchByQueryCondition(@Param("dateStr") String dateStr,@Param("matchIdArr") String[] matchIdArr,
			@Param("leagueIdArr") String[] leagueIdArr,@Param("matchFinish") String matchFinish);
	
	
	
	/**
	 * 查询最近一场比赛
	 * @param dateStr
	 * @return
	 */
	public List<DlMatchBasketball> queryLatestMatch();
	
	
}