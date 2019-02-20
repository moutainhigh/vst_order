package com.lvmama.vst.order.agent.hotel;

import com.lvmama.vst.back.order.po.OrdOrderGoods;
import com.lvmama.vst.back.order.vo.OrdOrderGoodsVO;
import com.lvmama.vst.back.order.vo.OrdOrderProductQueryVO;
import com.lvmama.vst.back.order.vo.OrdOrderProductVO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouyanqun on 2016/12/15.
 * IHotelOrderProductQueryService服务的代理
 */
public interface IHotelOrderProductQueryServiceAgent {
    /**
     * 查酒店产品
     * */
    List<OrdOrderProductVO> findOrderProductVOList(Map<String, Object> params);
    /**
     * 酒店产品计数
     * */
    int countOrderProductList(Map<String,Object> params);
    /**
     * 酒店商品查询
     */
    List<OrdOrderGoodsVO> findOrderGoodsVOList(HashMap<String, Object> params);

    /**
     * 单酒店下单商品信息查询
     * */
    List<OrdOrderGoods> getBizBranchPropByParams(OrdOrderProductQueryVO ordOrderProductQueryVO);

    /**
     * 单酒店下单商品信息查询
     * */
    List<OrdOrderGoods> getBizCategoryPropByParams(OrdOrderProductQueryVO ordOrderProductQueryVO);
}
