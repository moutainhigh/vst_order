package com.lvmama.vst.order.service.apportion.job;

import com.lvmama.comm.TaskServiceInterface;
import com.lvmama.comm.pet.po.pub.TaskResult;
import com.lvmama.log.util.LogTrackContext;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.vo.MemcachedEnum;
import com.lvmama.vst.order.service.apportion.BatchApportionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * Created by zhouyanqun on 2017/7/3.
 */
@Component("orderApportionJobWS")
public class OrderApportionJob implements TaskServiceInterface, Serializable {
    private static final Log log = LogFactory.getLog(OrderApportionJob.class);
    @Resource
    private BatchApportionService batchApportionService;
    @Override
    public TaskResult execute(Long aLong, String s) throws Exception {
        LogTrackContext.initTrackNumber();
        log.info("Order apportion web service job started....");
        TaskResult taskResult = new TaskResult();
        try {
            //是否有另一个job正在执行
            String apportionJobRunning = MemcachedUtil.getInstance().get(MemcachedEnum.ApportionJobExecuting.name());
            if (StringUtils.equals(Constants.Y_FLAG, apportionJobRunning)) {
                taskResult.setRunStatus(TaskResult.RUN_STATUS.SUCCESS);
                taskResult.setResult("另一个分摊批次正在运行");
            } else {
                //加入缓存锁，防止多个job同时跑
                MemcachedUtil.getInstance().set(MemcachedEnum.ApportionJobExecuting.name(), MemcachedEnum.ApportionJobExecuting.getSec(), Constants.Y_FLAG);
                batchApportionService.batchApportionOrders();
                taskResult.setRunStatus(TaskResult.RUN_STATUS.SUCCESS);
                taskResult.setResult(TaskResult.RUN_STATUS.SUCCESS.getCnName());
                //跑批完成，删除缓存锁
                MemcachedUtil.getInstance().remove(MemcachedEnum.ApportionJobExecuting.name());
            }
        } catch (Exception e) {
            taskResult.setRunStatus(TaskResult.RUN_STATUS.FAILED);
            taskResult.setResult(e.getMessage());
            log.error("Error batch apportion order", e);
            MemcachedUtil.getInstance().remove(MemcachedEnum.ApportionJobExecuting.name());
        }
        log.info("Order apportion web service job end....");
        return taskResult;
    }
}
