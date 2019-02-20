package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.lvmama.dest.api.utils.DynamicRouterUtils;
import com.lvmama.prod.router.service.RouterService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.client.prom.service.PromForbidBuyClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.prod.adapter.ProdProductHotelAdapterClientService;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prom.po.PromForbidKeyPo;
import com.lvmama.vst.back.prom.vo.PromForbidBuyQuery;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfo.Product;
import com.lvmama.vst.order.service.IOrderPromService;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;

@Service("orderPromServiceImpl")
public class OrderPromServiceImpl implements IOrderPromService{

	private static final Logger LOG = LoggerFactory.getLogger(OrderPromServiceImpl.class);
	
	@Autowired
	private ProdProductHotelAdapterClientService prodProductHotelAdapterClientServiceRemote;

	@Autowired
	private SuppGoodsHotelAdapterClientService suppGoodsClientAdapterClientRemote;
    
	@Autowired
	private RouterService routerService;
	
	@Autowired
	private PromForbidBuyClientService  promForbidBuyClientService;
	@Autowired
	private UserUserProxyAdapter userProxyAdapter;

	@Override
	public PromForbidKeyPo isPromForbidBuyOrder(BuyInfo buyInfo) {
		PromForbidKeyPo promForbidKeyPo = null;
		LOG.info("start  isPromForbidBuyOrder");
		//默认不属于限购
		//boolean  checkBuy = false;
		try
		{
			PromForbidBuyQuery query = new PromForbidBuyQuery();
			Long categoryId = 0L;
			Long productId=0L;
			LOG.info("buyInfo  productid is"+buyInfo.getProductId());
			
			ProdProduct prodProduct = null;
			if(null !=buyInfo.getProductId()){
//				prodProduct = prodProductClientService.findProdProductById(buyInfo.getProductId()).getReturnContent();
				prodProduct = prodProductHotelAdapterClientServiceRemote.findProdProductByIdFromCache(buyInfo.getProductId()).getReturnContent();
				categoryId = prodProduct.getBizCategoryId();
				productId = buyInfo.getProductId();
			}else
			{
				Long mainSuppGoodsId= 0L;
				if(CollectionUtils.isNotEmpty(buyInfo.getItemList())){
					for (Item item : buyInfo.getItemList()) {		
						if("true".equals(item.getMainItem())){
							mainSuppGoodsId = item.getGoodsId();
							break;
						}		
					}
				}
				
				if(mainSuppGoodsId.intValue() !=0){				
					SuppGoodsParam param = new SuppGoodsParam();
					ResultHandleT<SuppGoods> resultHandle = suppGoodsClientAdapterClientRemote.findSuppGoodsById(mainSuppGoodsId, param);
					if(null !=resultHandle.getReturnContent()){
						SuppGoods suppGoods = resultHandle.getReturnContent();
						categoryId = suppGoods.getCategoryId();
					}
				}

			}
			
			if(categoryId.intValue() ==0){
				if(CollectionUtils.isNotEmpty(buyInfo.getProductList())){
					Product product = buyInfo.getProductList().get(0);
					//prodProduct = prodProductClientService.findProdProductById(product.getProductId()).getReturnContent();
					prodProduct = prodProductHotelAdapterClientServiceRemote.findProdProductByIdFromCache(product.getProductId()).getReturnContent();
					categoryId = prodProduct.getBizCategoryId();
					if(productId ==0L){
						productId = prodProduct.getProductId();
					}
				}
			}
			
			if(categoryId.intValue()>0){				
				query = toPromForbidBuy(buyInfo,categoryId,productId);
				LOG.info("query to sring"+query.toString());
				promForbidKeyPo = promForbidBuyClientService.checkExistRestrainedBuy(query);
				LOG.info("promForbidBuyClientService  checkExistRestrainedBuy return"+buyInfo.getUserId()+"promForbidKeyPo.tostring::"+promForbidKeyPo.toString());
				return promForbidKeyPo;
			}else
			{	
				LOG.error("限购检查异常:categoryId is 0");
			}		
		}catch(Exception e){
			//checkBuy = false;
			LOG.error("限购检查异常", e);
			e.printStackTrace();
		}	
		return promForbidKeyPo;
	}



	/**
     * 限购数据验证数据转换
     * @param buyInfo
     * @param categoryId
     * @return
     */
    public  PromForbidBuyQuery toPromForbidBuy(BuyInfo buyInfo,Long categoryId,Long productId){
    	LOG.info("start toPromForbidBuy is and categoryId is"+categoryId);
    	PromForbidBuyQuery query = new PromForbidBuyQuery();
		query.setObjectIds(new ArrayList<Long>());
		query.setCategoryId(categoryId);
		if(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(categoryId)){
			LOG.info("自主打包或者供应商打包");			
			query.setObjectType("PRODUCT");
			if(buyInfo.getProductId()!=null){
				query.getObjectIds().add(buyInfo.getProductId());
			}else{
				if(CollectionUtils.isNotEmpty(buyInfo.getProductList())){
					if( buyInfo.getProductList().size()==1){
						query.getObjectIds().add(buyInfo.getProductList().get(0).getProductId());
					}else{
						List<Product> productList = buyInfo.getProductList();
						for (Product p : productList) {
							//ProdProduct prodProduct = prodProductClientService.findProdProductById(p.getProductId()).getReturnContent();
							ProdProduct prodProduct = prodProductHotelAdapterClientServiceRemote.findProdProductByIdFromCache(p.getProductId()).getReturnContent();
							if(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(prodProduct.getBizCategory().getCategoryId())){
								query.getObjectIds().add(p.getProductId());
							}
						}
					}
									
				}
			}
			//酒店调用适配器，以商品id验证
		} else if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(categoryId)) {
			LOG.info("酒店 以商品ID验证");
			query.setObjectType("GOODS");
			List<Item> itemList = buyInfo.getItemList();
			if(CollectionUtils.isNotEmpty(itemList)){
				if(itemList.size() == 1){
					query.getObjectIds().add(itemList.get(0).getGoodsId());
				} else {
					List<Long> suppGoodsIdList = new ArrayList<>();
					List<Long> destApiHotelSuppGoodsList;
					//是否包含对接商店商品
					boolean hasDestApiHotelGoods = false;
					for (Item item : itemList) {
						suppGoodsIdList.add(item.getGoodsId());
						ResultHandleT<Boolean> result = suppGoodsClientAdapterClientRemote.isGrayHotelGoods(item.getGoodsId());
						if(result.getReturnContent()){
							hasDestApiHotelGoods = true;
						}
					}
					if(hasDestApiHotelGoods) {
						destApiHotelSuppGoodsList = filterDestApiHotelSuppGoodsList(suppGoodsIdList);
						if(CollectionUtils.isNotEmpty(destApiHotelSuppGoodsList)){
							query.getObjectIds().addAll(destApiHotelSuppGoodsList);
							//去除目的地酒店的goodsId
							suppGoodsIdList = ListUtils.subtract(suppGoodsIdList, destApiHotelSuppGoodsList);
						}
					}

					List<Long> vstSuppGoodsIdList = queryVstSuppGoodsIdList(suppGoodsIdList);
					query.getObjectIds().addAll(vstSuppGoodsIdList);
				}
			}

			//酒店，景点门票，其它票，玩乐演出票 签证 以商品ID验证
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCategoryId().equals(categoryId)
				|| BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(categoryId)
				|| BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(categoryId)
				|| BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(categoryId)){
			LOG.info("景点门票，其它票，签证 以商品ID验证");
			query.setObjectType("GOODS");
			List<Item> itemList = buyInfo.getItemList();
			if(CollectionUtils.isNotEmpty(itemList)){
				if(itemList.size()==1){
					query.getObjectIds().add(itemList.get(0).getGoodsId());
				}else{
					List<Long> suppGoodsList = new ArrayList<Long>();
					for (Item item : itemList) {
						suppGoodsList.add(item.getGoodsId());
					}
					List<Long> vstSuppGoodsIdList = queryVstSuppGoodsIdList(suppGoodsList);
					query.getObjectIds().addAll(vstSuppGoodsIdList);
					
				}
				
			}			
		}else
		{
			LOG.info("其它产品验证 ");
			if(null!=buyInfo.getProductId()){
				query.getObjectIds().add(buyInfo.getProductId());
			}else{
				query.getObjectIds().add(productId);
			}
			
			query.setObjectType("PRODUCT");
		}
		query.setVisitDate(DateUtil.toDate(getVisitTime(buyInfo, categoryId), "yyyy-MM-dd"));
		List<Person> travellers = buyInfo.getTravellers();
		if(CollectionUtils.isNotEmpty(travellers))
		{
			List<String> phoneNums = new ArrayList<String>();
			List<Map<String, String>> certificateMapList = new ArrayList<Map<String,String>>();
			for (Person person : travellers) {
				Map<String, String> certificateMap = new HashMap<String, String>();
				if(StringUtils.isNotEmpty(person.getMobile())){
					phoneNums.add(person.getMobile());
				}		
				certificateMap.put("certificateType", person.getIdType());
				certificateMap.put("certificateVal", person.getIdNo());
				certificateMapList.add(certificateMap);
			}
				query.setPhoneNumbers(phoneNums);
			
			query.setCertificateMap(certificateMapList);
		}
		if(null != buyInfo.getUserNo()){
			LOG.info("buyInfo userNo is "+buyInfo.getUserNo());
			UserUser user = userProxyAdapter.getUserUserByPk(buyInfo.getUserNo());
			if(user!=null){
				query.setUserName(user.getUserName());
				query.setUserNo(user.getId());
			}
		}else{
			LOG.info("buyInfo userNo is null");
		}
		
		if(StringUtils.isNotEmpty(buyInfo.getMobileEquipmentNo())){
			LOG.info("buyInfo MobileEquipmentNo is "+buyInfo.getMobileEquipmentNo());
			query.setMobileId(buyInfo.getMobileEquipmentNo());
		}
		//设置渠道的对应匹配的这张表PROM_FORBID_BUY 的渠道数字
		/*
		 * 后台---2
		 * 前台---3
		 * 无线---4
		 * 兴旅同业 ---5
		 * 特卖会---6
		 * 其他分销---7
		 * 
		 * **/
		if(null != buyInfo.getDistributionId()){
			LOG.info("buyInfo distributionId is " + buyInfo.getDistributionId());
			Long buyinfoDistributionId =buyInfo.getDistributionId();
			
			if(buyinfoDistributionId==1L||buyinfoDistributionId==2L||buyinfoDistributionId==3L||buyinfoDistributionId==5L){
				LOG.info("buyInfo distributionId 1235");
				query.setDistributorId(buyInfo.getDistributionId());
			}
			else if(buyinfoDistributionId==4L && null != buyInfo.getDistributionChannel()){
				 Long distributionChannel = buyInfo.getDistributionChannel();
				 if(distributionChannel==10000){
					 LOG.info("distributionChannel 10000");
					 query.setDistributorId(4L);
				 }
				 else if(distributionChannel==10001||distributionChannel==10002||distributionChannel==108||distributionChannel==110){
					 LOG.info("distributionChannel 10001");
					 query.setDistributorId(6L);
				 }
				 else{
					 LOG.info("distributionChannel else 7L");
					 query.setDistributorId(7L);
				 }
			} else if(buyinfoDistributionId==6L){
				LOG.info("distributionChannel else 8L");
                query.setDistributorId(8L);
			}else if(buyinfoDistributionId == 21L){
				//立体设备使用  不限购
				LOG.info("distributionChannel else 21L");
                query.setDistributorId(21L);
			}else{
				 LOG.info("distributionChannel else waiceng 7L");
				 query.setDistributorId(7L);
			}

		}
		
		if(null != buyInfo.getDistributionChannel()){
			LOG.info("buyInfo distributionChannel is " + buyInfo.getDistributionChannel());
			query.setDistributionChannel(buyInfo.getDistributionChannel());
		}
		
		if(null!=buyInfo.getMobileEquipmentNo()){
			LOG.info("buyInfo MobileEquipmentNo is " + buyInfo.getMobileEquipmentNo());
			query.setMobileEquipmentNo(buyInfo.getMobileEquipmentNo());
		}
		
			query.setQuantity(buyInfo.getQuantity());
			Map<Long,Integer> goodsIdsAndquantity =new HashMap<Long, Integer>();
			Map<String,List<Long>> typeWithids =new HashMap<String, List<Long>>();
			
			List<Long> ids=new ArrayList<Long>();
			List<Long> idsproduct=new ArrayList<Long>();
			idsproduct.add(productId);
		
			if(CollectionUtils.isNotEmpty(buyInfo.getItemList())){
				for (Item item : buyInfo.getItemList()) {		
					if(item.getQuantity()>0){
						goodsIdsAndquantity.put(item.getGoodsId(), item.getQuantity());
						ids.add(item.getGoodsId());
					}
				}
				typeWithids.put("GOODS",ids);
				typeWithids.put("PRODUCT",idsproduct);
				goodsIdsAndquantity.put(productId, 1);
			}
				if(CollectionUtils.isNotEmpty(buyInfo.getProductList())){
					for (Product product: buyInfo.getProductList()) {
						if(product.getItemList()!=null&&product.getItemList().size()>0){
							for (Item item : product.getItemList()) {
								if(item.getQuantity()>0){
									goodsIdsAndquantity.put(item.getGoodsId(),item.getQuantity());
									ids.add(item.getGoodsId());
								}
								typeWithids.put("GOODS",ids);
							}
						}
						idsproduct.add(product.getProductId());
						typeWithids.put("PRODUCT",idsproduct);
						goodsIdsAndquantity.put(product.getProductId(), 1);
					}
			}
			query.setIdsWhitsType(typeWithids);
			query.setGoodsIdsAndquantity(goodsIdsAndquantity);
			query.setCreateDate(new Date());
			LOG.info("IdsWhitsType ==" +typeWithids +"goodsIdsAndquantity" +goodsIdsAndquantity);
			//设置是否限购
			query.setIsUseForbidBuy(buyInfo.getIsUseForbidBuy());
		return query;
	}

	//查询出goodsId中有多少是vst中已经包含的goods
	private List<Long> queryVstSuppGoodsIdList(List<Long> suppGoodsIdList) {
		List<Long> vstSuppGoodsIdList = new ArrayList<>();
		ResultHandleT<List<SuppGoods>> resultSuppGoods =suppGoodsClientAdapterClientRemote.findSuppGoodsByIdList(suppGoodsIdList);
		if(null != resultSuppGoods){
			List<SuppGoods> listGoods = resultSuppGoods.getReturnContent();
			for (SuppGoods suppGoods : listGoods) {
				if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(suppGoods.getCategoryId())){
					continue;
				}
				vstSuppGoodsIdList.add(suppGoods.getSuppGoodsId());
			}
		}

		return vstSuppGoodsIdList;
	}

	//筛选出目的地酒店的商品id
	private List<Long> filterDestApiHotelSuppGoodsList(List<Long> suppGoodsIdList) {
		List<Long> destHotelSuppGoodsList = new ArrayList<>();
		ResultHandleT<List<SuppGoods>> resultHandle = suppGoodsClientAdapterClientRemote.findSuppGoodsByIdList(suppGoodsIdList);
		if(null != resultHandle && null != resultHandle.getReturnContent()){
			List<SuppGoods> suppGoodsList = resultHandle.getReturnContent();
			if(CollectionUtils.isNotEmpty(suppGoodsList)){
				for (SuppGoods suppGoods : suppGoodsList) {
					destHotelSuppGoodsList.add(suppGoods.getSuppGoodsId());
				}
			}
		}
		return destHotelSuppGoodsList;
	}


	public String getVisitTime(BuyInfo buyInfo,Long categoryId){
    	String visitTime = null;
    	visitTime = buyInfo.getVisitTime();
    	if(StringUtils.isEmpty(visitTime)){
    		if(CollectionUtils.isNotEmpty(buyInfo.getProductList())){
    			visitTime = buyInfo.getProductList().get(0).getVisitTime();
    		}
    		if(StringUtils.isEmpty(visitTime) && CollectionUtils.isNotEmpty(buyInfo.getItemList())){
				visitTime = buyInfo.getItemList().get(0).getVisitTime();
			}
    	}  	
    	LOG.info(" visitTime is "+visitTime);
    	return visitTime;
    }
    
}
