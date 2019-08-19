package com.dl.task.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "dl_activity_user_info")
@Data
public class ActivityUserInfo {
    /**
     * id
     */
    @Id
    @Column(name = "id")
    private Integer id;
  
    /**
     * 用户id
     */
    @Column(name = "user_id")
    private Integer user_id;
  
    /**
     * 手机号码
     */
    @Column(name = "mobile")
    private String mobile;
    
    /**
     * 邀请人数
     */
    @Column(name = "invitation_number")
    private Integer invitation_number;
    
    /**
     * 邀请人数奖励
     */
    @Column(name = "invitation_number_reward")
    private Double invitation_number_reward;
    
    /**
     *历史 邀请人数
     */
    @Column(name = "history_invitation_number")
    private Integer history_invitation_number;
    
    /**
     *历史 邀请人数奖励
     */
    @Column(name = "history_invitation_number_reward")
    private Double history_invitation_number_reward;
    
    /**
     * 可提现收益
     */
    @Column(name = "withdrawable_reward")
    private Double withdrawable_reward;
    
    /**
     * 历史可提现总收益
     */
    @Column(name = "history_total_withdrawable_reward")
    private Double history_total_withdrawable_reward;
    
    /**
     * 当月返利
     */
    @Column(name = "month_return_reward")
    private Double month_return_reward;
    
    /**
     * 历史总返利
     */
    @Column(name = "history_total_return_reward")
    private Double history_total_return_reward;
    
    /**
     * 好友累计购彩金额
     */
    @Column(name = "invitation_add_reward")
    private Double invitation_add_reward;
    
    /**
     * 好友累计购彩额外奖励
     */
    @Column(name = "buy_add_reward")
    private Double buy_add_reward;
    

}