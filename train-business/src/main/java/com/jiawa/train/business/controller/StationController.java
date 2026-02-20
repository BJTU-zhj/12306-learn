package com.jiawa.train.business.controller;

import com.jiawa.train.business.VO.StationQueryVO;
import com.jiawa.train.business.service.StationService;
import com.jiawa.train.common.VO.CommonResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/station")
public class StationController {

    @Autowired
    private StationService stationService;

    //获取所有车站
    @GetMapping("/query-all")
    public CommonResp<List<StationQueryVO>> queryAll(){
        List<StationQueryVO> list = stationService.queryAll();
        return new CommonResp<>(list);
    }

}
