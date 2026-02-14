package com.jiawa.train.business.controller.admin;

import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.business.DTO.DailyTrainSeatQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainSeatSaveDTO;
import com.jiawa.train.business.VO.DailyTrainSeatQueryVO;
import com.jiawa.train.business.service.DailyTrainSeatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/daily-train-seat")
public class DailyTrainSeatController {

    @Autowired
    private DailyTrainSeatService dailyTrainSeatService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody DailyTrainSeatSaveDTO dailyTrainSeatSaveDTO){
        dailyTrainSeatService.save(dailyTrainSeatSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<DailyTrainSeatQueryVO>> queryList(@Valid DailyTrainSeatQueryDTO paseengerQueryDTO){

        PageVO<DailyTrainSeatQueryVO> pageVO = dailyTrainSeatService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        dailyTrainSeatService.delete(id);
        return new CommonResp<>();
    }

}
