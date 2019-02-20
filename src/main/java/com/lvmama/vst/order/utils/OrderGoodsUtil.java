package com.lvmama.vst.order.utils;

import com.lvmama.dest.api.vst.orderproduct.vo.HotelOrdOrderGoodsVO;
import com.lvmama.vst.back.order.po.OrdOrderGoods;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyanqun on 2017/2/21.
 */
public class OrderGoodsUtil {
    public static List<OrdOrderGoods> toVstOrderGoodsVoList(List<HotelOrdOrderGoodsVO> hotelOrdOrderGoodsVOList) {
        if(CollectionUtils.isEmpty(hotelOrdOrderGoodsVOList)) {
            return null;
        }
        List<OrdOrderGoods> orderGoodsList = new ArrayList<>();
        for (HotelOrdOrderGoodsVO hotelOrdOrderGoodsVO : hotelOrdOrderGoodsVOList) {
            OrdOrderGoods ordOrderGoods = new OrdOrderGoods();
            EnhanceBeanUtils.copyProperties(hotelOrdOrderGoodsVO, ordOrderGoods);
            orderGoodsList.add(ordOrderGoods);
        }
        return orderGoodsList;
    }
}
