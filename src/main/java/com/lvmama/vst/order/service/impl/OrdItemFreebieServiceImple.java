package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.lvmama.bridge.utils.hotel.DestHotelAdapterUtils;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.goods.interfaces.IHotelFreebieQueryApiService;
import com.lvmama.dest.api.order.vo.HotelOrdItemFreebiesRelation;
import com.lvmama.vst.back.client.hotelFreebie.service.HotelFreebieClientService;
import com.lvmama.vst.back.order.po.OrdItemFreebiesRelation;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.client.ord.service.impl.OrdOrderClientServiceImpl;
import com.lvmama.vst.order.dao.OrdItemFreebieDao;
import com.lvmama.vst.order.service.IOrdItemFreebieService;

@Service("ordItemFreebieService")
public class OrdItemFreebieServiceImple implements IOrdItemFreebieService{

	private static final Logger LOG = LoggerFactory.getLogger(OrdItemFreebieServiceImple.class);
	@Autowired
	private OrdItemFreebieDao ordItemFreebieDao;
	
	@Autowired
	private HotelFreebieClientService hotelFreebieClientService;
	
	@Autowired
	private IHotelFreebieQueryApiService hotelFreebieQueryApiRemote;
	
	@Autowired
	private DestHotelAdapterUtils destHotelAdapterUtils;
	/**
	 * 取消订单,恢复赠品库存
	 * @param orderItemlist
	 * @return
	 */
	@SuppressWarnings("unused")
	@Transactional(propagation=Propagation.REQUIRES_NEW) 
	public void cancelFreebie(List<OrdOrderItem> orderItemlist){		
		int result = 0;
		List<OrdItemFreebiesRelation> freebiesRelations = new ArrayList<OrdItemFreebiesRelation>();
	    Map<String, Object> params = new HashMap<String, Object>();	
		//单酒店子订单只要1个
		OrdOrderItem ordOrderItem = orderItemlist.get(0);
		params.put("suppGoodsId", ordOrderItem.getSuppGoodsId());
		params.put("orderItemId", ordOrderItem.getOrderItemId());
		freebiesRelations = ordItemFreebieDao.queryFreebieListByItem(params);
		boolean isNewHotel = false;//是否走新酒店路由
		isNewHotel = destHotelAdapterUtils.checkHotelRouteEnableByProductId(ordOrderItem.getProductId());
		LOG.info("------cancelFreebie----"+freebiesRelations.size()+"isNewHotel="+isNewHotel+" goodsId"+ordOrderItem.getSuppGoodsId());
		for (OrdItemFreebiesRelation ordItemFreebiesRelation : freebiesRelations) {
			ordItemFreebiesRelation.setConsumeNum(0-ordItemFreebiesRelation.getConsumeNum());
			if(isNewHotel)
			{
				RequestBody<HotelOrdItemFreebiesRelation> request = new RequestBody<HotelOrdItemFreebiesRelation>();
				HotelOrdItemFreebiesRelation hotelOrdItemFreebiesRelation = new HotelOrdItemFreebiesRelation();
				EnhanceBeanUtils.copyProperties(ordItemFreebiesRelation, hotelOrdItemFreebiesRelation);
				request.setT(hotelOrdItemFreebiesRelation);
				request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
				LOG.info("....newHotel productId cancelFreebie updateStockNum start...");
				ResponseBody<Long> response = hotelFreebieQueryApiRemote.updateStockNum(request);
				if(org.apache.commons.lang.StringUtils.isEmpty(response.getErrorMessage()) && null != response.getT()){
					result = response.getT().intValue();
				}
				LOG.info("....newHotel productId cancelFreebie updateStockNum end...");
			}else{
				result= hotelFreebieClientService.updateStockNum(ordItemFreebiesRelation);
			}			
			LOG.info("------取消结果----"+result);
			if(result>0)
			{
				ordItemFreebiesRelation.setCancelTime(new Date());
				ordItemFreebiesRelation.setCancel(0L);
				ordItemFreebieDao.updateItemFreebie(ordItemFreebiesRelation);
			}
		}		
	}
	
	
	@Override
	public int batchInsertOrdItemFreebie(List<OrdItemFreebiesRelation> ordItemfreebies) {
		return ordItemFreebieDao.batchInsert(ordItemfreebies);
	}

	public int batchUpdateOrdItemFreebie(List<OrdItemFreebiesRelation> ordItemfreebies){
		return ordItemFreebieDao.batchUpdate(ordItemfreebies);		
	}
	
	public int insert(OrdItemFreebiesRelation ordItemfreebie){
		return ordItemFreebieDao.insert(ordItemfreebie);
	}	

	@Override
	public List<OrdItemFreebiesRelation> queryFreebieListByItem(Map<String, Object> params) 
	{
		return ordItemFreebieDao.queryFreebieListByItem(params);
	}
		
}
