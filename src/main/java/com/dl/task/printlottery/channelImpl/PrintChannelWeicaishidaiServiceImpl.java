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
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.dl.base.enums.ThirdApiEnum;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.MD5Utils;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.enums.PrintLotteryStatusEnum;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.DlTicketChannel;
import com.dl.task.model.LotteryThirdApiLog;
import com.dl.task.printlottery.IPrintChannelService;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO.QueryStakeOrderResponse;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO.ToStakeBackOrderDetail;
import com.dl.task.printlottery.responseDto.weicaishidai.WeiCaiShiDaiDlToStakeDTO;
import com.dl.task.printlottery.responseDto.weicaishidai.WeiCaiShiDaiDlToStakeDTO.WeiCaiShiDaiBackOrderDetail;
import com.dl.task.printlottery.responseDto.weicaishidai.WeiCaiShiDaiQueryStakeDTO;
import com.dl.task.printlottery.responseDto.weicaishidai.WeiCaiShiDaiQueryStakeDTO.WeiCaiShiDaiQueryStakeResponse;
import com.dl.task.printlottery.responseDto.weicaishidai.WeiCaiShiDaiToStakeRetCode;

@Service
@Slf4j
public class PrintChannelWeicaishidaiServiceImpl  implements IPrintChannelService{
	private static Map<String,String> playTypeRelationMap = new HashMap<String, String>();
	private static String CMDSTAKE="CT01";
	private static String CMDQUERYSTAKE="CT03";
	private static String playTypeCTW="CTOW";//彩小秘对微彩时代
	private static String playTypeWTC="WTOC";//微彩时代对彩小秘
	static{
		playTypeRelationMap.put(playTypeCTW+"01", "02");//让球胜平负
		playTypeRelationMap.put(playTypeCTW+"02", "01");//胜平负
		playTypeRelationMap.put(playTypeCTW+"03", "04");//比分
		playTypeRelationMap.put(playTypeCTW+"04", "03");//总进球
		playTypeRelationMap.put(playTypeCTW+"05", "05");//半全场
		playTypeRelationMap.put(playTypeCTW+"06", "10");//混合投注
		playTypeRelationMap.put(playTypeWTC+"02", "01");//让球胜平负
		playTypeRelationMap.put(playTypeWTC+"01", "02");//胜平负
		playTypeRelationMap.put(playTypeWTC+"04", "03");//比分
		playTypeRelationMap.put(playTypeWTC+"03", "04");//总进球
		playTypeRelationMap.put(playTypeWTC+"05", "05");//半全场
		playTypeRelationMap.put(playTypeWTC+"10", "06");//混合投注
	}
	@Override
	public ToStakeResponseDTO toStake(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel,DlPrintLotteryMapper dlPrintLotteryMapper) {
		String body = createBody(CMDSTAKE,dlPrintLotterys);
		String header = createHeader(CMDSTAKE,dlTicketChannel,body);
		String backStr = sendHttpMessage(dlTicketChannel.getTicketUrl(),header,body,dlPrintLotteryMapper);
		JSONObject backJo = JSONObject.fromObject(backStr);
		@SuppressWarnings("rawtypes")
		Map<String,Class> mapClass = new HashMap<String,Class>();
		mapClass.put("err", WeiCaiShiDaiToStakeRetCode.class);
		mapClass.put("tickets", WeiCaiShiDaiBackOrderDetail.class);
		WeiCaiShiDaiDlToStakeDTO dlToStakeDTO = (WeiCaiShiDaiDlToStakeDTO) JSONObject.toBean(backJo, WeiCaiShiDaiDlToStakeDTO.class, mapClass); 
		ToStakeResponseDTO toStakeResponseDTO = new ToStakeResponseDTO();
		toStakeResponseDTO.setRetSucc(Boolean.FALSE);
		if(dlToStakeDTO==null){
			return toStakeResponseDTO;
		}
		toStakeResponseDTO.setRetCode(dlToStakeDTO.getErr().getCode());
		toStakeResponseDTO.setRetDesc(dlToStakeDTO.getErr().getDes());
		if(!CollectionUtils.isEmpty(dlToStakeDTO.getTickets())){
			toStakeResponseDTO.setRetSucc(Boolean.TRUE);
			List<ToStakeBackOrderDetail> orders = new ArrayList<ToStakeResponseDTO.ToStakeBackOrderDetail>();
			 for(WeiCaiShiDaiBackOrderDetail weiCaiShiDaiDetail : dlToStakeDTO.getTickets()){
				 ToStakeBackOrderDetail orderDetail = new ToStakeBackOrderDetail();
				 orderDetail.setErrorCode(Integer.parseInt(weiCaiShiDaiDetail.getCode()));
				 orderDetail.setErrorDesc(weiCaiShiDaiDetail.getMessage());
				 orderDetail.setTicketId(weiCaiShiDaiDetail.getOrderId());
				 Boolean printLotteryDoing =  "0000".equals(weiCaiShiDaiDetail.getCode())||"0016".equals(weiCaiShiDaiDetail.getCode());
				 orderDetail.setPrintLotteryDoing(printLotteryDoing);
				 orders.add(orderDetail);
			 }
			toStakeResponseDTO.setOrders(orders);	
		}
		return toStakeResponseDTO;
	}

	private String sendHttpMessage(String requestUrl,String header, String body,DlPrintLotteryMapper dlPrintLotteryMapper) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		try{
			header= java.net.URLEncoder.encode(header, "UTF-8");
			body = java.net.URLEncoder.encode(body, "UTF-8");
			}catch(Exception e){
				log.error("微彩时代 地址转换异常",e);
			}
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("head", header);
		map.add("body", header);
		String requestParam = map.toString();
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(requestUrl, request , String.class );
		String response = responseEntity.getBody();
//		RestTemplate rest = getRestTemplate();
//		HttpHeaders headers = new HttpHeaders();
//		MediaType type = MediaType.parseMediaType("application/x-www-form-urlencoded");
//		headers.setContentType(type);
//		JSONObject jo = new JSONObject();
//		HttpEntity<JSONObject> requestEntity = new HttpEntity<JSONObject>(jo, headers);
//		try{
//			header= java.net.URLEncoder.encode(header, "UTF-8");
//			body = java.net.URLEncoder.encode(body, "UTF-8");
//			}catch(Exception e){
//				log.error("微彩时代 地址转换异常",e);
//			}
//		String requestParam = "?head="+header+"&body="+body;
//		String requestUrlReal = requestUrl+requestParam;
////		parentLog.info("通用的访问第三方请求reqTime={},url={},header={},requestParams={},",System.currentTimeMillis(),requestUrl,JSONHelper.bean2json(headers),JSONHelper.bean2json(requestEntity));
//		parentLog.info("通用的访问第三方请求reqTime={},url={}",System.currentTimeMillis(),requestUrlReal);
////		String response = rest.postForObject(requestUrl, requestEntity, String.class);
//		Map<String, String> headerParams =new HashMap<String, String>();
//		Map<String, String> requestParams =new HashMap<String, String>();
//		headerParams.put("Content-Type", "application/x-www-form-urlencoded");
//		String response = httpPost(requestUrlReal,headerParams,requestParams,"UTF-8");
		parentLog.info("restreqTime={}, response={}",System.currentTimeMillis(),response);
		LotteryThirdApiLog thirdApiLog = new LotteryThirdApiLog(requestUrl,ThirdApiEnum.WEI_CAI_LOTTERY.getCode(), requestParam, response);
		dlPrintLotteryMapper.saveLotteryThirdApiLog(thirdApiLog);
		return response;
	}

	private String httpPost(String url, Map<String, String> headerParams,
			Map<String, String> requestParams, String urlEncode) {
		String str = null;
		HttpPost httpPost = null;
		HttpClient httpClient =  HttpClientBuilder.create().build();// new DefaultHttpClient();
		try {
			// 参数设置
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entry : requestParams.entrySet()) {
				params.add(new BasicNameValuePair((String) entry.getKey(),
						(String) entry.getValue()));
			}
			httpPost = new HttpPost(url);
			httpPost.setEntity(new UrlEncodedFormEntity(params, urlEncode));

			if (headerParams != null) {
				for (Map.Entry<String, String> entry : headerParams.entrySet()) {
					httpPost.setHeader(entry.getKey(), entry.getValue());
				}
			}
			// reponse header
			HttpResponse response = httpClient.execute(httpPost);
			log.info("statusCode={}",response.getStatusLine().getStatusCode());

			Header[] headers = response.getAllHeaders();
			for (Header header : headers) {
				log.info(header.getName() + ": " + header.getValue());
			}
			// 网页内容
			org.apache.http.HttpEntity httpEntity = response.getEntity();
			str = EntityUtils.toString(httpEntity);
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
		return str;
	}

	private String createHeader(String cmd, DlTicketChannel dlTicketChannel,
			String body) {
		Map<String,String> headerMap = new HashMap<String, String>();
		headerMap.put("cmd", cmd);
		headerMap.put("digestType", "md5");
		headerMap.put("userId", dlTicketChannel.getTicketMerchant());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timeStamp = sdf.format(new Date());
		headerMap.put("timeStamp", timeStamp);
		headerMap.put("userType", "company");
		String md5DigestBerfore = dlTicketChannel.getTicketMerchantPassword()+body+timeStamp;
		String md5After = MD5Utils.MD5(md5DigestBerfore);
		headerMap.put("digest", MD5Utils.MD5(md5After));
		return headerMap.toString();
	}

	private String createBody(String cmd,List<DlPrintLottery> dlPrintLotterys) {
		Map<String,String> body = new HashMap<String, String>();
		if(CMDSTAKE.equals(cmd)){
			List<Map<String,Object>> tickets = new ArrayList<Map<String,Object>>();
			for(DlPrintLottery lottery:dlPrintLotterys){
				Map<String,Object> ticketMap = new HashMap<String, Object>();
				String gameId = getGameId(lottery);
				ticketMap.put("game_id", gameId);
				ticketMap.put("play_type", getWeiCaiShiDaiPlayType(lottery.getTicketId(),lottery.getPlayType()));
				ticketMap.put("bet_type", getBetType(lottery.getBetType()));
				ticketMap.put("out_id", lottery.getTicketId());
				ticketMap.put("multiple", lottery.getTimes());
				ticketMap.put("number", getNumber(lottery.getGame(),lottery.getPlayType(),lottery.getBetType(),lottery.getStakes()));
				ticketMap.put("icount",1);
				ticketMap.put("amount", lottery.getMoney().intValue());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String termCode = sdf.format(new Date());
				ticketMap.put("term_code", termCode);
				tickets.add(ticketMap);
			}
			body.put("tickets", tickets.toString());	
		}else if(CMDQUERYSTAKE.equals(cmd)){
			List<String> ticketIds = dlPrintLotterys.stream().map(print-> print.getTicketId()).collect(Collectors.toList());
			body.put("out_id", ticketIds.toString());	
		}
		body.put("uuid", UUID.randomUUID().toString());
		return body.toString();
	}

	/**
	 * 转换投注类型
	 * @param caiXiaoMiBetType
	 * @return
	 */
	private Object getBetType(String caiXiaoMiBetType) {
		if("11".equals(caiXiaoMiBetType)){//单串
			return "01";
		}else{
			return "02";//串关
		}
	}

	/**
	 * 根据彩小秘玩法获取微彩时代玩法
	 * @param lottery
	 * @return
	 */
	private Object getWeiCaiShiDaiPlayType(String ticketId,String caiXiaoMiPlayType) {
		String weiCaiShiDaiPlayType = playTypeRelationMap.get(playTypeCTW+caiXiaoMiPlayType);
		if(!StringUtils.isEmpty(weiCaiShiDaiPlayType)){
			log.info("ticketId={},彩小秘对应的playType={},转化后微彩时代playType={}",ticketId,caiXiaoMiPlayType,weiCaiShiDaiPlayType);
			return weiCaiShiDaiPlayType;
		}
		log.error("ticketId={},playType={},未能找到对应的微彩时代的palyType",ticketId,caiXiaoMiPlayType);
		return "";
	}

	
	/**
	 * 转换投注信息
	 * @param game
	 * @param betType
	 * @param caixiaomiStatke
	 * @return
	 */
	private static String getNumber(String game,String playType,String betType,String caixiaomiStatke) {
		if("T51".equals(game)&&"06".equals(playType)){//混合投注
			StringBuffer weicaishidaiStake = new StringBuffer();
			String[] issueStakes = caixiaomiStatke.split(";");
			for(String issueStake:issueStakes){				
				String[] stakesArr = issueStake.split("\\|");
				weicaishidaiStake.append(stakesArr[1]);
				weicaishidaiStake.append(":");
				weicaishidaiStake.append(playTypeRelationMap.get(playTypeCTW+stakesArr[0]));
				weicaishidaiStake.append(":");
				weicaishidaiStake.append(stakesArr[2]);
				weicaishidaiStake.append(";");
			}
			weicaishidaiStake.replace(weicaishidaiStake.length()-1, weicaishidaiStake.length(), "\\|");
			weicaishidaiStake.append(betType.substring(0, 1)+"*"+betType.substring(1, 2));
			return weicaishidaiStake.toString();
		}else if("T51".equals(game)){
			StringBuffer weicaishidaiStake = new StringBuffer();
			String[] issueStakes = caixiaomiStatke.split(";");
			log.info("issueStakes={}",Arrays.toString(issueStakes));
			for(String issueStake:issueStakes){				
				String[] stakesArr = issueStake.split("\\|");
				weicaishidaiStake.append(stakesArr[1]);
				weicaishidaiStake.append(":");
				weicaishidaiStake.append(stakesArr[2]);
				weicaishidaiStake.append(";");
			}
			weicaishidaiStake.replace(weicaishidaiStake.length()-1, weicaishidaiStake.length(), "|");
			weicaishidaiStake.append(betType.substring(0, 1)+"*"+betType.substring(1, 2));
			return weicaishidaiStake.toString();
		}else{
			log.error("微彩时代出票，暂未实现的出牌game lottery_game={},betType={}",game,betType);
		}
		return null;
	}

	private String getGameId(DlPrintLottery lottery) {
		String game = lottery.getGame();
		String betType=lottery.getBetType();
		if("T51".equals(game)&&"11".equals(betType)){
			return "202";
		}else if("T51".equals(lottery.getGame())){
			return "201";
		}else{
			log.error("微彩时代出票，暂未实现的出牌game lottery_game={},betType={}",game,betType);
		}
		return null;
	}

	@Override
	public QueryStakeResponseDTO queryStake(List<DlPrintLottery> dlPrintLotterys,DlTicketChannel dlTicketChannel,
			DlPrintLotteryMapper dlPrintLotteryMapper) {
		String body = createBody(CMDQUERYSTAKE, dlPrintLotterys);
		String header = createHeader(CMDQUERYSTAKE, dlTicketChannel, body);
		String response = sendHttpMessage(dlTicketChannel.getTicketUrl(), header, body, dlPrintLotteryMapper);
		JSONObject backJo = JSONObject.fromObject(response);
		@SuppressWarnings("rawtypes")
		Map<String,Class> mapClass = new HashMap<String,Class>();
		mapClass.put("err", WeiCaiShiDaiToStakeRetCode.class);
		mapClass.put("tickets", WeiCaiShiDaiQueryStakeResponse.class);
		WeiCaiShiDaiQueryStakeDTO dlQueryStakeDTO = (WeiCaiShiDaiQueryStakeDTO) JSONObject.toBean(backJo, WeiCaiShiDaiQueryStakeDTO.class, mapClass); 
		QueryStakeResponseDTO queryStakeResponseDto = new QueryStakeResponseDTO();
		queryStakeResponseDto.setQuerySuccess(Boolean.FALSE);
		if(dlQueryStakeDTO==null){
			return queryStakeResponseDto;
		}
		queryStakeResponseDto.setRetCode(dlQueryStakeDTO.getErr().getCode());
		queryStakeResponseDto.setRetDesc(dlQueryStakeDTO.getErr().getDes());
		if(!CollectionUtils.isEmpty(dlQueryStakeDTO.getTickets())){
			queryStakeResponseDto.setQuerySuccess(Boolean.TRUE);
			List<QueryStakeOrderResponse> orders = new ArrayList<QueryStakeResponseDTO.QueryStakeOrderResponse>();
			for(WeiCaiShiDaiQueryStakeResponse weicaishidaiQueryResponse:dlQueryStakeDTO.getTickets()){
				QueryStakeOrderResponse queryStakeOrderResponse = new QueryStakeOrderResponse();
				String printStatus=weicaishidaiQueryResponse.getOrderStatus();
				PrintLotteryStatusEnum statusEnum = PrintLotteryStatusEnum.DOING;
				Boolean querySuccess = Boolean.FALSE;
				if("1".equals(printStatus)){
					statusEnum = PrintLotteryStatusEnum.SUCCESS;
					querySuccess = Boolean.TRUE;
				}else if("3".equals(printStatus)){
					statusEnum = PrintLotteryStatusEnum.FAIL;
					querySuccess = Boolean.TRUE;
				}else if("0".equals(printStatus)){
					statusEnum = PrintLotteryStatusEnum.DOING;
					querySuccess = Boolean.FALSE;
				}else{
					log.error("河南出票查询出现非预定状态printStatus={}",printStatus);
					querySuccess = Boolean.FALSE;	
				}
				queryStakeOrderResponse.setQuerySuccess(querySuccess);
				if(querySuccess){
					queryStakeOrderResponse.setStatusEnum(statusEnum);
					queryStakeOrderResponse.setPrintStatus(Integer.parseInt(printStatus));
					queryStakeOrderResponse.setPlatformId(weicaishidaiQueryResponse.getTicketId());
					queryStakeOrderResponse.setPrintNo(weicaishidaiQueryResponse.getTicketNumber());
					queryStakeOrderResponse.setSp(getCaiXiaoMiSpFromTicketNumber(weicaishidaiQueryResponse.getTicketNumber()));
					queryStakeOrderResponse.setTicketId(weicaishidaiQueryResponse.getOrderId());
					queryStakeOrderResponse.setPrintTime(weicaishidaiQueryResponse.getPrintTime());
					Date printTime = new Date();
					try{
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String printTimeStr = weicaishidaiQueryResponse.getPrintTime();
						printTime = sdf.parse(printTimeStr);
					}catch(Exception e){
						log.error("河南出票时间转化出错 ticketId={},printTimeStr={}",weicaishidaiQueryResponse.getTicketId(),weicaishidaiQueryResponse.getPrintTime());
					}
					queryStakeOrderResponse.setPrintTimeDate(printTime);	
				}
				orders.add(queryStakeOrderResponse);
			}
			queryStakeResponseDto.setOrders(orders);
		}
		return queryStakeResponseDto;
	}

	public static void main(String[] args) {
//		String tikectNUmber = "20180517001:3(2.39),0(2.39),1(2.39);20180517002:3(2.39)|2*1";
//		System.out.println(getCaiXiaoMiSpFromTicketNumber(tikectNUmber));
//		String[] stakesArr = "01|201806111101|3".split("\\|");
//		System.out.println(stakesArr[0]);
		
		String game="T51";
		String playType="01";
		String betType="41";
		String caixiaomiStatke="01|201806111101|3;01|201806111102|3;01|201806122101|3;01|201806122102|3";
		System.out.println(getNumber(game, playType, betType, caixiaomiStatke));
	}
	/**
	 * 将微彩时代的赔率转化为我们的
	 * @param ticketNumber
	 * @return
	 */
	private static String getCaiXiaoMiSpFromTicketNumber(String ticketNumber) {
//		20180517001:3(2.39),0(2.39),1(2.39);20180517002:3(2.39)|2*1
		StringBuffer caiXiaoMiSp = new StringBuffer(); 
		String[] ticketNumArr= ticketNumber.split("\\|");
		String WeiCaiShiDaiSp = ticketNumArr[0];
		String[] isssueAndSps = WeiCaiShiDaiSp.split(";");
		for(String isssueAndSp:isssueAndSps){
			String[] isssueAndSpArr = isssueAndSp.split(":");
			String issue = isssueAndSpArr[0];
			caiXiaoMiSp.append(issue);
			caiXiaoMiSp.append("|");
//			3(2.39),0(2.39),1(2.39)
			for(String onePlayAndSp:isssueAndSpArr[1].split(",")){
				String betCell = onePlayAndSp.substring(0, onePlayAndSp.indexOf("("));
				caiXiaoMiSp.append(betCell);
				caiXiaoMiSp.append("@");
				String betCellSp = onePlayAndSp.substring(onePlayAndSp.indexOf("(")+1, onePlayAndSp.indexOf(")"));
				caiXiaoMiSp.append(betCellSp);
				caiXiaoMiSp.append(",");
			}
			caiXiaoMiSp.deleteCharAt(caiXiaoMiSp.length()-1);
			caiXiaoMiSp.append(";");
		}
		caiXiaoMiSp.deleteCharAt(caiXiaoMiSp.length()-1);
		return caiXiaoMiSp.toString();
	}

	@Override
	public QueryRewardResponseDTO queryRewardByLottery(List<DlPrintLottery> dlPrintLotterys,DlTicketChannel dlTicketChannel,
			DlPrintLotteryMapper dlPrintLotteryMapper) {
		
		return null;
	}

	@Override
	public QueryRewardResponseDTO queryRewardByIssue(String issue,
			DlTicketChannel dlTicketChannel,
			DlPrintLotteryMapper dlPrintLotteryMapper) {
		// TODO Auto-generated method stub
		return null;
	}

}
