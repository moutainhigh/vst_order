package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.order.dao.OrdMulPriceRateDAO;
import com.lvmama.vst.order.dao.OrdOrderAmountItemDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.dao.OrdSettlementPriceRecordDao;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;
import com.lvmama.vst.order.service.IOrdOrderItemExtendService;
import com.lvmama.vst.order.service.IOrdSettlementPriceRecordService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrdSettlementPriceRecordImpl implements IOrdSettlementPriceRecordService{
	
	private static final Log LOG = LogFactory.getLog(OrdSettlementPriceRecordImpl.class);
	
	@Autowired
	private OrdSettlementPriceRecordDao ordSettlementPriceRecordDao;

	@Autowired
	private OrdOrderAmountItemDao ordOrderAmountItemDao;
	
	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	
	@Autowired
	private IOrdMulPriceRateService ordMulPriceRateService;
	

	@Autowired
	private IComMessageService comMessageService;
	
	@Autowired
	private OrdMulPriceRateDAO ordMulPriceRateDao;

	@Autowired
	private IOrdOrderItemExtendService ordOrderItemExtendService;
	
	@Override
	public int insert(OrdSettlementPriceRecord ordSettlementPriceRecord) {
		// TODO Auto-generated method stub
		return ordSettlementPriceRecordDao.insert(ordSettlementPriceRecord);
	}

	@Override
	public OrdSettlementPriceRecord selectByPrimaryKey(
			Long id) {
		// TODO Auto-generated method stub
		return ordSettlementPriceRecordDao.selectByPrimaryKey(id);
	}

	@Override
	public int updateByPrimaryKeySelective(
			OrdSettlementPriceRecord ordSettlementPriceRecord) {
		// TODO Auto-generated method stub
		return ordSettlementPriceRecordDao.updateByPrimaryKeySelective(ordSettlementPriceRecord);
	}

	@Override
	public int updateByPrimaryKey(
			OrdSettlementPriceRecord ordSettlementPriceRecord) {
		// TODO Auto-generated method stub
		return ordSettlementPriceRecordDao.updateByPrimaryKey(ordSettlementPriceRecord);
	}

	@Override
	public List<OrdSettlementPriceRecord> findOrdSettlementPriceRecordList(
			HashMap<String, Object> params) {
		// TODO Auto-generated method stub
		return ordSettlementPriceRecordDao.findOrdSettlementPriceRecordList(params);
	}

	@Override
	public Integer findOrdSettlementPriceRecordCounts(
			HashMap<String, Object> params) {
		// TODO Auto-generated method stub
		return ordSettlementPriceRecordDao.findOrdSettlementPriceRecordCounts(params);
	}
	
	public int saveBuyoutMultiPriceAfterApprove(Long newBuyoutTotalPrice,OrdSettlementPriceRecord newOrdSettlementPriceRecord, Long orderId,String priceModel){
		
		this.insert(newOrdSettlementPriceRecord);
		
		int rs=0 ;
		if(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name().equalsIgnoreCase(newOrdSettlementPriceRecord.getStatus())){
			
			OrdOrderAmountItem ordOrderAmountItem = new OrdOrderAmountItem();
			if("BUDGET_UNIT_PRICE".equals(priceModel)){
				Long newUpdateActualSettlementPrice=newOrdSettlementPriceRecord.getNewBudgetUnitSettlementPrice();
				newUpdateActualSettlementPrice = newUpdateActualSettlementPrice==null?0L:newUpdateActualSettlementPrice;
				// 保存扣记录
				Long a = newOrdSettlementPriceRecord.getOldBudgetUnitSettlementPrice();
				a = a==null?0L:a;
				ordOrderAmountItem.setItemAmount(newUpdateActualSettlementPrice-a);
				ordOrderAmountItem.setOrderAmountType(OrderEnum.ORDER_AMOUNT_TYPE.ACTUAL_SETTLEPRICE.name());
			}else{
				Long oldTotalPirce = newOrdSettlementPriceRecord.getOldBudgetTotalSettlementPrice();
				Long newTotalPrice = newOrdSettlementPriceRecord.getNewBudgetTotalSettlementPrice();
				ordOrderAmountItem.setItemAmount(newBuyoutTotalPrice-oldTotalPirce);
				ordOrderAmountItem.setOrderAmountType(OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_SETTLEPRICE.name());
			}
			ordOrderAmountItem.setOrderId(orderId);
			ordOrderAmountItem.setItemName(OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_ORDER.name());
			rs =ordOrderAmountItemDao.insert(ordOrderAmountItem);
		}
		
		return rs;
	}
	
	public int saveAfterApprove(OrdSettlementPriceRecord oldOrdSettlementPriceRecord, OrdSettlementPriceRecord newOrdSettlementPriceRecord, Long orderId){
		
		
		this.insert(newOrdSettlementPriceRecord);
		
		Long orderItemId=newOrdSettlementPriceRecord.getOrderItemId();
		OrdOrderItem ordOrderItem=ordOrderItemDao.selectByPrimaryKey(orderItemId);
		OrdOrderItemExtend extend = ordOrderItemExtendService.selectByPrimaryKey(ordOrderItem.getOrderItemId());
		if (extend != null) {
			ordOrderItem.setOrdOrderItemExtend(extend);
		}
		Long buyNum = ordOrderItem.getBuyoutQuantity();
		buyNum = buyNum==null?0L:buyNum;
		if("Y".equals(ordOrderItem.getBuyoutFlag()) && ordOrderItem.getQuantity()!=ordOrderItem.getBuyoutQuantity()){
			Long buyoutTotalPrice = ordOrderItem.getBuyoutTotalPrice();
			buyoutTotalPrice = buyoutTotalPrice==null?0L:buyoutTotalPrice;
			ordOrderItem.setTotalSettlementPrice(ordOrderItem.getTotalSettlementPrice() - buyoutTotalPrice);
			ordOrderItem.setActualSettlementPrice(ordOrderItem.getTotalSettlementPrice()/(ordOrderItem.getQuantity()-buyNum));
			
		}
		int rs=0 ;
		
		if(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name().equalsIgnoreCase(newOrdSettlementPriceRecord.getStatus())){
			
			Long newUpdateActualSettlementPrice=newOrdSettlementPriceRecord.getNewActualSettlementPrice();
			String updatePriceType=newOrdSettlementPriceRecord.getPriceType();
			
			updateNewSettlementPrice(ordOrderItem,newUpdateActualSettlementPrice, updatePriceType);
			
			//newOrdSettlementPriceRecord.setNewTotalSettlementPrice(ordOrderItem.getTotalSettlementPrice());
			
			// 保存扣记录
			OrdOrderAmountItem ordOrderAmountItem = new OrdOrderAmountItem();
			ordOrderAmountItem.setItemAmount(newUpdateActualSettlementPrice-newOrdSettlementPriceRecord.getOldActualSettlementPrice());
			ordOrderAmountItem.setOrderId(orderId);
			ordOrderAmountItem.setItemName(OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_ORDER.name());
			ordOrderAmountItem.setOrderAmountType(OrderEnum.ORDER_AMOUNT_TYPE.ACTUAL_SETTLEPRICE.name());
			rs =ordOrderAmountItemDao.insert(ordOrderAmountItem);
		}
		
		if (oldOrdSettlementPriceRecord!=null) {
//			oldOrdSettlementPriceRecord.setUpdateTime(new Date());
			rs =this.ordSettlementPriceRecordDao.updateByPrimaryKeySelective(oldOrdSettlementPriceRecord);
			
		}
		return rs;
	}

	/**
	 * 更新结算价格
	 * @param ordOrderItem
	 * @param newUpdateActualSettlementPrice
	 * @param updatePriceType
	 */
	public void updateNewSettlementPrice(OrdOrderItem ordOrderItem, Long newUpdateActualSettlementPrice,
			String updatePriceType) {
		String[] priceTypeArray = new String[] {ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.getCode() ,
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode() ,
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_GAP.getCode()};
				
		Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
		paramsMulPriceRate.put("orderItemId", ordOrderItem.getOrderItemId()); 
		paramsMulPriceRate.put("priceTypeArray",priceTypeArray); 
		List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
//			OrdMulPriceRate ordMulPriceRate=ordMulPriceRateList.get(0);
		
		Long newTotalSettlementPrice=0L;
		Long newActualSettlementPrice=0L;
		
		if (ordMulPriceRateList.size()>0) {
			
			Long quantity=0L;
			for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {
				
				if (updatePriceType.equalsIgnoreCase(ordMulPriceRate.getPriceType())) {
					newTotalSettlementPrice+=newUpdateActualSettlementPrice*ordMulPriceRate.getQuantity();
					
					//更新多价格类型对应结算价
					ordMulPriceRate.setPrice(newUpdateActualSettlementPrice);
					ordMulPriceRateService.updateByPrimaryKeySelective(ordMulPriceRate);
					
				}else{
					newTotalSettlementPrice+=ordMulPriceRate.getPrice()*ordMulPriceRate.getQuantity();
				}
				
				quantity+=ordMulPriceRate.getQuantity();
			}
			
			newActualSettlementPrice=newTotalSettlementPrice/quantity;
			
		}else{//单价类型
			
			newActualSettlementPrice=newUpdateActualSettlementPrice;
			
			Long buyNum = ordOrderItem.getBuyoutQuantity();
			buyNum = buyNum==null?0L:buyNum;
			newTotalSettlementPrice=newActualSettlementPrice*(ordOrderItem.getQuantity()-buyNum);
			if("Y".equals(ordOrderItem.getBuyoutFlag())){
				Long buyoutTotalPrice = ordOrderItem.getBuyoutTotalPrice();
				buyoutTotalPrice = buyoutTotalPrice==null?0L:buyoutTotalPrice;
				newTotalSettlementPrice +=buyoutTotalPrice;
				newActualSettlementPrice = newTotalSettlementPrice/ordOrderItem.getQuantity();
			}
			
		}
		
		
		ordOrderItem.setTotalSettlementPrice(newTotalSettlementPrice);
		ordOrderItem.setActualSettlementPrice(newActualSettlementPrice);
		
		ordOrderItemDao.updateByPrimaryKeySelective(ordOrderItem);

		//外币保存（存在extend为人民币情况默认保存一样的结算价）
		this.saveExtendPrice(ordOrderItem);
	}

	
	
	public Integer saveReservationForSettlementAmountChange(OrdSettlementPriceRecord newOrdSettlementPriceRecord, String assignor,String content) throws Exception {
		// TODO Auto-generated method stub
		
		ComMessage comMessage=new ComMessage();
		comMessage.setReceiver(newOrdSettlementPriceRecord.getOperatorApprove());
		
		
		int n=0;
		
		OrdOrderItem orderItem=ordOrderItemDao.selectByPrimaryKey(newOrdSettlementPriceRecord.getOrderItemId());
		
		
//		String content="子订单修改结算价<i style='color:red'>"+messge+"</i>，单价金额修改："+
//				priceChange.doubleValue()/100+"元,订单应付金额修改："+amountChange.doubleValue()/100+"元";
//		
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
		
		
	
		return n;
		
	}
	

	@Override
	public List<OrdMulPriceRate> calcSettlementUnitPrice(Long orderItemId,
			Long totalPrice) {
		OrdOrderItem orderItem = ordOrderItemDao.selectByPrimaryKey(orderItemId);
		Long buyoutTotalPrice = orderItem.getBuyoutTotalPrice();
		buyoutTotalPrice = buyoutTotalPrice==null ? 0L: buyoutTotalPrice;
		Long oriTotalPrice = (Long) orderItem.getTotalSettlementPrice() - buyoutTotalPrice;
		//查询出多单价的记录
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", orderItemId);
		String[] priceTypeArray = new String[] {ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.getCode() ,
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_GAP.getCode()};
		
		params.put("priceTypeArray", priceTypeArray);
		List<OrdMulPriceRate> mulPriceRates = ordMulPriceRateDao.selectByParams(params);
		
		
		//查询结算修改记录
		/*HashMap<String, Object> paramsRecord = new HashMap<String, Object>();
		paramsRecord.put("orderItemId", orderItem.getOrderItemId()); 
		paramsRecord.put("_orderby", "ORD_SETTLEMENT_PRICE_RECORD.CREATE_TIME");
		List<OrdSettlementPriceRecord> recordList = ordSettlementPriceRecordDao.findOrdSettlementPriceRecordList(paramsRecord);
		
		if (CollectionUtils.isNotEmpty(recordList)) {//找到数据说明之前已经操作过结算价
			for (OrdMulPriceRate priceRate : mulPriceRates) {
				for (OrdSettlementPriceRecord priceRecord : recordList) {
					if (priceRecord.getPriceType().equals(priceRate.getPriceType())) {
						priceRate.setPrice(priceRecord.getOldActualSettlementPrice());
						break;
					}
				}
			}
		}*/
		return resetOrdMulPriceRate(mulPriceRates, oriTotalPrice, totalPrice);
	}
	@Override
	public List<OrdMulPriceRate> calcSettlementUnitPrice(Long orderItemId,Long totalPrice,Long oriTotalPrice) {
		OrdOrderItem orderItem = ordOrderItemDao.selectByPrimaryKey(orderItemId);
		Long buyoutTotalPrice = orderItem.getBuyoutTotalPrice();
		buyoutTotalPrice = buyoutTotalPrice==null ? 0L: buyoutTotalPrice;
		//Long oriTotalPrice = (Long) orderItem.getTotalSettlementPrice() - buyoutTotalPrice;
		oriTotalPrice = oriTotalPrice - buyoutTotalPrice;

		//查询出多单价的记录
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", orderItemId);
		String[] priceTypeArray = new String[] {ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.getCode() ,
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_GAP.getCode()};

		params.put("priceTypeArray", priceTypeArray);
		List<OrdMulPriceRate> mulPriceRates = ordMulPriceRateDao.selectByParams(params);

		return resetOrdMulPriceRate(mulPriceRates, oriTotalPrice, totalPrice);
	}

	@Override
	public List<OrdMulPriceRate> calcBuyoutSettlementUnitPrice(Long orderItemId,Long totalPrice) {
		OrdOrderItem orderItem = ordOrderItemDao.selectByPrimaryKey(orderItemId);
		Long oriTotalPrice = (Long) orderItem.getBuyoutTotalPrice();
		//查询出多单价的记录
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", orderItemId);
		String[] priceTypeArray = new String[] {
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode() ,
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode()
		};
		
		params.put("priceTypeArray", priceTypeArray);
		List<OrdMulPriceRate> mulPriceRates = ordMulPriceRateDao.selectByParams(params);
		
		
		
		return resetOrdMulPriceRate(mulPriceRates, oriTotalPrice, totalPrice);
	}
	/**
	 * 把修改掉的数量按比例分摊到结算单价中.
	 * @param mulPriceRates
	 * @param oriTotalPrice
	 * @param offset
	 * @return
	 */
	private List<OrdMulPriceRate> resetOrdMulPriceRate(List<OrdMulPriceRate> mulPriceRates, Long oriTotalPrice, Long totalPrice) {
		BigDecimal tempTotal = new BigDecimal(0L);//修改后的总金额
		int index = 0;
		if(oriTotalPrice == 0L){
			int totalNum = 0;
			for (OrdMulPriceRate priceRate : mulPriceRates) {
				totalNum = totalNum + priceRate.getQuantity().intValue();
			}
			LOG.info("总人数：" + totalNum);
			//均摊-按人头均摊
			BigDecimal hasUsed = new BigDecimal(0L);
			BigDecimal avargePrice = BigDecimal.valueOf(totalPrice.longValue()).divide(BigDecimal.valueOf(totalNum), 2,BigDecimal.ROUND_HALF_EVEN);
			LOG.info("按人头均价：" + avargePrice.doubleValue());
			for (OrdMulPriceRate priceRate : mulPriceRates) {
				index++;
				priceRate.setOrigPrice(priceRate.getPrice());
				priceRate.setPrice(avargePrice.longValue());
				if(index == mulPriceRates.size()){
					BigDecimal left = BigDecimal.valueOf(totalPrice.longValue() - hasUsed.longValue());
					priceRate.setPrice(left.divide(BigDecimal.valueOf(priceRate.getQuantity()),2, BigDecimal.ROUND_HALF_EVEN).longValue());
				}
				LOG.info(index + "--" + priceRate.getPrice());
				hasUsed = hasUsed.add(BigDecimal.valueOf(priceRate.getPrice() * priceRate.getQuantity()));
			}
			
			
			
		}else{
			for (OrdMulPriceRate priceRate : mulPriceRates) {
				index++;
				priceRate.setOrigPrice(priceRate.getPrice());//把修改之前的价格缓存起来
				BigDecimal newUnitPrice = BigDecimal.valueOf(priceRate.getQuantity() * priceRate.getPrice())
					.divide(BigDecimal.valueOf(oriTotalPrice), 2, BigDecimal.ROUND_HALF_EVEN)
					.multiply(BigDecimal.valueOf(totalPrice))
					.divide(BigDecimal.valueOf(priceRate.getQuantity()), 2, BigDecimal.ROUND_HALF_EVEN);
				priceRate.setPrice(newUnitPrice.longValue());
				//用最好一个单价抹平总价
				tempTotal = tempTotal.add(BigDecimal.valueOf(priceRate.getPrice()).multiply(BigDecimal.valueOf(priceRate.getQuantity())));
				if (index == mulPriceRates.size()) {
					long lastCount = mulPriceRates.get(mulPriceRates.size() - 1).getQuantity();
					BigDecimal balance = BigDecimal.valueOf(totalPrice).subtract(tempTotal);
					if (lastCount > 0 && balance.compareTo(BigDecimal.ZERO) != 0) {
						priceRate.setPrice(priceRate.getPrice() + balance.multiply(BigDecimal.valueOf(lastCount)).longValue());
					}
				}
			}
		}
		
		return mulPriceRates;
	}

	@Override
	public OrdOrderItem resetOrderItem4Settlement(OrdOrderItem orderItem, Long totalPrice) {
		Long buyNum = orderItem.getBuyoutQuantity();
		buyNum = buyNum==null?0L:buyNum;
		orderItem.setActualSettlementPrice(BigDecimal.valueOf(totalPrice).
					divide(BigDecimal.valueOf(orderItem.getQuantity()-buyNum), 2, BigDecimal.ROUND_HALF_EVEN).longValue());
		orderItem.setTotalSettlementPrice(totalPrice);
		return orderItem;
	}
	

	@Override
	public boolean addSettlementTotalPrice(List<OrdSettlementPriceRecord> priceRecords, Long orderId) {
		if (priceRecords != null && priceRecords.size() > 0) {
			for (OrdSettlementPriceRecord priceRecord : priceRecords) {
				if (ordSettlementPriceRecordDao.insert(priceRecord) <= 0) {
					LOG.info(new StringBuffer("修改结算总价更新结算记录失败，子订单ID：").append(priceRecord.getOrderItemId())
								.append(" priceType=").append(priceRecord.getPriceType()).append(" changeType=").append(priceRecord.getChangeType())
								.append(" NewActualSettlementPrice=").append(priceRecord.getNewActualSettlementPrice())
								.append(" OldActualSettlementPrice").append(priceRecord.getOldActualSettlementPrice()).toString());
					return false;
				}
			}
		} else {
			LOG.info("updateSettlementTotalPrice:结算记录为空");
			return false;
		}
		OrdSettlementPriceRecord totalPriceChangeRecord = priceRecords.get(0);
		// 保存扣记录
		OrdOrderAmountItem ordOrderAmountItem = new OrdOrderAmountItem();
		
		Long newBudgetTotalSettlementPrice = totalPriceChangeRecord.getNewBudgetTotalSettlementPrice()!=null?totalPriceChangeRecord.getNewBudgetTotalSettlementPrice():0;
        Long newTotalSettlementPrice = totalPriceChangeRecord.getNewTotalSettlementPrice()!=null?totalPriceChangeRecord.getNewTotalSettlementPrice():0;
        
        Long oldTotalSettlementPrice = totalPriceChangeRecord.getOldTotalSettlementPrice()!=null?totalPriceChangeRecord.getOldTotalSettlementPrice():0;
        Long oldBudgetTotalSettlementPrice = totalPriceChangeRecord.getOldBudgetTotalSettlementPrice()!=null?totalPriceChangeRecord.getOldBudgetTotalSettlementPrice():0;
        
		
		ordOrderAmountItem.setItemAmount((newBudgetTotalSettlementPrice+newTotalSettlementPrice - oldTotalSettlementPrice-oldBudgetTotalSettlementPrice));
		ordOrderAmountItem.setOrderId(orderId);
		ordOrderAmountItem.setItemName(OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_ORDER.name());
		ordOrderAmountItem.setOrderAmountType(OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_SETTLEPRICE.name());
		if (ordOrderAmountItemDao.insert(ordOrderAmountItem) <= 0) {
			LOG.info("updateSettlementTotalPrice:新增价格变更记录失败！");
			return false;
		}
		return true;
	}
	
	public void updateMulPriceRates(List<OrdMulPriceRate> ordMulPriceRates) {
		if (ordMulPriceRates != null) {
			for (OrdMulPriceRate priceRate : ordMulPriceRates) {
				if (ordMulPriceRateDao.updateByPrimaryKeySelective(priceRate) <= 0) {
					LOG.info(new StringBuffer("修改结算总价更新子订单多价格记录失败，orderItemId：")
								.append(priceRate.getOrderItemId())
								.append(" priceType=").append(priceRate.getPriceType())
								.append(" price=").append(priceRate.getPrice()).toString());
				}
			}
		}
	}

	@Override
	public int saveForeignAfterApprove(OrdSettlementPriceRecord record, OrdOrderItemExtend orderItemExtend) {
		this.insert(record);

		int result = 1 ;

		if(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name().equalsIgnoreCase(record.getStatus())){

			//页面修改的外币结算单价（已转化为人民币）
			Long newUpdateActualSettlementPrice = record.getNewActualSettlementPrice();
			//页面修改的外币结算总价（已转化为人民币）
			Long newUpdateTotalSettlementPrice = record.getNewTotalSettlementPrice();

			//修改子单结算价单价与总价
			OrdOrderItem orderItem = new OrdOrderItem();
			orderItem.setOrderItemId(record.getOrderItemId());
			orderItem.setTotalSettlementPrice(newUpdateTotalSettlementPrice);
			orderItem.setActualSettlementPrice(newUpdateActualSettlementPrice);
			ordOrderItemDao.updateByPrimaryKeySelective(orderItem);

			// 保存金额变动记录
			OrdOrderAmountItem amountItem = new OrdOrderAmountItem();
			amountItem.setItemAmount(newUpdateActualSettlementPrice - record.getOldActualSettlementPrice());
			amountItem.setOrderId(record.getOrderId());
			amountItem.setItemName(OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_ORDER.name());
			if (OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.UNIT_PRICE.getCode().equalsIgnoreCase(record.getChangeType())) {
				amountItem.setOrderAmountType(OrderEnum.ORDER_AMOUNT_TYPE.ACTUAL_SETTLEPRICE.name());
			} else if (OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.TOTAL_PRICE.getCode().equalsIgnoreCase(record.getChangeType())) {
				amountItem.setOrderAmountType(OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_SETTLEPRICE.name());
			}
			ordOrderAmountItemDao.insert(amountItem);

			//更新子单拓展外币记录表
			OrdOrderItemExtend update = new OrdOrderItemExtend();
			update.setUpdateTime(new Date());
			update.setOrderItemId(orderItemExtend.getOrderItemId());
			update.setForeignActualSettlementPrice(orderItemExtend.getForeignActualSettlementPrice());
			update.setForeignActTotalSettlePrice(orderItemExtend.getForeignActTotalSettlePrice());

			ordOrderItemExtendService.updateByPrimaryKeySelective(update);
		}

		return result;
	}

	@Override
	public int saveExtendPrice(OrdOrderItem orderItem) {
		if (orderItem == null || orderItem.getOrdOrderItemExtend() == null) {
			LOG.info("saveExtendPrice no orderItemExtend exist");
			return 0;
		}

		OrdOrderItemExtend extend = orderItem.getOrdOrderItemExtend();

		if ("CNY".equalsIgnoreCase(extend.getCurrencyCode())) {
			OrdOrderItemExtend update = new OrdOrderItemExtend();
			update.setOrderItemId(orderItem.getOrderItemId());
			update.setUpdateTime(new Date());
			update.setForeignActualSettlementPrice(orderItem.getActualSettlementPrice());
			update.setForeignActTotalSettlePrice(orderItem.getTotalSettlementPrice());
			ordOrderItemExtendService.updateByPrimaryKeySelective(update);
		}

		return 1;
	}
}
