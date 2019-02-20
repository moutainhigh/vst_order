package com.lvmama.vst.order.tnt.contract.service;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.order.contract.service.OrderTravelElectricContactMailService;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 出境分销淘宝条款协议逻辑处理
 * @Author: LuWei
 * @Date: 2018/06/26 13:51
 */
@Service("designatedFreetourOrderService")
public class DesignatedFreetourOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(DesignatedFreetourOrderService.class);

    @Resource
    private OrderTravelElectricContactMailService orderTravelElectricContactMailService;

    @Resource
    private IOrdTravelContractService ordTravelContractService;

    /**
     * 分销淘宝条款协议逻辑处理
     * @敖顺华
     * @param order
     * @param opertator
     * @return
     */
    public ResultHandle sendOrderEcontractEmail(OrdOrder order, String opertator) {
        //初始化参数
        Map<String, Object> parametersTravelContract = new HashMap<String, Object>();
        parametersTravelContract.put("orderId",order.getOrderId());
        List<OrdTravelContract> ordTravelContractList=ordTravelContractService.findOrdTravelContractList(parametersTravelContract);
        List<OrdTravelContract> designatedTravelContractList = new ArrayList<OrdTravelContract>();
        List<OrdTravelContract> notDesignatedTravelContractList = new ArrayList<OrdTravelContract>();
        List<OrdTravelContract> travelContractList = new ArrayList<OrdTravelContract>();
        if("system".equalsIgnoreCase(opertator) && CollectionUtils.isNotEmpty(ordTravelContractList)){
            LOG.info("sendOrderEcontractEmail condition access:start==,orderId=" + order.getOrderId());
            for(OrdTravelContract otc:ordTravelContractList){
                if(otc.getContractTemplate().equals(CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode())
                        || otc.getContractTemplate().equals(CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode())
                        || otc.getContractTemplate().equals(CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode())
                        ){
                    designatedTravelContractList.add(otc);
                }else{
                    notDesignatedTravelContractList.add(otc);
                }
            }
            ResultHandle resultHandle = new ResultHandle();
            if(CollectionUtils.isNotEmpty(designatedTravelContractList)){
                LOG.info("sendEmailToDesignatedAddress:start==,orderId=" + order.getOrderId());
                resultHandle = sendEmailToDesignatedAddress(order, designatedTravelContractList,opertator);
            }
            if(CollectionUtils.isNotEmpty(notDesignatedTravelContractList)){

                resultHandle =  sendEmail(order, notDesignatedTravelContractList,opertator);
            }
            return resultHandle;
        }else {
            LOG.info("sendOrderEcontractEmail condition unvalid:start==,orderId=" + order.getOrderId());
            return sendEmail(order, ordTravelContractList, opertator);
        }
    }
    /**
     * 分销淘宝根据订单与合同号发送合同邮件
     * @param order
     * @param contractId
     * @return
     */
    public ResultHandle sendContractEmail(OrdOrder order,Long contractId,String operator){
        OrdTravelContract tract = ordTravelContractService.findOrdTravelContractById(contractId);
        List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
        list.add(tract);
        ResultHandle resultHandle = new ResultHandle();
        if(tract != null && "system".equalsIgnoreCase(operator)
                && (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode().equals(tract.getContractTemplate())
                || CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode().equals(tract.getContractTemplate())
                || CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode().equals(tract.getContractTemplate())
                )){
            LOG.info("sendContractEmail sendEmailToDesignatedAddress,orderId=" + order.getOrderId());
            resultHandle = sendEmailToDesignatedAddress(order, list,operator);
        } else {
            LOG.info("sendContractEmail sendEmail,orderId=" + order.getOrderId());
            resultHandle = sendEmail(order, list,operator);
        }
        return resultHandle;
    }

    /**
     * sendEmailToDesignatedAddress
     * @param order
     * @param designatedTravelContractList
     * @param opertator
     * @return
     */
    private ResultHandle sendEmailToDesignatedAddress(OrdOrder order,List<OrdTravelContract> designatedTravelContractList,String opertator){
        return orderTravelElectricContactMailService.sendEmailToDesignatedAddress(order, designatedTravelContractList,opertator);
    }
    /**
     * sendEmail
     * @param order
     * @param designatedTravelContractList
     * @param opertator
     * @return
     */
    private ResultHandle sendEmail(OrdOrder order,List<OrdTravelContract> designatedTravelContractList,String opertator){
        return orderTravelElectricContactMailService.sendEmail(order, designatedTravelContractList,opertator);
    }
}
