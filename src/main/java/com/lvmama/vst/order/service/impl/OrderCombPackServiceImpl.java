package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.prod.vo.ProdOrderPackVO;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.order.service.IOrderCombPackService;

/**
 * 
 * @author sunjian
 *
 */
@Service
public class OrderCombPackServiceImpl implements IOrderCombPackService {
	
	private static final Log LOG = LogFactory.getLog(OrderCombPackServiceImpl.class);
	
	@Autowired
	private ProdProductClientService prodProductClientService;

	@Override
	public ResultHandle validateOrderCombPack(BuyInfo buyInfo) {
		ResultHandle resultHandle = new ResultHandle();
		if (buyInfo != null) {
			if (buyInfo.getCategoryId() == null) {
				resultHandle.setMsg("组合产品的品类ID未设置。");
				return resultHandle;
			}
			
			if (buyInfo.getProductId() == null) {
				resultHandle.setMsg("组合产品ID未设置。");
				return resultHandle;
			}
			
			ProdOrderPackVO prodOrderPack = new ProdOrderPackVO();
			prodOrderPack.setCategoryId(buyInfo.getCategoryId());
			prodOrderPack.setProductId(buyInfo.getProductId());
			Map<Long, Long> goodsQuantityMap = new HashMap<Long, Long>();
			prodOrderPack.setGoodsQuantityMap(goodsQuantityMap);
			
			List<Item> itemList = buyInfo.getItemList();
			if (itemList != null && !itemList.isEmpty()) {
				for (Item item : itemList) {
					if (null == item) {
						resultHandle.setMsg("您选择了无效的商品。");
						break;
					} else if (null == item.getGoodsId()) {
						resultHandle.setMsg("您选择的商品不存在。");
						break;
					} else {
						goodsQuantityMap.put(item.getGoodsId(), (long) item.getQuantity());
					}
				}
				
				if (resultHandle.isFail()) {
					return resultHandle;
				}
				
				if (buyInfo.getTravellers() == null || buyInfo.getTravellers().isEmpty()) {
					resultHandle.setMsg("未设置游玩人。");
					return resultHandle;
				}
				
				prodOrderPack.setTravellerCount(buyInfo.getTravellers().size());
				
				resultHandle = prodProductClientService.validateOrderPack(prodOrderPack);
				LOG.info("validateOrderCombPack:[resultHandle.isSuccess=" + resultHandle.isSuccess() + ",msg= " + resultHandle.getMsg() + "]");
			} else {
				resultHandle.setMsg("您未选购商品。");
			}
		} else {
			resultHandle.setMsg("下单信息无效。");
		}
		
		return resultHandle;
	}

}
