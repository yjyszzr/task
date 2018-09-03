package com.dl.task.printlottery.channelImpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import com.dl.task.printlottery.responseDto.ToRewardResponseDTO;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO.ToStakeBackOrderDetail;
import com.dl.task.printlottery.responseDto.xian.XiAnDlToStakeDTO;
import com.dl.task.printlottery.responseDto.xian.XiAnDlToStakeDTO.XIANBackOrderDetail;
import com.dl.task.printlottery.responseDto.xian.XianQueryStakeResponseDTO;
import com.dl.task.printlottery.responseDto.xian.XianQueryStakeResponseDTO.XianQueryStakeOrderResponse;

@Service
@Slf4j
public class PrintChannelXianServiceImpl implements IPrintChannelService {

	private String version = "1.0";

	@Override
	public ToStakeResponseDTO toStake(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		CommonToStakeParam commonToStakeParam = defaultCommonToStakeParam(dlPrintLotterys, dlTicketChannel.getTicketMerchant(), version);
		JSONObject jo = JSONObject.fromObject(commonToStakeParam);
		String backStr = defaultCommonRestRequest(dlTicketChannel, dlPrintLotteryMapper, jo, "/order/create", ThirdApiEnum.XI_AN_LOTTERY);
		JSONObject backJo = JSONObject.fromObject(backStr);
		@SuppressWarnings("rawtypes")
		Map<String, Class> mapClass = new HashMap<String, Class>();
		mapClass.put("orders", XIANBackOrderDetail.class);
		XiAnDlToStakeDTO dlToStakeDTO = (XiAnDlToStakeDTO) JSONObject.toBean(backJo, XiAnDlToStakeDTO.class, mapClass);
		ToStakeResponseDTO toStakeResponseDTO = new ToStakeResponseDTO();
		toStakeResponseDTO.setRetSucc(Boolean.FALSE);
		if (dlToStakeDTO == null) {
			return toStakeResponseDTO;
		}
		toStakeResponseDTO.setRetCode(dlToStakeDTO.getRetCode());
		toStakeResponseDTO.setRetDesc(dlToStakeDTO.getRetDesc());
		if (!CollectionUtils.isEmpty(dlToStakeDTO.getOrders())) {
			toStakeResponseDTO.setRetSucc(Boolean.TRUE);
			List<ToStakeBackOrderDetail> orders = new ArrayList<ToStakeResponseDTO.ToStakeBackOrderDetail>();
			for (XIANBackOrderDetail xianDetail : dlToStakeDTO.getOrders()) {
				ToStakeBackOrderDetail orderDetail = new ToStakeBackOrderDetail();
				orderDetail.setErrorCode(xianDetail.getErrorCode());
				orderDetail.setErrorDesc(xianDetail.getErrorDesc());
				orderDetail.setPlatformId(xianDetail.getPlatformId());
				orderDetail.setTicketId(xianDetail.getTicketId());
				Boolean printLotteryDoing = xianDetail.getErrorCode() == 0 || xianDetail.getErrorCode() == 3002;
				orderDetail.setPrintLotteryDoing(printLotteryDoing);
				orders.add(orderDetail);
			}
			toStakeResponseDTO.setOrders(orders);
		}
		return toStakeResponseDTO;
	}

	@Override
	public QueryStakeResponseDTO queryStake(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		XianQueryStakeResponseDTO dlQueryStakeDTO = queryXianStake(dlPrintLotterys, dlTicketChannel, dlPrintLotteryMapper);
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
			for (XianQueryStakeOrderResponse heNanQueryOrderResponse : dlQueryStakeDTO.getOrders()) {
				QueryStakeOrderResponse queryStakeOrderResponse = new QueryStakeOrderResponse();
				Integer printStatus = heNanQueryOrderResponse.getPrintStatus();
				PrintLotteryStatusEnum statusEnum = PrintLotteryStatusEnum.DOING;
				Boolean querySuccess = Boolean.FALSE;
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
					log.error("河南出票查询出现非预定状态printStatus={}", printStatus);
					querySuccess = Boolean.FALSE;
				}
				queryStakeOrderResponse.setQuerySuccess(querySuccess);
				if (querySuccess) {
					queryStakeOrderResponse.setStatusEnum(statusEnum);
					queryStakeOrderResponse.setPrintStatus(printStatus);
					queryStakeOrderResponse.setPlatformId(heNanQueryOrderResponse.getPlatformId());
					queryStakeOrderResponse.setPrintNo(heNanQueryOrderResponse.getPrintNo());
					log.info("西安赔率处理 ticketId={},sp={}", heNanQueryOrderResponse.getTicketId(), heNanQueryOrderResponse.getSp());
					String ourSp = parseStakeIssues(heNanQueryOrderResponse.getSp(), Boolean.TRUE);
					log.info("转化后的赔率格式是={}", ourSp);
					queryStakeOrderResponse.setSp(ourSp);
					queryStakeOrderResponse.setTicketId(heNanQueryOrderResponse.getTicketId());
					queryStakeOrderResponse.setPrintTime(heNanQueryOrderResponse.getPrintTime());
					Date printTime = new Date();
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String printTimeStr = heNanQueryOrderResponse.getPrintTime();
						printTimeStr = printTimeStr.replaceAll("/", "-");
						printTime = sdf.parse(printTimeStr);
					} catch (Exception e) {
						log.error("西安出票时间转化出错 ticketId={},printTimeStr={}", heNanQueryOrderResponse.getTicketId(), heNanQueryOrderResponse.getPrintTime());
					}
					queryStakeOrderResponse.setPrintTimeDate(printTime);
				}
				orders.add(queryStakeOrderResponse);
			}
			queryStakeResponseDto.setOrders(orders);
		}
		return queryStakeResponseDto;
	}

	private XianQueryStakeResponseDTO queryXianStake(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		CommonQueryStakeParam commonQueryStakeParam = defaultCommonQueryStakeParam(dlPrintLotterys, dlTicketChannel.getTicketMerchant(), version);
		JSONObject jo = JSONObject.fromObject(commonQueryStakeParam);
		String backStr = defaultCommonRestRequest(dlTicketChannel, dlPrintLotteryMapper, jo, "/order/query", ThirdApiEnum.HE_NAN_LOTTERY);
		JSONObject backJo = JSONObject.fromObject(backStr);
		@SuppressWarnings("rawtypes")
		Map<String, Class> mapClass = new HashMap<String, Class>();
		mapClass.put("orders", XianQueryStakeOrderResponse.class);
		XianQueryStakeResponseDTO dlQueryStakeDTO = (XianQueryStakeResponseDTO) JSONObject.toBean(backJo, XianQueryStakeResponseDTO.class, mapClass);
		return dlQueryStakeDTO;
	}

	/**
	 * 处理投注信息转化
	 * 
	 * @param stakeIssues
	 * @param removePlayCode
	 * @return
	 */
	private static String parseStakeIssues(String stakeIssues, Boolean removePlayCode) {
		StringBuffer ourStakeSpResult = new StringBuffer();
		if (StringUtils.isNotEmpty(stakeIssues)) {
			TreeMap<String, String> ourStakes = new TreeMap<String, String>();
			stakeIssues = stakeIssues.replace(":", "");// 替换掉冒号
			String[] xianSpList = stakeIssues.split(";");
			for (int i = 0; i < xianSpList.length; i++) {
				String one = xianSpList[i];
				String[] issueStakes = one.split("\\|");
				if (issueStakes.length == 3) {// 玩法|场次|赔率
				// 统一场次只存一个
					String playAndIssue = issueStakes[0] + "|" + issueStakes[1];
					if (ourStakes.containsKey(playAndIssue)) {
						String result = ourStakes.get(playAndIssue);
						ourStakes.put(playAndIssue, result + "," + issueStakes[2]);
					} else {
						ourStakes.put(playAndIssue, issueStakes[2]);
					}
				} else {
					log.info("西安返回的赔率格式异常sp={}", stakeIssues);
				}
			}
			log.info("prase keymap={}", ourStakes.toString());
			for (String key : ourStakes.keySet()) {
				if (ourStakeSpResult.length() > 0) {// 除第一条数据外，其他的都要加上分号
					ourStakeSpResult.append(";");
				}
				if (removePlayCode) {
					String[] playCodeAndeIssueArr = key.split("\\|");
					ourStakeSpResult.append(playCodeAndeIssueArr[1]);
				} else {
					ourStakeSpResult.append(key);
				}
				ourStakeSpResult.append("|");
				ourStakeSpResult.append(ourStakes.get(key));
			}
		}
		return ourStakeSpResult.length() == 0 ? "" : ourStakeSpResult.toString();
	}

	@Override
	public QueryRewardResponseDTO queryRewardByLottery(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		XianQueryStakeResponseDTO dlQueryStakeDTO = queryXianStake(dlPrintLotterys, dlTicketChannel, dlPrintLotteryMapper);
		QueryRewardResponseDTO toStakeResponseDTO = new QueryRewardResponseDTO();
		toStakeResponseDTO.setQuerySuccess(Boolean.FALSE);
		if (dlQueryStakeDTO == null) {
			return toStakeResponseDTO;
		}
		toStakeResponseDTO.setRetCode(dlQueryStakeDTO.getRetCode());
		toStakeResponseDTO.setRetDesc(dlQueryStakeDTO.getRetDesc());
		if (!CollectionUtils.isEmpty(dlQueryStakeDTO.getOrders())) {
			toStakeResponseDTO.setQuerySuccess(Boolean.TRUE);
			List<QueryRewardOrderResponse> orders = new ArrayList<QueryRewardOrderResponse>();
			for (XianQueryStakeOrderResponse xianResponse : dlQueryStakeDTO.getOrders()) {
				Integer prizeStatus = xianResponse.getPrizeStatus();
				if (Integer.valueOf(1).equals(prizeStatus) || Integer.valueOf(2).equals(prizeStatus)) {
					String ticketId = xianResponse.getTicketId();
					Integer money = xianResponse.getPrizeMoney();
					if (money == null) {
						money = Integer.valueOf(0);
					}
					QueryRewardOrderResponse response = new QueryRewardOrderResponse();
					response.setQuerySuccess(Boolean.TRUE);
					response.setPrizeMoney(money);
					response.setThirdRewardStatusEnum(ThirdRewardStatusEnum.REWARD_OVER);
					response.setTicketId(ticketId);
					orders.add(response);
				}
			}
			toStakeResponseDTO.setOrders(orders);
		}
		return toStakeResponseDTO;
	}

	@Override
	public QueryRewardResponseDTO queryRewardByIssue(String issue, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
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

	@Override
	public ToRewardResponseDTO toRewardByLottery(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		// TODO Auto-generated method stub
		return null;
	}
}
