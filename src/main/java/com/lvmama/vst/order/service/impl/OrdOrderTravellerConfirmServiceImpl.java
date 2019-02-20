package com.lvmama.vst.order.service.impl;

import com.lvmama.comm.vst.VSTEnum;
import com.lvmama.vst.back.order.exception.OrderException;
import com.lvmama.vst.back.order.po.OrdOrderTravellerConfirm;
import com.lvmama.vst.back.order.po.OrderTravellerOperateDO;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.order.dao.OrdOrderTravellerConfirmDao;
import com.lvmama.vst.order.service.OrdOrderTravellerConfirmService;
import com.lvmama.vst.order.vo.TravellerOperateInfoCheckResult;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by zhouyanqun on 2016/4/29.
 * 游玩人确认信息对外服务的组件
 */
@Service("ordOrderTravellerConfirmService")
public class OrdOrderTravellerConfirmServiceImpl implements OrdOrderTravellerConfirmService {
    private static final Log log = LogFactory.getLog(OrdOrderTravellerConfirmServiceImpl.class);
    @Resource(name="orderTravellerConfirmDao")
    private OrdOrderTravellerConfirmDao orderTravellerConfirmDao;
    @Resource
    private LvmmLogClientService lvmmLogClientService;

    /**
     * 插入或者更新，根据OrdOrderTravellerConfirm的orderId判断记录是否存在，如果存在，就更新，如果不存在，就保存
     *
     */
    @Override
    public int saveOrUpdate(OrderTravellerOperateDO orderTravellerOperateDO) {
        log.info("=================================开始保存/更新游玩人确认信息, 传入参数是:" + JSONUtil.bean2Json(orderTravellerOperateDO));
        //检查参数
        this.checkTravellerOperateInfo(orderTravellerOperateDO);

        OrdOrderTravellerConfirm orderTravellerConfirm = orderTravellerOperateDO.getOrderTravellerConfirm();
        //补全日期
        orderTravellerConfirm = checkAndFillDate(orderTravellerConfirm);
        //操作日志内容字符串
        String content;
        //操作日志备注信息
        String memo = "";
        Long orderId = orderTravellerConfirm.getOrderId();
        int affectedRows;
        //检查是否已经存在
        OrdOrderTravellerConfirm originalOrderTravellerConfirm = selectSingleByOrderId(orderId);
        if(originalOrderTravellerConfirm == null){
            log.info("订单[" + orderId + "]尚未保存游玩人确认信息，此次将插入");
            //不存在记录，插入
            affectedRows = orderTravellerConfirmDao.persist(orderTravellerConfirm);
            content = this.generateOperateLogContent(ActionType.INSERT, orderTravellerConfirm, null);
        } else {
            log.info("订单[" + orderId + "]已有游玩人确认信息，此次将更新");
            //存在记录，更新
            orderTravellerConfirm.setCreateTime(originalOrderTravellerConfirm.getCreateTime());
            affectedRows = orderTravellerConfirmDao.merge(orderTravellerConfirm);
            content = this.generateOperateLogContent(ActionType.UPDATE, orderTravellerConfirm, originalOrderTravellerConfirm);
        }
        //生成操作日志内容
        log.info("记录操作日志，日志内容是" + content);
        //记录备注信息
        String distributorName = generateDistributorName(orderTravellerOperateDO.getChannelType());
        if(StringUtils.isNotBlank(distributorName)){
            memo="来源：" + distributorName;
        }

        this.recordSaveOperateLog(orderTravellerConfirm.getOrderId(), orderTravellerOperateDO.getUserCode(), content, memo);
        log.info("=================================完成保存/更新游玩人确认信息, 返回行数是:" + affectedRows);
        return affectedRows;
    }

    /**
     * 插入记录
     *
     */
    @Override
    public int save(OrderTravellerOperateDO orderTravellerOperateDO) {
        log.info("=================================开始保存游玩人确认信息, 传入参数是:" + JSONUtil.bean2Json(orderTravellerOperateDO));
        this.checkTravellerOperateInfo(orderTravellerOperateDO);

        OrdOrderTravellerConfirm orderTravellerConfirm = orderTravellerOperateDO.getOrderTravellerConfirm();
        //补全日期
        orderTravellerConfirm = checkAndFillDate(orderTravellerConfirm);
        String content = this.generateOperateLogContent(ActionType.INSERT, orderTravellerConfirm, null);
        //操作日志备注信息
        String memo = "";
        //记录操作日志
        log.info("记录操作日志，日志内容是" + content);

        //记录备注信息
        String distributorName = generateDistributorName(orderTravellerOperateDO.getChannelType());
        if(StringUtils.isNotBlank(distributorName)){
            memo="来源：" + distributorName;
        }

        this.recordSaveOperateLog(orderTravellerConfirm.getOrderId(), orderTravellerOperateDO.getUserCode(), content, memo);

        int affectedRows = orderTravellerConfirmDao.persist(orderTravellerConfirm);
        log.info("=================================完成保存游玩人确认信息, 返回行数是:" + affectedRows);
        return affectedRows;
    }

    /**
     * 更新记录
     *
     */
    @Override
    public int update(OrderTravellerOperateDO orderTravellerOperateDO) {
        return saveOrUpdate(orderTravellerOperateDO);
    }

    /**
     * 根据订单id查询一条记录
     *
     * @param orderId
     */
    @Override
    public OrdOrderTravellerConfirm selectSingleByOrderId(Long orderId) {
        if(orderId == null || orderId < 0){
            throw new OrderException(String.valueOf(VSTEnum.ERROR_CODE.MSG_PARAMETER_NULL.getErrorCode()), "orderId为空");
        }
        OrdOrderTravellerConfirm ordOrderTravellerConfirm = orderTravellerConfirmDao.selectSingleByOrderId(orderId);
        return ordOrderTravellerConfirm;
    }


    /**
     * 检查参数是否完整，如果不完整抛出异常
     * */
    private TravellerOperateInfoCheckResult checkTravellerOperateInfo(OrderTravellerOperateDO orderTravellerOperateDO){
        TravellerOperateInfoCheckResult checkResult = doCheckParam(orderTravellerOperateDO);
        if(!checkResult.isSuccessful()){
            log.info("参数检验不通过, 传入参数是:" + JSONUtil.bean2Json(orderTravellerOperateDO) + "=============, 错误信息是" + checkResult.getMessage());
            throw new OrderException(String.valueOf(checkResult.getErrorCode()), checkResult.getMessage());
        }
        log.info("校验结果是:" + checkResult.getMessage());
        return checkResult;
    }

    /**
     * 检查参数
     * */
    private TravellerOperateInfoCheckResult doCheckParam(OrderTravellerOperateDO orderTravellerOperateDO){

        TravellerOperateInfoCheckResult checkResult = new TravellerOperateInfoCheckResult(Boolean.TRUE, null, null);
        if(orderTravellerOperateDO == null){
            checkResult.appendInfo(Boolean.FALSE, VSTEnum.ERROR_CODE.MSG_PARAMETER_NULL.getErrorCode(), "传入参数整体为空");
            return checkResult;
        }
        StringBuilder sb = new StringBuilder();

        //订单id
        if(orderTravellerOperateDO.getUserCode() == null){
            checkResult.appendInfo(Boolean.FALSE, VSTEnum.ERROR_CODE.MSG_PARAMETER_NULL.getErrorCode(), "操作人code为空");
        }

        //游玩人确认信息检查
        if(orderTravellerOperateDO.getOrderTravellerConfirm() == null){
            checkResult.appendInfo(Boolean.FALSE, VSTEnum.ERROR_CODE.MSG_PARAMETER_NULL.getErrorCode(), "游玩人确认信息为空");
        } else {
            OrdOrderTravellerConfirm orderTravellerConfirm = orderTravellerOperateDO.getOrderTravellerConfirm();
            if(orderTravellerConfirm.getOrderId() == null || orderTravellerConfirm.getOrderId() <= 0){
                checkResult.appendInfo(Boolean.FALSE, VSTEnum.ERROR_CODE.MSG_PARAMETER_ILLEGAL.getErrorCode(), "订单编号为空或者非法");
            }
            //当游玩人确认信息里面的值为空时，仅仅修改信息，不改变成功标识和错误码信息
            if(orderTravellerConfirm.getContainForeign() == null){
                checkResult.appendMessage("是否包含外籍人士为空");
            }
            if(orderTravellerConfirm.getContainOldMan() == null){
                checkResult.appendMessage("是否包含老人为空");
            }
           /* if(orderTravellerConfirm.getContainPregnantWomen() == null){
                checkResult.appendMessage("是否包含孕妇为空");
            }*/
        }

        return checkResult;
    }

    /**
     * 修改对象的修改时间为当前时间
     * 如果记录的创建日期为空，则把系统当前时间填充进去
     * */
    private OrdOrderTravellerConfirm checkAndFillDate(OrdOrderTravellerConfirm orderTravellerConfirm){
        if(orderTravellerConfirm == null) {
            return null;
        }
        Date date = Calendar.getInstance().getTime();
        //如果创建时间为空，则设定创建时间为当前系统时间
        if(orderTravellerConfirm.getCreateTime() == null){
            orderTravellerConfirm.setCreateTime(date);
        }
        orderTravellerConfirm.setUpdateTime(date);
        return orderTravellerConfirm;
    }

    /**
     * 记录操作日志
     * @param content: 日志内容
     * @param memo: 备注信息
     * */
    private void recordSaveOperateLog(Long orderId, String operatorName, String content, String memo){
        lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
                orderId,
                orderId,
                operatorName,
                content,
                ComLog.COM_LOG_LOG_TYPE.CHANGE_TRAVELLER_CONFIRM_SUCCESS.name(),
                ComLog.COM_LOG_LOG_TYPE.CHANGE_TRAVELLER_CONFIRM_SUCCESS.getCnName(), memo);
    }

    /**
     * 生成操作日志信息
     * */
    private String generateOperateLogContent(ActionType actionType, OrdOrderTravellerConfirm orderTravellerConfirm, OrdOrderTravellerConfirm originalOrderTravellerConfirm){
        String separator = ";";
        StringBuilder sb = new StringBuilder();
        sb.append("操作类型:").append(actionType.getActionName()).append("游玩人确认信息").append(separator);
        //操作类型为更新时，记录操作前的值
        if(actionType.compareTo(ActionType.UPDATE) == 0){
            sb.append("操作前游玩人信息为:").append(generateConfirmOperateLogInfo(originalOrderTravellerConfirm)).append(separator);
        }
        //记录操作后的值
        sb.append("操作后游玩人信息为:").append(generateConfirmOperateLogInfo(orderTravellerConfirm));
        return sb.toString();
    }

    /**
     * 生成游玩人确认信息的操作日志信息
     * */
    private String generateConfirmOperateLogInfo(OrdOrderTravellerConfirm orderTravellerConfirm){
        String separator = ",";
        if(orderTravellerConfirm == null){
            return null;
        }
        StringBuilder sb = new StringBuilder().append("{");
        sb.append("是否包含孕妇:").append(orderTravellerConfirm.getContainPregnantWomen()).append(separator);
        if(orderTravellerConfirm.getContainForeign()!=null){
        	sb.append("是否包含外籍人士:").append(orderTravellerConfirm.getContainForeign()).append(separator);
        }
        sb.append("是否包含老年人:").append(orderTravellerConfirm.getContainOldMan()).append("}");
        return sb.toString();
    }

    private enum ActionType{
        UPDATE("更新"), INSERT("保存");
        private String actionName;

        ActionType(String actionName) {
            this.actionName = actionName;
        }

        public String getActionName() {
            return actionName;
        }
    }

    /**
     * 根据来源号得到来源名
     * */
    private String generateDistributorName(String channelType){
        final String EMPTY="";
        if(StringUtils.isBlank(channelType)){
            return EMPTY;
        }
        for (VSTEnum.DISTRIBUTION distribution : VSTEnum.DISTRIBUTION.values()) {
            if(String.valueOf(distribution.getNum()).equalsIgnoreCase(channelType)){
                return distribution.getName();
            }
        }
        return EMPTY;
    }
}
