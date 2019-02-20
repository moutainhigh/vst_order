package com.lvmama.vst.order.agent.hotel.impl;

import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.vst.orderproduct.service.IHotelOrderProductQueryService;
import com.lvmama.dest.api.vst.orderproduct.vo.HotelOrdOrderGoodsVO;
import com.lvmama.dest.api.vst.orderproduct.vo.HotelOrdOrderProductQueryVO;
import com.lvmama.dest.api.vst.orderproduct.vo.HotelOrdOrderProductVO;
import com.lvmama.vst.back.order.po.OrdOrderGoods;
import com.lvmama.vst.back.order.vo.OrdOrderGoodsVO;
import com.lvmama.vst.back.order.vo.OrdOrderProductQueryVO;
import com.lvmama.vst.back.order.vo.OrdOrderProductVO;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.agent.hotel.IHotelOrderProductQueryServiceAgent;
import com.lvmama.vst.order.utils.OrderGoodsUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouyanqun on 2016/12/15.
 */
@Component
public class HotelOrderProductQueryServiceAgentImpl implements IHotelOrderProductQueryServiceAgent {
    private static final Log log = LogFactory.getLog(HotelOrderProductQueryServiceAgentImpl.class);
    /**
     * 目的地酒店接口
     * */
    @Resource
    private IHotelOrderProductQueryService hotelOrderProductQueryService;

    @Override
    public List<OrdOrderProductVO> findOrderProductVOList(Map<String, Object> params) {
        RequestBody<Map<String, Object>> request = new RequestBody<>();
        request.setT(params);
        request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
        ResponseBody<List<HotelOrdOrderProductVO>> orderGoodsVOListResponse = hotelOrderProductQueryService.findOrderProductVOList(request);
        String paramJsonString = GsonUtils.toJson(params);
        log.info("begin to invoke IHotelOrderProductQueryService.findOrderProductVOList, param is " + paramJsonString);
        if(orderGoodsVOListResponse == null){
            log.warn("Got null response while invoking IHotelOrderProductQueryService.findOrderProductVOList, param is " + paramJsonString);
            return null;
        }
        if(orderGoodsVOListResponse.isFailure()){
            log.error("Error invoking IHotelOrderProductQueryService.findOrderProductVOList, error msg is " + orderGoodsVOListResponse.getMessage() + ", param is " + paramJsonString);
            return null;
        }
        List<HotelOrdOrderProductVO> hotelOrdOrderProductVOList = orderGoodsVOListResponse.getT();
        if(CollectionUtils.isEmpty(hotelOrdOrderProductVOList)) {
            log.info("IHotelOrderProductQueryService.findOrderProductVOList invoke finished, but result list is empty, param is " + paramJsonString);
            return null;
        }
        StringBuilder logStringBuilder = new StringBuilder();
        logStringBuilder.append("Got response from IHotelOrderProductQueryService.findOrderProductVOList, param is ").append(paramJsonString).append(", result size is ").append(hotelOrdOrderProductVOList.size());
        if(log.isDebugEnabled()) {
            logStringBuilder.append(", result is ").append(GsonUtils.toJson(hotelOrdOrderProductVOList));
        }
        log.info(logStringBuilder.append(", now begin to convert object").toString());
        List<OrdOrderProductVO> ordOrderProductVOList = new ArrayList<>();
        OrdOrderProductVO ordOrderProductVO;
        for (HotelOrdOrderProductVO hotelOrdOrderProductVO : hotelOrdOrderProductVOList) {
            if(hotelOrdOrderProductVO == null) {
                continue;
            }
            ordOrderProductVO = new OrdOrderProductVO();
            EnhanceBeanUtils.copyProperties(hotelOrdOrderProductVO, ordOrderProductVO);
            ordOrderProductVOList.add(ordOrderProductVO);
        }
        //清空
        logStringBuilder.delete(0, logStringBuilder.length());
        logStringBuilder.append("convert object for param ").append(paramJsonString).append(" completed, size is ").append(ordOrderProductVOList.size());
        if(log.isDebugEnabled()) {
            logStringBuilder.append(", result is ").append(GsonUtils.toJson(ordOrderProductVOList));
        }
        log.info(logStringBuilder.toString());
        return ordOrderProductVOList;
    }

    @Override
    public int countOrderProductList(Map<String, Object> params) {
        RequestBody<Map<String, Object>> request = new RequestBody<>();
        request.setT(params);
        request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
        String paramJsonString = GsonUtils.toJson(params);
        log.info("begin to invoke IHotelOrderProductQueryService.countOrderProductList, param is " + paramJsonString);
        ResponseBody<Integer> orderProductListResponseBody = hotelOrderProductQueryService.countOrderProductList(request);
        if(orderProductListResponseBody == null){
            log.warn("Got null response while invoking IHotelOrderProductQueryService.countOrderProductList, param is " + paramJsonString);
            return 0;
        }
        if(orderProductListResponseBody.isFailure()){
            log.error("Error invoking IHotelOrderProductQueryService.countOrderProductList, error msg is " + orderProductListResponseBody.getMessage() + ", param is " + paramJsonString);
            return 0;
        }
        Integer orderProductCount = orderProductListResponseBody.getT();
        if(orderProductCount == null) {
            orderProductCount = 0;
            log.info("IHotelOrderProductQueryService.countOrderProductList invoke finished, but result list is empty, param is " + paramJsonString);
        }
        log.info("Got response from IHotelOrderProductQueryService.countOrderProductList, param is " + paramJsonString + ", result is " + orderProductCount + ", now begin to convert object");
        return orderProductCount;
    }

    /**
     * 酒店商品查询
     *
     * @param params
     */
    @Override
    public List<OrdOrderGoodsVO> findOrderGoodsVOList(HashMap<String, Object> params) {
        RequestBody<Map<String, Object>> request = new RequestBody<>();
        request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
        request.setT(params);
        String paramJsonString = GsonUtils.toJson(params);
        log.info("begin to invoke IHotelOrderProductQueryService.findOrderGoodsVOList, param is " + paramJsonString);
        ResponseBody<List<HotelOrdOrderGoodsVO>> orderGoodsVOListResponse = hotelOrderProductQueryService.findOrderGoodsVOList(request);
        if(orderGoodsVOListResponse == null){
            log.warn("Got null response while invoking IHotelOrderProductQueryService.findOrderGoodsVOList, param is " + paramJsonString);
            return null;
        }
        if(orderGoodsVOListResponse.isFailure()){
            log.error("Error invoking IHotelOrderProductQueryService.findOrderGoodsVOList, error msg is " + orderGoodsVOListResponse.getMessage() + ", param is " + paramJsonString);
            return null;
        }
        List<HotelOrdOrderGoodsVO> hotelOrdOrderGoodsVOList = orderGoodsVOListResponse.getT();
        if(CollectionUtils.isEmpty(hotelOrdOrderGoodsVOList)) {
            log.info("IHotelOrderProductQueryService.findOrderGoodsVOList invoke finished, but result list is empty, param is " + paramJsonString);
            return null;
        }
        StringBuilder logStringBuilder = new StringBuilder();
        logStringBuilder.append("Got response from IHotelOrderProductQueryService.findOrderGoodsVOList, param is ").append(paramJsonString).append(", result size is ").append(hotelOrdOrderGoodsVOList.size());
        if(log.isDebugEnabled()) {
            logStringBuilder.append(", result is ").append(GsonUtils.toJson(hotelOrdOrderGoodsVOList));
        }
        log.info(logStringBuilder.append(", now begin to convert object").toString());
        List<OrdOrderGoodsVO> ordOrderGoodsVOList = new ArrayList<>();
        OrdOrderGoodsVO ordOrderGoodsVO;
        for (HotelOrdOrderGoodsVO hotelOrdOrderGoodsVO : hotelOrdOrderGoodsVOList) {
            if(hotelOrdOrderGoodsVO == null) {
                continue;
            }
            ordOrderGoodsVO = new OrdOrderGoodsVO();
            EnhanceBeanUtils.copyProperties(hotelOrdOrderGoodsVO, ordOrderGoodsVO);
            ordOrderGoodsVOList.add(ordOrderGoodsVO);
        }
        //清空
        logStringBuilder.delete(0, logStringBuilder.length());
        logStringBuilder.append("convert object for param ").append(paramJsonString).append(" completed, size is ").append(ordOrderGoodsVOList.size());
        if(log.isDebugEnabled()) {
            logStringBuilder.append(", result is ").append(GsonUtils.toJson(ordOrderGoodsVOList));
        }
        log.info(logStringBuilder.toString());
        return ordOrderGoodsVOList;
    }

    /**
     * 单酒店下单商品信息查询
     *
     * @param ordOrderProductQueryVO
     */
    @Override
    public List<OrdOrderGoods> getBizBranchPropByParams(OrdOrderProductQueryVO ordOrderProductQueryVO) {
        RequestBody<HotelOrdOrderProductQueryVO> requestBody = new RequestBody<>();
        HotelOrdOrderProductQueryVO hotelOrdOrderProductQueryVO = new HotelOrdOrderProductQueryVO();
        EnhanceBeanUtils.copyProperties(ordOrderProductQueryVO, hotelOrdOrderProductQueryVO);
        requestBody.setT(hotelOrdOrderProductQueryVO);
        requestBody.setToken(Constant.DEST_BU_HOTEL_TOKEN);
        log.info("Now invoke dest hotel interface IHotelOrderProductQueryService.getBizBranchPropByParams, param is " + GsonUtils.toJson(requestBody));
        ResponseBody<List<HotelOrdOrderGoodsVO>> hotelOrdOrderGoodsVOListResponse = hotelOrderProductQueryService.getBizBranchPropByParams(requestBody);
        //打印错误信息用
        String errorInvokeStr = "Error occurs while invoke dest hotel interface IHotelOrderProductQueryService.getBizBranchPropByParams";
        if(hotelOrdOrderGoodsVOListResponse == null) {
            log.error(errorInvokeStr + ", response is null");
            return null;
        }
        if(hotelOrdOrderGoodsVOListResponse.isFailure()) {
            log.error(errorInvokeStr + ", msg is " + hotelOrdOrderGoodsVOListResponse.getErrorMessage());
            return null;
        }
        if(hotelOrdOrderGoodsVOListResponse.getT() == null) {
            log.error(errorInvokeStr + ", query result is null");
            return null;
        }
        List<HotelOrdOrderGoodsVO> hotelOrdOrderGoodsVOList = hotelOrdOrderGoodsVOListResponse.getT();
        log.info("IHotelOrderProductQueryService.getBizBranchPropByParams invoke succeed, result size is " + hotelOrdOrderGoodsVOList.size());
        List<OrdOrderGoods> orderGoodsList = OrderGoodsUtil.toVstOrderGoodsVoList(hotelOrdOrderGoodsVOList);
        log.info("IHotelOrderProductQueryService.getBizBranchPropByParams hotelOrdOrderGoodsVOList VO convert completed, size is " + CollectionUtils.size(orderGoodsList));
        return orderGoodsList;
    }

    /**
     * 单酒店下单商品信息查询
     *
     * @param ordOrderProductQueryVO
     */
    @Override
    public List<OrdOrderGoods> getBizCategoryPropByParams(OrdOrderProductQueryVO ordOrderProductQueryVO) {
        RequestBody<HotelOrdOrderProductQueryVO> requestBody = new RequestBody<>();
        HotelOrdOrderProductQueryVO hotelOrdOrderProductQueryVO = new HotelOrdOrderProductQueryVO();
        EnhanceBeanUtils.copyProperties(ordOrderProductQueryVO, hotelOrdOrderProductQueryVO);
        requestBody.setT(hotelOrdOrderProductQueryVO);
        requestBody.setToken(Constant.DEST_BU_HOTEL_TOKEN);
        log.info("Now invoke dest hotel interface IHotelOrderProductQueryService.getBizCategoryPropByParams, param is " + GsonUtils.toJson(requestBody));
        ResponseBody<List<HotelOrdOrderGoodsVO>> hotelOrdOrderGoodsVOListResponse = hotelOrderProductQueryService.getBizCategoryPropByParams(requestBody);
        //打印错误信息用
        String errorInvokeStr = "Error occurs while invoke dest hotel interface IHotelOrderProductQueryService.getBizCategoryPropByParams";
        if(hotelOrdOrderGoodsVOListResponse == null) {
            log.error(errorInvokeStr + ", response is null");
            return null;
        }
        if(hotelOrdOrderGoodsVOListResponse.isFailure()) {
            log.error(errorInvokeStr + ", msg is " + hotelOrdOrderGoodsVOListResponse.getErrorMessage());
            return null;
        }
        if(hotelOrdOrderGoodsVOListResponse.getT() == null) {
            log.error(errorInvokeStr + ", query result is null");
            return null;
        }
        List<HotelOrdOrderGoodsVO> hotelOrdOrderGoodsVOList = hotelOrdOrderGoodsVOListResponse.getT();
        log.info("IHotelOrderProductQueryService.getBizCategoryPropByParams invoke succeed, result size is " + hotelOrdOrderGoodsVOList.size());
        List<OrdOrderGoods> orderGoodsList = OrderGoodsUtil.toVstOrderGoodsVoList(hotelOrdOrderGoodsVOList);
        log.info("method is getBizCategoryPropByParams hotelOrdOrderGoodsVOList VO convert completed, size is " + CollectionUtils.size(orderGoodsList));
        return orderGoodsList;
    }
}
