package com.dl.task.service;

import io.jsonwebtoken.lang.Collections;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tk.mybatis.mapper.entity.Condition;
import tk.mybatis.mapper.entity.Example.Criteria;

import com.alibaba.fastjson.JSON;
import com.dl.base.constant.CommonConstants;
import com.dl.base.enums.MatchBetTypeEnum;
import com.dl.base.enums.MatchPlayTypeEnum;
import com.dl.base.enums.MatchResultCrsEnum;
import com.dl.base.enums.MatchResultHadEnum;
import com.dl.base.enums.MatchResultHafuEnum;
import com.dl.base.enums.RespStatusEnum;
import com.dl.base.enums.SNBusinessCodeEnum;
import com.dl.base.exception.ServiceException;
import com.dl.base.model.UserDeviceInfo;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.base.util.DateUtilNew;
import com.dl.base.util.IpUtil;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.SNGenerator;
import com.dl.base.util.SessionUtil;

import com.dl.shop.payment.api.IpaymentService;
import com.dl.shop.payment.param.RollbackOrderAmountParam;
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.OrderDetailMapper;
import com.dl.task.dao.OrderMapper;
import com.dl.task.dto.MatchResultDTO;
import com.dl.task.dto.OrderDTO;
import com.dl.task.dto.OrderWithUserDTO;
import com.dl.task.model.ChannelOperationLog;
import com.dl.task.model.DlChannelConsumer;
import com.dl.task.model.DlChannelDistributor;
import com.dl.task.model.DlLeagueMatchResult;
import com.dl.task.model.Order;
import com.dl.task.model.OrderDetail;
import com.dl.task.model.User;
import com.dl.task.param.AddMessageParam;
import com.dl.task.param.GetCancelMatchesParam;
import com.dl.task.param.LotteryPrintParam;
import com.dl.task.param.MessageAddParam;
import com.dl.task.param.QueryMatchResultsByPlayCodesParam;
import com.dl.task.param.UpdateOrderInfoParam;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

@Slf4j
@Service
public class OrderService extends AbstractService<Order> {

	@Resource
	private OrderMapper orderMapper;

	@Resource
	private OrderDetailMapper orderDetailMapper;
	
	@Resource
	private	DlLeagueMatchResultService dleagueMatchResultService;
	
	@Resource
	private LotteryMatchService lotteryMatchService;	
	
	@Resource
	private LotteryPrintService lotteryPrintService;

//	@Resource
//	private OrderLogMapper orderLogMapper;
//
//	@Resource
//	private OrderLogContentService orderLogContentService;
//
//	@Resource
//	private OrderRollbackErrorLogService orderRollbackErrorLogService;
//
//	@Resource
//	private IUserBonusService userBonusService;
//
//	@Resource
//	private IUserAccountService userAccountService;
//


//
//	@Resource
//	private ILotteryRewardService lotteryRewardService;
	
	@Resource
	private IpaymentService paymentService;

	@Value("${spring.datasource.druid.url}")
	private String dbUrl;

	@Value("${spring.datasource.druid.username}")
	private String dbUserName;

	@Value("${spring.datasource.druid.password}")
	private String dbPass;

	@Value("${spring.datasource.druid.driver-class-name}")
	private String dbDriver;

//	@Resource
//	private IMatchResultService matchResultService;
//
//	@Resource
//	private IUserMessageService userMessageService;

	/**
	 * 出票成功，修改订单数据
	 * 
	 * @param param
	 */
	public void updateOrderInfoByPrint(List<LotteryPrintParam> lotteryPrintParams) {
		if (CollectionUtils.isNotEmpty(lotteryPrintParams)) {
//			log.info("--------------------- " + JSONHelper.bean2json(lotteryPrintParams));
			List<Order> orders = new LinkedList<Order>();
			List<String> orderSnList = new ArrayList<String>(lotteryPrintParams.size());
			List<Order> orderList = new ArrayList<Order>(lotteryPrintParams.size());
			List<OrderDetail> orderDetails = new LinkedList<OrderDetail>();
			for (LotteryPrintParam param : lotteryPrintParams) {
				Order orderInfoByOrderSn = orderMapper.getOrderInfoByOrderSn(param.getOrderSn());
				if (orderInfoByOrderSn == null) {
					continue;
				}
				// 更新订单表
				Order order = new Order();
				order.setOrderSn(param.getOrderSn());
				order.setAcceptTime(param.getAcceptTime());
				order.setTicketTime(param.getTicketTime());
				order.setOrderStatus(ProjectConstant.ORDER_STATUS_STAY);
				orders.add(order);
				if(!orderSnList.contains(param.getOrderSn())) {
					orderSnList.add(param.getOrderSn());
					orderList.add(orderInfoByOrderSn);
				}
				// 更新订单详情表
				if (StringUtils.isNotEmpty(param.getPrintSp())) {
					OrderDetail orderDetail = new OrderDetail();
					orderDetail.setOrderSn(param.getOrderSn());
					List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
					if (CollectionUtils.isNotEmpty(orderDetailList)) {
						String printSp = param.getPrintSp();
						if (StringUtils.isEmpty(printSp)) {
							log.error("出票成功，修改订单数据，没有返回的赔率参数，订单编号：" + param.getOrderSn());
							continue;
						}
						Integer lotteryClassifyId = orderInfoByOrderSn.getLotteryClassifyId();
						Integer lotteryPlayClassifyId = orderInfoByOrderSn.getLotteryPlayClassifyId();
						if (1 == lotteryClassifyId) {
							if (8 == lotteryPlayClassifyId) {
								List<String> spList = Arrays.asList(printSp.split(","));
								Map<String, String> map = new HashMap<String, String>(spList.size());
								for (String str : spList) {
									String[] split2 = str.split("@");
									map.put(split2[0], split2[1]);
								}
								log.info("0000000000 " + JSONHelper.bean2json(map));
								for (OrderDetail od : orderDetailList) {
									String ticketData = od.getTicketData();
									String[] split = ticketData.split("@");
									String odds = map.get(split[0]);
									if (StringUtils.isNotBlank(odds)) {
										od.setTicketData(split[0] + "@" + odds);
										orderDetails.add(od);
									}
								}
								log.info("0000000000 " + JSONHelper.bean2json(orderDetails));
								// 计算预测奖金
								List<Double> map2 = orderDetailList.stream().map(item -> Double.parseDouble(item.getTicketData().split("@")[1])).collect(Collectors.toList());
								Double maxOdds = map2.stream().max((item1, item2) -> item1.compareTo(item2)).get();
								Double minOdds = map2.stream().min((item1, item2) -> item1.compareTo(item2)).get();
								Integer times = orderInfoByOrderSn.getCathectic();
								Double max = maxOdds * times * 2;
								Double min = minOdds * times * 2;
								String forecastMoney = String.format("%.2f", min) + "~" + String.format("%.2f", max);
								order.setForecastMoney(forecastMoney);
							} else {
								List<TicketInfo> ticketInfos = new ArrayList<TicketInfo>(orderDetails.size());
								List<String> spList = Arrays.asList(printSp.split(";"));
								for (OrderDetail od : orderDetailList) {
									List<String> ticketData = Arrays.asList(od.getTicketData().split(";"));
									String nTicketData = this.updateTicketDataWithSp(spList, ticketData);
									od.setTicketData(nTicketData);
									orderDetails.add(od);
									TicketInfo ticketInfo = this.ticketPlayInfos(Arrays.asList(nTicketData.split(";")), od.getFixedodds());
									ticketInfos.add(ticketInfo);
								}
								// 计算预测奖金
								String forecastMoney = this.betMaxAndMinMoney(ticketInfos, orderInfoByOrderSn);
								order.setForecastMoney(forecastMoney);
							}
						}
					}
				}
			}
			if (CollectionUtils.isNotEmpty(orders)) {
//				log.info("--------------------- " + JSONHelper.bean2json(orders));
				for (Order order : orders) {
					orderMapper.updateOrderTicketInfo(order);
				}
			}
			if (CollectionUtils.isNotEmpty(orderDetails)) {
				for (OrderDetail orderDetail : orderDetails) {
					orderDetailMapper.updateTicketData(orderDetail);
				}
			}
			if(CollectionUtils.isNotEmpty(orderList)) {
				this.updateChannelOperationLog(orderList);
			}
		}
	}

	/**
	 * 西安活动专用代码
	 * 
	 * @param consumers
	 * @param userList
	 * @param distributors
	 * @param order
	 */
	private void updateChannelOperationLog(List<Order> orderList) {
//		log.info("--------------------- " + JSONHelper.bean2json(orderList));
		//订单用户列表
		List<Integer> userIds = orderList.stream().map(item->item.getUserId()).collect(Collectors.toList());
		//过滤店员下的用户列表
		List<DlChannelConsumer> consumers = orderMapper.selectConsumers(userIds);
		if(CollectionUtils.isNotEmpty(consumers)) {
			Map<Integer, DlChannelConsumer> consumerMap = new HashMap<Integer, DlChannelConsumer>(consumers.size());
			consumers.forEach(item->consumerMap.put(item.getUserId(), item));
			List<Integer> dUserIds = consumers.stream().map(item->item.getUserId()).collect(Collectors.toList());
			List<User> userList = orderMapper.findAllUser(dUserIds);
			Map<Integer, User> userMap = new HashMap<Integer, User>(userList.size());
			userList.forEach(item->userMap.put(item.getUserId(), item));
			List<Integer> channelDistributorIds = consumers.stream().map(item->item.getChannelDistributorId()).collect(Collectors.toList());
			//店员列表
			List<DlChannelDistributor> distributors = orderMapper.channelDistributorList(channelDistributorIds);
			Map<Integer, DlChannelDistributor> distributorMap = new HashMap<Integer, DlChannelDistributor>(distributors.size());
			distributors.forEach(item->distributorMap.put(item.getChannelDistributorId(), item));
			for(Order orderFor: orderList) {
				if(dUserIds.contains(orderFor.getUserId())) {
					User user = userMap.get(orderFor.getUserId());
					ChannelOperationLog channelOperationLog = new ChannelOperationLog();
					channelOperationLog.setOptionId(0);
					channelOperationLog.setUserName(user.getUserName());
					channelOperationLog.setMobile(user.getMobile());
					channelOperationLog.setUserId(user.getUserId());
					DlChannelConsumer dlChannelConsumer = consumerMap.get(orderFor.getUserId());
					DlChannelDistributor distributor = distributorMap.get(dlChannelConsumer.getChannelDistributorId());
					channelOperationLog.setDistributorId(distributor.getChannelDistributorId());
					channelOperationLog.setOperationNode(2);
					channelOperationLog.setStatus(1);
					channelOperationLog.setSource(orderFor.getOrderFrom());
					channelOperationLog.setOptionAmount(orderFor.getMoneyPaid());
					channelOperationLog.setOptionTime(DateUtilNew.getCurrentTimeLong());
					channelOperationLog.setChannelId(distributor.getChannelId());
					orderMapper.saveChannelOperation(channelOperationLog);
				}
			}
		}
	}

	/**
	 * 计算预测试奖金
	 */
	private String betMaxAndMinMoney(List<TicketInfo> ticketInfos, Order orderInfoByOrderSn) {
		Integer times = orderInfoByOrderSn.getCathectic();
		String betTypes = orderInfoByOrderSn.getPassType();
		Map<String, List<String>> indexMap = this.getBetIndexList(ticketInfos, betTypes);
		TMatchBetMaxAndMinOddsList maxMoneyBetPlayCellsForLottery = this.maxMoneyBetPlayCellsForLottery(ticketInfos);
		List<Double> maxOddsList = maxMoneyBetPlayCellsForLottery.getMaxOddsList();
		List<Double> minOddsList = maxMoneyBetPlayCellsForLottery.getMinOddsList();
		Double totalMaxMoney = 0.0;
		Double totalMinMoney = Double.MAX_VALUE;
		for (String betType : indexMap.keySet()) {
			List<String> betIndexList = indexMap.get(betType);
			for (String str : betIndexList) {// 所有注组合
				String[] strArr = str.split(",");
				Double maxMoney = 2.0 * times;
				Double minMoney = 2.0 * times;
				for (String item : strArr) {// 单注组合
					Double double1 = maxOddsList.get(Integer.valueOf(item));
					maxMoney = maxMoney * double1;
					Double double2 = minOddsList.get(Integer.valueOf(item));
					minMoney = minMoney * double2;
				}
				totalMaxMoney += maxMoney;
				totalMinMoney = Double.min(totalMinMoney, minMoney);
			}
		}
		String forecastMoney = String.format("%.2f", totalMinMoney) + "~" + String.format("%.2f", totalMaxMoney);
		return forecastMoney;
	}

	private TMatchBetMaxAndMinOddsList maxMoneyBetPlayCellsForLottery(List<TicketInfo> ticketInfos) {
		TMatchBetMaxAndMinOddsList tem = new TMatchBetMaxAndMinOddsList();
		List<Double> maxOdds = new ArrayList<Double>(ticketInfos.size());
		List<Double> minOdds = new ArrayList<Double>(ticketInfos.size());
		for (TicketInfo ticketInfo : ticketInfos) {
			List<TicketPlayInfo> ticketPlayInfos = ticketInfo.getTicketPlayInfos();
			List<Double> allbetComOdds = this.allbetComOdds(ticketPlayInfos);
			if (CollectionUtils.isEmpty(allbetComOdds)) {
				continue;
			}
			if (allbetComOdds.size() == 1) {
				Double maxOrMinOdds = allbetComOdds.get(0);
				maxOdds.add(maxOrMinOdds);
				minOdds.add(maxOrMinOdds);
			} else {
				Double max = allbetComOdds.stream().max((item1, item2) -> item1.compareTo(item2)).get();
				maxOdds.add(max);
				Double min = allbetComOdds.stream().min((item1, item2) -> item1.compareTo(item2)).get();
				minOdds.add(min);
			}
		}
		tem.setMaxOddsList(maxOdds);
		tem.setMinOddsList(minOdds);
		return tem;
	}

	/**
	 * 计算混合玩法的排斥后的该场次的几种可能赔率
	 * 
	 * @param list
	 *            混合玩法 同一场次的所有玩法选项
	 */
	private List<Double> allbetComOdds(List<TicketPlayInfo> list) {
		// 比分
		Optional<TicketPlayInfo> optionalcrs = list.stream().filter(dto -> dto.getPlayType() == (MatchPlayTypeEnum.PLAY_TYPE_CRS.getcode())).findFirst();
		TicketPlayInfo crsBetPlay = optionalcrs.isPresent() ? optionalcrs.get() : null;
		// 总进球
		Optional<TicketPlayInfo> optionalttg = list.stream().filter(dto -> dto.getPlayType() == (MatchPlayTypeEnum.PLAY_TYPE_TTG.getcode())).findFirst();
		TicketPlayInfo ttgBetPlay = optionalttg.isPresent() ? optionalttg.get() : null;
		// 让球胜平负
		Optional<TicketPlayInfo> optional2 = list.stream().filter(dto -> dto.getPlayType() == (MatchPlayTypeEnum.PLAY_TYPE_HHAD.getcode())).findFirst();
		TicketPlayInfo hhadBetPlay = optional2.isPresent() ? optional2.get() : null;
		// 胜平负
		Optional<TicketPlayInfo> optional3 = list.stream().filter(dto -> dto.getPlayType() == (MatchPlayTypeEnum.PLAY_TYPE_HAD.getcode())).findFirst();
		TicketPlayInfo hadBetPlay = optional3.isPresent() ? optional3.get() : null;
		// logger.info(JSONHelper.bean2json(hadBetPlay));
		// 半全场
		Optional<TicketPlayInfo> optional4 = list.stream().filter(dto -> dto.getPlayType() == (MatchPlayTypeEnum.PLAY_TYPE_HAFU.getcode())).findFirst();
		TicketPlayInfo hafuBetPlay = optional4.isPresent() ? optional4.get() : null;

		List<Double> rst = new ArrayList<Double>();
		if (crsBetPlay != null) {
			List<Double> cc = this.cc(crsBetPlay, ttgBetPlay, hhadBetPlay, hadBetPlay, hafuBetPlay);
			rst.addAll(cc);
		}
		if (ttgBetPlay != null) {
			crsBetPlay = this.bb(ttgBetPlay);
			List<Double> cc = this.cc(crsBetPlay, ttgBetPlay, hhadBetPlay, hadBetPlay, hafuBetPlay);
			rst.addAll(cc);
		}
		if (hadBetPlay != null) {
			List<Double> c = this.cc2(hhadBetPlay, hadBetPlay, hafuBetPlay);
			rst.addAll(c);
		}
		if (hafuBetPlay != null) {
			List<Double> c = this.cc2(hhadBetPlay, null, hafuBetPlay);
			rst.addAll(c);
		}
		if (hhadBetPlay != null) {
			List<Double> c = this.cc2(hhadBetPlay, null, null);
			rst.addAll(c);
		}
		return rst;

	}

	private List<Double> cc2(TicketPlayInfo hhadBetPlay, TicketPlayInfo hadBetPlay, TicketPlayInfo hafuBetPlay) {
		List<Double> allBetSumOdds = new ArrayList<Double>(1);
		// 胜平负
		List<Double> allOdds = new ArrayList<Double>();
		Double hOdds = null, dOdds = null, aOdds = null;
		if (hadBetPlay != null) {
			List<CellInfo> betCells = hadBetPlay.getCellInfos();
			for (CellInfo dto : betCells) {
				Integer cellCode = Integer.parseInt(dto.getCellCode());
				Double odds = Double.valueOf(dto.getCellOdds());
				if (MatchResultHadEnum.HAD_H.getCode().equals(cellCode)) {
					hOdds = odds;
				} else if (MatchResultHadEnum.HAD_D.getCode().equals(cellCode)) {
					dOdds = odds;
				} else if (MatchResultHadEnum.HAD_A.getCode().equals(cellCode)) {
					aOdds = odds;
				}
			}
		}
		// 半全场
		List<Double> hList = new ArrayList<Double>(0), dList = new ArrayList<Double>(0), aList = new ArrayList<Double>(0);
		if (hafuBetPlay != null) {
			List<CellInfo> betCells = hafuBetPlay.getCellInfos();
			for (CellInfo dto : betCells) {
				Integer checkCode = Integer.parseInt(dto.getCellCode().substring(1));
				Double odds = Double.valueOf(dto.getCellOdds());
				if (hOdds == null && dOdds == null && aOdds == null) {
					if (MatchResultHadEnum.HAD_H.getCode().equals(checkCode)) {
						hList.add(odds);
					} else if (MatchResultHadEnum.HAD_D.getCode().equals(checkCode)) {
						dList.add(odds);
					} else if (MatchResultHadEnum.HAD_A.getCode().equals(checkCode)) {
						aList.add(odds);
					}
				} else {
					if (hOdds != null && MatchResultHadEnum.HAD_H.getCode().equals(checkCode)) {
						hList.add(odds + hOdds);
					}
					if (dOdds != null && MatchResultHadEnum.HAD_D.getCode().equals(checkCode)) {
						dList.add(odds + dOdds);
					}
					if (aOdds != null && MatchResultHadEnum.HAD_A.getCode().equals(checkCode)) {
						aList.add(odds + aOdds);
					}
				}
			}

		}
		// 整合前两种
		boolean ish = false, isd = false, isa = false;
		if (hOdds != null || hList.size() > 0) {
			if (hList.size() == 0) {
				hList.add(hOdds);
			}
			ish = true;
		}
		if (dOdds != null || dList.size() > 0) {
			if (dList.size() == 0) {
				dList.add(dOdds);
			}
			isd = true;
		}
		if (aOdds != null || aList.size() > 0) {
			if (aList.size() == 0) {
				aList.add(aOdds);
			}
			isa = true;
		}
		// 让球
		// Double hhOdds = null, hdOdds = null, haOdds = null;
		if (hhadBetPlay != null) {
			List<CellInfo> betCells = hhadBetPlay.getCellInfos();
			Integer fixNum = Integer.valueOf(hhadBetPlay.getFixedodds());
			List<Double> naList = new ArrayList<Double>(aList.size() * 3);
			List<Double> ndList = new ArrayList<Double>(dList.size() * 3);
			List<Double> nhList = new ArrayList<Double>(hList.size() * 3);
			for (CellInfo dto : betCells) {
				Integer cellCode = Integer.parseInt(dto.getCellCode());
				Double odds = Double.valueOf(dto.getCellOdds());
				if (!ish && !isd && !isa) {
					allOdds.add(odds);
				} else {
					if (fixNum > 0) {
						if (ish && MatchResultHadEnum.HAD_H.getCode().equals(cellCode)) {
							/*
							 * hList.forEach(item->Double.sum(item, odds));
							 * nhList.addAll(hList);
							 */
							for (Double item : hList) {
								nhList.add(Double.sum(item, odds));
							}
						}
						if (isd && MatchResultHadEnum.HAD_H.getCode().equals(cellCode)) {
							/*
							 * dList.forEach(item->Double.sum(item, odds));
							 * ndList.addAll(dList);
							 */
							for (Double item : dList) {
								ndList.add(Double.sum(item, odds));
							}
						}
						if (isa) {
							List<Double> tnaList = new ArrayList<Double>(aList);
							for (Double item : tnaList) {
								naList.add(Double.sum(item, odds));
							}
							/*
							 * tnaList.forEach(item->Double.sum(item, odds));
							 * naList.addAll(tnaList);
							 */
						}
					} else {
						if (ish) {
							List<Double> tnhList = new ArrayList<Double>(hList);
							/*
							 * tnhList.forEach(item->Double.sum(item, odds));
							 * nhList.addAll(tnhList);
							 */
							for (Double item : tnhList) {
								nhList.add(Double.sum(item, odds));
							}
						}
						if (isd && MatchResultHadEnum.HAD_A.getCode().equals(cellCode)) {
							/*
							 * dList.forEach(item->Double.sum(item, odds));
							 * ndList.addAll(dList);
							 */
							for (Double item : dList) {
								ndList.add(Double.sum(item, odds));
							}
						}
						if (isa && MatchResultHadEnum.HAD_A.getCode().equals(cellCode)) {
							/*
							 * aList.forEach(item->Double.sum(item, odds));
							 * naList.addAll(aList);
							 */
							for (Double item : aList) {
								naList.add(Double.sum(item, odds));
							}
						}
					}
				}
			}
			if (nhList != null) {
				allOdds.addAll(nhList);
			}
			if (naList != null) {
				allOdds.addAll(naList);
			}
			if (ndList != null) {
				allOdds.addAll(ndList);
			}
		}
		if (allOdds.size() == 0) {
			if (hList != null) {
				allOdds.addAll(hList);
			}
			if (aList != null) {
				allOdds.addAll(aList);
			}
			if (dList != null) {
				allOdds.addAll(dList);
			}
		}
		// logger.info("--------------" + JSONHelper.bean2json(allOdds));
		allBetSumOdds.addAll(allOdds);
		return allBetSumOdds;
	}

	private List<Double> cc(TicketPlayInfo crsBetPlay, TicketPlayInfo ttgBetPlay, TicketPlayInfo hhadBetPlay, TicketPlayInfo hadBetPlay, TicketPlayInfo hafuBetPlay) {
		// 比分的所有项
		List<CellInfo> betCells = crsBetPlay.getCellInfos();// 比分的所有选项
		List<Double> allBetSumOdds = new ArrayList<Double>();
		for (CellInfo dto : betCells) {
			String cellCode = dto.getCellCode();
			String[] arr = cellCode.split("");
			int m = Integer.parseInt(arr[0]);
			int n = Integer.parseInt(arr[1]);
			int sum = m + n;// 总进球数
			int sub = m - n;// 进球差数
			List<Double> allOdds = new ArrayList<Double>();
			String cellOdds = dto.getCellOdds();
			if (StringUtils.isNotBlank(cellOdds)) {
				allOdds.add(Double.valueOf(cellOdds));
			}
			// 1.总进球
			if (ttgBetPlay != null) {
				List<CellInfo> betCells2 = ttgBetPlay.getCellInfos();
				int sucCode = sum > 7 ? 7 : sum;
				Optional<CellInfo> optional = betCells2.stream().filter(betCell -> Integer.parseInt(betCell.getCellCode()) == sucCode).findFirst();
				if (optional.isPresent()) {
					Double odds = Double.valueOf(optional.get().getCellOdds());// 选中的总进球玩法的可用赔率
					if (allOdds.size() == 0) {
						allOdds.add(odds);
					} else {
						Double old = allOdds.remove(0);
						allOdds.add(Double.sum(old, odds));
					}
				}
			}
			// 2。让球胜平负
			if (hhadBetPlay != null) {
				List<CellInfo> betCells2 = hhadBetPlay.getCellInfos();
				int sucCode = sub + Integer.valueOf(hhadBetPlay.getFixedodds());
				if (sucCode > 0) {
					sucCode = MatchResultHadEnum.HAD_H.getCode();
				} else if (sucCode < 0) {
					sucCode = MatchResultHadEnum.HAD_A.getCode();
				} else {
					sucCode = MatchResultHadEnum.HAD_D.getCode();
				}
				final int sucCode1 = sucCode;
				Optional<CellInfo> optional = betCells2.stream().filter(betCell -> Integer.parseInt(betCell.getCellCode()) == sucCode1).findFirst();
				if (optional.isPresent()) {
					Double odds = Double.valueOf(optional.get().getCellOdds());// 选中的让球胜平负玩法的可用赔率
					Double old = allOdds.remove(0);
					allOdds.add(Double.sum(old, odds));
				}
			}
			// 3.胜平负
			boolean isH = false, isA = false;
			if (hadBetPlay != null) {
				List<CellInfo> betCells2 = hadBetPlay.getCellInfos();
				if (sum == 0) {// 平
					int sucCode = MatchResultHadEnum.HAD_D.getCode();
					Optional<CellInfo> optional = betCells2.stream().filter(betCell -> Integer.parseInt(betCell.getCellCode()) == sucCode).findFirst();
					if (optional.isPresent()) {// 选中的胜平负玩法的可用赔率
						Double odds = Double.valueOf(optional.get().getCellOdds());
						Double old = allOdds.remove(0);
						allOdds.add(Double.sum(old, odds));
					}
				} else if (sum == 1) {// 胜，负
					if (n == 0) {
						int sucCode = MatchResultHadEnum.HAD_H.getCode();
						Optional<CellInfo> optional = betCells2.stream().filter(betCell -> Integer.parseInt(betCell.getCellCode()) == sucCode).findFirst();
						if (optional.isPresent()) {// 选中的胜平负玩法的可用赔率
							Double odds = Double.valueOf(optional.get().getCellOdds());
							Double old = allOdds.remove(0);
							allOdds.add(Double.sum(old, odds));
							isH = true;
						}
					} else {
						int sucCode = MatchResultHadEnum.HAD_A.getCode();
						Optional<CellInfo> optional1 = betCells2.stream().filter(betCell -> Integer.parseInt(betCell.getCellCode()) == sucCode).findFirst();
						if (optional1.isPresent()) {// 选中的胜平负玩法的可用赔率
							Double odds = Double.valueOf(optional1.get().getCellOdds());
							Double old = allOdds.remove(0);
							allOdds.add(Double.sum(old, odds));
							isA = true;
						}
					}
				} else {
					if (sub > 0) {// 胜
						int sucCode = MatchResultHadEnum.HAD_H.getCode();
						Optional<CellInfo> optional = betCells2.stream().filter(betCell -> Integer.parseInt(betCell.getCellCode()) == sucCode).findFirst();
						if (optional.isPresent()) {// 选中的胜平负玩法的可用赔率
							Double odds = Double.valueOf(optional.get().getCellOdds());
							Double old = allOdds.remove(0);
							allOdds.add(Double.sum(old, odds));
						}
					} else if (sub < 0) {// 负
						int sucCode = MatchResultHadEnum.HAD_A.getCode();
						Optional<CellInfo> optional = betCells2.stream().filter(betCell -> Integer.parseInt(betCell.getCellCode()) == sucCode).findFirst();
						if (optional.isPresent()) {// 选中的胜平负玩法的可用赔率
							Double odds = Double.valueOf(optional.get().getCellOdds());
							Double old = allOdds.remove(0);
							allOdds.add(Double.sum(old, odds));
						}
					} else {// 平
						int sucCode = MatchResultHadEnum.HAD_D.getCode();
						Optional<CellInfo> optional = betCells2.stream().filter(betCell -> Integer.parseInt(betCell.getCellCode()) == sucCode).findFirst();
						if (optional.isPresent()) {// 选中的胜平负玩法的可用赔率
							Double odds = Double.valueOf(optional.get().getCellOdds());
							Double old = allOdds.remove(0);
							allOdds.add(Double.sum(old, odds));
						}
					}
				}
			}
			// 4.半全场
			if (hafuBetPlay != null) {
				List<CellInfo> betCells2 = hafuBetPlay.getCellInfos();
				if (sum == 0) {
					Optional<CellInfo> optional = betCells2.stream().filter(betCell -> MatchResultHafuEnum.HAFU_DD.getCode().equals(betCell.getCellCode())).findFirst();
					if (optional.isPresent()) {
						Double odds = Double.valueOf(optional.get().getCellOdds());
						Double old = allOdds.remove(0);
						allOdds.add(Double.sum(old, odds));
					}
				} else if (sum == 1) {
					Double old = allOdds.remove(0);
					if (isH) {
						for (CellInfo betCell : betCells2) {
							String betCellCode = betCell.getCellCode();
							if (betCellCode.equals(MatchResultHafuEnum.HAFU_DH.getCode()) || betCellCode.equals(MatchResultHafuEnum.HAFU_HH.getCode())) {
								Double odds = Double.valueOf(betCell.getCellOdds());
								allOdds.add(Double.sum(old, odds));
							}
						}
					}
					if (isA) {
						for (CellInfo betCell : betCells2) {
							String betCellCode = betCell.getCellCode();
							if (betCellCode.equals(MatchResultHafuEnum.HAFU_DA.getCode()) || betCellCode.equals(MatchResultHafuEnum.HAFU_AA.getCode())) {
								Double odds = Double.valueOf(betCell.getCellOdds());
								allOdds.add(Double.sum(old, odds));
							}
						}
					}
				} else {
					Double old = allOdds.remove(0);
					if (sub > 0) {
						for (CellInfo betCell : betCells2) {
							String betCellCode = betCell.getCellCode();
							if (betCellCode.equals(MatchResultHafuEnum.HAFU_DH.getCode()) || betCellCode.equals(MatchResultHafuEnum.HAFU_HH.getCode())) {
								Double odds = Double.valueOf(betCell.getCellOdds());
								allOdds.add(Double.sum(old, odds));
							}
							if (n != 0 && betCellCode.equals(MatchResultHafuEnum.HAFU_AH.getCode())) {
								Double odds = Double.valueOf(betCell.getCellOdds());
								allOdds.add(Double.sum(old, odds));
							}
						}
					} else if (sub < 0) {
						for (CellInfo betCell : betCells2) {
							String betCellCode = betCell.getCellCode();
							if (betCellCode.equals(MatchResultHafuEnum.HAFU_DA.getCode()) || betCellCode.equals(MatchResultHafuEnum.HAFU_AA.getCode())) {
								Double odds = Double.valueOf(betCell.getCellOdds());
								allOdds.add(Double.sum(old, odds));
							}
							if (n != 0 && betCellCode.equals(MatchResultHafuEnum.HAFU_HA.getCode())) {
								Double odds = Double.valueOf(betCell.getCellOdds());
								allOdds.add(Double.sum(old, odds));
							}
						}
					} else {
						for (CellInfo betCell : betCells2) {
							String betCellCode = betCell.getCellCode();
							if (betCellCode.equals(MatchResultHafuEnum.HAFU_HD.getCode()) || betCellCode.equals(MatchResultHafuEnum.HAFU_DD.getCode()) || betCellCode.equals(MatchResultHafuEnum.HAFU_AD.getCode())) {
								Double odds = Double.valueOf(betCell.getCellOdds());
								allOdds.add(Double.sum(old, odds));
							}
						}
					}
				}
			}
			allBetSumOdds.addAll(allOdds);
		}
		return allBetSumOdds;
	}

	private TicketPlayInfo bb(TicketPlayInfo ttgBetPlay) {
		TicketPlayInfo crsBetPlay;
		List<CellInfo> ttgBetCells = ttgBetPlay.getCellInfos();
		List<CellInfo> ncrsBetCells = new ArrayList<CellInfo>();
		crsBetPlay = new TicketPlayInfo();
		crsBetPlay.setCellInfos(ncrsBetCells);
		for (CellInfo matchCellDto : ttgBetCells) {
			Integer qiuNum = Integer.parseInt(matchCellDto.getCellCode());
			if (qiuNum == 0) {
				CellInfo nmatchCellDto = new CellInfo(MatchResultCrsEnum.CRS_00.getCode(), MatchResultCrsEnum.CRS_00.getMsg());
				ncrsBetCells.add(nmatchCellDto);
			} else if (qiuNum == 1) {
				CellInfo nmatchCellDto = new CellInfo(MatchResultCrsEnum.CRS_10.getCode(), MatchResultCrsEnum.CRS_10.getMsg());
				CellInfo nmatchCellDto1 = new CellInfo(MatchResultCrsEnum.CRS_01.getCode(), MatchResultCrsEnum.CRS_01.getMsg());
				ncrsBetCells.add(nmatchCellDto);
				ncrsBetCells.add(nmatchCellDto1);
			} else if (qiuNum == 2) {
				CellInfo nmatchCellDto = new CellInfo(MatchResultCrsEnum.CRS_11.getCode(), MatchResultCrsEnum.CRS_11.getMsg());
				CellInfo nmatchCellDto1 = new CellInfo(MatchResultCrsEnum.CRS_02.getCode(), MatchResultCrsEnum.CRS_02.getMsg());
				CellInfo nmatchCellDto2 = new CellInfo(MatchResultCrsEnum.CRS_20.getCode(), MatchResultCrsEnum.CRS_20.getMsg());
				ncrsBetCells.add(nmatchCellDto);
				ncrsBetCells.add(nmatchCellDto1);
				ncrsBetCells.add(nmatchCellDto2);
			} else if (qiuNum == 3) {
				CellInfo nmatchCellDto = new CellInfo(MatchResultCrsEnum.CRS_30.getCode(), MatchResultCrsEnum.CRS_30.getMsg());
				CellInfo nmatchCellDto1 = new CellInfo(MatchResultCrsEnum.CRS_03.getCode(), MatchResultCrsEnum.CRS_03.getMsg());
				CellInfo nmatchCellDto2 = new CellInfo(MatchResultCrsEnum.CRS_21.getCode(), MatchResultCrsEnum.CRS_21.getMsg());
				CellInfo nmatchCellDto3 = new CellInfo(MatchResultCrsEnum.CRS_12.getCode(), MatchResultCrsEnum.CRS_12.getMsg());
				ncrsBetCells.add(nmatchCellDto);
				ncrsBetCells.add(nmatchCellDto1);
				ncrsBetCells.add(nmatchCellDto2);
				ncrsBetCells.add(nmatchCellDto3);
			} else if (qiuNum == 4) {
				CellInfo nmatchCellDto = new CellInfo(MatchResultCrsEnum.CRS_40.getCode(), MatchResultCrsEnum.CRS_40.getMsg());
				CellInfo nmatchCellDto1 = new CellInfo(MatchResultCrsEnum.CRS_04.getCode(), MatchResultCrsEnum.CRS_04.getMsg());
				CellInfo nmatchCellDto2 = new CellInfo(MatchResultCrsEnum.CRS_31.getCode(), MatchResultCrsEnum.CRS_31.getMsg());
				CellInfo nmatchCellDto3 = new CellInfo(MatchResultCrsEnum.CRS_13.getCode(), MatchResultCrsEnum.CRS_13.getMsg());
				CellInfo nmatchCellDto4 = new CellInfo(MatchResultCrsEnum.CRS_22.getCode(), MatchResultCrsEnum.CRS_22.getMsg());
				ncrsBetCells.add(nmatchCellDto);
				ncrsBetCells.add(nmatchCellDto1);
				ncrsBetCells.add(nmatchCellDto2);
				ncrsBetCells.add(nmatchCellDto3);
				ncrsBetCells.add(nmatchCellDto4);
			} else if (qiuNum == 5) {
				CellInfo nmatchCellDto = new CellInfo(MatchResultCrsEnum.CRS_50.getCode(), MatchResultCrsEnum.CRS_50.getMsg());
				CellInfo nmatchCellDto1 = new CellInfo(MatchResultCrsEnum.CRS_05.getCode(), MatchResultCrsEnum.CRS_05.getMsg());
				CellInfo nmatchCellDto2 = new CellInfo(MatchResultCrsEnum.CRS_41.getCode(), MatchResultCrsEnum.CRS_41.getMsg());
				CellInfo nmatchCellDto3 = new CellInfo(MatchResultCrsEnum.CRS_14.getCode(), MatchResultCrsEnum.CRS_14.getMsg());
				CellInfo nmatchCellDto4 = new CellInfo(MatchResultCrsEnum.CRS_32.getCode(), MatchResultCrsEnum.CRS_32.getMsg());
				CellInfo nmatchCellDto5 = new CellInfo(MatchResultCrsEnum.CRS_23.getCode(), MatchResultCrsEnum.CRS_23.getMsg());
				ncrsBetCells.add(nmatchCellDto);
				ncrsBetCells.add(nmatchCellDto1);
				ncrsBetCells.add(nmatchCellDto2);
				ncrsBetCells.add(nmatchCellDto3);
				ncrsBetCells.add(nmatchCellDto4);
				ncrsBetCells.add(nmatchCellDto5);
			} else if (qiuNum == 6) {
				CellInfo nmatchCellDto = new CellInfo(MatchResultCrsEnum.CRS_15.getCode(), MatchResultCrsEnum.CRS_15.getMsg());
				CellInfo nmatchCellDto1 = new CellInfo(MatchResultCrsEnum.CRS_51.getCode(), MatchResultCrsEnum.CRS_51.getMsg());
				CellInfo nmatchCellDto2 = new CellInfo(MatchResultCrsEnum.CRS_24.getCode(), MatchResultCrsEnum.CRS_24.getMsg());
				CellInfo nmatchCellDto3 = new CellInfo(MatchResultCrsEnum.CRS_42.getCode(), MatchResultCrsEnum.CRS_42.getMsg());
				CellInfo nmatchCellDto4 = new CellInfo(MatchResultCrsEnum.CRS_33.getCode(), MatchResultCrsEnum.CRS_33.getMsg());
				ncrsBetCells.add(nmatchCellDto);
				ncrsBetCells.add(nmatchCellDto1);
				ncrsBetCells.add(nmatchCellDto2);
				ncrsBetCells.add(nmatchCellDto3);
				ncrsBetCells.add(nmatchCellDto4);
			} else if (qiuNum == 7) {
				CellInfo nmatchCellDto = new CellInfo(MatchResultCrsEnum.CRS_52.getCode(), MatchResultCrsEnum.CRS_52.getMsg());
				CellInfo nmatchCellDto1 = new CellInfo(MatchResultCrsEnum.CRS_25.getCode(), MatchResultCrsEnum.CRS_25.getMsg());
				ncrsBetCells.add(nmatchCellDto);
				ncrsBetCells.add(nmatchCellDto1);
			}
		}
		return crsBetPlay;
	}

	/**
	 * 计算投注组合
	 * 
	 * @param ticketInfos
	 * @param betTypes
	 * @return
	 */
	private Map<String, List<String>> getBetIndexList(List<TicketInfo> ticketInfos, String betTypes) {
		// 读取设胆的索引
		List<String> indexList = new ArrayList<String>(ticketInfos.size());
		List<String> danIndexList = new ArrayList<String>(3);
		for (int i = 0; i < ticketInfos.size(); i++) {
			indexList.add(i + "");
			int isDan = ticketInfos.get(i).getIsDan();
			if (isDan != 0) {
				danIndexList.add(i + "");
			}
		}
		String[] split = betTypes.split(",");
		Map<String, List<String>> indexMap = new HashMap<String, List<String>>();
		for (String betType : split) {
			char[] charArray = betType.toCharArray();
			if (charArray.length == 2 && charArray[1] == '1') {
				int num = Integer.valueOf(String.valueOf(charArray[0]));
				// 计算场次组合
				List<String> betIndexList = new ArrayList<String>();
				betNum1("", num, indexList, betIndexList);
				if (danIndexList.size() > 0) {
					betIndexList = betIndexList.stream().filter(item -> {
						for (String danIndex : danIndexList) {
							if (!item.contains(danIndex)) {
								return false;
							}
						}
						return true;
					}).collect(Collectors.toList());
				}
				indexMap.put(betType, betIndexList);
			}
		}
		return indexMap;
	}

	/**
	 * 计算组合
	 * 
	 * @param str
	 * @param num
	 * @param list
	 * @param betList
	 */
	private static void betNum1(String str, int num, List<String> list, List<String> betList) {
		LinkedList<String> link = new LinkedList<String>(list);
		while (link.size() > 0) {
			String remove = link.remove(0);
			String item = str + remove + ",";
			if (num == 1) {
				betList.add(item.substring(0, item.length() - 1));
			} else {
				betNum1(item, num - 1, link, betList);
			}
		}
	}

	/**
	 * 转化投注信息用来计算预测试奖金
	 * 
	 * @param ticketData
	 */
	private TicketInfo ticketPlayInfos(List<String> ticketData, String fixedodds) {
		TicketInfo ticketInfo = new TicketInfo();
		List<TicketPlayInfo> ticketPlayInfos = new ArrayList<TicketPlayInfo>(ticketData.size());
		ticketInfo.setTicketPlayInfos(ticketPlayInfos);
		for (String temp : ticketData) {
			if (StringUtils.isBlank(temp)) {
				continue;
			}
			String cells = temp.substring(temp.lastIndexOf("|") + 1);
			String[] cellArr = cells.split(",");
			List<CellInfo> cellInfos = new ArrayList<CellInfo>(cellArr.length);
			for (String cellStr : cellArr) {
				String[] split = cellStr.split("@");
				CellInfo cellInfo = new CellInfo();
				cellInfo.setCellCode(split[0]);
				cellInfo.setCellOdds(split[1]);
				cellInfos.add(cellInfo);
			}
			TicketPlayInfo ticketPlayInfo = new TicketPlayInfo();
			String playType = temp.substring(0, temp.indexOf("|"));
			String playCode = temp.substring(temp.indexOf("|") + 1, temp.lastIndexOf("|"));
			ticketPlayInfo.setCellInfos(cellInfos);
			ticketPlayInfo.setPlayType(Integer.parseInt(playType));
			ticketPlayInfo.setFixedodds(fixedodds);
			ticketInfo.setPlayCode(playCode);
			ticketPlayInfos.add(ticketPlayInfo);
		}
		return ticketInfo;
	}

	/**
	 * 匹配了真实赔率的彩票数据
	 * 
	 * @param printSp
	 * @return
	 */
	public String updateTicketDataWithSp(List<String> spList, List<String> ticketData) {
		List<String> upTicketData = new LinkedList<String>();
		for (String temp : ticketData) {
			if (StringUtils.isBlank(temp)) {
				continue;
			}
			String playType = temp.substring(0, temp.indexOf("|"));
			String playCode = temp.substring(temp.indexOf("|") + 1, temp.lastIndexOf("|"));
			String cells = temp.substring(temp.lastIndexOf("|") + 1);
			for (String sp : spList) {
				String playType1 = sp.substring(0, sp.indexOf("|"));
				String playCode1 = sp.substring(sp.indexOf("|") + 1, sp.lastIndexOf("|"));
				String spStr = sp.substring(sp.lastIndexOf("|") + 1);
				if (playCode.equals(playCode1) && playType.equals(playType1)) {
					String temp3Str = "";
					String cellCode = "";
					String spCode = "";
					List<String> spStrs = new LinkedList<String>();
					if (cells.contains(",")) {
						String[] cellArr = cells.split(",");
						String[] spArr = spStr.split(",");
						for (int i = 0; i < cellArr.length; i++) {
							temp3Str = cellArr[i];
							cellCode = cellArr[i].substring(0, cellArr[i].indexOf("@"));
							for (int j = 0; j < spArr.length; j++) {
								spCode = spArr[j].substring(0, spArr[j].indexOf("@"));
								if (cellCode.equals(spCode)) {
									temp3Str = spArr[j];
								}
							}
							spStrs.add(temp3Str);
						}
					} else {
						temp3Str = cells;
						cellCode = cells.substring(0, cells.indexOf("@"));
						String[] spArr = spStr.split(",");
						for (int j = 0; j < spArr.length; j++) {
							spCode = spArr[j].substring(0, spArr[j].indexOf("@"));
							if (cellCode.equals(spCode)) {
								temp3Str = spArr[j];
							}
						}
						spStrs.add(temp3Str);
					}
					temp = playType + "|" + playCode + "|" + StringUtils.join(spStrs.toArray(), ",");
				}
			}
			upTicketData.add(temp);
		}
		return StringUtils.join(upTicketData.toArray(), ";");
	}

	/**
	 * 兑奖时，修改订单数据
	 * 
	 * @param params
	 */
	public int updateOrderInfoByExchangeReward(LotteryPrintMoneyParam param) {
		List<OrderDataParam> orderDataDTOs = param.getOrderDataDTOs();
		int n = 0;
		for (OrderDataParam orderDataParam : orderDataDTOs) {
			Order order = new Order();
			order.setOrderSn(orderDataParam.getOrderSn());
			order.setWinningMoney(orderDataParam.getRealRewardMoney());
			order.setOrderStatus(orderDataParam.getOrderStatus());
			order.setAwardTime(DateUtil.getCurrentTimeLong());
			n += orderMapper.updateWiningMoney(order);
		}
		return n;
	}

	/**
	 * 开奖后，修改订单数据
	 * 
	 * @param param
	 */
	public int updateOrderInfoByReward(LotteryPrintRewardParam param) {
		String issue = param.getIssue();
		DlLotteryRewardByIssueParam rewardParam = new DlLotteryRewardByIssueParam();
		rewardParam.setIssue(issue);
		BaseResult<LotteryRewardByIssueDTO> result = lotteryRewardService.queryRewardByIssue(rewardParam);
		LotteryRewardByIssueDTO lotteryRewardByIssueDTO = result.getData();
		if (null == lotteryRewardByIssueDTO) {
			log.error("开奖后，修改订单数据，期次：" + issue);
		}
		Condition condition = new Condition(OrderDetail.class);
		Criteria criteria = condition.createCriteria();
		criteria.andCondition("issue=", issue);
		List<OrderDetail> orderDetails = orderDetailMapper.selectByCondition(condition);
		if (CollectionUtils.isNotEmpty(orderDetails)) {
			List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>(orderDetails.size());
			for (OrderDetail orderDetail : orderDetails) {
				OrderDetail od = new OrderDetail();
				od.setOrderDetailId(orderDetail.getOrderDetailId());
				od.setMatchResult(getMatchResult(orderDetail.getTicketData(), lotteryRewardByIssueDTO.getRewardData(), issue));
				orderDetailList.add(od);
			}
			return updateOrderDetailByReward(orderDetailList);
		}
		return 1;
	}

	public int updateOrderInfoByMatchResult(String playCode) {
		QueryMatchResultByPlayCodeParam rewardParam = new QueryMatchResultByPlayCodeParam();
		rewardParam.setPlayCode(playCode);
		BaseResult<List<MatchResultDTO>> result = matchResultService.queryMatchResultByPlayCode(rewardParam);
		List<MatchResultDTO> resultDTOs = result.getData();
		if (CollectionUtils.isEmpty(resultDTOs)) {
			log.error("开奖后，修改订单数据，期次：" + playCode);
		}
		Condition condition = new Condition(OrderDetail.class);
		Criteria criteria = condition.createCriteria();
		criteria.andCondition("issue=", playCode);
		List<OrderDetail> orderDetails = orderDetailMapper.selectByCondition(condition);
		if (CollectionUtils.isNotEmpty(orderDetails)) {
			List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>(orderDetails.size());
			for (OrderDetail orderDetail : orderDetails) {
				String ticketDataStr = orderDetail.getTicketData();
				String[] split = ticketDataStr.split(";");
				OrderDetail od = new OrderDetail();
				od.setOrderDetailId(orderDetail.getOrderDetailId());
				StringBuffer sbuf = new StringBuffer();
				for (String ticketData : split) {
					if (StringUtils.isBlank(ticketData) || !ticketData.contains("|")) {
						continue;
					}
					Integer playType = Integer.valueOf(ticketData.substring(0, ticketData.indexOf("|")));
					// String issue =
					// ticketData.substring(ticketData.indexOf("|") + 1,
					// ticketData.lastIndexOf("|"));
					// String cells =
					// ticketData.substring(ticketData.lastIndexOf("|")+1,
					// ticketData.length());
					for (MatchResultDTO dto : resultDTOs) {
						if (playType.equals(dto.getPlayType())) {
							sbuf.append("0").append(dto.getPlayType()).append("|").append(dto.getPlayCode()).append("|").append(dto.getCellCode()).append(";");
						}
					}
				}
				if (sbuf.length() > 0) {
					od.setMatchResult(sbuf.substring(0, sbuf.length() - 1));
					orderDetailList.add(od);
				}
			}
			return updateOrderDetailByReward(orderDetailList);
		}
		return 1;
	}

	/**
	 * 高速批量更新Order的状态为已派奖
	 * 
	 * @param list
	 */
	public int updateOrderStatusRewarded(List<String> list) {
		Connection conn = null;
		try {
			Class.forName(dbDriver);
			conn = (Connection) DriverManager.getConnection(dbUrl, dbUserName, dbPass);
			conn.setAutoCommit(false);
			String sql = "update dl_order set order_status= ? where order_sn = ?";
			PreparedStatement prest = (PreparedStatement) conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			for (int i = 0, size = list.size(); i < size; i++) {
				prest.setInt(1, ProjectConstant.ORDER_STATUS_ALREADY);
				prest.setString(2, list.get(i));
				prest.addBatch();
			}
			prest.executeBatch();
			conn.commit();
			conn.close();
		} catch (Exception ex) {
			try {
				conn.rollback();
			} catch (Exception e) {
				e.printStackTrace();
				log.error(DateUtil.getCurrentDateTime() + "执行updateOrderStatus异常，且回滚异常:" + ex.getMessage());
				return -1;
			}
		}
		return 1;
	}

	/**
	 * 获取中奖用户及奖金
	 * 
	 * @param param
	 * @return
	 */
	public List<OrderWithUserDTO> getOrderWithUserAndMoney(OrderWithUserParam param) {
		OrderWithUserParam orderWithUserParam = new OrderWithUserParam();
		orderWithUserParam.setStr("");
		List<OrderWithUserDTO> orderWithUserDTOs = orderMapper.selectOpenedAllRewardOrderList();
		return orderWithUserDTOs;
	}

	/**
	 * 根据开奖结果，匹配用户所选赛项的比赛结果
	 * 
	 * @param ticketData
	 * @return
	 */
	private String getMatchResult(String ticketData, String rewardStake, String issue) {
		List<String> rewardStakes = createRewardStakes(rewardStake);
		if (CollectionUtils.isEmpty(rewardStakes)) {
			log.error("根据开奖结果，匹配用户所选赛项的比赛结果，开奖结果为空");
		}
		List<String> ticketDatas = Arrays.asList(ticketData.split(";"));
		List<String> upTicketDatas = new LinkedList<String>();
		for (String ticketDataStr : ticketDatas) {
			String temp1 = ticketDataStr.substring(0, ticketDataStr.indexOf("|"));
			String temp2 = ticketDataStr.substring(ticketDataStr.indexOf("|") + 1, ticketDataStr.lastIndexOf("|"));
			if (issue.equals(temp2)) {
				for (String rewardStakeStr : rewardStakes) {
					String rewardStake1 = rewardStakeStr.substring(0, rewardStakeStr.indexOf("|"));
					String rewardStake2 = rewardStakeStr.substring(rewardStakeStr.indexOf("|") + 1, ticketDataStr.lastIndexOf("|"));
					if (temp1.equals(rewardStake1) && temp2.equals(rewardStake2)) {
						upTicketDatas.add(rewardStakeStr);
					}
				}
			}
		}
		return StringUtils.join(upTicketDatas.toArray(), ";");
	}

	/**
	 * 把开奖信息解析成List<String>
	 * 
	 * @param rewardsTakes
	 * @return
	 */
	public List<String> createRewardStakes(String rewardStake) {
		String[] arr = rewardStake.split(";");
		List<String> list = java.util.Arrays.asList(arr);
		return list;
	}

	/**
	 * 高速批量更新OrderDetail
	 * 
	 * @param list
	 */
	private int updateOrderDetailByReward(List<OrderDetail> list) {
		try {
			Class.forName(dbDriver);
			Connection conn = (Connection) DriverManager.getConnection(dbUrl, dbUserName, dbPass);
			conn.setAutoCommit(false);
			String sql = "update dl_order_detail set match_result = ? where order_detail_id = ?";
			PreparedStatement prest = (PreparedStatement) conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			for (int i = 0, size = list.size(); i < size; i++) {
				prest.setString(1, list.get(i).getMatchResult());
				prest.setInt(2, list.get(i).getOrderDetailId());
				prest.addBatch();
			}
			int[] rsts = prest.executeBatch();
			conn.commit();
			conn.close();
			log.info("updateOrderDetailByReward param detailsize=" + list.size() + " rst size=" + rsts.length);
			return rsts.length;
		} catch (SQLException ex) {
			log.error(ex.getMessage());
		} catch (ClassNotFoundException ex) {
			log.error(ex.getMessage());
		}
		return 0;
	}

	/**
	 * 组装订单数据
	 * 
	 * @param param
	 * @return
	 */
	private Order getOrderData(SubmitOrderParam param, Integer userId, String orderSn) {
		Order order = new Order();
		order.setGiveIntegral(0);
		order.setIsDelete(0);
		order.setMoneyPaid(param.getMoneyPaid());
		order.setOrderFrom(param.getOrderFrom());
		order.setOrderSn(orderSn);
		order.setOrderStatus(ProjectConstant.ORDER_STATUS_NOT_PAY);
		order.setOrderType(0);
		order.setParentSn("");
		order.setPayCode("");
		order.setPayId(0);
		order.setPayName("");
		order.setPaySn("");
		order.setPayStatus(ProjectConstant.PAY_STATUS_STAY);
		order.setPayTime(0);
		order.setSurplus(param.getSurplus());
		order.setTicketAmount(param.getTicketAmount());
		order.setUserBonusId(param.getUserBonusId());
		order.setBonus(param.getBonusAmount());
		order.setUserId(userId);
		order.setUserSurplus(new BigDecimal("0.00"));
		order.setUserSurplusLimit(new BigDecimal("0.00"));
		order.setThirdPartyPaid(param.getThirdPartyPaid());
		order.setAddTime(DateUtil.getCurrentTimeLong());
		order.setLotteryClassifyId(param.getLotteryClassifyId());
		order.setLotteryPlayClassifyId(param.getLotteryPlayClassifyId());
		order.setMatchTime(param.getMatchTime());
		order.setWinningMoney(new BigDecimal("0.00"));
		order.setPassType(param.getPassType());
		order.setPlayType(param.getPlayType());
		order.setCathectic(param.getCathectic());
		order.setBetNum(param.getBetNum());
		order.setAcceptTime(0);
		order.setTicketTime(0);
		order.setForecastMoney(param.getForecastMoney());
		order.setIssue(param.getIssue());
		order.setTicketNum(param.getTicketNum());
		return order;
	}

	/**
	 * 组装订单详情数据
	 * 
	 * @param param
	 * @return
	 */
	private List<OrderDetail> getOrderDetailData(SubmitOrderParam param, Integer orderId, String orderSn, Integer userId) {
		List<OrderDetail> orderDetails = new ArrayList<OrderDetail>();
		for (TicketDetail td : param.getTicketDetails()) {
			OrderDetail orderDetail = new OrderDetail();
			orderDetail.setGiveIntegral(0);
			orderDetail.setIsGuess(0);
			orderDetail.setOrderId(orderId);
			orderDetail.setOrderSn(orderSn);
			orderDetail.setTicketData(td.getTicketData());
			orderDetail.setMatchId(td.getMatch_id());
			orderDetail.setChangci(td.getChangci());
			orderDetail.setIssue(td.getIssue());
			orderDetail.setMatchTeam(td.getMatchTeam());
			orderDetail.setMatchResult(td.getMatchResult());
			orderDetail.setTicketStatus(ProjectConstant.TICKET_STATUS_NOT);
			orderDetail.setUserId(userId);
			orderDetail.setLotteryClassifyId(td.getLotteryClassifyId());
			orderDetail.setLotteryPlayClassifyId(td.getLotteryPlayClassifyId());
			orderDetail.setIsDan(td.getIsDan());
			orderDetail.setMatchTime(td.getMatchTime());
			orderDetail.setAddTime(DateUtil.getCurrentTimeLong());
			orderDetail.setFixedodds(td.getFixedodds());
			orderDetails.add(orderDetail);
		}
		return orderDetails;
	}

	/**
	 * 获取订单所用红包
	 * 
	 * @param orderSn
	 * @param orders
	 * @return
	 */
	private UserBonusParam setupUserBonusParam(String orderSn, Integer userBonusId) {
		UserBonusParam userBonusParam = new UserBonusParam();
		userBonusParam.setOrderSn(orderSn);
		userBonusParam.setUserBonusId(userBonusId);
		return userBonusParam;
	}

	/**
	 * 将用户红包状态置为已使用
	 * 
	 * @param userBonusParam
	 */
	private void reduceUserAccount(UserBonusParam userBonusParam) {
		if (null == userBonusParam) {
			return;
		}
		try {
			BaseResult<?> result = userAccountService.changeUserAccountByCreateOrder(userBonusParam);
			if (result.getCode() != RespStatusEnum.SUCCESS.getStatus()) {
				throw new UserAccountException(result.getCode(), result.getMsg());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new UserAccountException(OrderExceptionEnum.SUBMIT_ERROR_ACCOUNT);
		}
	}

	/**
	 * 组装订单日志表数据
	 * 
	 * @param orders
	 * @return
	 */
	private OrderLog createOrderLog(Order order, Integer userId, String stateType, boolean isNew, boolean isSysOpt) {
		int time = DateUtil.getCurrentTimeLong();
		OrderLog orderLog = new OrderLog();
		orderLog.setAddIp(IpUtil.getIpAddr());
		orderLog.setAddTime(time);
		orderLog.setOrderId(order.getOrderId());
		orderLog.setPayStatus(order.getPayStatus());
		orderLog.setShowName("会员");
		orderLog.setStatus(order.getOrderStatus());
		orderLog.setUserId(userId);
		orderLog.setContent(orderLogContentService.getOrderContent(orderLog, null, stateType, isNew, isSysOpt, userId));
		return orderLog;
	}

	/**
	 * 下单失败，回滚用户红包数据
	 * 
	 * @param userBonusParam
	 * @param orders
	 */
	private void rollbackUserAccount(UserBonusParam userBonusParam, Order order) {
		if (null == userBonusParam) {
			return;
		}
		try {
			BaseResult<?> result = userAccountService.rollbackChangeUserAccountByCreateOrder(userBonusParam);
			if (result.getCode() != RespStatusEnum.SUCCESS.getStatus()) {
				throw new UserAccountException(result.getCode(), result.getMsg());
			}
		} catch (Exception ex) {
			// 处理回滚失败的异常
			orderRollbackErrorLogService.saveAccountRollbackErrorLog(order.getOrderId());
			log.error("回滚订单失败，异常信息：{}", ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * 根据订单编号查询订单数据
	 * 
	 * @param snParam
	 * @return
	 */
	public OrderDTO getOrderInfoByOrderSn(OrderSnParam snParam) {
		Order order = new Order();
		order.setOrderSn(snParam.getOrderSn());
		order = orderMapper.selectOne(order);
		OrderDTO orderDTO = new OrderDTO();
		if (null == order) {
			return orderDTO;
		}
		try {
			BeanUtils.copyProperties(orderDTO, order);
		} catch (IllegalAccessException | InvocationTargetException e) {
			log.error("订单id：" + order.getOrderId() + "，查询订单失败");
			e.printStackTrace();
			throw new ServiceException(RespStatusEnum.FAIL.getCode(), "订单列表查询失败");
		}
		return orderDTO;
	}

	/**
	 * 根据订单状态查询订单列表
	 * 
	 * @param param
	 * @return
	 */
	public PageInfo<OrderInfoListDTO> getOrderInfoList(OrderInfoListParam param) {
		List<Integer> statusList = new ArrayList<Integer>();
		if ("-1".equals(param.getOrderStatus())) {
			statusList.add(ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY);
			statusList.add(ProjectConstant.ORDER_STATUS_FAIL_LOTTERY);
			statusList.add(ProjectConstant.ORDER_STATUS_STAY);
			statusList.add(ProjectConstant.ORDER_STATUS_NOT);
			statusList.add(ProjectConstant.ORDER_STATUS_ALREADY);
			statusList.add(ProjectConstant.ORDER_STATUS_REWARDING);
		} else if (ProjectConstant.ORDER_STATUS_ALREADY.equals(Integer.valueOf(param.getOrderStatus()))) {
			statusList.add(ProjectConstant.ORDER_STATUS_ALREADY);
			statusList.add(ProjectConstant.ORDER_STATUS_REWARDING);
		} else {
			statusList.add(Integer.valueOf(param.getOrderStatus()));
		}
		if (null == param.getPageNum())
			param.setPageNum("1");
		if (null == param.getPageSize())
			param.setPageSize("20");
		PageHelper.startPage(Integer.valueOf(param.getPageNum()), Integer.valueOf(param.getPageSize()));
		List<OrderInfoListDTO> orderInfoListDTOs = orderMapper.getOrderInfoList(statusList, SessionUtil.getUserId(), Integer.valueOf(param.getLotteryClassifyId()));
		if (CollectionUtils.isNotEmpty(orderInfoListDTOs)) {
			SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (OrderInfoListDTO orderInfoListDTO : orderInfoListDTOs) {
				if ("1".equals(param.getLotteryClassifyId())) {
					orderInfoListDTO.setOrderType(0);
					if (8 == orderInfoListDTO.getLotteryPlayClassifyId()) {
						orderInfoListDTO.setOrderType(1);
					}
				}
				Date matchTime = null;
				try {
					matchTime = sdf.parse(orderInfoListDTO.getMatchTime());
					orderInfoListDTO.setMatchTime(sdf.format(matchTime));
					if (org.apache.commons.lang3.StringUtils.isNotBlank(orderInfoListDTO.getPayTime())) {
						long parseLong = Long.parseLong(orderInfoListDTO.getPayTime());
						if (parseLong > 0) {
							orderInfoListDTO.setPayTime(DateUtil.getCurrentTimeString(parseLong, DateUtil.datetimeFormat));
						}
					}
				} catch (ParseException e) {
					log.error("根据订单状态查询订单列表，时间转换异常");
					throw new ServiceException(RespStatusEnum.FAIL.getCode(), "订单列表查询失败");
				}
				Calendar calendar = Calendar.getInstance();
				if (ProjectConstant.ORDER_STATUS_FAIL_LOTTERY.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus()))) {
					orderInfoListDTO.setOrderStatusInfo("资金已退回");
					orderInfoListDTO.setOrderStatusDesc("出票失败");
				} else if (ProjectConstant.ORDER_STATUS_STAY.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus()))) {
					long from = new Date().getTime();
					long to = matchTime.getTime();
					int days = (int) ((to - from) / (1000 * 60 * 60 * 24));
					if (days < 0) {
						orderInfoListDTO.setOrderStatusInfo("开奖失败");
						if (1 == orderInfoListDTO.getOrderType()) {
							orderInfoListDTO.setOrderStatusInfo("即将开奖");
						}
					} else if (days == 0) {
						orderInfoListDTO.setOrderStatusInfo("即将开奖");
					} else {
						orderInfoListDTO.setOrderStatusInfo(days + "天后开奖");
					}
					orderInfoListDTO.setOrderStatusDesc("待开奖");
				} else if (ProjectConstant.ORDER_STATUS_NOT.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus()))) {
					calendar.setTime(matchTime);
					calendar.add(Calendar.HOUR_OF_DAY, 2);// 开赛2小时后
					orderInfoListDTO.setOrderStatusInfo(calendar.get(Calendar.MONTH) + 1 + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日" + "开奖");
					orderInfoListDTO.setOrderStatusDesc("未中奖");
				} else if (ProjectConstant.ORDER_STATUS_ALREADY.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus()))) {
					orderInfoListDTO.setOrderStatusInfo("中奖金额：¥" + orderInfoListDTO.getWinningMoney());
					orderInfoListDTO.setOrderStatusDesc("已中奖");
				} else if (ProjectConstant.ORDER_STATUS_REWARDING.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus()))) {
					orderInfoListDTO.setOrderStatusInfo("中奖金额：¥" + orderInfoListDTO.getWinningMoney());
					orderInfoListDTO.setOrderStatusDesc("派奖中");
				} else if (ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY.equals(Integer.valueOf(orderInfoListDTO.getOrderStatus()))) {
					orderInfoListDTO.setOrderStatusInfo("出票中");
					orderInfoListDTO.setOrderStatusDesc("待出票");
				}
			}
		}
		PageInfo<OrderInfoListDTO> pageInfo = new PageInfo<OrderInfoListDTO>(orderInfoListDTOs);
		return pageInfo;
	}

	/**
	 * 查询订单详情
	 * 
	 * @param param
	 * @return
	 */
	public OrderDetailDTO getOrderDetail(OrderDetailParam param) {
		if (StringUtils.isBlank(param.getOrderId())) {
			log.error("订单id：为空，该订单不存在");
			throw new ServiceException(RespStatusEnum.FAIL.getCode(), "订单不能为空");
		}
		OrderDetailDTO orderDetailDTO = new OrderDetailDTO();
		Order order = new Order();
		Integer userId = SessionUtil.getUserId();
		order.setUserId(userId);
		order.setOrderId(Integer.valueOf(param.getOrderId()));
		order = orderMapper.selectOne(order);
		if (null == order) {
			log.error("订单id：" + param.getOrderId() + "，该订单不存在");
			throw new ServiceException(RespStatusEnum.FAIL.getCode(), "订单不存在");
		}
		Integer lotteryClassifyId = order.getLotteryClassifyId();
		Integer lotteryPlayClassifyId = order.getLotteryPlayClassifyId();
		LotteryClassifyTemp lotteryClassify = orderDetailMapper.lotteryClassify(lotteryClassifyId);
		if (lotteryClassify != null) {
			orderDetailDTO.setLotteryClassifyImg(lotteryClassify.getLotteryImg());
			orderDetailDTO.setLotteryClassifyName(lotteryClassify.getLotteryName());
		} else {
			orderDetailDTO.setLotteryClassifyImg("");
			orderDetailDTO.setLotteryClassifyName("");
		}
		if (ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("等待出票");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY.toString());
			orderDetailDTO.setOrderStatusDesc("支付成功");
			orderDetailDTO.setProcessResult("处理中");
			orderDetailDTO.setForecastMoney("预测奖金¥" + order.getForecastMoney().toString());
		} else if (ProjectConstant.ORDER_STATUS_FAIL_LOTTERY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("彩金已退回");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_FAIL_LOTTERY.toString());
			orderDetailDTO.setOrderStatusDesc("出票失败");
			orderDetailDTO.setProcessResult("");
			orderDetailDTO.setForecastMoney("");
		} else if (ProjectConstant.ORDER_STATUS_STAY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_STAY.toString());
			orderDetailDTO.setOrderStatusDesc("出票成功");
			orderDetailDTO.setProcessResult("等待开奖");
			orderDetailDTO.setForecastMoney("预测奖金¥" + order.getForecastMoney().toString());
		} else if (ProjectConstant.ORDER_STATUS_NOT.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("感谢您助力公益事业");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_NOT.toString());
			orderDetailDTO.setOrderStatusDesc("未中奖");
			orderDetailDTO.setProcessResult("再接再厉");
			orderDetailDTO.setForecastMoney("");
		} else if (ProjectConstant.ORDER_STATUS_ALREADY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc(order.getWinningMoney().toString());
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_ALREADY.toString());
			orderDetailDTO.setOrderStatusDesc("已中奖");
			orderDetailDTO.setProcessResult("恭喜中奖");
			orderDetailDTO.setForecastMoney("");
		} else if (ProjectConstant.ORDER_STATUS_REWARDING.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc(order.getWinningMoney().toString());
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_REWARDING.toString());
			orderDetailDTO.setOrderStatusDesc("派奖中");
			orderDetailDTO.setProcessResult("派奖中");
			orderDetailDTO.setForecastMoney("");
		} else if (ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY.equals(order.getOrderStatus())) {
			orderDetailDTO.setProcessStatusDesc("");
			orderDetailDTO.setOrderStatus(ProjectConstant.ORDER_STATUS_PAY_FAIL_LOTTERY.toString());
			orderDetailDTO.setOrderStatusDesc("待出票");
			orderDetailDTO.setProcessResult("出票中");
			orderDetailDTO.setForecastMoney("");
		}
		BigDecimal moneyPaid = order.getMoneyPaid();
		orderDetailDTO.setMoneyPaid(moneyPaid != null ? moneyPaid.toString() : "");// 2018-05-13前端不变参数的情况下暂时使用原有参数,1.0.3更新为moneyPaid
		BigDecimal ticketAmount = order.getTicketAmount();
		orderDetailDTO.setTicketAmount(ticketAmount != null ? ticketAmount.toString() : "");
		BigDecimal surplus = order.getSurplus();
		orderDetailDTO.setSurplus(surplus != null ? surplus.toString() : "");
		BigDecimal userSurplusLimit = order.getUserSurplusLimit();
		orderDetailDTO.setUserSurplusLimit(userSurplusLimit != null ? userSurplusLimit.toString() : "");
		BigDecimal userSurplus = order.getUserSurplus();
		orderDetailDTO.setUserSurplus(userSurplus != null ? userSurplus.toString() : "");
		BigDecimal thirdPartyPaid = order.getThirdPartyPaid();
		orderDetailDTO.setThirdPartyPaid(thirdPartyPaid != null ? thirdPartyPaid.toString() : "");
		BigDecimal bonus = order.getBonus();
		orderDetailDTO.setBonus(bonus != null ? bonus.toString() : "");

		orderDetailDTO.setBetNum(order.getBetNum());
		orderDetailDTO.setPayName(order.getPayName());
		orderDetailDTO.setPassType(getPassType(order.getPassType()));
		orderDetailDTO.setCathectic(order.getCathectic());
		orderDetailDTO.setPlayType(order.getPlayType().replaceAll("0", ""));
		orderDetailDTO.setLotteryClassifyId(String.valueOf(lotteryClassifyId));
		orderDetailDTO.setLotteryPlayClassifyId(String.valueOf(lotteryPlayClassifyId));
		orderDetailDTO.setProgrammeSn(order.getOrderSn());
		orderDetailDTO.setCreateTime(DateUtil.getCurrentTimeString(order.getPayTime().longValue(), DateUtil.datetimeFormat));
		long acceptTime = order.getAcceptTime().longValue();
		if (acceptTime > 0) {
			orderDetailDTO.setAcceptTime(DateUtil.getCurrentTimeString(acceptTime, DateUtil.datetimeFormat));
		} else {
			orderDetailDTO.setAcceptTime("--");
		}
		long ticketTime = order.getTicketTime().longValue();
		if (ticketTime > 0) {
			orderDetailDTO.setTicketTime(DateUtil.getCurrentTimeString(ticketTime, DateUtil.datetimeFormat));
		} else {
			orderDetailDTO.setTicketTime("--");
		}
		List<PlayTypeName> playTypes = orderDetailMapper.getPlayTypes(lotteryClassifyId);
		Map<Integer, String> playTypeNameMap = new HashMap<Integer, String>();
		if (!Collections.isEmpty(playTypes)) {
			for (PlayTypeName type : playTypes) {
				playTypeNameMap.put(type.getPlayType(), type.getPlayName());
			}
		}
		orderDetailDTO.setDetailType(0);
		boolean isWorlCup = false;
		if (lotteryClassifyId == 1 && lotteryPlayClassifyId == 8) {
			isWorlCup = true;
		}
		String redirectUrl = "";
		LotteryPlayClassifyTemp map = orderDetailMapper.lotteryPlayClassifyStatusAndUrl(lotteryClassifyId, lotteryPlayClassifyId);
		log.info("dddd  " + JSONHelper.bean2json(map));
		if (map != null) {
			int status = map.getStatus();
			if (status == 0) {
				redirectUrl = map.getRedirectUrl();
			}
		}
		orderDetailDTO.setRedirectUrl(redirectUrl);
		List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(Integer.valueOf(param.getOrderId()), userId);
		if (CollectionUtils.isNotEmpty(orderDetails)) {
			List<MatchInfo> matchInfos = new ArrayList<MatchInfo>();
			for (OrderDetail orderDetail : orderDetails) {
				MatchInfo matchInfo = new MatchInfo();
				matchInfo.setChangci(orderDetail.getChangci());
				String match = orderDetail.getMatchTeam();
				/*
				 * String[] matchs = match.split("VS"); match = matchs[0] + "VS"
				 * + matchs[1];
				 */
				match = match.replaceAll("\r\n", "");
				matchInfo.setMatch(match);
				Integer isDan = orderDetail.getIsDan();
				String isDanStr = isDan == null ? "0" : isDan.toString();
				matchInfo.setIsDan(isDanStr);
				String playType = orderDetail.getPlayType();
				String playName = playTypeNameMap.getOrDefault(Integer.valueOf(playType), playType);
				String fixedodds = orderDetail.getFixedodds();
				if (Integer.valueOf(playType).equals(MatchPlayTypeEnum.PLAY_TYPE_HHAD.getcode())) {
					playName = StringUtils.isBlank(fixedodds) ? playName : ("[" + fixedodds + "]" + playName);
				}
				matchInfo.setPlayType(playName);
				String matchResult = orderDetail.getMatchResult();
				if (ProjectConstant.ORDER_MATCH_RESULT_CANCEL.equals(matchResult)) {

				}
				if (isWorlCup) {
					matchInfo.setCathecticResults(this.getWorldCupCathecticResults(orderDetail));
				} else {
					matchInfo.setCathecticResults(this.getCathecticResults(fixedodds, orderDetail.getTicketData(), matchResult, playTypeNameMap));
				}
				matchInfos.add(matchInfo);
			}
			if (isWorlCup) {
				String changci = orderDetails.get(0).getChangci();
				int detailType = 1;
				if ("T57".equals(changci)) {
					detailType = 2;
				}
				orderDetailDTO.setDetailType(detailType);
			}
			orderDetailDTO.setMatchInfos(matchInfos);
		}
		orderDetailDTO.setOrderSn(order.getOrderSn());
		return orderDetailDTO;
	}

	/**
	 * 世界杯
	 * 
	 * @param orderDetail
	 * @return
	 */
	private List<CathecticResult> getWorldCupCathecticResults(OrderDetail orderDetail) {
		List<CathecticResult> rsts = new ArrayList<CathecticResult>(1);
		CathecticResult ccRst = new CathecticResult();
		String ticketData = orderDetail.getTicketData();
		String matchTeam = orderDetail.getMatchTeam();
		String changci = orderDetail.getChangci();
		String playType = "冠军";
		if ("T57".equals(changci)) {
			playType = "冠亚军";
		}
		String[] split = ticketData.split("@");
		String isGuess = "0";
		String matchResult = orderDetail.getMatchResult();
		if (StringUtils.isBlank(matchResult)) {
			matchResult = "待定";
			isGuess = "";
		} else if (split[0].equals(matchResult)) {
			isGuess = "1";
		}
		ccRst.setPlayType(playType);
		ccRst.setMatchResult(matchResult);
		List<Cathectic> cathectics = new ArrayList<Cathectic>(1);
		Cathectic cathectic = new Cathectic();
		// String cat = matchTeam + "[" + split[1] + "]";
		cathectic.setCathectic(String.format("%.2f", Double.valueOf(split[1])));
		cathectic.setIsGuess(isGuess);
		cathectics.add(cathectic);
		ccRst.setCathectics(cathectics);
		rsts.add(ccRst);
		return rsts;
	}

	/**
	 * 组装投注、赛果列数据
	 * 
	 * @param ticketData
	 * @param matchResult
	 * @return
	 */
	private List<CathecticResult> getCathecticResults(String fixedodds, String ticketData, String matchResult, Map<Integer, String> types) {
		List<CathecticResult> cathecticResults = new LinkedList<CathecticResult>();
		if (StringUtils.isEmpty(ticketData))
			return cathecticResults;
		List<String> ticketDatas = Arrays.asList(ticketData.split(";"));
		List<String> matchResults = null;
		if (StringUtils.isNotEmpty(matchResult) && !ProjectConstant.ORDER_MATCH_RESULT_CANCEL.equals(matchResult)) {
			matchResults = Arrays.asList(matchResult.split(";"));
		}
		if (CollectionUtils.isNotEmpty(ticketDatas)) {
			for (String temp : ticketDatas) {
				CathecticResult cathecticResult = new CathecticResult();
				List<Cathectic> cathectics = new LinkedList<Cathectic>();
				String matchResultStr = "";
				String playType = temp.substring(0, temp.indexOf("|"));
				String playCode = temp.substring(temp.indexOf("|") + 1, temp.lastIndexOf("|"));
				String betCells = temp.substring(temp.lastIndexOf("|") + 1);
				String[] betCellArr = betCells.split(",");
				for (int i = 0; i < betCellArr.length; i++) {
					Cathectic cathectic = new Cathectic();
					String betCellCode = betCellArr[i].substring(0, betCellArr[i].indexOf("@"));
					String betCellOdds = betCellArr[i].substring(betCellArr[i].indexOf("@") + 1);
					String cathecticStr = getCathecticData(playType, betCellCode);
					cathectic.setCathectic(cathecticStr + "[" + String.format("%.2f", Double.valueOf(betCellOdds)) + "]");
					if (null != matchResults) {
						for (String matchStr : matchResults) {
							String rstPlayType = matchStr.substring(0, matchStr.indexOf("|"));
							String rstPlayCode = matchStr.substring(matchStr.indexOf("|") + 1, matchStr.lastIndexOf("|"));
							String rstPlayCells = matchStr.substring(matchStr.lastIndexOf("|") + 1);
							if (playType.equals(rstPlayType) && playCode.equals(rstPlayCode)) {
								if (rstPlayCells.equals(betCellCode)) {
									cathectic.setIsGuess("1");
								} else {
									cathectic.setIsGuess("0");
								}
								matchResultStr = getCathecticData(playType, rstPlayCells);
							}
						}
					} else {
						cathectic.setIsGuess("0");
					}
					cathectics.add(cathectic);
				}
				if (StringUtils.isBlank(matchResultStr)) {
					matchResultStr = "待定";
				}
				if (ProjectConstant.ORDER_MATCH_RESULT_CANCEL.equals(matchResult)) {
					matchResultStr = "#?";
				}
				String playName = types.getOrDefault(Integer.valueOf(playType), playType);
				if (Integer.valueOf(playType).equals(MatchPlayTypeEnum.PLAY_TYPE_HHAD.getcode())) {
					playName = StringUtils.isBlank(fixedodds) ? playName : ("[" + fixedodds + "]" + playName);
				}
				cathecticResult.setPlayType(playName);
				cathecticResult.setCathectics(cathectics);
				cathecticResult.setMatchResult(matchResultStr);
				cathecticResults.add(cathecticResult);
			}
		}
		return cathecticResults;
	}

	/**
	 * 通过玩法code与投注内容，进行转换
	 * 
	 * @param playCode
	 * @param cathecticStr
	 * @return
	 */
	private String getCathecticData(String playTypeStr, String cathecticStr) {
		int playType = Integer.parseInt(playTypeStr);
		String cathecticData = "";
		if (MatchPlayTypeEnum.PLAY_TYPE_HHAD.getcode() == playType || MatchPlayTypeEnum.PLAY_TYPE_HAD.getcode() == playType) {
			cathecticData = MatchResultHadEnum.getName(Integer.valueOf(cathecticStr));
		} else if (MatchPlayTypeEnum.PLAY_TYPE_CRS.getcode() == playType) {
			cathecticData = MatchResultCrsEnum.getName(cathecticStr);
		} else if (MatchPlayTypeEnum.PLAY_TYPE_TTG.getcode() == playType) {
			cathecticData = cathecticStr;
			if ("7".equals(cathecticStr)) {
				cathecticData = cathecticStr + "+";
			}
		} else if (MatchPlayTypeEnum.PLAY_TYPE_HAFU.getcode() == playType) {
			cathecticData = MatchResultHafuEnum.getName(cathecticStr);
		}
		return cathecticData;
	}

	/**
	 * 查询出票方案
	 * 
	 * @param param
	 * @return
	 */
	public TicketSchemeDTO getTicketScheme(TicketSchemeParam param) {
		TicketSchemeDTO ticketSchemeDTO = new TicketSchemeDTO();
		ticketSchemeDTO.setProgrammeSn(param.getProgrammeSn());
		Order orderInfoByOrderSn = orderMapper.getOrderInfoByOrderSn(param.getOrderSn());
		Integer orderStatus = orderInfoByOrderSn.getOrderStatus();
		Integer times = orderInfoByOrderSn.getCathectic();
		if (orderStatus < 8 && orderStatus > 2) {
			List<TicketSchemeDetailDTO> ticketSchemeDetailDTOs = new ArrayList<TicketSchemeDetailDTO>();
			Integer lotteryClassifyId = orderInfoByOrderSn.getLotteryClassifyId();
			Integer lotteryPlayClassifyId = orderInfoByOrderSn.getLotteryPlayClassifyId();
			if (8 == lotteryPlayClassifyId && 1 == lotteryClassifyId) {
				List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(orderInfoByOrderSn.getOrderId(), orderInfoByOrderSn.getUserId());
				int i = 0;
				for (OrderDetail detail : orderDetails) {
					TicketSchemeDetailDTO ticketSchemeDetailDTO = new TicketSchemeDetailDTO();
					ticketSchemeDetailDTO.setNumber(String.valueOf(++i));
					String playType = "冠军";
					if ("T57".equals(detail.getChangci())) {
						playType = "冠亚军";
					}
					String ticketContent = playType + "(" + detail.getMatchTeam() + "[" + detail.getTicketData().split("@")[1] + "])";
					ticketSchemeDetailDTO.setTickeContent(ticketContent);
					ticketSchemeDetailDTO.setPassType("");
					ticketSchemeDetailDTO.setMultiple(times.toString());
					ticketSchemeDetailDTO.setStatus(1);
					ticketSchemeDetailDTOs.add(ticketSchemeDetailDTO);
				}
			} else {
				GetBetInfoByOrderSn getBetInfoByOrderSn = new GetBetInfoByOrderSn();
				getBetInfoByOrderSn.setOrderSn(param.getOrderSn());
				BaseResult<DLZQBetInfoDTO> result = lotteryMatchService.getBetInfoByOrderSn(getBetInfoByOrderSn);
				if (result.getCode() == 0) {
					DLZQBetInfoDTO dLZQBetInfoDTO = result.getData();
					if (null != dLZQBetInfoDTO) {
						List<DLZQOrderLotteryBetInfoDTO> orderLotteryBetInfos = dLZQBetInfoDTO.getBetCells();
						if (CollectionUtils.isNotEmpty(orderLotteryBetInfos)) {
							orderLotteryBetInfos.forEach(betInfo -> {
								Integer status = betInfo.getStatus();
								List<DLBetMatchCellDTO> dLBetMatchCellDTOs = betInfo.getBetCells();
								if (CollectionUtils.isNotEmpty(orderLotteryBetInfos)) {
									for (int i = 0; i < dLBetMatchCellDTOs.size(); i++) {
										DLBetMatchCellDTO dLBetMatchCellDTO = dLBetMatchCellDTOs.get(i);
										TicketSchemeDetailDTO ticketSchemeDetailDTO = new TicketSchemeDetailDTO();
										ticketSchemeDetailDTO.setNumber(String.valueOf(i + 1));
										ticketSchemeDetailDTO.setTickeContent(dLBetMatchCellDTO.getBetContent());
										ticketSchemeDetailDTO.setPassType(getPassType(dLBetMatchCellDTO.getBetType()));
										ticketSchemeDetailDTO.setMultiple(String.valueOf(dLBetMatchCellDTO.getTimes()));
										ticketSchemeDetailDTO.setStatus(status);
										ticketSchemeDetailDTOs.add(ticketSchemeDetailDTO);
									}
								}
							});
						}
					}
				}
			}
			ticketSchemeDTO.setTicketSchemeDetailDTOs(ticketSchemeDetailDTOs);
		} else if (orderStatus.equals(1)) {
			List<TicketSchemeDetailDTO> ticketSchemeDetailDTOs = new ArrayList<TicketSchemeDetailDTO>(1);
			TicketSchemeDetailDTO dto = new TicketSchemeDetailDTO();
			dto.setNumber("1");
			dto.setTickeContent("-");
			dto.setPassType("-");
			dto.setMultiple("-");
			dto.setStatus(0);
			ticketSchemeDetailDTOs.add(dto);
			ticketSchemeDTO.setTicketSchemeDetailDTOs(ticketSchemeDetailDTOs);
		} else {
			List<TicketSchemeDetailDTO> ticketSchemeDetailDTOs = new ArrayList<TicketSchemeDetailDTO>(1);
			TicketSchemeDetailDTO dto = new TicketSchemeDetailDTO();
			dto.setNumber("1");
			dto.setTickeContent("-");
			dto.setPassType("-");
			dto.setMultiple("-");
			dto.setStatus(2);
			ticketSchemeDetailDTOs.add(dto);
			ticketSchemeDTO.setTicketSchemeDetailDTOs(ticketSchemeDetailDTOs);
		}
		return ticketSchemeDTO;
	}

	/**
	 * 组装通过方式字符串
	 * 
	 * @param passType
	 * @return
	 */
	private String getPassType(String passType) {
		String[] passTypes = passType.split(",");
		String passTypeStr = "";
		for (int i = 0; i < passTypes.length; i++) {
			passTypeStr += MatchBetTypeEnum.getName(passTypes[i]) + ",";
		}
		return passTypeStr.substring(0, passTypeStr.length() - 1);
	}

	/**
	 * 更新订单状态
	 * 
	 * @param param
	 * @return
	 */
	@Transactional
	public BaseResult<String> updateOrderInfoStatus(UpdateOrderInfoParam param) {
		log.info("-----------%%%--------更新订单状态:" + JSON.toJSONString(param));
		Order order = new Order();
		order.setOrderSn(param.getOrderSn());
		order.setOrderStatus(param.getOrderStatus());
		order.setPayStatus(param.getPayStatus());
		order.setPayTime(param.getPayTime());
		int rst = orderMapper.updateOrderStatus(order);
		log.info("-----------%%%%-----------更新订单状态结果:" + rst);
		return ResultGenerator.genSuccessResult("订单支付数据更新成功");
	}

	/**
	 * 支付成功修改订单信息
	 * 
	 * @param param
	 * @return
	 */
	@Transactional
	public BaseResult<String> updateOrderInfo(UpdateOrderInfoParam param) {
		try {
			if (StringUtils.isNotEmpty(param.getOrderSn())) {
				Order order = new Order();
				order.setOrderSn(param.getOrderSn());
				order = orderMapper.selectOne(order);
				if (param.getPayStatus() != null && param.getPayStatus().equals(0) && order.getPayStatus().equals(1)) {
					param.setPayStatus(null);
				}
				orderLogMapper.insertSelective(createOrderLog(order, SessionUtil.getUserId(), OrderLogContentService.PAY_STATE_TYPE, false, false));
				if (ProjectConstant.ORDER_STATUS_NOT_PAY.equals(param.getOrderStatus()) || ProjectConstant.ORDER_STATUS_FAIL_LOTTERY.equals(param.getOrderStatus())) {
					Integer userBonusId = order.getUserBonusId();
					if (null != userBonusId && userBonusId != 0) {
						UserBonusParam userBonusParam = new UserBonusParam();
						userBonusParam.setOrderSn(param.getOrderSn());
						userBonusParam.setUserBonusId(userBonusId);
						rollbackUserAccount(userBonusParam, order);
					}
					if (ProjectConstant.ORDER_STATUS_FAIL_LOTTERY.equals(param.getOrderStatus())) {
						orderMapper.updateOrderInfo(param);
					}
				} else {
					orderMapper.updateOrderInfo(param);
				}
			}
		} catch (Exception e) {
			log.error("订单编码：" + param.getOrderSn() + "支付成功，更新数据异常，error：" + e.getMessage());
			e.printStackTrace();
			throw new ServiceException(OrderExceptionEnum.SUBMIT_ERROR.getCode(), OrderExceptionEnum.SUBMIT_ERROR.getMsg());
		}
		return ResultGenerator.genSuccessResult("订单支付数据更新成功");
	}

	/**
	 * 根据订单编号查询订单及订单详情
	 * 
	 * @param param
	 * @return
	 */
	public OrderInfoAndDetailDTO getOrderWithDetailByOrderSn(OrderSnParam param) {
		Order order = orderMapper.getOrderInfoByOrderSn(param.getOrderSn());
		List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(order.getOrderId(), order.getUserId());
		OrderInfoAndDetailDTO orderInfoAndDetailDTO = new OrderInfoAndDetailDTO();
		OrderInfoDTO orderInfoDTO = new OrderInfoDTO();
		orderInfoDTO.setCathectic(order.getCathectic());
		orderInfoDTO.setLotteryClassifyId(order.getLotteryClassifyId());
		orderInfoDTO.setLotteryPlayClassifyId(order.getLotteryPlayClassifyId());
		orderInfoDTO.setPassType(order.getPassType());
		orderInfoDTO.setPlayType(order.getPlayType());
		orderInfoAndDetailDTO.setOrderInfoDTO(orderInfoDTO);
		List<OrderDetailDataDTO> orderDetailDataDTOs = new LinkedList<OrderDetailDataDTO>();
		if (CollectionUtils.isNotEmpty(orderDetails)) {
			for (OrderDetail orderDetail : orderDetails) {
				OrderDetailDataDTO orderDetailDataDTO = new OrderDetailDataDTO();
				orderDetailDataDTO.setChangci(orderDetail.getChangci());
				orderDetailDataDTO.setIsDan(orderDetail.getIsDan());
				orderDetailDataDTO.setLotteryClassifyId(orderDetail.getLotteryClassifyId());
				orderDetailDataDTO.setLotteryPlayClassifyId(orderDetail.getLotteryPlayClassifyId());
				orderDetailDataDTO.setMatchId(orderDetail.getMatchId());
				orderDetailDataDTO.setMatchTeam(orderDetail.getMatchTeam());
				orderDetailDataDTO.setMatchTime(orderDetail.getMatchTime());
				orderDetailDataDTO.setTicketData(orderDetail.getTicketData());
				orderDetailDataDTO.setIssue(orderDetail.getIssue());
				orderDetailDataDTOs.add(orderDetailDataDTO);
			}
		}
		orderInfoAndDetailDTO.setOrderDetailDataDTOs(orderDetailDataDTOs);
		return orderInfoAndDetailDTO;
	}

	public List<String> orderSnListGoPrintLottery() {
		List<Order> orders = orderMapper.ordersListGoPrintLottery();
		List<String> orderSns = new ArrayList<String>(0);
		if (CollectionUtils.isNotEmpty(orders)) {
			int sum = 0;
			orderSns = new ArrayList<String>(orders.size());
			for (Order order : orders) {
				/*
				 * Integer ticketNum = order.getTicketNum(); sum+=ticketNum;
				 * if(sum > 50) { break; }
				 */
				orderSns.add(order.getOrderSn());
			}
		}
		return orderSns;
	}

	public void updateOrderMatchResult() {
		List<OrderDetail> orderDetails = orderDetailMapper.unMatchResultOrderDetails();
		if (CollectionUtils.isEmpty(orderDetails)) {
			return;
		} 
		Set<String> playCodesSet = orderDetails.stream().map(detail -> detail.getIssue()).collect(Collectors.toSet());
		List<String> playCodes = new ArrayList<String>(playCodesSet.size());
		playCodes.addAll(playCodesSet);
		log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size());
		GetCancelMatchesParam getCancelMatchesParam = new GetCancelMatchesParam();
		getCancelMatchesParam.setPlayCodes(playCodes);
		List<String> cancelMatches = lotteryMatchService.getCancelMatches(playCodes);
		List<DlLeagueMatchResult> matchResults = dleagueMatchResultService.queryMatchResultsByPlayCodes(playCodes);
		if (CollectionUtils.isEmpty(matchResults) && CollectionUtils.isEmpty(cancelMatches)) {
			log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size() + " 没有获取到相应的赛事结果信息及没有取消赛事");
			return;
		}
		log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size() + " 获取到相应的赛事结果信息数：" + matchResult.size() + " 取消赛事数：" + cancelMatches.size());
		Map<String, List<OrderDetail>> detailMap = new HashMap<String, List<OrderDetail>>();
		List<OrderDetail> cancelList = new ArrayList<OrderDetail>(orderDetails.size());
		for (OrderDetail orderDetail : orderDetails) {
			String playCode = orderDetail.getIssue();
			if (cancelMatches.contains(playCode)) {
				orderDetail.setMatchResult(ProjectConstant.ORDER_MATCH_RESULT_CANCEL);
				cancelList.add(orderDetail);
			} else {
				List<OrderDetail> list = detailMap.get(playCode);
				if (list == null) {
					list = new ArrayList<OrderDetail>();
					detailMap.put(playCode, list);
				}
				list.add(orderDetail);
			}
		}
		log.info("取消赛事对应订单详情数：cancelList。si'ze" + cancelList.size() + "  detailMap.size=" + detailMap.size());
		Map<String, List<MatchResultDTO>> resultMap = new HashMap<String, List<MatchResultDTO>>();
		if (CollectionUtils.isNotEmpty(matchResults)) {
			for (MatchResultDTO dto : matchResults) {
				String playCode = dto.getPlayCode();
				List<MatchResultDTO> list = resultMap.get(playCode);
				if (list == null) {
					list = new ArrayList<MatchResultDTO>(5);
					resultMap.put(playCode, list);
				}
				list.add(dto);
			}
		}
		log.info("resultMap size=" + resultMap.size());
		List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>(orderDetails.size());
		for (String playCode : resultMap.keySet()) {
			List<MatchResultDTO> resultDTOs = resultMap.get(playCode);
			List<OrderDetail> details = detailMap.get(playCode);
			for (OrderDetail orderDetail : details) {
				String ticketDataStr = orderDetail.getTicketData();
				String[] split = ticketDataStr.split(";");
				OrderDetail od = new OrderDetail();
				od.setOrderDetailId(orderDetail.getOrderDetailId());
				StringBuffer sbuf = new StringBuffer();
				for (String ticketData : split) {
					if (StringUtils.isBlank(ticketData) || !ticketData.contains("|")) {
						continue;
					}
					Integer playType = Integer.valueOf(ticketData.substring(0, ticketData.indexOf("|")));
					if (playType.equals(MatchPlayTypeEnum.PLAY_TYPE_TSO.getcode())) {
						String hhadRst = null;
						String hadRst = null;
						for (MatchResultDTO dto : resultDTOs) {
							if (MatchPlayTypeEnum.PLAY_TYPE_HHAD.getcode() == dto.getPlayType().intValue()) {
								String cellCode = dto.getCellCode();
								if (cellCode.equals("3")) {
									hhadRst = "32";
								} else if (cellCode.equals("0")) {
									hhadRst = "33";
								} else if (cellCode.equals("0")) {
									hhadRst = "32,33";
								}
							} else if (MatchPlayTypeEnum.PLAY_TYPE_HAD.getcode() == dto.getPlayType().intValue()) {
								String cellCode = dto.getCellCode();
								if (cellCode.equals("3")) {
									hadRst = "31";
								} else if (cellCode.equals("0")) {
									hadRst = "30";
								}
							}
						}
						String cellCode = hhadRst;
						if (hadRst != null) {
							if (cellCode == null) {
								cellCode = hadRst;
							} else if (cellCode != null) {
								cellCode = cellCode + "," + hadRst;
							}
						}
						if (cellCode != null) {
							sbuf.append("07|").append(playCode).append("|").append(cellCode).append(";");
						}
					} else {
						for (MatchResultDTO dto : resultDTOs) {
							if (playType.equals(dto.getPlayType())) {
								sbuf.append("0").append(dto.getPlayType()).append("|").append(playCode).append("|").append(dto.getCellCode()).append(";");
							}
						}
					}
				}
				if (sbuf.length() > 0) {
					od.setMatchResult(sbuf.substring(0, sbuf.length() - 1));
					orderDetailList.add(od);
				}
			}
		}
		log.info("updateOrderMatchResult 准备去执行数据库更新操作：size=" + orderDetailList.size());
		updateOrderDetailByReward(orderDetailList);
		log.info("updateOrderMatchResult 准备去执行数据库更新取消赛事结果操作：size=" + cancelList.size());
		updateOrderDetailByReward(cancelList);
	}

	/**
	 * 根据订单状态查询订单号集合
	 * 
	 * @param param
	 * @return
	 */
	public BaseResult<List<String>> queryOrderSnListByStatus(OrderQueryParam param) {
		Order order = new Order();
		order.setOrderStatus(param.getOrderStatus());
		order.setPayStatus(param.getPayStatus());
		order.setIsDelete(0);
		List<String> orderSnList = orderMapper.queryOrderSnListByStatus(order);
		return ResultGenerator.genSuccessResult("success", orderSnList);
	}

	public BaseResult<String> updateOrderStatusByCondition(UpdateOrderInfoParam updateOrderInfoParam) {
		int rst = orderMapper.updateOrderInfo(updateOrderInfoParam);
		return ResultGenerator.genSuccessResult("success");
	}

	/**
	 * 查询未支付的超过15min的订单
	 * 
	 * @param orderCondtionParam
	 * @return
	 */
	public BaseResult<List<OrderDTO>> queryOrderByCondition(OrderCondtionParam orderCondtionParam) {
		List<Order> orderList = orderMapper.queryOrderListBySelective(DateUtil.getCurrentTimeLong());
		List<OrderDTO> orderDTOList = new ArrayList<>();
		orderList.stream().forEach(s -> {
			OrderDTO orderDTO = new OrderDTO();
			try {
				BeanUtils.copyProperties(orderDTO, s);
			} catch (Exception e) {
				e.printStackTrace();
			}
			orderDTOList.add(orderDTO);
		});
		return ResultGenerator.genSuccessResult("success", orderDTOList);
	}

	/**
	 * 更新订单出票状态
	 * 
	 * @param param
	 * @return
	 */
	public void refreshOrderPrintStatus() {
		List<Order> orders = orderMapper.ordersListGoPrintLottery();
		if (CollectionUtils.isNotEmpty(orders)) {
			log.info("refreshOrderPrintStatus需要获取出票状态的订单数：" + orders.size());
			int n = 0;
			List<Order> rollOrders = new ArrayList<Order>(orders.size());
			for (Order order : orders) {
				Integer orderStatus = lotteryPrintService.printLotteryStatusByOrderSn(order.getOrderSn());
				if (2 == orderStatus || 1 == orderStatus) {
					UpdateOrderInfoParam param1 = new UpdateOrderInfoParam();
					param1.setOrderSn(order.getOrderSn());
					param1.setOrderStatus(orderStatus);
					int rst = orderMapper.updateOrderInfo(param1);
					n += rst;
					if (2 == orderStatus) {
						RollbackOrderAmountParam param2 = new RollbackOrderAmountParam();
						param2.setOrderSn(order.getOrderSn());
						log.info("invoke rollbackOrderAmount 准备回流资金ordersn=" + order.getOrderSn());
						BaseResult rollbackOrderAmount = paymentService.rollbackOrderAmount(param2);
						log.info("invoke rollbackOrderAmount 准备回流资金ordersn=" + order.getOrderSn() + " result:code=" + rollbackOrderAmount.getCode() + " msg=" + rollbackOrderAmount.getMsg());
						rollOrders.add(order);
					}
				}
			}
			log.info("refreshOrderPrintStatus实际更新状态的订单数：" + n);
			this.goLotteryMessage(rollOrders);
		}
	}

	@Async
	private void goLotteryMessage(List<Order> orders) {
		AddMessageParam addParam = new AddMessageParam();
		List<MessageAddParam> params = new ArrayList<MessageAddParam>(orders.size());
		for (Order order : orders) {
			if (2 != order.getOrderStatus()) {
				continue;
			}
			// 消息
			MessageAddParam messageAddParam = new MessageAddParam();
			messageAddParam.setTitle(CommonConstants.FORMAT_PRINTLOTTERY_TITLE);
			messageAddParam.setContent(CommonConstants.FORMAT_PRINTLOTTERY_CONTENT);
			messageAddParam.setContentDesc(CommonConstants.FORMAT_PRINTLOTTERY_CONTENT_DESC);
			messageAddParam.setSender(-1);
			messageAddParam.setMsgType(0);
			messageAddParam.setReceiver(order.getUserId());
			messageAddParam.setReceiveMobile("");
			messageAddParam.setObjectType(3);
			messageAddParam.setMsgUrl("");
			messageAddParam.setSendTime(DateUtil.getCurrentTimeLong());
			String ticketAmount = order.getTicketAmount().toString();
			Integer addTime = order.getAddTime();
			LocalDateTime loclaTime = LocalDateTime.ofEpochSecond(addTime, 0, ZoneOffset.UTC);
			String format = loclaTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:dd"));
			messageAddParam.setMsgDesc(MessageFormat.format(CommonConstants.FORMAT_PRINTLOTTERY_MSG_DESC, ticketAmount, format));
			params.add(messageAddParam);
		}
		addParam.setParams(params);
		userMessageService.add(addParam);
	}

	/**
	 * 已中奖的用户订单，即订单状态是5-已中奖，派奖中（订单状态是6）的不自动加奖金， 更新用户账户，记录奖金流水
	 * 
	 * @param issue
	 */
	public void addRewardMoneyToUsers() {
		List<OrderWithUserDTO> orderWithUserDTOs = orderMapper.selectOpenedAllRewardOrderList();
		log.info("派奖已中奖的用户数据：code=" + orderWithUserDTOs.size());
		if (CollectionUtils.isNotEmpty(orderWithUserDTOs)) {
			log.info("需要派奖的数据:" + orderWithUserDTOs.size());
			UserIdAndRewardListParam userIdAndRewardListParam = new UserIdAndRewardListParam();
			List<UserIdAndRewardDTO> userIdAndRewardDTOs = new LinkedList<UserIdAndRewardDTO>();
			for (OrderWithUserDTO orderWithUserDTO : orderWithUserDTOs) {
				UserIdAndRewardDTO userIdAndRewardDTO = new UserIdAndRewardDTO();
				userIdAndRewardDTO.setUserId(orderWithUserDTO.getUserId());
				userIdAndRewardDTO.setOrderSn(orderWithUserDTO.getOrderSn());
				userIdAndRewardDTO.setReward(orderWithUserDTO.getRealRewardMoney());
				int betTime = orderWithUserDTO.getBetTime();
				userIdAndRewardDTO.setBetMoney(orderWithUserDTO.getBetMoney());
				userIdAndRewardDTO.setBetTime(DateUtil.getTimeString(betTime, DateUtil.datetimeFormat));
				userIdAndRewardDTOs.add(userIdAndRewardDTO);
			}
			userIdAndRewardListParam.setUserIdAndRewardList(userIdAndRewardDTOs);
			BaseResult<String> changeUserAccountByType = userAccountService.changeUserAccountByType(userIdAndRewardListParam);
			log.info("派奖changeUserAccountByType参数数据：" + userIdAndRewardDTOs.size() + " 返回的数据:code=" + changeUserAccountByType.getCode() + " msg=" + changeUserAccountByType.getMsg());
		}
	}
}
