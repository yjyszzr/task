package com.dl.task.printlottery.channelImpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.dl.base.enums.ThirdApiEnum;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.enums.PrintLotteryStatusEnum;
import com.dl.task.enums.ThirdRewardStatusEnum;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.DlTicketChannel;
import com.dl.task.printlottery.IPrintChannelService;
import com.dl.task.printlottery.requestDto.CommonQueryStakeParam;
import com.dl.task.printlottery.requestDto.CommonToStakeParam;
import com.dl.task.printlottery.responseDto.QueryPrintBalanceDTO;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO.QueryRewardOrderResponse;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO.QueryStakeOrderResponse;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO.ToStakeBackOrderDetail;
import com.dl.task.printlottery.responseDto.caixiaomi.CaiXiaoMiDlToStakeDTO;
import com.dl.task.printlottery.responseDto.caixiaomi.CaiXiaoMiDlToStakeDTO.CaiXiaoBackOrderDetail;
import com.dl.task.printlottery.responseDto.caixiaomi.CaiXiaoMiQueryStakeResponseDTO;
import com.dl.task.printlottery.responseDto.caixiaomi.CaiXiaoMiQueryStakeResponseDTO.CaiXiaoMiQueryStakeOrderResponse;
import com.dl.task.printlottery.responseDto.henan.HenanQueryRewardDTO;
import com.dl.task.printlottery.responseDto.henan.HenanQueryRewardDTO.HenanQueryRewardOrderResponse;

@Service
@Slf4j
public class PrintChannelCaixiaomiServiceImpl implements IPrintChannelService {

	private String version = "1.0";

	@Override
	public ToStakeResponseDTO toStake(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		CommonToStakeParam commonToStakeParam = defaultCommonToStakeParam(dlPrintLotterys, dlTicketChannel.getTicketMerchant(), version);
		JSONObject jo = JSONObject.fromObject(commonToStakeParam);
		String backStr = defaultCommonRestRequest(dlTicketChannel, dlPrintLotteryMapper, jo, "/stake", ThirdApiEnum.CAI_XIAO_MI_LOTTERY);
		JSONObject backJo = JSONObject.fromObject(backStr);
		@SuppressWarnings("rawtypes")
		Map<String, Class> mapClass = new HashMap<String, Class>();
		mapClass.put("orders", CaiXiaoBackOrderDetail.class);
		CaiXiaoMiDlToStakeDTO dlToStakeDTO = (CaiXiaoMiDlToStakeDTO) JSONObject.toBean(backJo, CaiXiaoMiDlToStakeDTO.class, mapClass);
		ToStakeResponseDTO toStakeResponseDTO = new ToStakeResponseDTO();
		toStakeResponseDTO.setRetSucc(Boolean.FALSE);
		if (dlToStakeDTO == null) {
			return toStakeResponseDTO;
		}
		toStakeResponseDTO.setRetCode(dlToStakeDTO.getRetCode());
		if (!CollectionUtils.isEmpty(dlToStakeDTO.getOrders())) {
			toStakeResponseDTO.setRetSucc(Boolean.TRUE);
			List<ToStakeBackOrderDetail> orders = new ArrayList<>();
			for (CaiXiaoBackOrderDetail heNanDetail : dlToStakeDTO.getOrders()) {
				ToStakeBackOrderDetail orderDetail = new ToStakeBackOrderDetail();
				orderDetail.setErrorCode(heNanDetail.getErrorCode());
				orderDetail.setPlatformId(heNanDetail.getPlatformId());
				orderDetail.setTicketId(heNanDetail.getTicketId());
				Boolean printLotteryDoing = heNanDetail.getErrorCode() == 0 || heNanDetail.getErrorCode() == 8;
				orderDetail.setPrintLotteryDoing(printLotteryDoing);
				orders.add(orderDetail);
			}
			toStakeResponseDTO.setOrders(orders);
		}
		return toStakeResponseDTO;
	}

	@Override
	public QueryStakeResponseDTO queryStake(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		CommonQueryStakeParam commonQueryStakeParam = defaultCommonQueryStakeParam(dlPrintLotterys, dlTicketChannel.getTicketMerchant(), version);
		JSONObject jo = JSONObject.fromObject(commonQueryStakeParam);
		String backStr = defaultCommonRestRequest(dlTicketChannel, dlPrintLotteryMapper, jo, "/stake_query", ThirdApiEnum.HE_NAN_LOTTERY);
		JSONObject backJo = JSONObject.fromObject(backStr);
		@SuppressWarnings("rawtypes")
		Map<String, Class> mapClass = new HashMap<String, Class>();
		mapClass.put("orders", CaiXiaoMiQueryStakeOrderResponse.class);
		CaiXiaoMiQueryStakeResponseDTO dlQueryStakeDTO = (CaiXiaoMiQueryStakeResponseDTO) JSONObject.toBean(backJo, CaiXiaoMiQueryStakeResponseDTO.class, mapClass);
		QueryStakeResponseDTO queryStakeResponseDto = new QueryStakeResponseDTO();
		queryStakeResponseDto.setQuerySuccess(Boolean.FALSE);
		if (dlQueryStakeDTO == null) {
			return queryStakeResponseDto;
		}
		queryStakeResponseDto.setRetCode(dlQueryStakeDTO.getRetCode());
		queryStakeResponseDto.setRetDesc(dlQueryStakeDTO.getRetDesc());
		if (!CollectionUtils.isEmpty(dlQueryStakeDTO.getOrders())) {
			queryStakeResponseDto.setQuerySuccess(Boolean.TRUE);
			List<QueryStakeOrderResponse> orders = new ArrayList<QueryStakeResponseDTO.QueryStakeOrderResponse>();
			for (CaiXiaoMiQueryStakeOrderResponse caiXiaoMiQueryOrderResponse : dlQueryStakeDTO.getOrders()) {
				QueryStakeOrderResponse queryStakeOrderResponse = new QueryStakeOrderResponse();
				PrintLotteryStatusEnum statusEnum = PrintLotteryStatusEnum.DOING;
				Boolean querySuccess = Boolean.FALSE;
				Integer retCode = caiXiaoMiQueryOrderResponse.getRetCode();
				if (retCode == 0) {
					Integer printStatus = caiXiaoMiQueryOrderResponse.getPrintStatus();
					if (Integer.valueOf(16).equals(printStatus)) {
						statusEnum = PrintLotteryStatusEnum.SUCCESS;
						querySuccess = Boolean.TRUE;
					} else if (Integer.valueOf(17).equals(printStatus)) {
						statusEnum = PrintLotteryStatusEnum.FAIL;
						querySuccess = Boolean.TRUE;
					} else if (Integer.valueOf(8).equals(printStatus)) {
						statusEnum = PrintLotteryStatusEnum.DOING;
						querySuccess = Boolean.FALSE;
					} else {
						log.error("彩小秘出票查询出现非预定状态printStatus={}", printStatus);
						querySuccess = Boolean.FALSE;
					}
				}
				queryStakeOrderResponse.setQuerySuccess(querySuccess);
				if (querySuccess) {
					queryStakeOrderResponse.setStatusEnum(statusEnum);
					queryStakeOrderResponse.setPrintStatus(caiXiaoMiQueryOrderResponse.getPrintStatus());
					queryStakeOrderResponse.setPlatformId(caiXiaoMiQueryOrderResponse.getPlatformId());
					queryStakeOrderResponse.setPrintNo(caiXiaoMiQueryOrderResponse.getPrintNo());
					queryStakeOrderResponse.setSp(caiXiaoMiQueryOrderResponse.getSp());
					queryStakeOrderResponse.setTicketId(caiXiaoMiQueryOrderResponse.getTicketId());
					queryStakeOrderResponse.setPrintTime(caiXiaoMiQueryOrderResponse.getPrintTime());
					Date printTime = new Date();
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String printTimeStr = caiXiaoMiQueryOrderResponse.getPrintTime();
						if (!printTimeStr.equals("")) {
							printTimeStr = printTimeStr.replaceAll("/", "-");
							printTime = sdf.parse(printTimeStr);
						}
					} catch (Exception e) {
						log.error("彩小秘出票时间转化出错 ticketId={},printTimeStr={}", caiXiaoMiQueryOrderResponse.getTicketId(), caiXiaoMiQueryOrderResponse.getPrintTime());
					}
					queryStakeOrderResponse.setPrintTimeDate(printTime);
				}
				orders.add(queryStakeOrderResponse);
			}
			queryStakeResponseDto.setOrders(orders);
		}
		return queryStakeResponseDto;
	}

	@Override
	public QueryRewardResponseDTO queryRewardByLottery(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		// TODO 胡贺东 暂时不实现
		// QueryRewardResponseDTO notSupport = new QueryRewardResponseDTO();
		// notSupport.setQuerySuccess(Boolean.FALSE);
		// notSupport.setRetCode("-1");
		// notSupport.setRetDesc("notSupprt");

		CommonQueryStakeParam commonQueryStakeParam = defaultCommonQueryStakeParam(dlPrintLotterys, dlTicketChannel.getTicketMerchant(), version);
		List<String> collect = dlPrintLotterys.stream().map(print -> print.getPlatformId()).collect(Collectors.toList());
		String[] platformIds = collect.toArray(new String[collect.size()]);
		commonQueryStakeParam.setOrders(platformIds);
		JSONObject jo = JSONObject.fromObject(commonQueryStakeParam);
		String backStr = defaultCommonRestRequest(dlTicketChannel, dlPrintLotteryMapper, jo, "/award_query", ThirdApiEnum.CAI_XIAO_MI_LOTTERY);
		JSONObject backJo = JSONObject.fromObject(backStr);
		@SuppressWarnings("rawtypes")
		Map<String, Class> mapClass = new HashMap<String, Class>();
		mapClass.put("orders", HenanQueryRewardOrderResponse.class);
		HenanQueryRewardDTO dlQueryRewardDTO = (HenanQueryRewardDTO) JSONObject.toBean(backJo, HenanQueryRewardDTO.class, mapClass);
		QueryRewardResponseDTO queryRewardResponseDto = new QueryRewardResponseDTO();
		queryRewardResponseDto.setQuerySuccess(Boolean.FALSE);
		if (dlQueryRewardDTO == null) {
			return queryRewardResponseDto;
		}
		queryRewardResponseDto.setRetCode(dlQueryRewardDTO.getRetCode());
		queryRewardResponseDto.setRetDesc(dlQueryRewardDTO.getRetDesc());
		if (!CollectionUtils.isEmpty(dlQueryRewardDTO.getOrders())) {
			queryRewardResponseDto.setQuerySuccess(Boolean.TRUE);
			List<QueryRewardOrderResponse> orders = new ArrayList<QueryRewardResponseDTO.QueryRewardOrderResponse>();
			for (HenanQueryRewardOrderResponse rewardOrder : dlQueryRewardDTO.getOrders()) {
				QueryRewardOrderResponse queryRewardOrderResponse = new QueryRewardOrderResponse();
				Integer status = rewardOrder.getStatus();
				String ticketId = rewardOrder.getTicketId();
				Boolean querySuccess = Boolean.FALSE;
				if (Integer.valueOf(8).equals(status) || Integer.valueOf(9).equals(status) || Integer.valueOf(10).equals(status)) {
					querySuccess = Boolean.TRUE;
				}
				queryRewardOrderResponse.setQuerySuccess(querySuccess);
				if (querySuccess) {
					queryRewardOrderResponse.setTicketId(ticketId);
					queryRewardOrderResponse.setThirdRewardStatusEnum(ThirdRewardStatusEnum.REWARD_OVER);
					Integer preTax = rewardOrder.getPreTax();
					Integer tax = rewardOrder.getTax();
					Integer prizeMoneyInteger = preTax - tax;
					queryRewardOrderResponse.setPrizeMoney(prizeMoneyInteger);
				}
				orders.add(queryRewardOrderResponse);
			}
			queryRewardResponseDto.setOrders(orders);
		}
		return queryRewardResponseDto;

		// return notSupport;
	}

	@Override
	public QueryRewardResponseDTO queryRewardByIssue(String issue, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		// TODO 胡贺东 暂时不实现
		QueryRewardResponseDTO notSupport = new QueryRewardResponseDTO();
		notSupport.setQuerySuccess(Boolean.FALSE);
		notSupport.setRetCode("-1");
		notSupport.setRetDesc("notSupprt");
		return notSupport;
	}

	@Override
	public QueryPrintBalanceDTO queryBalance(DlTicketChannel channel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		QueryPrintBalanceDTO notSupport = new QueryPrintBalanceDTO();
		notSupport.setQuerySuccess(Boolean.FALSE);
		notSupport.setRetCode("-1");
		notSupport.setRetDesc("notSupprt");
		return notSupport;
	}

}
