package com.jiawa.train.business.controller.admin;

import com.jiawa.train.business.DTO.DailyTrainQueryDTO;
import com.jiawa.train.business.DTO.DailyTrainSaveDTO;
import com.jiawa.train.business.VO.DailyTrainQueryVO;
import com.jiawa.train.business.service.DailyTrainService;
import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

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

    //测试springboot内置缓存的接口
    @GetMapping("/query-list2")
    public CommonResp<PageVO<DailyTrainQueryVO>> queryList2(@Valid DailyTrainQueryDTO paseengerQueryDTO){

        PageVO<DailyTrainQueryVO> pageVO = dailyTrainService.queryList2(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        dailyTrainService.delete(id);
        return new CommonResp<>();
    }

    //给feign调用的
    @GetMapping("gen-daily/{date}")
    public CommonResp<Object> genDailyTrain(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date){
        dailyTrainService.genDaily(date);
        return new CommonResp<>();
    }

}
