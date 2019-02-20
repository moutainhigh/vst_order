package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;
import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.control.vo.ResPrecontrolOrderVo;
import com.lvmama.vst.order.service.IOrdOrderHotelTimeRateService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.prod.adapter.ProdProductHotelAdapterClientService;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.mybatis.annotation.ForceRead;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkOrderClientService;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkSupplierGroupClientService;
import com.lvmama.vst.ebooking.ebk.po.EbkSupplierGroup;
import com.lvmama.vst.ebooking.vo.DepartureNoticeVo;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.service.IOrdOrderService;

@Service
public class OrdOrderServiceImpl implements IOrdOrderService{
	
	private static Logger logger = LoggerFactory.getLogger(OrdOrderServiceImpl.class);
	@Autowired
	private OrdOrderDao ordOrderDao;
	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	@Autowired
	private EbkOrderClientService ebkOrderClientServiceReomte;
	
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;
	/**
	 * 产品适配器，决定调用vst还是目的地酒店服务
	 * */
	@Autowired
	private ProdProductHotelAdapterClientService productHotelAdapterClientService;
	
	@Autowired
	private ProdProductClientService productClientService;
	
	@Autowired
	private EbkSupplierGroupClientService ebkSupplierGroupClientRemote;

	@Autowired
	private IOrdOrderHotelTimeRateService ordOrderHotelTimeRateService;
	
	@Override
	public List<DepartureNoticeVo> selectDepartureNoticeList(
			Map<String, Object> paramsMap) {	
		paramsMap = getDepartureNoticeParamsMap(paramsMap);
		List<DepartureNoticeVo>  listNotice = ordOrderDao.selectDepartureNoticeList(paramsMap);	
		if(CollectionUtils.isNotEmpty(listNotice)){
			for (DepartureNoticeVo departureNoticeVo : listNotice) {
				ResultHandleT<ProdProduct> resultProd = productHotelAdapterClientService.findProdProductByIdFromCache(departureNoticeVo.getProductId());
				if(null != resultProd && resultProd.isSuccess()&&null != resultProd.getReturnContent()){
					departureNoticeVo.setSupplierProductName(resultProd.getReturnContent().getSuppProductName());
				}
				
			}
		}
		return listNotice;
	}
	@Override
	public Long getDepartureNoticeCount(Map<String, Object> params) {
		params = getDepartureNoticeParamsMap(params);		
		return ordOrderDao.getDepartureNoticeCount(params);
	}
	@Override
	public List<OrdOrder> getordOrderList(Map<String, Object> params) {
		return ordOrderDao.getordOrderList(params);
	}

	/**
	 * 根据订单id查询订单集合，单表查询
	 *
	 * @param orderIdList
	 */
	@Override
	public List<OrdOrder> getOrderList(List<Long> orderIdList) {
		return ordOrderDao.selectByPrimaryKeyList(orderIdList);
	}

	@Override
	public List<OrdOrderItem> listOrderItemByConditions(Page<OrdOrderItem> page,Map<String, Object> paramMap) {
		List<OrdOrderItem> orderResItemList = new ArrayList<OrdOrderItem>();
		List<OrdOrderItem> orderItemList = ordOrderItemDao.listOrderItemByConditions(paramMap);
		Set<Long> supplierSet = new HashSet<Long>();
		for (OrdOrderItem ordOrderItem : orderItemList) {
			supplierSet.add(ordOrderItem.getSupplierId());
			ResultHandleT<ProdProduct> resultProduct = productHotelAdapterClientService.findProdProductByIdFromCache(ordOrderItem.getProductId());
			if(resultProduct.isSuccess() && "SUPPLIER".equals(resultProduct.getReturnContent().getPackageType())){
				orderResItemList.add(ordOrderItem);
			}
		}	
		//批量查询供应商
		List<Long> supplierIdList = new ArrayList<Long>(supplierSet);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("supplierIds", supplierIdList);
		ResultHandleT<List<SuppSupplier>> resultSupplier = suppSupplierClientService.findSuppSupplierList(map);
		Map<Long, String> supplierMap = new HashMap<Long, String>();
		if(resultSupplier.isSuccess() && null != resultSupplier.getReturnContent()){			
			for (SuppSupplier supplier : resultSupplier.getReturnContent()) {
				supplierMap.put(supplier.getSupplierId(), supplier.getSupplierName());
			}
		}
		if(CollectionUtils.isNotEmpty(orderResItemList) && supplierMap.size()>0){
			for (OrdOrderItem ordOrderItem : orderResItemList){
				if(null != ordOrderItem.getSupplierId()){
					ordOrderItem.setSupplierName(supplierMap.get(ordOrderItem.getSupplierId()));
				}
			}
		}
		return orderResItemList;
	}

	@Override
	public Long listOrderItemByConditionsCount(Map<String, Object> paramMap) {
		return ordOrderItemDao.listOrderItemByConditionsCount(paramMap);
	}

	@Override
	public OrdOrder loadOrderWithItemByOrderId(Long orderId) {
		Assert.notNull(orderId,"orderId can't be null");
		OrdOrder order = ordOrderDao.selectByPrimaryKey(orderId);
		if(order!=null){
			order.setOrderItemList(ordOrderItemDao.selectByOrderId(orderId));
		}
		return order;
	}
	@Override
	public int updateManagerIdPerm(OrdOrder ordOrder) {
		return ordOrderDao.updateManagerIdPerm(ordOrder);
	}

    @Override
    public List<ResPrecontrolOrderVo> findPercontrolGoodsOrderList(Map<String,Object> paramsMap) {
        return ordOrderDao.findPercontrolGoodsOrderList(paramsMap);
    }
    
    @Override
    public List<ResPrecontrolOrderVo> findPercontrolHotelGoodsOrderList(Map<String,Object> paramsMap) {
        return ordOrderDao.findPercontrolHotelGoodsOrderList(paramsMap);
    }

    @Override
    public Long countPercontrolGoodsOrderList(Map<String,Object> paramsMap) {
        return ordOrderDao.countPercontrolGoodsOrderList(paramsMap);
    }
	@Override
	public OrdOrder findByOrderId(Long orderId) {
		if(orderId==null){
			return null;
		}
		return ordOrderDao.selectByPrimaryKey(orderId);
	}
	@Override
	public Long countPercontrolHotelGoodsOrderList(Map<String, Object> paramsMap) {
		
		 return ordOrderDao.countPercontrolHotelGoodsOrderList(paramsMap);
	}

	@Override
	public List<OrdOrder> findHotelOrderListByParams(Map<String, Object> params) {
		List<OrdOrder> ordOrderList = ordOrderDao.findHotelOrderListByParams(params);
		for(OrdOrder ordOrder : ordOrderList){
			List<OrdOrderItem> ordOrderItemList = ordOrderItemDao.selectByOrderId(ordOrder.getOrderId());
			if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrder.getCategoryId())){
				for(OrdOrderItem orderItem : ordOrderItemList){
					Map<String, Object> itemParams = new HashMap<String, Object>();
					itemParams.put("orderItemId", orderItem.getOrderItemId());
					orderItem.setOrderHotelTimeRateList(ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateList(itemParams));
				}
			}
			ordOrder.setOrderItemList(ordOrderItemList);
		}
		return ordOrderList;
	}

	@Override
	public Long countPercontrolGoodsHisOrder(Map<String, Object> paramsMap) {
		return ordOrderDao.countPercontrolGoodsHisOrder(paramsMap);
	}
	@Override
	public List<ResPrecontrolOrderVo> findPercontrolGoodsHisOrderList(Map<String, Object> paramsMap) {
		return ordOrderDao.findPercontrolGoodsHisOrderList(paramsMap);
	}
	
	
	
	public Map<String, Object> getDepartureNoticeParamsMap(Map<String, Object> paramsMap){
		logger.info("method start selectDepartureNoticeList getParamsMap:"+GsonUtils.toJson(paramsMap));
		if(null != paramsMap.get("ebkSupplierGroupIds")){
			Map<String, Object> ebkmap = new HashMap<>();
			ebkmap.put("ebkSupplierGroupIds", paramsMap.get("ebkSupplierGroupIds"));
			ebkmap.put("cancelFlag", "Y");
			ResultHandleT<List<EbkSupplierGroup>> resultSupplierGroup = ebkSupplierGroupClientRemote.getEbkSupplierGroupTreeLeavesByParams(ebkmap);
			if(resultSupplierGroup.isSuccess()){
				List<EbkSupplierGroup> list = resultSupplierGroup.getReturnContent();
				if(CollectionUtils.isNotEmpty(list)){
					List<Long> groupIdList = new ArrayList<Long>();
					for (EbkSupplierGroup ebkSupplierGroup : list) {
						groupIdList.add(ebkSupplierGroup.getGroupId());
					}
					String ebkSupplierGroupIdStr = StringUtil.getOrIn(groupIdList, "ooi.EBK_SUPPLIER_GROUP_ID");
					paramsMap.put("ebkSupplierGroupIdStr", ebkSupplierGroupIdStr);
				}
			}
		}		
		
		List<DepartureNoticeVo> listNotice = ordOrderDao.selectAllDepartureNoticeList(paramsMap);		
		Map<String, Object> certParam = new HashMap<String, Object>();
		if(CollectionUtils.isNotEmpty(listNotice)){
			List<Long> orderIdList = new ArrayList<Long>();
			for (DepartureNoticeVo noticeVo : listNotice) {
				orderIdList.add(noticeVo.getOrderId());
			}
			if(CollectionUtils.isNotEmpty(orderIdList)){
				String orderIdStr = StringUtil.getOrIn(orderIdList, "eci.orderId");
				certParam.put("orderIdStr", orderIdStr);
			}
			Set<Long> productIdSet = new HashSet<Long>();
			ResultHandleT<List<Long>> resultHandle = ebkOrderClientServiceReomte.getCertifItemOrderByEbkTask(certParam);
			orderIdList.clear();
			if(resultHandle.isSuccess()){
				List<Long> certOrderIdList = resultHandle.getReturnContent();
				Map<Long, Object> certMap = new HashMap<Long, Object>();
				if(CollectionUtils.isNotEmpty(certOrderIdList)){
					for (Long orderId : certOrderIdList) {
						certMap.put(orderId, orderId);
					}
				}
				for (DepartureNoticeVo noticeVo : listNotice){
					if(null!= certMap.get(noticeVo.getOrderId())){
						orderIdList.add(noticeVo.getOrderId());	
						productIdSet.add(noticeVo.getProductId());
					}				
				}	
			}	
			
			//反查产品表获取SupplierProductName		
			List<Long> productList = new ArrayList<Long>(productIdSet);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("source", "EBK");
			params.put("productIds", productList);
			if(null != paramsMap.get("prodCreateUser")){
				params.put("createUser", paramsMap.get("prodCreateUser"));
			}			
			ResultHandleT<List<ProdProduct>> resultProd = productHotelAdapterClientService.findProdProductList(params);
			List<ProdProduct> prodList = resultProd.getReturnContent();
			Map<Long, String> prodMap = new HashMap<Long, String>();
			productList.clear();
			for (ProdProduct prodProduct : prodList) {
				prodMap.put(prodProduct.getProductId(), prodProduct.getSuppProductName());
				productList.add(prodProduct.getProductId());
			}
			paramsMap.put("productIds", productList);
			String productIdStr= StringUtil.getOrIn(productList, "ooi.product_id");
			paramsMap.put("productIdStr", productIdStr);
			
			paramsMap.put("orderIdList", orderIdList);
			String orderIdListStr= StringUtil.getOrIn(orderIdList, "oo.order_id");
			paramsMap.put("orderIdListStr", orderIdListStr);
		}	
		logger.info("method end selectDepartureNoticeList getParamsMap:"+GsonUtils.toJson(paramsMap));
		return paramsMap;
	}

	@Override
	public int updateOrderMemo(OrdOrder order) {
		return ordOrderDao.updateByPrimaryKeySelective(order);
	}
	/**
     * 查询邮寄订单
     * @param param
     * @return
     */
    public List<OrdOrder> selectMailOrderInfoByParams(Map<String, Object> param){
    	return ordOrderDao.selectMailOrderInfoByParams(param);
    }
    
    @Override
    @ReadOnlyDataSource
    @ForceRead
    public List<OrdOrder> getOrderIdsForSendMail(Map<String, Object> param){
    	return ordOrderDao.getOrderIdsForSendMail(param);
    }
    
	@Override
	@ReadOnlyDataSource
	public List<Long> queryOrderForDelWorkflowByParams(Map<String, Object> params) {
		return ordOrderDao.queryOrderIdsByParams(params);
	}
}
