/**
 * 
 */
package com.lvmama.vst.order.timeprice.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.dist.po.TimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.TimePriceCheckVO;
import com.lvmama.vst.back.goods.po.SuppGoodsLineTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.newHotelcomb.po.NewHotelCombTimePrice;
import com.lvmama.vst.back.newHotelcomb.po.SuppGoodsTimeStock;
import com.lvmama.vst.back.newHotelcomb.service.INewHotelCombProdAdditionService;
import com.lvmama.vst.back.newHotelcomb.service.INewHotelCombTimePriceService;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo.Item;
import com.lvmama.vst.comm.vo.order.destbu.NewHotelCombBuyInfo;
import com.lvmama.vst.comm.web.BusinessException;

import net.sf.json.JSONArray;

/**
 * 新酒套餐库存校验服务类
 */
@Component("newOrderHotelCompTimePriceService")
public class NewOrderHotelCompTimePriceServiceImpl{
	private static final Log LOG = LogFactory.getLog(NewOrderHotelCompTimePriceServiceImpl.class);

	 @Autowired
	 private INewHotelCombProdAdditionService newHotelCombProdAdditionClientRemote;
	 @Autowired
	 private DistGoodsTimePriceClientService distGoodsTimePriceClientService;
	 @Autowired
	 private INewHotelCombTimePriceService newHotelCombTimePriceClientRemote;
	 @Resource(name="goodsOraTimePriceStockService")
	 private IGoodsTimePriceStockService goodsTimePriceStockService;

	 
	/**
	 * suppGoods 酒套餐商品
	 * Item 购买的商品列表
	 * distributionId 分销商渠道
	 * dataMap 其他参数
	 * 
	 */
	 
	public ResultHandleT<Object> checkStock(SuppGoods suppGoods, Item item, Long distributionId,
			Map<String, Object> dataMap) {
			ResultHandleT<Object> resultHandleT = new ResultHandleT<Object>();
	
			if(suppGoods==null &&  !suppGoods.isValid()){
				throw new BusinessException("商品不存在或无效。");	
			 }
			ProdLineRoute prodLineRoute = newHotelCombTimePriceClientRemote.getLineRouteBySuppGoodsId(suppGoods.getSuppGoodsId());
			if(prodLineRoute==null){
				throw new BusinessException("酒套餐无行程。");	
			}
			//优先验证酒套餐
			Date currDate = new Date();
			Short days=prodLineRoute.getStayNum();
 			if(SuppGoods.BIZ_STOCK_TYPE.ALONE_STOCK.getCode().equals(suppGoods.getStockType())){

				//校验酒店套餐商品信息
				NewHotelCombTimePrice timePrice=	this.getNewHotelCombTimePriceAndCheck(suppGoods, item, item.getVisitTimeDate());
				if(!"1".equals(timePrice.getOnsaleFlag()+"")){
					throw new BusinessException("酒套餐不可售。");	
				}
				checkHotelCombInfo( resultHandleT,  currDate, (long)(item.getQuantity()),  timePrice);
				if(resultHandleT.isFail()){
					throw new BusinessException(resultHandleT.getMsg());	
				}
				Long aloneStock =null;
				if(timePrice!=null && timePrice.getSuppGoodsTimeStock()!=null){
					aloneStock= timePrice.getSuppGoodsTimeStock().getStock();
				}
				if(aloneStock!=null){
					if(!this.checkHotelComb2HotelTimePrice(timePrice,currDate,(long)(item.getQuantity()),aloneStock)){
						LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
						resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
						resultHandleT.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.getErrorCode());
						return resultHandleT;
					}
				}	
			}

			String stockType = suppGoods.getStockType();//newHotelCombTimePriceClientRemote.getStockTypeBySuppGoodsId(suppGoods.getSuppGoodsId());
			if ((SuppGoods.BIZ_STOCK_TYPE.SHARE_STOCK.getCode()).equals(stockType)) {//共享库存
				//校验酒店套餐商品信息
				NewHotelCombTimePrice timePrice=	this.getNewHotelCombTimePriceAndCheck(suppGoods, item, item.getVisitTimeDate());
				if(!"1".equals(timePrice.getOnsaleFlag()+"")){
					throw new BusinessException("酒套餐不可售。");	
				}
				checkHotelInfo(resultHandleT, currDate, (long)(item.getQuantity()), timePrice);
			
				if(resultHandleT.isFail()){
					throw new BusinessException(resultHandleT.getMsg());	
				}	
				//验证酒店是否可售
				SuppGoods suppGoodsOfHotel =newHotelCombProdAdditionClientRemote.getSuppGoodsOfHotel(suppGoods.getProductId(),suppGoods.getSuppGoodsId());
				if(suppGoodsOfHotel==null &&  !suppGoodsOfHotel.isValid()){
					  resultHandleT.setMsg("酒店商品不存在或无效。");
					return resultHandleT;
			     }
				for(Integer i=0;i<days;i++){
					Date oneDate = DateUtils.addDays(item.getVisitTimeDate(), i);
					Long groupId = suppGoodsOfHotel.getGroupId();
					Long shareStock = null;
					if(null != groupId){
						LOG.info("查询共享库存：groupId="+groupId+"    visitTime="+oneDate);
						shareStock = goodsTimePriceStockService.getShareStock(groupId,oneDate);
						LOG.info("oneDate="+oneDate+"    groupId="+groupId+"     shareStock="+shareStock);
					}
					ResultHandleT<TimePrice> timePriceHolder = distGoodsTimePriceClientService.findTimePrice(distributionId, suppGoodsOfHotel.getSuppGoodsId(), oneDate);
					
					LOG.info("正在进行库存检查，获取时间价格表数据,得到的时间数据为 \n"+JSONArray.fromObject(timePriceHolder));
					LOG.info("OrderTimePriceServiceImpl.checkStock(Date=" + oneDate + "): timePriceHolder.isSuccess=" + timePriceHolder.isSuccess());
					if(timePriceHolder.isFail() || timePriceHolder.getReturnContent() == null){
						LOG.info("商品ID=" + suppGoodsOfHotel.getSuppGoodsId() + ",时间" + oneDate + "时间价格表不存在。");
						resultHandleT.setMsg("商品  " + suppGoodsOfHotel.getGoodsName() + " (ID:" + suppGoodsOfHotel.getSuppGoodsId() + ")时间价格表不存在。");
						return resultHandleT;
					}
					SuppGoodsTimePrice hotelTimePrice = timePriceHolder.getReturnContent();
					//查询共享库存
					if(null != shareStock){
						if(!this.checkSharedTimePrice(hotelTimePrice,currDate,(long)(item.getQuantity()),shareStock)){
							LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
							resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
							resultHandleT.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.getErrorCode());
							return resultHandleT;
						}
					}else{
						if (!this.checkTimePrice(hotelTimePrice, currDate, (long) (item.getQuantity()))) {
							LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
							resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
							resultHandleT.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.getErrorCode());
							return resultHandleT;
						}
					}
				}
			}
		
		return resultHandleT;
	}

	
	/**
	 * 新酒套餐检查时间价格
	 * @param suppGoods
	 * @param item
	 * @param visitTime
	 * @return
	 */
	private NewHotelCombTimePrice getNewHotelCombTimePriceAndCheck(SuppGoods suppGoods,
			NewHotelCombBuyInfo.Item item, Date visitTime) {
		ResultHandleT<NewHotelCombTimePrice> timePriceHandle = newHotelCombTimePriceClientRemote.findSuppGoodsTimePriceBySuppGoodsIdAndSpecDate(suppGoods.getSuppGoodsId(), visitTime);
		if (timePriceHandle == null||timePriceHandle.hasNull()) {
			throw new BusinessException(suppGoods.getSuppGoodsId()+"商品无时间价格");
		}
		NewHotelCombTimePrice timePrice = timePriceHandle.getReturnContent();
		checkOnsaleFlag(timePrice,suppGoods);
		checkParam(suppGoods, item, true);

		return timePrice;
	}
	
	/**
	 * 商品影响订购的参数检查
	 * @param suppGoods
	 * @param item
	 */
	private void checkParam(final SuppGoods suppGoods, final NewHotelCombBuyInfo.Item item, boolean ck){
		
		if(suppGoods==null){
			throw new BusinessException("商品ID=" + item.getGoodsId() + "不存在");
		}
		if(ck){
			if (item.getQuantity() <= 0) {
				throw new BusinessException("商品 " + suppGoods.getGoodsName() + " 订购数量小于等于零");
			}

            if((null != suppGoods.getMaxQuantity()) && (item.getQuantity() > suppGoods.getMaxQuantity())){
//              throw new IllegalArgumentException("商品 " + suppGoods.getGoodsName() + " 订购数量超出最大值");
            	throw new  	BusinessException("商品 " + suppGoods.getGoodsName() + " 订购数量超出最大值");
            }
            
            if((null != suppGoods.getMinQuantity()) && (item.getQuantity() <suppGoods.getMinQuantity())){
            	throw new 	BusinessException ("商品 " + suppGoods.getGoodsName() + " 订购数量小于最小值");
            }
			
			if (item.getOwnerQuantity() > item.getQuantity()) {
				throw new 	BusinessException ("商品" + suppGoods.getGoodsName() + "  实际订购数量小于零");
			}
		}
	}
	
	private void checkOnsaleFlag(NewHotelCombTimePrice timePrice,SuppGoods suppGoods){	
		if(SuppGoods.BIZ_STOCK_TYPE.ALONE_STOCK.getCode().equals(suppGoods.getStockType())){
			if (timePrice.getSuppGoodsTimeStock()==null || !"1".equalsIgnoreCase(timePrice.getSuppGoodsTimeStock().getOnsaleFlag()+"")) {
				throw new BusinessException("商品游玩日期不可售");
			}
		}
		if(SuppGoods.BIZ_STOCK_TYPE.SHARE_STOCK.getCode().equals(suppGoods.getStockType())){
			List<SuppGoodsTimePrice> suppGoodsTimePrices=timePrice.getSuppGoodsTimePrice();
			if(CollectionUtils.isNotEmpty(suppGoodsTimePrices)){
				for(SuppGoodsTimePrice sgt:suppGoodsTimePrices){
					if (!"1".equalsIgnoreCase(sgt.getOnsaleFlag()+"")) {
						throw new BusinessException("商品游玩日期不可售");
					}	
				}
			}
		}
	}
	  /**
     * 新酒套餐检验库存
     * @return
     */
    protected boolean checkHotelComb2HotelTimePrice(NewHotelCombTimePrice timePrice, Date date, Long stock,Long sharedStock) {
        return  this.checkHotelComb2HotelTimePriceForOrder(date,stock,sharedStock,timePrice);
    }
    
    
 	//新酒套餐检验库存，检查能否下单
  	public boolean checkHotelComb2HotelTimePriceForOrder(Date currDate, Long quatity,Long stock,NewHotelCombTimePrice timePrice) {
        
  		SuppGoodsTimeStock sgts=timePrice.getSuppGoodsTimeStock();
  		//如果是满房
  		if(SuppGoodsTimePrice.STOCKSTATUS.FULL.name().equalsIgnoreCase(timePrice.getSuppGoodsTimeStock().getStockStatus())){
  			return false;
  		}

  	    if(SuppGoodsLineTimePrice.STOCKTYPE.CONTROL.name().equalsIgnoreCase(sgts.getStockType())
                 ||SuppGoodsLineTimePrice.STOCKTYPE.INQUIRE_WITH_STOCK.name().equalsIgnoreCase(sgts.getStockType())){

             if(sgts.getStock()<quatity){
                 if("0".equalsIgnoreCase(sgts.getOversellFlag()+"")){
                	 return false;
                 }
             }
         }

  	    return true;
  	}
	/**
	 * 独立库存校验信息
	 * @param resultHandleT
	 * @param currDate
	 * @param quatity
	 * @param timePrice
	 */
	private void checkHotelCombInfo(ResultHandleT<Object> resultHandleT, Date currDate, Long quatity, NewHotelCombTimePrice timePrice) {
		SuppGoodsTimeStock sgts=timePrice.getSuppGoodsTimeStock();
	  	if(sgts==null){
	  		resultHandleT.setMsg("酒套餐自有的库存异常！");
	  		return;
	  	}
  		//基础条件不满足，则直接返回不能下单
  		if (currDate == null) {
  			resultHandleT.setMsg("下单时间为空。");
  			return;
  		}
  		
  		if(timePrice.getProdRefund()==null || StringUtils.isEmpty(timePrice.getProdRefund().getCancelStrategy())){
  			resultHandleT.setMsg("退改规则不能为空。");
  			return;
  		}	
  		if(quatity <0){
  			resultHandleT.setMsg("购买数量为" + quatity);
  			return;
  		}
  		//判断提前预定时间
  		if(!this.isBeforeAheadBookTime(currDate,timePrice)){
  			resultHandleT.setMsg("下单已过提前预定时间。");
  			return;
  		}
  		//如果没有价格
  		if(timePrice.getPrice()==null||timePrice.getPrice()<0){
  			resultHandleT.setMsg("没有价格。");
  			return;
  		}
  		//商品不可售
  		if(!"1".equalsIgnoreCase(sgts.getOnsaleFlag()+"")){
  			resultHandleT.setMsg("时间价格不可售。");
  			return;
  		}
	}
	/**
	 * 共享库存校验信息
	 * @param resultHandleT
	 * @param currDate
	 * @param quatity
	 * @param timePrice
	 */
	
	private void checkHotelInfo(ResultHandleT<Object> resultHandleT, Date currDate, Long quatity, NewHotelCombTimePrice timePrice) {
		List<SuppGoodsTimePrice>  sgts=timePrice.getSuppGoodsTimePrice();
	  	if(sgts==null){
	  		resultHandleT.setMsg("酒套餐自有的库存异常！");
	  		return;
	  	}
  		//基础条件不满足，则直接返回不能下单
  		if (currDate == null) {
  			resultHandleT.setMsg("下单时间为空。");
  			return;
  		}
  		
  		if(timePrice.getProdRefund()==null || StringUtils.isEmpty(timePrice.getProdRefund().getCancelStrategy())){
  			resultHandleT.setMsg("退改规则不能为空。");
  			return;
  		}	
  		if(quatity <0){
  			resultHandleT.setMsg("购买数量为" + quatity);
  			return;
  		}
  		//判断提前预定时间
  		if(!this.isBeforeAheadBookTime(currDate,timePrice)){
  			resultHandleT.setMsg("下单已过提前预定时间。");
  			return;
  		}
  		//如果没有价格
  		if(timePrice.getPrice()==null||timePrice.getPrice()<0){
  			resultHandleT.setMsg("没有价格。");
  			return;
  		}
  		
  		for(SuppGoodsTimePrice  suppGoodsTimePrice:sgts){
  			//商品不可售
  			if(!"1".equalsIgnoreCase(suppGoodsTimePrice.getOnsaleFlag()+"")){
  				resultHandleT.setMsg("时间价格不可售。");
  				return;
  			}	
  		}
  		
	}
	
  	//判断是否在提前预定时间之前
  	public boolean isBeforeAheadBookTime(Date currDate,NewHotelCombTimePrice timePrice){
  		if(timePrice.getAheadBookTime()==null || "".equals(timePrice.getAheadBookTime())) return Boolean.FALSE;
  		boolean isBefore = false;
  		if (timePrice.getSpecDate()!= null && currDate != null) {
  			//convert Long to int type, Maybe block
  			Date countDate = DateUtil.DsDay_Minute(timePrice.getSpecDate(), (int)(-timePrice.getAheadBookTime()));
  			if (currDate.before(countDate)){
  				isBefore = true;
  			}
  		}
  		return isBefore;	
  	}

	/**
	 * 校验单酒店独立库存
	 * @return
	 */
	protected boolean checkTimePrice(SuppGoodsBaseTimePrice timePrice, Date date, Long stock) {
		boolean isSuccess = false;
		//时间价格表验证
		TimePriceCheckVO checkVO = timePrice.checkTimePriceForOrder(date, stock);
		if (checkVO != null) {
			if (checkVO.isOrderAble()) {
				isSuccess = true;
			} else {
				LOG.info("NewOrderHotelCompTimePriceServiceImpl.checkTimePrice(TimePriceID=" + timePrice.getTimePriceId() + "): checkVO.isOrderAble()=false, notAbleReason=" + checkVO.getNotAbleReason());
			}
		} else {
			LOG.info("NewOrderHotelCompTimePriceServiceImpl.checkTimePrice(TimePriceID=" + timePrice.getTimePriceId() + "): checkVO=null");
		}
		
		return isSuccess;
	}
	   /**
     * 检验单酒店共享库存
     * @return
     */
    protected boolean checkSharedTimePrice(SuppGoodsBaseTimePrice timePrice, Date date, Long stock,Long sharedStock) {
        boolean isSuccess = false;
        //时间价格表验证
        TimePriceCheckVO checkVO = timePrice.checkTimePriceForShareOrder(date,stock,sharedStock);
        if (checkVO != null) {
            if (checkVO.isOrderAble()) {
                isSuccess = true;
            } else {
                LOG.info("NewOrderHotelCompTimePriceServiceImpl.checkTimePrice(TimePriceID=" + timePrice.getTimePriceId() + "): checkVO.isOrderAble()=false, notAbleReason=" + checkVO.getNotAbleReason());
            }
        } else {
            LOG.info("NewOrderHotelCompTimePriceServiceImpl.checkTimePrice(TimePriceID=" + timePrice.getTimePriceId() + "): checkVO=null");
        }

        return isSuccess;
    }
}
