package com.lvmama.vst.order.snapshot.factory;

import com.lvmama.order.snapshot.comm.po.goods.SuppGoodsVo;
import com.lvmama.order.snapshot.comm.po.prod.ProdProductBranchVo;
import com.lvmama.order.snapshot.comm.po.prod.ProdProductVo;
import com.lvmama.order.snapshot.comm.vo.param.OrderItemParamVo;
import com.lvmama.order.snapshot.comm.vo.param.OrderParamVo;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.back.prod.po.PropValue;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.order.snapshot.vo.SourceBeanVo;
import com.lvmama.vst.order.snapshot.vo.TargetBeanVo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 快照接口参数工厂
 */
public class SnapshotParamFactory {
    /**
     * 产品转换
     * @param prodProduct
     * @return
     */
    public static ProdProductVo convertSnapshotProdProduct(ProdProduct prodProduct){
        ProdProductVo target =new ProdProductVo();
        if(prodProduct ==null) return target;

        EnhanceBeanUtils.copyProperties(prodProduct, target);
        //PropValue 转换
        convertPropValueMap(target.getPropValue());

        return target;
    }
    /**
     * 产品规格转换
     * @param prodProductBranch
     * @return
     */
    public static ProdProductBranchVo convertSnapshotProdProductBranch(ProdProductBranch prodProductBranch){
        ProdProductBranchVo target =new ProdProductBranchVo();
        if(prodProductBranch ==null) return target;

        EnhanceBeanUtils.copyProperties(prodProductBranch, target);
        //PropValue 转换
        convertPropValueMap(target.getPropValue());

        return target;
    }
    /**
     * 商品转换
     * @param suppGoods
     * @return
     */
    public static SuppGoodsVo convertSnapshotSuppGoods(SuppGoods suppGoods){
        SuppGoodsVo target =new SuppGoodsVo();
        if(suppGoods ==null) return target;

        EnhanceBeanUtils.copyProperties(suppGoods, target);
        //PropValue 转换
        convertPropValueMap(target.getProdProductBranch().getPropValue());

        return target;
    }
    /**
     * 主单转换
     * @param ordOrder
     * @return
     */
    public static OrderParamVo convertOrderParamVo(OrdOrder ordOrder){
        OrderParamVo target =new OrderParamVo();
        if(ordOrder ==null) return target;

        EnhanceBeanUtils.copyProperties(ordOrder, target);
        target.setOrderAmountItemByType(ordOrder.getOrderAmountItemByType(OrderEnum.ORDER_AMOUNT_TYPE.PROMOTION_PRICE.name()));
        return target;
    }
    /**
     * 子单转换
     * @param ordOrderItem
     * @return
     */
    public static OrderItemParamVo convertOrdOrderItem(OrdOrderItem ordOrderItem){
        OrderItemParamVo target =new OrderItemParamVo();
        if(ordOrderItem ==null) return target;

        EnhanceBeanUtils.copyProperties(ordOrderItem, target);
        return target;
    }
    /**
     * convertPropValueMap
     */
    private static void convertPropValueMap(Map<String, Object> propValueMap){
        if(propValueMap ==null) return ;
        //PropValue 转换
        Iterator<Map.Entry<String, Object>> it = propValueMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if(value ==null) continue;
            if(List.class.isAssignableFrom(value.getClass())){
                List<PropValue> propValueList = (List<PropValue>) value;
                SourceBeanVo sourceBeanVo =SourceBeanVo.install(propValueList);
                TargetBeanVo targetBeanVo = TargetBeanVo.install();
                //copy
                EnhanceBeanUtils.copyProperties(sourceBeanVo, targetBeanVo);
                propValueMap.put(key, targetBeanVo.getPropValueList());
            }
        }
    }
}
