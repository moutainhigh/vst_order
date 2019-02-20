package com.lvmama.vst.order.dao.goods;


import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.goods.po.SuppGoodsShareStockLog;
import com.lvmama.vst.comm.mybatis.MyBatisOraDao;

@Repository
public class SuppGoodsShareStockLogOraDao extends MyBatisOraDao {

	public SuppGoodsShareStockLogOraDao() {
		super("SUPP_GOODS_SHARE_STOCK_LOG");
	}
	

	  public int insert(SuppGoodsShareStockLog record) {
	    	return super.insert("insert", record);
	    }
    
}