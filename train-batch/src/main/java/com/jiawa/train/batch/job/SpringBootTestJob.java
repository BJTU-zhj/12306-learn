package com.jiawa.train.batch.job;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;


/*
 * @Description:自带的定时功能适合单体不是集群，可用分布式锁解决
 * 无法随时停止，只能重启
 */

@EnableScheduling
@Component
public class SpringBootTestJob {

//    @Scheduled(cron="0/5 * * * * ? ")
    public void test(){
        System.out.println("定时任务执行了");
    }

}
