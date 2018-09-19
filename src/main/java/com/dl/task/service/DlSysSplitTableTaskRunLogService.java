//package com.dl.task.service;
//import javax.annotation.Resource;
//
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.dl.base.service.AbstractService;
//import com.dl.base.util.DateUtil;
//import com.dl.task.dao.DlSysSplitTableTaskRunLogMapper;
//import com.dl.task.model.DlSysSplitTableTaskRunLog;
//
//@Service
//@Transactional
//public class DlSysSplitTableTaskRunLogService extends AbstractService<DlSysSplitTableTaskRunLog> {
//    @Resource
//    private DlSysSplitTableTaskRunLogMapper dlSysSplitTableTaskRunLogMapper;
//
//    /**
//     * 插入task运行日志
//     * @param runnningTaskCode
//     * @return
//     */
//	public Integer createSplitTableTaskRunLog(String runnningTaskCode) {
//		DlSysSplitTableTaskRunLog log = new DlSysSplitTableTaskRunLog();
//		log.setTaskCode(runnningTaskCode);
//		log.setStartTime(DateUtil.getCurrentTimeLong());
//		log.setRunParams(runParams);
//		return null;
//	}
//
//}
