package com.jiawa.train.batch.feign;

import com.jiawa.train.common.VO.CommonResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;

@FeignClient("train-business")
public interface BusinessFeign {

    @GetMapping("/business/hello")
    String hello();

    @GetMapping("/business/admin/daily-train/gen-daily/{date}")
    CommonResp<Object> genDailyTrain(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date );

}
