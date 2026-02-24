package com.jiawa.train.business.service;

import cn.hutool.core.date.DateTime;
import com.jiawa.train.business.domain.DailyTrainSeat;
import com.jiawa.train.business.mapper.DailyTrainSeatMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AfterConfirmOrderService {

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    @Transactional
    public void batchOrderSellUpdate(List<DailyTrainSeat> dailyTrainSeatList) {
        for (DailyTrainSeat dailyTrainSeat : dailyTrainSeatList){
            DateTime now= DateTime.now();
            DailyTrainSeat aimDailyTrainSeat = new DailyTrainSeat();
            aimDailyTrainSeat.setId(dailyTrainSeat.getId());
            aimDailyTrainSeat.setSell(dailyTrainSeat.getSell());
            aimDailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.updateByPrimaryKeySelective(aimDailyTrainSeat);
        }

    }
}
