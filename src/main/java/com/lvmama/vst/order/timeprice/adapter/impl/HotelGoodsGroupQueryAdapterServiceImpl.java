package com.lvmama.vst.order.timeprice.adapter.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.vst.goods.po.HotelGoodsGroupVstPo;
import com.lvmama.dest.api.vst.goods.service.IHotelGoodsGroupQueryVstApiService;
import com.lvmama.dest.api.vst.goods.vo.HotelSuppGoodsGroupStockVo;
import com.lvmama.vst.back.goods.po.SuppGoodsGroupStock;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.timeprice.adapter.IHotelGoodsGroupQueryAdapterService;

@Component("hotelGoodsGroupQueryAdapterService")
public class HotelGoodsGroupQueryAdapterServiceImpl implements
		IHotelGoodsGroupQueryAdapterService {

	private static final Log log = LogFactory.getLog(HotelGoodsGroupQueryAdapterServiceImpl.class);

    /**
     * 目的地酒店查询服务
     * */
    @Autowired
    private IHotelGoodsGroupQueryVstApiService hotelGoodsGroupQueryVstApiService;
    
	@Override
	public List<SuppGoodsGroupStock> selectBySpecDateRangeAndGroupId(
			Map<String, Object> params) {
		if (params==null || params.size()<=0) {
			return null;
		}
        
        RequestBody<HotelGoodsGroupVstPo> requestBody = new RequestBody<HotelGoodsGroupVstPo>();
        HotelGoodsGroupVstPo hotelGoodsGroupVstPo = new HotelGoodsGroupVstPo();
        hotelGoodsGroupVstPo.setGroupId(Long.valueOf(params.get("groupId")+""));
        hotelGoodsGroupVstPo.setBeginDate((Date) params.get("startDate"));
        hotelGoodsGroupVstPo.setEndDate((Date) params.get("endDate"));
        hotelGoodsGroupVstPo.setSpecDate((Date) params.get("specDate"));
        requestBody.setT(hotelGoodsGroupVstPo);
        requestBody.setToken(Constant.DEST_BU_HOTEL_TOKEN);
        log.info("selectBySpecDateRangeAndGroupId,groupId:"+params.get("groupId")+",param:"+params);
        ResponseBody<List<HotelSuppGoodsGroupStockVo>> responseBody = hotelGoodsGroupQueryVstApiService.selectByDateRangeAndGroupId(requestBody);
        
        if (responseBody == null) {
            log.warn("Interface IHotelGoodsGroupQueryVstApiService.selectBySpecDateRangeAndGroupId returned null response");
            return null;
        }
        if(responseBody.isFailure()){
        	log.error("Interface IHotelGoodsGroupQueryVstApiService.selectBySpecDateRangeAndGroupId failed!");
            throw null;
        }

        List<HotelSuppGoodsGroupStockVo> hotelSuppGoodsGroupStockVoList = responseBody.getT();
        if(CollectionUtils.isEmpty(hotelSuppGoodsGroupStockVoList)) {
        	log.warn("Interface IHotelGoodsGroupQueryVstApiService.selectBySpecDateRangeAndGroupId end! list is null!");
            return null;
        }
        
        List<SuppGoodsGroupStock> resultList = new ArrayList<SuppGoodsGroupStock>();
        for (HotelSuppGoodsGroupStockVo vo : hotelSuppGoodsGroupStockVoList) {
        	SuppGoodsGroupStock stockVo = new SuppGoodsGroupStock();
        	EnhanceBeanUtils.copyProperties(vo, stockVo);
        	resultList.add(stockVo);
		}
        
		return resultList;
	}

}
