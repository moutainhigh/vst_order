/**
 * 
 */
package com.lvmama.vst.order.timeprice.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.lvmama.comm.stamp.vo.PresaleEnum.PRICE_CLASSIFICATION_CODE;
import com.lvmama.dest.api.order.vo.HotelOrderUpdateStockDTO;
import com.lvmama.dest.api.utils.DynamicRouterUtils;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.control.vo.ResPreControlTimePriceVO;
import com.lvmama.vst.back.goods.po.PresaleStampTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsGroupStock;
import com.lvmama.vst.back.goods.po.SuppGoodsLineTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.order.dao.OrdOrderGroupStockDao;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderGroupStock;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderSharedStock;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageDetailAddPrice;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.back.prom.rule.favor.FavorableAmount;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.order.DestBuCheckOrderUtil;
import com.lvmama.vst.comm.utils.order.DestBuOrderPropUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdOrderSharedStockDao;
import com.lvmama.vst.order.dao.goods.SuppGoodsLineTimePriceOraDao;
import com.lvmama.vst.order.service.IHotelTradeApiService;
import com.lvmama.vst.order.timeprice.adapter.IHotelGoodsGroupQueryAdapterService;
import com.lvmama.vst.order.timeprice.service.AbstractOrderTimePriceService;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;

import net.sf.json.JSONArray;

/**
 * 线路时间价格表
 * @author lancey
 *
 */

public class AbstractOrderLineTimePriceServiceImpl extends AbstractOrderTimePriceService
	implements OrderTimePriceService {
	
	@Resource(name="goodsOraLineTimePriceStockService")
	private IGoodsTimePriceStockService goodsLineTimePriceStockService;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private  OrdOrderGroupStockDao ordOrderGroupStockDao;
	
	@Autowired
	protected ResPreControlService resControlBudgetRemote;
	
    @Autowired
    private OrdOrderSharedStockDao ordOrderSharedStockDao;
	
    @Autowired
    private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;
    
    @Autowired
    private SuppGoodsClientService suppGoodsClientRemote;
    
	@Autowired
	private IHotelTradeApiService hotelTradeApiService;
	
    @Autowired
    private SuppGoodsLineTimePriceOraDao suppGoodsLineTimePriceDao;
    
    @Autowired
    private IHotelGoodsGroupQueryAdapterService hotelGoodsGroupQueryAdapterService;
    

	public static final Log log = LogFactory.getLog(AbstractOrderLineTimePriceServiceImpl.class);

	@Override
	public ResultHandleT<Object> checkStock(SuppGoods suppGoods, Item item,
			Long distributionId, Map<String, Object> dataMap) {
		ResultHandleT<Object> handle = new ResultHandleT<Object>();
		SuppGoodsBaseTimePrice timePrice = getTimePriceAndCheck(suppGoods,distributionId, item, item.getVisitTimeDate());
		LOG.info("AbstractOrderLineTimePriceServiceImpl产品正在进行库存检查，所检查出的时间价格表数据是\n"+JSONArray.fromObject(timePrice));
		SuppGoodsLineTimePrice lineTimePrice = (SuppGoodsLineTimePrice)timePrice;

        //判断酒店套餐
        long stockQuantity = getNeedStock(item);
        //List<OrdOrderSharedStock> ordOrderSharedStockList = new ArrayList<OrdOrderSharedStock>();
        //item.setSharedStockList(ordOrderSharedStockList);

        LOG.info("#######LAST产品品类LAST########="+JSONObject.toJSONString(item));
        if(null != item.getProductCategoryId() && 17L == item.getProductCategoryId()){
            //共享库存判断
            LOG.info("#######LAST酒店套餐LAST########");
            Long groupId = suppGoods.getGroupId();
            Long shareStock = null;
            LOG.info("groupId="+groupId+"    SuppGoodsId"+suppGoods.getSuppGoodsId());
            Boolean flag = DynamicRouterUtils.getInstance().isHotelSystemOnlineEnabled();	
            if(null != groupId){
            
                LOG.info("查询共享库存：groupId="+groupId+"    visitTime="+item.getVisitTimeDate()+" flag="+flag);              
                if(flag){
                   shareStock = hotelTradeApiService.getHotelShareStock(groupId, item.getVisitTimeDate());
                }else{
                   shareStock = goodsLineTimePriceStockService.getShareStock(groupId,item.getVisitTimeDate());
                }             
                LOG.info("oneDate="+item.getVisitTimeDate()+"    groupId="+groupId+"     shareStock="+shareStock);
            }

            if(null!=shareStock){
                lineTimePrice.setStockType(SuppGoodsLineTimePrice.STOCKTYPE.CONTROL.name());
                if(shareStock < stockQuantity){
                    throwIllegalException(OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.name(),"共享库存不足");
                }else{
//                    OrdOrderSharedStock ordOrderSharedStock = new OrdOrderSharedStock();
//                    ordOrderSharedStock.setGroupId(groupId);
//                    ordOrderSharedStock.setVisitTime(item.getVisitTimeDate());
//                    ordOrderSharedStock.setQuantity((long)item.getQuantity());
//                    ordOrderSharedStockList.add(ordOrderSharedStock);
                }
                // （宋城）对接酒店套餐，保留房和非保留房都走如下逻辑，共享库存也进行供应商校验
                log.info("AbstractOrderLineTimePriceServiceImpl hotelcomb shareStock init supplierProductInfo suppGoods="+JSONObject.toJSONString(suppGoods));	
                if (suppGoods != null && suppGoods.getSupplierId() != null) {
                	if ("Y".equals(suppGoods.getApiFlag())) {
                		com.lvmama.vst.comm.vo.SupplierProductInfo.Item supplierItem = makeSupplierItem(item);
                		handle.setReturnContent(supplierItem);
                	}
                }                
            }else{
            	log.info("AbstractOrderLineTimePriceServiceImpl category=17 checkstock lineTimePrice="+JSONObject.toJSONString(lineTimePrice) + "stockQuantity=" + stockQuantity);
                if(SuppGoodsLineTimePrice.STOCKTYPE.CONTROL.name().equalsIgnoreCase(lineTimePrice.getStockType())
                        ||SuppGoodsLineTimePrice.STOCKTYPE.INQUIRE_WITH_STOCK.name().equalsIgnoreCase(lineTimePrice.getStockType())){

                    if(lineTimePrice.getStock()<stockQuantity){
                        if("N".equalsIgnoreCase(lineTimePrice.getOversellFlag())){
                            throwIllegalException(OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.name(),"库存不足");
                        }
                    }
                }else{
//                    OrdOrderSharedStock ordOrderSharedStock = new OrdOrderSharedStock();
//                    ordOrderSharedStock.setGroupId(null);
//                    ordOrderSharedStock.setVisitTime(item.getVisitTimeDate());
//                    ordOrderSharedStock.setQuantity((long)item.getQuantity());
//                    ordOrderSharedStockList.add(ordOrderSharedStock);
                }

                // （宋城）对接酒店套餐，保留房和非保留房都走如下逻辑
                log.info("AbstractOrderLineTimePriceServiceImpl hotelApi checkstock suppGoods="+JSONObject.toJSONString(suppGoods));	
                if (suppGoods != null && suppGoods.getSupplierId() != null) {
                	if ("Y".equals(suppGoods.getApiFlag())) {
                		com.lvmama.vst.comm.vo.SupplierProductInfo.Item supplierItem = makeSupplierItem(item);
                		handle.setReturnContent(supplierItem);
                	}
                }

            }
        }
        else if(null != item.getProductCategoryId() && 15L == item.getProductCategoryId()){//跟团游对接
        	log.info("AbstractOrderLineTimePriceServiceImpl checkstock suppGoods="+JSONObject.toJSONString(suppGoods));	
        	if (suppGoods != null && suppGoods.getSupplierId() != null) {
				if ("Y".equals(suppGoods.getStockApiFlag())) {
					com.lvmama.vst.comm.vo.SupplierProductInfo.Item supplierItem = makeSupplierItem(item);
					handle.setReturnContent(supplierItem);
				}
			}
        }
        else{
            if(SuppGoodsLineTimePrice.STOCKTYPE.CONTROL.name().equalsIgnoreCase(lineTimePrice.getStockType())
                    ||SuppGoodsLineTimePrice.STOCKTYPE.INQUIRE_WITH_STOCK.name().equalsIgnoreCase(lineTimePrice.getStockType())){

                if(lineTimePrice.getStock()<stockQuantity){
                    if("N".equalsIgnoreCase(lineTimePrice.getOversellFlag())){
                        throwIllegalException(OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.name(),"库存不足");
                    }
                }
            }
        }

		return handle;
	}

	private com.lvmama.vst.comm.vo.SupplierProductInfo.Item makeSupplierItem(Item buyInfoItem) {
		com.lvmama.vst.comm.vo.SupplierProductInfo.Item item = null;
		
		if (buyInfoItem != null) {
			item = new com.lvmama.vst.comm.vo.SupplierProductInfo.Item(null, buyInfoItem.getVisitTimeDate());
			item.setQuantity(new Long(buyInfoItem.getQuantity()));
			item.setSuppGoodsId(buyInfoItem.getGoodsId());
		}
		
		return item;
	}

	protected long getNeedStock(Item item) {
		long stockQuantity=0;
		for(BuyInfo.PriceType pt:item.getPriceTypeList()){
			if(!StringUtils.equalsIgnoreCase(pt.getPriceKey(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.name())){
				stockQuantity+=pt.getQuantity();
			}
		}
		return stockQuantity;
	}
	
	

	/**
	 * 线路检查以成人是否有值为准
	 */
	@Override
	protected void checkOnsaleFlag(SuppGoodsBaseTimePrice timePrice) {
		SuppGoodsLineTimePrice lineTimePrice = (SuppGoodsLineTimePrice)timePrice;
		if(lineTimePrice.getAuditPrice()==null||lineTimePrice.getAuditSettlementPrice()==null){
			throwIllegalException("商品游玩日期不可售");
		}
	}



	@Override
	public void updateStock(Long timePriceId, Long stock,
			Map<String, Object> dataMap) {
		Long orderItemId=null;
		LOG.info("method deductStock updateStock  from AbstractOrderLineTimePriceServiceImpl。。。。。");
		boolean hasShareStock = false;
		Long hotelGroupId = 0L;
		if(dataMap!=null&&!dataMap.isEmpty()){
			orderItemId=(Long)dataMap.get("orderItemId");
			Map<String, Object> shareMap = isShareHotelComb(dataMap, stock);
			hasShareStock = (Boolean) shareMap.get("hasShareStock");
			hotelGroupId = (Long) shareMap.get("hotelGroupId");
		}
		LOG.info("method deductStock updateStock  hasShareStock is:"+hasShareStock);
		List<OrdOrderGroupStock> ordOrderGroupStockList=new ArrayList<OrdOrderGroupStock>(1);
		LOG.info("timePriceId="+timePriceId+"    stock="+stock+"   orderItemId="+orderItemId);
		boolean deductSuccess = false;
		HotelOrderUpdateStockDTO hotelUpdateStock = null;
		if(hasShareStock){
			hotelUpdateStock = new HotelOrderUpdateStockDTO();
			hotelUpdateStock.setTimePriceId(timePriceId);
			hotelUpdateStock.setUpdateStock(stock);	
			hotelUpdateStock.setOrderItemId(orderItemId);
			hotelUpdateStock.setGroupId(hotelGroupId);
			SuppGoodsLineTimePrice suppGoodsLineTimePrice = suppGoodsLineTimePriceDao.selectByPrimaryKey(timePriceId);
			hotelUpdateStock.setSpecDate(suppGoodsLineTimePrice.getSpecDate());
			ResultHandleT<HotelOrderUpdateStockDTO> resultHandle = hotelTradeApiService.deductStock(hotelUpdateStock);
			deductSuccess = resultHandle.isSuccess();
		}else{		
			deductSuccess = goodsLineTimePriceStockService.updateGroupStock(timePriceId, -stock, orderItemId,ordOrderGroupStockList);
		}
		LOG.info("method deductStock updateStock  deductSuccess is:"+deductSuccess);
        if(!deductSuccess){
			throwIllegalException("库存扣除操作失败");
		}else{
			if(null != hotelUpdateStock){
				List<HotelOrderUpdateStockDTO> orderTradeUpdateStockList = new ArrayList<HotelOrderUpdateStockDTO>();
				orderTradeUpdateStockList.add(hotelUpdateStock);
				dataMap.put("orderTradeUpdateStockList", orderTradeUpdateStockList);
				LOG.info("method deductStock updateStock  orderTradeUpdateStockList is:"+GsonUtils.toJson(orderTradeUpdateStockList));
			}
            //如果扣减库存成功，查看是否是酒店套餐，如果是的话需要将记录插入ORD_ORDER_SHARED_STOCK表中（update by luoweiyi）
            if(null != dataMap && null != dataMap.get("orderItem")){
                OrdOrderItem orderItem = (OrdOrderItem)dataMap.get("orderItem");
                if(orderItem.getCategoryId() == 17L){
                    Long groupId = orderItem.getSuppGoods().getGroupId();
                    LOG.info("groupId=" + groupId + "    SuppGoodsId" + orderItem.getSuppGoods().getSuppGoodsId());
                    if (null != orderItem.getOrderStockList()) {
                        List<OrdOrderStock> ordOrderStockList = orderItem.getOrderStockList();
                        List<OrdOrderSharedStock> ordOrderSharedStockList = new ArrayList<OrdOrderSharedStock>();
                        for (OrdOrderStock oos : ordOrderStockList) {
                            Date visitTime = oos.getVisitTime();
                            OrdOrderSharedStock ooss = new OrdOrderSharedStock();
                            ooss.setVisitTime(visitTime);
                            ooss.setQuantity(oos.getQuantity());
                            ooss.setOrderItemId(orderItem.getOrderItemId());
							ooss.setInventory(oos.getInventory());
                            ooss.setResourceStatus(oos.getResourceStatus());
                            ooss.setNeedResourceConfirm(oos.getNeedResourceConfirm());
                            Long shareStock = null;
                            if(null != hotelUpdateStock){
                            	ooss.setGroupId(hotelGroupId);
                            }else if (null != groupId){
                                LOG.info("查询共享库存：groupId=" + groupId + "    visitTime=" + visitTime);
                                shareStock = goodsLineTimePriceStockService.getShareStock(groupId, visitTime);
                                LOG.info("oneDate=" + visitTime + "    groupId=" + groupId + "     shareStock=" + shareStock);
                                if (null == shareStock) {
                                    ooss.setGroupId(null);
                                } else {
                                    ooss.setGroupId(groupId);
                                }
                            }

                            ordOrderSharedStockDao.insert(ooss);
                        }
                    }
                }
            }
        }
	}
	
	@Override
	public ResultHandle validate(SuppGoods suppGoods, Item item,
			OrdOrderItemDTO orderItem, OrdOrderDTO order) {
		ResultHandle handle = new ResultHandle();
		SuppGoodsBaseTimePrice timePrice = getTimePriceAndCheck(suppGoods, item, orderItem.getVisitTime());
		
		//多个价格，转换
		SuppGoodsLineTimePrice lineTimePrice = (SuppGoodsLineTimePrice)timePrice;
//		makeOrderItemTime(orderItem, lineTimePrice);
		
		ProdPackageDetailAddPrice detailAddPrice = null;
		ProdPackageDetail detail = null;
		if(orderItem.getOrderPack()!=null){
			OrdOrderItemDTO orderItemDTO = (OrdOrderItemDTO)orderItem;
			if(orderItemDTO.getItem().getDetailId()!=null){
				detailAddPrice = OrderUtils.getProdPackageDetailAddPriceByDetailId((OrdOrderPackDTO)orderItem.getOrderPack(), orderItemDTO.getItem().getDetailId(), item.getVisitTimeDate());
				if(detailAddPrice == null) {
					detail = OrderUtils.getProdPackageDetailByDetailId((OrdOrderPackDTO)orderItem.getOrderPack(), orderItemDTO.getItem().getDetailId());
				}
			}
		}
		
		long stockQuantity = doCalcPriceInfo(suppGoods, item, orderItem, lineTimePrice, detailAddPrice, detail);
		handle =DestBuCheckOrderUtil.checkTimePriceForOrder(suppGoods, lineTimePrice);
		if(handle.isFail()){
			return handle;
		}
		
		List<OrdOrderStock> orderStockList = new ArrayList<OrdOrderStock>();
		Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("orderItem", orderItem);
        dataMap.put("beginDate", lineTimePrice.getSpecDate());
		if(SuppGoodsLineTimePrice.STOCKTYPE.CONTROL.name().equalsIgnoreCase(lineTimePrice.getStockType())
				||SuppGoodsLineTimePrice.STOCKTYPE.INQUIRE_WITH_STOCK.name().equalsIgnoreCase(lineTimePrice.getStockType())){
			
            Long sharedStock = null;
            if(null != suppGoods.getProdProduct() && 17L == suppGoods.getProdProduct().getBizCategoryId()){
                Long groupId = suppGoods.getGroupId();
                if(null != groupId){
                    LOG.info("groupId="+groupId+"    SuppGoodsId"+suppGoods.getSuppGoodsId());
                    if(null != groupId){
//                        sharedStock = goodsLineTimePriceStockService.getShareStock(groupId,item.getVisitTimeDate());                       
                        LOG.info("validate查询共享库存isShareHotelComb：groupId="+groupId+"    SpecDate="+lineTimePrice.getSpecDate());
                        Map<String, Object> shareMap = isShareHotelComb(dataMap, stockQuantity);
                       if(null != shareMap.get("shareStocks")){
                    	   sharedStock = (Long) shareMap.get("shareStocks");
                       } 
                        LOG.info("oneDate="+item.getVisitTimeDate()+"    groupId="+groupId+"     shareStock="+sharedStock);
                    }
                }
            }
            if(null != sharedStock){
                lineTimePrice.setStock(sharedStock);
                lineTimePrice.setOversellFlag("N");	//共享库存不可超卖
            }


            if(lineTimePrice.getStock()<stockQuantity){
				if("N".equalsIgnoreCase(lineTimePrice.getOversellFlag())){
				    throwIllegalException(OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.name(), "库存不足");
				}else{//支持超卖
					OrdOrderStock stock = createStock(orderItem.getVisitTime(), lineTimePrice.getStock());

                    if(SuppGoodsLineTimePrice.STOCKTYPE.CONTROL.name().equalsIgnoreCase(lineTimePrice.getStockType())){
						makeNotNeedResourceConfirm(stock);
					}else{
						makeNeedResourceConfirm(stock);
					}
					makeInventoryFlag(stock);
					orderStockList.add(stock);
					order.addUpdateStock(lineTimePrice, stock.getQuantity(), this);
					
					stock = createStock(orderItem.getVisitTime(), stockQuantity-lineTimePrice.getStock());
					makeNeedResourceConfirm(stock);
//					makeInventoryFlag(stock);
					orderStockList.add(stock);
				}
			}else{
                OrdOrderStock stock = createStock(orderItem.getVisitTime(), orderItem.getQuantity());
                if(null != sharedStock){
                    makeNotNeedResourceConfirm(stock);
                }else{
                    if(SuppGoodsLineTimePrice.STOCKTYPE.CONTROL.name().equalsIgnoreCase(lineTimePrice.getStockType())){
                        makeNotNeedResourceConfirm(stock);
                    }else{
                        makeNeedResourceConfirm(stock);
                    }
                }
                //董宁波 2016年5月18日 13:57:45 现询已知 改为 无库存下单 start
                if (17L != suppGoods.getProdProduct().getBizCategoryId()) {
					makeInventoryFlag(stock);
				}
				//end
                orderStockList.add(stock);
                order.addUpdateStock(lineTimePrice, stock.getQuantity(), this);
			}
		}else{//库存现询
			OrdOrderStock stock = createStock(orderItem.getVisitTime(), orderItem.getQuantity());
            Long sharedStock = null;
            if(null != suppGoods.getProdProduct() && 17L == suppGoods.getProdProduct().getBizCategoryId()){
                Long groupId = suppGoods.getGroupId();
                if(null != groupId){
                    LOG.info("groupId="+groupId+"    SuppGoodsId"+suppGoods.getSuppGoodsId());
                    if(null != groupId){                    	
                        LOG.info("validate查询共享库存：groupId="+groupId+"    visitTime="+item.getVisitTimeDate());
//                        sharedStock = goodsLineTimePriceStockService.getShareStock(groupId,item.getVisitTimeDate());
                        Map<String, Object> shareMap = isShareHotelComb(dataMap, orderItem.getQuantity());
                        if(null != shareMap.get("shareStocks")){
                     	   sharedStock = (Long) shareMap.get("shareStocks");
                        } 
                        LOG.info("oneDate="+item.getVisitTimeDate()+"    groupId="+groupId+"     shareStock="+sharedStock);
                    }
                }
            }
            if(null != sharedStock) {
                order.addUpdateStock(lineTimePrice, stock.getQuantity(), this);
                makeNotNeedResourceConfirm(stock);
            }else{
                makeNeedResourceConfirm(stock);
            }
            orderStockList.add(stock);
		}
		
		makeNeedResourceConfirm(orderItem, orderStockList);
		orderItem.setOrderStockList(orderStockList);
		makeOrderItemTime(orderItem, lineTimePrice);
		setCancel(order,orderItem);
		return handle;
	}
	

	
	protected long doCalcPriceInfo(SuppGoods suppGoods, Item item, OrdOrderItem orderItem, 
			SuppGoodsLineTimePrice lineTimePrice, ProdPackageDetailAddPrice detailAddPrice, ProdPackageDetail detail) {
		List<OrdMulPriceRate> list = new ArrayList<OrdMulPriceRate>();
		long stockQuantity=0;
		long totalAmount = 0;
		long totalSettlement=0;
		long realSettlement=0;

		Long suppGoodsId = suppGoods.getSuppGoodsId();		
		Long precontrolSettlePrice = null;
		Long precontrolSalePrice = null;
		Long preSaleSettlePrice=null;
		Long precontrolGapPrice = null;
		List<ResPreControlTimePriceVO> resPriceList = null;
		/** 开始资源预控买断价格  **/
		SuppGoods goods = orderItem.getSuppGoods();
		Long goodsId = goods.getSuppGoodsId();
		Date visitDate = orderItem.getVisitTime();
		GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = new GoodsResPrecontrolPolicyVO();
		List<PresaleStampTimePrice> presales=new ArrayList<PresaleStampTimePrice>();
		boolean hasControled=false;
		//是否是券兑换商品
		boolean hasStamped=false;
		//如果是券兑换的商品就不走买断的时间价格表
		if(!OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(item.getOrderSubType())){
			//通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
			goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
			//如果能找到该有效预控的资源
		    hasControled = goodsResPrecontrolPolicyVO != null && goodsResPrecontrolPolicyVO.isControl();
		    LOG.info("vst_order===goodsResPrecontrolPolicyVO==="+ GsonUtils.toJson(goodsResPrecontrolPolicyVO));
		}
		//如果是券
		if(OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(item.getOrderSubType())){
			Map<String,Object> map =new HashMap<String, Object>();
			map.put("goodsId", goodsId);
			map.put("applyDate", visitDate);
			presales=suppGoodsTimePriceClientService.selectPresaleStampTimePrices(map);
			log.info("线路的预售结算价是"+presales.get(0).getValue());
			hasStamped=true;
		}
		//非买断结算总价
		long notBuyoutTotalPrice = 0;
		//买断结算总价
		long buyoutTotalPrice = 0;
		
		Long leftMoney = null;
		long hasSaledOut = 0;
		long buyoutNum = 0;
		if(hasControled ){
			// --ziyuanyukong  通过接口获取该商品在这个时间的价格【参数：成人数，儿童数，商品Id,游玩时间】
			resPriceList = resControlBudgetRemote.queryPreControlTimePriceByParam(orderItem.getVisitTime(),orderItem.getCategoryId(), orderItem.getSuppGoodsId());
			if(resPriceList==null || (resPriceList!=null && resPriceList.size()<=0)){
				hasControled = false;
			}else{
				LOG.info("***资源预控***");
				LOG.info("线路默认calc price：" + orderItem.getSuppGoodsId() + "存在预控资源");
			}
			if(hasControled && ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory.name().equals(goodsResPrecontrolPolicyVO.getControlType()) ){
				long leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum();
				if(orderItem.getQuantity()>leftQuantity&&"N".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())){
					orderItem.setBuyoutQuantity(leftQuantity);
				}else{
					orderItem.setBuyoutQuantity(orderItem.getQuantity());
				}
			}
		}
		//方差取原先的价格
		for(BuyInfo.PriceType pt:item.getPriceTypeList()){
			boolean isSpread = StringUtils.equalsIgnoreCase(pt.getPriceKey(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.name());
			if(!isSpread && resPriceList!=null && resPriceList.size()>0){
				precontrolSettlePrice = getPrecontrolSettlementPriceTypeValue(suppGoods,resPriceList,pt.getPriceKey());
			}else{
				precontrolSettlePrice = null;
			}
			if(!isSpread && presales!=null && presales.size()>0){
				preSaleSettlePrice = getPreSaleSettlementPriceTypeValue(suppGoods,presales,pt.getPriceKey());
			}else{
				preSaleSettlePrice = null;
			}
			long settlementPrice = getSettlementPriceTypeValue(suppGoods, lineTimePrice, pt.getPriceKey());
			long sourceSettlementPrice = settlementPrice;
			//不处理房差
			if(!isSpread&&hasControled && ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory.name().equals(goodsResPrecontrolPolicyVO.getControlType()) ){
				//按库存预控
				//如果两者都存在的情况，这里记录非买断的
				long overSaleNum  = 0;
				//同上 ： 记录买断的
				long buyoutNumTmp = 0;
				
				overSaleNum = hasSaledOut+pt.getQuantity()-orderItem.getBuyoutQuantity();
				if(overSaleNum>0){
					//非买断结算价
					if(hasSaledOut>=orderItem.getBuyoutQuantity()){
						overSaleNum = pt.getQuantity();
					}
					addMulPriceRate(list,converSettlement(pt.getPriceKey()),overSaleNum,sourceSettlementPrice,OrdMulPriceRate.AmountType.SETTLEMENT.name());
					notBuyoutTotalPrice = notBuyoutTotalPrice + sourceSettlementPrice*overSaleNum;
					buyoutNumTmp = pt.getQuantity() - overSaleNum;
					if(buyoutNumTmp>0){
						//买断结算价
						buyoutNum = buyoutNum + buyoutNumTmp;
						addMulPriceRate(list,converSettlement(pt.getPriceKey())+"_PRE",buyoutNumTmp,precontrolSettlePrice,OrdMulPriceRate.AmountType.SETTLEMENT.name());
						buyoutTotalPrice = buyoutTotalPrice + precontrolSettlePrice*buyoutNumTmp;
					}
				}else{
					//买断结算价
					buyoutNum = buyoutNum + pt.getQuantity();
					addMulPriceRate(list,converSettlement(pt.getPriceKey())+"_PRE",Long.valueOf(pt.getQuantity()),precontrolSettlePrice,OrdMulPriceRate.AmountType.SETTLEMENT.name());
					buyoutTotalPrice = buyoutTotalPrice + precontrolSettlePrice*pt.getQuantity();
				}
				
			}else if(!isSpread&&hasControled && ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(goodsResPrecontrolPolicyVO.getControlType())){
				//按金额预控
				//记录买断和非买断的结算总额
				if(leftMoney == null)
					leftMoney = goodsResPrecontrolPolicyVO.getLeftAmount().longValue();
				if("N".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())){
					if(leftMoney.longValue()>0||"Y".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())){
						buyoutNum = buyoutNum + pt.getQuantity();
					}
				}else if(leftMoney.longValue()>0||"Y".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())){
					if(leftMoney>0||"Y".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())){
						buyoutNum = buyoutNum + pt.getQuantity();
					}
				}
				long shouldSettleTotalPrice = pt.getQuantity()*precontrolSettlePrice;
				if(shouldSettleTotalPrice>leftMoney&&leftMoney>0&&"N".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())){
					//买断个数
					long tmp = (long) Math.ceil(leftMoney/precontrolSettlePrice.doubleValue());
					//买断
					buyoutTotalPrice = buyoutTotalPrice + tmp*precontrolSettlePrice;
					addMulPriceRate(list,converSettlement(pt.getPriceKey())+"_PRE",Long.valueOf(tmp),precontrolSettlePrice,OrdMulPriceRate.AmountType.SETTLEMENT.name());
					buyoutNum = buyoutNum - pt.getQuantity() + tmp;
					long notBuyNum = (pt.getQuantity() - tmp);
					if(notBuyNum>0){
						//非买断个数
						notBuyoutTotalPrice = notBuyoutTotalPrice + notBuyNum * sourceSettlementPrice;
						addMulPriceRate(list,converSettlement(pt.getPriceKey()),Long.valueOf(notBuyNum),sourceSettlementPrice,OrdMulPriceRate.AmountType.SETTLEMENT.name());
					}
					
					
				}else if(shouldSettleTotalPrice<=leftMoney||"Y".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())){
					//买断
					buyoutTotalPrice = buyoutTotalPrice + shouldSettleTotalPrice;
					addMulPriceRate(list,converSettlement(pt.getPriceKey())+"_PRE",Long.valueOf(pt.getQuantity()),precontrolSettlePrice,OrdMulPriceRate.AmountType.SETTLEMENT.name());
				}else{
					//只有非买断
					shouldSettleTotalPrice = (pt.getQuantity()* sourceSettlementPrice);
					notBuyoutTotalPrice = notBuyoutTotalPrice + shouldSettleTotalPrice;
					addMulPriceRate(list,converSettlement(pt.getPriceKey()),Long.valueOf(pt.getQuantity()),sourceSettlementPrice,OrdMulPriceRate.AmountType.SETTLEMENT.name());
				}
				leftMoney = leftMoney - shouldSettleTotalPrice;
				
				
				
			}

			//不处理房差
			if(hasControled == false && !isSpread){
				OrdMulPriceRate price = new OrdMulPriceRate();
				price.setPriceType(converSettlement(pt.getPriceKey()));
				price.setQuantity((long)pt.getQuantity());
				price.setPrice(settlementPrice);
				if(hasStamped){
					realSettlement=settlementPrice;
					price.setPrice(preSaleSettlePrice);
				}
				price.setAmountType(OrdMulPriceRate.AmountType.SETTLEMENT.name());
				list.add(price);
			}

			//是房差 ，那么暗原有价格去扣减结算价
			if(isSpread){
				addMulPriceRate(list,converSettlement(pt.getPriceKey()),Long.valueOf(pt.getQuantity()),sourceSettlementPrice,OrdMulPriceRate.AmountType.SETTLEMENT.name());
				notBuyoutTotalPrice = notBuyoutTotalPrice + sourceSettlementPrice*pt.getQuantity();
			}
			
			
			if(precontrolSettlePrice!=null){
				settlementPrice = precontrolSettlePrice.longValue();
			}
			if(preSaleSettlePrice!=null){
				settlementPrice = preSaleSettlePrice.longValue();
			}
			//--
			if(!isSpread &&resPriceList!=null && resPriceList.size()>0){
				precontrolSalePrice = getPrecontrolPriceTypeValue(suppGoods,resPriceList,pt.getPriceKey());
			}else{
				precontrolSalePrice = null;
			}
			
			long salePrice = getPriceTypeValue(suppGoods, lineTimePrice, pt.getPriceKey());
			if(precontrolSalePrice!=null)
				salePrice = precontrolSalePrice.longValue();
			
			//销售价走原来的价格，结算价才是预售结算价
			/**/
			
			/*if(hasStamped){
				salePrice=Long.valueOf(item.getPrice());
			}*/
			//销售价

			OrdMulPriceRate price = new OrdMulPriceRate();
			price.setPriceType(pt.getPriceKey());
			
			price.setQuantity((long)pt.getQuantity());
			price.setPrice(salePrice);
			//重新进行预售券的判断进行计价打包
			if(hasStamped){
				if(detailAddPrice != null && !isSpread && OrderUtils.hasRouteBranch(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchCode.name()))){//针对打包重新计算单价
					fillPackageOrderItemPrice(price, detailAddPrice, realSettlement);
				} else if(detail != null && !isSpread && OrderUtils.hasRouteBranch(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchCode.name()))){//针对打包重新计算单价
					fillPackageOrderItemPrice(price, detail, realSettlement);
				}
			}else{
				if(detailAddPrice != null && !isSpread && OrderUtils.hasRouteBranch(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchCode.name()))){//针对打包重新计算单价
					fillPackageOrderItemPrice(price, detailAddPrice, settlementPrice);
				} else if(detail != null && !isSpread && OrderUtils.hasRouteBranch(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchCode.name()))){//针对打包重新计算单价
					fillPackageOrderItemPrice(price, detail, settlementPrice);
				}
				
			}
			price.setAmountType(OrdMulPriceRate.AmountType.PRICE.name());
			list.add(price);

			log.info("suppGoodsId is " + suppGoodsId + " " + pt.getPriceKey() + " price.getPrice():" + price.getPrice() + " price.getQuantity(), before add, totalAmount is " + totalAmount);

			totalAmount += price.getPrice()*price.getQuantity();
			log.info("suppGoodsId is " + suppGoodsId + " " + pt.getPriceKey() + " price.getPrice():" + price.getPrice() + " price.getQuantity(), after add, totalAmount is " + totalAmount);
			if(hasControled){
				totalSettlement = (notBuyoutTotalPrice+ buyoutTotalPrice);
			}else{
				totalSettlement += settlementPrice*price.getQuantity();
			}
			if(!isSpread){
				hasSaledOut = hasSaledOut+ pt.getQuantity();
				stockQuantity += pt.getQuantity();
			}
		}
		
		orderItem.setOrdMulPriceRateList(list);
		orderItem.setQuantity((long)stockQuantity);
		if(hasControled){
			orderItem.setBuyoutQuantity(buyoutNum);
			orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
			orderItem.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
			orderItem.setBuyoutPrice(buyoutTotalPrice/orderItem.getBuyoutQuantity());
			orderItem.setBuyoutFlag("Y");
			orderItem.setNebulaProjectId(goodsResPrecontrolPolicyVO.getNebulaProjectId());
			
		}
		
		orderItem.setPrice(totalAmount/stockQuantity);
		log.info("suppGoodsId is " + suppGoodsId + " totalAmount is " + totalAmount + ", will set to orderItem");
		orderItem.setTotalAmount(totalAmount);
		orderItem.setSettlementPrice(totalSettlement/stockQuantity);
		orderItem.setActualSettlementPrice(orderItem.getSettlementPrice());
		orderItem.setTotalSettlementPrice(totalSettlement);
		return stockQuantity;
	}
	
	protected void setCancel(OrdOrderDTO order,OrdOrderItem orderItem){
		ProdRefund refund = prodProductClientService.findProductReFundByProdId(orderItem.getProductId(),orderItem.getVisitTime());
		if(refund==null){
			LOG.info("route product id:{} 无退改规则",orderItem.getProductId());
			throwIllegalException("无退改规则不可以下单");
		}
		orderItem.setCancelStrategy(refund.getCancelStrategy());
		//判断子订单是否是酒店套餐
		if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())){
			//判断退改规则是否是可退改
			if(ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(refund.getCancelStrategy())){
				String refundRules = DestBuOrderPropUtil.getRefundRulesStr(refund);
				orderItem.setRefundRules(refundRules);
			}
		}

	}
	
	
	protected Long getPriceTypeValue(SuppGoods suppGoods,SuppGoodsLineTimePrice timePrice, String priceKey){
		OrderEnum.ORDER_PRICE_RATE_TYPE linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.valueOf(priceKey);
		if(linePriceType==null){
			throwIllegalException("价格类型错误");
		}
		if(SuppGoods.PRICETYPE.SINGLE_PRICE.name().equalsIgnoreCase(suppGoods.getPriceType())){
			linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT;
		}
		Long value=0L;
		switch (linePriceType) {
		case PRICE_ADULT:
			value = timePrice.getAuditPrice();
			break;
		case PRICE_CHILD:
			value = timePrice.getChildPrice();
			break;
		case PRICE_SPREAD:
			value = timePrice.getGapPrice();
			break;
		default:
			break;
		}
		if(value==null){
			throwIllegalException("时间价格表禁售");
		}
		return value;
	}
	protected Long getPrecontrolPriceTypeValue(SuppGoods suppGoods,List<ResPreControlTimePriceVO> resPriceList, String priceKey){
		OrderEnum.ORDER_PRICE_RATE_TYPE linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.valueOf(priceKey);
		if(linePriceType==null){
			throwIllegalException("价格类型错误");
		}
		if(suppGoods.getCategoryId().intValue()!=17 && SuppGoods.PRICETYPE.SINGLE_PRICE.name().equalsIgnoreCase(suppGoods.getPriceType())){
			linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT_PRE;
		}
		if(suppGoods.getCategoryId()!=null && suppGoods.getCategoryId().equals(17)){
			linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_PRE;
		}
		Long value=null;
		switch (linePriceType) {
		case PRICE_ADULT:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT_PRE.name().equals(timePrice.getPreControlTimePriceAttrCode())){
					value = timePrice.getValue();
				}
			}
			break;
		case PRICE_CHILD:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD_PRE.name().equals(timePrice.getPreControlTimePriceAttrCode())){
					value = timePrice.getValue();
				}
			}
			break;
		case PRICE_SPREAD:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_SPREAD_PRE.name().equals(timePrice.getPreControlTimePriceAttrCode())){
					value = timePrice.getValue();
				}
			}
			break;
		//酒店套餐
		case PRICE_PRE:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_PRE.name().equals(timePrice.getPriceClassificationCode())){
					value = timePrice.getValue();
				}
			}
			break;
		default:
			break;
		}
		if(value==null){
			throwIllegalException("时间价格表禁售");
		}
		return value;
	}
	protected Long getSettlementPriceTypeValue(SuppGoods suppGoods,SuppGoodsLineTimePrice timePrice, String priceKey){
		OrderEnum.ORDER_PRICE_RATE_TYPE linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.valueOf(priceKey);
		if(linePriceType==null){
			throwIllegalException("价格类型错误");
		}
		if(SuppGoods.PRICETYPE.SINGLE_PRICE.name().equalsIgnoreCase(suppGoods.getPriceType())){
			linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT;
		}

		Long value=0L;
		switch (linePriceType) {
		case PRICE_ADULT:
			value = timePrice.getAuditSettlementPrice();
			break;
		case PRICE_CHILD:
			value = timePrice.getChildSettlementPrice();
			break;
		case PRICE_SPREAD:
			value = timePrice.getGrapSettlementPrice();
			break;
		default:
			break;
		}
		if(value==null){
			throwIllegalException("时间价格表禁售");
		}
		return value;
	}
	
	protected Long getPrecontrolSettlementPriceTypeValue(SuppGoods suppGoods,List<ResPreControlTimePriceVO> resPriceList, String priceKey){
		OrderEnum.ORDER_PRICE_RATE_TYPE linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.valueOf(priceKey);
		if(linePriceType==null){
			throwIllegalException("价格类型错误");
		}
		if(suppGoods.getCategoryId().intValue()!=17 &&SuppGoods.PRICETYPE.SINGLE_PRICE.name().equalsIgnoreCase(suppGoods.getPriceType())){
			linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE;
		}
		if(suppGoods.getCategoryId()!=null && suppGoods.getCategoryId().equals(17)){
			linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENTPRICE_PRE;
		}
		Long value=null;
		switch (linePriceType) {
		case PRICE_ADULT:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.name().equals(timePrice.getPreControlTimePriceAttrCode())){
					value = timePrice.getValue();
				}
			}
			
			break;
		case PRICE_CHILD:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.name().equals(timePrice.getPreControlTimePriceAttrCode())){
					value = timePrice.getValue();
				}
			}
			break;
		case PRICE_SPREAD:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD_PRE.name().equals(timePrice.getPreControlTimePriceAttrCode())){
					value = timePrice.getValue();
				}
			}
			break;
		//酒店套餐
		case SETTLEMENTPRICE_PRE:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENTPRICE_PRE.name().equals(timePrice.getPriceClassificationCode())){
					value = timePrice.getValue();
				}
			}
			
			break;
		default:
			break;
		}
		if(value==null){
			throwIllegalException("时间价格表禁售");
		}
		return value;
	}

	@Override
	public ResultHandleT<SuppGoodsBaseTimePrice> getTimePrice(Long goodsId,
			Date specDate, boolean checkAhead) {
		ResultHandleT<SuppGoodsBaseTimePrice> result = new ResultHandleT<SuppGoodsBaseTimePrice>();
//		SuppGoodsBaseTimePrice timePrice = goodsLineTimePriceStockService.getTimePrice(goodsId, specDate, checkAhead);
//		result.setReturnContent(timePrice);
		result.setReturnContent(getTimePriceWithGroupStock(goodsId, specDate, checkAhead));
		return result;
	}
	
	private SuppGoodsBaseTimePrice getTimePriceWithGroupStock(Long goodsId,
			Date specDate, boolean checkAhead) {
		Map<String, Object> params = new HashMap<String, Object>();
        params.put("suppGoodsId", goodsId);
        params.put("specDate", specDate);
        SuppGoodsLineTimePrice timePrice = suppGoodsLineTimePriceDao.selectByGoodsSpecDate(params);
        if (timePrice != null && checkAhead) {
            Long aheadBookTime = timePrice.getAheadBookTime();
            if (aheadBookTime == null) {
                aheadBookTime = 0L;
            }
            Date date = DateUtils.addMinutes(timePrice.getSpecDate(), -aheadBookTime.intValue());
            if (date.before(new Date())) {
                return null;
            }
        }

        Long groupId = suppGoodsLineTimePriceDao.findGroupIdBySuppGoodsId(goodsId);
        if (groupId == null) {
            return timePrice;
        }else{
        	Map<String, Object> groupParamsMap = new HashMap<String, Object>();
        	groupParamsMap.put("groupId", groupId);
        	groupParamsMap.put("specDate", specDate);
        	List<SuppGoodsGroupStock> suppGoodsGroupStockList = hotelGoodsGroupQueryAdapterService.selectBySpecDateRangeAndGroupId(groupParamsMap);
        	if(suppGoodsGroupStockList != null && suppGoodsGroupStockList.size() > 0){
        		Long shareStock = suppGoodsGroupStockList.get(0).getStock();
        		if (shareStock == null) {
                    return timePrice;
                }
        		timePrice.setStock(shareStock);
        		return timePrice;
        	}
        }
        
		return timePrice;
	}

	@Override
	public void updateRevertStock(Long suppGoodsId, Date specDate, Long stock,
			Map<String, Object> dataMap) {
//		SuppGoodsBaseTimePrice timePrice = goodsLineTimePriceStockService.getTimePrice(suppGoodsId, specDate, false);
		SuppGoodsBaseTimePrice timePrice = getTimePriceWithGroupStock(suppGoodsId, specDate, false);
		if(timePrice!=null){
			boolean hasHotelCombShareStock = false;
			Long hotelGroupId = 0L;
			Map<String, Object> shareMap = isShareHotelComb(dataMap, stock);
			hasHotelCombShareStock = (Boolean) shareMap.get("hasShareStock");
			hotelGroupId = (Long) shareMap.get("hotelGroupId");
			if(hasHotelCombShareStock)
			{
				HotelOrderUpdateStockDTO hotelOrderUpdateStock = new HotelOrderUpdateStockDTO();
				hotelOrderUpdateStock.setGroupId(hotelGroupId);
				hotelOrderUpdateStock.setSpecDate(specDate);
				hotelOrderUpdateStock.setUpdateStock(stock);
				hotelOrderUpdateStock.setSuppGoodsId(suppGoodsId);
				hotelOrderUpdateStock.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId());
				hotelTradeApiService.revertHotelCombGroupStock(hotelOrderUpdateStock);
			}else{
				goodsLineTimePriceStockService.updateStock(timePrice.getTimePriceId(), stock);
			}			
		}
	}

	@Override
	public String getTimePricePrefix() {
		return "LineTimePrice";
	}



	@Override
	public void calcSettlementPromotion(OrdOrderItem orderItem,
			List<OrdPromotion> promotions) {
		long adultAmount = 0;
		long childAmount = 0;
		long oldTalSettlementPrice=0;
		
		for(OrdPromotion op:promotions){
			FavorableAmount fa = op.getPromotion().getFavorableAmount();
			adultAmount += fa.getAdultAmount();
			childAmount += fa.getChildAmount();
		}
		
		oldTalSettlementPrice = orderItem.getTotalSettlementPrice();
		long totalPrice = oldTalSettlementPrice-adultAmount-childAmount;
		if(totalPrice<0){
			totalPrice=0;
		}
		orderItem.setTotalSettlementPrice(totalPrice);
		orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
		
		if(orderItem.getOrdMulPriceRateList()!=null){
			for(OrdMulPriceRate rate:orderItem.getOrdMulPriceRateList()){
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.name().equalsIgnoreCase(rate.getPriceType())){
					if(adultAmount>0){
						long oldActualSettlementPrice =rate.getPrice(); 
						rate.setPrice(rate.getPrice()-adultAmount/rate.getQuantity());
						initOrdSettlementPriceRecord(rate.getPriceType(),oldActualSettlementPrice,oldTalSettlementPrice, orderItem);
					}
				}else if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.name().equalsIgnoreCase(rate.getPriceType())){
					if(childAmount>0){
						long oldActualSettlementPrice =rate.getPrice(); 
						rate.setPrice(rate.getPrice()-childAmount/rate.getQuantity());
						initOrdSettlementPriceRecord(rate.getPriceType(),oldActualSettlementPrice,oldTalSettlementPrice, orderItem);
					}
					
				}
			}
		}
		
		
		
		
	}
	
	protected Long getPreSaleSettlementPriceTypeValue(SuppGoods suppGoods,List<PresaleStampTimePrice> timePrices, String priceKey){
		OrderEnum.ORDER_PRICE_RATE_TYPE linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.valueOf(priceKey);
		if(linePriceType==null){
			throwIllegalException("价格类型错误");
		}
		if(SuppGoods.PRICETYPE.SINGLE_PRICE.name().equalsIgnoreCase(suppGoods.getPriceType())){
			linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT;
		}

		Long value=0L;
		switch (linePriceType) {
		case PRICE_ADULT:
			for (PresaleStampTimePrice presaleStampTimePrice : timePrices) {
				if(PRICE_CLASSIFICATION_CODE.AUDIT.name().equalsIgnoreCase(presaleStampTimePrice.getPriceClassificationCode())){
					value=presaleStampTimePrice.getValue();
					break;
				}
			}
			break;
		case PRICE_CHILD:
			for (PresaleStampTimePrice presaleStampTimePrice : timePrices) {
				if(PRICE_CLASSIFICATION_CODE.CHILD.name().equalsIgnoreCase(presaleStampTimePrice.getPriceClassificationCode())){
					value=presaleStampTimePrice.getValue();
					break;
				}
			}
			break;
/*		case PRICE_SPREAD:
			value = timePrice.getGrapSettlementPrice();
			break;*/
		default:
			break;
		}
		if(value==null){
			throwIllegalException("时间价格表禁售");
		}
		return value;
	}

	
	private Long isShareStockAvailable(Long shareStock,Long stock,Long groupId){
        if (shareStock == null) {
            return null;
        }

        if (Long.valueOf(0).compareTo(stock) < 0) {
            return groupId;
        }

        if (shareStock.compareTo(Math.abs(stock)) < 0) {
            throw new BusinessException("Share stock is not enough to be deducted, share stock is:" + shareStock
                    + ",stock is:" + stock);
        }
		return groupId;
	}
	
	/**
	 * 酒店套餐迁移上线后共享库存
	 * @param dataMap
	 * @param stock
	 * @return
	 */
	private Map<String, Object> isShareHotelComb(Map<String, Object> dataMap,Long stock){		
		LOG.info("method isShareHotelComb deductStock begin。。。。。");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("hotelGroupId", 0L);
		boolean hasShareStock = false;
		Boolean flag = DynamicRouterUtils.getInstance().isHotelSystemOnlineEnabled();		
		if(flag)
		{			
			OrdOrderItem orderItem = (OrdOrderItem) dataMap.get("orderItem");
			SuppGoods suppGoods = null;
			//判断 1:酒店套餐 2:共享库存
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())){
				suppGoods = orderItem.getSuppGoods();				
				if(null == suppGoods){
					//查询DB
					ResultHandleT<SuppGoods> resultSupp = suppGoodsClientRemote.findSuppGoodsById(orderItem.getSuppGoodsId());
					if(resultSupp.isSuccess()){
						suppGoods = resultSupp.getReturnContent();
					}
				}
			}		
			if(null != suppGoods && null != suppGoods.getGroupId()){
				LOG.info("method isShareHotelComb deductStock suppGoods not null。。。。。");
				Date specDate = (Date) dataMap.get("beginDate");
				//远程查询新酒店Trade接口获取共享库存
				Long shareStocks = hotelTradeApiService.getHotelShareStock(suppGoods.getGroupId(), specDate);
				Long groupId = isShareStockAvailable(shareStocks, stock, suppGoods.getGroupId());
				resultMap.put("hotelGroupId", suppGoods.getGroupId());
				resultMap.put("shareStocks", shareStocks);
				LOG.info("method isShareHotelComb deductStock hotelGroupId"+suppGoods.getGroupId());
				if(null != groupId){
					hasShareStock = true;
				}
			}	
		}	
		resultMap.put("hasShareStock", hasShareStock);
		LOG.info(" method isShareHotelComb deductStock end。。。flag="+flag+"  hasShareStock="+hasShareStock);
		return resultMap;
	}

}
