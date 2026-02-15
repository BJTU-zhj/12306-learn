package com.jiawa.train.business.controller.admin;

import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.business.DTO.DailyTrainTicketQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainTicketSaveDTO;
import com.jiawa.train.business.VO.DailyTrainTicketQueryVO;
import com.jiawa.train.business.service.DailyTrainTicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/daily-train-ticket")
public class DailyTrainTicketController {

    @Autowired
    private DailyTrainTicketService dailyTrainTicketService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody DailyTrainTicketSaveDTO dailyTrainTicketSaveDTO){
        dailyTrainTicketService.save(dailyTrainTicketSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<DailyTrainTicketQueryVO>> queryList(@Valid DailyTrainTicketQueryDTO paseengerQueryDTO){

        PageVO<DailyTrainTicketQueryVO> pageVO = dailyTrainTicketService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        dailyTrainTicketService.delete(id);
        return new CommonResp<>();
    }

}
