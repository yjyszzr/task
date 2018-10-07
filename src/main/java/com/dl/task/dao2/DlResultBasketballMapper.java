package com.dl.task.dao2;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.dl.base.mapper.Mapper;
import com.dl.task.model.DlResultBasketball;

public interface DlResultBasketballMapper extends Mapper<DlResultBasketball> {
	/**
	 * 根据changciid集合查询篮球球赛信息
	 * @param changciIds
	 * @return
	 */
	List<DlResultBasketball> queryMatchResultsByChangciIds(@Param("changciIds")List<Integer> changciIds);
	
}