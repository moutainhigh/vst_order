package com.lvmama.vst.order.service.book.impl.play;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderInitBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 基础当地玩乐(购物美食娱乐)数据初始化及校验
 *
 */
@Component("playOrderInitBussiness")
public class PlayOrderInitBussiness extends AbstractBookService implements	OrderInitBussiness {

	@Override
	public boolean initOrderItem(OrdOrderItem orderItem,final OrdOrderDTO order) {
		if (order.isCreateFlag()) {
			String useTime = orderItem.getUseTime();
			String localHotelAddress = orderItem.getLocalHotelAddress();
			Map<String, Object> contentMap = orderItem.getContentMap();
			if(StringUtil.isNotEmptyString(localHotelAddress)){
				contentMap.put(OrderEnum.ORDER_PLAY_TYPE.localHotelAddress.name(),localHotelAddress);
			}
			if(StringUtil.isNotEmptyString(useTime)){
				contentMap.put(OrderEnum.ORDER_PLAY_TYPE.useTime.name(), useTime);
			}
			//EBK是否支持发邮件
			SuppGoods suppGoods = orderItem.getSuppGoods();
			if(null!=suppGoods && null!=suppGoods.getEbkEmailFlag()){
				orderItem.setEbkEmailFlag(suppGoods.getEbkEmailFlag());
			}
		}
		return true;

	}


}
