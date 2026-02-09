package com.jiawa.train.business.controller.admin;

import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.business.DTO.TrainSeatQueryDTO;
import com.jiawa.train.business.DTO.TrainSeatSaveDTO;
import com.jiawa.train.business.VO.TrainSeatQueryVO;
import com.jiawa.train.business.service.TrainSeatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/train-seat")
public class TrainSeatController {

    @Autowired
    private TrainSeatService trainSeatService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody TrainSeatSaveDTO trainSeatSaveDTO){
        trainSeatService.save(trainSeatSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<TrainSeatQueryVO>> queryList(@Valid TrainSeatQueryDTO paseengerQueryDTO){

        PageVO<TrainSeatQueryVO> pageVO = trainSeatService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        trainSeatService.delete(id);
        return new CommonResp<>();
    }

}
