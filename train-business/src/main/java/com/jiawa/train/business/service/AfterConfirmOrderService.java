package com.jiawa.train.business.service;

import cn.hutool.core.date.DateTime;
import com.jiawa.train.business.domain.DailyTrainSeat;
import com.jiawa.train.business.domain.DailyTrainTicket;
import com.jiawa.train.business.mapper.DailyTrainSeatMapper;
import com.jiawa.train.business.mapper.custom.DailyTrainTicketCustomMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AfterConfirmOrderService {

    private static final Logger LOG= LoggerFactory.getLogger(AfterConfirmOrderService.class);

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    @Resource
    private DailyTrainTicketCustomMapper dailyTrainTicketCustomMapper;

    @Transactional
    public void batchOrderTicketsUpdate(DailyTrainTicket dailyTrainTicket,List<DailyTrainSeat> dailyTrainSeatList) {
        for (DailyTrainSeat dailyTrainSeat : dailyTrainSeatList){
            //修改每日座位德 sell字段
            DateTime now= DateTime.now();
            DailyTrainSeat aimDailyTrainSeat = new DailyTrainSeat();
            aimDailyTrainSeat.setId(dailyTrainSeat.getId());
            aimDailyTrainSeat.setSell(dailyTrainSeat.getSell());
            aimDailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.updateByPrimaryKeySelective(aimDailyTrainSeat);

            //修改每日车票的余票信息--找出所有受影响的车票
            // 计算这个站卖出去后，影响了哪些站的余票库存
            // 参照2-3节 如何保证不超卖、不少卖，还要能承受极高的并发 10:30左右
            // 影响的库存：本次选座之前没卖过票的，和本次购买的区间有交集的区间
            // 假设10个站，本次买4~7站
            // 原售：001000001
            // 购买：000011100
            // 新售：001011101
            // 影响：XXX11111X（直到碰到1，就开始不受影响）
            //先获取本次购票的站序
            Integer startStationIndex = dailyTrainTicket.getStartIndex();
            Integer endStationIndex = dailyTrainTicket.getEndIndex();
            //计算受影响的站序最大最小起始站序和最大最小终点站序
            char[] sellChar= dailyTrainSeat.getSell().toCharArray();
            //受影响的最小起始站序,从startStationIndex这个站序往前探索，遇1停止
            Integer minStartIndex=0;
            for (int i=startStationIndex-1;i>=0;i--){
                if(sellChar[i]=='1'){
                    minStartIndex=i+1;
                    break;
                }
            }
            //受影响的最大起始站序，endStationIndex的前一位
            Integer maxStartIndex=endStationIndex-1;
            LOG.info("影响出发站区间：" + minStartIndex + "-" + maxStartIndex);

            //受影响的最小终点站序,startStationIndex的后一位
            Integer minEndIndex=startStationIndex+1;
            Integer maxEndIndex=sellChar.length;
            //受影响的最大终点站序，从endStationIndex这个站序往后探索，遇1停止
            for (int i=endStationIndex;i<sellChar.length;i++){
                if(sellChar[i]=='1'){
                    maxEndIndex=i;
                    break;
                }
            }
            LOG.info("影响到达站区间：" + minEndIndex + "-" + maxEndIndex);

            dailyTrainTicketCustomMapper.updateInfluenceTickets(
                    dailyTrainTicket.getDate(),
                    dailyTrainTicket.getTrainCode(),
                    dailyTrainSeat.getSeatType(),
                    minStartIndex,
                    maxStartIndex,
                    minEndIndex,
                    maxEndIndex);
        }

    }
}
