package com.lvmama.vst.order.utils;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.constant.ApportionConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Objects;

/**
 * Created by zhouyanqun on 2017/4/18.
 * 分摊相关的辅助
 */
public class ApportionUtil {
    private static final Log log = LogFactory.getLog(ApportionUtil.class);
    //是否执行分摊
    private static Boolean apportionEnabled;
    //是否开启分销的供应商打包跟团游的订单实付的修正
    private static Boolean distributorActPaidCorrectEnable;
    
    //是否走lvmm_order_Apportion系统（订单子系统）
    private static Boolean lvmmOrderApportion;
    

    /**是否执行分摊*/
    public static boolean isApportionEnabled(){
        if(apportionEnabled == null) {
            log.info("apportion enabled is not set, will get config");
            String apportionEnabledStr = Constant.getInstance().getProperty("orderApportion.enabled");
            if(apportionEnabledStr != null) {
                apportionEnabled = Boolean.valueOf(apportionEnabledStr);
            } else {
                apportionEnabled = Boolean.FALSE;
            }
            log.info("Apportion enabled string is " + apportionEnabledStr + ",apportion enabled is set to " + apportionEnabled);
        }

        return apportionEnabled;
    }

    /**是否修正分销订单实付的修正*/
    public static boolean isDistributorActPaidCorrectEnabled(){
        if(distributorActPaidCorrectEnable == null) {
            log.info("Distributor act paid correct enabled is not set, will get config");
            String correctEnabledStr = Constant.getInstance().getProperty("distributorActPaidApportionCorrect.enabled");
            if(correctEnabledStr != null) {
                distributorActPaidCorrectEnable = Boolean.valueOf(correctEnabledStr);
            } else {
                distributorActPaidCorrectEnable = Boolean.FALSE;
            }
            log.info("Distributor act paid correct enabled string is " + correctEnabledStr + ",apportion enabled is set to " + distributorActPaidCorrectEnable);
        }

        return distributorActPaidCorrectEnable;
    }
    /**
     * 订单是否走订单分摊子系统（lvmm_order_apportion）
     * @return
     */
    public static boolean isLvmmOrderApportion(){
        if(lvmmOrderApportion == null) {
            log.info("lvmmOrderApportion enabled is not set, will get config");
            String lvmmOrderApportionEnabledStr = Constant.getInstance().getProperty("lvmmOrderApportion.enabled");
            if(lvmmOrderApportionEnabledStr != null) {
            	lvmmOrderApportion = Boolean.valueOf(lvmmOrderApportionEnabledStr);
            } else {
            	lvmmOrderApportion = Boolean.FALSE;
            }
            log.info("lvmmOrderApportion enabled string is " + lvmmOrderApportionEnabledStr + ",apportion enabled is set to " + apportionEnabled);
        }
        return lvmmOrderApportion;
    }

    /**
     * 是否需要分摊到价格类型上去，比如按成人价，儿童价分摊
     * 目前的规则是
     * 子订单品类=跟团游，自由行，当地游，定制游；优惠分摊，订单金额减少分摊记录到价格类型这一层级，如成人价，儿童价，单房差；按照销售总金额比例分摊，将销售金额最大的放在最后做减法计算。
     * 子订单品类=其它机票，其它巴士，其它火车票；优惠分摊，订单金额减少分摊记录到价格类型这一层级，如成人价，儿童价；按照销售总金额比例分摊，将销售金额最大的放在最后做减法计算。
     * 子订单品类=酒店；优惠分摊，订单金额减少分摊；记录到每个入住日期上，如2017-01-01；按照销售总金额比例分摊，将销售金额最大的放在最后做减法计算。
     * 除以上几个品类外，都直接记录到子订单上。
     *
     */
    public static OrderEnum.ORDER_APPORTION_PARTICLE judgeApportionParticle(Long categoryId) {
        if(categoryId == null || categoryId <= 0) {
            return OrderEnum.ORDER_APPORTION_PARTICLE.apportion_particle_item;
        }

        //按价格类型分，含房差
        if(Objects.equals(categoryId, BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId())
                || Objects.equals(categoryId, BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId())
                || Objects.equals(categoryId, BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId())
                || Objects.equals(categoryId, BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId())
                ){
            return OrderEnum.ORDER_APPORTION_PARTICLE.apportion_particle_price_type_with_spread;
        }

        if(Objects.equals(categoryId, BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId())
                || Objects.equals(categoryId, BizEnum.BIZ_CATEGORY_TYPE.category_traffic_bus_other.getCategoryId())
                || Objects.equals(categoryId, BizEnum.BIZ_CATEGORY_TYPE.category_traffic_train_other.getCategoryId())
                || Objects.equals(categoryId, BizEnum.BIZ_CATEGORY_TYPE.category_traffic_ship_other.getCategoryId())) {
            return OrderEnum.ORDER_APPORTION_PARTICLE.apportion_particle_price_type_without_spread;
        }

        //按入住日期分
        if(Objects.equals(categoryId, BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId())) {
            return OrderEnum.ORDER_APPORTION_PARTICLE.apportion_particle_check_in_date;
        }

        return OrderEnum.ORDER_APPORTION_PARTICLE.apportion_particle_item;
    }

    /**
     * 根据品类返回对应的相关的多价格类型
     * 每个品类对应一种分摊粒度，见本类的judgeApportionParticle方法，每种分摊粒度又对应多个价格类型
     * */
    public static String[] catchRelatedPriceTypeArray(Long categoryId){
        OrderEnum.ORDER_APPORTION_PARTICLE orderApportionParticle = judgeApportionParticle(categoryId);
        switch (orderApportionParticle) {
            case apportion_particle_item: return ApportionConstants.apportionByItemRelatedPriceTypeArray;
            case apportion_particle_price_type_with_spread: return ApportionConstants.apportionByPriceTypeWithSpreadRelatedPriceTypeArray;
            case apportion_particle_price_type_without_spread: return ApportionConstants.apportionByPriceTypeNoSpreadRelatedPriceTypeArray;
            case apportion_particle_check_in_date: return ApportionConstants.apportionByCheckInDateRelatedPriceTypeArray;
            default: return ApportionConstants.apportionAllRelatedPriceTypeArray;
        }
    }

    /**
     * 判断某个分摊品类是否是支付前的分摊，支付前的分摊包括优惠、促销、渠道优惠，手工改价
     * */
    public static boolean isBeforePaymentApportionType(String costCategory) {
        return StringUtils.isNotBlank(costCategory) && (StringUtils.equals(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_coupon.name(), costCategory)
                || StringUtils.equals(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_promotion.name(), costCategory)
                || StringUtils.equals(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_distributor.name(), costCategory)
                || StringUtils.equals(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_manual.name(), costCategory));
    }
    
    /**
     * 判断某个分摊品类是否不是支付前的分摊，支付前的分摊包括优惠、促销、渠道优惠，手工改价
     * 如果传入空串，也返回true
     * */
    public static boolean isNotBeforePaymentApportionType(String costCategory) {
        return !isBeforePaymentApportionType(costCategory);
    }
    
    /**
     * 判断是否是实付分摊
     * @param costCategory
     * @return
     */
    public static boolean isActualPaymentApportionType(String costCategory){
    	return StringUtils.equals(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name(), costCategory);
    }
    
    /**
     * 判断是否非实付分摊，
     * true:表示优惠分摊（优惠、促销、渠道优惠，手工改价、支付立减）
     * false:表示实付分摊
     * @param costCategory
     * @return
     */
    public static boolean isNotActualPaymentApportionType(String costCategory){
    	return !StringUtils.equals(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name(), costCategory);
    }

    /**
     * 判断分摊粒度是否是按价格类型
     * */
    public static boolean isPriceTypeParticle(OrderEnum.ORDER_APPORTION_PARTICLE orderApportionParticle) {
        return orderApportionParticle == OrderEnum.ORDER_APPORTION_PARTICLE.apportion_particle_price_type_with_spread
                || orderApportionParticle == OrderEnum.ORDER_APPORTION_PARTICLE.apportion_particle_price_type_without_spread;
    }

    /**
     * 判断订单是否不再需要分摊
     * 目前不再需要分摊的订单是指超时未支付的订单
     * */
    public static boolean doNotNeedApportionAnyMore(OrdOrder order) {
        return order == null || StringUtils.equals(order.getCancelCode(), OrderEnum.ORDER_CANCEL_CODE.TIME_OUT_WAIT.getCode());
    }
}
