package com.dl.task.printlottery.channelImpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.dl.base.enums.ThirdApiEnum;
import com.dl.base.util.DateUtil;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.enums.PrintLotteryStatusEnum;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.DlTicketChannel;
import com.dl.task.printlottery.IPrintChannelService;
import com.dl.task.printlottery.requestDto.CommonQueryStakeParam;
import com.dl.task.printlottery.requestDto.CommonToStakeParam;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO.QueryStakeOrderResponse;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO.ToStakeBackOrderDetail;
import com.dl.task.printlottery.responseDto.henan.HeNanDlToStakeDTO;
import com.dl.task.printlottery.responseDto.henan.HeNanDlToStakeDTO.HeNanBackOrderDetail;
import com.dl.task.printlottery.responseDto.henan.HenanQueryStakeResponseDTO;
import com.dl.task.printlottery.responseDto.henan.HenanQueryStakeResponseDTO.HenanQueryStakeOrderResponse;

@Service
@Slf4j
public class PrintChannelHenanServiceImpl  implements IPrintChannelService{

	private String version="1.0";
	@Override
	public ToStakeResponseDTO toStake(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel,DlPrintLotteryMapper dlPrintLotteryMapper) {
		CommonToStakeParam commonToStakeParam = defaultCommonToStakeParam(dlPrintLotterys,dlTicketChannel.getTicketMerchant(),version);
		ToStakeResponseDTO dlToStakeDTO = this.toStakeHenan(commonToStakeParam,dlTicketChannel,dlPrintLotteryMapper);
		return dlToStakeDTO;
	}
	/**
	 * 投注接口（竞彩足球，game参数都是T51）
	 * @return
	 */
	private ToStakeResponseDTO toStakeHenan(CommonToStakeParam param,DlTicketChannel dlTicketChannel,DlPrintLotteryMapper dlPrintLotteryMapper) {
		param.setTimestamp(DateUtil.getCurrentTimeString(DateUtil.getCurrentTimeLong().longValue(), DateUtil.datetimeFormat));
		JSONObject jo = JSONObject.fromObject(param);
		String backStr = defaultCommonRestRequest(dlTicketChannel, dlPrintLotteryMapper, jo, "/stake", ThirdApiEnum.HE_NAN_LOTTERY);
		JSONObject backJo = JSONObject.fromObject(backStr);
		@SuppressWarnings("rawtypes")
		Map<String,Class> mapClass = new HashMap<String,Class>();
		mapClass.put("orders", HeNanBackOrderDetail.class);
		HeNanDlToStakeDTO dlToStakeDTO = (HeNanDlToStakeDTO) JSONObject.toBean(backJo, HeNanDlToStakeDTO.class, mapClass); 
		ToStakeResponseDTO toStakeResponseDTO = new ToStakeResponseDTO();
		toStakeResponseDTO.setRetSucc(Boolean.FALSE);
		if(dlToStakeDTO==null){
			return toStakeResponseDTO;
		}
		toStakeResponseDTO.setRetCode(dlToStakeDTO.getRetCode());
		toStakeResponseDTO.setRetDesc(dlToStakeDTO.getRetDesc());
		if(!CollectionUtils.isEmpty(dlToStakeDTO.getOrders())){
			toStakeResponseDTO.setRetSucc(Boolean.TRUE);
			List<ToStakeBackOrderDetail> orders = new ArrayList<ToStakeResponseDTO.ToStakeBackOrderDetail>();
			 for(HeNanBackOrderDetail heNanDetail : dlToStakeDTO.getOrders()){
				 ToStakeBackOrderDetail orderDetail = new ToStakeBackOrderDetail();
				 orderDetail.setErrorCode(heNanDetail.getErrorCode());
				 orderDetail.setErrorDesc(heNanDetail.getErrorDesc());
				 orderDetail.setPlatformId(heNanDetail.getPlatformId());
				 orderDetail.setTicketId(heNanDetail.getTicketId());
				 Boolean printLotteryDoing =  heNanDetail.getErrorCode()==0||heNanDetail.getErrorCode()==3002;
				 orderDetail.setPrintLotteryDoing(printLotteryDoing);
				 orders.add(orderDetail);
			 }
			toStakeResponseDTO.setOrders(orders);	
		}
		return toStakeResponseDTO;
	}
	@Override
	public QueryStakeResponseDTO queryStake(List<DlPrintLottery> dlPrintLotterys,DlTicketChannel dlTicketChannel,DlPrintLotteryMapper dlPrintLotteryMapper) {
		CommonQueryStakeParam commonQueryStakeParam = defaultCommonQueryStakeParam(dlPrintLotterys, dlTicketChannel.getTicketMerchant(), version);
		JSONObject jo = JSONObject.fromObject(commonQueryStakeParam);
		String backStr = defaultCommonRestRequest(dlTicketChannel, dlPrintLotteryMapper, jo, "/stake_query", ThirdApiEnum.HE_NAN_LOTTERY);
		JSONObject backJo = JSONObject.fromObject(backStr);
		@SuppressWarnings("rawtypes")
		Map<String,Class> mapClass = new HashMap<String,Class>();
		mapClass.put("orders", HenanQueryStakeOrderResponse.class);
		HenanQueryStakeResponseDTO dlQueryStakeDTO = (HenanQueryStakeResponseDTO) JSONObject.toBean(backJo, HenanQueryStakeResponseDTO.class, mapClass); 
		QueryStakeResponseDTO queryStakeResponseDto = new QueryStakeResponseDTO();
		queryStakeResponseDto.setQuerySuccess(Boolean.FALSE);
		if(dlQueryStakeDTO==null){
			return queryStakeResponseDto;
		}
		queryStakeResponseDto.setRetCode(dlQueryStakeDTO.getRetCode());
		queryStakeResponseDto.setRetDesc(dlQueryStakeDTO.getRetDesc());
		if(!CollectionUtils.isEmpty(dlQueryStakeDTO.getOrders())){
			queryStakeResponseDto.setQuerySuccess(Boolean.TRUE);
			List<QueryStakeOrderResponse> orders = new ArrayList<QueryStakeResponseDTO.QueryStakeOrderResponse>();
			for(HenanQueryStakeOrderResponse heNanQueryOrderResponse:dlQueryStakeDTO.getOrders()){
				QueryStakeOrderResponse queryStakeOrderResponse = new QueryStakeOrderResponse();
				Integer printStatus=heNanQueryOrderResponse.getPrintStatus();
				PrintLotteryStatusEnum statusEnum = PrintLotteryStatusEnum.DOING;
				Boolean querySuccess = Boolean.FALSE;
				if(Integer.valueOf(16).equals(printStatus)){
					statusEnum = PrintLotteryStatusEnum.SUCCESS;
					querySuccess = Boolean.TRUE;
				}else if(Integer.valueOf(17).equals(printStatus)){
					statusEnum = PrintLotteryStatusEnum.FAIL;
					querySuccess = Boolean.TRUE;
				}else if(Integer.valueOf(8).equals(printStatus)){
					statusEnum = PrintLotteryStatusEnum.DOING;
					querySuccess = Boolean.FALSE;
				}else{
					log.error("河南出票查询出现非预定状态printStatus={}",printStatus);
					querySuccess = Boolean.FALSE;	
				}
				queryStakeOrderResponse.setQuerySuccess(querySuccess);
				if(querySuccess){
					queryStakeOrderResponse.setStatusEnum(statusEnum);
					queryStakeOrderResponse.setPrintStatus(printStatus);
					queryStakeOrderResponse.setOrderId(heNanQueryOrderResponse.getOrderId());
					queryStakeOrderResponse.setPlatformId(heNanQueryOrderResponse.getPlatformId());
					queryStakeOrderResponse.setPrintNo(heNanQueryOrderResponse.getPrintNo());
					queryStakeOrderResponse.setSp(heNanQueryOrderResponse.getSp());
					queryStakeOrderResponse.setTicketId(heNanQueryOrderResponse.getTicketId());
					queryStakeOrderResponse.setPrintTime(heNanQueryOrderResponse.getPrintTime());
					Date printTime = new Date();
					try{
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String printTimeStr = heNanQueryOrderResponse.getPrintTime();
						printTimeStr = printTimeStr.replaceAll("/", "-");
						printTime = sdf.parse(printTimeStr);
					}catch(Exception e){
						log.error("河南出票时间转化出错 ticketId={},printTimeStr={}",heNanQueryOrderResponse.getTicketId(),heNanQueryOrderResponse.getPrintTime());
					}
					queryStakeOrderResponse.setPrintTimeDate(printTime);	
				}
				orders.add(queryStakeOrderResponse);
			}
			queryStakeResponseDto.setOrders(orders);
		}
		return queryStakeResponseDto;
	}
}
