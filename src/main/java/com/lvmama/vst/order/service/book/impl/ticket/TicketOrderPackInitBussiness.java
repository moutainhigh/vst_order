/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.ticket;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.prod.service.ProdPackageGroupClientService;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderPackInitBussiness;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;

/**
 * 初始对应的打包信息
 * @author lancey
 *
 */
@Component("ticketOrderPackInitBussiness")
public class TicketOrderPackInitBussiness extends AbstractBookService implements OrderPackInitBussiness{

	@Autowired
	private ProdPackageGroupClientService prodPackageGroupClientService;
	
	
	
	@Override
	public boolean initOrderPack(OrdOrderPackDTO pack,BuyInfo.Product itemProduct) {
		List<ProdPackageGroup> groupList = prodPackageGroupClientService.getProdPackageGroupByProductId(pack.getProductId());
		if(CollectionUtils.isEmpty(groupList)){
			throwNullException("打包的产品不存在");
		}
		pack.putContent(OrderEnum.ORDER_TICKET_TYPE.quantity.name(), itemProduct.getQuantity());
		//门票打包的产品只会存在一个组
		List<ProdPackageDetail> list = prodPackageGroupClientService.getProdPackageDetailByGroupId(groupList.get(0).getGroupId());
		pack.setPackageDetailList(list);
		List<BuyInfo.Item> itemList = new ArrayList<BuyInfo.Item>();
		for(ProdPackageDetail detail:list){
			BuyInfo.Item item = new BuyInfo.Item();
//			item.setDetailId(detail.getDetailId());
			item.setGoodsId(detail.getObjectId());
			item.setQuantity((int)(itemProduct.getQuantity()));
			item.setVisitTime(itemProduct.getVisitTime());
			itemList.add(item);
		}
		itemProduct.setItemList(itemList);
		return true;
	}

}
