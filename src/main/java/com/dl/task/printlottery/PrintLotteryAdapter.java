package com.dl.task.printlottery;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.dao.DlTicketChannelLotteryClassifyMapper;
import com.dl.task.dao.DlTicketChannelMapper;
import com.dl.task.dto.PrintChannelInfo;
import com.dl.task.enums.PrintLotteryStatusEnum;
import com.dl.task.enums.ThirdRewardStatusEnum;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.DlTicketChannel;
import com.dl.task.model.DlTicketChannelLotteryClassify;
import com.dl.task.printlottery.channelImpl.PrintChannelCaixiaomiServiceImpl;
import com.dl.task.printlottery.channelImpl.PrintChannelHenanServiceImpl;
import com.dl.task.printlottery.channelImpl.PrintChannelWeicaishidaiServiceImpl;
import com.dl.task.printlottery.channelImpl.PrintChannelXianServiceImpl;
import com.dl.task.printlottery.responseDto.QueryPrintBalanceDTO;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO;

@Service
@Slf4j
public class PrintLotteryAdapter {

	@Resource
	private PrintChannelHenanServiceImpl printChannelHeNanServiceImpl;
	@Resource
	private PrintChannelXianServiceImpl printChannelXianServiceImpl;
	@Resource
	private PrintChannelCaixiaomiServiceImpl printChannelCaixiaomiServiceImpl;
	@Resource
	private PrintChannelWeicaishidaiServiceImpl printChannelWeicaishidaiServiceImpl;
	@Resource
	private DlPrintLotteryMapper dlPrintLotteryMapper;
	@Resource
	private DlTicketChannelMapper dlTicketChannelMapper;
	@Resource
	private DlTicketChannelLotteryClassifyMapper dlTicketChannelLotteryClassifyMapper;
	/**
	 * 投注
	 * @return 
	 */
	public List<DlPrintLottery> getLotteryList(PrintComEnums printComEnums,PrintLotteryStatusEnum lotteryStatus){
		return dlPrintLotteryMapper.lotteryPrintsByUnPrintByChannelId(printComEnums.getPrintChannelId(),lotteryStatus.getStatus());
	}
	public List<DlPrintLottery> getReWardLotteryList(PrintComEnums printComEnums,ThirdRewardStatusEnum thirdRewardStatusEnum) {
		return dlPrintLotteryMapper.selectRewardLotterys(printComEnums.getPrintChannelId(),thirdRewardStatusEnum.getStatus());
	}
	/**
	 * 投注
	 * @return 
	 */
	public DlTicketChannel selectChannelByChannelId(PrintComEnums printComEnums){
		return dlTicketChannelMapper.selectChannelByChannelId(printComEnums.getPrintChannelId());
	}
	
	/**
	 * 投注
	 * @param dlTicketChannel 
	 * @param subList 
	 * @return 
	 */
	public ToStakeResponseDTO toStake(PrintComEnums printComEnums, List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel){
		IPrintChannelService iPrintChannelService = getIPrintChannelServiceImpl(printComEnums);
		return iPrintChannelService.toStake(dlPrintLotterys,dlTicketChannel,dlPrintLotteryMapper);
	}
	
	/**
	 * 查询投注信息
	 * @param dlTicketChannel 
	 * @param subList 
	 */
	public QueryStakeResponseDTO queryStake(PrintComEnums printComEnums, List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel){
		IPrintChannelService iPrintChannelService = getIPrintChannelServiceImpl(printComEnums);
		return iPrintChannelService.queryStake(dlPrintLotterys,dlTicketChannel,dlPrintLotteryMapper);
	}
	
	/**
	 * 主动兑奖 有些公司需要
	 */
	public void award(PrintComEnums printComEnums){
		IPrintChannelService iPrintChannelService = getIPrintChannelServiceImpl(printComEnums);
	}
	public QueryRewardResponseDTO queryLotterysReward(PrintComEnums printComEnums, List<DlPrintLottery> dlPrintLotterys,DlTicketChannel dlTicketChannel) {
		IPrintChannelService iPrintChannelService = getIPrintChannelServiceImpl(printComEnums);
		return iPrintChannelService.queryRewardByLottery(dlPrintLotterys,dlTicketChannel,dlPrintLotteryMapper);
	}
	public QueryRewardResponseDTO queryLotterysRewardByIssue(PrintComEnums printComEnums, String issue,DlTicketChannel dlTicketChannel) {
		IPrintChannelService iPrintChannelService = getIPrintChannelServiceImpl(printComEnums);
		return iPrintChannelService.queryRewardByIssue(issue,dlTicketChannel,dlPrintLotteryMapper);
	}
	
	public PrintChannelInfo getPrintChannelId(Integer lotteryClassifyId,String orderSn,Date minMatchStartTime,BigDecimal ticketMoney){
		log.info("进入出票路由设置 参数lotteryClassifyId={},orderSn={},minMatchStartTime={},ticketMoney={}",lotteryClassifyId,orderSn,minMatchStartTime,ticketMoney);
		PrintChannelInfo printChannelInfo = null;
		List<DlTicketChannelLotteryClassify> isOkChannels = dlTicketChannelLotteryClassifyMapper.selectOpenPrintChanel(lotteryClassifyId,minMatchStartTime,ticketMoney);
		if(!CollectionUtils.isEmpty(isOkChannels)){
			List<Integer> channelIds = isOkChannels.stream().map(channel-> channel.getId()).collect(Collectors.toList());
			log.error("orderSn={},匹配到的所有出票路由集合={}",orderSn,channelIds);
			Integer tailNumber = Integer.parseInt(orderSn.substring(orderSn.length()-4,orderSn.length()));
			int channelIndex = tailNumber%isOkChannels.size();
			DlTicketChannelLotteryClassify classify = isOkChannels.get(channelIndex);
			DlTicketChannel channel = dlTicketChannelMapper.selectChannelByChannelId(classify.getTicketChannelId());
			if(channel==null){
				return printChannelInfo;
			}
			printChannelInfo = new PrintChannelInfo();
			printChannelInfo.setClassify(classify);
			printChannelInfo.setChannel(channel);
			log.error("orderSn={},选中的出票路由id={}",orderSn,channel.getId());
		}else{
			log.error("orderSn={},未找到对应的出票路由",orderSn);
		}
		return printChannelInfo;
	}
	
	public QueryPrintBalanceDTO getBalance(PrintComEnums printComEnums) {
		IPrintChannelService iPrintChannelService = getIPrintChannelServiceImpl(printComEnums);
		DlTicketChannel channel = selectChannelByChannelId(printComEnums);
		QueryPrintBalanceDTO dto = iPrintChannelService.queryBalance(channel,dlPrintLotteryMapper);
		return dto;
	}
	private IPrintChannelService getIPrintChannelServiceImpl(PrintComEnums printComEnums){
		IPrintChannelService iPrintChannelService=null;
		switch(printComEnums){
			case HENAN : iPrintChannelService = printChannelHeNanServiceImpl; break;
			case XIAN : iPrintChannelService = printChannelXianServiceImpl ; break;
			case CAIXIAOMI : iPrintChannelService = printChannelCaixiaomiServiceImpl; break;
			case WEICAISHIDAI : iPrintChannelService = printChannelWeicaishidaiServiceImpl; break;
			default :;
		}
		return iPrintChannelService;
	}
}
