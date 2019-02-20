package com.lvmama.vst.order.service.book.impl.connects;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrderEnum;

import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderInitBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 基础交通接驳数据初始化及校验
 * @author jszhouwei
 *
 */
@Component("connectsOrderInitBussiness")
public class ConnectsOrderInitBussiness extends AbstractBookService implements OrderInitBussiness{
	
	private static final Logger logger = LoggerFactory.getLogger(ConnectsOrderInitBussiness.class);
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;

	@Autowired
	private DistrictClientService districtClientService;

	@Override
	public boolean initOrderItem(OrdOrderItem orderItem, OrdOrderDTO order) {

		//EBK是否支持发邮件
		SuppGoods suppGoods = orderItem.getSuppGoods();
		if(null!=suppGoods && null!=suppGoods.getEbkEmailFlag()){
			orderItem.setEbkEmailFlag(suppGoods.getEbkEmailFlag());
		}
        orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.notify_type.name(), suppGoods.getNoticeType());
		return true;
	}

}
