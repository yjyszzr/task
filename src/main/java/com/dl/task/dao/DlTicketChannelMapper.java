package com.dl.task.dao;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.task.model.DlTicketChannel;

public interface DlTicketChannelMapper extends Mapper<DlTicketChannel> {

	DlTicketChannel selectChannelByChannelId(@Param("printChannelId") Integer printChannelId);
}