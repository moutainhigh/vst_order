package com.lvmama.vst.precontrol.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.precontrol.vo.VstOrderItemVo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

/**
 * Created by zhouguoliang on 2017/4/25.
 */
@Repository
public class RemoteResPrecontrolBindGoodsDao extends MyBatisDao {

    public RemoteResPrecontrolBindGoodsDao() {
        super("REMOTE_RES_PRECONTROL_BIND_GOODS");
    }

    public Long getOrderItemNum(Map param) {
        return super.get("getOrderItemNum", param);
    }

    public Long getHotelOrderItemNum(Map param) {
        return super.get("getHotelOrderItemNum", param);
    }
    
    /**
	 * 设置vst子订单为买断订单，非酒店
	 * @param params 如下
     * <li>goodIds -商品idList</li>
     * <li>startDate -预控起始日期</li>
     * <li>endDate -预控结束日期</li>
	 */
    public void updateVstBudgetFlagBylist(Map<String, Object> params) {
        super.update("updateVstBudgetFlagBylist", params);
    }

    /**
 	 * 查询预控期内非买断的子订单，非酒店
 	 * @param params
	 * <li>goodIds -商品idList</li>
     * <li>startDate -预控起始日期</li>
     * <li>endDate -预控结束日期</li>
 	 * @return
 	 */
    public List<VstOrderItemVo> getVstNotBuyoutOrder(Map<String, Object> params) {
        return super.getList("getVstNotBuyoutOrder", params);
    }

    public List<Long> getPreControlPolicyHistoryOrder(Map<String, Object> params) {
		return super.getList("getPreControlPolicyHistoryOrder", params);
	}

    public int setVstOrderItemBudgetFlag(Map<String, Object> params) {
		 return super.update("setVstOrderItemBudgetFlag", params);
	}

    /**
 	 * 查询预控期内非买断的子订单，酒店
 	 * @param params
	 * <li>goodIds -商品idList</li>
     * <li>startDate -预控起始日期</li>
     * <li>endDate -预控结束日期</li>
 	 * @return
 	 */
	public List<VstOrderItemVo> getVstNotBuyoutOrderHotel(Map<String, Object> params) {
		return super.getList("getVstNotBuyoutOrderHotel", params);
	}

	/**
	 * 设置vst子订单为买断订单，酒店
	 * @param params 如下
     * <li>goodIds -商品idList</li>
     * <li>startDate -预控起始日期</li>
     * <li>endDate -预控结束日期</li>
	 */
	public int updateVstBudgetFlagBylistHotel(Map<String, Object> params) {
		 return super.update("updateVstBudgetFlagBylistHotel", params);
	}

	/**
	 * 批量更新为买断，酒店
	 * @param list
	 */
	public int updateOrderBatchHotel(List<VstOrderItemVo> list) {
		 return super.update("updateOrderBatchHotel", list);
	}
}
