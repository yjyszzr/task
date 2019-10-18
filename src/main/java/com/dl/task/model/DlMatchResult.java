package com.dl.task.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "dl_match_result")
public class DlMatchResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 场次id
     */
    @Column(name = "changci_id")
    private Integer changciId;

    /**
     * 半场比分
     */
    @Column(name = "first_half")
    private String firstHalf;

    /**
     * 全场比分
     */
    private String whole;

    /**
     * 比赛状态
     */
    @Column(name = "match_status")
    private String matchStatus;

    /**
     * 比赛进行时间
     */
    @Column(name = "match_minutes")
    private String matchMinutes;

    /**
     * 让球数
     */
    private String goalline;

    /**
     * 胜平负
     */
    private String had;

    /**
     * 让球胜平负
     */
    private String hhad;

    /**
     * 总进球
     */
    private String ttg;

    /**
     * 半全场
     */
    private String hafu;

    /**
     * 比分
     */
    private String crs;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
    /**
     * 0未进行计算1已进行计算2已确认可开奖
     */
    @Column(name = "status")
    private Date status;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取场次id
     *
     * @return changci_id - 场次id
     */
    public Integer getChangciId() {
        return changciId;
    }

    /**
     * 设置场次id
     *
     * @param changciId 场次id
     */
    public void setChangciId(Integer changciId) {
        this.changciId = changciId;
    }

    /**
     * 获取半场比分
     *
     * @return first_half - 半场比分
     */
    public String getFirstHalf() {
        return firstHalf;
    }

    /**
     * 设置半场比分
     *
     * @param firstHalf 半场比分
     */
    public void setFirstHalf(String firstHalf) {
        this.firstHalf = firstHalf;
    }

    /**
     * 获取全场比分
     *
     * @return whole - 全场比分
     */
    public String getWhole() {
        return whole;
    }

    /**
     * 设置全场比分
     *
     * @param whole 全场比分
     */
    public void setWhole(String whole) {
        this.whole = whole;
    }

    /**
     * 获取比赛状态
     *
     * @return match_status - 比赛状态
     */
    public String getMatchStatus() {
        return matchStatus;
    }

    /**
     * 设置比赛状态
     *
     * @param matchStatus 比赛状态
     */
    public void setMatchStatus(String matchStatus) {
        this.matchStatus = matchStatus;
    }

    /**
     * 获取比赛进行时间
     *
     * @return match_minutes - 比赛进行时间
     */
    public String getMatchMinutes() {
        return matchMinutes;
    }

    /**
     * 设置比赛进行时间
     *
     * @param matchMinutes 比赛进行时间
     */
    public void setMatchMinutes(String matchMinutes) {
        this.matchMinutes = matchMinutes;
    }

    /**
     * 获取让球数
     *
     * @return goalline - 让球数
     */
    public String getGoalline() {
        return goalline;
    }

    /**
     * 设置让球数
     *
     * @param goalline 让球数
     */
    public void setGoalline(String goalline) {
        this.goalline = goalline;
    }

    /**
     * 获取胜平负
     *
     * @return had - 胜平负
     */
    public String getHad() {
        return had;
    }

    /**
     * 设置胜平负
     *
     * @param had 胜平负
     */
    public void setHad(String had) {
        this.had = had;
    }

    /**
     * 获取让球胜平负
     *
     * @return hhad - 让球胜平负
     */
    public String getHhad() {
        return hhad;
    }

    /**
     * 设置让球胜平负
     *
     * @param hhad 让球胜平负
     */
    public void setHhad(String hhad) {
        this.hhad = hhad;
    }

    /**
     * 获取总进球
     *
     * @return ttg - 总进球
     */
    public String getTtg() {
        return ttg;
    }

    /**
     * 设置总进球
     *
     * @param ttg 总进球
     */
    public void setTtg(String ttg) {
        this.ttg = ttg;
    }

    /**
     * 获取半全场
     *
     * @return hafu - 半全场
     */
    public String getHafu() {
        return hafu;
    }

    /**
     * 设置半全场
     *
     * @param hafu 半全场
     */
    public void setHafu(String hafu) {
        this.hafu = hafu;
    }

    /**
     * 获取比分
     *
     * @return crs - 比分
     */
    public String getCrs() {
        return crs;
    }

    /**
     * 设置比分
     *
     * @param crs 比分
     */
    public void setCrs(String crs) {
        this.crs = crs;
    }

    /**
     * @return create_time
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * @return update_time
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * @param updateTime
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

	public Date getStatus() {
		return status;
	}

	public void setStatus(Date status) {
		this.status = status;
	}
    
}