package com.dl.task.service;
import com.dl.base.service.AbstractService;
import com.dl.task.dao.SysConfigMapper;
import com.dl.task.dto.SysConfigDTO;
import com.dl.task.model.SysConfig;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

@Service
@Transactional("transactionManager1")
public class SysConfigService extends AbstractService<SysConfig> {
    @Resource
    private SysConfigMapper sysConfigMapper;
    
    public SysConfigDTO querySysConfig(Integer businessId) {
    	SysConfig sysConfig = this.findBy("businessId", businessId);
    	if(null == sysConfig) {
    		return new SysConfigDTO();
    	}
    	SysConfigDTO sysConfigDTO = new SysConfigDTO();
    	BeanUtils.copyProperties(sysConfig, sysConfigDTO);
		return sysConfigDTO;
    }

}
