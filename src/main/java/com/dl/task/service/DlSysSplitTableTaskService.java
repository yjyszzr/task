package com.dl.task.service;
import java.util.Date;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.task.dao.DlSysSplitTableTaskMapper;
import com.dl.task.model.DlSysSplitTableTask;
import com.dl.task.splittable.SplitTableCommonDto;

@Service
@Transactional
@Slf4j
public class DlSysSplitTableTaskService extends AbstractService<DlSysSplitTableTask> {
	public static String runnningTaskCodeOrder="split_order";
	
	public static String taskRunningStatusRunning="running";
    @Resource
    private DlSysSplitTableTaskMapper dlSysSplitTableTaskMapper;
    
    /**
     * 获取拆分任务的指定任务
     * @param taskCode
     * @return
     */
	public DlSysSplitTableTask queryRunningTaskByTaskCode(String taskCode) {
		return dlSysSplitTableTaskMapper.selectSplitTaskByTaskCode(taskCode);
	}

	/**
	 * 
	 * @param splitTask
	 * @return
	 */
	public boolean isNotRunning(DlSysSplitTableTask splitTask) {
		if(splitTask!=null&&!StringUtils.isEmpty(splitTask.getTaskRunStatus())&&taskRunningStatusRunning.equalsIgnoreCase(splitTask.getTaskRunStatus())){
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	public void setSplitTableCommonParams(SplitTableCommonDto dto) {
		if(dto!=null){
			String splitUnit = dto.getSplitTableTimeUnit();
			Integer limit = dto.getSplitTableTimeLimit();
			if("month".equalsIgnoreCase(splitUnit)){
				Date now = new Date();
				
			}
		}
	}

}
