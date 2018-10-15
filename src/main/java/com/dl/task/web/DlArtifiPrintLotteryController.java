package com.dl.task.web;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.task.model.DlArtifiPrintLottery;
import com.dl.task.service.DlArtifiPrintLotteryService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
* Created by CodeGenerator on 2018/10/15.
*/
@RestController
@RequestMapping("/dl/artifi/print/lottery")
public class DlArtifiPrintLotteryController {
    @Resource
    private DlArtifiPrintLotteryService dlArtifiPrintLotteryService;

    @PostMapping("/add")
    public BaseResult add(DlArtifiPrintLottery dlArtifiPrintLottery) {
        dlArtifiPrintLotteryService.save(dlArtifiPrintLottery);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/delete")
    public BaseResult delete(@RequestParam Integer id) {
        dlArtifiPrintLotteryService.deleteById(id);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/update")
    public BaseResult update(DlArtifiPrintLottery dlArtifiPrintLottery) {
        dlArtifiPrintLotteryService.update(dlArtifiPrintLottery);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/detail")
    public BaseResult detail(@RequestParam Integer id) {
        DlArtifiPrintLottery dlArtifiPrintLottery = dlArtifiPrintLotteryService.findById(id);
        return ResultGenerator.genSuccessResult(null,dlArtifiPrintLottery);
    }

    @PostMapping("/list")
    public BaseResult list(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "0") Integer size) {
        PageHelper.startPage(page, size);
        List<DlArtifiPrintLottery> list = dlArtifiPrintLotteryService.findAll();
        PageInfo pageInfo = new PageInfo(list);
        return ResultGenerator.genSuccessResult(null,pageInfo);
    }
}
