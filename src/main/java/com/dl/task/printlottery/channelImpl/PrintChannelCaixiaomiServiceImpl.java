package com.dl.task.printlottery.channelImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dl.task.dao.DlPrintLotteryMapper;
import com.dl.task.model.DlPrintLottery;
import com.dl.task.model.DlTicketChannel;
import com.dl.task.printlottery.IPrintChannelService;
import com.dl.task.printlottery.responseDto.QueryRewardResponseDTO;
import com.dl.task.printlottery.responseDto.QueryStakeResponseDTO;
import com.dl.task.printlottery.responseDto.ToStakeResponseDTO;

@Service
public class PrintChannelCaixiaomiServiceImpl implements IPrintChannelService{

	@Override
	public ToStakeResponseDTO toStake(List<DlPrintLottery> dlPrintLotterys, DlTicketChannel dlTicketChannel,DlPrintLotteryMapper dlPrintLotteryMapper) {
		return null;
	}

	@Override
	public QueryStakeResponseDTO queryStake(
			List<DlPrintLottery> dlPrintLotterys,
			DlTicketChannel dlTicketChannel,
			DlPrintLotteryMapper dlPrintLotteryMapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryRewardResponseDTO queryRewardByLottery(
			List<DlPrintLottery> dlPrintLotterys,
			DlTicketChannel dlTicketChannel,
			DlPrintLotteryMapper dlPrintLotteryMapper) {
		// TODO Auto-generated method stub
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
