package com.jiawa.train.business.controller.admin;

import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.business.DTO.TrainStationQueryDTO;
import com.jiawa.train.business.DTO.TrainStationSaveDTO;
import com.jiawa.train.business.VO.TrainStationQueryVO;
import com.jiawa.train.business.service.TrainStationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/train-station")
public class TrainStationController {

    @Autowired
    private TrainStationService trainStationService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody TrainStationSaveDTO trainStationSaveDTO){
        trainStationService.save(trainStationSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<TrainStationQueryVO>> queryList(@Valid TrainStationQueryDTO paseengerQueryDTO){

        PageVO<TrainStationQueryVO> pageVO = trainStationService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        trainStationService.delete(id);
        return new CommonResp<>();
    }


}
