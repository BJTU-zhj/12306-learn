package com.jiawa.train.business.controller.admin;

import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import com.jiawa.train.business.DTO.TrainCarriageQueryDTO;
import com.jiawa.train.business.DTO.TrainCarriageSaveDTO;
import com.jiawa.train.business.VO.TrainCarriageQueryVO;
import com.jiawa.train.business.service.TrainCarriageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/train-carriage")
public class TrainCarriageController {

    @Autowired
    private TrainCarriageService trainCarriageService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody TrainCarriageSaveDTO trainCarriageSaveDTO){
        trainCarriageService.save(trainCarriageSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<TrainCarriageQueryVO>> queryList(@Valid TrainCarriageQueryDTO paseengerQueryDTO){

        PageVO<TrainCarriageQueryVO> pageVO = trainCarriageService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        trainCarriageService.delete(id);
        return new CommonResp<>();
    }

}
