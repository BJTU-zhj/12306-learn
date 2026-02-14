package com.jiawa.train.business.controller.admin;

import com.jiawa.train.business.DTO.DailyTrainStationQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainStationSaveDTO;
import com.jiawa.train.business.VO.DailyTrainStationQueryVO;
import com.jiawa.train.business.service.DailyTrainStationService;
import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/daily-train-station")
public class DailyTrainStationController {

    @Autowired
    private DailyTrainStationService dailyTrainStationService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody DailyTrainStationSaveDTO dailyTrainStationSaveDTO){
        dailyTrainStationService.save(dailyTrainStationSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<DailyTrainStationQueryVO>> queryList(@Valid DailyTrainStationQueryDTO paseengerQueryDTO){

        PageVO<DailyTrainStationQueryVO> pageVO = dailyTrainStationService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        dailyTrainStationService.delete(id);
        return new CommonResp<>();
    }


}
