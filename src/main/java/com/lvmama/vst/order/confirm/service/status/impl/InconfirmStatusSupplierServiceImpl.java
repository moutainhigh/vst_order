package com.lvmama.vst.order.confirm.service.status.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.ord.service.DestOrderWorkflowService;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;
import com.lvmama.vst.ebooking.ebk.po.EbkCertifItem;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmStatusService;
import com.lvmama.vst.order.confirm.service.status.IConfirmStatusService;
import com.lvmama.vst.order.confirm.vo.ConfirmStatusParamVo;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.OrderItemConfirmStatusClientService;

/**
 * 已审库服务(供应商)
 */
@Service("inconfirmStatusSupplierService")
public class InconfirmStatusSupplierServiceImpl extends DefaultConfirmStatusService implements IConfirmStatusService {
    private static final Logger LOG = LoggerFactory.getLogger(InconfirmStatusSupplierServiceImpl.class);
    @Autowired
    private IOrderUpdateService orderUpdateService;
    @Autowired
    private DestOrderWorkflowService destOrderWorkflowService;
    @Autowired
    IOrdItemConfirmStatusService ordItemConfirmStatusService;
    @Autowired
	OrderItemConfirmStatusClientService orderItemConfirmStatusClientService;
    /**
     * 员工库处理
     * @param confirmStatusParamVo 接口参数
     * @return
     * @throws Exception
     */
    @Override
    public <T> T handle(ConfirmStatusParamVo confirmStatusParamVo) throws Exception{
        OrdOrder order =confirmStatusParamVo.getOrder();
        EbkCertif ebkCertif =confirmStatusParamVo.getEbkCertif();

        ResultHandleT<List<Object[]>> result =new ResultHandleT<List<Object[]>>();
        if(order ==null || ebkCertif ==null ||ebkCertif.getEbkCertifItemList() ==null){
            result.setMsg("order or ebkCertif or ebkCertifItemList is null");
            return (T)result;
        }
        LOG.info("orderId=" +order.getOrderId()
                +",isNormal=" +order.isNormal()
                +",ebkItemSize=" +ebkCertif.getEbkCertifItemList().size()
                +",memo=" +ebkCertif.getMemo()
                +",reason=" +ebkCertif.getReason());
        //非前置支付
        if(!order.isNormal()) {
            result.setMsg("order not is normal orderId=" + order.getOrderId());
            return (T)result;
        }
        if(!OrdOrderUtils.isDestBuFrontOrderNew(order)&&!OrdOrderUtils.isDestBuFrontOrderNew_O2O(order)&&!OrdOrderUtils.isLocalBuOrderNew(order)){
            result.setMsg("isDestBuFrontOrderNew false,orderId=" +order.getOrderId());
            return (T)result;
        }
        List<Object[]> list =new ArrayList<Object[]>();
        result.setReturnContent(list);
        //获取供应商处理状态
        Confirm_Enum.CONFIRM_STATUS newStatus =null;
        if(ebkCertif.hasCertificateStatusAccept()){
            if(ebkCertif.getMemo()==null || ebkCertif.getMemo().length() ==0){
                newStatus = Confirm_Enum.CONFIRM_STATUS.SUCCESS;
            }else{
            	LOG.info("orderId=" +order.getOrderId()+",newStatus=" +newStatus);
                //dongningbo 供应商为EBK、如供应商接受预定，但是填写备注，订单停留在已审库。（现状是分配至满房库）
                //newStatus =CONFIRM_STATUS.FULL;//备注不为空,默认满房
            	 for (EbkCertifItem ebkCertifItem : ebkCertif.getEbkCertifItemList()) {
                     //酒店、酒店套餐
                     if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId()
                             .equals(ebkCertifItem.getCategoryId())
                             || BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb
                             .getCategoryId().equals(
                                     ebkCertifItem.getCategoryId())) {
                    	 OrdOrderItem orderItem=orderUpdateService.getOrderItem(ebkCertifItem.getOrderItemId());
		                if(ebkCertif.hasCertificateStatusAccept()
		                		&&StringUtil.isNotEmptyString(ebkCertif.getMemo())){
		                	orderItemConfirmStatusClientService.updateOrderItemStatusByOrderItemIdList(Arrays.asList(orderItem.getOrderItemId()), Confirm_Enum.CONFIRM_AUDIT_TYPE .INCONFIRM_AUDIT);
		                }
                     }
            	 }
                return (T)result;
            }
        }else{
            newStatus =destOrderWorkflowService
                    .convertConfirmStatusByEbk(ebkCertif.getReason());
        }
        LOG.info("orderId=" +order.getOrderId()
                +",newStatus=" +newStatus);
        for (EbkCertifItem ebkCertifItem : ebkCertif.getEbkCertifItemList()) {
            //酒店、酒店套餐
            if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId()
                    .equals(ebkCertifItem.getCategoryId())
                    || BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb
                    .getCategoryId().equals(
                            ebkCertifItem.getCategoryId())) {
                updateChildConfirmStatus(confirmStatusParamVo, list, newStatus, ebkCertifItem);
                OrdOrderItem orderItem=orderUpdateService.getOrderItem(ebkCertifItem.getOrderItemId());
                try{
                	if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb
                            .getCategoryId().equals( ebkCertifItem.getCategoryId())
                            &&!ebkCertif.hasCertificateStatusAccept()
                            &&(ebkCertif.getMemo()==null || ebkCertif.getMemo().length() ==0)){
                		ordItemConfirmStatusService.closeFullhotelAndForbidSale(orderItem, ebkCertif.getConfirmUser(),"满房自动关房(供应商EBK操作)", newStatus.getCnName() ,null,null,null);
                	}else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId()
                            .equals(ebkCertifItem.getCategoryId())
                           && EbkCertif.REASON_DESC.ROOM_FULL.getCode().equals(ebkCertif.getReason())
                           &&!orderItem.hasSupplierApi()
                			&&(ebkCertif.getMemo()==null || ebkCertif.getMemo().length() ==0)){
                		ordItemConfirmStatusService.closeFullhotelAndForbidSale(orderItem, ebkCertif.getConfirmUser(),"满房自动关房(供应商EBK操作)", newStatus.getCnName() ,null,null,null);
                	}
                }catch(Exception e){
                	LOG.error("====orderItemId:"+orderItem.getOrderItemId()+",close hotel fail===",e);
                }
            }
        }
        LOG.info("orderId=" +order.getOrderId()
                +",isSuccess=" +result.isSuccess()
                +",msg=" +result.getMsg());

        return (T)result;
    }

    /**
     * 更新子订单确认状态
     * @param confirmStatusParamVo
     * @param list
     * @param newStatus
     * @param ebkCertifItem
     * @throws Exception
     */
    private void updateChildConfirmStatus(ConfirmStatusParamVo confirmStatusParamVo, List<Object[]> list
            , Confirm_Enum.CONFIRM_STATUS newStatus, EbkCertifItem ebkCertifItem) throws Exception {
        OrdOrderItem orderItem = orderUpdateService
                .getOrderItem(ebkCertifItem.getOrderItemId());
        //初始化参数vo
        initSupplierItem(confirmStatusParamVo, orderItem, newStatus);

        //更新子订单确认状态
        ResultHandleT<ComAudit> resultHandle = updateChildConfirmStatus(confirmStatusParamVo);
        if (resultHandle.isSuccess()) {
            Object[] array = new Object[2];
            array[0] = orderItem;
            array[1] = resultHandle.getReturnContent();
            list.add(array);
        } else {
            throw new BusinessException(resultHandle.getMsg());
        }
    }

    /**
     * 初始化供应商子项
     * @param confirmStatusParamVo
     * @param orderItem
     * @param newStatus
     */
    private void initSupplierItem(ConfirmStatusParamVo confirmStatusParamVo
            , OrdOrderItem orderItem, Confirm_Enum.CONFIRM_STATUS newStatus){
        confirmStatusParamVo.setOrderItem(orderItem);
        confirmStatusParamVo.setNewStatus(newStatus);
        confirmStatusParamVo.setOperator("SYSTEM");//confirmStatusParamVo.getEbkCertif().getConfirmUser();//ebk不更新操作人
    }
}
