package com.jiawa.train.business.controller;

import com.jiawa.train.business.VO.TrainQueryVO;
import com.jiawa.train.business.service.TrainService;
import com.jiawa.train.common.VO.CommonResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/train")
public class TrainController {

    @Autowired
    private TrainService trainService;


    //查询当前所有车次
    @GetMapping("/query-all")
    public CommonResp<List<TrainQueryVO>> queryList(){
        return new CommonResp<>(trainService.queryALL());
    }

}
