/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.wifi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.wifi.service.WifiClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.wifi.po.OrdOrderWifiPickingPoint;
import com.lvmama.vst.back.wifi.po.WifiPickingPoint;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfo.WifiAdditation;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderInitBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * 基础wifi数据初始化及校验
 * @author lancey
 *
 */
@Component("wifiOrderInitBussiness")
public class WifiOrderInitBussiness extends AbstractBookService implements OrderInitBussiness {

	
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	
	 @Autowired
	 private WifiClientService wifiClientService;
	 
	 @Autowired
	 private DistrictClientService districtClientService;
	 private static final Logger logger = LoggerFactory.getLogger(WifiOrderInitBussiness.class);
	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.service.book.OrderInitBussiness#initOrderItem(com.lvmama.vst.back.order.po.OrdOrderItem, com.lvmama.vst.order.vo.OrdOrderDTO)
	 */
	@Override
	public boolean initOrderItem(OrdOrderItem orderItem, OrdOrderDTO order) {
		SuppGoods suppGoods = orderItem.getSuppGoods();
		ProdProduct product  = suppGoods.getProdProduct();
		Item  item = orderItem.getItem();
		/**
		 * 下单时检验网点信息
		 * */
		if(order.isCreateFlag()){
			Map<String, Object> contentMap = orderItem.getContentMap();
			contentMap.put(OrderEnum.ORDER_COMMON_TYPE.goodsType.name(),suppGoods.getGoodsType());
			orderItem.setTicketType(suppGoods.getGoodsType());
			if(ProdProduct.WIFIPRODUCTTYPE.WIFI.name().equals(product.getProductType())){
				if(item.getWifiAdditation()!=null){
					WifiAdditation wifiAdditation = item.getWifiAdditation();
					Long tackePickingPointId = wifiAdditation.getTackePickingPointId();
					Long backPickingPointId = wifiAdditation.getBackPickingPointId();
					
					contentMap.put(OrderEnum.ORDER_WIFI_TYPE.lease_startDay.name(),item.getVisitTime());
					contentMap.put(OrderEnum.ORDER_WIFI_TYPE.lease_endDay.name(),wifiAdditation.getBackTime());
					contentMap.put(OrderEnum.ORDER_WIFI_TYPE.picking_type.name(),suppGoods.getGoodsType());
					
					logger.info("WifiOrderInitBussiness init ordOrderWifiPickingPoint info goodType: "+suppGoods.getGoodsType()+"tackePickingPointId:"+tackePickingPointId+"--"+"backPickingPointId:"+backPickingPointId);
					WifiPickingPoint wifiTackePickingPoint = null;
					WifiPickingPoint wifiBackPickingPoint = null;
					OrdOrderWifiPickingPoint ordOrderWifiPickingPoint = new OrdOrderWifiPickingPoint();
					List<Long> pointIdList = new ArrayList<Long>();
				if(SuppGoods.GOODSTYPE.EXPRESSTYPE_DISPLAY.name().equals(suppGoods.getGoodsType())){
					orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.express_type.name(), suppGoods.getExpressType());
					if(backPickingPointId ==null){
						throwIllegalException("还件网点不能为空!");
					}else{
						pointIdList.add(backPickingPointId);
						ResultHandleT<List<WifiPickingPoint>> mailPointResult = wifiClientService.findWifiPickingPoint(suppGoods.getSuppGoodsId(), null, pointIdList, suppGoods.getGoodsType());
						if(mailPointResult == null || mailPointResult.getReturnContent()==null||mailPointResult.getReturnContent().size()==0){
							throwIllegalException("还网点已失效!");;
						}
						wifiBackPickingPoint = mailPointResult.getReturnContent().get(0);
						ordOrderWifiPickingPoint.setBackPickingPointId(wifiBackPickingPoint.getPickingPointId());
						ordOrderWifiPickingPoint.setDistrictId(wifiBackPickingPoint.getDistrictId());
						orderItem.setOrdOrderWifiPickingPoint(ordOrderWifiPickingPoint);
						String cityName = getCityName(wifiBackPickingPoint.getDistrictId());
						contentMap.put(OrderEnum.ORDER_WIFI_TYPE.back_city_name.name(),cityName);
						contentMap.put(OrderEnum.ORDER_WIFI_TYPE.back_picking_point.name(),wifiBackPickingPoint.getPickingAddr());
					}
					
				}else if(SuppGoods.GOODSTYPE.NOTICETYPE_DISPLAY.name().equals(suppGoods.getGoodsType())){
					if(tackePickingPointId == null){
						throwIllegalException("取件网点不能为空!");
					}
					if(backPickingPointId == null){
						throwIllegalException("还件网点不能为空!");
					}
					pointIdList.add(tackePickingPointId);
					
					ResultHandleT<List<WifiPickingPoint>> tackeResult = wifiClientService.findWifiPickingPoint(suppGoods.getSuppGoodsId(), null, pointIdList, suppGoods.getGoodsType());
					if(tackeResult == null || tackeResult.getReturnContent()==null ||tackeResult.getReturnContent().size()==0 ){
						throwIllegalException("取件网点已失效!");
					}
					pointIdList.clear();
					pointIdList.add(backPickingPointId);
					ResultHandleT<List<WifiPickingPoint>> backResult = wifiClientService.findWifiPickingPoint(suppGoods.getSuppGoodsId(), null, pointIdList, suppGoods.getGoodsType());
					if(backResult == null || backResult.getReturnContent()==null ||backResult.getReturnContent().size()==0 ){
						throwIllegalException("还网点已失效!");
					}
					wifiTackePickingPoint = tackeResult.getReturnContent().get(0);
					wifiBackPickingPoint = backResult.getReturnContent().get(0);
					if(!wifiTackePickingPoint.getDistrictId().equals(wifiBackPickingPoint.getDistrictId())){
						throwIllegalException("抱歉，暂时只支持同城市取还网点!");
					}
					ordOrderWifiPickingPoint.setTakePickingPointId(wifiTackePickingPoint.getPickingPointId());
					ordOrderWifiPickingPoint.setDistrictId(wifiTackePickingPoint.getDistrictId());
					ordOrderWifiPickingPoint.setBackPickingPointId(wifiBackPickingPoint.getPickingPointId());
					ordOrderWifiPickingPoint.setDistrictId(wifiBackPickingPoint.getDistrictId());
					orderItem.setOrdOrderWifiPickingPoint(ordOrderWifiPickingPoint);
					String takeCityName = getCityName(wifiTackePickingPoint.getDistrictId());
					String backCityName = getCityName(wifiBackPickingPoint.getDistrictId());
					contentMap.put(OrderEnum.ORDER_WIFI_TYPE.take_city_name.name(),takeCityName);
					contentMap.put(OrderEnum.ORDER_WIFI_TYPE.back_city_name.name(),backCityName);
					contentMap.put(OrderEnum.ORDER_WIFI_TYPE.take_picking_point.name(),wifiTackePickingPoint.getPickingAddr());
					contentMap.put(OrderEnum.ORDER_WIFI_TYPE.back_picking_point.name(),wifiBackPickingPoint.getPickingAddr());
				}
				
				}else{
					throwIllegalException("商品参数异常!");
				}
			}else if(ProdProduct.WIFIPRODUCTTYPE.PHONE.name().equals(product.getProductType())){
				contentMap.put(OrderEnum.ORDER_WIFI_TYPE.lease_startDay.name(),item.getVisitTime());
			}

		}
			
		return true;
	}
	
	private String getCityName(Long cityId){
		ResultHandleT<BizDistrict> cityResult = districtClientService.findDistrictById(cityId);
		if(cityResult!=null && cityResult.getReturnContent()!=null){
			BizDistrict city = cityResult.getReturnContent();
			String cityName = city.getDistrictName();
			if(cityName!=null){
				return cityName;
			}
		}
		return "";
	}
}
