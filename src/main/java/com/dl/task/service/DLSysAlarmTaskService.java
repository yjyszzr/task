package com.dl.task.service;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.dl.base.util.DateUtil;
import com.dl.base.util.JSONHelper;
import com.dl.task.dao.DLSysAlarmTaskMapper;
import com.dl.task.dto.XianfengQueryBalanceDto;
import com.dl.task.enums.AlarmTaskEnum;
import com.dl.task.model.DLSysAlarmTask;
import com.dl.task.printlottery.PrintComEnums;
import com.dl.task.printlottery.PrintLotteryAdapter;
import com.dl.task.printlottery.responseDto.QueryPrintBalanceDTO;
import com.dl.task.util.DingDingUtil;
import com.dl.task.util.RestTemplateFactory;
import com.dl.task.util.SmsUtil;

@Service
@Transactional
@Slf4j
public class DLSysAlarmTaskService {
	
	@Resource
	private PrintLotteryAdapter printLotteryAdapter; 
	@Resource
	private XianFengPayService xianFengPayService;
    @Resource
    private DLSysAlarmTaskMapper dLSysAlarmTaskMapper;

	public List<DLSysAlarmTask> selectOpenAlarmTask() {
		return dLSysAlarmTaskMapper.selectSysAlarmOpenTask();
	}

	public void alarmTask(DLSysAlarmTask alarmTask) {
		AlarmTaskEnum alarmEnum = AlarmTaskEnum.getAlarmTaskEnum(alarmTask.getAlarmCode());
		if(alarmEnum==null){
			log.error("未找到对应的报警任务设置,alarmCode={}",alarmTask.getAlarmCode());
			return;
		}
		if(!alarmEnum.getIsOk()){
			log.error("alarmCode={},is not ok (暂未实现完成)",alarmTask.getAlarmCode());
			return;
		}
		switch(alarmEnum){
			case ALARM_TX_BALLANCE_XIANFENG: alarmXfPayBalance(alarmTask);break;
			case ALARM_LOTTERY_BALLANCE_HENAN:alarmLotteryBalance(PrintComEnums.HENAN,alarmTask) ;break;
			case ALARM_LOTTERY_BALLANCE_XIAN: alarmLotteryBalance(PrintComEnums.XIAN,alarmTask) ;break;
			case ALARM_LOTTERY_BALLANCE_CAIXIAOMI:alarmLotteryBalance(PrintComEnums.CAIXIAOMI,alarmTask) ;break;
			case ALARM_LOTTERY_BALLANCE_WEICAISHIDAI:alarmLotteryBalance(PrintComEnums.WEICAISHIDAI,alarmTask) ;break;
			default : log.error("未找到对应的报警设置项 alarmCode={}",alarmEnum.getAlarmCode());;
		}
	}

	/**
	 * 获取出票公司余额
	 * @param printLotteryCom
	 * @param alarmTask
	 */
	private void alarmLotteryBalance(PrintComEnums printComEnums,
			DLSysAlarmTask alarmTask) {
//		获取余额
		QueryPrintBalanceDTO printBalanceDto = printLotteryAdapter.getBalance(printComEnums);
//		检测是否超限制
		Integer balance = printBalanceDto.getBalance();
//		检测是否超限制
		if(balance.compareTo(alarmTask.getAlarmLimit())>0){
//			未超过限制不报警
			return;
		}
		Map<String,String> params = new HashMap<String, String>();
		params.put("balance", balance+"");
		sendMsg(alarmTask,params);
	}

	/**
	 * 获取先锋支付余额报警
	 * @param alarmTask
	 */
	private void alarmXfPayBalance(DLSysAlarmTask alarmTask) {
//		获取余额
		XianfengQueryBalanceDto queryBalance = xianFengPayService.queryBalance();
		if(!"00000".equals(queryBalance.getResCode())){
			log.error("查询先锋支付余额报错返回信息={}",JSONHelper.bean2json(queryBalance));
			return;
		}
		Integer balance = Integer.parseInt(queryBalance.getBalance());
//		检测是否超限制
		if(balance.compareTo(alarmTask.getAlarmLimit())>0){
//			未超过限制不报警
			return;
		}
		Map<String,String> params = new HashMap<String, String>();
		params.put("balance", queryBalance.getBalance());
		sendMsg(alarmTask,params);
	}

	
	private void sendMsg(DLSysAlarmTask alarmTask, Map<String, String> params) {
		String sendMethod = alarmTask.getSendMethod();
		if(isSend(sendMethod,"SMS")){
			Boolean send = sendAlarmSms(alarmTask, params);
			if(send){
				DLSysAlarmTask update = new DLSysAlarmTask();
				update.setAlarmCode(alarmTask.getAlarmCode());
				if(alarmTask.getSmsAlarmCount()==null||alarmTask.getSmsAlarmCount().equals(Integer.valueOf(0))){
					update.setSmsFirstAlarmTime(DateUtil.getCurrentTimeLong());
				}
				dLSysAlarmTaskMapper.updateSmsCountPlusOne(update);
			}
		}
		if(isSend(sendMethod,"DINGDING")){
			Boolean send = sendAlarmDingDing(alarmTask, params);
			if(send){
				DLSysAlarmTask update = new DLSysAlarmTask();
				update.setAlarmCode(alarmTask.getAlarmCode());
				Integer dingdingSendAcount = alarmTask.getDingdingAlarmCount();
				if(dingdingSendAcount==null||dingdingSendAcount.equals(Integer.valueOf(0))){
					update.setDingdingFirstAlarmTime(DateUtil.getCurrentTimeLong());
				}
				dLSysAlarmTaskMapper.updateDingDingCountPlusOne(update);
			}
		}
	}

	private Boolean sendAlarmDingDing(DLSysAlarmTask alarmTask,
			Map<String, String> params) {
		Boolean send=Boolean.FALSE;
		String dingDingContent = alarmTask.getDingdingSendContent();
		String dingDingUrl=alarmTask.getDingdingUrl();
		if(StringUtils.isEmpty(dingDingUrl)){
			log.info("短信报警，未设置报警钉钉地址,alarmCode={}",alarmTask.getAlarmCode());
			return send;
		}
		if(StringUtils.isEmpty(dingDingContent)){
			log.info("短信报警，未设置报警钉钉内容,alarmCode={}",alarmTask.getAlarmCode());
			return send;
		}
		for(String key:params.keySet()){
			dingDingContent = dingDingContent.replaceAll("{"+key+"}", params.get(key));
		}
		Integer firstSendTime = alarmTask.getDingdingFirstAlarmTime();
		Integer account = alarmTask.getDingdingAlarmCount();
		if(Integer.valueOf(0).equals(account)){//首次发送
			send=Boolean.TRUE;
		}else{//非首次发送
			String timesStr = alarmTask.getSmsAlarmTime();
			if(StringUtils.isEmpty(timesStr)){
				log.info("未配置时间间隔，只发送一次");
				return send;
			}
			String[] timesArr = timesStr.split(";");
			if(account<=timesArr.length){
				log.info("已达发送上线，不再发送");
				return send;
			}
			Integer sumTime = Integer.valueOf(0);
			sumTime = sumTime+firstSendTime;
			Integer currentTime = DateUtil.getCurrentTimeLong();
			for(Integer i=0;i<account;i++){
				Integer timeTemp = Integer.parseInt(timesArr[i]);
				sumTime = timeTemp+timeTemp;
			}
			if(currentTime.compareTo(sumTime)<0){
				log.info("未达到时间发送间隔，不发送");
				return send;
			}else{
				send=Boolean.TRUE;
			}
		}
		if(send){
			String mobileStr = alarmTask.getDingdingMobile();
			String[] mobiles = null;
			if(StringUtils.isEmpty(mobileStr)){
				mobiles = mobileStr.split(";");	
			}
			DingDingUtil.sendDingDingMsg(dingDingUrl, dingDingContent, mobiles);
		}
		return send;
	}

	private Boolean sendAlarmSms(DLSysAlarmTask alarmTask,
			Map<String, String> params) {
		Boolean send=Boolean.FALSE;
		String tplId="";
		String tplValue="";
		String smsContent = alarmTask.getSmsSendContent();
		if(StringUtils.isEmpty(smsContent)){
			log.info("短信报警，未设置报警短信内容,alarmCode={}",alarmTask.getAlarmCode());
			return send;
		}
		for(String key:params.keySet()){
			smsContent = smsContent.replaceAll("{"+key+"}", params.get(key));
		}
		String mobileStr = alarmTask.getSmsSendMobile();
		if(StringUtils.isEmpty(mobileStr)){
			log.info("短信报警，未设置报警手机号,alarmCode={}",alarmTask.getAlarmCode());
			return send;
		}
		String[] mobiles = mobileStr.split(";");
		Integer firstSendTime = alarmTask.getSmsFirstAlarmTime();
		Integer account = alarmTask.getSmsAlarmCount();
		if(Integer.valueOf(0).equals(account)){//首次发送
			send=Boolean.TRUE;
			firstSendTime = DateUtil.getCurrentTimeLong();
		}else{//非首次发送
			String timesStr = alarmTask.getSmsAlarmTime();
			if(StringUtils.isEmpty(timesStr)){
				log.info("未配置时间间隔，只发送一次");
				return send;
			}
			String[] timesArr = timesStr.split(";");
			if(account<=timesArr.length){
				log.info("已达发送上线，不再发送");
				return send;
			}
			Integer sumTime = Integer.valueOf(0);
			sumTime = sumTime+firstSendTime;
			Integer currentTime = DateUtil.getCurrentTimeLong();
			for(Integer i=0;i<account;i++){
				Integer timeTemp = Integer.parseInt(timesArr[i]);
				sumTime = timeTemp+timeTemp;
			}
			if(currentTime.compareTo(sumTime)<0){
				log.info("未达到时间发送间隔，不发送");
				return send;
			}else{
				send=Boolean.TRUE;
			}
		}
		if(send){
			for(String mobile:mobiles){
				SmsUtil.send(mobile,tplId,tplValue);
			}
		}
		return send;
	}
	
	private Boolean isSend(String sendMethod,String method) {
		Boolean isSend = sendMethod.contains(method.toLowerCase())||sendMethod.contains(method.toUpperCase())||sendMethod.contains("ALL");
		return isSend;
	}
}