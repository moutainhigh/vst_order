/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.cruise;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.prod.curise.service.ProdCuriseProductClientService;
import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.curise.vo.CuriseProductParamsVO;
import com.lvmama.vst.back.prod.curise.vo.CuriseProductVO;
import com.lvmama.vst.back.prod.po.PropValue;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfo.Product;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderPackInitBussiness;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;

/**
 * @author lancey
 *
 */
@Component("cruiseOrderPackInitBussiness")
public class CruiseOrderPackInitBussiness extends AbstractBookService implements OrderPackInitBussiness{
	
	private static final Logger LOG = LoggerFactory.getLogger(CruiseOrderPackInitBussiness.class);
	
	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;
	@Autowired
	private ProdCuriseProductClientService prodCuriseProductClientService;

	@Override
	public boolean initOrderPack(OrdOrderPackDTO pack, Product itemProduct) {
		String errorMsg = null;
		BuyInfo buyInfo = pack.getOrder().getBuyInfo();
		CuriseProductParamsVO curiseProductParamsVO = new CuriseProductParamsVO();
		
		errorMsg = fillCuriseProductParamsVO(pack,itemProduct, curiseProductParamsVO);
		if (errorMsg != null) {
			throwIllegalException(errorMsg);
		}
		
		OrdAdditionStatus ordAdditionStatus = OrderUtils.makeOrdAdditionStatus(OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.name(), 
				OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.NO_UPLOAD.name());
		pack.getOrder().addOrdAdditionStatus(ordAdditionStatus);
		
		ResultHandleT<CuriseProductVO> resultHandleCuriseProductVO = prodCuriseProductClientService.findCombCuriseProduct(curiseProductParamsVO);
		if (resultHandleCuriseProductVO.isSuccess()) {
			CuriseProductVO curiseProductVO = resultHandleCuriseProductVO.getReturnContent();
			if (curiseProductVO != null) {
				if (curiseProductVO.getProduct() != null) {
					pack.setProductName(curiseProductVO.getProduct().getProductName());
					pack.putContent(OrderEnum.ORDER_PACK_TYPE.departure.name(), curiseProductVO.getDeparturePlace());
				}
				
				if (curiseProductVO.getProductPropMap() != null) {
					Map<String, Object> productPropeMap = curiseProductVO.getProductPropMap();
					
					String airlineStr = null;
					List<PropValue> airlinePropValueList = (List<PropValue>) productPropeMap.get(OrderEnum.ORDER_CURISE_PROP_CODE.an_airline.name());
					if (airlinePropValueList != null && !airlinePropValueList.isEmpty()) {
						airlineStr = airlinePropValueList.get(0).getName();
					}
					
					String charterStr = (String) productPropeMap.get(OrderEnum.ORDER_CURISE_PROP_CODE.are_charter.name());
					String earnestStr = (String) productPropeMap.get(OrderEnum.ORDER_CURISE_PROP_CODE.earnest.name());
					
					//包船逻辑
					if ("Y".equalsIgnoreCase(charterStr)) {
						pack.putContent(OrderEnum.ORDER_PACK_TYPE.chartered_boat.name(), "Y");
						
						Long curiseProductId = curiseProductVO.getCuriseProductId();
						Long travellerCapacity = curiseProductVO.getTravellerCapacity();
						
						if (curiseProductId != null && travellerCapacity != null) {
							Long personCount = ordItemPersonRelationService.getPersonCountByProductId(curiseProductId, curiseProductParamsVO.getGroupDate());
							if (personCount != null) {
								if (travellerCapacity < personCount + buyInfo.getTravellers().size()) {
									long leftCapacity = travellerCapacity - personCount;
									LOG.info("超出邮轮最大载客量，目前此邮轮剩余载客量为" + leftCapacity + "人。");
									
									if (leftCapacity < 0) {
										leftCapacity = 0;
									}
									
									errorMsg = "超出邮轮最大载客量，目前此邮轮剩余载客量为" + leftCapacity + "人。";
								}
							} else {
								errorMsg = "检测组合产品ID" + buyInfo.getProductId() + "载客量信息失败。";
							}
						} else {
							errorMsg = "获取组合产品ID" + buyInfo.getProductId() + "载客量信息失败。";
						}
					} else {
						pack.putContent(OrderEnum.ORDER_PACK_TYPE.chartered_boat.name(), "N");
					}
					
					//定金
					if (earnestStr != null && StringUtil.isNumber(earnestStr)) {
						List<Person> travellers = buyInfo.getTravellers();
						if (travellers != null && !travellers.isEmpty()) {
							//原本定金单位为元，现在定金单位又变成分了。
							pack.getOrder().setDepositsAmount(Long.valueOf(earnestStr) * travellers.size());
						}
						
					}
					
					//航线
					if (airlineStr != null) {
						pack.putContent(OrderEnum.ORDER_PACK_TYPE.route.name(), airlineStr);
					}
				}
				
				Date beginDate = curiseProductParamsVO.getGroupDate();
				Date endDate = null;
				pack.putContent(OrderEnum.ORDER_PACK_TYPE.start_sailing_date.name(), DateUtil.formatDate(beginDate, "yyyy-MM-dd"));
				Long nDays = curiseProductVO.getTravelDays();
				if (nDays == null) {
					endDate = beginDate;
				} else {
					endDate = DateUtil.getDateAfterDays(beginDate, (nDays.intValue() - 1));
				}
				pack.putContent(OrderEnum.ORDER_PACK_TYPE.end_sailing_date.name(), DateUtil.formatDate(endDate, "yyyy-MM-dd"));
				
				//设置组合邮轮上船地点、下船地点
				setCombCuriseOnOffPlace(curiseProductVO.getShipLineList(), pack);
				
				//设置电子合同
				setTravelEContract(curiseProductVO, pack.getOrder());
				
				//设置所属公司
				pack.getOrder().setFilialeName(curiseProductVO.getProduct().getFiliale());
			}
			
		} else {
			errorMsg = resultHandleCuriseProductVO.getMsg();
		}
		if(StringUtils.isNotEmpty(errorMsg)){
			throwIllegalException(errorMsg);
		}
		return true;
	}
	
	/**
	 * 设置电子合同
	 * 
	 * @param curiseProductVO
	 * @param ordOrder
	 */
	private void setTravelEContract(CuriseProductVO curiseProductVO, OrdOrderDTO ordOrder) {
		if (curiseProductVO != null && ordOrder != null) {
			OrdTravelContract ordTravelContract = OrderUtils.makeOrdTravelContract(curiseProductVO.getEcontract(),ordOrder.getDistributorId());
			if (ordTravelContract != null) {
				List<OrdTravelContract> ordTravelContractList = new ArrayList<OrdTravelContract>();
				ordTravelContractList.add(ordTravelContract);
				
				ordOrder.setOrdTravelContractList(ordTravelContractList);
			}
		}
		
	}
	
	/**
	 * 设置组合邮轮上船地点、下船地点
	 * 
	 * @param shipLineList
	 * @param curiseProductVO
	 */
	private void setCombCuriseOnOffPlace(List<String> shipLineList, OrdOrderPack orderPack) {
		if (shipLineList != null) {
			String onShipPlace = null;
			String offShipPlace = null;
			for (String place : shipLineList) {
				if (place != null && !place.trim().equals("")) {
					if (onShipPlace == null) {
						onShipPlace = place;
						continue;
					} else if (offShipPlace == null) {
						offShipPlace = place;
						break;
					} else {
						break;
					}
				}
			}
			
			if (onShipPlace != null && offShipPlace == null) {
				offShipPlace = onShipPlace;
			}
			
			if (onShipPlace != null) {
				orderPack.putContent(OrderEnum.ORDER_PACK_TYPE.up_place.name(), onShipPlace);
			}
			
			if (offShipPlace != null) {
				orderPack.putContent(OrderEnum.ORDER_PACK_TYPE.down_place.name(), offShipPlace);
			}
		}
	}
	
	/**
	 * 返回最早游玩时间
	 * 
	 * @param product
	 * @return
	 */
	private Date getEarlyVisitDate(BuyInfo.Product product) {
		Date retVisitDate = null;
		Date itemVisitDate = null;
		if (product != null && product.getItemList() != null) {
			for (Item item : product.getItemList()) {
				if (item != null) {
					itemVisitDate = item.getVisitTimeDate();
					if (itemVisitDate != null) {
						if (retVisitDate == null) {
							retVisitDate = itemVisitDate;
						} else {
							if (retVisitDate.after(itemVisitDate)) {
								retVisitDate = itemVisitDate;
							}
						}
					}
				}
			}
		}
		
		return retVisitDate;
	}
	

	/**
	 * 组合邮轮查询参数
	 * 
	 * @param buyInfo
	 * @param curiseProductParamsVO
	 * @return
	 */
	private String fillCuriseProductParamsVO(OrdOrderPackDTO pack,
			BuyInfo.Product buyInfo, CuriseProductParamsVO curiseProductParamsVO) {
		String errorMsg = null;
		Date groupDate = getEarlyVisitDate(buyInfo);
		if (groupDate == null) {
			errorMsg = "请填写出游日期";
			return errorMsg;
		}
		
		List<String> propCodeList = new ArrayList<String>();
		propCodeList.add(OrderEnum.ORDER_CURISE_PROP_CODE.an_airline.name());
		propCodeList.add(OrderEnum.ORDER_CURISE_PROP_CODE.are_charter.name());
		propCodeList.add(OrderEnum.ORDER_CURISE_PROP_CODE.earnest.name());
		
		curiseProductParamsVO.setCategoryId(pack.getCategoryId());
		curiseProductParamsVO.setProductId(buyInfo.getProductId());
		curiseProductParamsVO.setPropCodeList(propCodeList);
		curiseProductParamsVO.setGroupDate(groupDate);
		curiseProductParamsVO.setDistributionId(pack.getOrder().getBuyInfo().getDistributionId());
		
		curiseProductParamsVO.setQueryEcontract(true);
		curiseProductParamsVO.setQueryGroupDate(true);
		curiseProductParamsVO.setQueryRoute(true);
		curiseProductParamsVO.setQueryShipLine(true);
		
		return errorMsg;
	}
}
