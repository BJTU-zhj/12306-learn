package com.jiawa.train.business.service;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.jiawa.train.business.DTO.ConfirmOrderTicketDTO;
import com.jiawa.train.business.domain.ConfirmOrder;
import com.jiawa.train.business.domain.DailyTrainSeat;
import com.jiawa.train.business.domain.DailyTrainTicket;
import com.jiawa.train.business.feign.MemberFeign;
import com.jiawa.train.business.mapper.DailyTrainSeatMapper;
import com.jiawa.train.business.mapper.custom.DailyTrainTicketCustomMapper;
import com.jiawa.train.common.DTO.MemberTicketDTO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AfterConfirmOrderService {

    private static final Logger LOG= LoggerFactory.getLogger(AfterConfirmOrderService.class);

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    @Resource
    private DailyTrainTicketCustomMapper dailyTrainTicketCustomMapper;

    @Resource
    private MemberFeign memberFeign;

    /*
    dailyTrainTicket:满足当前订单的车票（某站到某站的余票详情）
    dailyTrainSeatList:当前订单所选座位（某站到某站的多个座位）
    confirmOrder:business当前订单信息，前端传递的主要信息，其中tickets为选出dailyTrainSeatList的依据。顺序一致
     */
    @Transactional
    public void batchOrderTicketsUpdate(DailyTrainTicket dailyTrainTicket, List<DailyTrainSeat> dailyTrainSeatList, ConfirmOrder confirmOrder) {
        //获取车票
        String ticketsJson = confirmOrder.getTickets();
        List<ConfirmOrderTicketDTO> ticketList= JSON.parseArray(ticketsJson, ConfirmOrderTicketDTO.class);

        for(int seatIndex=0;seatIndex<dailyTrainSeatList.size();seatIndex++){
            //修改每日座位德 sell字段
            DateTime now= DateTime.now();
            DailyTrainSeat aimDailyTrainSeat = new DailyTrainSeat();
            aimDailyTrainSeat.setId(dailyTrainSeatList.get(seatIndex).getId());
            aimDailyTrainSeat.setSell(dailyTrainSeatList.get(seatIndex).getSell());
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
            char[] sellChar= dailyTrainSeatList.get(seatIndex).getSell().toCharArray();
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
                    dailyTrainSeatList.get(seatIndex).getSeatType(),
                    minStartIndex,
                    maxStartIndex,
                    minEndIndex,
                    maxEndIndex);

            //保存订单信息到member端
            MemberTicketDTO memberTicketDTO = new MemberTicketDTO();
            memberTicketDTO.setMemberId(confirmOrder.getMemberId());
            memberTicketDTO.setPassengerId(ticketList.get(seatIndex).getPassengerId());
            memberTicketDTO.setPassengerName(ticketList.get(seatIndex).getPassengerName());
            memberTicketDTO.setTrainDate(dailyTrainSeatList.get(seatIndex).getDate());
            memberTicketDTO.setTrainCode(confirmOrder.getTrainCode());
            memberTicketDTO.setCarriageIndex(dailyTrainSeatList.get(seatIndex).getCarriageIndex());
            memberTicketDTO.setSeatRow(dailyTrainSeatList.get(seatIndex).getRow());
            memberTicketDTO.setSeatCol(dailyTrainSeatList.get(seatIndex).getCol());
            memberTicketDTO.setStartStation(confirmOrder.getStart());
            memberTicketDTO.setStartTime(dailyTrainTicket.getStartTime());
            memberTicketDTO.setEndStation(dailyTrainTicket.getEnd());
            memberTicketDTO.setEndTime(dailyTrainTicket.getEndTime());
            memberTicketDTO.setSeatType(ticketList.get(seatIndex).getSeatTypeCode());
            memberTicketDTO.setCreateTime(new Date());
            memberTicketDTO.setUpdateTime(new Date());

            memberFeign.saveConfirm(memberTicketDTO);


        }

    }
}
