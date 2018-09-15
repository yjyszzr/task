package com.dl.task.model;

import javax.persistence.*;

@Table(name = "dl_sys_split_table_task")
public class DlSysSplitTableTask {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 任务定义
     */
    @Column(name = "task_code")
    private String taskCode;

    /**
     * 任务描述
     */
    @Column(name = "task_desc")
    private String taskDesc;

    /**
     * 参数:根据实现，自定义参数 JSON keyValue
     */
    @Column(name = "task_params")
    private String taskParams;

    /**
     * 任务运行：running:运行中，stop:关停
     */
    @Column(name = "task_run_status")
    private String taskRunStatus;

    /**
     * 获取ID
     *
     * @return id - ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置ID
     *
     * @param id ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取任务定义
     *
     * @return task_code - 任务定义
     */
    public String getTaskCode() {
        return taskCode;
    }

    /**
     * 设置任务定义
     *
     * @param taskCode 任务定义
     */
    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    /**
     * 获取任务描述
     *
     * @return task_desc - 任务描述
     */
    public String getTaskDesc() {
        return taskDesc;
    }

    /**
     * 设置任务描述
     *
     * @param taskDesc 任务描述
     */
    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    /**
     * 获取参数:根据实现，自定义参数 JSON keyValue
     *
     * @return task_params - 参数:根据实现，自定义参数 JSON keyValue
     */
    public String getTaskParams() {
        return taskParams;
    }

    /**
     * 设置参数:根据实现，自定义参数 JSON keyValue
     *
     * @param taskParams 参数:根据实现，自定义参数 JSON keyValue
     */
    public void setTaskParams(String taskParams) {
        this.taskParams = taskParams;
    }

    /**
     * 获取任务运行：running:运行中，stop:关停
     *
     * @return task_run_status - 任务运行：running:运行中，stop:关停
     */
    public String getTaskRunStatus() {
        return taskRunStatus;
    }

    /**
     * 设置任务运行：running:运行中，stop:关停
     *
     * @param taskRunStatus 任务运行：running:运行中，stop:关停
     */
    public void setTaskRunStatus(String taskRunStatus) {
        this.taskRunStatus = taskRunStatus;
    }
}