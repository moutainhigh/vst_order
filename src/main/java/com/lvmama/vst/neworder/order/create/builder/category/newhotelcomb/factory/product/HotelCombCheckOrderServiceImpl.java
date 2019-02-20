package com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.product;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombOrderService;
import com.lvmama.dest.api.hotelcomb.vo.GoodsCheckResponse;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombBuyInfoVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.dest.hotel.trade.utils.BusinessException;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.SupplierProductInfo;
import com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.ICheckOrderStock;
import com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.VstCheckStockService;
import com.lvmama.vst.order.client.ord.service.impl.OrdOrderClientServiceImpl;

import net.sf.json.JSONArray;

@Component("hotelCombCheckOrderService")
public class HotelCombCheckOrderServiceImpl implements ICheckOrderStock {

	private static final Logger LOG = LoggerFactory.getLogger(HotelCombCheckOrderServiceImpl.class);

	
	@Resource
	IHotelCombOrderService    hotelCombOrderService ;
	
	@Autowired
	private  VstCheckStockService vstCheckOrderStockService ;

	@Override
	public boolean checkStock(List<HotelCombBuyInfoVo> hotelCombBuyInfoVo) {
		RequestBody<List<HotelCombBuyInfoVo>> request = new  RequestBody<List<HotelCombBuyInfoVo>>();
		request.setT(hotelCombBuyInfoVo);
		ResponseBody<GoodsCheckResponse> response =	hotelCombOrderService.checkGoodsAvailable(request);
		GoodsCheckResponse check = response.getT();

		if (!response.isSuccess()) {
			throw  new BusinessException(String.format("%s,%s",check.getCode(),check.getDes()));
		}
		return true;
	}

	@Override
	public boolean checkStock(HotelCombTradeBuyInfoVo.Item item,Long distributionId) {
		ResultHandleT<SupplierProductInfo> handleContent;
		LOG.info("HotelCombCheckOrderServiceImpl  checkStock  is not HotelComb start");
		 
        handleContent = vstCheckOrderStockService.checkStock(item, distributionId);
        if(handleContent.getErrorCode()!=null||!handleContent.isSuccess()){
        	throw  new BusinessException(String.format("%s,%s",handleContent.getErrorCode(),handleContent.getMsg()));
        }
		return true;
	}

//	@Override update by ltwangwei 2017.4.7 16:47
//	public void checkOrder(List<HotelCombBuyInfoVo> hotelCombBuyInfoVo) {
//			this.checkStock(hotelCombBuyInfoVo);
//	}
//
//
//	private void checkStock(List<HotelCombBuyInfoVo> hotelCombBuyInfoVo){
//		RequestBody<List<HotelCombBuyInfoVo>> request = new  RequestBody<List<HotelCombBuyInfoVo>>();
//		request.setT(hotelCombBuyInfoVo);
//		ResponseBody<GoodsCheckResponse> response =	hotelCombOrderService.checkGoodsAvailable(request);
//		GoodsCheckResponse check = response.getT();
//
//		if (!response.isSuccess()) {
//			throw  new BusinessException(String.format("%s,%s",check.getCode(),check.getDes()));
//		}
//
//	}


}
