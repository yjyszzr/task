package com.dl.task.printlottery.channelImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.dl.base.enums.ThirdApiEnum;
import com.dl.base.util.DateUtil;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.enums.PrintLotteryStatusEnum;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.DlTicketChannel;
import com.dl.task.printlottery.IPrintChannelService;
import com.dl.task.printlottery.requestDto.CommonQueryStakeParam;
import com.dl.task.printlottery.requestDto.CommonToQueryBanlanceParam;
import com.dl.task.printlottery.requestDto.CommonToStakeParam;
import com.dl.task.printlottery.requestDto.henan.HeNanQueryPrizeFileParam;
import com.dl.task.printlottery.responseDto.QueryPrintBalanceDTO;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO.QueryRewardOrderResponse;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO.QueryStakeOrderResponse;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO.ToStakeBackOrderDetail;
import com.dl.task.printlottery.responseDto.henan.HeNanDlToStakeDTO;
import com.dl.task.printlottery.responseDto.henan.HeNanDlToStakeDTO.HeNanBackOrderDetail;
import com.dl.task.printlottery.responseDto.henan.HeNanQueryPrizeFileDTO;
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
	@Override
	public QueryRewardResponseDTO queryRewardByLottery(List<DlPrintLottery> dlPrintLotterys,DlTicketChannel dlTicketChannel,
			DlPrintLotteryMapper dlPrintLotteryMapper) {
		QueryRewardResponseDTO notSupport = new QueryRewardResponseDTO();
		notSupport.setQuerySuccess(Boolean.FALSE);
		notSupport.setRetCode("-1");
		notSupport.setRetDesc("notSupprt");
		return notSupport;
	}
	@Override
	public QueryRewardResponseDTO queryRewardByIssue(String issueAndGame,DlTicketChannel dlTicketChannel,DlPrintLotteryMapper dlPrintLotteryMapper) {
		QueryRewardResponseDTO resultDto = new QueryRewardResponseDTO();
		resultDto.setQuerySuccess(Boolean.FALSE);
		String[] issueAndGameArr = issueAndGame.split(";");
		HeNanQueryPrizeFileParam param = new HeNanQueryPrizeFileParam();
		param.setMerchant(dlTicketChannel.getTicketMerchant());
		param.setVersion(version);
		param.setGame(issueAndGameArr[0]);
		param.setIssue(issueAndGameArr[1]);
		param.setTimestamp(DateUtil.getCurrentTimeString(DateUtil.getCurrentTimeLong().longValue(), DateUtil.datetimeFormat));
		JSONObject jo = JSONObject.fromObject(param);
		String backStr = defaultCommonRestRequest(dlTicketChannel, dlPrintLotteryMapper, jo,  "/prize_file", ThirdApiEnum.HE_NAN_LOTTERY);
		JSONObject backJo = JSONObject.fromObject(backStr);
		HeNanQueryPrizeFileDTO dlQueryPrizeFileDTO = (HeNanQueryPrizeFileDTO) JSONObject.toBean(backJo, HeNanQueryPrizeFileDTO.class); 
		if(dlQueryPrizeFileDTO!=null&&StringUtils.isNotEmpty(dlQueryPrizeFileDTO.getUrl())){
			resultDto.setQuerySuccess(Boolean.TRUE);
			List<QueryRewardOrderResponse> ticketIds = new ArrayList<QueryRewardResponseDTO.QueryRewardOrderResponse>();
//			读取文件内容
			try {
				log.info("解析河南出票兑奖信息 文件地址={}",dlQueryPrizeFileDTO.getUrl());
				 URL url = new URL(dlQueryPrizeFileDTO.getUrl());
		        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		        String s;
		        int row=0;
		        while ((s = reader.readLine()) != null) {
		        	log.info("查询河南出奖信息内容为={}",s);
		        	row++;
		        	if(row==1){
		        		continue;
		        	}
		        	String[] printPrizeInfoArr = s.split("\t");
		        	log.info("出奖信息={}",printPrizeInfoArr);
		        	String ticketId = printPrizeInfoArr[1];
		        	String thirdReward = printPrizeInfoArr[3];
		        	QueryRewardOrderResponse response = new QueryRewardOrderResponse();
		        	response.setQuerySuccess(Boolean.TRUE);
		        	response.setPrizeMoney(Integer.parseInt(thirdReward));
		        	response.setTicketId(ticketId);
		        	ticketIds.add(response);
		        }
		        resultDto.setOrders(ticketIds);
		        reader.close();
			} catch (IOException e) {
				log.info("解析河南出票奖金文件失败",e);
			}
		}
		return resultDto;
	}
	@Override
	public QueryPrintBalanceDTO queryBalance(DlTicketChannel dlTicketChannel,DlPrintLotteryMapper dlPrintLotteryMapper) {
		CommonToQueryBanlanceParam querybalance = defaultCommonToQueryBanlanceParam(dlTicketChannel.getTicketMerchant(),version);
		JSONObject jo = JSONObject.fromObject(querybalance);
		String backStr = defaultCommonRestRequest(dlTicketChannel, dlPrintLotteryMapper, jo, "/account", ThirdApiEnum.HE_NAN_LOTTERY);
		JSONObject backJo = JSONObject.fromObject(backStr);
		log.info("河南查询余额返回信息={}",backStr);
		QueryPrintBalanceDTO dlQueryStakeDTO = (QueryPrintBalanceDTO) JSONObject.toBean(backJo, QueryPrintBalanceDTO.class); 
		if(dlQueryStakeDTO==null||dlQueryStakeDTO.getBalance()==null||Integer.valueOf(0).equals(dlQueryStakeDTO.getBalance())){
			dlQueryStakeDTO = new QueryPrintBalanceDTO();
			dlQueryStakeDTO.setQuerySuccess(Boolean.FALSE);
			return dlQueryStakeDTO;
		}
		dlQueryStakeDTO.setQuerySuccess(Boolean.TRUE);
		return dlQueryStakeDTO;
	}
}
