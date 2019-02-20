package com.lvmama.vst.order.contract.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.prod.curise.service.ProdCuriseProductClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.back.prod.curise.vo.CuriseProductParamsVO;
import com.lvmama.vst.back.prod.curise.vo.CuriseProductVO;
import com.lvmama.vst.back.prod.po.ProdEcontract;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.order.contract.service.IOrderTravelContractDataService;
import com.lvmama.vst.order.contract.vo.OutboundTourContractVO;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.dao.OrdOrderPackDao;
import com.lvmama.vst.order.dao.OrdPersonDao;
import com.lvmama.vst.order.dao.OrdTravelContractDAO;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;

/**
 * 
 * @author sunjian
 *
 */
@Service("orderCombCuriseTravelContractDataService")
public class OrderCombCuriseTravelContractDataServiceImpl implements IOrderTravelContractDataService {
	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	
	@Autowired
	private OrdPersonDao ordPersonDao;
	
	@Autowired
	private OrdOrderPackDao ordOrderPackDao;
	
	@Autowired
	private OrdTravelContractDAO ordTravelContractDAO;
	
	@Autowired
	private ProdCuriseProductClientService prodCuriseProductClientService;
	
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;
	
	@Autowired
	private IOrdMulPriceRateService ordMulPriceRateService;
	
	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;
	
	private static final int MAX_COUNT_OF_PDF_LINE = 50;

	@Override
	public ResultHandleT<OutboundTourContractVO> captureOutboundTourContract(OrdOrder order) {
		ResultHandleT<OutboundTourContractVO> resultHandleOutboundTourContractVO = new ResultHandleT<OutboundTourContractVO>();
		if (order != null) {
			//订单子项
			if (order.getOrderItemList() == null || order.getOrderItemList().isEmpty()) {
				List<OrdOrderItem> ordOrderItemList = ordOrderItemDao.selectByOrderId(order.getOrderId());
				if (ordOrderItemList == null || ordOrderItemList.isEmpty()) {
					resultHandleOutboundTourContractVO.setMsg("订单ID=" + order.getOrderId() + "不存在子订单");
					return resultHandleOutboundTourContractVO;
				} 
				
				order.setOrderItemList(ordOrderItemList);
			}
			
			//游玩人
			if (order.getOrdPersonList() == null || order.getOrdPersonList().isEmpty()) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("objectId", order.getOrderId());
				params.put("objectType", OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
				List<OrdPerson> ordPersonList = ordPersonDao.findOrdPersonList(params);
				if (ordPersonList == null || ordPersonList.isEmpty()) {
					resultHandleOutboundTourContractVO.setMsg("订单ID=" + order.getOrderId() + "不存在游玩人。");
					return resultHandleOutboundTourContractVO;
				}
				
				order.setOrdPersonList(ordPersonList);
			}
			
			//组合
			if (order.getOrderPackList() == null || order.getOrderPackList().isEmpty()) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("orderId", order.getOrderId());
				List<OrdOrderPack> ordOrderPackList = ordOrderPackDao.selectByParams(params);
				if (ordOrderPackList == null || ordOrderPackList.isEmpty() || ordOrderPackList.size() != 1) {
					resultHandleOutboundTourContractVO.setMsg("订单ID=" + order.getOrderId() + "组合信息错误。");
					return resultHandleOutboundTourContractVO;
				}
				
				order.setOrderPackList(ordOrderPackList);
			}
			
			//电子合同
			if (order.getOrdTravelContractList() == null || order.getOrdTravelContractList().isEmpty()) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("orderId", order.getOrderId());
				List<OrdTravelContract> ordTravelContractList = ordTravelContractDAO.selectByParam(params);
				
				order.setOrdTravelContractList(ordTravelContractList);
			}
			
			OrdOrderPack ordOrderPack = order.getOrderPackList().get(0);
			ResultHandleT<CuriseProductVO> resultHandleCuriseProductVO = getCombCuriseProducatData(ordOrderPack.getCategoryId(), ordOrderPack.getProductId());
			if (resultHandleCuriseProductVO.isFail()) {
				resultHandleOutboundTourContractVO.setMsg(resultHandleCuriseProductVO.getMsg());
				return resultHandleOutboundTourContractVO;
			}
			
			CuriseProductVO curiseProductVO = resultHandleCuriseProductVO.getReturnContent();
			OutboundTourContractVO outboundTourContractVO = buildOutboundTourContractVOData(order, curiseProductVO);
			
			if (outboundTourContractVO == null) {
				resultHandleOutboundTourContractVO.setMsg("合同数据抓取失败。");
			}
			
			resultHandleOutboundTourContractVO.setReturnContent(outboundTourContractVO);
		} else {
			resultHandleOutboundTourContractVO.setMsg("订单为空。");
		}
		
		return resultHandleOutboundTourContractVO;
	}
	
	public ResultHandleT<CuriseProductVO> getCombCuriseProducatData(Long combCategoryId, Long combProductId) {
		CuriseProductParamsVO curiseProductParamsVO = new CuriseProductParamsVO();
		
		List<String> propCodeList = new ArrayList<String>();
		propCodeList.add(BizEnum.BIZ_CATEGORY_PROP_CODE.the_fee_includes.name());
		propCodeList.add(BizEnum.BIZ_CATEGORY_PROP_CODE.cost_free.name());
		propCodeList.add(BizEnum.BIZ_CATEGORY_PROP_CODE.shopping_help.name());
		propCodeList.add(BizEnum.BIZ_CATEGORY_PROP_CODE.change_and_cancellation_instructions.name());
		propCodeList.add(BizEnum.BIZ_CATEGORY_PROP_CODE.attention.name());
		propCodeList.add(BizEnum.BIZ_CATEGORY_PROP_CODE.important.name());
		propCodeList.add(BizEnum.BIZ_CATEGORY_PROP_CODE.play_tips.name());
		propCodeList.add(BizEnum.BIZ_CATEGORY_PROP_CODE.travel_service_guarantee.name());
		propCodeList.add(BizEnum.BIZ_CATEGORY_PROP_CODE.recommended_items.name());
		
		
		curiseProductParamsVO.setCategoryId(combCategoryId);
		curiseProductParamsVO.setProductId(combProductId);
		curiseProductParamsVO.setPropCodeList(propCodeList);
		
		curiseProductParamsVO.setQueryEcontract(true);
		curiseProductParamsVO.setQueryGroupDate(false);
		curiseProductParamsVO.setQueryRoute(true);
		curiseProductParamsVO.setQueryShipLine(true);
		
		return prodCuriseProductClientService.findCombCuriseProduct(curiseProductParamsVO);
	}
	
	private OutboundTourContractVO buildOutboundTourContractVOData(OrdOrder order, CuriseProductVO curiseProductVO) {
		OutboundTourContractVO outboundTourContractVO = null;
		SuppSupplier suppSupplier = null;
		if (order != null && curiseProductVO != null) {
			outboundTourContractVO = new OutboundTourContractVO();
			OrdTravelContract ordTravelContract=order.getOrdTravelContract();
			String appendVersion = getAppendVersion(ordTravelContract);
			//合同编号
			String version = DateUtil.formatDate(order.getVisitTime(), "yyyyMMdd") + "-" + order.getOrderId() + "-" + appendVersion;
			outboundTourContractVO.setContractVersion(version);
			
			//订单编号
			outboundTourContractVO.setOrderId(order.getOrderId().toString());
			
			//甲方
			String travellers = null;
			int travellerCount = 0;
			for (OrdPerson ordPerson : order.getOrdPersonList()) {
				if (ordPerson != null && OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equalsIgnoreCase(ordPerson.getPersonType())) {
					if (travellers == null) {
						travellers = ordPerson.getFullName();
					} else {
						if (travellerCount % 5 == 0) {
							travellers = travellers + ",<br />" + ordPerson.getFullName();
						} else {
							travellers = travellers + "," + ordPerson.getFullName();
						}
						
					}
					
					travellerCount++;
				}
			}
			
			outboundTourContractVO.setTravellers(travellers);
			
			//产品名称
			outboundTourContractVO.setProductName(order.getOrdOrderPack().getProductName());
			
			ProdEcontract prodEcontract = curiseProductVO.getEcontract();
			
			if (CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.name().equalsIgnoreCase(prodEcontract.getGroupType())) {
				outboundTourContractVO.setDelegateGroup(true);
				
				for(OrdOrderItem ordOrderItem : order.getOrderItemList()) {
					if (ordOrderItem != null) {
						String categoryCode = (String) ordOrderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
						if (BizEnum.BIZ_CATEGORY_TYPE.category_cruise.name().equalsIgnoreCase(categoryCode)) {
							ResultHandleT<SuppSupplier> resultHandleSuppSupplier =suppSupplierClientService.findSuppSupplierById(ordOrderItem.getSupplierId());
							if (resultHandleSuppSupplier.isSuccess()) {
								suppSupplier = resultHandleSuppSupplier.getReturnContent();
								break;
							}
						}
					}
				}
				
				if (suppSupplier != null) {
					outboundTourContractVO.setDelegateGroupName(suppSupplier.getSupplierName());
				}
			} else {
				outboundTourContractVO.setDelegateGroup(false);
				outboundTourContractVO.setDelegateGroupName("");
			}
			
			outboundTourContractVO.setVistDate(DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd"));
			
			outboundTourContractVO.setDeparturePlace(curiseProductVO.getDeparturePlace());
			
			String destination = "";
			int i = 0;
			List<String> shipLineList=curiseProductVO.getShipLineList();
			for (int j = 0; j < shipLineList.size(); j++) {
				
				String place=shipLineList.get(j);
				
				if (place != null && place.trim() != null) {
					if (j!=0 && j % 5 == 0) {
						destination = destination + "<br />" ;
					} 
					
					destination +=  place;
				}
				
				if ( !StringUtils.isEmpty(destination)&& !StringUtils.isEmpty(place) && (j+1)<shipLineList.size()) {
					destination +=   "," ;
				}
				
			}
			/*for (String place : shipLineList) {
				if (place != null && place.trim() != null) {
					if (i!=0 && i % 5 == 0) {
						destination = destination + ",<br />" + place;
					} else {
						//destination = destination + "," + place;
						destination +=  place;
					}
					i++;
				}
			}*/
			outboundTourContractVO.setDestination(destination);
			
			Date beginDate = order.getVisitTime();
			Date endDate = null;
			Long nDays = curiseProductVO.getTravelDays();
			if (nDays == null) {
				endDate = beginDate;
			} else {
				endDate = DateUtil.getDateAfterDays(beginDate, nDays.intValue() - 1);
			}
			outboundTourContractVO.setOverDate(DateUtil.formatDate(endDate, "yyyy-MM-dd"));
			
			outboundTourContractVO.setReturnPlace(curiseProductVO.getDeparturePlace());
			
			if (outboundTourContractVO.getDelegateGroup()) {
				outboundTourContractVO.setLocalTravelAgencyName(suppSupplier.getSupplierName());
				outboundTourContractVO.setLocalTravelAgencyAddress(suppSupplier.getAddress());
			} else {
				outboundTourContractVO.setLocalTravelAgencyName("/");
				outboundTourContractVO.setLocalTravelAgencyAddress("/");
			}
			
			List<OrdOrderItem> insuranceOrderItemList = getInsuranceOrdOrderItem(order);
			if (insuranceOrderItemList != null && !insuranceOrderItemList.isEmpty()) {
				outboundTourContractVO.setHasInsurance(true);
				outboundTourContractVO.setAgreeInsurance("同意");
				OrdOrderItem insurance = insuranceOrderItemList.get(0);
				outboundTourContractVO.setInsuranceCompanyAndProductName(insurance.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.insurance_company.name())
						+ insurance.getProductName());
				
				long totalInsurancePrice = getTotalPrice(insuranceOrderItemList);
				outboundTourContractVO.setInsuranceAmount(PriceUtil.trans2YuanStr(totalInsurancePrice));
				
				long totalPrice = order.getOughtAmount() - totalInsurancePrice;
				outboundTourContractVO.setTraveAmount(PriceUtil.trans2YuanStr(totalPrice));
				
			} else {
				outboundTourContractVO.setAgreeInsurance("不同意");
				outboundTourContractVO.setInsuranceCompanyAndProductName("/");
				outboundTourContractVO.setInsuranceAmount("/");
				
				outboundTourContractVO.setTraveAmount(order.getOughtAmountYuan());
			}
			
			if (prodEcontract.getMinPerson() != null) {
				outboundTourContractVO.setMinPersonCountOfGroup(prodEcontract.getMinPerson().toString());
				outboundTourContractVO.setHasMinPersonCount(true);
			} else {
				outboundTourContractVO.setMinPersonCountOfGroup("");
				outboundTourContractVO.setHasMinPersonCount(false);
			}
			
			Map<String, Object> productPropMap = curiseProductVO.getProductPropMap();
			if (productPropMap != null && !productPropMap.isEmpty()) {
				String code = null;
				String value = null;
				String cnName = null;
				int titleNum = 1;
				StringBuilder stringBuilder = new StringBuilder();
				for (Entry<String, Object> entry : productPropMap.entrySet()) {
					if (entry != null) {
						code = entry.getKey();
						value = (String) entry.getValue();
						cnName = BizEnum.BIZ_CATEGORY_PROP_CODE.getCnName(code);
						
						if (value != null && !value.trim().isEmpty()) {
							stringBuilder.append(StringUtil.transition(String.valueOf(titleNum)) + "、" + cnName + "<br />");
							String brStr = "<br />";
							value = value.replace("\r\n", brStr);
							value = value.replace("\n", brStr);
							int brStrIndex = -1;
							int length = value.length();
							
							for (int index = 0; index < length;) {
								brStrIndex = value.indexOf(brStr, index);
								if (brStrIndex >= 0 && brStrIndex - index <= MAX_COUNT_OF_PDF_LINE) {
									stringBuilder.append(value.substring(index, brStrIndex + brStr.length()));
									index = brStrIndex + brStr.length();
								} else {
									if (index + MAX_COUNT_OF_PDF_LINE < length) {
										stringBuilder.append(value.substring(index, index + MAX_COUNT_OF_PDF_LINE));
										stringBuilder.append("<br />");
									} else {
										stringBuilder.append(value.substring(index, length));
									}
									
									index += MAX_COUNT_OF_PDF_LINE;
								}
							}
							
							titleNum++;
							stringBuilder.append("<br />");
						}
					}
				}
				
				outboundTourContractVO.setSupplementaryTerms(stringBuilder.toString());
			} else {
				outboundTourContractVO.setSupplementaryTerms("");
			}
			
			OrdPerson traveller = getFirstTraveller(order);
			outboundTourContractVO.setSignaturePersonName(traveller.getFullName());
			
			outboundTourContractVO.setFirstDelegatePersonName(traveller.getFullName());
			
			outboundTourContractVO.setContactTelePhoneNo(order.getContactPerson().getMobile());
			
			outboundTourContractVO.setFirstSignatrueDate(DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd"));
			
			outboundTourContractVO.setSecondSignatrueDate(DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd"));
			
			outboundTourContractVO.setStampImage(getStampImageNameByFilialeName(curiseProductVO.getProduct().getFiliale()));
		}
		//订单为邮轮组合
		if(BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(order.getCategoryId())){
			List<OrdOrderItem> itemList = order.getOrderItemList();
			List<OrderMonitorRst> rstList = null;
			if(null != itemList && itemList.size()>0){
				rstList = new ArrayList<OrderMonitorRst>();
				for (OrdOrderItem ordOrderItem : itemList) 
				{
					OrderMonitorRst orderMonitorRst = new OrderMonitorRst();
					Map<String,Object> contentMap = ordOrderItem.getContentMap();
					String categoryType =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
					orderMonitorRst.setChildOrderTypeName(BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCnName(categoryType));
					String branchName =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());					
					String productName = ordOrderItem.getProductName()+"-"+branchName+"";							
					orderMonitorRst.setProductName(productName);
					orderMonitorRst.setVisitTime(DateUtil.formatSimpleDate(ordOrderItem.getVisitTime()));
					orderMonitorRst.setOrderItemId(ordOrderItem.getOrderItemId());
					orderMonitorRst.setSuppGoodsName(ordOrderItem.getSuppGoodsName());
					orderMonitorRst.setChildOrderType(categoryType);
					orderMonitorRst.setBuyCount(ordOrderItem.getQuantity().intValue());
					buildBuyCountAndPrice(ordOrderItem, orderMonitorRst);
					
					rstList.add(orderMonitorRst);					
				}
				outboundTourContractVO.setOrderMonitorRstList(rstList);
			}			
		}
		
		return outboundTourContractVO;
	}

	public String getAppendVersion(OrdTravelContract ordTravelContract) {
		String appendVersion="A";
		if (ordTravelContract != null && ordTravelContract.getVersion() != null) {
			String oldVersion = ordTravelContract.getVersion();
			char oldAppendVersion = oldVersion.charAt(oldVersion.length() - 1);
			oldAppendVersion++;
			if (oldAppendVersion > 'A' && oldAppendVersion < 'Z') {
				appendVersion = oldAppendVersion + "";
			}
		}
		return appendVersion;
	}
	
	/**
	 * 获取公司专用章
	 * 
	 * @param filialeName
	 * @return
	 */
	private String getStampImageNameByFilialeName(String filialeName) {
		String StampImageName = null;
		if (CommEnumSet.FILIALE_NAME.SH_FILIALE.name().equalsIgnoreCase(filialeName)) {
			StampImageName = "SH_ECONTRACT.png";
		} else if (CommEnumSet.FILIALE_NAME.BJ_FILIALE.name().equalsIgnoreCase(filialeName)) {
			StampImageName = "BJ_ECONTRACT.png";
		} else if (CommEnumSet.FILIALE_NAME.GZ_FILIALE.name().equalsIgnoreCase(filialeName)) {
			StampImageName = "GZ_ECONTRACT.png";
		} else if (CommEnumSet.FILIALE_NAME.CD_FILIALE.name().equalsIgnoreCase(filialeName)) {
			StampImageName = "SC_ECONTRACT.png";
		}
		
		return StampImageName;
	}
	
	/**
	 * 
	 * @param order
	 * @return
	 */
	private List<OrdOrderItem> getInsuranceOrdOrderItem(OrdOrder order) {
		List<OrdOrderItem> ordOrderItemList = null;
		
		if (order != null) {
			ordOrderItemList = new ArrayList<OrdOrderItem>();
			for (OrdOrderItem orderItem : order.getOrderItemList()) {
				if (orderItem != null) {
					String categoryCode = (String) orderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
					if (BizEnum.BIZ_CATEGORY_TYPE.category_insurance.name().equalsIgnoreCase(categoryCode)) {
						ordOrderItemList.add(orderItem);
					}
				}
			}
		}
		
		return ordOrderItemList;
	}
	
	/**
	 * 
	 * @param ordOrderItemList
	 * @return
	 */
	private long getTotalPrice(List<OrdOrderItem> ordOrderItemList) {
		long totalPrice = 0;
		if (ordOrderItemList != null) {
			for (OrdOrderItem ordOrderItem : ordOrderItemList) {
				if (ordOrderItem != null) {
					totalPrice = totalPrice + ordOrderItem.getPrice() * ordOrderItem.getQuantity();
				}
			}
		}
		
		return totalPrice;
	}
	
	/**
	 * 
	 * @param order
	 * @return
	 */
	private OrdPerson getFirstTraveller(OrdOrder order) {
		OrdPerson traveller = null;
		for (OrdPerson ordPerson : order.getOrdPersonList()) {
			if (ordPerson != null 
					&& OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equalsIgnoreCase(ordPerson.getPersonType())
					&& OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name().equalsIgnoreCase(ordPerson.getPeopleType())) {
				traveller = ordPerson;
			}
		}
		
		return traveller;
	}
	
	/**
	 * 组装合同子订单数据
	 * @param orderItem
	 * @param orderMonitorRst
	 */
private void buildBuyCountAndPrice(OrdOrderItem orderItem, OrderMonitorRst orderMonitorRst) {		
		
	   orderMonitorRst.setBuyItemCount(orderItem.getQuantity()+"份");
		Map<String, Object> paramOrdItemPersonRelation = new HashMap<String, Object>();
		paramOrdItemPersonRelation.put("orderItemId", orderItem.getOrderItemId()); 
		List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(paramOrdItemPersonRelation);
		orderMonitorRst.setPersonCount(ordItemPersonRelationList.size());
	}
	
}
