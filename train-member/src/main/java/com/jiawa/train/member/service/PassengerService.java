package com.jiawa.train.member.service;

import cn.hutool.core.date.DateTime;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.DTO.PassengerSaveDTO;
import com.jiawa.train.member.domain.Passenger;
import com.jiawa.train.member.mapper.PassengerMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class PassengerService {

    @Resource
    private PassengerMapper passengerMapper;

    //保存
    public void save(PassengerSaveDTO passengerSaveDTO){
        DateTime now=DateTime.now();
        Passenger passenger = new Passenger();
        BeanUtils.copyProperties(passengerSaveDTO,passenger);
        passenger.setMemberId(LoginMemberContext.getId());
        passenger.setId(SnowUtil.getSnowflakeId());
        passenger.setCreateTime(now);
        passenger.setUpdateTime(now);
        passengerMapper.insert(passenger);
    }
}
