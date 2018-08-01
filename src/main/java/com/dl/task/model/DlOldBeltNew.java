package com.dl.task.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "dl_old_belt_new")
public class DlOldBeltNew {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/**
	 * 邀请人用户Id
	 */
	@Column(name = "inviter_user_id")
	private Integer inviterUserId;

	/**
	 * 邀请人加密用户Id
	 */
	@Column(name = "inviter_encryption_user_id")
	private String inviterEncryptionUserId;

	/**
	 * 注册人Id
	 */
	@Column(name = "register_user_id")
	private Integer registerUserId;

	/**
	 * 消费状态 >=20元为1,<20元为0
	 */
	@Column(name = "consumption_status")
	private Integer consumptionStatus;

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
	 * 获取邀请人用户Id
	 *
	 * @return inviter_user_id - 邀请人用户Id
	 */
	public Integer getInviterUserId() {
		return inviterUserId;
	}

	/**
	 * 设置邀请人用户Id
	 *
	 * @param inviterUserId
	 *            邀请人用户Id
	 */
	public void setInviterUserId(Integer inviterUserId) {
		this.inviterUserId = inviterUserId;
	}

	/**
	 * 获取邀请人加密用户Id
	 *
	 * @return inviter_encryption_user_id - 邀请人加密用户Id
	 */
	public String getInviterEncryptionUserId() {
		return inviterEncryptionUserId;
	}

	/**
	 * 设置邀请人加密用户Id
	 *
	 * @param inviterEncryptionUserId
	 *            邀请人加密用户Id
	 */
	public void setInviterEncryptionUserId(String inviterEncryptionUserId) {
		this.inviterEncryptionUserId = inviterEncryptionUserId;
	}

	/**
	 * 获取注册人Id
	 *
	 * @return register_user_id - 注册人Id
	 */
	public Integer getRegisterUserId() {
		return registerUserId;
	}

	/**
	 * 设置注册人Id
	 *
	 * @param registerUserId
	 *            注册人Id
	 */
	public void setRegisterUserId(Integer registerUserId) {
		this.registerUserId = registerUserId;
	}

	public Integer getConsumptionStatus() {
		return consumptionStatus;
	}

	public void setConsumptionStatus(Integer consumptionStatus) {
		this.consumptionStatus = consumptionStatus;
	}

}