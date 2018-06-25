package com.dl.task.service;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dl.task.dao.UserMapper;

@Service
@Transactional
@Slf4j
public class UserService {

	@Resource
	private UserMapper userMapper;
}
