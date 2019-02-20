package com.lvmama.vst.order.client.ord.service.impl;

import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.vst.back.client.ord.dto.OrdPersonQueryTO;
import com.lvmama.vst.back.client.ord.po.OrderRelatedPersonsVO;
import com.lvmama.vst.back.client.ord.service.OrdItemPersonRelationClientService;
import com.lvmama.vst.back.client.ord.service.OrdPersonClientService;
import com.lvmama.vst.back.client.ord.service.OrderRelatedPersonClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.MemcachedEnum;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.IOrdAddressService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouyanqun on 2016/10/9.
 */
@Component("orderRelatedPersonServiceRemote")
public class OrderRelatedPersonClientServiceImpl implements OrderRelatedPersonClientService {
    private static final Log log = LogFactory.getLog(OrderRelatedPersonClientServiceImpl.class);

    @Resource(name = "ordPersonServiceRemote")
    private OrdPersonClientService ordPersonClientService;
    @Resource(name = "ordItemPersonRelationServiceRemote")
    private OrdItemPersonRelationClientService ordItemPersonRelationClientService;
    @Resource
    private IOrdAddressService ordAddressService;
    @Resource
    private OrderService orderService;

    /**
     * 加载订单所有相关人员的信息
     * 目前查询5类订单相关人：联系人，紧急联系人，游玩人，快递联系人，自备签游玩人
     * 前4类订单相关人可以在表ord_person中取到，自备签游玩人需要在表ord_item_person_relation中取到
     * */
    @Override
    public ResultHandleT<OrderRelatedPersonsVO> loadOrderRelatedPersons(OrdPersonQueryTO orderPersonQueryTO) {
        ResultHandleT<OrderRelatedPersonsVO> resultHandleT = new ResultHandleT<OrderRelatedPersonsVO>();
        if(orderPersonQueryTO == null || orderPersonQueryTO.getOrderId() == null || orderPersonQueryTO.getOrderId() < 0){
            resultHandleT.setMsg("Can't load order related person without order id!");
            return resultHandleT;
        }
        Long orderId = orderPersonQueryTO.getOrderId();
        log.info("Now begin to query order related person for order " + orderId);

        try {
            OrderRelatedPersonsVO orderRelatedPersonsVO;
            String key = MemcachedEnum.OrderRelatedPerson.getKey() + orderId;
            //尝试从缓存中取值
            orderRelatedPersonsVO  = MemcachedUtil.getInstance().get(key);
            if(orderRelatedPersonsVO != null){
                log.info("Hit memory cache record for order " + orderId + ", key is " + key + " , record is " + GsonUtils.toJson(orderRelatedPersonsVO));
                resultHandleT.setReturnContent(orderRelatedPersonsVO);
                return resultHandleT;
            }

            List<OrdPerson> ordPersonList = loadDirectOrderPersonList(orderId);
            if(CollectionUtils.isEmpty(ordPersonList)){
                log.warn("order " + orderId + " don't have any related person, will return null value.");
                return resultHandleT;
            }
            log.info("direct order person loaded, size is " + ordPersonList.size() + ", now loading person from table ord_item_person_relation");
            List<OrdItemPersonRelation> ordItemPersonRelationList = loadRelationPersonList(orderId);
            log.info("relation persons loaded, size is " + (ordItemPersonRelationList == null?0:ordItemPersonRelationList.size()) + ", now begin to generate OrderRelatedPersonsVO");
            orderRelatedPersonsVO = generateOrderRelatedPersonsVO(ordPersonList, ordItemPersonRelationList);
            log.info("generate OrderRelatedPersonsVO completed, result is " + GsonUtils.toJson(orderRelatedPersonsVO) + ", will be set to mem cache, key is " + key);
            resultHandleT.setReturnContent(orderRelatedPersonsVO);
            //设定缓存
            MemcachedUtil.getInstance().set(key, MemcachedEnum.OrderRelatedPerson.getSec(), orderRelatedPersonsVO);
        } catch (Exception e) {
            log.error("Error loading order related person for order " + orderPersonQueryTO.getOrderId(), e);
            resultHandleT.setMsg(e.getMessage());
        }

        return resultHandleT;
    }

    /**
     * 把查询到的人员List和自备签游玩人List拼装，生成OrderRelatedPersonsVO
     * */
    private OrderRelatedPersonsVO generateOrderRelatedPersonsVO(List<OrdPerson> ordPersonList, List<OrdItemPersonRelation> ordItemPersonRelationList){
        if(CollectionUtils.isEmpty(ordPersonList)){
            return null;
        }
        OrderRelatedPersonsVO orderRelatedPersonsVO = new OrderRelatedPersonsVO();
        //成人数，儿童数
        int adultAmount = 0, childAmount = 0;
        for (OrdPerson ordPerson : ordPersonList) {
            if(ordPerson == null || StringUtils.isEmpty(ordPerson.getPersonType())){
                continue;
            }
            if(ordPerson.getOrdPersonId() == null || ordPerson.getOrdPersonId() < 0){
                continue;
            }

            //如果自备签游玩人不为空，而且ordPersonId又匹配，则把ordPerson设定到自备签游玩人的属性中去
            if(CollectionUtils.isNotEmpty(ordItemPersonRelationList)){
                //循环自备签联系人集合，比较其中的联系人id与外层的person的id是否匹配
                for (OrdItemPersonRelation ordItemPersonRelation : ordItemPersonRelationList) {
                    if(ordItemPersonRelation == null || ordItemPersonRelation.getOrdPersonId() == null || ordItemPersonRelation.getOrdPersonId() < 0){
                        continue;
                    }

                    if(ordItemPersonRelation.getOrdPersonId().longValue() == ordPerson.getOrdPersonId().longValue()){
                        ordItemPersonRelation.setOrdPerson(ordPerson);
                    }
                }
            }

            //下单人
            if(StringUtils.equalsIgnoreCase(OrderEnum.ORDER_PERSON_TYPE.BOOKER.name(), ordPerson.getPersonType())){
                orderRelatedPersonsVO.setBooker(ordPerson);
                continue;
            }
            //订单联系人
            if(StringUtils.equalsIgnoreCase(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name(), ordPerson.getPersonType())){
                orderRelatedPersonsVO.setContact(ordPerson);
                continue;
            }
            //紧急联系人
            if(StringUtils.equalsIgnoreCase(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name(), ordPerson.getPersonType())){
                orderRelatedPersonsVO.setEmergencyContact(ordPerson);
                continue;
            }
            //游玩人
            if(StringUtils.equalsIgnoreCase(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name(), ordPerson.getPersonType())){
                List<OrdPerson> travellerList = orderRelatedPersonsVO.getTravellerList();
                if(CollectionUtils.isEmpty(orderRelatedPersonsVO.getTravellerList())){
                    travellerList = new ArrayList<OrdPerson>();
                    orderRelatedPersonsVO.setTravellerList(travellerList);
                }
                travellerList.add(ordPerson);
                //儿童
                if(StringUtils.equalsIgnoreCase(VstOrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_CHILD.name(), ordPerson.getPeopleType())){
                    childAmount++;
                } else {
                    //成人
                    adultAmount++;
                    if(StringUtils.isBlank(ordPerson.getPeopleType())){
                        orderRelatedPersonsVO.setHasNullPeopleTypeTraveller(true);
                    }
                }
                continue;
            }
            //快递联系人
            if(StringUtils.equalsIgnoreCase(OrderEnum.ORDER_PERSON_TYPE.ADDRESS.name(), ordPerson.getPersonType())){
                //查询快递联系人的地址列表
                Map<String, Object> ordAddressParams = new HashMap<String, Object>();
                ordAddressParams.put("ordPersonId", ordPerson.getOrdPersonId());
                List<OrdAddress> ordAddressList = ordAddressService.findOrdAddressList(ordAddressParams);
                ordPerson.setAddressList(ordAddressList);
                orderRelatedPersonsVO.setAddressee(ordPerson);
                continue;
            }
            //未分类联系人
            List<OrdPerson> currentUnknownPersonList = orderRelatedPersonsVO.getCurrentUnknownPersonList();
            if(CollectionUtils.isEmpty(currentUnknownPersonList)){
                currentUnknownPersonList = new ArrayList<OrdPerson>();
                orderRelatedPersonsVO.setCurrentUnknownPersonList(currentUnknownPersonList);
            }
            currentUnknownPersonList.add(ordPerson);

        }
        orderRelatedPersonsVO.setAdultAmount(adultAmount);
        orderRelatedPersonsVO.setChildAmount(childAmount);
        orderRelatedPersonsVO.setOrdItemPersonRelationList(ordItemPersonRelationList);

        return orderRelatedPersonsVO;
    }

    /**
     * 通过订单id加载直接的订单相关人员信息，也即从ord_person表中取数，并对数据做处理
     * */
    private List<OrdPerson> loadDirectOrderPersonList(Long orderId){
        if(orderId == null || orderId < 0){
            return null;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("objectId", orderId);
        params.put("objectType", OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
        return ordPersonClientService.findOrdPersonList(params);
    }

    /**
     * 通过订单id加载自备签游玩人信息
     * */
    private List<OrdItemPersonRelation> loadRelationPersonList(Long orderId){
        if(orderId == null || orderId < 0){
            return null;
        }
        //根据订单id，查询子订单id
        ResultHandleT<OrdOrder> resultHandleT = orderService.loadOrderWithItemByOrderId(orderId);
        if(resultHandleT == null || resultHandleT.isFail() || resultHandleT.getReturnContent() == null){
            return null;
        }
        OrdOrder ordOrder = resultHandleT.getReturnContent();
        List<OrdOrderItem> orderItemList = ordOrder.getOrderItemList();
        if(CollectionUtils.isEmpty(orderItemList)){
            return null;
        }
        List<Long> orderItemIdList = new ArrayList<Long>();
        for (OrdOrderItem ordOrderItem : orderItemList) {
            if(ordOrderItem == null || ordOrderItem.getOrderItemId() == null || ordOrderItem.getOrderItemId() < 0){
                continue;
            }
            orderItemIdList.add(ordOrderItem.getOrderItemId());
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("orderItemIdArray", orderItemIdList);
        return ordItemPersonRelationClientService.findOrdItemPersonRelationList(params);
    }
}
