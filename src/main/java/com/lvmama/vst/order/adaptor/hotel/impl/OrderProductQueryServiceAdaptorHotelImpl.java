package com.lvmama.vst.order.adaptor.hotel.impl;

import com.lvmama.dest.api.utils.DynamicRouterUtils;
import com.lvmama.vst.back.order.po.OrdOrderGoods;
import com.lvmama.vst.back.order.vo.OrdOrderGoodsVO;
import com.lvmama.vst.back.order.vo.OrdOrderProductQueryVO;
import com.lvmama.vst.back.order.vo.OrdOrderProductVO;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.order.adaptor.hotel.OrderProductQueryServiceAdaptor;
import com.lvmama.vst.order.agent.hotel.IHotelOrderProductQueryServiceAgent;
import com.lvmama.vst.order.service.IOrderProductQueryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouyanqun on 2016/12/15.
 */
@Component
public class OrderProductQueryServiceAdaptorHotelImpl implements OrderProductQueryServiceAdaptor {
    private static final Log log = LogFactory.getLog(OrderProductQueryServiceAdaptorHotelImpl.class);
    /**
     * 目的地酒店查询接口
     * */
    @Resource
    private IHotelOrderProductQueryServiceAgent hotelOrderProductQueryServiceAgent;
    /**
     * vst查询接口
     * */
    @Resource
    private IOrderProductQueryService orderProductQueryService;
    /**
     * 查询酒店产品信息
     *
     * @param params
     */
    @Override
    public List<OrdOrderProductVO> findOrderProductVOList(HashMap<String, Object> params) {
        String paramJsonStr = GsonUtils.toJson(params);
        //根据酒店系统是否上线判断走哪条路径
        if(judgeHotelSystemEnabled()) {
            log.info("Hotel system is on line, will invoke dest hotel interface for OrdOrderProductVO list with param " + paramJsonStr);
            return hotelOrderProductQueryServiceAgent.findOrderProductVOList(params);
        }
        log.info("Hotel system is not on line, will invoke vst interface for OrdOrderProductVO list with param " + paramJsonStr);
        return orderProductQueryService.findOrderProductVOList(params);
    }

    /**
     * 酒店产品信息计数
     *
     * @param params
     */
    @Override
    public int countOrderProductList(Map<String, Object> params) {
        String paramJsonStr = GsonUtils.toJson(params);
        if(judgeHotelSystemEnabled()){
            log.info("Hotel system is on line, will invoke dest hotel interface OrdOrderProductVO count with param " + paramJsonStr);
            return hotelOrderProductQueryServiceAgent.countOrderProductList(params);
        }
        log.info("Hotel system is not on line, will invoke vst interface for OrdOrderProductVO count with param " + paramJsonStr);
        return orderProductQueryService.countOrderProductList(params);
    }

    /**
     * 查询酒店商品信息
     *
     * @param params
     */
    @Override
    public List<OrdOrderGoodsVO> findOrderGoodsVOList(HashMap<String, Object> params) {
        String paramJsonStr = GsonUtils.toJson(params);
        if(judgeHotelSystemEnabled()){
            log.info("Hotel system is on line, will invoke dest hotel interface OrdOrderGoodsVO list with param " + paramJsonStr);
            return hotelOrderProductQueryServiceAgent.findOrderGoodsVOList(params);
        }
        log.info("Hotel system is not on line, will invoke vst interface for OrdOrderGoodsVO list with param " + paramJsonStr);
        return orderProductQueryService.findOrderGoodsVOList(params);
    }

    //判断酒店系统是否生效
    private boolean judgeHotelSystemEnabled(){
        return DynamicRouterUtils.getInstance().isHotelSystemOnlineEnabled();
    }

    /**
     * 查询酒店商品和规格信息
     *
     * @param ordOrderProductQueryVO
     */
    @Override
    public List<OrdOrderGoods> getBizBranchPropByParams(OrdOrderProductQueryVO ordOrderProductQueryVO) {
        String paramJsonStr = GsonUtils.toJson(ordOrderProductQueryVO);
        if(judgeHotelSystemEnabled()){
            log.info("Hotel system is on line, will invoke dest hotel interface method getBizBranchPropByParams OrdOrderGoodsVO list with param " + paramJsonStr);
            return hotelOrderProductQueryServiceAgent.getBizBranchPropByParams(ordOrderProductQueryVO);
        }
        log.info("Hotel system is not on line, will invoke vst interface method getBizBranchPropByParams for OrdOrderGoodsVO list with param " + paramJsonStr);
        return orderProductQueryService.getBizBranchPropByParams(ordOrderProductQueryVO);
    }

    @Override
    public List<OrdOrderGoods> getBizCategoryPropByParams(OrdOrderProductQueryVO ordOrderProductQueryVO) {
        String paramJsonStr = GsonUtils.toJson(ordOrderProductQueryVO);
        if(judgeHotelSystemEnabled()){
            log.info("Hotel system is on line, will invoke dest hotel interface method getBizCategoryPropByParams OrdOrderGoodsVO list with param " + paramJsonStr);
            return hotelOrderProductQueryServiceAgent.getBizCategoryPropByParams(ordOrderProductQueryVO);
        }
        log.info("Hotel system is not on line, will invoke vst interface method getBizCategoryPropByParams for OrdOrderGoodsVO list with param " + paramJsonStr);
        return orderProductQueryService.getBizCategoryPropByParams(ordOrderProductQueryVO);
    }
}
