package com.dl.task.model;

import javax.persistence.*;

@Table(name = "dl_lottery_classify")
public class DlLotteryClassify {
    /**
     * 彩票分类表id
     */
    @Id
    @Column(name = "lottery_classify_id")
    private Integer lotteryClassifyId;

    /**
     * 彩票名称
     */
    @Column(name = "lottery_name")
    private String lotteryName;

    /**
     * 彩票图片
     */
    @Column(name = "lottery_img")
    private String lotteryImg;

    /**
     * 副标题
     */
    @Column(name = "sub_title")
    private String subTitle;

    /**
     * 彩票排序
     */
    private Integer sort;

    /**
     * 彩票状态 0-售卖 1-停售 2-未开售
     */
    private Integer status;

    /**
     * 是否显示 0-不显示 1-显示
     */
    @Column(name = "is_show")
    private Integer isShow;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Integer createTime;

    /**
     * 跳转链接地址
     */
    @Column(name = "redirect_url")
    private String redirectUrl;

    /**
     * 状态说明
     */
    @Column(name = "status_reason")
    private String statusReason;

    /**
     * 获取彩票分类表id
     *
     * @return lottery_classify_id - 彩票分类表id
     */
    public Integer getLotteryClassifyId() {
        return lotteryClassifyId;
    }

    /**
     * 设置彩票分类表id
     *
     * @param lotteryClassifyId 彩票分类表id
     */
    public void setLotteryClassifyId(Integer lotteryClassifyId) {
        this.lotteryClassifyId = lotteryClassifyId;
    }

    /**
     * 获取彩票名称
     *
     * @return lottery_name - 彩票名称
     */
    public String getLotteryName() {
        return lotteryName;
    }

    /**
     * 设置彩票名称
     *
     * @param lotteryName 彩票名称
     */
    public void setLotteryName(String lotteryName) {
        this.lotteryName = lotteryName;
    }

    /**
     * 获取彩票图片
     *
     * @return lottery_img - 彩票图片
     */
    public String getLotteryImg() {
        return lotteryImg;
    }

    /**
     * 设置彩票图片
     *
     * @param lotteryImg 彩票图片
     */
    public void setLotteryImg(String lotteryImg) {
        this.lotteryImg = lotteryImg;
    }

    /**
     * 获取副标题
     *
     * @return sub_title - 副标题
     */
    public String getSubTitle() {
        return subTitle;
    }

    /**
     * 设置副标题
     *
     * @param subTitle 副标题
     */
    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    /**
     * 获取彩票排序
     *
     * @return sort - 彩票排序
     */
    public Integer getSort() {
        return sort;
    }

    /**
     * 设置彩票排序
     *
     * @param sort 彩票排序
     */
    public void setSort(Integer sort) {
        this.sort = sort;
    }

    /**
     * 获取彩票状态 0-售卖 1-停售 2-未开售
     *
     * @return status - 彩票状态 0-售卖 1-停售 2-未开售
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置彩票状态 0-售卖 1-停售 2-未开售
     *
     * @param status 彩票状态 0-售卖 1-停售 2-未开售
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取是否显示 0-不显示 1-显示
     *
     * @return is_show - 是否显示 0-不显示 1-显示
     */
    public Integer getIsShow() {
        return isShow;
    }

    /**
     * 设置是否显示 0-不显示 1-显示
     *
     * @param isShow 是否显示 0-不显示 1-显示
     */
    public void setIsShow(Integer isShow) {
        this.isShow = isShow;
    }

    /**
     * 获取创建时间
     *
     * @return create_time - 创建时间
     */
    public Integer getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取跳转链接地址
     *
     * @return redirect_url - 跳转链接地址
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * 设置跳转链接地址
     *
     * @param redirectUrl 跳转链接地址
     */
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    /**
     * 获取状态说明
     *
     * @return status_reason - 状态说明
     */
    public String getStatusReason() {
        return statusReason;
    }

    /**
     * 设置状态说明
     *
     * @param statusReason 状态说明
     */
    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }
}