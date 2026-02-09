package com.jiawa.train.business.controller.admin;

import com.jiawa.train.business.DTO.TrainQueryDTO;
import com.jiawa.train.business.DTO.TrainSaveDTO;
import com.jiawa.train.business.VO.TrainQueryVO;
import com.jiawa.train.business.service.TrainService;
import com.jiawa.train.common.VO.CommonResp;
import com.jiawa.train.common.VO.PageVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/train")
public class TrainController {

    @Autowired
    private TrainService trainService;

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody TrainSaveDTO trainSaveDTO){
        trainService.save(trainSaveDTO);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageVO<TrainQueryVO>> queryList(@Valid TrainQueryDTO paseengerQueryDTO){

        PageVO<TrainQueryVO> pageVO = trainService.queryList(paseengerQueryDTO);
        return new CommonResp<>(pageVO);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        trainService.delete(id);
        return new CommonResp<>();
    }

}
