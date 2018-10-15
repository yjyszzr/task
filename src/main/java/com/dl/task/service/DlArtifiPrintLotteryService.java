package com.dl.task.service;
import com.dl.task.model.DlArtifiPrintLottery;
import com.dl.task.dao.DlArtifiPrintLotteryMapper;
import com.dl.base.service.AbstractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class DlArtifiPrintLotteryService extends AbstractService<DlArtifiPrintLottery> {
    @Resource
    private DlArtifiPrintLotteryMapper dlArtifiPrintLotteryMapper;

}
