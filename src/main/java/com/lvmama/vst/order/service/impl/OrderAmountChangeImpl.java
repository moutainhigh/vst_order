package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.tnt.po.OrderPrice;
import com.lvmama.vst.back.order.po.OrdAmountChange;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderAmountItem;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.order.dao.ComplexQueryDAO;
import com.lvmama.vst.order.dao.OrdAmountChangeDao;
import com.lvmama.vst.order.dao.OrdOrderAmountItemDao;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;
import com.lvmama.vst.order.service.IOrderAmountChangeService;
import com.lvmama.vst.order.service.IOrderResponsibleService;

@Service
public class OrderAmountChangeImpl implements IOrderAmountChangeService{
	
	@Autowired
	private OrdAmountChangeDao ordAmountChangeDao;
	@Autowired
	private OrdOrderDao orderDao;
	@Autowired
	private OrdOrderItemDao orderItemDao;
	
	@Autowired
	private ComplexQueryDAO complexQueryDAO;
	@Autowired
	private OrdOrderAmountItemDao orderAmountItemDao;
	
	@Autowired
	private IComMessageService comMessageService;
	

	@Autowired
	private IOrderResponsibleService orderResponsibleService;
	

	@Autowired
	private IOrdMulPriceRateService ordMulPriceRateService;

	
	@Override
	public int insert(OrdAmountChange ordAmountChange) {
		
		return ordAmountChangeDao.insert(ordAmountChange);
	}

	@Override
	public OrdAmountChange selectByPrimaryKey(Long ordAmountChangeId) {
		
		return ordAmountChangeDao.selectByPrimaryKey(ordAmountChangeId);
	}

	@Override
	public int updateByPrimaryKeySelective(OrdAmountChange ordAmountChange) {
		
		return ordAmountChangeDao.updateByPrimaryKeySelective(ordAmountChange);
	}

	@Override
	public int updateByPrimaryKey(OrdAmountChange ordAmountChange) {
		
		return ordAmountChangeDao.updateByPrimaryKey(ordAmountChange);
	}

	
	@Override
	public List<OrdAmountChange> findOrdAmountChangeList(
			HashMap<String, Object> params) {
		return ordAmountChangeDao.findOrdAmountChangeList(params);
	}

	@Override
	public Integer findOrdAmountChangeCounts(HashMap<String, Object> params) {
		return ordAmountChangeDao.findOrdAmountChangeCounts(params);
	}

	@Override
	public Integer isOrderApproving(Long orderId) {
		return ordAmountChangeDao.isOrderApproving(orderId);
	}

	@Override
	public Integer updateOrderAmount(OrdAmountChange ordAmountChange,OrdOrder ordOrder) {
		//更新订单
		int result = orderDao.updateByPrimaryKey(ordOrder);
		if(result == 1){
			//保存扣记录
			OrdOrderAmountItem ordOrderAmountItem = new OrdOrderAmountItem();
			ordOrderAmountItem.setItemAmount(-ordAmountChange.getAmount());
			ordOrderAmountItem.setOrderId(ordOrder.getOrderId());
			ordOrderAmountItem.setItemName(OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_ORDER.name());
			ordOrderAmountItem.setOrderAmountType(OrderEnum.ORDER_AMOUNT_TYPE.DEDUCTION_PRICE.name());
			int rs = orderAmountItemDao.insert(ordOrderAmountItem);
			//更新审批表
			if(rs == 1)
			return ordAmountChangeDao.updateByPrimaryKey(ordAmountChange);
		}
		return 0;
	}

	
	public List<OrdOrderAmountItem> findOrderAmountItemList(Map<String, Object> params){
		
		return orderAmountItemDao.findOrderAmountItemList(params);
	}

	@Override
	public void updateOrderAmount(OrdAmountChange oldOrdAmountChange,OrderPrice orderPrice) {
		// TODO Auto-generated method stub
		

		
		OrdOrder order =null;
		Long amountChange=0L;
		if ("ORDER_ITEM".equals(oldOrdAmountChange.getObjectType())) {
			
			OrdOrderItem orderItem=orderItemDao.selectByPrimaryKey(oldOrdAmountChange.getObjectId());
			order = orderDao.selectByPrimaryKey(orderItem.getOrderId());
			orderPrice.setOrderId(order.getOrderId());
			Long newPrice=0L;
			//新的子单总金额
			Long newItemTotalAmount = 0L;
			//得到之前的销售总价格
			orderPrice.setOldPrice(order.getOughtAmount());
			Map<String, Object> map=oldOrdAmountChange.getContentMap();
			
			if (map!=null && map.size()>0 && !map.containsKey("price")) {//修改的是多价格类型情况
				
				String[] priceTypeArray = new String[] {
						ORDER_PRICE_RATE_TYPE.PRICE_ADULT_12.getCode(),
						ORDER_PRICE_RATE_TYPE.PRICE_ADULT_34.getCode(),
						ORDER_PRICE_RATE_TYPE.PRICE_CHILD_34.getCode(),
						ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode(),
						ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode() ,
						ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.getCode() };
				
				Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
				paramsMulPriceRate.put("orderItemId", orderItem.getOrderItemId()); 
				paramsMulPriceRate.put("priceTypeArray",priceTypeArray); 
				List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
				
				
				Long newOrdItemTotalAmount=0L;
				for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {

					String priceType = ordMulPriceRate.getPriceType();
					if (map.containsKey(priceType)) {
						
						Long priceTypeAmountChange=NumberUtils.toLong(map.get(priceType) + "");
						if (OrderEnum.ORDER_AMOUNT_FORMULAS.SUBTRACT.name().equals(oldOrdAmountChange.getFormulas())) {
							priceTypeAmountChange=-priceTypeAmountChange;
						}
						Long newPriceTypeAmount=ordMulPriceRate.getPrice()+priceTypeAmountChange;
						
						newOrdItemTotalAmount +=newPriceTypeAmount * ordMulPriceRate.getQuantity();
						
						
						//更新对应价格类型的销售价格
						ordMulPriceRate.setPrice(newPriceTypeAmount);
						ordMulPriceRateService.updateByPrimaryKeySelective(ordMulPriceRate);
						
					} else {
						newOrdItemTotalAmount += ordMulPriceRate.getPrice()
								* ordMulPriceRate.getQuantity();
					}
				}
				
				newPrice=newOrdItemTotalAmount/orderItem.getQuantity();
				newItemTotalAmount = newOrdItemTotalAmount;
				
			}else{//修改的是销售价情况
				
				Long  priceChange=NumberUtils.toLong(map.get("price")+ "");//销售单价变化值
				
//				priceChange=oldOrdAmountChange.getAmount();
				
				if (OrderEnum.ORDER_AMOUNT_FORMULAS.SUBTRACT.name().equals(oldOrdAmountChange.getFormulas())) {
					priceChange=-priceChange;
				}
				newPrice=orderItem.getPrice()+priceChange;
				newItemTotalAmount = newPrice * orderItem.getQuantity();
			}
			orderItem.setTotalAmount(newItemTotalAmount);
			orderItem.setPrice(newPrice);

			amountChange=oldOrdAmountChange.getAmount();//总价变化值
			Long newOughtAmount=0L;//最新总价
			if (OrderEnum.ORDER_AMOUNT_FORMULAS.SUBTRACT.name().equals(oldOrdAmountChange.getFormulas())) {
				amountChange=-amountChange;
			}
			newOughtAmount=order.getOughtAmount()+amountChange;
			if (newOughtAmount<0) {
				newOughtAmount=0L;
			}
			order.setOughtAmount(newOughtAmount);
			//得到新的价格
			orderPrice.setNewPrice(newOughtAmount);
			orderItemDao.updateByPrimaryKey(orderItem);
			orderDao.updateByPrimaryKey(order);
		}else{
			
			amountChange= oldOrdAmountChange.getAmount();//总价变化值
			
			order = orderDao.selectByPrimaryKey(oldOrdAmountChange.getObjectId());
			orderPrice.setOrderId(order.getOrderId());
			orderPrice.setOldPrice(order.getOughtAmount());
			if (OrderEnum.ORDER_AMOUNT_FORMULAS.SUBTRACT.name().equals(oldOrdAmountChange.getFormulas())) {
				amountChange=-amountChange;
			}
			Long newOughtAmount=order.getOughtAmount()+amountChange;
			if (newOughtAmount<0) {
				newOughtAmount=0L;
			}
			order.setOughtAmount(newOughtAmount);
			orderPrice.setNewPrice(newOughtAmount);
			orderDao.updateByPrimaryKey(order);
			
			
		}
		
		
		//保存扣记录
		OrdOrderAmountItem ordOrderAmountItem = new OrdOrderAmountItem();
		ordOrderAmountItem.setItemAmount(amountChange);
		ordOrderAmountItem.setOrderId(order.getOrderId());
		ordOrderAmountItem.setItemName(OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_ORDER.name());
		ordOrderAmountItem.setOrderAmountType(OrderEnum.ORDER_AMOUNT_TYPE.DEDUCTION_PRICE.name());
		orderAmountItemDao.insert(ordOrderAmountItem);
		
		
		//更新审批表
		ordAmountChangeDao.updateByPrimaryKey(oldOrdAmountChange);
		
		
	
		
	}

	@Override
	public Integer saveReservationForOrderAmountChang(OrdAmountChange oldOrdAmountChange, String assignor,String messge) throws Exception {
		// TODO Auto-generated method stub
		
		ComMessage comMessage=new ComMessage();
		
		String auditSubType="";
		OrdOrder order =null;
		Long amountChange=0L;
		int n=0;
		if ("ORDER_ITEM".equals(oldOrdAmountChange.getObjectType())) {
			
			auditSubType=OrderEnum.AUDIT_SUB_TYPE.CHILD_ORDER_AMOUNT_CHANGE.getCode();
			
			OrdOrderItem orderItem=orderItemDao.selectByPrimaryKey(oldOrdAmountChange.getObjectId());
			
			Long priceChange=oldOrdAmountChange.getAmount();
			amountChange=priceChange;
//			if (OrderEnum.ORDER_AMOUNT_FORMULAS.SUBTRACT.name().equals(oldOrdAmountChange.getFormulas())) {
//				amountChange=-amountChange;
//				priceChange=-priceChange;
//			}
			
			String objectType="ORDER";
			Long objectId=orderItem.getOrderId();
			PermUser permUserPrincipal= orderResponsibleService.getOrderPrincipal(objectType, objectId);
			
			comMessage.setReceiver(permUserPrincipal.getUserName());
			
			StringBuffer singlePriceMsg = new StringBuffer();
			Map<String,Object> singlePriceMap = oldOrdAmountChange.getContentMap();
			if(singlePriceMap != null && singlePriceMap.size() > 0) {
				Set<String> priceTypeSet = singlePriceMap.keySet();
				for(String priceType : priceTypeSet) {
					if(StringUtils.isBlank(priceType) || singlePriceMap.get(priceType) == null) {
						continue;
					}
					
					if(singlePriceMsg.length() > 0) {
						singlePriceMsg.append("、");
					}
					singlePriceMsg.append("price".equals(priceType) ? "" : OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(priceType));
					singlePriceMsg.append(OrderEnum.ORDER_AMOUNT_FORMULAS.getCnName(oldOrdAmountChange.getFormulas()));
					singlePriceMsg.append(PriceUtil.trans2YuanStr(NumberUtils.toLong(String.valueOf(singlePriceMap.get(priceType)))));
					singlePriceMsg.append("元");
				}
			} else {
				singlePriceMsg.append(OrderEnum.ORDER_AMOUNT_FORMULAS.getCnName(oldOrdAmountChange.getFormulas()));
				singlePriceMsg.append(priceChange.doubleValue() / (orderItem.getQuantity() * 100));
				singlePriceMsg.append("元");
			}
			
			String content = "子订单修改单价价格"
					+ messge
					+ "，单价金额修改："
					+ singlePriceMsg.toString()
					+ ",订单应付金额修改："
					+ OrderEnum.ORDER_AMOUNT_FORMULAS
							.getCnName(oldOrdAmountChange.getFormulas())
					+ amountChange.doubleValue() / 100 + "元";
			comMessage.setMessageContent(content);
			
			try{
				n=comMessageService.saveReservationChildOrder(comMessage, null,OrderEnum.AUDIT_SUB_TYPE.APPROVAL.name(),
						orderItem.getOrderId(),orderItem.getOrderItemId(), assignor,content);
			}catch (Exception e) {
				// TODO: handle exception
				if (Constants.NO_PERSON.equals(e.getMessage())) {
					n=0;
				}else{
					throw e;
				}
			}
			
			
		}else{
			
			auditSubType=OrderEnum.AUDIT_SUB_TYPE.ORDER_AMOUNT_CHANGE.getCode();
			
			amountChange= oldOrdAmountChange.getAmount();
			
			order = orderDao.selectByPrimaryKey(oldOrdAmountChange.getObjectId());
			
//			if (OrderEnum.ORDER_AMOUNT_FORMULAS.SUBTRACT.name().equals(oldOrdAmountChange.getFormulas())) {
//				amountChange=-amountChange;
//			}
			
			String objectType="ORDER";
			Long objectId=order.getOrderId();
			PermUser permUserPrincipal= orderResponsibleService.getOrderPrincipal(objectType, objectId);
			
			comMessage.setReceiver(permUserPrincipal.getUserName());
			
			String content="主订单修改价格"+messge+"，订单应付金额修改："+OrderEnum.ORDER_AMOUNT_FORMULAS.SUBTRACT
					.getCnName(oldOrdAmountChange.getFormulas())+amountChange.doubleValue()/100+"元";
			comMessage.setMessageContent(content);
			
			try{
				n=comMessageService.saveReservation(comMessage, null,auditSubType,
					order.getOrderId(), assignor,content);
			}catch (Exception e) {
				// TODO: handle exception
				if (Constants.NO_PERSON.equals(e.getMessage())) {
					n=0;
				}else{
					throw e;
				}
			}
		}
		return n;
		
	}

	@Override
	public Integer queryApprovingRecords(Long orderId) {
		return ordAmountChangeDao.queryApprovingRecords(orderId);
	}

	@Override
	public String getOrderAmountItemByType(OrdOrder order, String amountType) {
		Long totalAmount = getOrderAmountItemValue(order, amountType);
		return PriceUtil.trans2YuanStr(totalAmount);
	}

	/**
	 * 获取所有主单上已经验证通过的改价信息
	 *
	 * @param orderId
	 */
	@Override
	public List<OrdAmountChange> queryOrderPassedAmountChangeList(Long orderId) {
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("orderId", orderId);
		param.put("approveStatus", OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
		param.put("objectType", OrderEnum.ORDER_AMOUNT_CHANG_OBJECT_TYPE.ORDER.name());
		return ordAmountChangeDao.findOrdAmountChangeList(param);
	}

	/**
	 * 批量获取所有主单上已经验证通过的改价信息
	 *
	 * @param orderIdList
	 */
	@Override
	public List<OrdAmountChange> queryOrderPassedAmountChangeList(List<Long> orderIdList) {
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("orderIdList", orderIdList);
		param.put("approveStatus", OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
		param.put("objectType", OrderEnum.ORDER_AMOUNT_CHANG_OBJECT_TYPE.ORDER.name());
		return ordAmountChangeDao.findOrdAmountChangeList(param);
	}

	private Long getOrderAmountItemValue(OrdOrder order, String orderAmountType) {
		Long totalAmount = 0L;
		if(order == null || StringUtils.isBlank(orderAmountType)){
			return totalAmount;
		}
		List<OrdOrderAmountItem> orderAmountItemList = order.getOrderAmountItemList();
		if(CollectionUtils.isEmpty(orderAmountItemList)){
			return totalAmount;
		}
		for (OrdOrderAmountItem item : orderAmountItemList) {
			if (orderAmountType.equals(item.getOrderAmountType())) {
				totalAmount += item.getItemAmount();
			}
		}
		return totalAmount;
	}
}
