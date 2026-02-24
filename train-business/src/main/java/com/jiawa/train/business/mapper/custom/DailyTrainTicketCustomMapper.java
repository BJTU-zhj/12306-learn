package com.jiawa.train.business.mapper.custom;

import java.util.Date;

public interface DailyTrainTicketCustomMapper {

    void updateInfluenceTickets(Date  date,String trainCode,String seatTypeCode,Integer minStartIndex,Integer maxStartIndex,Integer minEndIndex,Integer maxEndIndex);
}