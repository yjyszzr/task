package com.dl.task.service;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.dl.base.constant.CommonConstants;
import com.dl.base.enums.MatchPlayTypeEnum;
import com.dl.base.enums.MatchResultCrsEnum;
import com.dl.base.enums.MatchResultHadEnum;
import com.dl.base.enums.MatchResultHafuEnum;
import com.dl.base.enums.RespStatusEnum;
import com.dl.base.enums.SNBusinessCodeEnum;
import com.dl.base.exception.ServiceException;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.base.util.DateUtilNew;
import com.dl.base.util.SNGenerator;
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.dao.OrderDetailMapper;
import com.dl.task.dao.OrderMapper;
import com.dl.task.dao.UserAccountMapper;
import com.dl.task.dao.UserBonusMapper;
import com.dl.task.dao.UserMapper;
import com.dl.task.dao2.DlLeagueMatchResultMapper;
import com.dl.task.dao2.LotteryMatchMapper;
import com.dl.task.dto.CellInfo;
import com.dl.task.dto.OrderDTO;
import com.dl.task.dto.OrderWithUserDTO;
import com.dl.task.dto.TMatchBetMaxAndMinOddsList;
import com.dl.task.dto.TicketInfo;
import com.dl.task.dto.TicketPlayInfo;
import com.dl.task.dto.UserIdAndRewardDTO;
import com.dl.task.model.ChannelOperationLog;
import com.dl.task.model.DlChannelConsumer;
import com.dl.task.model.DlChannelDistributor;
import com.dl.task.model.DlLeagueMatchResult;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.Order;
import com.dl.task.model.OrderDetail;
import com.dl.task.model.User;
import com.dl.task.model.UserAccount;
import com.dl.task.model.UserBonus;
import com.dl.task.param.AddMessageParam;
import com.dl.task.param.MessageAddParam;
import com.dl.task.param.OrderSnParam;
import com.dl.task.param.UpdateOrderInfoParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService extends AbstractService<Order> {

	@Resource
	private OrderMapper orderMapper;

	@Resource
	private OrderDetailMapper orderDetailMapper;
	
	@Resource
	private	UserAccountService userAccountService;

	@Resource 
	private DlMessageService dlMessageService;
	@Resource
	private UserBonusMapper userBonusMapper;

	@Resource
	private UserAccountMapper userAccountMapper;
	@Resource
	private UserMapper userMapper;
	private DlPrintLotteryMapper dlPrintLotteryMapper;
	
	@Resource
	private LotteryMatchMapper lotteryMatchMapper;
	
	@Resource
	private DlLeagueMatchResultMapper dlLeagueMatchResultMapper;

	/**
	 * 更新订单状态
	 * 
	 * @param param
	 * @return
	 */
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
	
	//更新待出票订单状态及出票赔率信息
	public void refreshOrderPrintStatus() {
		List<Order> orders = orderMapper.ordersListNoFinishAllPrintLottery();
		if (CollectionUtils.isNotEmpty(orders)) {
			log.info("refreshOrderPrintStatus需要获取出票状态的订单数：" + orders.size());
			int n = 0;
			List<Order> succOrders = new ArrayList<Order>(orders.size());
			List<Order> rollOrders = new ArrayList<Order>(orders.size());
			for (Order order : orders) {
				Integer userId = order.getUserId();
				if(userId == null) {//?????有没有必要
					continue;
				}
				List<DlPrintLottery> printLotterys = dlPrintLotteryMapper.printLotterysByOrderSn(order.getOrderSn());
				if(CollectionUtils.isEmpty(printLotterys)) {//?????
					continue;
				}
				Set<Integer> lotteryStatus = printLotterys.stream().map(item->item.getStatus()).collect(Collectors.toSet());
				if(lotteryStatus.contains(0) || lotteryStatus.contains(3)) {//存在有未出票或出票中的
					continue;
				}
				//更新出票状态及订单状态，及回退金额到余额
				List<DlPrintLottery> succPrintLotterys = new ArrayList<DlPrintLottery>(printLotterys.size());
				BigDecimal refundMoney = BigDecimal.ZERO;
				for(DlPrintLottery item: printLotterys) {
					if(2 == item.getStatus()) {
						refundMoney = refundMoney.add(item.getMoney().divide(BigDecimal.valueOf(100)));
					} else if(1 == item.getStatus()){
						succPrintLotterys.add(item);
					}
				}
				if(lotteryStatus.contains(1)) {//存在出票成功的
					//更新出票信息到订单详情
					this.updateOrderInfoByPrint(succPrintLotterys, order);
					order.setPrintLotteryStatus(4);
					if(refundMoney.setScale(2,RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO.setScale(2,RoundingMode.HALF_EVEN))>0) {
						order.setPrintLotteryStatus(2);
					}
					order.setPrintLotteryRefundAmount(refundMoney);
					int rst = orderMapper.updateOrderStatus1To3(order);
					n += rst;
					succOrders.add(order);
				}else {//全为出票失败的
					order.setPrintLotteryStatus(3);
					order.setPrintLotteryRefundAmount(refundMoney);
					int rst = orderMapper.updateOrderStatus1To2(order);
					n += rst;
				}
				//帐户回流
				if(refundMoney.setScale(2,RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO.setScale(2,RoundingMode.HALF_EVEN))>0) {//退款
					BigDecimal bonusAmount = order.getBonus();
					Integer userBonusId = order.getUserBonusId();
					if(userBonusId > 0 && 
							refundMoney.setScale(2,RoundingMode.HALF_EVEN).compareTo(bonusAmount.setScale(2,RoundingMode.HALF_EVEN)) > 0) {
						refundMoney = refundMoney.subtract(bonusAmount);
						UserBonus updateUserBonus = new UserBonus();
						updateUserBonus.setUserId(userId);
						updateUserBonus.setUserBonusId(userBonusId);
						updateUserBonus.setUsedTime(DateUtil.getCurrentTimeLong());
						int i = userBonusMapper.updateBonusUnuseByUserBonusId(updateUserBonus);
						log.info("出票失败退回优惠券，userid={},user_bonus_id={}",userId,userBonusId);
					}
					if(refundMoney.setScale(2,RoundingMode.HALF_EVEN).compareTo(bonusAmount.setScale(2,RoundingMode.HALF_EVEN)) <= 0) {
						continue;
					}
					//账户流水查看
					UserAccount userAccountRoll = new UserAccount();
					userAccountRoll.setUserId(userId);
//					userAccountRoll.setThirdPartPaid(refundMoney);
					userAccountRoll.setOrderSn(order.getOrderSn());
					userAccountRoll.setProcessType(ProjectConstant.ACCOUNT_ROLLBACK);
					List<UserAccount> userAccountListRoll = userAccountMapper.queryUserAccountBySelective(userAccountRoll);
					if (!CollectionUtils.isEmpty(userAccountListRoll)) {
						log.info("[rollbackUserMoneyOrderFailure]" +order.getOrderSn()+ " 订单已经回滚，无法再次回滚");
						continue ;
					}
					User user = new User();
					//调整为不可提现余额
					user.setUserMoneyLimit(refundMoney);
					user.setUserId(userId);
					int cnt = userMapper.updateInDBUserMoneyLimit(user);
					log.info("[rollbackUserMoneyOrderFailure]" + " userId:" + userId + " amt:" + refundMoney +" result cnt:" + cnt);
					//===========记录退款流水====================
					UserAccount userAccountParamByType = new UserAccount();
					Integer accountType = ProjectConstant.ACCOUNT_ROLLBACK;
					log.info("===========更新用户流水表=======:" + accountType);
					userAccountParamByType.setProcessType(accountType);
					userAccountParamByType.setAmount(BigDecimal.ZERO.subtract(refundMoney));
					userAccountParamByType.setBonusPrice(BigDecimal.ZERO);//暂无红包金额
					userAccountParamByType.setOrderSn(order.getOrderSn());
					userAccountParamByType.setThirdPartPaid(refundMoney);
					userAccountParamByType.setUserId(userId);
					userAccountParamByType.setAddTime(DateUtil.getCurrentTimeLong());
					userAccountParamByType.setLastTime(DateUtil.getCurrentTimeLong());
					userAccountParamByType.setParentSn("");
					String accountSn = SNGenerator.nextSN(SNBusinessCodeEnum.ACCOUNT_SN.getCode());
					userAccountParamByType.setAccountSn(accountSn);
					userAccountParamByType.setPayId("0");
					userAccountParamByType.setPaymentName("");
					userAccountParamByType.setThirdPartName("");
					int count = userAccountMapper.insertUserAccount(userAccountParamByType);
					log.info("退款成功记录流水成功 cnt:" + count);
					rollOrders.add(order);
				}
			}
			//西安活动
			if(CollectionUtils.isNotEmpty(succOrders)) {
				this.updateChannelOperationLog(succOrders);
			}
			log.info("refreshOrderPrintStatus实际更新状态的订单数：" + n);
			this.goLotteryMessage(rollOrders);
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
					ChannelOperationLog channelOperationByOrderSn = orderMapper.getChannelOperationByOrderSn(orderFor.getOrderSn());
					if(channelOperationByOrderSn == null) {
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
						BigDecimal moneyPaid = orderFor.getMoneyPaid();///???????
						channelOperationLog.setOptionAmount(moneyPaid);
						channelOperationLog.setOptionTime(DateUtilNew.getCurrentTimeLong());
						channelOperationLog.setChannelId(distributor.getChannelId());
						channelOperationLog.setOrderSn(orderFor.getOrderSn());
						orderMapper.saveChannelOperation(channelOperationLog);
					}
				}
			}
		}
	}
	//更新出票赔率到订单详情
	private void updateOrderInfoByPrint(List<DlPrintLottery> succPrintLotterys, Order order) {
		if(CollectionUtils.isEmpty(succPrintLotterys)) {
			return ;
		}
		Map<String, Double> map = new HashMap<String, Double>();
		Integer acceptTime = 0;
		Integer ticketTime = 0;
		for(DlPrintLottery printLottery: succPrintLotterys) {
			String stakes = printLottery.getStakes();
			String printSp = printLottery.getPrintSp();
			this.getPrintOdds(map, stakes, printSp);
			Integer acceptTime1 = printLottery.getAcceptTime();
			acceptTime = acceptTime<acceptTime1?acceptTime1:acceptTime;
			Integer ticketTime1 = DateUtil.getCurrentTimeLong(printLottery.getPrintTime().getTime()/1000);
			ticketTime = ticketTime<ticketTime1?ticketTime1:ticketTime;
		}
		order.setAcceptTime(acceptTime);
		order.setTicketTime(ticketTime);
		// 更新订单详情表
		List<OrderDetail> orderDetailList = orderDetailMapper.queryListByOrderSn(order.getOrderSn());
		if (CollectionUtils.isEmpty(orderDetailList)) {
			return;
		}
		List<TicketInfo> ticketInfos = new ArrayList<TicketInfo>(orderDetailList.size());
		for (OrderDetail od : orderDetailList) {
			String playCode = od.getIssue();
			String ticketData = od.getTicketData();
			String[] split = ticketData.split(";");
			StringBuffer sbuf = new StringBuffer();
			for(int i=0; i< split.length; i++) {
				if(i != 0) {
					sbuf.append(";");
				}
				String str = split[i];
				int lastIndexOf = str.lastIndexOf("\\|");
				String preKey = str.substring(0,lastIndexOf+1);
				String oldBetItems = str.substring(lastIndexOf+1);
				String[] split2 = oldBetItems.split(",");
				sbuf.append(preKey);
				for(int j=0; j< split2.length; j++) {
					if(j != 0) {
						sbuf.append(",");
					}
					String oldBetItem = split2[j];
					String[] split3 = oldBetItem.split("@");
					String betItem = split3[0];
					String key = preKey + betItem;
					Double betOdds = map.get(key);
					String betOddsStr = betOdds == null?split3[1]:betOdds.toString();
					sbuf.append(betItem).append("@").append(betOddsStr);
				}
			}
			String nTicketData = sbuf.toString();
			od.setTicketData(nTicketData);
			TicketInfo ticketInfo = this.ticketPlayInfos(Arrays.asList(nTicketData.split(";")), od.getFixedodds());
			ticketInfos.add(ticketInfo);
		}
		//更新赔率到DB
		for (OrderDetail orderDetail : orderDetailList) {
			orderDetailMapper.updateTicketData(orderDetail);
		}
		// 计算预测奖金
		String forecastMoney = this.betMaxAndMinMoney(ticketInfos, order);
		order.setForecastMoney(forecastMoney);
	}
	//获取出票赔率
	private void getPrintOdds(Map<String, Double> map, String stakes, String spStr) {
		String[] stakesList = stakes.split(";");
		Map<String, String> codeTypeMap = new HashMap<String, String>();
		for(int i=0; i<stakesList.length; i++) {
			String stake = stakesList[i];
			String playType = stake.substring(0, stake.indexOf("|"));
			String playCode = stake.substring(stake.indexOf("|") + 1, stake.lastIndexOf("|"));
			codeTypeMap.put(playCode, playType);
		}
		String[] spArr = spStr.split(";");
		StringBuffer sbuf = new StringBuffer();
		for(String sp: spArr) {
			String[] split = sp.split("\\|");
			String playCode = split[0];
			String betInfos = split[1];
			String[] split2 = betInfos.split(",");
			String playType = codeTypeMap.get(playCode);
			String preKey = playType+"|"+playCode + "|";
			for(String str: split2) {
				String[] split3 = str.split("@");
				String betItem = split3[0];
				Double betOdds = Double.valueOf(split3[1]);
				String key = preKey + betItem;
				Double oldBetOdds = map.get(key);
				if(oldBetOdds == null || oldBetOdds > betOdds) {
					map.put(key, betOdds);
				}
			}
		}
	}

	@Async
	private void goLotteryMessage(List<Order> orders) {
		AddMessageParam addParam = new AddMessageParam();
		List<MessageAddParam> params = new ArrayList<MessageAddParam>(orders.size());
		for (Order order : orders) {
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
			LocalDateTime loclaTime = LocalDateTime.ofEpochSecond(addTime, 0, ZoneOffset.of("+08:00"));
			String format = loclaTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:dd"));
			messageAddParam.setMsgDesc(MessageFormat.format(CommonConstants.FORMAT_PRINTLOTTERY_MSG_DESC, ticketAmount, format));
			params.add(messageAddParam);
		}
		addParam.setParams(params);
		dlMessageService.add(addParam);
	}
	//更新订单的比赛结果
	public void updateOrderMatchResult() {
		List<OrderDetail> orderDetails = orderDetailMapper.unMatchResultOrderDetails();
		if (CollectionUtils.isEmpty(orderDetails)) {
			return;
		}
		Set<String> playCodesSet = orderDetails.stream().map(detail -> detail.getIssue()).collect(Collectors.toSet());
		List<String> playCodes = new ArrayList<String>(playCodesSet.size());
		playCodes.addAll(playCodesSet);
		log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size());
		List<String> cancelMatches = lotteryMatchMapper.getCancelMatches(playCodes);
		List<DlLeagueMatchResult> matchResults = dlLeagueMatchResultMapper.queryMatchResultsByPlayCodes(playCodes);
		if (CollectionUtils.isEmpty(matchResults) && CollectionUtils.isEmpty(cancelMatches)) {
			log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size() + " 没有获取到相应的赛事结果信息及没有取消赛事");
			return;
		}
		log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size() + " 获取到相应的赛事结果信息数：" + matchResults.size() + " 取消赛事数：" + cancelMatches.size());
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
		Map<String, List<DlLeagueMatchResult>> resultMap = new HashMap<String, List<DlLeagueMatchResult>>();
		if (CollectionUtils.isNotEmpty(matchResults)) {
			for (DlLeagueMatchResult dto : matchResults) {
				String playCode = dto.getPlayCode();
				List<DlLeagueMatchResult> list = resultMap.get(playCode);
				if (list == null) {
					list = new ArrayList<DlLeagueMatchResult>(5);
					resultMap.put(playCode, list);
				}
				list.add(dto);
			}
		}
		log.info("resultMap size=" + resultMap.size());
		List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>(orderDetails.size());
		for (String playCode : resultMap.keySet()) {
			List<DlLeagueMatchResult> resultDTOs = resultMap.get(playCode);
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
						for (DlLeagueMatchResult dto : resultDTOs) {
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
						for (DlLeagueMatchResult dto : resultDTOs) {
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
		for(OrderDetail detail: orderDetailList) {
			orderDetailMapper.updateMatchResult(detail);
		}
		log.info("updateOrderMatchResult 准备去执行数据库更新取消赛事结果操作：size=" + cancelList.size());
		for(OrderDetail detail: cancelList) {
			orderDetailMapper.updateMatchResult(detail);
		}
	}

	public void addRewardMoneyToUsers() {
		List<OrderWithUserDTO> orderWithUserDTOs = orderMapper.selectOpenedAllRewardOrderList();
		log.info("派奖已中奖的用户数据：code=" + orderWithUserDTOs.size());
		if (CollectionUtils.isNotEmpty(orderWithUserDTOs)) {
			log.info("需要派奖的数据:" + orderWithUserDTOs.size());
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
			userAccountService.batchUpdateUserAccount(userIdAndRewardDTOs,ProjectConstant.REWARD_AUTO);
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
}
