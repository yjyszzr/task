package com.dl.task.printlottery;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.dao.DlTicketChannelMapper;
import com.dl.task.enums.PrintLotteryStatusEnum;
import com.dl.task.enums.ThirdRewardStatusEnum;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.DlTicketChannel;
import com.dl.task.printlottery.channelImpl.PrintChannelCaixiaomiServiceImpl;
import com.dl.task.printlottery.channelImpl.PrintChannelHenanServiceImpl;
import com.dl.task.printlottery.channelImpl.PrintChannelWeicaishidaiServiceImpl;
import com.dl.task.printlottery.channelImpl.PrintChannelXianServiceImpl;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO;

@Service
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
	
	/**
	 * 查询账户余额
	 */
	private void queryAccountBalance(PrintComEnums printComEnums){
		IPrintChannelService iPrintChannelService = getIPrintChannelServiceImpl(printComEnums);
		
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
