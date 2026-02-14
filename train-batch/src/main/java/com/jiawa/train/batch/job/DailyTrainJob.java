package com.jiawa.train.batch.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.jiawa.train.batch.feign.BusinessFeign;
import com.jiawa.train.common.VO.CommonResp;
import jakarta.annotation.Resource;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Date;

public class DailyTrainJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainJob.class);

    @Resource
    private BusinessFeign businessFeign;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        MDC.put("LOG_ID", System.currentTimeMillis() + RandomUtil.randomString(3));
        LOG.info("生成15天后的每日车次座位开始");
        Date date = new Date();
        DateTime dateTime= DateUtil.offsetDay(date, 15);
        Date offDay=dateTime.toJdkDate();
        CommonResp<Object> resp = businessFeign.genDailyTrain(offDay);
        LOG.info("生成15天后的 每日车次座位结束,{}",resp);
    }

}
