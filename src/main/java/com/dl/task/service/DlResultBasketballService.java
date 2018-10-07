package com.dl.task.service;
import com.dl.base.service.AbstractService;
import com.dl.task.dao2.DlResultBasketballMapper;
import com.dl.task.model.DlResultBasketball;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional(value="transactionManager2")
public class DlResultBasketballService extends AbstractService<DlResultBasketball> {
    @Resource
    private DlResultBasketballMapper dlResultBasketballMapper;

}
