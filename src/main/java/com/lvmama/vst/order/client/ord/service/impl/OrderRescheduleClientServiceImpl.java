package com.lvmama.vst.order.client.ord.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.ord.service.OrderRescheduleClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.vo.SuppGoodsRescheduleVO;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderRescheduleInfo;
import com.lvmama.vst.comm.vo.RescheduleTimePrice;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkUserClientService;
import com.lvmama.vst.ebooking.ebk.po.EbkUser;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.reschedule.IOrderRescheduleService;

@Component("orderRescheduleClientServiceRemote")
public class OrderRescheduleClientServiceImpl implements OrderRescheduleClientService {

    private static final Log LOG = LogFactory.getLog(OrderRescheduleClientServiceImpl.class);
    
    @Autowired
    private IOrderRescheduleService orderRescheduleService;

    @Autowired
    private IComplexQueryService complexQueryService;
    
    @Autowired
    private EbkUserClientService ebkUserClientService;
    
    @Override
    public String rescheduleFlag(Long orderId) {
        return orderRescheduleService.rescheduleFlag(orderId);
    }
    
    @Override
    public String rescheduleMsg(Long orderId) {
        OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
        return orderRescheduleService.rescheduleMsg(order);
    }
    
    @Override
    public Long getOrdChangeCount(Long orderId) {
        OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
        return orderRescheduleService.getOrdChangeCount(order);
    }

    @Override
    public ResultHandle reschedule(Long orderId, List<SuppGoodsRescheduleVO.Item> list,Map<String,Object> map) {
        ResultHandle resultHandleCheck = orderRescheduleService.rescheduleCheck(orderId, list);
        if(!resultHandleCheck.isSuccess()){
            return resultHandleCheck;
        }
        ResultHandleT<List<SuppGoodsRescheduleVO.Item>> listResultHandleT = orderRescheduleService.updateOrderItemVisitTime(orderId, list);
        orderRescheduleService.addRescheduleLog(listResultHandleT,orderId,map);
        return listResultHandleT;
    }

    @Override
    public String getRescheduleTimePrice(Long suppGoodsId, Long productId, Long distributorId, Map<String, Object> map) {
        String result="";
        String[] properties = new String[]{"specDateStr","suppGoodsId","subZeroPriceYuan","stock","sale","price","oversellFlag","stockFlag"};
        try {
            List<RescheduleTimePrice> rescheduleTimePriceList = orderRescheduleService.getRescheduleTimePriceList(suppGoodsId, productId, distributorId, map);
            SimplePropertyPreFilter simplePropertyPreFilter = new SimplePropertyPreFilter(SuppGoodsAddTimePrice.class,properties);
            result = JSON.toJSONString(rescheduleTimePriceList,simplePropertyPreFilter);;
        } catch (Exception e) {
            LOG.error("getRescheduleTimePrice error:"+e.getMessage(),e);
        }
        return result;
    }

    @Override
    public OrderRescheduleInfo queryOrderRescheduleInfo(Long orderId, Map<String, Object> map) {
        return orderRescheduleService.queryOrderRescheduleInfo(orderId,map);
    }

    @Override
    public boolean isEbkAndEnterInTime(SuppGoods suppGoods) {
        boolean isEBKAndEnterInTime = false;
        try {
            Boolean IS_EBK_NOTICE = Boolean.FALSE;
            Boolean IS_FAX_NOTICE = "Y".equalsIgnoreCase(suppGoods.getFaxFlag());
            if (!IS_FAX_NOTICE && isRescheduleCategory(suppGoods.getCategoryId())) {
                if (!IS_EBK_NOTICE) {
                    Map<String,Object> paramUser=new HashMap<String,Object>();
                    paramUser.put("cancelFlag", "Y");
                    paramUser.put("supplierId", suppGoods.getSupplierId());
                    List<EbkUser> ebkUserList = ebkUserClientService.getEbkUserList(paramUser).getReturnContent();
                    if(ebkUserList!=null&& !ebkUserList.isEmpty()){
                        IS_EBK_NOTICE= true;
                    }
                }
                if (IS_EBK_NOTICE) {
                    String notInTimeFlag_suppgoods = suppGoods.getNotInTimeFlag();
                    if (!"Y".equals(notInTimeFlag_suppgoods)) {
                        isEBKAndEnterInTime = true;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("OrderRescheduleClientServiceImpl isEbkAndEnterInTime error:", e);
        }
        return isEBKAndEnterInTime;
    }

    @Override
    public String getApiFlag(SuppGoods suppGoods){
        String apiFlag = "N";
        if(null != suppGoods && isRescheduleCategory(suppGoods.getCategoryId())){
            if(SuppGoods.NOTICETYPE.QRCODE.name().equalsIgnoreCase(suppGoods.getNoticeType())){
                apiFlag = "Y";
            }
        }
        return  apiFlag;
    }

    private boolean isRescheduleCategory(Long categoryId) {
        return categoryId != null && (
        BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(categoryId) ||
        BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(categoryId) ||
        BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(categoryId));
    }

}
