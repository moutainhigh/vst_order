package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.prom.service.BuyPresentClientService;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TICKET_TYPE;
import com.lvmama.vst.back.prom.vo.PromBuyPresentInfoParams;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyPresentActivityInfo;
import com.lvmama.vst.comm.vo.order.OrdOrderDTO;


@Component
public class PromBuyPresentforClient {
	private static Logger logger = LoggerFactory.getLogger(PromBuyPresentforClient.class);
	@Autowired
	private BuyPresentClientService buyPresentClientService;
	
	public BuyPresentActivityInfo findPromBuyPresent(OrdOrderDTO order){
		try {
			List<OrdOrderPack> packList = order.getOrderPackList();
			long quantity = 0;
			Long userId = order.getBuyInfo().getUserNo();
			//未登陆用户直接返回
			if(userId==null){
				return null;
			}
			long amount = order.getOughtAmount();
			ResultHandleT<BuyPresentActivityInfo>  result=null;
			if(packList!=null&&!packList.isEmpty()){
				OrdOrderPack pack = packList.get(0);
				Long productId = pack.getProductId();
				Long categoryId = pack.getCategoryId();
				//邮轮组合产品
				if(8==categoryId){
					List<OrdOrderItem> itemList = pack.getOrderItemList();
					for(OrdOrderItem item:itemList){
						if("true".equals(item.getMainItem())){
							quantity+=item.getQuantity();
						}
					}
				}
				else{
					//儿童数  
					  Object childQuantityObj = pack.getContentValueByKey(ORDER_TICKET_TYPE.child_quantity.name());
					  //成人数
					  Object adultQuantityObj = pack.getContentValueByKey(ORDER_TICKET_TYPE.adult_quantity.name());
					  if(childQuantityObj!=null){
						  quantity+= Integer.parseInt(childQuantityObj.toString());
					  }
					  if(adultQuantityObj!=null){
						  quantity+= Integer.parseInt(adultQuantityObj.toString());
					  }
                      if(quantity <= 0L){
                          Object quantityObj=pack.getContentValueByKey(ORDER_TICKET_TYPE.quantity.name());
                          if(quantityObj!=null){
                              quantity=Long.valueOf(quantityObj.toString());
                          }
                      }
                }
				 result =  buyPresentClientService.gainToOrderActivity(productId, 0, amount, quantity, userId);
			}
			else{
				List<OrdOrderItem> itemList = order.getOrderItemList();
				Long categoryId = order.getCategoryId();
				//景点门票存在一个订单多个商品，多个活动的情况处理
				if(categoryId!=null && Constant.VST_CATEGORY.CATEGORY_SINGLE_TICKET.getCategoryId().equals(categoryId.toString())){
					List<PromBuyPresentInfoParams> paramsList = new ArrayList<PromBuyPresentInfoParams>();
					for(OrdOrderItem item:itemList){
						if(11 ==item.getCategoryId()){
							PromBuyPresentInfoParams params = new PromBuyPresentInfoParams();
							params.setActivityTypeCount(item.getQuantity());
							params.setActivityTypeMoney(item.getQuantity()*item.getPrice());
							params.setProdGoodsId(item.getSuppGoodsId());
							params.setProdGoodsType(1);
							paramsList.add(params);
						}
						result= buyPresentClientService.gainToOrderActivityListIds(paramsList, userId);
					}
				}
				else{
					OrdOrderItem mainItem = null;
					for(OrdOrderItem item:itemList){
						if("true".equals(item.getMainItem())){
							mainItem = item;
						}
					}
					quantity = mainItem.getQuantity();
					result = buyPresentClientService.gainToOrderActivity(mainItem.getSuppGoodsId(), 1, amount, quantity, userId);
				}
			}
			if(result.getReturnContent()!=null){
				  return result.getReturnContent();
			  }
		} catch (Exception e) {
			logger.error("PromBuyPresentBussiness 计算赠品操作异常",e);
		}
		return null;
		
	}

}
