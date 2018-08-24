package com.dl.task.printlottery.channelImpl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
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
import org.springframework.util.StringUtils;

import com.dl.base.enums.ThirdApiEnum;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.MD5Utils;
import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.enums.PrintLotteryStatusEnum;
import com.dl.task.enums.ThirdRewardStatusEnum;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.DlTicketChannel;
import com.dl.task.model.LotteryThirdApiLog;
import com.dl.task.printlottery.IPrintChannelService;
import com.dl.task.printlottery.requestDto.weicaishidai.WeiCaiShiDaiBodyRequesDto;
import com.dl.task.printlottery.requestDto.weicaishidai.WeiCaiShiDaiBodyRequesDto.WeiCaiShiDaiBodyTicketRequesDto;
import com.dl.task.printlottery.requestDto.weicaishidai.WeiCaiShiDaiHearRequestDto;
import com.dl.task.printlottery.requestDto.weicaishidai.WeiCaiShiDaiQueryBalanceBodyRequesDto;
import com.dl.task.printlottery.requestDto.weicaishidai.WeiCaiShiDaiQueryBodyRequesDto;
import com.dl.task.printlottery.responseDto.QueryPrintBalanceDTO;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO.QueryRewardOrderResponse;
import com.dl.task.printlottery.responseDto.QueryRewardStatusResponseDTO;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO.QueryStakeOrderResponse;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO.ToStakeBackOrderDetail;
import com.dl.task.printlottery.responseDto.weicaishidai.WeiCaiShiDaiDlToStakeDTO;
import com.dl.task.printlottery.responseDto.weicaishidai.WeiCaiShiDaiDlToStakeDTO.WeiCaiShiDaiBackOrderDetail;
import com.dl.task.printlottery.responseDto.weicaishidai.WeiCaiShiDaiQueryBalanceDTO;
import com.dl.task.printlottery.responseDto.weicaishidai.WeiCaiShiDaiQueryBalanceDTO.WeiCaiShiDaiQueryBalanceResponse;
import com.dl.task.printlottery.responseDto.weicaishidai.WeiCaiShiDaiQueryStakeDTO;
import com.dl.task.printlottery.responseDto.weicaishidai.WeiCaiShiDaiQueryStakeDTO.WeiCaiShiDaiQueryStakeResponse;
import com.dl.task.printlottery.responseDto.weicaishidai.WeiCaiShiDaiToStakeRetCode;

@Service
@Slf4j
public class PrintChannelWeicaishidaiServiceImpl implements IPrintChannelService {
	private static Map<String, String> playTypeRelationMap = new HashMap<String, String>();
	private static Map<String, String> betTypeRelationMap = new HashMap<String, String>();
	private static String CAIXIAOMI_GAME_JZ = "T51";
	private static String CAIXIAOMI_GAME_LETTO = "T01";
	private static String CMDSTAKE = "CT01";
	private static String CMDQUERYSTAKE = "CT03";
	private static String CMDQUERYBALANCE = "CT04";
	private static String playTypeCTW = "CTOW";// 彩小秘对微彩时代
	private static String playTypeWTC = "WTOC";// 微彩时代对彩小秘
	static {
		betTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_JZ + "11", "01");
		betTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_JZ + "21", "02");
		betTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_JZ + "31", "02");
		betTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_JZ + "41", "02");
		betTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_JZ + "51", "02");
		betTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_JZ + "61", "02");
		betTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_JZ + "71", "02");
		betTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_JZ + "81", "02");
		betTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_LETTO + "00", "10");
		betTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_LETTO + "01", "20");
		betTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_LETTO + "02", "30");
		// 竞彩投注彩小秘对微彩对应关系
		playTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_JZ + "01", "02");// 让球胜平负
		playTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_JZ + "02", "01");// 胜平负
		playTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_JZ + "03", "04");// 比分
		playTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_JZ + "04", "03");// 总进球
		playTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_JZ + "05", "05");// 半全场
		playTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_JZ + "06", "10");// 混合投注
		// 微彩投注彩小秘对微彩对应关系
		playTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_LETTO + "00", "10");// 标准
		playTypeRelationMap.put(playTypeCTW + CAIXIAOMI_GAME_LETTO + "05", "60");// 追加投注
		// 竞彩投注微彩对彩小秘对应关系
		playTypeRelationMap.put(playTypeWTC + CAIXIAOMI_GAME_JZ + "02", "01");// 让球胜平负
		playTypeRelationMap.put(playTypeWTC + CAIXIAOMI_GAME_JZ + "01", "02");// 胜平负
		playTypeRelationMap.put(playTypeWTC + CAIXIAOMI_GAME_JZ + "04", "03");// 比分
		playTypeRelationMap.put(playTypeWTC + CAIXIAOMI_GAME_JZ + "03", "04");// 总进球
		playTypeRelationMap.put(playTypeWTC + CAIXIAOMI_GAME_JZ + "05", "05");// 半全场
		playTypeRelationMap.put(playTypeWTC + CAIXIAOMI_GAME_JZ + "10", "06");// 混合投注

	}

	@Override
	public ToStakeResponseDTO toStake(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		WeiCaiShiDaiBodyRequesDto body = createBody(CMDSTAKE, dlPrintLotterys);
		String bodyStr = JSONHelper.bean2json(body);
		WeiCaiShiDaiHearRequestDto header = createHeader(CMDSTAKE, dlTicketChannel, bodyStr);
		String headerStr = JSONHelper.bean2json(header);
		String requestUrlReal = dlTicketChannel.getTicketUrl();
		parentLog.info("通用的访问第三方请求reqTime={},url={}", System.currentTimeMillis(), requestUrlReal);
		Map<String, String> headerParams = new HashMap<String, String>();
		Map<String, String> requestParams = new HashMap<String, String>();
		requestParams.put("head", headerStr);
		requestParams.put("body", bodyStr);
		headerParams.put("Content-Type", "application/x-www-form-urlencoded");
		log.info("head={},body={}", headerStr, bodyStr);
		String response = httpPost(requestUrlReal, headerParams, requestParams, "UTF-8");
		log.info("response={}", response);
		LotteryThirdApiLog thirdApiLog = new LotteryThirdApiLog(requestUrlReal, ThirdApiEnum.WEI_CAI_LOTTERY.getCode(), JSONHelper.bean2json(requestParams), response);
		dlPrintLotteryMapper.saveLotteryThirdApiLog(thirdApiLog);
		JSONObject backJo = JSONObject.fromObject(response);
		@SuppressWarnings("rawtypes")
		Map<String, Class> mapClass = new HashMap<String, Class>();
		mapClass.put("err", WeiCaiShiDaiToStakeRetCode.class);
		mapClass.put("tickets", WeiCaiShiDaiBackOrderDetail.class);
		WeiCaiShiDaiDlToStakeDTO dlToStakeDTO = (WeiCaiShiDaiDlToStakeDTO) JSONObject.toBean(backJo, WeiCaiShiDaiDlToStakeDTO.class, mapClass);
		ToStakeResponseDTO toStakeResponseDTO = new ToStakeResponseDTO();
		toStakeResponseDTO.setRetSucc(Boolean.FALSE);
		if (dlToStakeDTO == null) {
			return toStakeResponseDTO;
		}
		toStakeResponseDTO.setRetCode(dlToStakeDTO.getErr().getCode());
		toStakeResponseDTO.setRetDesc(dlToStakeDTO.getErr().getDes());
		if (!CollectionUtils.isEmpty(dlToStakeDTO.getTickets())) {
			toStakeResponseDTO.setRetSucc(Boolean.TRUE);
			List<ToStakeBackOrderDetail> orders = new ArrayList<ToStakeResponseDTO.ToStakeBackOrderDetail>();
			for (WeiCaiShiDaiBackOrderDetail weiCaiShiDaiDetail : dlToStakeDTO.getTickets()) {
				ToStakeBackOrderDetail orderDetail = new ToStakeBackOrderDetail();
				orderDetail.setErrorCode(Integer.parseInt(weiCaiShiDaiDetail.getCode()));
				orderDetail.setErrorDesc(weiCaiShiDaiDetail.getMessage());
				orderDetail.setTicketId(weiCaiShiDaiDetail.getOrderId());
				Boolean printLotteryDoing = "0000".equals(weiCaiShiDaiDetail.getCode()) || "0016".equals(weiCaiShiDaiDetail.getCode());
				orderDetail.setPrintLotteryDoing(printLotteryDoing);
				orders.add(orderDetail);
			}
			toStakeResponseDTO.setOrders(orders);
		}
		return toStakeResponseDTO;
	}

	@Override
	public QueryStakeResponseDTO queryStake(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		Map<String, String> ticketsAndGameMap = new HashMap<String, String>();
		dlPrintLotterys.forEach(print -> {
			ticketsAndGameMap.put(print.getTicketId(), print.getGame());
		});
		WeiCaiShiDaiQueryBodyRequesDto body = createQueryBody(CMDQUERYSTAKE, dlPrintLotterys);
		String bodyStr = JSONHelper.bean2json(body);
		WeiCaiShiDaiHearRequestDto header = createHeader(CMDQUERYSTAKE, dlTicketChannel, bodyStr);
		String headerStr = JSONHelper.bean2json(header);
		String requestUrlReal = dlTicketChannel.getTicketUrl();
		parentLog.info("通用的访问第三方请求reqTime={},url={}", System.currentTimeMillis(), requestUrlReal);
		Map<String, String> headerParams = new HashMap<String, String>();
		Map<String, String> requestParams = new HashMap<String, String>();
		requestParams.put("head", headerStr);
		requestParams.put("body", bodyStr);
		headerParams.put("Content-Type", "application/x-www-form-urlencoded");
		log.info("head={},body={}", headerStr, bodyStr);
		String response = httpPost(requestUrlReal, headerParams, requestParams, "UTF-8");
		log.info("response={}", response);
		LotteryThirdApiLog thirdApiLog = new LotteryThirdApiLog(requestUrlReal, ThirdApiEnum.WEI_CAI_LOTTERY.getCode(), JSONHelper.bean2json(requestParams), response);
		dlPrintLotteryMapper.saveLotteryThirdApiLog(thirdApiLog);
		JSONObject backJo = JSONObject.fromObject(response);
		@SuppressWarnings("rawtypes")
		Map<String, Class> mapClass = new HashMap<String, Class>();
		mapClass.put("err", WeiCaiShiDaiToStakeRetCode.class);
		mapClass.put("tickets", WeiCaiShiDaiQueryStakeResponse.class);
		WeiCaiShiDaiQueryStakeDTO dlQueryStakeDTO = (WeiCaiShiDaiQueryStakeDTO) JSONObject.toBean(backJo, WeiCaiShiDaiQueryStakeDTO.class, mapClass);
		QueryStakeResponseDTO queryStakeResponseDto = new QueryStakeResponseDTO();
		queryStakeResponseDto.setQuerySuccess(Boolean.FALSE);
		if (dlQueryStakeDTO == null) {
			return queryStakeResponseDto;
		}
		queryStakeResponseDto.setRetCode(dlQueryStakeDTO.getErr().getCode());
		queryStakeResponseDto.setRetDesc(dlQueryStakeDTO.getErr().getDes());
		if (!CollectionUtils.isEmpty(dlQueryStakeDTO.getTickets())) {
			queryStakeResponseDto.setQuerySuccess(Boolean.TRUE);
			List<QueryStakeOrderResponse> orders = new ArrayList<QueryStakeResponseDTO.QueryStakeOrderResponse>();
			for (WeiCaiShiDaiQueryStakeResponse weicaishidaiQueryResponse : dlQueryStakeDTO.getTickets()) {
				QueryStakeOrderResponse queryStakeOrderResponse = new QueryStakeOrderResponse();
				String printStatus = weicaishidaiQueryResponse.getOrderStatus();
				PrintLotteryStatusEnum statusEnum = PrintLotteryStatusEnum.DOING;
				Boolean querySuccess = Boolean.FALSE;
				if ("1".equals(printStatus)) {
					statusEnum = PrintLotteryStatusEnum.SUCCESS;
					querySuccess = Boolean.TRUE;
				} else if ("3".equals(printStatus)) {
					statusEnum = PrintLotteryStatusEnum.FAIL;
					querySuccess = Boolean.TRUE;
				} else if ("0".equals(printStatus)) {
					statusEnum = PrintLotteryStatusEnum.DOING;
					querySuccess = Boolean.FALSE;
				} else {
					log.error("微彩时代出票查询出现非预定状态printStatus={}", printStatus);
					querySuccess = Boolean.FALSE;
				}
				queryStakeOrderResponse.setQuerySuccess(querySuccess);
				if (querySuccess) {
					String ticketId = weicaishidaiQueryResponse.getOrderId();
					queryStakeOrderResponse.setStatusEnum(statusEnum);
					queryStakeOrderResponse.setPrintStatus(Integer.parseInt(printStatus));
					queryStakeOrderResponse.setPlatformId(weicaishidaiQueryResponse.getTicketId());
					queryStakeOrderResponse.setPrintNo(weicaishidaiQueryResponse.getNumber());
					String game = ticketsAndGameMap.get(ticketId);
					if (CAIXIAOMI_GAME_JZ.equals(game)) {
						queryStakeOrderResponse.setSp(getCaiXiaoMiSpFromTicketNumber(weicaishidaiQueryResponse.getNumber()));
					} else {// 不需要赔率设置为""
						queryStakeOrderResponse.setSp("");
					}
					queryStakeOrderResponse.setTicketId(ticketId);
					queryStakeOrderResponse.setPrintTime(weicaishidaiQueryResponse.getPrintTime());
					Date printTime = new Date();
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String printTimeStr = weicaishidaiQueryResponse.getPrintTime();
						printTime = sdf.parse(printTimeStr);
					} catch (Exception e) {
						log.error("微彩时代出票时间转化出错 ticketId={},printTimeStr={}", weicaishidaiQueryResponse.getTicketId(), weicaishidaiQueryResponse.getPrintTime());
					}
					queryStakeOrderResponse.setPrintTimeDate(printTime);
				}
				orders.add(queryStakeOrderResponse);
			}
			queryStakeResponseDto.setOrders(orders);
		}
		return queryStakeResponseDto;
	}

	private String httpPost(String url, Map<String, String> headerParams, Map<String, String> requestParams, String urlEncode) {
		String str = null;
		HttpPost httpPost = null;
		HttpClient httpClient = HttpClientBuilder.create().build();// new
																	// DefaultHttpClient();
		try {
			// 参数设置
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entry : requestParams.entrySet()) {
				params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			// try{
			// url = java.net.URLEncoder.encode(url, "UTF-8");
			// }catch(Exception e){
			// log.error("微彩时代 地址转换异常",e);
			// }
			log.info("httpPostUrl={}", url);
			httpPost = new HttpPost(url);
			httpPost.setEntity(new UrlEncodedFormEntity(params, urlEncode));

			if (headerParams != null) {
				for (Map.Entry<String, String> entry : headerParams.entrySet()) {
					httpPost.setHeader(entry.getKey(), entry.getValue());
				}
			}
			// reponse header
			HttpResponse response = httpClient.execute(httpPost);
			log.info("statusCode={}", response.getStatusLine().getStatusCode());

			org.apache.http.Header[] headers = response.getAllHeaders();
			for (org.apache.http.Header header : headers) {
				log.info(header.getName() + ": " + header.getValue());
			}
			// 网页内容
			org.apache.http.HttpEntity httpEntity = response.getEntity();
			str = EntityUtils.toString(httpEntity);
			log.info("response={}", str);
		} catch (UnsupportedEncodingException e) {
			log.error("", e);
		} catch (ClientProtocolException e) {
			log.error("", e);
		} catch (IOException e) {
			log.error("", e);
		} finally {
			if (httpPost != null) {
				httpPost.abort();
			}
		}
		return str;
	}

	private WeiCaiShiDaiHearRequestDto createHeader(String cmd, DlTicketChannel dlTicketChannel, String body) {
		// Map<String,String> headerMap = new HashMap<String, String>();
		WeiCaiShiDaiHearRequestDto headDto = new WeiCaiShiDaiHearRequestDto();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timeStamp = sdf.format(new Date());
		try {
			timeStamp = java.net.URLEncoder.encode(timeStamp, "UTF-8");
		} catch (Exception e) {
			log.error("微彩时代 地址转换异常", e);
		}
		headDto.setCmd(cmd);
		String md5DigestBerfore = dlTicketChannel.getTicketMerchantPassword() + body + timeStamp;
		log.info("md5Before={}", md5DigestBerfore);
		String md5After = MD5Utils.MD5(md5DigestBerfore);
		log.info("md5After={}", md5After);
		headDto.setDigest(md5After);
		headDto.setDigestType("md5");
		headDto.setTimeStamp(timeStamp);
		headDto.setUserId(dlTicketChannel.getTicketMerchant());
		headDto.setUserType("company");
		return headDto;
	}

	private WeiCaiShiDaiQueryBodyRequesDto createQueryBody(String cmd, List<DlPrintLottery> dlPrintLotterys) {
		WeiCaiShiDaiQueryBodyRequesDto body = new WeiCaiShiDaiQueryBodyRequesDto();
		List<String> ticketIds = dlPrintLotterys.stream().map(print -> print.getTicketId()).collect(Collectors.toList());
		body.setOut_id(ticketIds);
		return body;
	}

	private WeiCaiShiDaiBodyRequesDto createBody(String cmd, List<DlPrintLottery> dlPrintLotterys) {
		WeiCaiShiDaiBodyRequesDto body = new WeiCaiShiDaiBodyRequesDto();
		List<WeiCaiShiDaiBodyTicketRequesDto> tickets = new ArrayList<WeiCaiShiDaiBodyTicketRequesDto>();
		for (DlPrintLottery lottery : dlPrintLotterys) {
			WeiCaiShiDaiBodyTicketRequesDto ticket = new WeiCaiShiDaiBodyTicketRequesDto();
			String gameId = getGameId(lottery);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String termCode = sdf.format(new Date());
			String caiXiaoMiGame = lottery.getGame();
			ticket.setOut_id(lottery.getTicketId());
			ticket.setAmount("" + lottery.getMoney().intValue());
			ticket.setGame_id(gameId);
			ticket.setIcount(getBetNum(lottery.getMoney(), lottery.getTimes()));
			ticket.setMultiple("" + lottery.getTimes());
			ticket.setTerm_code(termCode);
			ticket.setBet_type(getBetType(caiXiaoMiGame, lottery.getBetType()));
			ticket.setNumber(getNumber(caiXiaoMiGame, lottery.getPlayType(), lottery.getBetType(), lottery.getStakes()));
			ticket.setPlay_type(getWeiCaiShiDaiPlayType(caiXiaoMiGame, lottery.getTicketId(), lottery.getPlayType()));
			tickets.add(ticket);
		}
		body.setTickets(tickets);
		body.setUuid(UUID.randomUUID().toString());
		return body;
	}

	/**
	 * 转换投注类型
	 * 
	 * @param caiXiaoMiBetType
	 * @return
	 */
	private String getBetType(String game, String caiXiaoMiBetType) {
		String playTypeMapKey = playTypeCTW + game + caiXiaoMiBetType;
		String weicaishidaiBetType = betTypeRelationMap.get(playTypeMapKey);
		if (!StringUtils.isEmpty(weicaishidaiBetType)) {
			log.info("彩小秘投注方式转为微彩时代投注方式key={},value={}", playTypeMapKey, weicaishidaiBetType);
			return weicaishidaiBetType;
		}
		log.error("彩小秘对微彩时代未知的玩法对应，game={},caiXiaoMiBetType={}", game, caiXiaoMiBetType);
		return "";
	}

	/**
	 * 根据彩小秘玩法获取微彩时代玩法
	 * 
	 * @param game
	 * @param lottery
	 * @return
	 */
	private String getWeiCaiShiDaiPlayType(String game, String ticketId, String caiXiaoMiPlayType) {
		String weiCaiShiDaiPlayType = playTypeRelationMap.get(playTypeCTW + game + caiXiaoMiPlayType);
		if (!StringUtils.isEmpty(weiCaiShiDaiPlayType)) {
			log.info("ticketId={},彩小秘对应的playType={},转化后微彩时代playType={}", ticketId, caiXiaoMiPlayType, weiCaiShiDaiPlayType);
			return weiCaiShiDaiPlayType;
		}
		log.error("ticketId={},game={},playType={},未能找到对应的微彩时代的palyType", ticketId, game, caiXiaoMiPlayType);
		return "";
	}

	/**
	 * 转换投注信息
	 * 
	 * @param game
	 * @param betType
	 * @param caixiaomiStatke
	 * @return
	 */
	private String getNumber(String game, String playType, String betType, String caixiaomiStatke) {
		if (CAIXIAOMI_GAME_LETTO.equals(game)) {
			return caixiaomiStatke;
		} else if (CAIXIAOMI_GAME_JZ.equals(game) && "06".equals(playType)) {// 混合投注
			StringBuffer weicaishidaiStake = new StringBuffer();
			String[] issueStakes = caixiaomiStatke.split(";");
			for (String issueStake : issueStakes) {
				String[] stakesArr = issueStake.split("\\|");
				weicaishidaiStake.append(removeIssueWeekDay(stakesArr[1]));
				weicaishidaiStake.append(":");
				weicaishidaiStake.append(playTypeRelationMap.get(playTypeCTW + stakesArr[0]));
				weicaishidaiStake.append(":");
				weicaishidaiStake.append(stakesArr[2]);
				weicaishidaiStake.append(";");
			}
			weicaishidaiStake.replace(weicaishidaiStake.length() - 1, weicaishidaiStake.length(), "|");
			weicaishidaiStake.append(betType.substring(0, 1) + "*" + betType.substring(1, 2));
			return weicaishidaiStake.toString();
		} else if (CAIXIAOMI_GAME_JZ.equals(game)) {
			StringBuffer weicaishidaiStake = new StringBuffer();
			String[] issueStakes = caixiaomiStatke.split(";");
			log.info("issueStakes={}", Arrays.toString(issueStakes));
			for (String issueStake : issueStakes) {
				String[] stakesArr = issueStake.split("\\|");
				weicaishidaiStake.append(removeIssueWeekDay(stakesArr[1]));
				weicaishidaiStake.append(":");
				weicaishidaiStake.append(stakesArr[2]);
				weicaishidaiStake.append(";");
			}
			weicaishidaiStake.replace(weicaishidaiStake.length() - 1, weicaishidaiStake.length(), "|");
			weicaishidaiStake.append(betType.substring(0, 1) + "*" + betType.substring(1, 2));
			return weicaishidaiStake.toString();
		} else {
			log.error("微彩时代出票，暂未实现的出牌game lottery_game={},betType={}", game, betType);
		}
		return null;
	}

	private String getGameId(DlPrintLottery lottery) {
		String game = lottery.getGame();
		String betType = lottery.getBetType();
		if (CAIXIAOMI_GAME_LETTO.equals(game)) {
			return "200";
		} else if (CAIXIAOMI_GAME_JZ.equals(game) && "11".equals(betType)) {
			return "202";
		} else if (CAIXIAOMI_GAME_JZ.equals(lottery.getGame())) {
			return "201";
		} else {
			log.error("微彩时代出票，暂未实现的出牌game lottery_game={},betType={}", game, betType);
		}
		return null;
	}

	public static void main(String[] args) throws IOException {
		// String tikectNUmber =
		// "20180517001:3(2.39),0(2.39),1(2.39);20180517002:3(2.39)|2*1";
		// System.out.println(getCaiXiaoMiSpFromTicketNumber("20180819080:0(1.65);20180819081:0(1.88)|2*1"));
		// String[] stakesArr = "01|201806111101|3".split("\\|");
		// System.out.println(stakesArr[0]);
		// String game="T51";
		// String playType="01";
		// String betType="41";
		// String
		// caixiaomiStatke="01|201806111101|3;01|201806111102|3;01|201806122101|3;01|201806122102|3";
		// System.out.println(getNumber(game, playType, betType,
		// caixiaomiStatke));
		// System.out.println(removeIssueWeekDay("201808105001"));
		// System.out.println(addIssueWeekDay("20180810001"));
		// System.out.println(getIcount(12000,5));
	}

	/**
	 * 将微彩时代的赔率转化为我们的
	 * 
	 * @param ticketNumber
	 * @return
	 */
	private String getCaiXiaoMiSpFromTicketNumber(String ticketNumber) {
		if (StringUtils.isEmpty(ticketNumber)) {
			return "";// 没有赔率
		}
		// 20180517001:3(2.39),0(2.39),1(2.39);20180517002:3(2.39)|2*1
		StringBuffer caiXiaoMiSp = new StringBuffer();
		String[] ticketNumArr = ticketNumber.split("\\|");
		String WeiCaiShiDaiSp = ticketNumArr[0];
		String[] isssueAndSps = WeiCaiShiDaiSp.split(";");
		for (String isssueAndSp : isssueAndSps) {
			String[] isssueAndSpArr = isssueAndSp.split(":");
			String issue = isssueAndSpArr[0];
			caiXiaoMiSp.append(addIssueWeekDay(issue));
			caiXiaoMiSp.append("|");
			// 3(2.39),0(2.39),1(2.39)
			int spIndex = 1;
			if (isssueAndSpArr.length > 2) {
				spIndex = 2;
			}
			for (String onePlayAndSp : isssueAndSpArr[spIndex].split(",")) {
				String betCell = onePlayAndSp.substring(0, onePlayAndSp.indexOf("("));
				caiXiaoMiSp.append(betCell);
				caiXiaoMiSp.append("@");
				String betCellSp = onePlayAndSp.substring(onePlayAndSp.indexOf("(") + 1, onePlayAndSp.indexOf(")"));
				caiXiaoMiSp.append(betCellSp);
				caiXiaoMiSp.append(",");
			}
			caiXiaoMiSp.deleteCharAt(caiXiaoMiSp.length() - 1);
			caiXiaoMiSp.append(";");
		}
		caiXiaoMiSp.deleteCharAt(caiXiaoMiSp.length() - 1);
		return caiXiaoMiSp.toString();
	}

	@Override
	public QueryRewardResponseDTO queryRewardByLottery(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		WeiCaiShiDaiQueryBodyRequesDto body = createQueryBody(CMDQUERYSTAKE, dlPrintLotterys);
		String bodyStr = JSONHelper.bean2json(body);
		WeiCaiShiDaiHearRequestDto header = createHeader(CMDQUERYSTAKE, dlTicketChannel, bodyStr);
		String headerStr = JSONHelper.bean2json(header);
		String requestUrlReal = dlTicketChannel.getTicketUrl();
		parentLog.info("微彩时代查询奖金reqTime={},url={}", System.currentTimeMillis(), requestUrlReal);
		Map<String, String> headerParams = new HashMap<String, String>();
		Map<String, String> requestParams = new HashMap<String, String>();
		requestParams.put("head", headerStr);
		requestParams.put("body", bodyStr);
		headerParams.put("Content-Type", "application/x-www-form-urlencoded");
		log.info("head={},body={}", headerStr, bodyStr);
		String response = httpPost(requestUrlReal, headerParams, requestParams, "UTF-8");
		log.info("response={}", response);
		LotteryThirdApiLog thirdApiLog = new LotteryThirdApiLog(requestUrlReal, ThirdApiEnum.WEI_CAI_LOTTERY.getCode(), JSONHelper.bean2json(requestParams), response);
		dlPrintLotteryMapper.saveLotteryThirdApiLog(thirdApiLog);
		JSONObject backJo = JSONObject.fromObject(response);
		@SuppressWarnings("rawtypes")
		Map<String, Class> mapClass = new HashMap<String, Class>();
		mapClass.put("err", WeiCaiShiDaiToStakeRetCode.class);
		mapClass.put("tickets", WeiCaiShiDaiQueryStakeResponse.class);
		WeiCaiShiDaiQueryStakeDTO dlQueryStakeDTO = (WeiCaiShiDaiQueryStakeDTO) JSONObject.toBean(backJo, WeiCaiShiDaiQueryStakeDTO.class, mapClass);
		QueryRewardResponseDTO queryRewardResponseDto = new QueryRewardResponseDTO();
		queryRewardResponseDto.setQuerySuccess(Boolean.FALSE);
		if (dlQueryStakeDTO == null) {
			return queryRewardResponseDto;
		}
		queryRewardResponseDto.setRetCode(dlQueryStakeDTO.getErr().getCode());
		queryRewardResponseDto.setRetDesc(dlQueryStakeDTO.getErr().getDes());
		if (!CollectionUtils.isEmpty(dlQueryStakeDTO.getTickets())) {
			queryRewardResponseDto.setQuerySuccess(Boolean.TRUE);
			List<QueryRewardOrderResponse> orders = new ArrayList<QueryRewardOrderResponse>();
			for (WeiCaiShiDaiQueryStakeResponse weicaishidaiQueryResponse : dlQueryStakeDTO.getTickets()) {
				QueryRewardOrderResponse queryRewardOrderResponse = new QueryRewardOrderResponse();
				// #中奖状态, 0未开奖 1未中奖 2已算奖
				String winStatus = weicaishidaiQueryResponse.getWinStatus();
				String ticketId = weicaishidaiQueryResponse.getOrderId();
				Boolean querySuccess = Boolean.FALSE;
				if ("1".equals(winStatus) || "2".equals(winStatus)) {
					querySuccess = Boolean.TRUE;
				} else if ("0".equals(winStatus)) {
					querySuccess = Boolean.FALSE;
				} else {
					log.error("微彩时代出票查询出现非预定状态printStatus={}", winStatus);
					querySuccess = Boolean.FALSE;
				}
				queryRewardOrderResponse.setQuerySuccess(querySuccess);
				if (querySuccess) {
					queryRewardOrderResponse.setTicketId(ticketId);
					queryRewardOrderResponse.setThirdRewardStatusEnum(ThirdRewardStatusEnum.REWARD_OVER);
					BigDecimal prizeMoney = new BigDecimal(weicaishidaiQueryResponse.getActualPrize()).multiply(new BigDecimal("100"));
					queryRewardOrderResponse.setPrizeMoney(prizeMoney.intValue());
				}
				orders.add(queryRewardOrderResponse);
			}
			queryRewardResponseDto.setOrders(orders);
		}
		return queryRewardResponseDto;
	}

	@Override
	public QueryRewardResponseDTO queryRewardByIssue(String issue, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryPrintBalanceDTO queryBalance(DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		WeiCaiShiDaiQueryBalanceBodyRequesDto body = new WeiCaiShiDaiQueryBalanceBodyRequesDto();
		body.setUuid(UUID.randomUUID().toString());
		String bodyStr = JSONHelper.bean2json(body);
		WeiCaiShiDaiHearRequestDto header = createHeader(CMDQUERYBALANCE, dlTicketChannel, bodyStr);
		String headerStr = JSONHelper.bean2json(header);
		String requestUrlReal = dlTicketChannel.getTicketUrl();
		parentLog.info("通用的访问第三方请求reqTime={},url={}", System.currentTimeMillis(), requestUrlReal);
		Map<String, String> headerParams = new HashMap<String, String>();
		Map<String, String> requestParams = new HashMap<String, String>();
		requestParams.put("head", headerStr);
		requestParams.put("body", bodyStr);
		headerParams.put("Content-Type", "application/x-www-form-urlencoded");
		log.info("head={},body={}", headerStr, bodyStr);
		String response = httpPost(requestUrlReal, headerParams, requestParams, "UTF-8");
		log.info("response={}", response);
		// LotteryThirdApiLog thirdApiLog = new
		// LotteryThirdApiLog(requestUrlReal,ThirdApiEnum.WEI_CAI_LOTTERY.getCode(),
		// JSONHelper.bean2json(requestParams), response);
		// dlPrintLotteryMapper.saveLotteryThirdApiLog(thirdApiLog);
		JSONObject backJo = JSONObject.fromObject(response);
		@SuppressWarnings("rawtypes")
		Map<String, Class> mapClass = new HashMap<String, Class>();
		mapClass.put("err", WeiCaiShiDaiToStakeRetCode.class);
		mapClass.put("account", WeiCaiShiDaiQueryBalanceResponse.class);
		WeiCaiShiDaiQueryBalanceDTO dlToStakeDTO = (WeiCaiShiDaiQueryBalanceDTO) JSONObject.toBean(backJo, WeiCaiShiDaiQueryBalanceDTO.class, mapClass);
		QueryPrintBalanceDTO toStakeResponseDTO = new QueryPrintBalanceDTO();
		toStakeResponseDTO.setQuerySuccess(Boolean.FALSE);
		if (dlToStakeDTO == null) {
			return toStakeResponseDTO;
		}
		toStakeResponseDTO.setRetCode(dlToStakeDTO.getErr().getCode());
		toStakeResponseDTO.setRetDesc(dlToStakeDTO.getErr().getDes());
		if (dlToStakeDTO.getAccount() != null && !StringUtils.isEmpty(dlToStakeDTO.getAccount().getBalance())) {
			String balanceStr = dlToStakeDTO.getAccount().getBalance();
			long balanceFen = new BigDecimal(balanceStr).multiply(new BigDecimal("100")).longValue();
			toStakeResponseDTO.setQuerySuccess(Boolean.TRUE);
			toStakeResponseDTO.setBalance(balanceFen);
		}
		return toStakeResponseDTO;
	}

	@Override
	public QueryRewardStatusResponseDTO queryRewardStatusByLottery(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel, DlPrintLotteryMapper dlPrintLotteryMapper) {
		// TODO Auto-generated method stub
		return null;
	}
}
