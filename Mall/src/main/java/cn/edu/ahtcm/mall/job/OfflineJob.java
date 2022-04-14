package cn.edu.ahtcm.mall.job;

import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * 离线推荐定时任务
 */
public class OfflineJob extends QuartzJobBean {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    protected void executeInternal(JobExecutionContext context) {
        String cmd = "ping baidu.com";
        logger.info("cmd -> " + cmd);
        Runtime run = Runtime.getRuntime();
        try {
            logger.info(new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date()) + ", 开始执行 " + this.getClass().getName());

            Process process = run.exec(cmd);
            int exeResult = process.waitFor();
            if (exeResult == 0) {
                logger.info(cmd + " -> 执行成功!");
            } else {
                logger.info(cmd + " -> 执行失败!");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
