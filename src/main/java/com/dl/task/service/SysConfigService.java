package com.dl.task.service;
import com.dl.base.service.AbstractService;
import com.dl.task.dao.SysConfigMapper;
import com.dl.task.dto.SysConfigDTO;
import com.dl.task.model.SysConfig;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional("transactionManager1")
@Slf4j
public class SysConfigService extends AbstractService<SysConfig> {
    @Resource
    private SysConfigMapper sysConfigMapper;
    
    public SysConfigDTO querySysConfig(Integer businessId) {
    	SysConfig sysConfig = sysConfigMapper.selectConfigByBusinessId(businessId);
    	log.info("businessId={},value={}",businessId,sysConfig==null?"":sysConfig.getValue());
    	if(null == sysConfig) {
    		return new SysConfigDTO();
    	}
    	SysConfigDTO sysConfigDTO = new SysConfigDTO();
    	BeanUtils.copyProperties(sysConfig, sysConfigDTO);
		return sysConfigDTO;
    }

    public void updateSysConfig(Integer value) {
    	sysConfigMapper.updateConfigByBusinessId(value);
    }
}
