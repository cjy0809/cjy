package com.community.center.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.center.entity.Activity;
import com.community.center.mapper.ActivityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ActivityStatusUpdateTask {

    private static final Logger logger = LoggerFactory.getLogger(ActivityStatusUpdateTask.class);

    @Autowired
    private ActivityMapper activityMapper;

    @Scheduled(fixedRate = 60000)
    public void autoUpdateActivityStatus() {
        try {
            logger.info("开始执行活动状态自动更新任务");

            LocalDateTime now = LocalDateTime.now();

            // 将状态为未开始(0)且开始时间已到的活动更新为进行中(1)
            LambdaUpdateWrapper<Activity> toInProgressWrapper = new LambdaUpdateWrapper<>();
            toInProgressWrapper.eq(Activity::getStatus, 0)
                    .le(Activity::getStartTime, now)
                    .gt(Activity::getEndTime, now)
                    .set(Activity::getStatus, 1);
            int updatedToInProgress = activityMapper.update(null, toInProgressWrapper);
            if (updatedToInProgress > 0) {
                logger.info("将{}个活动状态更新为进行中", updatedToInProgress);
            }

            // 将状态为进行中(1)且结束时间已到的活动更新为已结束(2)
            LambdaUpdateWrapper<Activity> toEndedWrapper = new LambdaUpdateWrapper<>();
            toEndedWrapper.eq(Activity::getStatus, 1)
                    .le(Activity::getEndTime, now)
                    .set(Activity::getStatus, 2);
            int updatedToEnded = activityMapper.update(null, toEndedWrapper);
            if (updatedToEnded > 0) {
                logger.info("将{}个活动状态更新为已结束", updatedToEnded);
            }

            if (updatedToInProgress == 0 && updatedToEnded == 0) {
                logger.info("没有需要更新状态的活动");
            } else {
                logger.info("活动状态自动更新任务完成，共更新{}条记录", updatedToInProgress + updatedToEnded);
            }
        } catch (Exception e) {
            logger.error("执行活动状态自动更新任务时发生错误", e);
        }
    }
}