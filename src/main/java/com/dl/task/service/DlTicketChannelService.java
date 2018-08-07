package com.dl.task.service;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dl.base.service.AbstractService;
import com.dl.task.dao.DlTicketChannelMapper;
import com.dl.task.model.DlTicketChannel;

@Service
@Transactional
public class DlTicketChannelService extends AbstractService<DlTicketChannel> {
    @Resource
    private DlTicketChannelMapper dlTicketChannelMapper;

}
