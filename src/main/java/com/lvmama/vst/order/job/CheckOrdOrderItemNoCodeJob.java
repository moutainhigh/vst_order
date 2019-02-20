package com.lvmama.vst.order.job;

import com.lvmama.comm.pet.po.email.EmailContent;
import com.lvmama.vst.back.order.po.SendFailedMessaeInfo;
import com.lvmama.vst.comm.utils.ResourceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.dao.SendFailedMessageInfoDao;
import com.lvmama.vst.pet.adapter.VstEmailServiceAdapter;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by sangbowei on 2017/8/1.
 */
public class CheckOrdOrderItemNoCodeJob implements Runnable{

    private static final Log LOG = LogFactory.getLog(CheckOrdOrderItemNoCodeJob.class);

    @Autowired
    private OrdOrderItemDao ordOrderItemDao;

    @Autowired
    private SendFailedMessageInfoDao sendFailedMessageInfoDao;

    @Autowired
    private VstEmailServiceAdapter vstEmailServiceAdapter;

    private freemarker.template.Configuration cfg;

    private Template template;

    private final static String SYSTEM_EMAIL_ADDRESS = "service@cs.lvmama.com";
    private final static String TARGET_EMAIL_ADDRESS = "sangbowei@lvmama.com";
    private final static String TARGET_EMAIL_ADDRESS_2 = "shenjing@lvmama.com";
    private final static String EMAIL_FROM_NAME = "后台系统邮件";
    private final static String EMAIL_SUBJECT = "子订单中未绑定结算对象CODE";

    @Override
    public void run() {
        try {
            if (Constant.getInstance().isJobRunnable()) {
                long startTime = System.nanoTime();
                LOG.info("CheckOrdOrderItemNoCodeJob begins running at " + startTime);
                this.CheckOrdOrderItemNoCode();
                long endTime = System.nanoTime();
                LOG.info("CheckOrdOrderItemNoCodeJob finished, spent " + TimeUnit.SECONDS.convert(endTime - startTime, TimeUnit.SECONDS) + " s");
            }
            LOG.info("CheckOrdOrderItemNoCodeJob send email  success");
        } catch (Exception e) {
            LOG.error("CheckOrdOrderItemNoCodeJob send email fail",e);
        }

    }

    private void CheckOrdOrderItemNoCode(){
        LOG.info("============ Auto CheckOrdOrderItemNoCode start ========");
        if (cfg == null) {
            try {
                cfg = new freemarker.template.Configuration();
                cfg.setDefaultEncoding("utf-8");
                //设置模板文件目录
                cfg.setDirectoryForTemplateLoading(ResourceUtil.getResourceFile("/WEB-INF/resources/orderSettlement/"));
                // 取得模板文件
                template = cfg.getTemplate("order_item_code_email_template.ftl");
                cfg.setClassicCompatible(true);//处理空值为空字符串
            } catch (IOException e) {
                LOG.error("CheckOrdOrderItemNoCode error:" + e.getMessage());
            }
        }

        //发送邮件提醒
        this.sendMail();

        LOG.info("Auto CheckOrdOrderItemNoCode end.....");
    };

    private void sendMail(){

        // 获取各个品类对应的数据总数
       Long orderItemNoCodeCount  = handleOrderItemNoCodeCount();

       Long failedMessageInfoCount = countSendFailedMessageNumbers();

        // 发送邮件
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("orderItemNoCodeCount", orderItemNoCodeCount);
        rootMap.put("failedMessageInfoCount", failedMessageInfoCount);
        if(null!=failedMessageInfoCount && failedMessageInfoCount>0 ){
            rootMap.put("failedMessageInfoList", getSendFailedMessageInfoList()); // 最多发送100条数据
        }

        try {
            template.process(rootMap, stringWriter);
        } catch (TemplateException e) {
            LOG.error("[ sendMail ] method template.process has error , msg is "+e.getMessage());
        } catch (IOException e) {
            LOG.error("[ sendMail ] method template.process has error , msg is "+e.getMessage());
        }
        EmailContent emailContent = new EmailContent();
        emailContent.setFromAddress(SYSTEM_EMAIL_ADDRESS);
        emailContent.setFromName(EMAIL_FROM_NAME);
        emailContent.setSubject(EMAIL_SUBJECT);
        emailContent.setContentText(stringWriter.toString());
        emailContent.setToAddress(TARGET_EMAIL_ADDRESS);
        vstEmailServiceAdapter.sendEmailDirect(emailContent);

        emailContent.setToAddress(TARGET_EMAIL_ADDRESS_2);
        vstEmailServiceAdapter.sendEmailDirect(emailContent);
    }

    /**
     * 获取 线上子订单未绑定结算CODE的总数
     * @return
     */
    private Long handleOrderItemNoCodeCount(){
        Long orderItemNoCodeCount = ordOrderItemDao.findOrderItemNoCodeCount(new HashedMap());
        return orderItemNoCodeCount;
    }

    /**
     * 获取推送失败的订单总数
     * @return
     */
    private Long countSendFailedMessageNumbers(){
        return sendFailedMessageInfoDao.getFailedMessageCount(new HashedMap());
    }

    /**
     * 获取推送失败的订单信息，包含异常信息
     * @return
     */
    private List<SendFailedMessaeInfo> getSendFailedMessageInfoList(){
        List<SendFailedMessaeInfo> failedMessaeInfoList = sendFailedMessageInfoDao.getFailedMessageInfoList();
        if(CollectionUtils.isEmpty(failedMessaeInfoList)){
            return failedMessaeInfoList;
        }

        if(failedMessaeInfoList.size()>100){
            failedMessaeInfoList = failedMessaeInfoList.subList(0,100);
        }

        return failedMessaeInfoList;
    }
}
