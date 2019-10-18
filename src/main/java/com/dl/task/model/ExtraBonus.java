package com.dl.task.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Table(name = "dl_old_belt_new_extra_bonus")
public class ExtraBonus {
	@Id
	@Column(name = "id")
	private Integer id;
	/**
	 * 邀请人Id
	 */
	@Column(name = "user_id")
	private Integer userId;

	/**
	 * 奖励金额
	 */
	@Column(name = "extra_bonus")
	private Integer extraBonus;
}
