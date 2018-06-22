package com.dl.task.param;

import java.util.List;

import lombok.Data;

/**
 * 消息添加参数
 *
 * @author 
 */
@Data
public class AddMessageParam {

	private List<MessageAddParam> params;
}
