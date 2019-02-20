package com.lvmama.vst.order.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.dist.service.DistGoodsClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsBlackListService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.supp.po.SuppGoodsLimit;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.order.dao.OrdBlackListDao;
import com.lvmama.vst.order.service.IComplexQueryService;

@Component
public class BlackListBussiness {

	@Autowired
	private DistGoodsClientService distGoodsClientService;// 
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private DistGoodsTimePriceClientService distGoodsTimePriceClientService;//
	
	@Autowired
	private SuppGoodsBlackListService blackListService;
	
	@Autowired
	protected IComplexQueryService complexQueryService;
	
	@Autowired
	private OrdBlackListDao ordBlackListDao;
	
	public String isBlackList(OrdOrder order){
		if(order.getOrderItemList()!=null){
			for (OrdOrderItem item : order.getOrderItemList()) {
				//期票判断
				if (item.hasTicketAperiodic()){
						continue;
				}
				if (order.getContactPerson() != null) {
					OrdPerson p = order.getContactPerson();
					Map<String, Object> params = new HashMap<String, Object>();
//					if (p.getMobile() != null && p.getMobile().length() > 0) {
//						params.put("blacklistNum", p.getMobile());
//						params.put("blacklistType", Constants.BLACK_LIST_PHONE);
//						params.put("goodId", item.getSuppGoodsId());
//						Long count1 = ordBlackListDao.queryCount(params);
//						if (count1 != null && count1 > 0) {
//							return "您的手机号被驴妈妈限制，不允许购买"+item.getProductName()+"-"+item.getSuppGoodsName(); 
//						}
//					}
//					params.clear();
					if(CollectionUtils.isNotEmpty(order.getOrdPersonList())){
						for (OrdPerson person : order.getOrdPersonList()) {
							if (person.getMobile() != null && person.getMobile().length() > 0) {
								params.put("blacklistNum", person.getMobile());
								params.put("blacklistType", Constants.BLACK_LIST_PHONE);
								params.put("goodId", item.getSuppGoodsId());
								Long count1 = ordBlackListDao.queryCount(params);
								if (count1 != null && count1 > 0) {
									return "您的手机号被驴妈妈限制，不允许购买"+item.getProductName()+"-"+item.getSuppGoodsName(); 
								}
							}
							params.clear();
							if(StringUtils.isNotEmpty(person.getIdNo())){
								params.put("blacklistNum", person.getIdNo().toUpperCase());
								params.put("blacklistType", Constants.BLACK_LIST_IDCARD);
								params.put("goodId", item.getSuppGoodsId());
								Long count2 = ordBlackListDao.queryCount(params);
								if (count2 != null && count2 > 0) {
		//							ProdProduct prodProduct = prodProductClientService.findProdProductById(goods.getProductId()).getReturnContent();
									return "您的证件号被驴妈妈限制，不允许购买"+item.getProductName()+"-"+item.getSuppGoodsName(); 
								}
							}
						}
					}
					params.clear();
					
					if (p.getMobile() != null && p.getMobile().length() > 0) {
						SuppGoodsLimit limit = blackListService.findVisitTimeLimitList(item.getSuppGoodsId());
						if (limit != null) {
							Date visitTime = item.getVisitTime();
							if(visitTime.before(limit.getStartTime())||visitTime.after(limit.getEndTime())){
								continue;
							}
							ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
							condition.getOrderContentParam().setSuppGoodstId(item.getSuppGoodsId());
							condition.getOrderTimeRangeParam().setVisitTimeBegin(visitTime);
							condition.getOrderTimeRangeParam().setVisitTimeEnd(visitTime);
							condition.getOrderContentParam().setMobile(p.getMobile());
							condition.getOrderExcludedParam().setOrderStatus(OrderEnum.ORDER_STATUS.CANCEL.name());
							condition.getOrderContentParam().setPersoType(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
							long count = complexQueryService.queryOrderCountByCondition(condition);
							/*for (int i = 0; i < orderList.size(); i++) {
								for (int j = i + 1; j < orderList.size();) {
									if (orderList.get(j).getOrderId()
											.equals(orderList.get(i).getOrderId())) {
										orderList.remove(j);
									} else {
										j++;
									}
								}
							}*/
							if (count >= limit.getLimitNum()) {
								return item.getProductName()+"-"+item.getSuppGoodsName()+"，相同游玩日期，1个手机号只能预订"+limit.getLimitNum()+"笔";
							}
						}
					}
				}
			}
		}
		return null;
	}
}
