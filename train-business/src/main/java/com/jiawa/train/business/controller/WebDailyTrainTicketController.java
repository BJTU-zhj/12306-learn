package com.jiawa.train.business.controller;

import com.jiawa.train.business.DTO.DailyTrainTicketQueryDTO;
import com.jiawa.train.business.VO.DailyTrainTicketQueryVO;
import com.jiawa.train.business.service.DailyTrainTicketService;
import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/daily-train-ticket")
public class WebDailyTrainTicketController {

    @Autowired
    private DailyTrainTicketService dailyTrainTicketService;


    @GetMapping("/query-list")
    public CommonResp<PageVO<DailyTrainTicketQueryVO>> queryList(@Valid DailyTrainTicketQueryDTO paseengerQueryDTO){

        PageVO<DailyTrainTicketQueryVO> pageVO = dailyTrainTicketService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

}
