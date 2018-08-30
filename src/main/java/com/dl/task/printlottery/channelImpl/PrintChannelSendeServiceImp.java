package com.dl.task.printlottery.channelImpl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import com.dl.base.enums.ThirdApiEnum;
import com.dl.base.util.DateUtilNew;
import com.dl.base.util.MD5Utils;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.enums.PrintLotteryStatusEnum;
import com.dl.task.enums.ThirdRewardStatusEnum;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.DlTicketChannel;
import com.dl.task.model.LotteryThirdApiLog;
import com.dl.task.printlottery.IPrintChannelService;
import com.dl.task.printlottery.requestDto.sende.BetContentSDParam;
import com.dl.task.printlottery.requestDto.sende.RelationSDUtil;
import com.dl.task.printlottery.requestDto.sende.StakeSDParam;
import com.dl.task.printlottery.requestDto.sende.BetContentSDParam.MatchContentSDParam;
import com.dl.task.printlottery.requestDto.sende.QueryStakeSDParam;
import com.dl.task.printlottery.responseDto.QueryPrintBalanceDTO;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO.QueryRewardOrderResponse;
import com.dl.task.printlottery.responseDto.QueryRewardStatusResponseDTO;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO.QueryStakeOrderResponse;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO.ToStakeBackOrderDetail;
import com.dl.task.printlottery.responseDto.sende.SendeResultToStakeDTO;
import com.dl.task.printlottery.responseDto.sende.SendeQueryBalanceDTO;
import com.dl.task.printlottery.responseDto.sende.SendeQueryBalanceDTO.SendeBalanceMessageDTO;
import com.dl.task.printlottery.responseDto.sende.SendeQueryRewardDTO;
import com.dl.task.printlottery.responseDto.sende.SendeQueryRewardDTO.SendeBonusMessageDTO;
import com.dl.task.printlottery.responseDto.sende.SendeResultMessageDTO;
import com.dl.task.printlottery.responseDto.sende.SendeResultMessageDTO.MatchNumber;
import com.dl.task.printlottery.responseDto.sende.SendeResultMessageDTO.OddsDTO;
import com.dl.task.printlottery.responseDto.sende.SendeResultMessageDTO.SpMap;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
@Service
@Slf4j
public class PrintChannelSendeServiceImp implements IPrintChannelService {
	/*
	 * 出票公司提供的常量数据
	 */
	private static String PUBLIC_KEY = "T03209Z480T2SJQ34S3V8SNG";
	private static String TO_MASSAGE_TYPE = "pushTicketList";
	private static String QUERY_MASSAGE_TYPE = "queryTicketsOrderStatusList";
	private static String BONUS_MASSAGE_TYPE = "queryTicketsOrderBonusList";
	private static String BALANCE_MASSAGE_TYPE = "queryBalance";
	
	
	@Override
	public ToStakeResponseDTO toStake(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel,DlPrintLotteryMapper dlPrintLotteryMapper) {
		StakeSDParam stakeSDParam = this.getStakeSDParam(dlPrintLotterys,dlTicketChannel);
		String content = com.alibaba.fastjson.JSONObject.toJSONString(stakeSDParam.getBetContent());
		Map<String,Object> map = this.creatPostMap(dlTicketChannel, TO_MASSAGE_TYPE,content);
		String backStr = this.httpPost(dlTicketChannel.getTicketUrl()+TO_MASSAGE_TYPE,map,"UTF-8",dlPrintLotteryMapper);
		JSONObject backJo = JSONObject.fromObject(backStr);
		Map<String,Class> mapClass = new HashMap<String,Class>();
		mapClass.put("message", SendeResultMessageDTO.class);
		SendeResultToStakeDTO dlToStakeDTO = (SendeResultToStakeDTO) JSONObject.toBean(backJo, SendeResultToStakeDTO.class, mapClass); 
		ToStakeResponseDTO toStakeResponseDTO = new ToStakeResponseDTO();
		toStakeResponseDTO.setRetSucc(Boolean.FALSE);
		if(dlToStakeDTO==null){
			return toStakeResponseDTO;
		}
		toStakeResponseDTO.setRetCode(dlToStakeDTO.getResultCode());//!!!!
		toStakeResponseDTO.setRetDesc(dlToStakeDTO.getResultCode());
		if(!CollectionUtils.isEmpty(dlToStakeDTO.getMessage())){
			toStakeResponseDTO.setRetSucc(Boolean.TRUE);
			List<ToStakeBackOrderDetail> orders = new ArrayList<ToStakeResponseDTO.ToStakeBackOrderDetail>();
			 for(SendeResultMessageDTO sendeDetail : dlToStakeDTO.getMessage()){
				 ToStakeBackOrderDetail orderDetail = new ToStakeBackOrderDetail();
				 Boolean printLotteryDoing =  sendeDetail.getResult().equals("SUCCESS")||sendeDetail.getResult().equals("ORDER_EXIT_ERROR");
				 if(printLotteryDoing) {
					 orderDetail.setErrorDesc(sendeDetail.getResult());
					 orderDetail.setTicketId(sendeDetail.getTicketId());
					 orderDetail.setPrintLotteryDoing(printLotteryDoing);
				 }else {
					 orderDetail.setErrorCode(5006);//!!!!
					 log.info("森德投注失败result={}",sendeDetail.getResult());
				 }
				 orders.add(orderDetail);
			 }
			toStakeResponseDTO.setOrders(orders);	
		}
		return toStakeResponseDTO;
	}

	@Override
	public QueryStakeResponseDTO queryStake(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel,DlPrintLotteryMapper dlPrintLotteryMapper) {
		QueryStakeSDParam param = new QueryStakeSDParam();
		StringBuffer message = new StringBuffer();
		dlPrintLotterys.forEach(item->{
			message.append(","+item.getTicketId());
		});
		param.setMessage(message.substring(1));
		Map<String,Object> map = this.creatPostMap(dlTicketChannel, QUERY_MASSAGE_TYPE, param.getMessage());
		String backStr = this.httpPost(dlTicketChannel.getTicketUrl()+QUERY_MASSAGE_TYPE,map,"UTF-8",dlPrintLotteryMapper);
		JSONObject backJo = JSONObject.fromObject(backStr);
		Map<String,Class> mapClass = new HashMap<String,Class>();
		mapClass.put("message", SendeResultMessageDTO.class);
		mapClass.put("odds", SpMap.class);
		mapClass.put("spMap", OddsDTO.class);
		mapClass.put("matchNumber", MatchNumber.class);
		SendeResultToStakeDTO dlQueryStakeDTO = (SendeResultToStakeDTO) JSONObject.toBean(backJo, SendeResultToStakeDTO.class, mapClass); 
		QueryStakeResponseDTO queryStakeResponseDto = new QueryStakeResponseDTO();
		queryStakeResponseDto.setQuerySuccess(Boolean.FALSE);
		if(dlQueryStakeDTO==null){
			return queryStakeResponseDto;
		}
		queryStakeResponseDto.setRetCode(dlQueryStakeDTO.getResultCode());
		queryStakeResponseDto.setRetDesc(dlQueryStakeDTO.getResultCode());
		if(!CollectionUtils.isEmpty(dlQueryStakeDTO.getMessage())){
			queryStakeResponseDto.setQuerySuccess(Boolean.TRUE);
			List<QueryStakeOrderResponse> orders = new ArrayList<QueryStakeResponseDTO.QueryStakeOrderResponse>();
			String resultCode = dlQueryStakeDTO.getResultCode();
			Boolean querySuccess = Boolean.FALSE;
			if(resultCode.equals("SUCCESS") || resultCode.equals("ORDER_EXIT_ERROR")) {
				querySuccess = Boolean.TRUE;
			}
			for(SendeResultMessageDTO queryMessage:dlQueryStakeDTO.getMessage()){
				QueryStakeOrderResponse queryStakeOrderResponse = new QueryStakeOrderResponse();
				String result = queryMessage.getResult();
				Integer printStatus = 0;//兼容第三方出票状态 8出票中16成功17失败
				PrintLotteryStatusEnum statusEnum = PrintLotteryStatusEnum.DOING;
				if(result.equals("SUC_TICKET") || result.equals("ORDER_EXIT_ERROR")) {
					statusEnum = PrintLotteryStatusEnum.SUCCESS;
					printStatus = 16;
				}else if(result.equals("SUC_ENTRUST") || result.equals("ING_ENTRUST")){
					statusEnum = PrintLotteryStatusEnum.DOING;
					querySuccess = Boolean.FALSE;
				}else {
					statusEnum = PrintLotteryStatusEnum.FAIL;
					querySuccess = Boolean.TRUE;
					printStatus = 17;
					log.error("森德出票查询出票失败result={}",result);
				}
				queryStakeOrderResponse.setQuerySuccess(querySuccess);
				if(querySuccess) {
					queryStakeOrderResponse.setStatusEnum(statusEnum);
					queryStakeOrderResponse.setPrintStatus(printStatus);
					queryStakeOrderResponse.setPlatformId("");
					queryStakeOrderResponse.setPrintNo(queryMessage.getOrderNumber());
					queryStakeOrderResponse.setTicketId(queryMessage.getTicketId());
					queryStakeOrderResponse.setPrintTime(queryMessage.getSuccessTime());
					Date printTime = new Date();
					try{
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String printTimeStr = queryMessage.getSuccessTime();
						printTimeStr = printTimeStr.replaceAll("/", "-");
						printTime = sdf.parse(printTimeStr);
					}catch(Exception e){
						log.error("森德出票时间转化出错 ticketId={},printTimeStr={}",queryMessage.getTicketId(),queryMessage.getSuccessTime());
					}
					queryStakeOrderResponse.setPrintTimeDate(printTime);	
					List<MatchNumber> marchNumbers = queryMessage.getOdds().getSpMap().getMatchNumber();
					StringBuffer numBuff = new StringBuffer();
					marchNumbers.forEach(item->{
						numBuff.append(";"+addIssueWeekDay(item.getMatchNumber())+"|");//添加第九位
						Map<String,String> val = item.getValue();
						String str ="";
						for(String key:val.keySet()) {
							str = str +","+key+"@"+val.get(key);
						}
						numBuff.append(str.substring(1));
					});
					queryStakeOrderResponse.setSp(numBuff.substring(1));
				}
				orders.add(queryStakeOrderResponse);
			}
			queryStakeResponseDto.setOrders(orders);
		}
		return queryStakeResponseDto;
	}

	@Override
	public QueryRewardResponseDTO queryRewardByLottery(List<DlPrintLottery> dlPrintLotterys,DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		QueryStakeSDParam param = new QueryStakeSDParam();
		StringBuffer message = new StringBuffer();
		dlPrintLotterys.forEach(item->{
			message.append(","+item.getTicketId());
		});
		param.setMessage(message.substring(1));
		Map<String,Object> map = this.creatPostMap(dlTicketChannel, BONUS_MASSAGE_TYPE, param.getMessage());
		String backStr = this.httpPost(dlTicketChannel.getTicketUrl()+BONUS_MASSAGE_TYPE,map,"UTF-8",dlPrintLotteryMapper);
		JSONObject backJo = JSONObject.fromObject(backStr);
		Map<String,Class> mapClass = new HashMap<String,Class>();
		mapClass.put("message", SendeBonusMessageDTO.class);
		SendeQueryRewardDTO dlQueryRewardDTO = (SendeQueryRewardDTO) JSONObject.toBean(backJo, SendeQueryRewardDTO.class, mapClass); 
		QueryRewardResponseDTO queryRewardResponseDto = new QueryRewardResponseDTO();
		queryRewardResponseDto.setQuerySuccess(Boolean.FALSE);
		if(dlQueryRewardDTO==null){
			return queryRewardResponseDto;
		}
		queryRewardResponseDto.setRetCode(dlQueryRewardDTO.getResultCode());
		queryRewardResponseDto.setRetDesc(dlQueryRewardDTO.getResultCode());
		if(!CollectionUtils.isEmpty(dlQueryRewardDTO.getMessage())){
			queryRewardResponseDto.setQuerySuccess(Boolean.TRUE);
			List<QueryRewardOrderResponse> orders = new ArrayList<QueryRewardResponseDTO.QueryRewardOrderResponse>();
			for(SendeBonusMessageDTO bonusMessage : dlQueryRewardDTO.getMessage()) {
				QueryRewardOrderResponse queryRewardOrderResponse = new QueryRewardOrderResponse();
				String ticketId = bonusMessage.getTicketId();
				String result = bonusMessage.getResult();
				Boolean querySuccess = Boolean.FALSE;
				//DISTRIBUTE已派奖,NOT_DISTRIBUTE等待派奖, ORDER_NOT_EXIT_ERROR 订单不存在
				if(result.equals("DISTRIBUTE") || result.equals("NOT_DISTRIBUTE")) {
					querySuccess = Boolean.TRUE;
				}
				queryRewardOrderResponse.setQuerySuccess(querySuccess);
				if(querySuccess){
					queryRewardOrderResponse.setTicketId(ticketId);
					queryRewardOrderResponse.setThirdRewardStatusEnum(ThirdRewardStatusEnum.REWARD_OVER);
					Double prizeMoney = bonusMessage.getAfterTaxBonus()*100;
					queryRewardOrderResponse.setPrizeMoney(prizeMoney.intValue());
				}
				orders.add(queryRewardOrderResponse);
			}
			queryRewardResponseDto.setOrders(orders);
		}
		return queryRewardResponseDto;
	}

	@Override
	public QueryPrintBalanceDTO queryBalance(DlTicketChannel channel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		Map<String,Object> map = creatPostMap(channel,BALANCE_MASSAGE_TYPE,"");
		String backStr = this.httpPost(channel.getTicketUrl()+BALANCE_MASSAGE_TYPE,map,"UTF-8",dlPrintLotteryMapper);
		log.info("森德查询余额返回信息={}",backStr);
		JSONObject backJo = JSONObject.fromObject(backStr);
		JSONObject backJo2 = JSONObject.fromObject(backJo.get("message"));//!!
		backJo.put("message", backJo2);//!!返回参数message的值最外层中有“”，直接转json无法封装对象
		Map<String,Class> mapClass = new HashMap<String,Class>();
		mapClass.put("message", SendeBalanceMessageDTO.class);
		SendeQueryBalanceDTO dlToStakeDTO = (SendeQueryBalanceDTO) JSONObject.toBean(backJo, SendeQueryBalanceDTO.class, mapClass); 
		QueryPrintBalanceDTO dlQueryStakeDTO = new QueryPrintBalanceDTO();
		if(dlToStakeDTO==null||!dlToStakeDTO.getResultCode().endsWith("SUCCESS")||Integer.valueOf(0).equals(dlToStakeDTO.getMessage().getBalance())){
			dlQueryStakeDTO.setQuerySuccess(Boolean.FALSE);
			return dlQueryStakeDTO;
		}
		dlQueryStakeDTO.setBalance(dlToStakeDTO.getMessage().getBalance().longValue());
		dlQueryStakeDTO.setQuerySuccess(Boolean.TRUE);
		return dlQueryStakeDTO;
	}
	/**
	 * 封装投注对象
	 * @param dlPrintLotterys
	 * @param dlTicketChannel
	 * @return
	 */
	private StakeSDParam getStakeSDParam(List<DlPrintLottery> dlPrintLotterys,DlTicketChannel dlTicketChannel) {
		StakeSDParam stakeSDParam = new StakeSDParam();
		List<BetContentSDParam> betContentSDParams = new ArrayList<>();
		dlPrintLotterys.forEach(item->{
			BetContentSDParam param = new BetContentSDParam();
			List<MatchContentSDParam> matchContents = new ArrayList<>();
			param.setMultiple(item.getTimes());
			param.setPlayType(RelationSDUtil.getPlayType((item.getPlayType())));
			param.setPassType(RelationSDUtil.getBetType(item.getBetType()));
			param.setPassMode(item.getBetType().equals("11")?"SINGLE":"PASS");
			param.setSchemeCost((double)item.getMoney().doubleValue()/100);
			param.setOrderStatus("ING_ENTRUST");//固定值
			param.setPassItem("");
			param.setUnits(Integer.parseInt(getBetNum(item.getMoney(),item.getTimes())));//计算注数
			param.setTicketId(item.getTicketId());
			param.setOrderTime(DateUtilNew.getCurrentDateTime());
			String stakes = item.getStakes();
			List<String> stakeList = Arrays.asList(stakes.split(";"));
			stakeList.forEach(stake->{
				String[] sk = stake.split("\\|");
				MatchContentSDParam content = new MatchContentSDParam();
				content.setMatchKey("");
				content.setValue(RelationSDUtil.getValueMap(sk[0], sk[2]));
				content.setMatchNumber(removeIssueWeekDay(item.getIssue())); //去除第九位
				matchContents.add(content);
			});
			param.setMatchContent(matchContents);
			betContentSDParams.add(param);
		});
		stakeSDParam.setBetContent(betContentSDParams);
		return stakeSDParam;
	}
	
	/**
	 * 封装post参数
	 * @param dlTicketChannel 渠道对象
	 * @param messageType 接口类型（固定值）
	 * @param content MD5不同的加密内容
	 * @return
	 */
	private Map<String,Object> creatPostMap(DlTicketChannel dlTicketChannel,String messageType,String content){
		Map<String,Object> map = new HashMap<>();
		StringBuffer buff = new StringBuffer();//加密串
		map.put("cardCode",dlTicketChannel.getTicketMerchant());//账号
		map.put("messageType",messageType);//信息类型
		buff.append("cardCode="+dlTicketChannel.getTicketMerchant()+"$");
		if(messageType.equals(BALANCE_MASSAGE_TYPE)) {//账户余额查询接口
			map.put("pwd",dlTicketChannel.getTicketMerchantPassword());
			map.put("betSoruce",dlTicketChannel.getChannelCode());//第三方渠道编码
			buff.append("betSource="+dlTicketChannel.getChannelCode()+"$");
			buff.append("messageType="+messageType+"$");
			buff.append("pwd="+dlTicketChannel.getTicketMerchantPassword());
		}else {
			map.put("lotteryCode","JCZQ");//彩种
			buff.append("lotteryCode="+"JCZQ"+"$");
			if(messageType.equals(TO_MASSAGE_TYPE)) {//投注接口
				map.put("pwd",dlTicketChannel.getTicketMerchantPassword());
				map.put("betSoruce",dlTicketChannel.getChannelCode());//第三方渠道编码
				map.put("betContent",content);
				buff.append("pwd="+dlTicketChannel.getTicketMerchantPassword()+"$");
				buff.append("betSoruce="+dlTicketChannel.getChannelCode()+"$");
				buff.append("betContent="+content+"$");
			}
			if(messageType.equals(QUERY_MASSAGE_TYPE) ||messageType.equals(BONUS_MASSAGE_TYPE) ) {//查询接口
				map.put("message",content);
				buff.append("message="+content+"$");
				buff.append("messageType="+messageType+"$");
			}
			buff.append("publicKey="+PUBLIC_KEY);
		}
		map.put("key", MD5Utils.MD5(buff.toString()));
		return map;
	}
	/**
	 * 发送请求
	 * @param url 地址
	 * @param requestParams 参数集
	 * @param urlEncode 编码
	 * @return
	 */
	 private String httpPost(String url,Map<String, Object> requestParams, String urlEncode,DlPrintLotteryMapper dlPrintLotteryMapper) {
		 String respContent = null;
		 HttpPost httpPost = null;
	        HttpClient httpClient =  HttpClientBuilder.create().build();// new DefaultHttpClient();
	        try {
	            // 参数设置
	            List<NameValuePair> params = new ArrayList<NameValuePair>();
	            for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
	                params.add(new BasicNameValuePair((String) entry.getKey(), entry.getValue().toString()));
	            }
	            log.info("森德httpPostUrl={}",url);
	            httpPost = new HttpPost(url);
	            httpPost.setEntity(new UrlEncodedFormEntity(params, urlEncode));
	            HttpResponse response = httpClient.execute(httpPost);
	            log.info("森德statusCode={}",response.getStatusLine().getStatusCode());
	            org.apache.http.Header[] headers = response.getAllHeaders();
	            for (org.apache.http.Header header : headers) {
	                log.info(header.getName() + ": " + header.getValue());
	            }
	            // 网页内容
	            org.apache.http.HttpEntity httpEntity = response.getEntity();
	            respContent = EntityUtils.toString(httpEntity);
	            log.info("森德response={}",respContent);
	        } catch (UnsupportedEncodingException e) {
	            log.error("",e);
	        } catch (ClientProtocolException e) {
	            log.error("",e);
	        } catch (IOException e) {
	            log.error("",e);
	        } finally {
	            if (httpPost != null) {
	                httpPost.abort();
	            }
	        }
	        JSONObject requsetParamJSONObject = JSONObject.fromObject(requestParams);
	        if(requsetParamJSONObject!=null){	        	
	        	LotteryThirdApiLog thirdApiLog = new LotteryThirdApiLog(url, ThirdApiEnum.SENDE_LOTTERY.getCode(),requsetParamJSONObject.toString(), respContent);
	        	dlPrintLotteryMapper.saveLotteryThirdApiLog(thirdApiLog);
	        }
			return respContent;
	    }

	 @Override
		public QueryRewardResponseDTO queryRewardByIssue(String issue, DlTicketChannel dlTicketChannel,
				DlPrintLotteryMapper dlPrintLotteryMapper) {
			// TODO Auto-generated method stub
			return null;
		}

	@Override
	public QueryRewardStatusResponseDTO queryRewardStatusByLottery(List<DlPrintLottery> dlPrintLotterys,
			DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		// TODO Auto-generated method stub
		return null;
	}
}
