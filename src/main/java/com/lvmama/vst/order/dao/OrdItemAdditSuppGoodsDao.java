package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrderItemAdditSuppGoods;
import com.lvmama.vst.back.order.vo.OrderItemAdditSuppGoodsVo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository("ordItemAdditSuppGoodsDao")
public class OrdItemAdditSuppGoodsDao extends MyBatisDao{
	
	public OrdItemAdditSuppGoodsDao(){
		super("ORD_ITEM_ADDITSUPPGOODS");
	}
	
	public List<OrderItemAdditSuppGoodsVo>   getOrderItemAdditSuppGoodsList(Map<String,Object>  paramMap){
		return super.getList("getOrderItemAdditSuppGoodsList", paramMap);
	}

	public int  insertOrdItemAdditSuppGoods(OrderItemAdditSuppGoods orderItemAdditSuppGoods){
		return super.insert("addOrderItemAdditSupp", orderItemAdditSuppGoods);
	}
}
