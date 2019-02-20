package com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.product;

import com.google.common.collect.Maps;
import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.dest.api.prodrefund.interfaces.IProdRefundService;
import com.lvmama.dest.api.product.interfaces.IHotelBranchQueryApiService;
import com.lvmama.dest.api.product.interfaces.IHotelProductQueryApiService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsRefund;
import com.lvmama.vst.back.newHotelcomb.po.NewHotelCombTimePrice;
import com.lvmama.vst.back.order.exception.OrderException;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.back.prod.po.ProdRefundRule;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.neworder.order.create.builder.category.AbstractOrderBuilder;
import com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.adapter.IHotelSysProductAdpaterService;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.service.book.NewHotelComOrderBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by dengcheng on 17/3/31.
 */
public abstract  class AbstractDTOProduct {

    public  static ThreadLocal<Map<String,Object>> queryThreadCache  = new ThreadLocal<Map<String,Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            return Maps.newConcurrentMap();
        }
    };

    public  void dbLoader(OrderHotelCombBuyInfo buyInfo) {};
    /**
     * 创建订单头
     * @param order
     * @param  buyInfo
     * @return
     */
    public OrdOrderDTO buildOrderHeader(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {return null;};

    /**
     *创建订单项目
     * @param order
     * @param  buyInfo
     * @return
     */
    public OrdOrderDTO buildOrderItem(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {return null;};

    /**
     * 创建订单游客相关
     * @param order
     * @param  buyInfo
     * @return
     */
    public OrdOrderDTO buildOrderPerson(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {return null;}

    /**
     * 构建订单游玩日期项目
     * @param order
     * @param buyInfo
     * @return
     */
    public OrdOrderDTO buildOrderVisitTime(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo){return null;}

    /**
     * 构建订单退改策略
     * @param order
     * @param buyInfo
     * @return
     */
    public OrdOrderDTO buildOrderCancelStrategy(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo){return null;};
//
//    /**
//     *
//     * @param order
//     * @param buyInfo
//     * @return
//     */
//    OrdOrderDTO buildOrderlatestCancelTime(OrdOrderDTO order, DestBuBuyInfo buyInfo);

    /**
     * 构建最晚支付等待时间
     * @param order
     * @param buyInfo
     * @return
     */
    public  OrdOrderDTO buildOrderLatestPayedForWait(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo){return null;};




    /**
     * 构建主工作流逻辑
     * @param order
     * @return
     */
    public OrdOrderDTO buildMainWorkFlow(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo){return null;};

    /**
     * 构建支付方式
     * @param order
     * @return
     */
    public OrdOrderDTO buildOrderPaymentType(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo){return null;};

    /**
     * 构建订单资审状态
     * @param order
     * @param buyInfo
     * @return
     */
    public OrdOrderDTO buildOrderResourceConfirmStatus(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo){return null;};

    /**
     * 构建订单金额
     * @param order
     * @param buyInfo
     * @return
     */

    public OrdOrderDTO buildOrderAmmount(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo){return null;};

    /**
     * 构建ManagerId
     * @param order
     * @param buyInfo
     * @return
     */
    public OrdOrderDTO buildManagerId(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo){return null;};

    /**
     * 构建返现项目
     * @param order
     * @param buyInfo
     * @return
     */
    public OrdOrderDTO buildRebate(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo){return null;};

    /**
     * 构建合同项
     * @param order
     * @param buyInfo
     * @return
     */
    public OrdOrderDTO buildtravelContract(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo){return null;};
    
    /**
     * 构建订单展示状态
     * @param order
     * @param buyInfo
     * @return
     */
    public OrdOrderDTO buildOrderViewStatus(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo) {return null;}


    /**
     *判断是否是测试订单
     *@author fangxiang
     *@param order
     *@param buyInfo
     *@return
     *
     */
    public OrdOrderDTO  checkTestOrder(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo){return null;};

  /**
   * 构建促销
   * @author fangxiang
   * @param order
   * @param  buyInfo
   * */
    public OrdOrderDTO buildPromotion(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo){return null;};
    
    /***
     *@author fangxiang
     *@param order
     *@parm buyInfo 
     */
    public OrdOrderDTO buildBonus(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo){return null;};
     
     
    /**
     * 构建游客和订单子项商品关系
     * @param order
     * @param buyInfo
     * @return
     */
    public  OrdOrderDTO buildOrderItemPersonRelation(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo){
        return null;
    }

    private static final Logger LOG = LoggerFactory
            .getLogger(AbstractOrderBuilder.class);


    private static OrderEnum.RESOURCE_STATUS[] RESOURCE_STATUS_ARRAY = {
            OrderEnum.RESOURCE_STATUS.LOCK,
            OrderEnum.RESOURCE_STATUS.UNVERIFIED,
            OrderEnum.RESOURCE_STATUS.AMPLE };


    @Resource
    protected IHotelBranchQueryApiService hotelBranchQueryApiService;

    @Resource
    protected IProdRefundService prodRefundService;

    @Resource
    IHotelProductQueryApiService hotelProductQueryApiService;

    @Resource
    protected NewHotelComOrderBussiness newHotelComOrderBussiness;

    @Resource
    protected IHotelSysProductAdpaterService iHotelSysProductAdpaterService;
//
//
//
//
//    @Autowired
//    protected INewHotelCombTimePriceService newHotelCombTimePriceClientRemote;




    protected void throwIllegalException(String message){
        throw new OrderException("H002", message);
    }


    protected void throwNullException(String message) {
        throw new OrderException("H0001",message);
    }


    /**
     *校验参数
     */
    protected void checkOnsaleFlag(NewHotelCombTimePrice timePrice)
            throws BusinessException {
        if (0 == (timePrice.getOnsaleFlag())) {
            throw new BusinessException("商品游玩日期不可售");
        }
    }

    /**
     * 不需要资源确认
     *
     * @param stock
     */
    protected void makeNotNeedResourceConfirm(final OrdOrderStock stock) {
        stock.setNeedResourceConfirm("false");
        stock.setInventory(OrderEnum.INVENTORY_STATUS.INVENTORY.name());
        stock.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.name());
    }

    /**
     * 需要资源审核的库存项
     *
     * @param stock
     */
    protected void makeNeedResourceConfirm(final OrdOrderStock stock) {
        stock.setNeedResourceConfirm("true");
        stock.setInventory(OrderEnum.INVENTORY_STATUS.UNINVENTORY.name());
        stock.setResourceStatus(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name());
    }

    protected void makeNeedResourceConfirm(OrdOrderItem orderItem,
                                           List<OrdOrderStock> stockList) {
        for (OrdOrderStock stock : stockList) {
            setOrderItemsNeedResourceConfirm(stock.getNeedResourceConfirm(),
                    orderItem);
        }
    }


    /**
     * 设置订单那子项是否需要资源确认
     *
     * @param needResourceConfirm
     * @param orderItem
     */
    private void setOrderItemsNeedResourceConfirm(String needResourceConfirm,
                                                  OrdOrderItem orderItem) {
        if ("true".equals(orderItem.getNeedResourceConfirm())) {
            orderItem.setNeedResourceConfirm(needResourceConfirm);
        }
    }


    protected void checkParam(SuppGoods suppGoods, DestBuBuyInfo.Item item)
            throws BusinessException {

        if (suppGoods == null) {
            throw new BusinessException("商品ID=" + item.getGoodsId() + "不存在");
        }

        if (item.getQuantity() <= 0) {
            throw new BusinessException("商品 " + suppGoods.getGoodsName()
                    + " 订购数量小于等于零");
        }

        if ((null != suppGoods.getMaxQuantity())
                && (item.getQuantity() > suppGoods.getMaxQuantity())) {
            // throw new IllegalArgumentException("商品 " +
            // suppGoods.getGoodsName() + " 订购数量超出最大值");
            throw new BusinessException(
                    OrderStatusEnum.ORDER_ERROR_CODE.OUT_MAXIMUM_DAY
                            .getErrorCode(),
                    "商品 " + suppGoods.getGoodsName() + " 订购数量超出最大值");
        }

        if ((null != suppGoods.getMinQuantity())
                && (item.getQuantity() < suppGoods.getMinQuantity())) {
            throw new BusinessException("商品 " + suppGoods.getGoodsName()
                    + " 订购数量小于最小值");
        }

        if (item.getOwnerQuantity() > item.getQuantity()) {
            throw new BusinessException("商品" + suppGoods.getGoodsName()
                    + "  实际订购数量小于零");

        }
    }

    protected void calcBuCode(OrdOrderDTO order, SuppGoods supp) {
        //主商品bu
        LOG.info("=主商品id==id:"+supp.getSuppGoodsId()+"buCode:"+supp.getBu());
        order.setBuCode(supp.getBu());// bucode
        order.setCompanyType(supp.getCompanyType());// 公司主体
        order.setAttributionId(supp.getAttributionId());// 公司归属地
    }

    protected OrdOrderStock createStock(Date visitTime, long quantity) {
        OrdOrderStock stock = new OrdOrderStock();
        stock.setQuantity(quantity);
        // stock.setInventory(OrderEnum.INVENTORY_STATUS.UNINVENTORY.name());
        stock.setVisitTime(visitTime);
        // stock.setNeedResourceConfirm("true");
        // stock.setResourceStatus(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name());
        return stock;
    }

    protected void makeOrderItemTime(OrdOrderItem item,
                                     NewHotelCombTimePrice newHotelCombTimePrice ) {
		/*if(item.getVisitTime()==null||newHotelCombTimePrice.getSpecDate().before(item.getVisitTime())){
			item.setVisitTime(newHotelCombTimePrice.getSpecDate());
		}*/
        if(newHotelCombTimePrice.getAheadBookTime()!=null){
            Date aheadTime = DateUtils.addMinutes(item.getVisitTime(),-newHotelCombTimePrice.getAheadBookTime().intValue());
            if(item.getAheadTime() == null||aheadTime.before(item.getAheadTime())){
                item.setAheadTime(aheadTime);
            }
        }
//		if(timePrice.getLatestCancelTime()!=null){
//			Date lastCancelTime = DateUtils.addMinutes(timePrice.getSpecDate(), -timePrice.getLatestCancelTime().intValue());
//			if(item.getLastCancelTime()==null||lastCancelTime.before(item.getLastCancelTime())){
//				item.setLastCancelTime(lastCancelTime);
//			}
//		}
    }

    // 设置子项的退改
    protected void setCancelStrategyToOrderItem(OrdOrderDTO order,
                                                ProdRefund refund) throws BusinessException {
        if (order.getRealCancelStrategy() == null
                || ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE
                .getCode().equals(order.getRealCancelStrategy())
                || ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(
                order.getRealCancelStrategy()))
            return;

        if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(
                order.getRealCancelStrategy())) {
            if (CollectionUtils.isEmpty(refund.getProdRefundRules())) {// 退改规则不能为空
                LOG.error("prodProduct product id:{} 无退改规则"
                        + refund.getProductId());
                throw new BusinessException(ErrorCodeMsg.ERR_PROREFUND_002);
            }

            String refundRules = getRefundRulesByOrderItem(order, refund);
            for (OrdOrderItem orderItem : order.getOrderItemList()) {
                orderItem.setCancelStrategy(order.getRealCancelStrategy());
                orderItem.setRefundRules(refundRules);
            }
        }

    }

    /**
     * 获取退改规则JSON
     *
     * @param order
     * @param refund
     *            主产品退改策略
     * @return
     */
    protected String getRefundRulesByOrderItem(OrdOrderDTO order,
                                               ProdRefund refund) {

        List<SuppGoodsRefund> refunds = new ArrayList<SuppGoodsRefund>();
        for (ProdRefundRule rule : refund.getProdRefundRules()) {
            SuppGoodsRefund goodsrefund = new SuppGoodsRefund();
            goodsrefund.setCancelStrategy(refund.getCancelStrategy());
            goodsrefund.setLatestCancelTime(rule.getLongLastTime());
            goodsrefund.setCancelTimeType(rule.getLastTime());
            BeanUtils.copyProperties(rule, goodsrefund);
            refunds.add(goodsrefund);
        }
        String jsonSt = JSONArray.fromObject(refunds).toString();
        if (jsonSt.length() >= 4000) {
            LOG.warn("Order [productId=" + refund.getProductId()
                    + "]'s refundRules_json is out of size 4000/"
                    + jsonSt.length() + " .Has ["
                    + refund.getProdRefundRules().size() + "] refund rules.");
            jsonSt = jsonSt.substring(0, 3999);
        }
        return jsonSt;
    }

    protected String getOrderResourceStatus(final String resourceStatus,
                                            final String newResourceStatus) {
        if (StringUtils.isEmpty(resourceStatus)) {
            return newResourceStatus;
        }
        OrderEnum.RESOURCE_STATUS newStatus = OrderEnum.RESOURCE_STATUS
                .valueOf(newResourceStatus);
        OrderEnum.RESOURCE_STATUS orderResourceStatus = OrderEnum.RESOURCE_STATUS
                .valueOf(resourceStatus);
        int newPos = ArrayUtils.indexOf(RESOURCE_STATUS_ARRAY, newStatus);
        int oldPos = ArrayUtils.indexOf(RESOURCE_STATUS_ARRAY,
                orderResourceStatus);
        if (newPos < oldPos) {
            return newStatus.name();
        } else {
            return orderResourceStatus.name();
        }
    }

    protected void setOrderResourceStatus(final OrdOrderItem orderItem,
                                          final OrdOrderDTO order) {
        String status = getOrderResourceStatus(order.getResourceStatus(),
                orderItem.getResourceStatus());
        if (StringUtils.isNotEmpty(status)) {
            order.setResourceStatus(status);
        }
    }

    /**
     *初始化订单合同信息
     * @param distributorId
     */
    protected void createOrderTravelContract(String template, List<OrdTravelContract> contracts, List<OrdOrderItem> orderItemList, Long distributorId){
        OrdTravelContract travel = new OrdTravelContract();
        travel.setContractTemplate(template);
        travel.setCreateTime(new Date());
        if (distributorId!=null && distributorId==10) {
            travel.setSigningType(VstOrderEnum.ORDER_CONTRACT_SIGNING_TYPE.BRANCHES.getCode());
        }else {
            travel.setSigningType(VstOrderEnum.ORDER_CONTRACT_SIGNING_TYPE.ONLINE.getCode());
        }
        travel.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.UNSIGNED.getCode());
        travel.setOrderItems(orderItemList);
        contracts.add(travel);
    }
	
}
