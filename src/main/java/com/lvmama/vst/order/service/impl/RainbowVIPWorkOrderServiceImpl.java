package com.lvmama.vst.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.comm.pet.service.user.UserUserProxy;
import com.lvmama.comm.pet.service.work.PublicWorkOrderService;
import com.lvmama.comm.pet.vo.InvokeResult;
import com.lvmama.comm.pet.vo.WorkOrderCreateParam;
import com.lvmama.crm.service.CsVipDubboService;
import com.lvmama.crm.vo.CsVipStaffUserRelationVO;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.dujia.comm.prod.po.ProdLineBasicInfo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrdOrderPackService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.RainbowVIPWorkOrderService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 彩虹会员工单服务层
 */
@Service
public class RainbowVIPWorkOrderServiceImpl implements RainbowVIPWorkOrderService {

    private final Logger logger = LoggerFactory.getLogger(RainbowVIPWorkOrderServiceImpl.class);

    private final String workOrderTypeCode = "VIPtask";

    @Autowired
    private IOrdOrderItemService iOrdOrderItemService;
    @Autowired
    private IOrdOrderPackService iOrdOrderPackService;
    @Autowired
    private CsVipDubboService csVipDubboService;
    @Autowired
    private PublicWorkOrderService publicWorkOrderService;
    @Autowired
    private UserUserProxy userUserProxy;
    @Autowired
    private ProdProductClientService prodProductClientService;

    @Override
    public void pushWorkOrderRemindAfterOrder(OrdOrder ordOrder) {
        try {
            this.push(ordOrder);
        }catch (Exception e){
            logger.debug("#RainbowVIPWorkOrder# orderId="+ordOrder.getOrderId()+", exceptionInfo="+ ExceptionFormatUtil.getTrace(e));
        }
    }

    @Override
    public void push(OrdOrder ordOrder) throws Exception {
        if(ordOrder == null){
            throw new Exception("#RainbowVIPWorkOrderService# ordOrder is null");
        }
        Long userId = ordOrder.getUserNo();
        if(userId == null){
            throw new Exception("#RainbowVIPWorkOrderService# userId is null");
        }

        Map<String,Object> params = new HashMap<String,Object>();
        params.put("orderId",ordOrder.getOrderId());
        //获取主子订单
        List<OrdOrderItem> ordOrderItemList = iOrdOrderItemService.selectByParams(params);
        if(CollectionUtils.isNotEmpty(ordOrderItemList)){
            for(OrdOrderItem ordOrderItem : ordOrderItemList){
                if(ordOrderItem != null && "true".equals(ordOrderItem.getMainItem())){
                    List<OrdOrderItem> tempList = new ArrayList<OrdOrderItem>();
                    tempList.add(ordOrderItem);
                    ordOrder.setOrderItemList(tempList);
                }
            }
        }
        //只有跟团游和自由行的自主打包在 ord_order_pack 中才有数据
        List<OrdOrderPack> ordOrderPackList = iOrdOrderPackService.findOrdOrderPackList(params);
        if(CollectionUtils.isNotEmpty(ordOrderPackList)){
            ordOrder.setOrderPackList(ordOrderPackList);
        }

        //验证是否符合条件
        if(!this.needPush(ordOrder)){
            throw new Exception("#RainbowVIPWorkOrderService# needPush failure");
        }

        CsVipStaffUserRelationVO csVipStaffUserRelationVO = csVipDubboService.queryUserAssignedCsVipStaffByUserId(userId);
        if(csVipStaffUserRelationVO == null || StringUtil.isEmptyString(csVipStaffUserRelationVO.getCsUserId())){
            throw new Exception("#RainbowVIPWorkOrderService# csVipStaffUserRelationVO | csVipStaffUserRelationVO.getCsUserId() is null");
        }
        String csUserId = csVipStaffUserRelationVO.getCsUserId();

        WorkOrderCreateParam workOrderCreateParam = new WorkOrderCreateParam();
        //workOrderTypeCode 工单类型编码
        workOrderCreateParam.setWorkOrderTypeCode(workOrderTypeCode);
        //订单号
        workOrderCreateParam.setOrderId(ordOrder.getOrderId());
        //产品Id
        workOrderCreateParam.setProductId(ordOrder.getOrderItemList().get(0).getProductId());
        //游客用户名
        UserUser userUser = this.userUserProxy.getUserUserByPk(userId);
        workOrderCreateParam.setVisitorUserName(userUser.getUserName());
        //联系人手机号
        workOrderCreateParam.setMobileNumber(userUser.getMobileNumber());
        //处理时限(单位：分钟)
        workOrderCreateParam.setLimitTime(120l);
        //workOrderContent 工单内容
        //workOrderCreateParam.setWorkOrderContent();
        //接收组id receiveGroupId
        //workOrderCreateParam.setReceiveGroupId();
        //接收人用户名 receiveUserName
        workOrderCreateParam.setReceiveUserName(csUserId);
        //任务内容 workTaskContent
        Date date = ordOrder.getCreateTime();
        StringBuffer workTaskContent = new StringBuffer("客人下单时间：");
        workTaskContent.append(DateUtil.formatDate(date,DateUtil.HHMMSS_DATE_FORMAT));
        workTaskContent.append("，客人购买产品（订单号）：");
        workTaskContent.append("<a href=\"http://super.lvmama.com/vst_order/order/ordCommon/showOrderDetails.do?orderId="+ordOrder.getOrderId()+"\">"+ordOrder.getOrderId()+"</a>");
        workOrderCreateParam.setWorkTaskContent(workTaskContent.toString());
        //处理工单时打开的页面地址 url
        //workOrderCreateParam.setUrl(null);
        //创建人用户名 sendUserName
        //workOrderCreateParam.setSendUserName("admin");
        //创建人组织Id sendGroupId
        //workOrderCreateParam.setSendGroupId();
        //工单级别 processLevel
        //workOrderCreateParam.setProcessLevel();
        //是否要重新加载接受用户 isNotGetFitReceiveUser
        workOrderCreateParam.setNotGetFitReceiveUser(true);
        //下单人工号 takenOperator
        //workOrderCreateParam.setTakenOperator();
        //isJdGroup
        workOrderCreateParam.setJdGroup(false);

        InvokeResult invokeResult = null;
        try {
            invokeResult = publicWorkOrderService.createWorkOrder(workOrderCreateParam);
        }catch (Exception e){
            throw new Exception("#RainbowVIPWorkOrderService# "+ExceptionFormatUtil.getTrace(e));
        }
        if(invokeResult==null || invokeResult.getCode() !=0 ){
            throw new Exception("#RainbowVIPWorkOrderService# "+ (invokeResult == null ? "" : "["+invokeResult.getCode()+"]"+invokeResult.getDescription()));
        }
    }

    /**
     * 判断该订单是否要工单提醒
     * @param ordOrder
     * @return
     */
    private boolean needPush(OrdOrder ordOrder) throws Exception{
        Long categoryId = ordOrder.getCategoryId();
        Long productId = ordOrder.getProductId();

        //邮轮 邮轮附加项 邮轮组合产品 岸上观光 签证
        if(BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCategoryId().equals(categoryId)
                || BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(categoryId)
                || BizEnum.BIZ_CATEGORY_TYPE.category_cruise_addition.getCategoryId().equals(categoryId)
                || BizEnum.BIZ_CATEGORY_TYPE.category_sightseeing.getCategoryId().equals(categoryId)
                || BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCategoryId().equals(categoryId)){
            return true;
        }

        //查询产品
        ProdProduct prodProduct = null;
        ResultHandleT<ProdProduct> resultHandleT = prodProductClientService.findProdProductById(productId);
        if(resultHandleT != null && resultHandleT.getReturnContent() != null){
            prodProduct = resultHandleT.getReturnContent();
        }

        //跟团游
        if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(categoryId)){
            if(prodProduct != null
                    && (ProdProduct.PRODUCTTYPE.INNERLONGLINE.getCode().equals(prodProduct.getProductType())
                    || ProdProduct.PRODUCTTYPE.FOREIGNLINE.getCode().equals(prodProduct.getProductType()))){
                return true;
            }
        }
        //自由行
        if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(categoryId)){
            if(prodProduct != null
                    && BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(prodProduct.getSubCategoryId())
                    && (ProdProduct.PRODUCTTYPE.INNERLONGLINE.getCode().equals(prodProduct.getProductType())
                        || ProdProduct.PRODUCTTYPE.FOREIGNLINE.getCode().equals(prodProduct.getProductType()))){
                return true;
            }
        }
        //当地游
        if(BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(categoryId)){
            if(prodProduct != null
                    && (ProdProduct.PRODUCTTYPE.FOREIGNLINE.getCode().equals(prodProduct.getProductType()))){
                return true;
            }
        }
        return false;
    }

}
