/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.wifi;

import java.util.Date;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.lvmama.vst.back.client.wifi.service.WifiClientService;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdOrderWifiTimeRate;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.wifi.po.OrdOrderWifiPickingPoint;
import com.lvmama.vst.comm.utils.order.OrderUtil;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.dao.OrdOrderWifiPickingPointDao;
import com.lvmama.vst.order.dao.OrdOrderWifiTimeRateDao;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderItemSaveBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;


@Component("wifiOrderItemSaveBussiness")
public class WifiOrderItemSaveBussiness extends AbstractBookService implements OrderItemSaveBussiness{
	
	@Autowired
	private OrdOrderStockDao orderStockDao;
	@Autowired
	private OrdOrderWifiTimeRateDao ordOrderWifiTimeRateDao;
	@Autowired
	OrdOrderWifiPickingPointDao ordOrderWifiPickingPointDao;
	@Autowired
	private WifiClientService wifiClientService;
	
	@Override
	public void saveAddition(OrdOrderDTO order, OrdOrderItem orderItem) {
		if ((orderItem != null) && (order!=null)) {
			//wifi时间段快照
			if(orderItem.getOrdOrderWifiTimeRateList()!= null&&orderItem.getOrdOrderWifiTimeRateList().size()>0 && ProdProduct.WIFIPRODUCTTYPE.WIFI.name().equals(OrderUtil.getProductType(orderItem))){
				for (OrdOrderWifiTimeRate ordOrderWifiTimeRate : orderItem.getOrdOrderWifiTimeRateList()) {
					ordOrderWifiTimeRate.setOrderItemId(orderItem.getOrderItemId());
					ordOrderWifiTimeRate.setUpdateTime(new Date());
					ordOrderWifiTimeRateDao.insertSelective(ordOrderWifiTimeRate);
					saveOrderStockInWifiTimeRate(ordOrderWifiTimeRate);
				}
				//wifi产品保存网点信息
				OrdOrderWifiPickingPoint ordOrderWifiPickingPoint = orderItem.getOrdOrderWifiPickingPoint();
				if(ordOrderWifiPickingPoint!=null){
					ordOrderWifiPickingPoint.setOrderItemId(orderItem.getOrderItemId());
					ordOrderWifiPickingPointDao.insertSelective(ordOrderWifiPickingPoint);
				}
			}else if(ProdProduct.WIFIPRODUCTTYPE.PHONE.name().equals(OrderUtil.getProductType(orderItem))){
				if(CollectionUtils.isNotEmpty(orderItem.getOrderStockList())){
					for(OrdOrderStock stock:orderItem.getOrderStockList()){
						stock.setOrderItemId(orderItem.getOrderItemId());
						stock.setObjectId(orderItem.getOrderItemId());
						stock.setObjectType(OrderEnum.ORDER_STOCK_OBJECT_TYPE.ORDERITEM.name());
						orderStockDao.insert(stock);
					}
				}
			}
		}
		
		
	}
	
	/**
	 * 更新WIFI类订单本地库存量
	 * 
	 * @param hotelTimeRate
	 */
	private void saveOrderStockInWifiTimeRate(OrdOrderWifiTimeRate wifiTimeRate) {
		if ((wifiTimeRate != null) && (wifiTimeRate.getOrderStockList() != null)) {
			for (OrdOrderStock orderStock : wifiTimeRate.getOrderStockList()) {
				orderStock.setObjectId(wifiTimeRate.getWifiTimeRateId());
				orderStock.setObjectType(OrderEnum.ORDER_STOCK_OBJECT_TYPE.WIFI_TIME_RATE.name());
				orderStock.setOrderItemId(wifiTimeRate.getOrderItemId());
				orderStockDao.insertSelective(orderStock);
			}
		}
	}


	@Override
	public void saveOrderItemPersonRelation(OrdOrderItem orderItem) {
		
	}

}
