/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.route;

import com.alibaba.fastjson.JSONObject;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageDetailAddPrice;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;
import com.lvmama.vst.back.prod.po.ProdPackageGroupTransport;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Product;
import com.lvmama.vst.order.cache.OrderContextCache;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自主打包的线路产品
 * @author lancey
 *
 */
@Component("lvmamaRouteOrderPackInitBussiness")
public class LvmamaRouteOrderPackInitBussiness extends RouteOrderPackInitBussiness{
	
	private static final Log LOG = LogFactory.getLog(LvmamaRouteOrderPackInitBussiness.class);
	
//	@Autowired
//	private SuppGoodsClientService suppGoodsClientService;

	@Override
	protected boolean checkProductValid(ProdProduct product,
			Product itemProduct, OrdOrderPackDTO pack) {
		if(CollectionUtils.isEmpty(itemProduct.getItemList())){
			throwNullException("打包商品下单数据不完整");
		}

		Map<Long,BuyInfo.Item> itemMap = new HashMap<Long, BuyInfo.Item>();
		Map<Long,BuyInfo.Item> suppGoodsMap = new HashMap<Long, BuyInfo.Item>();
		for(BuyInfo.Item item:itemProduct.getItemList()){
			//针对自动打包交通产品的子订单，放过检查
			//是否是自动打包交通的产品
			boolean isAutoPackTrafficProduct = StringUtils.equalsIgnoreCase(Constants.Y_FLAG, product.getProp(autoPackTrafficProductKey));
			//是否是交通商品
			boolean isTrafficGoods = suppGoodsClientService.isTrafficGoods(item.getGoodsId());
			LOG.info("product is " + product.getProductId() + ", goods is " + item.getGoodsId() + ", isAutoPackTrafficProduct=" + isAutoPackTrafficProduct + ", isTrafficGoods=" + isTrafficGoods);
			if (isAutoPackTrafficProduct && isTrafficGoods) {
				continue;
			}
			if(BuyInfo.ItemRelation.PACK.equals(item.getRouteRelation())){
				if(item.getDetailId()==null){
					throwNullException("打包产品信息为空");
				}
				itemMap.put(item.getDetailId(), item);
			}else if(BuyInfo.ItemRelation.ADDITION.equals(item.getRouteRelation())){
				suppGoodsMap.put(item.getGoodsId(), item);
			}else{
				throwIllegalException("打包商品参数信息不完整");
			}
		}
		checkPackDetail(pack, product, itemMap);
		
//		checkAdditionRelation(product, itemMap);
		return true;
	}

	
	private boolean checkPackDetail(OrdOrderPackDTO pack,ProdProduct product,Map<Long,BuyInfo.Item> itemMap){
		LOG.info("处理打包产品，产品ID = " + product.getProductId() + ", 打包商品数量 = " + itemMap.size());
		List<ProdPackageGroup> packageGroupList = prodPackageGroupClientService.getProdPackageGroupByProductId(product.getProductId());

		if(packageGroupList.isEmpty()){
			throwNullException("自主打包的产品,打包参数不存在");
		}
		LOG.info("产品[" + product.getProductId() + "]所含打包组数量=" + packageGroupList.size());
		disposeItemMap(pack, itemMap, packageGroupList);
		checkTransportNum(pack, itemMap, product);
		return false;
	}


	private ResultHandleT<List<ProdPackageGroup>> getProdPackageGroupList(Long productId){

		ResultHandleT<List<ProdPackageGroup>> resultHandle = null;
		Object resultHandleOjb = OrderContextCache.get(OrderContextCache.KeyEnum.ProdPackageGroupList.getName()+"_"+productId);
		if(resultHandleOjb != null) {
			resultHandle  = (ResultHandleT<List<ProdPackageGroup>>)resultHandleOjb;
			LOG.info("getProdPackageGroupList from cache:"+ JSONObject.toJSONString(resultHandle));
		}else {
			Long start = System.currentTimeMillis();
			resultHandle = prodPackageGroupClientService.getProdPackageGroupList(productId,ProdPackageGroup.GROUPTYPE.TRANSPORT.getCode());
			LOG.info("productId="+productId+"调用getProdPackageGroupList耗时"+(System.currentTimeMillis() - start));
			OrderContextCache.set(OrderContextCache.KeyEnum.ProdPackageGroupList.getName()+"_"+productId,resultHandle);
		}

		return resultHandle;
	}

	/**
	 * 对打包中的大交通进行数量检查
	 * @param pack
	 * @param itemMap
	 * @param packageGroupList
	 */
	private void checkTransportNum(OrdOrderPackDTO pack,  Map<Long, BuyInfo.Item> itemMap, ProdProduct product){
//		if(CollectionUtils.isEmpty(packageGroupList)){
//			return;
//		}
//		List<ProdPackageGroup> sportPackageGroupList=new ArrayList<ProdPackageGroup>();
		LOG.info("检查产品[" + product.getProductId() + "]中大交通数量 开始");
		//线路下单优化
		ResultHandleT<List<ProdPackageGroup>> resultHandle = getProdPackageGroupList(product.getProductId());
		//ResultHandleT<List<ProdPackageGroup>> resultHandle=prodPackageGroupClientService.getProdPackageGroupList(product.getProductId(),ProdPackageGroup.GROUPTYPE.TRANSPORT.getCode());
		if(resultHandle==null){
			throwIllegalException("检查产品[" + product.getProductId() + "]中大交通数量出错 resultHandle is NULL");
		}else if(resultHandle.isFail()){
			LOG.error("检查产品[" + product.getProductId() + "]中大交通数量出错："+resultHandle.getMsg());
			throwIllegalException("检查产品[" + product.getProductId() + "]中大交通数量出错");
		}else if(resultHandle.hasNull()){
			LOG.info("产品[" + product.getProductId() + "]中没有打包大交通");
			return;
		}

		//打包组中单程数量
		int groupToNum=0;
		//打包组中的往返数量
		int groupToBackNum=0;
		//buyinfo中已匹配的单程子单数量
		int buyinfoToNum=0;
		//buyinfo中已匹配的往返子单数量
		int buyinfoToBackNum=0;


		List<ProdPackageGroup> sportPackageGroupList=resultHandle.getReturnContent();
		for (ProdPackageGroup prodPackageGroup : sportPackageGroupList) {
			ProdPackageGroupTransport sportPackageGroup=prodPackageGroup.getProdPackageGroupTransport();
			if(sportPackageGroup==null){
				LOG.warn("产品[" + product.getProductId() + "]中大交通组["+prodPackageGroup.getGroupId()+"]没有ProdPackageGroupTransport");
				continue;
			}
			boolean exis=hasTransportItem(prodPackageGroup,itemMap);
			if(StringUtils.equalsIgnoreCase(sportPackageGroup.getTransportType(),ProdPackageGroupTransport.TRANSPORTTYPE.TO.getCode())){
				groupToNum++;
				if(exis){
					buyinfoToNum++;
				}
			}else{
				groupToBackNum++;
				if(exis){
					buyinfoToBackNum++;
				}
			}

		}
		LOG.info("检查产品[" + product.getProductId() + "]中大交通数量 groupToNum："+groupToNum+" groupToBackNum:"+groupToBackNum+" buyinfoToNum:"+buyinfoToNum+" buyinfoToBackNum:"+buyinfoToBackNum);
		if(buyinfoToBackNum==1 && buyinfoToNum!=0){
			//除了往返还下单了单程
			throwIllegalException("检查产品[" + product.getProductId() + "]中大交通数量不正确");
		}else if(buyinfoToBackNum==0 && buyinfoToNum!=groupToNum){
			//没有打包往返，同时单程数量与打包关系中的数量不符
			throwIllegalException("检查产品[" + product.getProductId() + "]中大交通数量不正确");
		}else if(buyinfoToBackNum==0 && buyinfoToNum==0 && groupToBackNum!=0){
			//没买任何商品，但是大交通里有往返打包
			throwIllegalException("检查产品[" + product.getProductId() + "]中大交通数量不正确");
		}
		LOG.info("检查产品[" + product.getProductId() + "]中大交通数量 完成");
	}


	private boolean hasTransportItem(ProdPackageGroup prodPackageGroup,Map<Long, BuyInfo.Item> itemMap){
		if(prodPackageGroup==null || itemMap==null ||prodPackageGroup.getProdPackageDetails()==null){
			return false;
		}
		List<ProdPackageDetail> detailList=prodPackageGroup.getProdPackageDetails();
		for (ProdPackageDetail prodPackageDetail : detailList) {
			Long detailId=prodPackageDetail.getDetailId();
			for(BuyInfo.Item item : itemMap.values()){
				if(item.getDetailId().longValue()==detailId.longValue()){
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * 处理Item
	 * @param pack
	 * @param itemMap
	 * @param packageGroupList
	 */
	private void disposeItemMap(OrdOrderPackDTO pack,
			Map<Long, BuyInfo.Item> itemMap,
			List<ProdPackageGroup> packageGroupList) {
		LOG.info("处理打包商品start, 数量 = " + itemMap.size());
		Map<Long, Long> groupDetailIdMap = new HashMap<Long, Long>();
		Map<Long, ProdPackageGroup> groupMap = new HashMap<Long, ProdPackageGroup>();
		List<ProdPackageDetail> detailList = null;
		for(ProdPackageGroup ppg : packageGroupList) {
			detailList = prodPackageGroupClientService.getProdPackageDetailByGroupId(ppg.getGroupId());
			groupMap.put(ppg.getGroupId(), ppg);

			for(ProdPackageDetail ppd : detailList){
				groupDetailIdMap.put(ppd.getDetailId(), ppg.getGroupId());
				
				if(ppg.getCategoryId().longValue() == 18L || ppg.getCategoryId().longValue() == 15L){
					LOG.info("打包组为线路[" + ppg.getGroupName() + "], 组ID = " + ppg.getGroupId());
					getProdChangedGroup(groupDetailIdMap, groupMap, ppd);
				}
			}
		}
		
		for(BuyInfo.Item item : itemMap.values()){
			Long groupId = groupDetailIdMap.get(item.getDetailId());
			ProdPackageGroup ppg = groupMap.get(groupId);
			LOG.info("打包商品ID = " + item.getGoodsId() + ", 打包详细ID = " + item.getDetailId()+",打包组GroupID = "+groupId);
			if(groupId==null || ppg==null){
				throwIllegalException("商品不在打包关系里，不可以下单");
			}
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("detailId", item.getDetailId());
			params.put("specDate", item.getVisitTimeDate());
			params.put("groupId", groupId);
			if("HOTEL".equals(ppg.getGroupType()) && item.getHotelAdditation() != null){
				LOG.info("被打包产品含有酒店，入住日期 = " + item.getVisitTimeDate() + ", 离店日期 = " + item.getHotelAdditation().getLeaveTimeDate());

				//酒店特殊加价规则，打包到商品需要增加商品ID属性
				params.put("suppGoodsId",item.getGoodsId());

				Date startDate = item.getVisitTimeDate();
				Date endDate = item.getHotelAdditation().getLeaveTimeDate();
				List<Date> dateList = CalendarUtils.getDatesExtension(startDate, true, endDate, false);
				for(Date date : dateList) {
					params.put("specDate", date);
					fillDetail(pack, ppg, params);
				}
			} else {
				fillDetail(pack, ppg, params);
			}
		}
		LOG.info("处理打包商品end, 数量 = " + itemMap.size());
		LOG.info("分段加价列表数量 = " + pack.getPackageDetailAddPriceList().size() + ", 普通加价列表数量 = " + pack.getPackageDetailList().size());
	}


	private void getProdChangedGroup(Map<Long, Long> groupDetailIdMap,
			Map<Long, ProdPackageGroup> groupMap, ProdPackageDetail ppd) {
		LOG.info("跟团游或自由行是否存在可换酒店start...");
		try {
			if(ProdPackageDetail.OBJECT_TYPE_DESC.PROD_BRANCH.name().equalsIgnoreCase(ppd.getObjectType())){
				
				ResultHandleT<ProdProductBranch> byProductBranchHdl = prodProductBranchClientRemote.findProdProductBranchById(ppd.getObjectId());
				if(byProductBranchHdl !=  null && byProductBranchHdl.getReturnContent() != null) {
					ProdProductBranch byProductBranch = byProductBranchHdl.getReturnContent();
					
					Map<String, Object> changedHotelMap = new HashMap<String, Object>();
					changedHotelMap.put("productId", byProductBranch.getProductId());
					LOG.info("打包详细ID = " + ppd.getDetailId() + ", 对象类型 = " + ppd.getObjectType() + "对象ID = " + ppd.getObjectId());
					LOG.info("打包详细的规格名  = " + byProductBranch.getBranchName());
					LOG.info("规格关联的产品ID = " + byProductBranch.getProductId());
					ResultHandleT<List<ProdPackageGroup>> changeHotelGroupListHdl = prodPackageGroupClientService.findPackGroupListForProdCal(changedHotelMap);
					
					if(changeHotelGroupListHdl == null || changeHotelGroupListHdl.getReturnContent() == null){
						LOG.info("规格关联的产品ID = " + byProductBranch.getProductId()+"没有可换组");
						return;
					}
					
					List<ProdPackageGroup> changeHotelGroupList = changeHotelGroupListHdl.getReturnContent();
					LOG.info("规格关联的产品ID = " + byProductBranch.getProductId()+"的可换组有"+changeHotelGroupList.size()+"个");
					if(CollectionUtils.isEmpty(changeHotelGroupList)){
						LOG.info("规格关联的产品ID = " + byProductBranch.getProductId()+"没有可换组");
						return;
					}
					
					for(ProdPackageGroup byProdGroup : changeHotelGroupList){
						//增加了对grouptype为update的计算价格
						if(byProdGroup == null || (!ProdPackageGroup.GROUPTYPE.CHANGE.name().equalsIgnoreCase(byProdGroup.getGroupType()) 
								&& !ProdPackageGroup.GROUPTYPE.UPDATE.name().equalsIgnoreCase(byProdGroup.getGroupType())) ){
							if(byProdGroup != null){
								LOG.info("不是update或者change的组会去掉 ，组id GroupId："+byProdGroup.getGroupId()+"组类型GroupType:"+byProdGroup.getGroupType());
							}
							continue;
						}
						groupMap.put(byProdGroup.getGroupId(), byProdGroup);
						LOG.info("规格关联的产品ID = " + byProductBranch.getProductId()+"加入groupMap的GroupId = "+byProdGroup.getGroupId());
						
						List<ProdPackageDetail> packageDetailList = byProdGroup.getProdPackageDetails();
						if(packageDetailList != null){
							LOG.info("GroupId = " + byProdGroup.getGroupId()+"的detail有"+packageDetailList.size()+"个");
						}
						if(CollectionUtils.isEmpty(packageDetailList)){
							LOG.info("GroupId = " + byProdGroup.getGroupId()+"没有detail信息");
							continue;
						}
						LOG.info("产品[" + byProductBranch.getProductId() + "]存在可换酒店组， 组ID = " + byProdGroup.getGroupId());
						for(ProdPackageDetail packDetail:packageDetailList){
							if(packDetail != null){
								groupDetailIdMap.put(packDetail.getDetailId(), byProdGroup.getGroupId());
								LOG.info("规格关联的产品ID = " + byProductBranch.getProductId()+"加入groupDetailIdMap的DetailId = "+packDetail.getDetailId()
										+"GroupId="+byProdGroup.getGroupId());
							}
						}						
					}
				}
			}
			
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		LOG.info("跟团游或自由行是否存在可换酒店end...");
	}

	//填充
	private void fillDetail(OrdOrderPackDTO pack, ProdPackageGroup ppg, Map<String, Object> params) {
		LOG.info("处理分段加价、普通加价start，打包组ID = " + ppg.getGroupId());
		if(params.containsKey("suppGoodsId")){
			LOG.info("开始查找酒店打包组打包到商品逻辑，打包组ID = "+ppg.getGroupId());
			Map<String, Object> tempParams = new HashedMap();
			//分段加价
			tempParams.put("groupId",params.get("groupId"));
			tempParams.put("objectId",params.get("suppGoodsId"));
			tempParams.put("specDate",params.get("specDate"));
			tempParams.put("detailId",params.get("detailId"));
			tempParams.put("objectType","SUPP_GOODS");
			List<ProdPackageDetailAddPrice> queryDetailAddPriceList = prodPackageGroupClientService.getProdPackageDetailAddPriceByParams(tempParams);
			if (CollectionUtils.isNotEmpty(queryDetailAddPriceList)) {
				LOG.info("已找到酒店打包组打包到商品，打包组ID = "+ppg.getGroupId()+" suppGoodsId = "+params.get("suppGoodsId"));
				pack.getPackageDetailAddPriceList().addAll(queryDetailAddPriceList);
				for (ProdPackageDetailAddPrice detailAddPrice : queryDetailAddPriceList) {
					diposeDetail(pack, ppg, detailAddPrice.getObjectId(), detailAddPrice.getPriceType(), detailAddPrice.getPrice(),detailAddPrice.getObjectType());
				}
			}else{
				//分段加价,规格查询
				tempParams = new HashedMap();
				tempParams.put("groupId",params.get("groupId"));
				tempParams.put("specDate",params.get("specDate"));
				tempParams.put("detailId",params.get("detailId"));
				tempParams.put("objectType","PROD_BRANCH");
				queryDetailAddPriceList = prodPackageGroupClientService.getProdPackageDetailAddPriceByParams(tempParams);
				if (CollectionUtils.isNotEmpty(queryDetailAddPriceList)) {
					LOG.info("已找到酒店打包组打包到规格，打包组ID = "+ppg.getGroupId()+" suppGoodsId = "+params.get("suppGoodsId"));
					pack.getPackageDetailAddPriceList().addAll(queryDetailAddPriceList);
					for (ProdPackageDetailAddPrice detailAddPrice : queryDetailAddPriceList) {
						diposeDetail(pack, ppg, detailAddPrice.getObjectId(), detailAddPrice.getPriceType(), detailAddPrice.getPrice());
					}
				} else {
					//普通加价
					tempParams = new HashedMap();
					tempParams.put("groupId",params.get("groupId"));
					tempParams.put("specDate",params.get("specDate"));
					tempParams.put("detailId",params.get("detailId"));
					LOG.info("普通加价，打包组ID = "+ppg.getGroupId()+" suppGoodsId = "+params.get("suppGoodsId"));
					List<ProdPackageDetail> queryDetailList = prodPackageGroupClientService.findProdPackageDetailByParams(tempParams);
					if (CollectionUtils.isNotEmpty(queryDetailList)) {
						pack.getPackageDetailList().addAll(queryDetailList);
						for (ProdPackageDetail detail : queryDetailList) {
							diposeDetail(pack, ppg, detail.getObjectId(), detail.getPriceType(), detail.getPrice());
						}
					}
				}
			}
		}else{
			//分段加价
			List<ProdPackageDetailAddPrice> queryDetailAddPriceList = prodPackageGroupClientService.getProdPackageDetailAddPriceByParams(params);
			if (CollectionUtils.isNotEmpty(queryDetailAddPriceList)) {
				pack.getPackageDetailAddPriceList().addAll(queryDetailAddPriceList);
				for (ProdPackageDetailAddPrice detailAddPrice : queryDetailAddPriceList) {
					diposeDetail(pack, ppg, detailAddPrice.getObjectId(), detailAddPrice.getPriceType(), detailAddPrice.getPrice());
				}
			} else {
				//普通加价
				List<ProdPackageDetail> queryDetailList = prodPackageGroupClientService.findProdPackageDetailByParams(params);
				if (CollectionUtils.isNotEmpty(queryDetailList)) {
					pack.getPackageDetailList().addAll(queryDetailList);
					for (ProdPackageDetail detail : queryDetailList) {
						diposeDetail(pack, ppg, detail.getObjectId(), detail.getPriceType(), detail.getPrice());
					}
				}
			}
		}
		LOG.info("处理分段加价、普通加价end，打包组ID = " + ppg.getGroupId());
		
	}

	/**
	 * 处理分段加价、普通加价
	 * 为线路（跟团游、自由行）时，设置打包商品的为分段/普通加价
	 * @param pack
	 * @param ppg
	 * @param objectId
	 * @param addPriceType
	 * @param addPrice
	 * @param objectType
	 */
	private void diposeDetail(OrdOrderPackDTO pack, ProdPackageGroup ppg, Long objectId, String addPriceType, Long addPrice,String objectType){
		if(objectType==null || "PROD_BRANCH".equalsIgnoreCase(objectType)){
			diposeDetail(pack,ppg,objectId,addPriceType,addPrice);
		}else{
			//objectType==SUPP_GOODS
			diposeDetailBySuppGoods(pack,ppg,objectId,addPriceType,addPrice);
		}
	}

	/**
	 * 目前仅酒店组进入
	 * 处理分段加价、普通加价
	 * 为线路（跟团游、自由行）时，设置打包商品的为分段/普通加价
	 * @param pack
	 * @param ppg
	 * @param objectId
	 * @param addPriceType
	 * @param addPrice
	 * @param objectType
	 */
	private void diposeDetailBySuppGoods(OrdOrderPackDTO pack, ProdPackageGroup ppg,
							  Long objectId, String addPriceType, Long addPrice) {
		if(ppg != null && "HOTEL".equalsIgnoreCase(ppg.getGroupType())
				&& ppg.getCategoryId().longValue() == 1L){
			LOG.info("处理产品[" + ppg.getProductId() + "]，打包组ID["+ppg.getGroupId()+"]，打包组名称["+ppg.getGroupName()+"], 设置商品价格类型及加价start...");
			LOG.info("商品ID = " + objectId);
			Long suppGoodsId = objectId;
			ResultHandleT<ProdProductBranch> productBranchHandler;
			try {
				ResultHandleT<SuppGoods> suppGoodsHandler = suppGoodsClientService.findSuppGoodsById(suppGoodsId);
				if(suppGoodsHandler == null || suppGoodsHandler.getReturnContent() == null){
					return;
				}
				//被打包的产品
				ProdProduct changedUpdateHotelProduct = new ProdProduct();
				//被打包的线路产品Id
				Long byPackProductId = suppGoodsHandler.getReturnContent().getProductId();
				LOG.info("商品ID = "+ objectId +" 产品ID = " + byPackProductId);
				ProdProductParam linePraram = new ProdProductParam();
				ResultHandleT<ProdProduct> packChangedHotelProductHandler = prodProductClientRemote.findLineProductByProductId(byPackProductId, linePraram);
				changedUpdateHotelProduct = packChangedHotelProductHandler.getReturnContent();
				if(changedUpdateHotelProduct != null && changedUpdateHotelProduct.getProductId() != null){
					List<ProdPackageGroup> packageGroupList = prodPackageGroupClientService.getProdPackageGroupByProductId(changedUpdateHotelProduct.getProductId());
					if(packageGroupList != null && !packageGroupList.isEmpty()){
						LOG.info("被打包的产品ID = " + changedUpdateHotelProduct.getProductId() + "打包组数量 = " + packageGroupList.size());
						for(ProdPackageGroup ppg2 : packageGroupList){
							LOG.info("产品ID["+changedUpdateHotelProduct.getProductId()+"], 组ID["+ppg2.getGroupId()+"]");
							List<ProdPackageDetail> detailList2 = prodPackageGroupClientService.getProdPackageDetailByGroupId(ppg2.getGroupId());
							for(ProdPackageDetail ppd : detailList2){
								LOG.info("为打包详细["+ppd.getDetailId()+"]设置价格类型为["+addPriceType+"], 加价为["+addPrice+"]");
								ppd.setPriceType(addPriceType);
								ppd.setPrice(addPrice);
							}
							pack.getPackageDetailList().addAll(detailList2);
						}
					}
				}
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
			}
			LOG.info("处理产品[" + ppg.getProductId() + "]，打包组ID["+ppg.getGroupId()+"]，打包组名称["+ppg.getGroupName()+"], 设置商品价格类型及加价end...");
		}

	}


	/**
	 * 处理分段加价、普通加价
	 * 为线路（跟团游、自由行）时，设置打包商品的为分段/普通加价
	 * @param pack
	 * @param ppg
	 * @param objectId
	 * @param addPriceType
	 * @param addPrice
	 */
	//
	private void diposeDetail(OrdOrderPackDTO pack, ProdPackageGroup ppg,
			Long objectId, String addPriceType, Long addPrice) {
		if(ppg != null && "LINE".equalsIgnoreCase(ppg.getGroupType()) 
				&& (ppg.getCategoryId().longValue() == 18L || ppg.getCategoryId().longValue() == 15L)){
			LOG.info("处理产品[" + ppg.getProductId() + "]，打包组ID["+ppg.getGroupId()+"]，打包组名称["+ppg.getGroupName()+"], 设置商品价格类型及加价start...");
			LOG.info("对象ID = " + objectId);
			Long lineProductBranchId = objectId;
			ResultHandleT<ProdProductBranch> productBranchHandler;
			try {
				productBranchHandler = prodProductBranchClientRemote.findProdProductBranchById(lineProductBranchId);
				if(productBranchHandler == null || productBranchHandler.getReturnContent() == null){
					return;
				}
				ProdProductBranch productBranch = productBranchHandler.getReturnContent();
				//被打包的产品
				ProdProduct changedUpdateHotelProduct = new ProdProduct();
				//被打包的线路产品Id
				Long byPackProductId = productBranch.getProductId();
				ProdProductParam linePraram = new ProdProductParam();
				ResultHandleT<ProdProduct> packChangedHotelProductHandler = prodProductClientRemote.findLineProductByProductId(byPackProductId, linePraram);
				changedUpdateHotelProduct = packChangedHotelProductHandler.getReturnContent();	
				if(changedUpdateHotelProduct != null && changedUpdateHotelProduct.getProductId() != null){
					List<ProdPackageGroup> packageGroupList = prodPackageGroupClientService.getProdPackageGroupByProductId(changedUpdateHotelProduct.getProductId());
					if(packageGroupList != null && !packageGroupList.isEmpty()){
						LOG.info("被打包的产品ID = " + changedUpdateHotelProduct.getProductId() + "打包组数量 = " + packageGroupList.size());
						for(ProdPackageGroup ppg2 : packageGroupList){
							LOG.info("产品ID["+changedUpdateHotelProduct.getProductId()+"], 组ID["+ppg2.getGroupId()+"]");
							List<ProdPackageDetail> detailList2 = prodPackageGroupClientService.getProdPackageDetailByGroupId(ppg2.getGroupId());
							for(ProdPackageDetail ppd : detailList2){
								LOG.info("为打包详细["+ppd.getDetailId()+"]设置价格类型为["+addPriceType+"], 加价为["+addPrice+"]");
								ppd.setPriceType(addPriceType);
								ppd.setPrice(addPrice);
							}
							pack.getPackageDetailList().addAll(detailList2);
						}
					}
				}
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
			}
			LOG.info("处理产品[" + ppg.getProductId() + "]，打包组ID["+ppg.getGroupId()+"]，打包组名称["+ppg.getGroupName()+"], 设置商品价格类型及加价end...");
		}
		
	}

//	private boolean checkAdditionRelation(ProdProduct product,Map<Long,BuyInfo.Item> itemMap){
//		List<SuppGoodsRelation> list = suppGoodsClientService.findSuppGoodsRelationBySuppGoodsId(suppGoodsId);
//		return true;
//	}
	
	
}
