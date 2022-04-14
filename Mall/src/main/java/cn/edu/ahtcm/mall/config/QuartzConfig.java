package cn.edu.ahtcm.mall.config;

import cn.edu.ahtcm.mall.job.OfflineJob;
import cn.edu.ahtcm.mall.job.StatisticsJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    private static final String JOB_GROUP_NAME = "JOB_GROUP";
    private static final String TRIGGER_GROUP_NAME = "TRIGGER_GROUP";

    /**
     * 定时任务 1
     * 离线推荐 Job（任务详情）
     */
    @Bean
    public JobDetail offlineJobDetail() {
        return JobBuilder.newJob(OfflineJob.class)
                .withIdentity("offlineJobDetail", JOB_GROUP_NAME)
                .storeDurably() // 即使没有 Trigger 关联时，也不需要删除该 JobDetail
                .build();
    }

    /**
     * 定时任务 1
     * 离线推荐 Job（触发器）
     */
    @Bean
    public Trigger offlineJobTrigger() {
        // 0/5 * * * * ?   每隔 5 秒执行一次
        // 0 0 1 * * ?     每天一点执行一次
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule("0 0 1 * * ?");

        //创建触发器
        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(offlineJobDetail())//关联上述的JobDetail
                .withIdentity("offlineJobTrigger", TRIGGER_GROUP_NAME) // 给 Trigger 起个名字
                .withSchedule(cronScheduleBuilder)
                .build();
        return trigger;
    }

    /**
     * 定时任务 2
     * 统计推荐 Job（任务详情）
     */
    @Bean
    public JobDetail statisticsJobDetail() {
        return JobBuilder.newJob(StatisticsJob.class)
                .withIdentity("statisticsJobDetail", JOB_GROUP_NAME)
                .storeDurably() // 即使没有 Trigger 关联时，也不需要删除该 JobDetail
                .build();
    }

    /**
     * 定时任务 2
     * 统计推荐 Job（触发器）
     */
    @Bean
    public Trigger statisticsJobTrigger() {
        // 0/5 * * * * ?   每隔 5 秒执行一次
        // 0 0 1 * * ?     每天01:00执行一次
        // 0 30 0 * * ?    每天00:30执行一次
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule("0 30 0 * * ?");

        //创建触发器
        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(statisticsJobDetail())//关联上述的JobDetail
                .withIdentity("statisticsJobTrigger", TRIGGER_GROUP_NAME) // 给 Trigger 起个名字
                .withSchedule(cronScheduleBuilder)
                .build();
        return trigger;
    }


}
