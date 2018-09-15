package com.dl.task.model;

import javax.persistence.*;

@Table(name = "dl_sys_split_table_task_run_log")
public class DlSysSplitTableTaskRunLog {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 任务标识
     */
    @Column(name = "task_code")
    private String taskCode;

    /**
     * 任务开始时间
     */
    @Column(name = "start_time")
    private Integer startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private Integer endTime;

    /**
     * 任务运行：success:成功，fail:失败
     */
    @Column(name = "run_result")
    private String runResult;

    /**
     * 任务运行详细描述
     */
    @Column(name = "run_result_desc")
    private String runResultDesc;

    /**
     * 运行失败时步骤停留位置
     */
    @Column(name = "run_fail_step")
    private String runFailStep;

    /**
     * 任务运行时参数
     */
    @Column(name = "run_params")
    private String runParams;

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
     * 获取任务标识
     *
     * @return task_code - 任务标识
     */
    public String getTaskCode() {
        return taskCode;
    }

    /**
     * 设置任务标识
     *
     * @param taskCode 任务标识
     */
    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    /**
     * 获取任务开始时间
     *
     * @return start_time - 任务开始时间
     */
    public Integer getStartTime() {
        return startTime;
    }

    /**
     * 设置任务开始时间
     *
     * @param startTime 任务开始时间
     */
    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    /**
     * 获取结束时间
     *
     * @return end_time - 结束时间
     */
    public Integer getEndTime() {
        return endTime;
    }

    /**
     * 设置结束时间
     *
     * @param endTime 结束时间
     */
    public void setEndTime(Integer endTime) {
        this.endTime = endTime;
    }

    /**
     * 获取任务运行：success:成功，fail:失败
     *
     * @return run_result - 任务运行：success:成功，fail:失败
     */
    public String getRunResult() {
        return runResult;
    }

    /**
     * 设置任务运行：success:成功，fail:失败
     *
     * @param runResult 任务运行：success:成功，fail:失败
     */
    public void setRunResult(String runResult) {
        this.runResult = runResult;
    }

    /**
     * 获取任务运行详细描述
     *
     * @return run_result_desc - 任务运行详细描述
     */
    public String getRunResultDesc() {
        return runResultDesc;
    }

    /**
     * 设置任务运行详细描述
     *
     * @param runResultDesc 任务运行详细描述
     */
    public void setRunResultDesc(String runResultDesc) {
        this.runResultDesc = runResultDesc;
    }

    /**
     * 获取运行失败时步骤停留位置
     *
     * @return run_fail_step - 运行失败时步骤停留位置
     */
    public String getRunFailStep() {
        return runFailStep;
    }

    /**
     * 设置运行失败时步骤停留位置
     *
     * @param runFailStep 运行失败时步骤停留位置
     */
    public void setRunFailStep(String runFailStep) {
        this.runFailStep = runFailStep;
    }

    /**
     * 获取任务运行时参数
     *
     * @return run_params - 任务运行时参数
     */
    public String getRunParams() {
        return runParams;
    }

    /**
     * 设置任务运行时参数
     *
     * @param runParams 任务运行时参数
     */
    public void setRunParams(String runParams) {
        this.runParams = runParams;
    }
}