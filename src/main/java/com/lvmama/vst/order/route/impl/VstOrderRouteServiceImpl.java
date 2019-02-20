package com.lvmama.vst.order.route.impl;

import com.lvmama.config.common.ZooKeeperConfigProperties;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.order.route.po.OrderRouteRelationInfo;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.order.route.IVstOrderRouteService;
import com.lvmama.vst.order.route.constant.JedisEnum;
import com.lvmama.vst.order.route.constant.OrderSystemEnum;
import com.lvmama.vst.order.route.constant.VstRouteConstants;
import com.lvmama.vst.order.route.service.IOrderRouteRelationInfoService;
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

@Component
public class VstOrderRouteServiceImpl implements IVstOrderRouteService {
    private static final Log log = LogFactory.getLog(VstOrderRouteServiceImpl.class);

    /**
     * */
    @Resource
    private IOrderRouteRelationInfoService orderRouteRelationInfoService;

    /**
     * 根据订单号路由
     *
     * @param orderId 订单号
     */
    @Override
    public boolean isOrderRouteToNewSys(Long orderId) {
        log.info("Order " + orderId + " is routing");
        boolean isNewOrderSys =false;
        try {
            // 检查总开关
            if (!this.isSwitchOn()) {
                log.info("Route switch is OFF");
                return false;
            }

            // 检查参数
            if (orderId == null) {
                // 订单号为空，老系统
                log.warn("Route param is incomplete: order id is null");
                return false;
            }

            // 检查配置的品类是否为空
            List<Long> routeCategoryList = this.getRouteCategoryList();
            if (CollectionUtils.isEmpty(routeCategoryList)) {
                log.warn("Route config param is incomplete: routeCategoryList");
                return false;
            }

            // 获取订单路由对象
            OrderRouteRelationInfo orderRouteRelationInfo = this.getOrderRouteRelationInfoByOrder(orderId);
            // 路由结果
            isNewOrderSys = this.judgeByRelationInfo(orderRouteRelationInfo);
            log.info("Order " + orderId + " is route to " + isNewOrderSys);
        } catch (Exception e) {
            log.error("Error route order " + orderId, e);
        }

        return isNewOrderSys;
    }

    /**
     * 根据子单号路由
     *
     * @param orderId
     */
    @Override
    public boolean isOrderItemRouteToNewSys(Long orderItemId) {
        log.info("Order item " + orderItemId + " routing");
        boolean isNewOrderSys = false;
        try {
            // 检查总开关
            if (!this.isSwitchOn()) {
                log.info("Route switch is OFF");
                return false;
            }

            // 检查参数
            if (orderItemId == null) {
                log.warn("Route param is incomplete: order item id is null");
                return false;
            }

            // 检查配置的品类是否为空
            List<Long> routeCategoryList = this.getRouteCategoryList();
            if (CollectionUtils.isEmpty(routeCategoryList)) {
                log.warn("Route config param is incomplete: routeCategoryList");
                return false;
            }

            // 获取订单路由对象
            OrderRouteRelationInfo orderRouteRelationInfo = this.getOrderRouteRelationInfoByItem(orderItemId);
            // 路由结果
            isNewOrderSys = this.judgeByRelationInfo(orderRouteRelationInfo);
            log.info("Order item " + orderItemId + " is route to " + isNewOrderSys);
        } catch (Exception e) {
            log.error("Error route order item " + orderItemId, e);
        }

        return isNewOrderSys;
    }

    /**
     * 无参数的路由
     */
    @Override
    public boolean isRequestRouteToNewSys() {
        boolean routeResult = false;
        // 检查总开关
        try {
            if (!this.isSwitchOn()) {
                routeResult = false;
            } else {
                //检查配置
                String emptyParamRouteConfig = ZooKeeperConfigProperties.getProperties(VstRouteConstants.EMPTY_PARAM_ROUTE_CONFIG);
                routeResult = StringUtils.equals(VstRouteConstants.FLAG_Y, emptyParamRouteConfig);
            }
        } catch (Exception e) {
            log.error("Error routing request", e);
        }
        log.info("Non param routing result is " + routeResult);
        return routeResult;
    }

    @Override
    public boolean isRequestRouteToNewSys4Ord2() {
    	 boolean routeResult = false;
         // 检查总开关
         try {
             if (this.isNewSwitchOn()) {
                 routeResult = true;
             }
         } catch (Exception e) {
             log.error("Error routing request", e);
         }
         return routeResult;
    }
    
    /**
     * 是否把job路由到新系统
     */
    @Override
    public boolean isJobRouteToNewSys() {
        boolean routeResult = false;
        try {
            // 检查总开关
            if (!this.isSwitchOn()) {
                routeResult = false;
            } else {
                //检查配置
                String jobSwitchStatus = ZooKeeperConfigProperties.getProperties(VstRouteConstants.KEY_JOB_SWITCH);
                routeResult = StringUtils.equals(jobSwitchStatus, OrderSystemEnum.ORDER_ROUTE_SWITCH_STATUS.ON.getStatus());
            }
        } catch (Exception e) {
            log.error("Error routing job", e);
        }
        log.info("Job routing result is " + routeResult);
        return routeResult;
    }

    @Override
    public String getVersion() {
        String version = ZooKeeperConfigProperties.getProperties(VstRouteConstants.WORKFLOW_VERSION);
        if(StringUtils.isBlank(version)){
            version="v5";
        }
        return version;
    }

    @Override
    public boolean isRouteToNewWorkflow(Long categoryId) {
        try {

            if (categoryId == null) {
                log.warn("categoryId is null");
                return false;
            }

            // 检查配置的品类是否为空
            List<Long> workflowCategoryList = this.getWorflowCategoryList();
            if (CollectionUtils.isEmpty(workflowCategoryList)) {
                log.warn("new workflow category list is empty");
                return false;
            }
            log.info("workflow category list size: " + workflowCategoryList.size());

            return workflowCategoryList.contains(categoryId);
        } catch (Exception e) {
            log.error("categoryId " + categoryId + " met error while routing, msg is " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isRouteToNewWorkflowBySingleHotel(OrdOrder ordOrder) {
        Long categoryId=ordOrder.getCategoryId();
        boolean isNewWorkflow=this.isRouteToNewWorkflow(categoryId);
        String newWf=ordOrder.getNewWorkflowFlag();
        /**
         * 1.新系统开放
         * 2.新版工作流标识
         * 3.品类为1 单酒店
         */
        if(isNewWorkflow
                &&("Y".equalsIgnoreCase(newWf))
                &&(categoryId.equals(1L))){
            return true;
        }
        return false;
    }

    /**
     * 根据主单查询路由信息
     * @param orderId
     * @return
     */
    private OrderRouteRelationInfo getOrderRouteRelationInfoByOrder(Long orderId){
        // 检查缓存
        String cacheKey = JedisEnum.JEDIS_NAPE_ENUM.JEDIS_NAPE_ORDER_ROUTE.getKey() + orderId;
        OrderRouteRelationInfo orderRouteRelationInfo = MemcachedUtil.getInstance().get(cacheKey);
        if(orderRouteRelationInfo != null){
            return orderRouteRelationInfo;
        }
        // 查询订单服务系统
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orderId", orderId);
        orderRouteRelationInfo = orderRouteRelationInfoService.queryOrderRouteRelationInfo(paramMap);
        MemcachedUtil.getInstance().set(cacheKey, JedisEnum.JEDIS_NAPE_ENUM.JEDIS_NAPE_ORDER_ROUTE.getSeconds(), orderRouteRelationInfo);
        return orderRouteRelationInfo;
    }

    /**
     * 根据子单查询路由信息
     * @param orderItemId
     * @return
     */
    private OrderRouteRelationInfo getOrderRouteRelationInfoByItem(Long orderItemId){
        // 检查缓存
        String cacheKey = JedisEnum.JEDIS_NAPE_ENUM.JEDIS_NAPE_ORDER_ITEM_ROUTE.getKey() + orderItemId;
        OrderRouteRelationInfo orderRouteRelationInfo = MemcachedUtil.getInstance().get(cacheKey);
        if(orderRouteRelationInfo != null){
            return orderRouteRelationInfo;
        }
        // 查询订单服务系统
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orderItemId", orderItemId);
        orderRouteRelationInfo = orderRouteRelationInfoService.queryOrderRouteRelationInfo(paramMap);
        MemcachedUtil.getInstance().set(cacheKey, JedisEnum.JEDIS_NAPE_ENUM.JEDIS_NAPE_ORDER_ROUTE.getSeconds(), orderRouteRelationInfo);
        return orderRouteRelationInfo;
    }

    /**
     * 根据查询结果，判断路由
     * */
    private boolean judgeByRelationInfo(OrderRouteRelationInfo orderRouteRelationInfo){
        // 没有路由信息，走vst_order
        if (orderRouteRelationInfo == null) {
            return false;
        }
        //品类判断
        if (!this.judgeByCategory(orderRouteRelationInfo)) {
            return false;
        }
        //测试单判断
        if (this.judgeByTestOrderFlag(orderRouteRelationInfo)) {
            return true;
        }

        //判断分流
        return this.judgeByShunt(orderRouteRelationInfo);
    }

    /**
     * 检查总开关
     * */
    private boolean isSwitchOn() {
        String switchStatus = ZooKeeperConfigProperties.getProperties(VstRouteConstants.TOTAL_SWITCH);
        return StringUtils.equalsIgnoreCase(switchStatus, OrderSystemEnum.ORDER_ROUTE_SWITCH_STATUS.ON.getStatus());
    }

    /**
     * 检查总开关订单二期
     * */
    private boolean isNewSwitchOn() {
        String switchStatus = ZooKeeperConfigProperties.getProperties(VstRouteConstants.TOTAL_SWITCH_NEW);
        return StringUtils.equalsIgnoreCase(switchStatus, OrderSystemEnum.ORDER_ROUTE_SWITCH_STATUS.ON.getStatus());
    }

    /**
     * 检查品类是否应该路由到新系统
     * */
    private boolean judgeByCategory(OrderRouteRelationInfo orderRouteRelationInfoVo) {
        List<Long> routeCategoryList = this.getRouteCategoryList();
        return routeCategoryList != null && routeCategoryList.contains(orderRouteRelationInfoVo.getOrderCategoryId());
    }

    /**
     * 根据测试单标识判断是否路由到新系统
     * */
    private boolean judgeByTestOrderFlag(OrderRouteRelationInfo orderRouteRelationInfoVo) {
        return 'Y' == orderRouteRelationInfoVo.getIsTestOrder();
    }

    /**
     * 根据分流规则判断是否路由到新系统
     * */
    private boolean judgeByShunt(OrderRouteRelationInfo orderRouteRelationInfo) {
        if (orderRouteRelationInfo == null || orderRouteRelationInfo.getOrderCategoryId() == null) {
            return false;
        }
        //订单品类
        Long orderCategoryId = orderRouteRelationInfo.getOrderCategoryId();
        //配置的变量的键
        String configKey = VstRouteConstants.MODULO_CONFIG_PREFIX + orderCategoryId;
        //流量模
        String shuntModuloStr = ZooKeeperConfigProperties.getProperties(configKey);
        if (StringUtils.isBlank(shuntModuloStr)) {
            return false;
        }
        int shuntModulo;
        try {
            shuntModulo = Integer.valueOf(shuntModuloStr);
        } catch (Exception e) {
            log.warn("Shunt mode is illegal:" + shuntModuloStr + ", order is " + orderRouteRelationInfo.getOrderId());
            return false;
        }
        return orderRouteRelationInfo.getOrderId() % shuntModulo == VstRouteConstants.SHUNT_REMAINDER;

    }

    /**
     * 获取需要路由的品类List
     * */
    private List<Long> getRouteCategoryList() {
        List<Long> routeCategoryList = new ArrayList<>();
        String routeCategories = ZooKeeperConfigProperties.getProperties(VstRouteConstants.CATEGORIES_CONFIG_KEY);
        if (StringUtils.isBlank(routeCategories)) {
            return routeCategoryList;
        }
        String[] routeCategoryArray = routeCategories.split(",");
        if (routeCategoryArray.length == 0){
            return routeCategoryList;
        }
        for (String categoryIdStr : routeCategoryArray) {
            if (StringUtils.isBlank(categoryIdStr)) {
                continue;
            }

            routeCategoryList.add(Long.valueOf(categoryIdStr));
        }

        return routeCategoryList;
    }

    /**
     * 获取新工作流的品类List
     * */
    private List<Long> getWorflowCategoryList() {
        List<Long> workflowCategoryList = new ArrayList<>();
        String routeCategories = ZooKeeperConfigProperties.getProperties(VstRouteConstants.NEW_WORKFLOW_CATEGORIES_KEY);
        if (StringUtils.isBlank(routeCategories)) {
            return workflowCategoryList;
        }
        String[] routeCategoryArray = routeCategories.split(",");
        if (routeCategoryArray.length == 0){
            return workflowCategoryList;
        }
        for (String categoryIdStr : routeCategoryArray) {
            if (StringUtils.isBlank(categoryIdStr)) {
                continue;
            }

            workflowCategoryList.add(Long.valueOf(categoryIdStr));
        }

        return workflowCategoryList;
    }


}
