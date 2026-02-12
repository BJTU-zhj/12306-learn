package com.jiawa.train.batch.controller;

import com.jiawa.train.batch.DTO.CronJobDTO;
import com.jiawa.train.batch.VO.CronJobVO;
import com.jiawa.train.common.VO.CommonResp;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping("/admin/job")
public class JobController {

    private static Logger LOG= LoggerFactory.getLogger(JobController.class);

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @RequestMapping("/run")
    public CommonResp<Object> run(@RequestBody CronJobDTO cronJobDTO) throws  SchedulerException {
        String jobClassName=cronJobDTO.getName();
        String jobGroupName=cronJobDTO.getGroup();
        LOG.info("手动执行任务开始：{}，{}",jobClassName,jobGroupName);
        schedulerFactoryBean.getScheduler().triggerJob(JobKey.jobKey(jobClassName,jobGroupName));
        return new CommonResp<>();
    }

    @RequestMapping("/add")
    public  CommonResp<Object> add(@RequestBody CronJobDTO cronJobDTO){
        String jobClassName=cronJobDTO.getName();
        String jobGroupName=cronJobDTO.getGroup();
        String jobDescription=cronJobDTO.getDescription();
        String cronExpression=cronJobDTO.getCronExpression();
        LOG.info("创建定时任务开始：{}，{}，{}，{}",jobClassName,jobGroupName,jobDescription,cronExpression);
        CommonResp<Object> commonResp=new CommonResp<>();
        try {
            //通过SchedulerFactoryBean获取Scheduler的实例
            Scheduler scheduler=schedulerFactoryBean.getScheduler();
            //创建调度器
            scheduler.start();
            //构建jobDetail信息
            JobDetail jobDetail= JobBuilder.newJob((Class<? extends Job>) Class.forName(jobClassName)).withIdentity(
                    jobClassName,jobGroupName).build();
            //表达式调度构建器（即任务执行时间）
            CronScheduleBuilder scheduleBuilder=CronScheduleBuilder.cronSchedule(cronExpression);
            //按新的cronExpression表达式构建一个新的trigger
            CronTrigger trigger= TriggerBuilder.newTrigger().withIdentity(jobClassName,jobGroupName).withDescription(jobDescription).withSchedule(scheduleBuilder).build();
            scheduler.scheduleJob(jobDetail,trigger);
        } catch (ClassNotFoundException e) {
            LOG.error("创建定时任务失败：{}",e);
            commonResp.setSuccess(false);
            commonResp.setMessage("创建定时任务失败:任务类不存在");
        } catch (SchedulerException e) {
            LOG.error("创建定时任务失败：{}",e);
            commonResp.setSuccess(false);
            commonResp.setMessage("创建定时任务失败:调度器异常");
        }
        LOG.info("创建定时任务结束：{}，{}，{}，{}",jobClassName,jobGroupName,jobDescription,cronExpression);
        return commonResp;
    }

    @RequestMapping("/pause")
    public CommonResp<Object> pause(@RequestBody CronJobDTO cronJobDTO){
        String jobClassName=cronJobDTO.getName();
        String jobGroupName=cronJobDTO.getGroup();
        LOG.info("暂停定时任务开始：{}，{}",jobClassName,jobGroupName);
        CommonResp<Object> commonResp=new CommonResp<>();
        try{
            Scheduler scheduler=schedulerFactoryBean.getScheduler();
            scheduler.pauseJob(JobKey.jobKey(jobClassName,jobGroupName));
        } catch (SchedulerException e) {
            LOG.error("暂停定时任务失败：{}",e);
            commonResp.setSuccess(false);
            commonResp.setMessage("暂停定时任务失败:调度器异常");
        }
        LOG.info("暂停定时任务结束：{}，{}",jobClassName,jobGroupName);
        return commonResp;
    }

    @RequestMapping("/resume")
    public CommonResp<Object> resume(@RequestBody   CronJobDTO cronJobDTO){
        String jobClassName=cronJobDTO.getName();
        String jobGroupName=cronJobDTO.getGroup();
        LOG.info("重启定时任务开始：{}，{}",jobClassName,jobGroupName);
        CommonResp<Object> commonResp=new CommonResp<>();
        try{
            Scheduler scheduler=schedulerFactoryBean.getScheduler();
            scheduler.resumeJob(JobKey.jobKey(jobClassName,jobGroupName));
        } catch (SchedulerException e) {
            LOG.error("重启定时任务失败：{}",e);
            commonResp.setSuccess(false);
            commonResp.setMessage("重启定时任务失败:调度器异常");
        }
        LOG.info("重启定时任务结束：{}，{}",jobClassName,jobGroupName);
        return commonResp;
    }

    @RequestMapping("/reschedule")
    public CommonResp<Object> rescheduls(@RequestBody CronJobDTO cronJobDTO){
        String jobClassName=cronJobDTO.getName();
        String jobGroupName=cronJobDTO.getGroup();
        String jobDescription=cronJobDTO.getDescription();
        String cronExpression=cronJobDTO.getCronExpression();
        LOG.info("重置更新定时任务开始：{}，{}，{}，{}",jobClassName,jobGroupName,jobDescription,cronExpression);
        CommonResp commonResp=new CommonResp<>();
        try {
            Scheduler scheduler=schedulerFactoryBean.getScheduler();
            TriggerKey triggerKey= TriggerKey.triggerKey(jobClassName,jobGroupName);
            CronScheduleBuilder scheduleBuilder=CronScheduleBuilder.cronSchedule(cronExpression);
            CronTriggerImpl trigger1= (CronTriggerImpl) scheduler.getTrigger(triggerKey);
            trigger1.setStartTime(new Date());
            CronTrigger trigger=trigger1;
            //按新的cronExpression表达式构建一个新的trigger
            trigger= trigger.getTriggerBuilder().withIdentity(triggerKey).withDescription(jobDescription).withSchedule(scheduleBuilder).build();
            //按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey,trigger);
        } catch (SchedulerException e) {
            LOG.info("重置更新定时任务失败：{}",e);
            commonResp.setSuccess(false);
            commonResp.setMessage("重置更新定时任务失败:调度器异常");
        }
        LOG.info("重置更新定时任务结束：{}，{}，{}，{}",jobClassName,jobGroupName,jobDescription,cronExpression);
        return commonResp;
    }

    @RequestMapping("/delete")
    public CommonResp<Object> delete(@RequestBody CronJobDTO cronJobDTO){
        String jobClassName=cronJobDTO.getName();
        String jobGroupName=cronJobDTO.getGroup();
        LOG.info("删除定时任务开始：{}，{}",jobClassName,jobGroupName);
        CommonResp<Object> commonResp=new CommonResp<>();
        try {
            Scheduler scheduler=schedulerFactoryBean.getScheduler();
            scheduler.pauseJob(JobKey.jobKey(jobClassName,jobGroupName));
            scheduler.unscheduleJob(TriggerKey.triggerKey(jobClassName,jobGroupName));
            scheduler.deleteJob(JobKey.jobKey(jobClassName,jobGroupName));
        } catch (SchedulerException e) {
            LOG.info("删除定时任务失败：{}",e);
            commonResp.setSuccess(false);
            commonResp.setMessage("删除定时任务失败:调度器异常");
        }
        LOG.info("删除定时任务结束：{}，{}",jobClassName,jobGroupName);
        return commonResp;
    }

    @RequestMapping("/query")
    public CommonResp<Object> query(){
        LOG.info("查询所有定时任务开始");
        CommonResp<Object> commonResp=new CommonResp<>();
        List<CronJobVO> cronJobVOList=new ArrayList<>();
        try{
            Scheduler scheduler=schedulerFactoryBean.getScheduler();
            for(String groupName:scheduler.getJobGroupNames()){
                for(JobKey jobKey:scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))){
                    CronJobVO cronJobVO=new CronJobVO();
                    cronJobVO.setName(jobKey.getName());
                    cronJobVO.setGroup(jobKey.getGroup());
                    //获取job的触发器
                    List<Trigger> triggers=(List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                    CronTrigger cronTrigger=(CronTrigger) triggers.get(0);
                    cronJobVO.setNextFireTime(cronTrigger.getNextFireTime());
                    cronJobVO.setPreFireTime(cronTrigger.getPreviousFireTime());
                    cronJobVO.setCronExpression(cronTrigger.getCronExpression());
                    cronJobVO.setCronExpression(cronTrigger.getCronExpression());
                    Trigger.TriggerState tariggerState=scheduler.getTriggerState(cronTrigger.getKey());
                    cronJobVO.setState(tariggerState.name());

                    cronJobVOList.add(cronJobVO);
                }
            }
        } catch (SchedulerException e) {
            LOG.info("查询所有定时任务失败：{}",e);
            commonResp.setSuccess(false);
            commonResp.setMessage("查询所有定时任务失败:调度器异常");
        }
        commonResp.setContent(cronJobVOList);
        LOG.info("查询所有定时任务结束");
        return commonResp;
    }

}
