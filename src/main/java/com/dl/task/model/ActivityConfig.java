package com.dl.task.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "dl_activity_config")
public class ActivityConfig {
	/**
     * id
     */
    @Id
    @Column(name = "id")
    private Integer id;
    /**
     * id
     */
    @Column(name = "act_id")
    private Integer act_id;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getAct_id() {
		return act_id;
	}
	public void setAct_id(Integer act_id) {
		this.act_id = act_id;
	}
   

}