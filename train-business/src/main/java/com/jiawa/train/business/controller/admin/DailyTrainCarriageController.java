package com.jiawa.train.business.controller.admin;

import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.business.DTO.DailyTrainCarriageQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainCarriageSaveDTO;
import com.jiawa.train.business.VO.DailyTrainCarriageQueryVO;
import com.jiawa.train.business.service.DailyTrainCarriageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/daily-train-carriage")
public class DailyTrainCarriageController {

    @Autowired
    private DailyTrainCarriageService dailyTrainCarriageService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody DailyTrainCarriageSaveDTO dailyTrainCarriageSaveDTO){
        dailyTrainCarriageService.save(dailyTrainCarriageSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<DailyTrainCarriageQueryVO>> queryList(@Valid DailyTrainCarriageQueryDTO paseengerQueryDTO){

        PageVO<DailyTrainCarriageQueryVO> pageVO = dailyTrainCarriageService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        dailyTrainCarriageService.delete(id);
        return new CommonResp<>();
    }

}
