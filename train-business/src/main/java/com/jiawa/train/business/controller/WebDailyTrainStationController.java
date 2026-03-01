package com.jiawa.train.business.controller;

import cn.hutool.core.bean.BeanUtil;
import com.jiawa.train.business.DTO.DailyTrainStationAllDTO;
import com.jiawa.train.business.VO.DailyTrainStationQueryVO;
import com.jiawa.train.business.domain.DailyTrainStation;
import com.jiawa.train.business.service.DailyTrainStationService;
import com.jiawa.train.common.VO.CommonResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/daily-train-station")
public class WebDailyTrainStationController {

    @Resource
    private DailyTrainStationService dailyTrainStationService;


    @GetMapping("/query-by-train-code")
    public CommonResp<List<DailyTrainStationQueryVO>> queryByTrainCode(DailyTrainStationAllDTO dailyTrainStationAllDTO) {
        Date date = dailyTrainStationAllDTO.getDate();
        String trainCode = dailyTrainStationAllDTO.getTrainCode();
        List<DailyTrainStation> dailyTrainStationList =dailyTrainStationService.selectByDateTrain(date, trainCode);
        List<DailyTrainStationQueryVO> dailyTrainStationQueryVOList = BeanUtil.copyToList(dailyTrainStationList, DailyTrainStationQueryVO.class);
        return new CommonResp<>(dailyTrainStationQueryVOList);
    }


}
