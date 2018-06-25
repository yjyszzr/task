package com.dl.task.service;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.dl.base.constant.CommonConstants;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.task.dao.DlMessageMapper;
import com.dl.task.dao.UserMapper;
import com.dl.task.model.DlMessage;
import com.dl.task.param.AddMessageParam;
import com.dl.task.param.MessageAddParam;
import com.dl.task.util.GeTuiMessage;
import com.dl.task.util.GeTuiUtil;

@Service
@Transactional
public class DlMessageService extends AbstractService<DlMessage> {
    @Resource
    private DlMessageMapper dlMessageMapper;
    @Resource
    private GeTuiUtil geTuiUtil;
    @Resource
    private UserMapper userMapper;
    public BaseResult<String> add(@RequestBody AddMessageParam addParam) {
    	List<MessageAddParam> params = addParam.getParams();
    	List<Integer> lotteryFailUserIds = new ArrayList<Integer>(params.size());
    	for(MessageAddParam param: params) {
    		DlMessage dlMessage = new DlMessage();
    		dlMessage.setContent(param.getContent());
    		dlMessage.setContentDesc(param.getContentDesc());
    		dlMessage.setMsgDesc(param.getMsgDesc());
    		dlMessage.setMsgType(param.getMsgType());
    		dlMessage.setReceiver(param.getReceiver());
    		dlMessage.setReceiverMobile(param.getReceiveMobile());
    		dlMessage.setObjectType(param.getObjectType());
    		dlMessage.setSendTime(param.getSendTime());
    		dlMessage.setSender(param.getSender());
    		dlMessage.setTitle(param.getTitle());
    		dlMessage.setMsgUrl(param.getMsgUrl());
    		dlMessage.setContentUrl(param.getContentUrl());
    		this.save(dlMessage);
    		if(3 == param.getObjectType()) {
    			lotteryFailUserIds.add(param.getReceiver());
    		}
    	}
    	//出票失败提示
    	if(CollectionUtils.isNotEmpty(lotteryFailUserIds)) {
    		List<String> clientIds = userMapper.getClientIds(lotteryFailUserIds);
    		for(String clientId : clientIds) {
    			GeTuiMessage getuiMessage = new GeTuiMessage(CommonConstants.FORMAT_PRINTLOTTERY_PUSH_TITLE, CommonConstants.FORMAT_PRINTLOTTERY_PUSH_DESC, DateUtil.getCurrentTimeLong());
    			geTuiUtil.pushMessage(clientId, getuiMessage);
    		}
    	}
        return ResultGenerator.genSuccessResult();
    }
    
}
