package com.dl.task.service;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dl.base.service.AbstractService;
import com.dl.task.dao.DlTicketChannelLotteryClassifyMapper;
import com.dl.task.model.DlTicketChannelLotteryClassify;

@Service
@Transactional
public class DlTicketChannelLotteryClassifyService extends AbstractService<DlTicketChannelLotteryClassify> {
    @Resource
    private DlTicketChannelLotteryClassifyMapper dlTicketChannelLotteryClassifyMapper;

}
