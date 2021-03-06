package com.dl.task.service;

import com.dl.base.constant.CommonConstants;
import com.dl.base.enums.*;
import com.dl.base.exception.ServiceException;
import com.dl.base.lotto.LottoMoneyUtil;
import com.dl.base.lotto.LottoUtils;
import com.dl.base.lotto.entity.LottoResultEntity;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.base.util.DateUtilNew;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.SNGenerator;
import com.dl.lottery.api.ISupperLottoService;
import com.dl.lottery.dto.DlSuperLottoRewardDTO;
import com.dl.lottery.param.SupperLottoParam;
import com.dl.store.api.IStoreUserMoneyService;
import com.dl.store.param.AwardParam;
import com.dl.task.core.ProjectConstant;
import com.dl.task.dao.*;
import com.dl.task.dao2.*;
import com.dl.task.dto.*;
import com.dl.task.model.*;
import com.dl.task.param.*;
import com.dl.task.printlottery.PrintLotteryAdapter;
import com.dl.task.util.GeTuiMessage;
import com.dl.task.util.GeTuiUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService extends AbstractService<Order> {

	@Resource
	private OrderMapper orderMapper;

	@Resource
	private OrderDetailMapper orderDetailMapper;
	
	@Resource
	private UserBonusMapper userBonusMapper;

	@Resource
	private UserAccountMapper userAccountMapper;
	
	@Resource
	private UserMapper userMapper;
	
	@Resource
	private DlPrintLotteryMapper dlPrintLotteryMapper;
	
	@Resource
	private LotteryMatchMapper lotteryMatchMapper;
	
	@Resource
	private DlPrintLotteryService dlPrintLotteryService;
	
	@Resource
	private DlSuperLottoMapper dlSuperLottoMapper;
	
	@Resource
	private DlLeagueMatchResultMapper dlLeagueMatchResultMapper;

	@Resource
	private	UserAccountService userAccountService;

	@Resource 
	private DlMessageService dlMessageService;
	
	@Resource
	private PayLogMapper payLogMapper;
	
    @Resource
    private UserMatchCollectMapper userMatchCollectMapper;
    @Resource
    private PrintLotteryAdapter printLotteryAdapter;
    
    @Resource
    private DlMatchBasketballMapper dlMatchBasketballMapper;
    
    @Resource
    private DlResultBasketballMapper dlResultBasketballMapper;
    
    @Resource
    private SysConfigService   sysConfigService;
    
    @Resource
    private IStoreUserMoneyService   storeUserMoneyService;
    
    @Resource
    private ISupperLottoService   supperLottoService;
    
    @Resource
    private GeTuiUtil geTuiUtil;
    
    /**
     * 处理订单超时订单
     */
    public void dealBeyondTimeOrderOut() {
    	log.info("开始执行超时订单任务");
    	SysConfigDTO sysConfigDTO = sysConfigService.querySysConfig(71);//圣和彩电订单超时时间
    	Integer orderExpireTime = sysConfigDTO.getValue().intValue();
		List<Order> orderList = orderMapper.queryOrderListByOrder20minOut(DateUtil.getCurrentTimeLong(),orderExpireTime);
    	
		log.info("超时订单数："+orderList.size());
    	if(orderList.size() == 0) {
    		log.info("没有超时订单,定时任务结束");
    		return;
    	}
    	List<String> orderSnList = orderList.stream().map(s->s.getOrderSn()).collect(Collectors.toList());
    	orderMapper.batchUpdateOrderStatus0To8(orderSnList);
		log.info("结束执行超时订单任务");
    }
    
    
	/**
	 * 更新订单状态
	 * 
	 * @param param
	 * @return
	 */
	public BaseResult<String> updateOrderInfoStatus(UpdateOrderInfoParam param) {
//		log.info("-----------%%%--------更新订单状态:" + JSON.toJSONString(param));
		Order order = new Order();
		order.setOrderSn(param.getOrderSn());
		order.setOrderStatus(param.getOrderStatus());
		order.setPayStatus(param.getPayStatus());
		order.setPayTime(param.getPayTime());
		int rst = orderMapper.updateOrderStatus(order);
//		log.info("-----------%%%%-----------更新订单状态结果:" + rst);
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
				if(userId == null) {
					continue;
				}
				List<DlPrintLottery> printLotterys = dlPrintLotteryMapper.printLotterysByOrderSn(order.getOrderSn());
				if(CollectionUtils.isEmpty(printLotterys)) {
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
							refundMoney.setScale(2,RoundingMode.HALF_EVEN).compareTo(bonusAmount.setScale(2,RoundingMode.HALF_EVEN)) >= 0) {
						refundMoney = refundMoney.subtract(bonusAmount);
						UserBonus updateUserBonus = new UserBonus();
						updateUserBonus.setUserId(userId);
						updateUserBonus.setUserBonusId(userBonusId);
						updateUserBonus.setUsedTime(DateUtil.getCurrentTimeLong());
						int i = userBonusMapper.updateBonusUnuseByUserBonusId(updateUserBonus);
						log.info("出票失败退回优惠券，userid={},user_bonus_id={}",userId,userBonusId);
					}
					if(refundMoney.compareTo(BigDecimal.ZERO)<=0){//刚好等于优惠券 的情况不需要流水变动
						continue;
					}
					//账户流水查看
					UserAccount userAccountRoll = new UserAccount();
					userAccountRoll.setUserId(userId);
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
//					log.info("[rollbackUserMoneyOrderFailure]" + " userId:" + userId + " amt:" + refundMoney +" result cnt:" + cnt);
					//===========记录退款流水====================
					UserAccount userAccountParamByType = new UserAccount();
					Integer accountType = ProjectConstant.ACCOUNT_ROLLBACK;
					log.info("===========更新用户流水表=======:" + accountType);
					User curUser = userMapper.queryUserByUserId(userId);
					BigDecimal curBalance = curUser.getUserMoney().add(curUser.getUserMoneyLimit());
					userAccountParamByType.setCurBalance(curBalance);
					userAccountParamByType.setProcessType(accountType);
					userAccountParamByType.setAmount(refundMoney);
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
					log.info("select order_sn={},channel option log .......",orderFor.getOrderSn());
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
						BigDecimal moneyPaid = orderFor.getMoneyPaid();
						channelOperationLog.setOptionAmount(moneyPaid);
						channelOperationLog.setOptionTime(DateUtilNew.getCurrentTimeLong());
						channelOperationLog.setChannelId(distributor.getChannelId());
						channelOperationLog.setOrderSn(orderFor.getOrderSn());
						orderMapper.saveChannelOperation(channelOperationLog);
						log.info("save order_sn={},channel option log .......",orderFor.getOrderSn());
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
			if("T51".equals(printLottery.getGame()) || "T52".equals(printLottery.getGame())){
				
				String stakes = printLottery.getStakes();
				String printSp = printLottery.getPrintSp();
				this.getPrintOdds(map, stakes, printSp);
			}
			Integer acceptTime1 = printLottery.getAcceptTime();
			acceptTime = acceptTime<acceptTime1?acceptTime1:acceptTime;
			Integer ticketTime1 = DateUtil.getCurrentTimeLong(printLottery.getPrintTime().getTime()/1000);
			ticketTime = ticketTime<ticketTime1?ticketTime1:ticketTime;
		}
		order.setAcceptTime(acceptTime);
		order.setTicketTime(ticketTime);
		if(Integer.valueOf(2).equals(order.getLotteryClassifyId())){
			return;
		}
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
		String forecastMoney = "";
		Integer lotteryClassifyId = order.getLotteryClassifyId();
		if( 3  == lotteryClassifyId) {//篮彩
			forecastMoney = this.betMaxAndMinMoneyForBasket(ticketInfos, order);
		}else {
			forecastMoney = this.betMaxAndMinMoney(ticketInfos, order);
		}		
		order.setForecastMoney(forecastMoney);
		
		//购买并成功出票后就收藏赛事
		for (OrderDetail orderDetail : orderDetailList) {
			addUserMatchCollect(orderDetail);
		}
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
		List<Integer> lotteryFailUserIds = new ArrayList<Integer>(orders.size());
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
			String gameDesc="竞彩足球";
			if(Integer.valueOf(2).equals(order.getLotteryClassifyId())){
				gameDesc="大乐透";
			}else if(Integer.valueOf(3).equals(order.getLotteryClassifyId())) {
				gameDesc="竞彩篮球";
			}
			lotteryFailUserIds.add(order.getUserId());
			User user = userMapper.queryUserByUserId(order.getUserId());
			String pushKey= user.getPushKey();
			String content = MessageFormat.format(CommonConstants.FORMAT_PRINTLOTTERY_PUSH_DESC,pushKey);
			GeTuiMessage getuiMessage = new GeTuiMessage(CommonConstants.FORMAT_PRINTLOTTERY_PUSH_TITLE, content, DateUtil.getCurrentTimeLong());
			geTuiUtil.pushMessage(pushKey, getuiMessage);
			messageAddParam.setMsgDesc(MessageFormat.format(CommonConstants.FORMAT_PRINTLOTTERY_MSG_DESC,gameDesc,ticketAmount, format));
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
				int hasResultCount=0;
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
							hasResultCount++;
							sbuf.append("07|").append(playCode).append("|").append(cellCode).append(";");
						}
					} else {
						for (DlLeagueMatchResult dto : resultDTOs) {
							if (playType.equals(dto.getPlayType())) {
								hasResultCount++;
								sbuf.append("0").append(dto.getPlayType()).append("|").append(playCode).append("|").append(dto.getCellCode()).append(";");
							}
						}
					}
				}
				if (sbuf.length() > 0&& hasResultCount==split.length) {
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

    /**
     * 构造篮球的matchResult
     */
    public List<JsonResultBasketball> generateBasketResult(List<CountBasketBaseInfo> countBasketBaseInfoList){
        List<JsonResultBasketball> jsonResultBasketballList = new ArrayList<>();
        for(CountBasketBaseInfo s:countBasketBaseInfoList) {
            JsonResultBasketball jsonResultBasketball = new JsonResultBasketball();
            jsonResultBasketball.setOrderDetailId(s.getOrderDetailId());
            jsonResultBasketball.setOrderSn(s.getOrderSn());
            jsonResultBasketball.setTicketData(s.getTicketData());
            Integer changCiId = s.getChangCiId();
            jsonResultBasketball.setChangciId(changCiId);
            jsonResultBasketball.setPlayCode(s.getPlayCode());
            String score = s.getScore();//客队：主队
            if (StringUtils.isNotEmpty(score)) {
                String[] VHArr = score.split(":");
                if (Integer.valueOf(VHArr[0]) > Integer.valueOf(VHArr[1])) {
                    jsonResultBasketball.setMnlResult("客胜");
                } else if (Integer.valueOf(VHArr[0]) < Integer.valueOf(VHArr[1])) {
                    jsonResultBasketball.setMnlResult("主胜");
                }

                if (StringUtils.isNotEmpty(s.getRangFen())) {
                    if (Double.valueOf(VHArr[1]) + Double.valueOf(s.getRangFen()) > Double.valueOf(VHArr[0])) {
                        jsonResultBasketball.setHdcResult("主胜");
                    } else if (Double.valueOf(VHArr[1]) + Double.valueOf(s.getRangFen()) < Double.valueOf(VHArr[0])) {
                        jsonResultBasketball.setHdcResult("主负");
                    }
                }

                Integer vnmScore = Integer.valueOf(VHArr[0]) - Integer.valueOf(VHArr[1]);
                jsonResultBasketball.setWnmResult(whichWNMPeriod(vnmScore));

                if (StringUtils.isNotEmpty(s.getForecastScore())) {
                    if (Double.valueOf(VHArr[0]) + Double.valueOf(VHArr[1]) > Double.valueOf(s.getForecastScore())) {
                        jsonResultBasketball.setHiloResult("大");
                    } else if (Double.valueOf(VHArr[0]) + Double.valueOf(VHArr[1]) < Double.valueOf(s.getForecastScore())) {
                        jsonResultBasketball.setHiloResult("小");
                    }
                }

            }
            jsonResultBasketballList.add(jsonResultBasketball);
        }

        return jsonResultBasketballList;
    }

    /**
     * 不包含差值得于0的情况
     * @param vnmScore
     * @return
     */
    public static String whichWNMPeriod(Integer vnmScore){
        if(vnmScore >= 1 && vnmScore <= 5){
            return "客胜1-5";
        }else if(vnmScore >= 6 && vnmScore <= 10){
            return "客胜6-10";
        }else if(vnmScore >= 11 && vnmScore <= 15){
            return "客胜11-15";
        }else if(vnmScore >= 16 && vnmScore <= 20){
            return "客胜16-20";
        }else if(vnmScore >= 21 && vnmScore <= 25){
            return "客胜21-25";
        }else if(vnmScore >= 26){
            return "客胜26+";
        }else if(vnmScore >= -5 && vnmScore <= -1){
            return "主胜1-5";
        }else if(vnmScore >= -10 && vnmScore <= -6){
            return "主胜6-10";
        }else if(vnmScore >= -15 && vnmScore <= -11){
            return "主胜11-15";
        }else if(vnmScore >= -20 && vnmScore <= -16){
            return "主胜16-20";
        }else if(vnmScore >= -25 && vnmScore <= -21){
            return "主胜21-25";
        }else if( vnmScore <= -26){
            return "主胜26+";
        }

        return "";
    }




	/*
	 *更新订单的比赛结果,主体为订单详情（订单详情对应的每一场比赛进行更新赛果）
	 */
	public void updateOrderBasketMatchResult() {
		List<OrderDetail> orderDetails = orderDetailMapper.unBasketMatchResultOrderDetails();
		if (CollectionUtils.isEmpty(orderDetails)) {
			return;
		}
		Set<String> playCodesSet = orderDetails.stream().map(detail -> detail.getIssue()).collect(Collectors.toSet());
		List<String> playCodes = new ArrayList<String>(playCodesSet.size());
		playCodes.addAll(playCodesSet);
		log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size());
		List<String> cancelMatches = dlMatchBasketballMapper.getCancelMatches(playCodes);
		List<DlMatchBasketball> matchBasketBallList = dlMatchBasketballMapper.getEndBasketMatchByPlayCodes(playCodes);

		//转换
        List<CountBasketBaseInfo> countBasketBaseInfoList = new ArrayList<CountBasketBaseInfo>();

        for(OrderDetail s:orderDetails) {
            String issue = s.getIssue();
            CountBasketBaseInfo countBasketBaseInfo = new CountBasketBaseInfo();
            for(DlMatchBasketball ss:matchBasketBallList) {
                if (s.getTicketData().contains(ss.getMatchSn())) {
                    countBasketBaseInfo.setOrderDetailId(s.getOrderDetailId());
                    countBasketBaseInfo.setOrderSn(s.getOrderSn());
                    countBasketBaseInfo.setTicketData(s.getTicketData());
                    countBasketBaseInfo.setChangCiId(ss.getChangciId());
                    countBasketBaseInfo.setPlayCode(s.getIssue());//issue 就是playcode
                    countBasketBaseInfo.setScore(ss.getWhole());//比分
                    countBasketBaseInfo.setForecastScore(s.getForecastScore());//订单详情中预设总分
                    countBasketBaseInfo.setRangFen(s.getFixedodds());//让分
                    countBasketBaseInfoList.add(countBasketBaseInfo);
                    break;
                }
            }
        }

        List<JsonResultBasketball> jsonResultBasketballList = generateBasketResult(countBasketBaseInfoList);
        //转换

		if (CollectionUtils.isEmpty(jsonResultBasketballList) && CollectionUtils.isEmpty(cancelMatches)) {
			log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size() + " 没有获取到相应的赛事结果信息及没有取消赛事");
			return;
		}
		log.info("updateOrderMatchResult 准备获取赛事结果的场次数：" + playCodes.size() + " 获取到相应的赛事结果信息数：" + jsonResultBasketballList.size() + " 取消赛事数：" + cancelMatches.size());
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
		Map<Integer,List<BasketMatchOneResultDTO>> resultMap = new HashMap<>();
		for(JsonResultBasketball basketBallResult:jsonResultBasketballList) {//每个订单详情对应一场比赛的4种赛果
		    Integer orderDetailId = basketBallResult.getOrderDetailId();
			List<BasketMatchOneResultDTO> matchOneResult = new ArrayList<>();
			Integer changciId = basketBallResult.getChangciId();
			String playCode = basketBallResult.getPlayCode();
			String hdc_result = basketBallResult.getHdcResult();
			String hilo_result = basketBallResult.getHiloResult();
			String mnl_result = basketBallResult.getMnlResult();
			String wnm_result = basketBallResult.getWnmResult();
			BasketMatchOneResultDTO dto1 = new BasketMatchOneResultDTO();

			if(StringUtils.isNotEmpty(mnl_result)){
                dto1.setPlayType(String.valueOf(MatchBasketPlayTypeEnum.PLAY_TYPE_MNL.getcode()));
                dto1.setPlayCode(playCode);
                mnl_result = mnl_result.replaceAll(" ", "");
                if(mnl_result.equals("主负")){
                    mnl_result = "客胜";
                }
                dto1.setCellCode(String.valueOf(MatchBasketBallResultHDCEnum.getCode(mnl_result)));
                dto1.setCellName(mnl_result);
            }

			
			BasketMatchOneResultDTO dto2 = new BasketMatchOneResultDTO();
			if(StringUtils.isNotEmpty(hdc_result)){
                dto2.setPlayType(String.valueOf(MatchBasketPlayTypeEnum.PLAY_TYPE_HDC.getcode()));
                dto2.setPlayCode(playCode);
                if(hdc_result.equals("主胜")){
                    hdc_result = "让分主胜";
                }else if(hdc_result.equals("主负") || hdc_result.equals("让分主负")){
                    hdc_result = "让分客胜";
                }
                dto2.setCellCode(String.valueOf(MatchBasketResultHdEnum.getCode(hdc_result)));
                dto2.setCellName(hdc_result);
            }

			
			BasketMatchOneResultDTO dto3 = new BasketMatchOneResultDTO();
			if(StringUtils.isNotEmpty(wnm_result)){
                dto3.setPlayType(String.valueOf(MatchBasketPlayTypeEnum.PLAY_TYPE_WNM.getcode()));
                dto3.setPlayCode(playCode);
                dto3.setCellCode(reHVMNLCode(wnm_result));
                dto3.setCellName(wnm_result);
            }

			BasketMatchOneResultDTO dto4 = new BasketMatchOneResultDTO();
			if(StringUtils.isNotEmpty(hilo_result)){
                dto4.setPlayType(String.valueOf(MatchBasketPlayTypeEnum.PLAY_TYPE_HILO.getcode()));
                dto4.setPlayCode(playCode);
                dto4.setCellCode(MatchBasketBallResultHILOEnum.getCode(hilo_result+"分"));
                dto4.setCellName(hilo_result);

            }

			matchOneResult.add(dto1);
			matchOneResult.add(dto2);
			matchOneResult.add(dto3);
			matchOneResult.add(dto4);
			
			resultMap.put(orderDetailId, matchOneResult);
		}

		log.info("resultMap size=" + resultMap.size());
		List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>(orderDetails.size());

        for (JsonResultBasketball orderDetail : jsonResultBasketballList) {
            String ticketDataStr = orderDetail.getTicketData();
            List<BasketMatchOneResultDTO> resultDTOs = resultMap.get(orderDetail.getOrderDetailId());
            if(CollectionUtils.isEmpty(resultDTOs)){
                continue;
            }
            String[] split = ticketDataStr.split(";");
            OrderDetail od = new OrderDetail();
            od.setOrderDetailId(orderDetail.getOrderDetailId());
            StringBuffer sbuf = new StringBuffer();
            for (String ticketData : split) {
                if (StringUtils.isBlank(ticketData) || !ticketData.contains("|")) {
                    continue;
                }
                Integer playType = Integer.valueOf(ticketData.substring(0, ticketData.indexOf("|")));
                for (BasketMatchOneResultDTO dto : resultDTOs) {
                   if(dto.getPlayType() == null){
                       continue;
                   }
                    if (playType.equals(Integer.valueOf(dto.getPlayType()))) {
                        sbuf.append("0").append(dto.getPlayType()).append("|").append(dto.getPlayCode()).append("|").append(dto.getCellCode()).append(";");
                    }
                }
            }
            if (sbuf.length() > 0) {
                od.setMatchResult(sbuf.substring(0, sbuf.length() - 1));
                orderDetailList.add(od);
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


    /**
     * 获取胜分差的code
     * @param wnm_result
     * @return
     */
	public String reHVMNLCode(String wnmResult){
        String hRst = BasketBallHILOLeverlEnum.getCode(wnmResult.substring(2)+"分");
	    if(wnmResult.contains("客胜")){
            hRst = String.valueOf(Integer.valueOf(hRst) + 6);
        }
	    return hRst;
    }
	
	/*
	 * 需求：购买并成功出票，就收藏赛事
	 */
	public void addUserMatchCollect(OrderDetail detail) {
		Integer userId = detail.getUserId();
		Integer matchId = detail.getMatchId();
    	int rst = userMatchCollectMapper.queryUserMatchCollect(userId, matchId);
//    	log.info("查询到已购赛事:"+rst);
    	if(rst <= 0) {
//    		log.info("已购赛事收藏开始");
        	UserMatchCollect umc = new UserMatchCollect();
        	umc.setUserId(detail.getUserId());
        	umc.setMatchId(detail.getMatchId());
        	Date matchDate = detail.getMatchTime();
//        	log.info("日期格式："+matchDate.toString());
        	umc.setAddTime(DateUtil.getTimeSomeDate(matchDate));
        	umc.setIsDelete(0);
        	userMatchCollectMapper.insertUserCollectMatch(umc);
//        	log.info("已购赛事收藏結束");
    	}
	}

	public void addRewardMoneyToUsers() {
		List<OrderWithUserDTO> orderWithUserDTOs = orderMapper.selectOpenedAllRewardOrderList();
//		log.info("派奖已中奖的用户数据,需要派奖数量size============{}", orderWithUserDTOs.size());
//		log.info("派奖已中奖的用户数据：orderWithUserDTOs=============={}", orderWithUserDTOs );
		if (CollectionUtils.isNotEmpty(orderWithUserDTOs)) {
			
			for (OrderWithUserDTO orderWithUserDTO : orderWithUserDTOs) {
				orderMapper.updateStatisticsRewardStatusTo0(orderWithUserDTO.getOrderSn());
//				log.info("订单的中奖编号=============={}", orderWithUserDTO.getOrderSn());
				if (null == orderWithUserDTO.getMaxLevel() || !(orderWithUserDTO.getMaxLevel() == 1 || orderWithUserDTO.getMaxLevel() == 2 || orderWithUserDTO.getMaxLevel() == 3)) {
//					log.info("订单的中奖级别=============={}", orderWithUserDTO.getMaxLevel());
					AwardParam awardParam =new AwardParam();
					awardParam.setOrderSn(orderWithUserDTO.getOrderSn());
					storeUserMoneyService.orderAward(awardParam);
				}
			}
		}
	}
	
	public void addRewardMoneyToUsersTwo() {
		List<OrderWithUserDTO> orderWithUserDTOs = orderMapper.selectQddOpenedAllRewardOrderList();
//		log.info("派奖已中奖的用户数据：code=" + orderWithUserDTOs.size());
		if (CollectionUtils.isNotEmpty(orderWithUserDTOs)) {
//			log.info("需要派奖的数据:" + orderWithUserDTOs.size());
			List<UserIdAndRewardDTO> userIdAndRewardDTOs = new LinkedList<UserIdAndRewardDTO>();
			for (OrderWithUserDTO orderWithUserDTO : orderWithUserDTOs) {
				UserIdAndRewardDTO userIdAndRewardDTO = new UserIdAndRewardDTO();
				userIdAndRewardDTO.setUserId(orderWithUserDTO.getUserId());
				userIdAndRewardDTO.setOrderSn(orderWithUserDTO.getOrderSn());
				userIdAndRewardDTO.setReward(orderWithUserDTO.getRealRewardMoney());
				int betTime = orderWithUserDTO.getBetTime();
				userIdAndRewardDTO.setBetMoney(orderWithUserDTO.getBetMoney());
				userIdAndRewardDTO.setBetTime(DateUtil.getTimeString(betTime, DateUtil.datetimeFormat));
				userIdAndRewardDTO.setLotteryClassifyId(orderWithUserDTO.getLotteryClassifyId());
				userIdAndRewardDTOs.add(userIdAndRewardDTO);
			}
//			Integer accountTime = DateUtil.getCurrentTimeLong();
//			userAccountService.saveRewardMessageAsync(userIdAndRewardDTOs,accountTime);
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
	private String betMaxAndMinMoneyForBasket(List<TicketInfo> ticketInfos, Order orderInfoByOrderSn) {
		Integer times = orderInfoByOrderSn.getCathectic();
		String betTypes = orderInfoByOrderSn.getPassType();
		Map<String, List<String>> indexMap = this.getBetIndexList(ticketInfos, betTypes);
		TMatchBetMaxAndMinOddsList maxMoneyBetPlayCellsForLottery = this.maxMoneyBetPlayCellsForLotteryForBasket(ticketInfos);
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

	private TMatchBetMaxAndMinOddsList maxMoneyBetPlayCellsForLotteryForBasket(List<TicketInfo> ticketInfos) {
		TMatchBetMaxAndMinOddsList tem = new TMatchBetMaxAndMinOddsList();
		List<Double> maxOdds = new ArrayList<Double>(ticketInfos.size());
		List<Double> minOdds = new ArrayList<Double>(ticketInfos.size());
		for (TicketInfo ticketInfo : ticketInfos) {
			List<TicketPlayInfo> ticketPlayInfos = ticketInfo.getTicketPlayInfos();
			List<Double> allbetComOdds = new ArrayList<>();//this.allbetComOdds(ticketPlayInfos);
			allbetComOdds.add(1.32);
			allbetComOdds.add(1.32);
			allbetComOdds.add(2.32);
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
                            if(!MatchResultHadEnum.HAD_H.getCode().equals(cellCode)) {
                                List<Double> tnaList = new ArrayList<Double>(aList);
                                for(Double item: tnaList) {
                                    naList.add(Double.sum(item, odds));
                                }
							}
							/*
							 * tnaList.forEach(item->Double.sum(item, odds));
							 * naList.addAll(tnaList);
							 */
						}
					} else {
                        if(ish) {
                            if(!MatchResultHadEnum.HAD_A.getCode().equals(cellCode)) {
                                List<Double> tnhList = new ArrayList<Double>(hList);
                                /*tnhList.forEach(item->Double.sum(item, odds));
                                nhList.addAll(tnhList);*/
                                for(Double item: tnhList) {
                                    nhList.add(Double.sum(item, odds));
                                }
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
//		if (allOdds.size() == 0) {
			if (hList != null) {
				allOdds.addAll(hList);
			}
			if (aList != null) {
				allOdds.addAll(aList);
			}
			if (dList != null) {
				allOdds.addAll(dList);
			}
//		}
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

	/**
	 * 查询支付成功订单未进行出票数据
	 * @return
	 */
	public List<Order> getPaySuccessOrdersList() {
		return orderMapper.selectPaySuccessOrdersList();
	}

	@Transactional(value="transactionManager1")
	public void doPaySuccessOrder(Order order) {
		String orderSn = order.getOrderSn();
		//进行预出票
		List<DlPrintLottery> dlPrints = dlPrintLotteryMapper.printLotterysByOrderSn(orderSn);
		if(CollectionUtils.isEmpty(dlPrints)){
			OrderInfoAndDetailDTO orderDetail = getOrderWithDetailByOrder(order);
			List<LotteryPrintDTO> lotteryPrints = dlPrintLotteryService.getPrintLotteryListByOrderInfo(orderDetail,orderSn);
			if(CollectionUtils.isNotEmpty(lotteryPrints)) {
				log.info("=============进行预出票和生成消息======================");
				dlPrintLotteryService.saveLotteryPrintInfo(lotteryPrints, order.getOrderSn(),order.getLotteryClassifyId());
		        return;
			}
		}
	}
	/**
	 * 插入第三方支付流水
	 * @param order
	 */
	private void insertThirdPayAccount(Order order) {
		Integer userId = order.getUserId();
		String orderSn = order.getOrderSn();
		User user = userMapper.queryUserByUserId(userId);
		PayLog payLog = payLogMapper.findPayLogByOrderSn(orderSn);
//		生成账户流水
		UserAccount userAccount = new UserAccount();
		userAccount.setUserId(userId);
		String accountSn = SNGenerator.nextSN(SNBusinessCodeEnum.ACCOUNT_SN.getCode());
		userAccount.setAmount(BigDecimal.ZERO.subtract(payLog.getOrderAmount()));
		userAccount.setOrderSn(orderSn);
		userAccount.setAccountSn(accountSn);
		userAccount.setBonusPrice(BigDecimal.ZERO);
		userAccount.setProcessType(3);
		userAccount.setThirdPartName("");
		userAccount.setPayId(""+payLog.getLogId());
		userAccount.setAddTime(DateUtil.getCurrentTimeLong());
		userAccount.setLastTime(DateUtil.getCurrentTimeLong());
		userAccount.setCurBalance(user.getUserMoney().add(user.getUserMoneyLimit()));
		userAccount.setStatus(1);
		userAccount.setNote("支付成功");
		String payCode = payLog.getPayCode();
		String payName;
		if(payCode.equals("app_weixin") || payCode.equals("app_weixin_h5")) {
			payName = "微信";
		}else {
			payName = "银行卡";
		}
		userAccount.setPaymentName(payName);
		userAccount.setThirdPartName(payName);
		userAccount.setThirdPartPaid(payLog.getOrderAmount());
		int rst = userAccountMapper.insertUserAccountBySelective(userAccount);
		if(rst>0){
			log.info("插入提三方支付流水成功 orderSn={},insertRow={}",orderSn,rst);
		}else{
			log.info("插入提三方支付流水失败 orderSn={},insertRow={},accountInfo={}",orderSn,rst,JSONHelper.bean2json(userAccount));
		}
	}

	/**
	 * 根据订单编号查询订单及订单详情
	 * 
	 * @param param
	 * @return
	 */
	public OrderInfoAndDetailDTO getOrderWithDetailByOrder(Order order) {
		List<OrderDetail> orderDetails = orderDetailMapper.queryListByOrderSn(order.getOrderSn());
		OrderInfoAndDetailDTO orderInfoAndDetailDTO = new OrderInfoAndDetailDTO();
		OrderInfoDTO orderInfoDTO = new OrderInfoDTO();
		orderInfoDTO.setCathectic(order.getCathectic());
		orderInfoDTO.setLotteryClassifyId(order.getLotteryClassifyId());
		orderInfoDTO.setLotteryPlayClassifyId(order.getLotteryPlayClassifyId());
		orderInfoDTO.setPassType(order.getPassType());
		orderInfoDTO.setPlayType(order.getPlayType());
		Date minMatchStartTime = null;
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
				if(minMatchStartTime==null){
					minMatchStartTime = orderDetail.getMatchTime();
				}else{
					if(minMatchStartTime.after(orderDetail.getMatchTime())){
						minMatchStartTime = orderDetail.getMatchTime();
					}
				}
				orderDetailDataDTO.setTicketData(orderDetail.getTicketData());
				orderDetailDataDTO.setIssue(orderDetail.getIssue());
				orderDetailDataDTO.setBetType(orderDetail.getBetType());
				orderDetailDataDTOs.add(orderDetailDataDTO);
			}
		}
		orderInfoDTO.setMinMatchStartTime(minMatchStartTime);
		orderInfoAndDetailDTO.setOrderDetailDataDTOs(orderDetailDataDTOs);
		return orderInfoAndDetailDTO;
	}

	public List<Order> getPayFailOrdersList() {
		return orderMapper.selectPayFailOrdersList();
	}


	public void openPrizeForSupperLotto() {
		//查询待开奖的投注列表
		 List<Order> orderList =orderMapper.selectAllUnOpenPrizeListForSupperLotto();  
//			log.info("大乐透未开奖订单列表.size※※※※※※※※※※※※※※※※※※※※※※※※※※※※{}",orderList.size());
			List<String> gameIssue = new ArrayList<String>();
			orderList.forEach(order->{
					gameIssue.add(order.getIssue()); 
			});
			//对期次去重
		   List<String> uniqueGameIssue = gameIssue.stream().distinct().collect(Collectors.toList());
//		   log.info("大乐透未开奖的期次※※※※※※※※※※※※※※※※※※※※※※※※※※※※{}",uniqueGameIssue);
		   
		   //获取期次相关信息
		   for (int i = 0; i < uniqueGameIssue.size(); i++) {
//			   log.info("当前要开奖的期次※※※※※※※※※※※※※※※※※※※※※※※※※※※※{}",uniqueGameIssue.get(i));
			   DlSuperLotto dlSuperLotto = dlSuperLottoMapper.selectPrizeResultByTermNum(uniqueGameIssue.get(i));
			   SupperLottoParam supperLottoParam =new SupperLottoParam();
			   supperLottoParam.setTermNum(Integer.parseInt(uniqueGameIssue.get(i)));
				List<DlSuperLottoRewardDTO> superLottoRewardList = supperLottoService.findByTermNum(supperLottoParam).getData();
//			   log.info("第"+uniqueGameIssue.get(i)+"期次信息※※※※※※※※※※※※※※※※※※※※※※※※※※※※{}",dlSuperLotto);
//			   判断该期次是否开奖
			   if(dlSuperLotto!=null&&!StringUtils.isEmpty(dlSuperLotto.getPrizeNum())){	
				BigDecimal prizeA = new BigDecimal(0);
				BigDecimal prizeB = new BigDecimal(0);
				BigDecimal prizeC = new BigDecimal(0);
				BigDecimal prizeAAppend = new BigDecimal(0);
				BigDecimal prizeBAppend = new BigDecimal(0);
				BigDecimal prizeCAppend= new BigDecimal(0);
				   for (int j = 0; j < superLottoRewardList.size(); j++) {
					   DlSuperLottoRewardDTO superLottoRewardDTO =superLottoRewardList.get(j);
					if (superLottoRewardDTO.getRewardLevel()==1) {
						prizeA = BigDecimal.valueOf(superLottoRewardDTO.getRewardPrice1());
						prizeAAppend = BigDecimal.valueOf(superLottoRewardDTO.getRewardPrice2());
					}else if (superLottoRewardDTO.getRewardLevel()==2) {
						prizeB = BigDecimal.valueOf(superLottoRewardDTO.getRewardPrice1());
						prizeBAppend = BigDecimal.valueOf(superLottoRewardDTO.getRewardPrice2());
					}else if (superLottoRewardDTO.getRewardLevel()==3) {
						prizeC = BigDecimal.valueOf(superLottoRewardDTO.getRewardPrice1());
						prizeCAppend = BigDecimal.valueOf(superLottoRewardDTO.getRewardPrice2());
					}
				}
				   orderDetailMapper.beatchUpdateMatchResult(uniqueGameIssue.get(i),dlSuperLotto.getPrizeNum());
			   //操作订单,计算奖金
			   for (int j = 0; j < orderList.size(); j++) {
				  
				List< OrderDetail>  orderDetailList =orderDetailMapper.queryListByOrderSn(orderList.get(j).getOrderSn());
//				log.info("第"+uniqueGameIssue.get(i)+"期,订单号为:"+orderList.get(j).getOrderSn()+"要开奖的订单详情列表.size※※※※※※※※※※※※※※※※※※※※※※※※※※※※{}",orderDetailList.size());
				boolean flag = true;
				BigDecimal winningMoney = new BigDecimal(0);
				Integer maxWinningLevel = 888;
				for (int k = 0; k < orderDetailList.size(); k++) {
					OrderDetail orderDetail = orderDetailList.get(k);
//					log.info("订单详情Id为:"+orderDetailList.get(k).getOrderDetailId()+"的赛果为不为空※※※※※※※※※※※※※※※※※※※※※※※※※※※※{}",null == orderDetail.getMatchResult());
					if (null == orderDetail.getMatchResult()) {
						flag = false;
						break;
					}
//					log.info("订单详情为※※※※※※※※※※※※※※※※※※※※※※※※※※※※{}",orderDetailList.get(k));
				   StringBuilder matchResult = new StringBuilder(orderDetail.getMatchResult());
				   matchResult=matchResult.replace(14,15, "|");
					LottoResultEntity resultEntity = LottoUtils.calPrizeLevel(orderDetail.getTicketData(), matchResult.toString());
//					log.info("算奖结果实体类※※※※※※※※※※※※※※※※※※※※※※※※※※※※{}",resultEntity);
					BigDecimal moneyPrize = new BigDecimal(0);
					if (resultEntity.status == LottoResultEntity.STATUS_HIT) {
						 boolean isAppend = false;
						   if (orderList.get(j).getPlayType().equals("05")) {
							   isAppend = true;
						}
						moneyPrize = LottoMoneyUtil.calculateV2(resultEntity ,  prizeA,  prizeB,  prizeC, prizeAAppend,  prizeBAppend,  prizeCAppend,  isAppend);
					}
//					log.info("待开奖编号※※※{}。用户投注※※※{}。开奖结果※※※{}。中奖金额※※※{}。一等奖※※※{}。二等奖※※※{}。三等奖※※※{}。追加一※※※{}。追加二※※※{}。追加三※※※{}。",orderDetail.getOrderSn(),orderDetail.getTicketData(), matchResult.toString(),moneyPrize,  prizeA,  prizeB,  prizeC, prizeAAppend,  prizeBAppend,  prizeCAppend);
					//赛选出最大的奖项 数值越小 奖项越靠前
					if (maxWinningLevel > resultEntity.getMaxLevel()) {
						maxWinningLevel = resultEntity.getMaxLevel();
					}
					moneyPrize =moneyPrize.multiply(BigDecimal.valueOf(orderList.get(j).getCathectic()));
					winningMoney = winningMoney.add(moneyPrize);
					SupperLottoOrderDetailParam supperLottoOrderDetailParam =new SupperLottoOrderDetailParam();
					supperLottoOrderDetailParam.setOrderDetailId(orderDetailList.get(k).getOrderDetailId());
					if (resultEntity.status== LottoResultEntity.STATUS_HIT) {
						supperLottoOrderDetailParam.setMoneyPrize(moneyPrize);
						supperLottoOrderDetailParam.setIsGuess(1);//0-未猜中 1-已猜中
						supperLottoOrderDetailParam.setTicketStatus(2);//1-未中奖,2-已中奖
						if (resultEntity.isCompund) {
							supperLottoOrderDetailParam.setLevelPrize(resultEntity.lottoLevel.toCompoundPrizeLevle());
						}else {
							supperLottoOrderDetailParam.setLevelPrize(resultEntity.lottoLevel.level+"");
						}
					}else {
						supperLottoOrderDetailParam.setMoneyPrize(BigDecimal.ZERO);
						supperLottoOrderDetailParam.setIsGuess(0);//0-未猜中 1-已猜中
						supperLottoOrderDetailParam.setTicketStatus(1);//1-未中奖,2-已中奖
						supperLottoOrderDetailParam.setLevelPrize("");
					}
//					回写到订单详情
					orderDetailMapper.updateOrderDetailInfoForSupperLotto(supperLottoOrderDetailParam);
//					log.info("第"+uniqueGameIssue.get(i)+"期要更新的订单详情数据※※※※※※※※※※※※※※※※※※※※※※※※※※※※{}",supperLottoOrderDetailParam);
				}
				if (flag) {
					SupperLottoOrderParam supperLottoOrderParam =new SupperLottoOrderParam();
					supperLottoOrderParam.setOrderSn(orderList.get(j).getOrderSn());
					if (winningMoney.compareTo(BigDecimal.ZERO) <= 0 ) {
						supperLottoOrderParam.setOrderStatus(4);
					}else {
						supperLottoOrderParam.setOrderStatus(5);
					}
					supperLottoOrderParam.setWinningMoney(winningMoney);
					supperLottoOrderParam.setMaxLevel(maxWinningLevel);
					//	累加计算订单详情金额保存到订单
					orderMapper.updateOrderInfoForSupperLotto(supperLottoOrderParam);
//					log.info("第"+uniqueGameIssue.get(i)+"期"+orderList.get(j).getOrderSn()+"要更新的订单数据※※※※※※※※※※※※※※※※※※※※※※※※※※※※{}",supperLottoOrderParam);
				}
			}
		}
	}
		//查询期次
		//根据期次查询开奖结果
		//对比投注结果,
	}
}