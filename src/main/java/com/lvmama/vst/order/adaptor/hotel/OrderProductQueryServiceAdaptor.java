package com.lvmama.vst.order.adaptor.hotel;

import com.lvmama.vst.back.order.po.OrdOrderGoods;
import com.lvmama.vst.back.order.vo.OrdOrderGoodsVO;
import com.lvmama.vst.back.order.vo.OrdOrderProductQueryVO;
import com.lvmama.vst.back.order.vo.OrdOrderProductVO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouyanqun on 2016/12/15.
 */
public interface OrderProductQueryServiceAdaptor {
    /**
     * 查询酒店产品信息
     * */
    List<OrdOrderProductVO> findOrderProductVOList(HashMap<String, Object> params);

    /**
     * 酒店产品信息计数
     * */
    int countOrderProductList(Map<String,Object> params);

    /**
     * 查询酒店商品信息
     * */
    List<OrdOrderGoodsVO> findOrderGoodsVOList(HashMap<String, Object> params);

    /**
     * 查询酒店商品和规格信息
     * */
    List<OrdOrderGoods> getBizBranchPropByParams(OrdOrderProductQueryVO ordOrderProductQueryVO);

    List<OrdOrderGoods> getBizCategoryPropByParams(OrdOrderProductQueryVO ordOrderProductQueryVO);
}
