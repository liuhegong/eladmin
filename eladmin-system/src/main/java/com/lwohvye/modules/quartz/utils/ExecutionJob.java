/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.lwohvye.modules.quartz.utils;

import cn.hutool.core.util.StrUtil;
import com.lwohvye.config.thread.ThreadPoolExecutorUtil;
import com.lwohvye.modules.quartz.domain.QuartzJob;
import com.lwohvye.modules.quartz.domain.QuartzLog;
import com.lwohvye.modules.quartz.service.IQuartzJobService;
import com.lwohvye.utils.MailAdapter;
import com.lwohvye.utils.SpringContextHolder;
import com.lwohvye.utils.StringUtils;
import com.lwohvye.utils.ThrowableUtil;
import com.lwohvye.utils.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 参考人人开源，https://gitee.com/renrenio/renren-security
 *
 * @author /
 * @date 2019-01-07
 */
@Async
@Slf4j
public class ExecutionJob extends QuartzJobBean {

    /**
     * 该处仅供参考
     */
    private static final ThreadPoolExecutor EXECUTOR = ThreadPoolExecutorUtil.getPoll();

    @Override
    public void executeInternal(JobExecutionContext context) {
        QuartzJob quartzJob = (QuartzJob) context.getMergedJobDataMap().get(QuartzJob.JOB_KEY);
        // 获取spring bean
        IQuartzJobService quartzJobService = SpringContextHolder.getBean(IQuartzJobService.class);
        RedisUtils redisUtils = SpringContextHolder.getBean(RedisUtils.class);

        String uuid = quartzJob.getUuid();

        QuartzLog quartzLog = new QuartzLog();
        quartzLog.setJobName(quartzJob.getJobName());
        quartzLog.setBeanName(quartzJob.getBeanName());
        quartzLog.setMethodName(quartzJob.getMethodName());
        quartzLog.setParams(quartzJob.getParams());
        long startTime = System.currentTimeMillis();
        quartzLog.setCronExpression(quartzJob.getCronExpression());
        try {
            // 执行任务
            QuartzRunnable task = new QuartzRunnable(quartzJob.getBeanName(), quartzJob.getMethodName(), quartzJob.getParams());
            Future<?> future = EXECUTOR.submit(task);
            future.get();
            long times = System.currentTimeMillis() - startTime;
            quartzLog.setTime(times);
            if (StringUtils.isNotBlank(uuid)) {
                redisUtils.set(uuid, true);
            }
            // 任务状态
            quartzLog.setIsSuccess(true);
            // 判断是否存在子任务
            if (StrUtil.isNotBlank(quartzJob.getSubTask())) {
                String[] tasks = quartzJob.getSubTask().split("[,，]");
                // 执行子任务
                quartzJobService.executionSubJob(tasks);
            }
        } catch (Exception e) {
            if (StringUtils.isNotBlank(uuid)) {
                redisUtils.set(uuid, false);
            }
            long times = System.currentTimeMillis() - startTime;
            quartzLog.setTime(times);
            // 任务状态 0：成功 1：失败
            quartzLog.setIsSuccess(false);
            quartzLog.setExceptionDetail(ThrowableUtil.getStackTrace(e));
            // 任务如果失败了则暂停
            if (quartzJob.getPauseAfterFailure() != null && quartzJob.getPauseAfterFailure()) {
                quartzJob.setIsPause(false);
                //更新状态
                quartzJobService.updateIsPause(quartzJob);
            }
            // 邮箱报警
            if (StrUtil.isNotBlank(quartzJob.getEmail())) {
                var to = quartzJob.getEmail();
                var subject = "定时任务【" + quartzJob.getJobName() + "】执行失败，请尽快处理！";
                var templateName = "email/taskAlarm.ftl";
                Map<String, Object> paramsMap = Map.of("task", quartzJob, "msg", ThrowableUtil.getStackTrace(e)); // 这里用var类型推断，会是Map<String, Serializable>
                var res = MailAdapter.sendTemplatedMail(to, subject, templateName, paramsMap);
                log.error("Task Error，Name {} || Reason {} || NoticeRes {} ", quartzJob.getJobName(), e.getMessage(), res);
            }
//            执行失败再记录日志
            quartzJobService.saveLog(quartzLog);
        }
    }
}
