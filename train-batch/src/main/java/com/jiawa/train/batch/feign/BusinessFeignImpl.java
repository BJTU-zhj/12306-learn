package com.jiawa.train.batch.feign;

import com.jiawa.train.common.VO.CommonResp;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class BusinessFeignImpl implements BusinessFeign{

    @Override
    public String hello() {
        return "FallBack";
    }

    @Override
    public CommonResp<Object> genDailyTrain(Date date) {
        return null;
    }
}
