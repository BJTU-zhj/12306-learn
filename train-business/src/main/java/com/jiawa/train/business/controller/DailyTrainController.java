package com.jiawa.train.business.controller;

import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.business.DTO.DailyTrainQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainSaveDTO;
import com.jiawa.train.business.VO.DailyTrainQueryVO;
import com.jiawa.train.business.service.DailyTrainService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/daily-train")
public class DailyTrainController {

    @Autowired
    private DailyTrainService dailyTrainService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody DailyTrainSaveDTO dailyTrainSaveDTO){
        dailyTrainService.save(dailyTrainSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<DailyTrainQueryVO>> queryList(@Valid DailyTrainQueryDTO paseengerQueryDTO){

        PageVO<DailyTrainQueryVO> pageVO = dailyTrainService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        dailyTrainService.delete(id);
        return new CommonResp<>();
    }

}
