package com.lvmama.vst.order.utils;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.vo.Constant;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by zhouyanqun on 2017/5/22.
 */
public class OrdOrderItemUtils {
	public static List<Long> supplierIdList =new ArrayList<Long>();
	static{
		initSupplierIdList();
	}
    /**
     * 获取订单子单id列表
     * */
    public static List<Long> getOrderItemIdList(OrdOrder order) {
        if (order == null || order.getOrderItemList() == null) {
            return null;
        }
        List<Long> orderItemList = new ArrayList<>();
        for (OrdOrderItem orderItem : order.getOrderItemList()) {
            if (orderItem == null || NumberUtils.isNotAboveZero(orderItem.getOrderItemId())) {
                continue;
            }
            orderItemList.add(orderItem.getOrderItemId());
        }

        return orderItemList;
    }

    /**
     * 检查子单是否是保险子单
     * 除非子单品类是保险，其它情况(子单为null，品类为null,品类不是保险)都返回false
     * */
    public static boolean isInsuranceOrderItem(OrdOrderItem orderItem){
        return orderItem != null && Objects.equals(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId(), orderItem.getCategoryId());
    }

    /**
     * 检查子单是否是酒店子单
     * 除非子单品类是酒店，其它情况(子单为null，品类为null,品类不是酒店)都返回false
     * */
    public static boolean isHotelOrderItem(OrdOrderItem orderItem) {
        return orderItem != null && Objects.equals(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId(), orderItem.getCategoryId());
    }

    /**
     * 检查子单是否是酒店套餐子单
     * 除非子单品类是酒店套餐，其它情况(子单为null，品类为null,品类不是酒店套餐)都返回false
     * */
    public static boolean isHotelComboOrderItem(OrdOrderItem orderItem) {
        return orderItem != null && Objects.equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId(), orderItem.getCategoryId());
    }

    /**
     * 检查子单是否是邮轮子单
     * 除非子单品类是(邮轮、邮轮附加项、岸上观光)，其它情况(子单为null，品类为null,品类不是酒店套餐)都返回false
     * */
    public static boolean isShipOrderItem(OrdOrderItem orderItem) {
        return orderItem != null && (Objects.equals(BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCategoryId(), orderItem.getCategoryId())
        || Objects.equals(BizEnum.BIZ_CATEGORY_TYPE.category_cruise_addition.getCategoryId(), orderItem.getCategoryId())
        || Objects.equals(BizEnum.BIZ_CATEGORY_TYPE.category_sightseeing.getCategoryId(), orderItem.getCategoryId()));
    }
    
    /**
     * 初始化供应商id集合
     */
    private static void initSupplierIdList(){
        //非即时确认供应商{星海,大都市,港捷旅,长隆{广州长隆,珠海横琴,珠海马戏,珠海企鹅,携程}}
        //{3522L,25046L,5400L,3732L,7403L,13910L,14130L,27299L}
        String supplier_id_list = Constant.getInstance().getProperty("supplier_id_list");
        if (supplier_id_list ==null) {
            supplier_id_list ="3522L,25046L,5400L,3732L,7403L,13910L,14130L,27299L";
        }
        for (String idStr : supplier_id_list.split(",")) {
            if (StringUtils.isNotBlank(idStr)) {
                supplierIdList.add(Long.valueOf(idStr));
            }
        }
    }
    
    /**
     * 校验供应商异步确认子订单
     * @param orderItem
     * @return
     */
    public static boolean checkSupplierIdItem(OrdOrderItem orderItem){
        if(orderItem.hasSupplierApi() && StringUtils.isNotBlank(orderItem.getConfirmStatus())){
            return true;
        }
        return false;
    }
    
    /**
     * 是否非即时确认供应商
     * @param supplierId
     * @return
     */
    public static boolean isNotImmediately (Long supplierId){
        return supplierIdList.contains(supplierId);
    }
}
