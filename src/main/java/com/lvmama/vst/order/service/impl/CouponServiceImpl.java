/**
 * 
 */
package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.goods.vo.CuriseHoldPeopleNumber;

import com.lvmama.vst.comm.utils.gson.GsonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.prod.curise.service.ProdCuriseProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsMultiTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsSimpleTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsSingleTimePrice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.Pair;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Coupon;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfo.ItemPersonRelation;
import com.lvmama.vst.comm.vo.order.FavorStrategyInfo;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.order.service.ICouponService;
import com.lvmama.vst.order.service.IOrderInitService;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.pet.adapter.FavorServiceAdapter;

/**
 * @author pengyayun
 *
 */
@Service
public class CouponServiceImpl implements ICouponService {
	
	private static final Log LOG = LogFactory.getLog(CouponServiceImpl.class);
	
	private static final String ADULT = "adult";
	
	private static final String CHILDREN = "children";
	
	private static final String FIRST_SENCOND_ADULT = "first_sencond_adult";
	
	private static final String THIRD_FOURTH_ADULT = "third_fourth_adult";
	
	private static final String THIRD_FOURTH_CHILDREN = "third_fourth_children";
	
	@Autowired
	protected FavorServiceAdapter favorService;
	
	@Autowired
	protected SuppGoodsClientService suppGoodsClientService;
	
	@Autowired
	private CategoryClientService categoryClientService;
	
	@Autowired
	private IOrderInitService orderInitService;
	
	/**
	 * 时间价格业务接口
	 */
	@Autowired
	private DistGoodsTimePriceClientService distGoodsTimePriceClientService;
	
	@Autowired
	private ProdCuriseProductClientService prodCuriseProductClientService;
	
	@Override
	public ResultHandle validateCoupon(BuyInfo buyInfo) {
		// TODO Auto-generated method stub
		ResultHandle result=new ResultHandle();
		List<Coupon> list=buyInfo.getCouponList();
		if(null!=list&&list.size()>0){
			OrdOrder orderVst=initOrdOrder(buyInfo);
			Long productId = 0L;
			if (CollectionUtils.isNotEmpty(buyInfo.getProductList()) && buyInfo.getProductList().get(0) != null) {
				productId = buyInfo.getProductList().get(0).getProductId();
			}
			LOG.info("Now invoking coupon remote interface for product " + productId + " buy info is " + GsonUtils.toJson(buyInfo) + ", orderVst is " + GsonUtils.toJson(orderVst) + ", couponList is " + GsonUtils.toJson(buyInfo.getCouponList()));
			result=favorService.validateCoupon(orderVst, buyInfo.getCouponList());
			LOG.info("Invoking coupon remote interface for product " + productId + " finished, result is " + GsonUtils.toJson(result));
		}else{
			result.setMsg("优惠券代码为空.");
		}
		return result;
	}

	@Override
	public Pair<FavorStrategyInfo,Object> calCoupon(BuyInfo buyInfo) {
		// TODO Auto-generated method stub
		Pair<FavorStrategyInfo,Object> result=new Pair<FavorStrategyInfo, Object>();
		List<Coupon> list=buyInfo.getCouponList();
		if(null!=list&&list.size()>0){
			/*OrdOrder orderVst=initOrdOrder(buyInfo);
			result=favorService.calculateFavor(orderVst, list.get(0).getCode());*/
			//OrdOrderDTO orderDto= new OrdOrderDTO(buyInfo);
			OrdOrderDTO orderDto = orderInitService.initOrderAndCalc(buyInfo);
			
			OrdOrder order=new OrdOrder();
			order.setDistributionChannel(buyInfo.getDistributionChannel());
			order.setDistributorId(buyInfo.getDistributionId());
			List<OrdOrderItem> nopackOrderItemList = orderDto.getNopackOrderItemList();
			if(nopackOrderItemList!=null){
				List<OrdOrderItem> itemList = new ArrayList<OrdOrderItem>();
				for(OrdOrderItem orderItem:nopackOrderItemList){
					OrdOrderItem item = new OrdOrderItem();
					item.setPrice(orderItem.getPrice());
					item.setQuantity(orderItem.getQuantity());
					item.setSuppGoods(orderItem.getSuppGoods());
					item.setSuppGoodsId(orderItem.getSuppGoodsId());
					item.setCategoryId(orderItem.getCategoryId());
					item.setSuppGoods(orderItem.getSuppGoods());
					
					itemList.add(item);
				}
				order.setNopackOrderItemList(itemList);
			}
			List<OrdOrderPack> packList = orderDto.getOrderPackList();
			if(packList!=null){
				List<OrdOrderPack> orderPackList = new ArrayList<OrdOrderPack>();
				for(OrdOrderPack packDto :packList){
					OrdOrderPack pack = new OrdOrderPack();
					pack.setCategoryId(packDto.getCategoryId());
					pack.setContent(packDto.getContent());
					pack.setProductId(packDto.getProductId());
					
					List<OrdOrderItem> itemDtoList = packDto.getOrderItemList();
					if(itemDtoList!=null){
						List<OrdOrderItem> itemList = new ArrayList<OrdOrderItem>();
						for(OrdOrderItem orderItem:itemDtoList){
							OrdOrderItem item = new OrdOrderItem();
							item.setPrice(orderItem.getPrice());
							item.setQuantity(orderItem.getQuantity());
							item.setSuppGoods(orderItem.getSuppGoods());
							item.setSuppGoodsId(orderItem.getSuppGoodsId());
							item.setCategoryId(orderItem.getCategoryId());
							itemList.add(item);
						}
						pack.setOrderItemList(itemList);
					}
					orderPackList.add(pack);
				}
				order.setOrderPackList(orderPackList);
			}
			order.setOughtAmount(orderDto.getOughtAmount());
			
			/**long oughtAmount=0;
			List<OrdOrderItem> orderItemDtoList=orderDto.getOrderItemList();
			List<OrdOrderItem> orderItemList=new ArrayList<OrdOrderItem>();
			for (OrdOrderItem ordOrderItem : orderItemDtoList) {
				oughtAmount+=ordOrderItem.getPrice()*ordOrderItem.getQuantity();
				OrdOrderItem item =new OrdOrderItem();
				item.setSuppGoods(ordOrderItem.getSuppGoods());
				item.setSuppGoodsId(ordOrderItem.getSuppGoodsId());
				item.setPrice(ordOrderItem.getPrice());
				item.setQuantity(ordOrderItem.getQuantity());
				item.setCategoryId(ordOrderItem.getCategoryId());
				orderItemList.add(item);
			}
			order.setOughtAmount(oughtAmount);
			order.setOrderItemList(orderItemList);*/
			try {
				result = favorService.calculateFavor(
						order,
						list.get(0).getCode(),
						buyInfo.getUserNo());
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
			}
			
		}else{
			result.setMsg("优惠券代码为空.");
		}
		return result;
	}
	
	private OrdOrder initOrdOrder(BuyInfo buyInfo){
		OrdOrder orderVst=new OrdOrder();
	 	orderVst.setOughtAmount(buyInfo.getOrderTotalPrice());

		// 获取产品ID
		Long productId = buyInfo.getProductId();
	 	List<OrdOrderItem> orderItemList=new ArrayList<OrdOrderItem>();
	 	List<Item> itemList = buyInfo.getItemList();
		// 商品子项存在
		if ((itemList != null) && (itemList.size() > 0)) {
			for (int i = 0; i < itemList.size(); i++) {
				Item item = itemList.get(i);
				OrdOrderItem orderItem = new OrdOrderItem();
				orderItem.setSuppGoodsId(item.getGoodsId());
				if(null!=productId){
					orderItem.setProductId(productId);
				}
				ResultHandleT<SuppGoods> goodsResultHandleT=suppGoodsClientService.findSuppGoodsById(item.getGoodsId(), Boolean.TRUE, Boolean.TRUE);
					
				if(goodsResultHandleT!=null&&goodsResultHandleT.getReturnContent()!=null){
					SuppGoods suppGoods=goodsResultHandleT.getReturnContent();
					BizCategory category = categoryClientService.findCategoryById(suppGoods.getProdProduct().getBizCategoryId()).getReturnContent();
					
					String categoryCode = category.getCategoryCode();
					//酒店
					if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.name().equalsIgnoreCase(categoryCode))
					{
						
					//邮轮
					}else if (BizEnum.BIZ_CATEGORY_TYPE.category_cruise.name().equalsIgnoreCase(categoryCode)) {
						 
						ResultHandleT<SuppGoodsMultiTimePrice> timePriceHolder = distGoodsTimePriceClientService.findSuppGoodsMultiTimePrice(Constant.DIST_BACK_END, item.getGoodsId(), item.getVisitTimeDate());
						
						if(null==timePriceHolder||timePriceHolder.getReturnContent()==null){
							orderItem.setPrice(0L);
						}else{
							orderItem.setCategoryId(category.getCategoryId());
							orderItem.setBranchId(suppGoods.getProdProductBranch().getProductBranchId());
							initOrderItemMultiTimePrice(buyInfo, item, orderItem, timePriceHolder.getReturnContent());
						}

					//岸上观光、邮轮附加项
					}else if (BizEnum.BIZ_CATEGORY_TYPE.category_sightseeing.name().equalsIgnoreCase(categoryCode)
							|| BizEnum.BIZ_CATEGORY_TYPE.category_cruise_addition.name().equalsIgnoreCase(categoryCode)) {
						
						ResultHandleT<SuppGoodsSingleTimePrice> timePriceHolder = distGoodsTimePriceClientService.findSuppGoodsSingleTimePrice(Constant.DIST_BACK_END, item.getGoodsId(), item.getVisitTimeDate());
						if(null==timePriceHolder||timePriceHolder.getReturnContent()==null){
							orderItem.setPrice(0L);
						}else{
							initOrderItemSingleTimePrice(buyInfo, item, orderItem, timePriceHolder.getReturnContent());
						}

					//签证
					}else if (BizEnum.BIZ_CATEGORY_TYPE.category_visa.name().equalsIgnoreCase(categoryCode)) {
						ResultHandleT<SuppGoodsSimpleTimePrice> timePriceHolder = distGoodsTimePriceClientService.findSuppGoodsSimpleTimePrice(Constant.DIST_BACK_END, item.getGoodsId(), item.getVisitTimeDate());
						if(null==timePriceHolder||timePriceHolder.getReturnContent()==null){
							orderItem.setPrice(0L);
						}else{
							orderItem.setPrice(timePriceHolder.getReturnContent().getPrice());
						}
					//门票
					}else if (BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.name().equalsIgnoreCase(categoryCode)
                            ||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.name().equalsIgnoreCase(categoryCode)
                            ||BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.name().equalsIgnoreCase(categoryCode)) {
						ResultHandleT<SuppGoodsAddTimePrice> timePriceHolder = distGoodsTimePriceClientService.findSuppGoodsTicketTimePriceList(Constant.DIST_BACK_END, item.getGoodsId(), DateUtil.getDateByStr(buyInfo.getVisitTime(), "yyyy-MM-dd"));
						if(null==timePriceHolder||timePriceHolder.getReturnContent()==null){
							orderItem.setPrice(0L);
						}else{
							orderItem.setPrice(timePriceHolder.getReturnContent().getPrice());
						}
					}
					Long categoryID =  category.getCategoryId();
					if(null!=categoryID){
						orderItem.setCategoryId(categoryID);
					}
				}
				orderItem.setQuantity(Long.valueOf(item.getQuantity()));
				orderItemList.add(orderItem);
			}
		} 
	 	orderVst.setOrderItemList(orderItemList);
	 	return orderVst;
	}
	
	/**
	 * 设置游轮 价格
	 * @param buyInfo
	 * @param item
	 * @param orderItem
	 * @param timePrice
	 */
	private void initOrderItemMultiTimePrice(BuyInfo buyInfo,BuyInfo.Item item,OrdOrderItem orderItem,SuppGoodsMultiTimePrice timePrice){
		
		long adultPersonCount12 = 0;
		long adultPersonCount34 = 0;
		long childrenPersonCount34 = 0;
		long bedPersonCount=0;
		long allPriceAmount = 0;
		long allSettlementPriceAmount = 0;
		long allMarketPriceAmount = 0;
		long houseNum=item.getQuantity();//当前子订单(商品)仓房订购数量
		long child=item.getChildQuantity();
		long adult=item.getAdultQuantity();
		

		ResultHandleT<CuriseHoldPeopleNumber> resultHandleLong = prodCuriseProductClientService.getCuriseMaxHoldPeopleNumber(orderItem.getCategoryId(), orderItem.getBranchId());
		if (resultHandleLong.isSuccess()) {
			int maxPersonCount = resultHandleLong.getReturnContent().getMaxHoldPeopleNumber().intValue();
			//当前商品成人数和儿童数之和
			long totalPerson=adult+child;
			//需要支付的床位费人数=房间数*房间最大入住人数-总人数
			bedPersonCount=houseNum*maxPersonCount-totalPerson;
			
			if(houseNum * 2 >= totalPerson){
				//支付第一二成人价人数等于总人数
				adultPersonCount12 = totalPerson;
	        }else{
	            if((totalPerson - houseNum*2) <= child){
	            	adultPersonCount12=houseNum*2;//需要支付成人价的份数等于房间数*2
	            	childrenPersonCount34=totalPerson -houseNum*2;//需要支付三四儿童人数=总人数-支付一二人价格的人数
	            }else{
	            	adultPersonCount12=houseNum*2;//需要支付成人价的份数等于房间数*2
	            	childrenPersonCount34=child;//需要支付三四儿童价格
	            	adultPersonCount34=adult -houseNum*2;
	                
	            }
	        }
			//第一二人价格
			allPriceAmount += timePrice.getFstPrice() * adultPersonCount12;
			//第三四成人价格
			if (adultPersonCount34 > 0) {
				if(timePrice.getSecPrice()==null){
					timePrice.setSecPrice(timePrice.getFstPrice());
				}
				allPriceAmount += timePrice.getSecPrice() * adultPersonCount34;
			}
			//第三四儿童价格
			if (childrenPersonCount34 > 0) {
				if(timePrice.getChildPrice()==null){
					timePrice.setChildPrice(timePrice.getFstPrice());
				}
				allPriceAmount += timePrice.getChildPrice() * childrenPersonCount34;
			}
			//第三四儿童价格
			if (bedPersonCount > 0) {
				if(timePrice.getGapMarketPrice()==null){
					timePrice.setGapMarketPrice(timePrice.getFstPrice());
				}
				allPriceAmount += timePrice.getGapPrice() * bedPersonCount;
			}
			
			long priceAmount = (long)(allPriceAmount * 1.0 / item.getQuantity() + 0.5);
			// 单价
			if (orderItem.getPrice() == null) {
				orderItem.setPrice(priceAmount);
			} else {
				orderItem.setPrice(orderItem.getPrice() + priceAmount);
			}
		}else{
			orderItem.setPrice(0L);
		}
	}
	
	
	/**
	 * 设置岸上观光、邮轮附加项 价格
	 * @param buyInfo
	 * @param item
	 * @param orderItem
	 * @param timePrice
	 */
	private void initOrderItemSingleTimePrice(BuyInfo buyInfo,BuyInfo.Item item,OrdOrderItem orderItem,SuppGoodsSingleTimePrice timePrice){
		
		Map<String, List<Person>> splitedPersonMap = splitTravllerrPerson(item.getItemPersonRelationList());
		long allPriceAmount = 0;
		int count=0;
		List<Person> ordAdultPersonList = splitedPersonMap.get(ADULT);
		if (ordAdultPersonList != null && !ordAdultPersonList.isEmpty()) {
			count = ordAdultPersonList.size();
			allPriceAmount += timePrice.getAuditPrice() * count;
		}
		
		List<Person> ordChildrenPersonList = splitedPersonMap.get(CHILDREN);
		if (ordChildrenPersonList != null && !ordChildrenPersonList.isEmpty()) {
			count = ordChildrenPersonList.size();
			allPriceAmount += timePrice.getChildPrice() * count;
		}
		long priceAmount = (long)(allPriceAmount * 1.0 / item.getQuantity() + 0.5);
		// 单价
		if (orderItem.getPrice() == null) {
			orderItem.setPrice(priceAmount);
		}else{
			orderItem.setPrice(orderItem.getPrice() + priceAmount);
		}
	}
	
	
	/**
	 * 
	 * @param itemPersonRelationList
	 * @return
	 */
	private Map<String, List<Person>> splitTravllerrPerson(List<ItemPersonRelation>  itemPersonRelationList) {
		Map<String, List<Person>> personMap = new HashMap<String, List<Person>>();
		String peopleType = null;
		Person person = null;
		for (ItemPersonRelation itemPersonRelation : itemPersonRelationList) {
			if (itemPersonRelation != null && itemPersonRelation.getPerson() != null) {
				person = itemPersonRelation.getPerson();
				peopleType = person.getPeopleType();
				if (peopleType == null || !peopleType.equals(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_CHILD.name())) {
					List<Person> ordAdultPersonList = personMap.get(ADULT);
					if (ordAdultPersonList == null) {
						ordAdultPersonList = new ArrayList<Person>();
						personMap.put(ADULT, ordAdultPersonList);
					}
					
					ordAdultPersonList.add(person);
				} else {
					List<Person> ordChildrenPersonList = personMap.get(CHILDREN);
					if (ordChildrenPersonList == null) {
						ordChildrenPersonList = new ArrayList<Person>();
						personMap.put(CHILDREN, ordChildrenPersonList);
					}
					
					ordChildrenPersonList.add(person);
				}
			}
		}
		
		return personMap;
	}
	
	
	private List<Map<String, List<Person>>> splitTravllerrPersonWithSequence(List<ItemPersonRelation> itemPersonRelationList, long quantity, int maxPersonCount) {
		List<Map<String, List<Person>>> splitedSequencePersonList = new ArrayList<Map<String, List<Person>>>();
		List<Person> ordAdultPersonList = new ArrayList<Person>();
		List<Person> ordChildrenPersonList = new ArrayList<Person>();
		
		Map<String, List<Person>> personMap = null;
		List<Person> personList = null;
		
		int quantityIndex = 0;
		int personCountInRoom = 0;
		
		splitTravllerrPersonByAdultAndChildren(itemPersonRelationList, ordAdultPersonList, ordChildrenPersonList);
		

		
		for (int k = 0; k < quantity; k++) {
			personMap = new HashMap<String, List<Person>>();
			splitedSequencePersonList.add(personMap);
		}
		
		for (Person person : ordAdultPersonList) {
			personMap = splitedSequencePersonList.get(quantityIndex);
			personCountInRoom = computeAllPersonCount(personMap);
			if (personCountInRoom > maxPersonCount) {
				throw new IllegalArgumentException("人数超过最大入住人数。");
			}
			
			//第一二人成人
			personList = personMap.get(FIRST_SENCOND_ADULT);
			if (personList == null) {
				personList = new ArrayList<Person>();
				personMap.put(FIRST_SENCOND_ADULT, personList);
			}
			
			if (personList.size() < 2) {
				personList.add(person);
				quantityIndex = (int) ((quantityIndex + 1) % quantity);
				continue;
			}
			
			//第三四人成人
			personList = personMap.get(THIRD_FOURTH_ADULT);
			if (personList == null) {
				personList = new ArrayList<Person>();
				personMap.put(THIRD_FOURTH_ADULT, personList);
			}
			
			personList.add(person);
			quantityIndex = (int) ((quantityIndex + 1) % quantity);
		}
		
		for (Person person : ordChildrenPersonList) {
			personMap = splitedSequencePersonList.get(quantityIndex);
			personCountInRoom = computeAllPersonCount(personMap);
			if (personCountInRoom > maxPersonCount) {
				throw new IllegalArgumentException("人数超过最大入住人数。");
			}
			
			//第一二人儿童
			personList = personMap.get(FIRST_SENCOND_ADULT);
			if (personList == null || personList.isEmpty()) {
				throw new IllegalArgumentException("人数和订购数量不匹配（儿童不能单独入住一个房间）。");
			}
			
			if (personList.size() < 2) {
				personList.add(person);
				quantityIndex = (int) ((quantityIndex + 1) % quantity);
				continue;
			}
			
			//第三四人儿童
			personList = personMap.get(THIRD_FOURTH_CHILDREN);
			if (personList == null) {
				personList = new ArrayList<Person>();
				personMap.put(THIRD_FOURTH_CHILDREN, personList);
			}
			
			personList.add(person);
			quantityIndex = (int) ((quantityIndex + 1) % quantity);
		}
		
		return splitedSequencePersonList;
	}
	
	private void splitTravllerrPersonByAdultAndChildren(List<ItemPersonRelation> itemPersonRelationListList, List<Person> ordAdultPersonList, List<Person> ordChildrenPersonList) {
		String peopleType = null;
		if (itemPersonRelationListList != null && !itemPersonRelationListList.isEmpty()) {
			for (ItemPersonRelation itemPersonRelation : itemPersonRelationListList) {
				if (itemPersonRelation != null && itemPersonRelation.getPerson() != null) {
					Person person = itemPersonRelation.getPerson();
					peopleType = person.getPeopleType();
					if (peopleType == null || !peopleType.equals(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_CHILD.name())) {
						ordAdultPersonList.add(person);
					} else {
						ordChildrenPersonList.add(person);
					}
				}
			}
		}
		
	}
	
	
	private int computeAllPersonCount(Map<String, List<Person>> itemPersonMap) {
		int personCount = 0;
		
		List<Person> adultPerson12List = itemPersonMap.get(FIRST_SENCOND_ADULT);
		if (adultPerson12List != null) {
			personCount = personCount + adultPerson12List.size();
		}
		
		List<Person> adultPerson34List = itemPersonMap.get(THIRD_FOURTH_ADULT);
		if (adultPerson34List != null) {
			personCount = personCount + adultPerson34List.size();
		}
		
		List<Person> childrenPerson34List = itemPersonMap.get(THIRD_FOURTH_CHILDREN);
		if (childrenPerson34List != null) {
			personCount = personCount + childrenPerson34List.size();
		}
		
		return personCount;
	}
}
