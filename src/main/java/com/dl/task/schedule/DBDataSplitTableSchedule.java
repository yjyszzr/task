//package com.dl.task.schedule;
//
//import java.time.format.DateTimeFormatter;
//import java.util.Date;
//
//import javax.annotation.Resource;
//
//import lombok.extern.slf4j.Slf4j;
//
//import org.apache.commons.lang.StringUtils;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.dl.base.util.DateUtil;
//import com.dl.base.util.JSONHelper;
//import com.dl.task.model.DlSysSplitTableTask;
//import com.dl.task.service.DlSysSplitTableTaskRunLogService;
//import com.dl.task.service.DlSysSplitTableTaskService;
//import com.dl.task.splittable.SplitTableCommonDto;
//
//@Slf4j
//@Configuration
//@EnableScheduling
//public class DBDataSplitTableSchedule {
//	@Resource
//	private DlSysSplitTableTaskService dlSysSplitTableTaskService;
//	@Resource
//	private DlSysSplitTableTaskRunLogService dlSysSplitTableTaskRunLogService;
//	/**
//	 * 数据库数据分表任务orders相关
//	 */
//	@Scheduled(cron = "${task.schedule.db.split.orders}")
//	public void splitDbTableOrders() {
//		log.info("数据库分表任务order开始");
//		DlSysSplitTableTask splitTask = dlSysSplitTableTaskService.queryRunningTaskByTaskCode(DlSysSplitTableTaskService.runnningTaskCodeOrder);
//		String taskParams = splitTask.getTaskParams();
//		SplitTableCommonDto dto = null;
//		if(StringUtils.isEmpty(taskParams)){			
//			dto = (SplitTableCommonDto)JSONHelper.getBeanList(splitTask.getTaskParams(), SplitTableCommonDto.class);
//			dlSysSplitTableTaskService.setSplitTableCommonParams(dto);
//		}
//		Integer logId = dlSysSplitTableTaskRunLogService.createSplitTableTaskRunLog(DlSysSplitTableTaskService.runnningTaskCodeOrder,);
//		if(dlSysSplitTableTaskService.isNotRunning(splitTask)){
//			log.info("数据库分表任务task_code={},结束,未开启或者未配置运行开关",DlSysSplitTableTaskService.runnningTaskCodeOrder);
//			return;
//		}
//		Boolean isTableIsOk = dlSysSplitTableTaskService.checkAndCreateTableByTaskCode(DlSysSplitTableTaskService.runnningTaskCodeOrder);
//		if(!isTableIsOk){
//			log.info("数据库分表任务task_code={},结束,建表失败",DlSysSplitTableTaskService.runnningTaskCodeOrder);
//			return;
//		}
//		log.info("数据库分表任务表dl_order迁移开始");
//		log.info("数据库分表任务表dl_order_detail迁移开始");
//		log.info("数据库分表任务表dl_print_lottery迁移开始");
//		log.info("数据库分表任务表dl_order_log迁移开始");
//		
//		log.info("数据库分表任务迁移完成开始删除历史数据");
//		
//		log.info("数据库分表任务表dl_order删除数据");
//		log.info("数据库分表任务表dl_order_detail删除数据");
//		log.info("数据库分表任务表dl_print_lottery删除数据");
//		log.info("数据库分表任务表dl_order_log删除数据");
//		log.info("数据库分表任务order结束");
//	}
//}
