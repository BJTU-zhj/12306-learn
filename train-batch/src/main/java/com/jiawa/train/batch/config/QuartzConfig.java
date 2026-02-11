package com.jiawa.train.batch.config;

import com.jiawa.train.batch.job.TestJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    /*
        声明一个任务
     */
    @Bean
    public JobDetail jobDetail(){
        return JobBuilder.newJob(TestJob.class).withIdentity(
                "TestJob","Test").storeDurably().build();
    }

    /*
        声明一个触发器
     */
//    @Bean
    public Trigger trigger(){
        //cron表达式
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0/5 * * * * ?");
        return TriggerBuilder.newTrigger().forJob(jobDetail()).withIdentity(
                "trigger","trigger").withSchedule(scheduleBuilder).build();
    }


}
