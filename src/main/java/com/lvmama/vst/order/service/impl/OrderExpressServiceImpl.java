/**
 * 
 */
package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.dist.service.DistGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.vo.ExpressSuppGoodsVO;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.order.service.IOrderExpressService;
import com.lvmama.vst.order.service.IOrderInitService;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * @author pengyayun
 *
 */
@Service
public class OrderExpressServiceImpl implements IOrderExpressService{
	
	private static final Log LOG = LogFactory.getLog(OrderExpressServiceImpl.class);
	
	@Autowired
	protected SuppGoodsClientService suppGoodsClientRemote;
	
	@Autowired
	private IOrderInitService orderInitService;
	
	@Override
	public ResultHandleT<List<ExpressSuppGoodsVO>> findOrderExpressGoods(
			BuyInfo buyInfo) {
		ResultHandleT<List<ExpressSuppGoodsVO>> result=new ResultHandleT<List<ExpressSuppGoodsVO>>();
		try {
			if(buyInfo==null||buyInfo.getExpressage()==null){
				throw new IllegalArgumentException("buyInfo信息为空。");
			}
			List<Long> productIdsList = new ArrayList<Long>();
			String provinceCode = "";
			String cityCode = "";
			
			if(CollectionUtils.isNotEmpty(buyInfo.getProductList())){
				for(BuyInfo.Product bp:buyInfo.getProductList()){
					if(bp.getQuantity()>0||CollectionUtils.isNotEmpty(bp.getItemList())){
						if(bp.getAdultQuantity()<=0){
							bp.setAdultQuantity(buyInfo.getAdultQuantity());
						}
						if(bp.getChildQuantity()<=0){
							bp.setChildQuantity(buyInfo.getChildQuantity());
						}
						if(bp.getQuantity()<=0){
							bp.setQuantity(buyInfo.getQuantity());
						}
						if(StringUtils.isEmpty(buyInfo.getVisitTime())){
							bp.setVisitTime(buyInfo.getVisitTime());
						}
						if(buyInfo.getProductId()!=null){
							bp.setProductId(buyInfo.getProductId());
						}
					}
				}
			}
			
			// 得到需要查询价格的商品ID
			OrdOrderDTO order = new OrdOrderDTO(buyInfo);
			order = orderInitService.initOrder(order, false);
			if (CollectionUtils.isNotEmpty(order.getOrderItemList())) {
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					if(SuppGoods.GOODSTYPE.EXPRESSTYPE_DISPLAY.name().equalsIgnoreCase(orderItem.getSuppGoods().getGoodsType())){
						productIdsList.add(orderItem.getSuppGoodsId());
					}						
				}
				provinceCode = buyInfo.getExpressage().getProvinceCode();
				cityCode = buyInfo.getExpressage().getCityCode();
				if (StringUtils.isEmpty(provinceCode)
						|| StringUtils.isEmpty(cityCode)
						|| provinceCode.equals("-1") || cityCode.equals("-1")
						|| productIdsList.size() == 0||provinceCode.indexOf("选择")>-1||cityCode.indexOf("选择")>-1) {
					return result;
				}
			}
			
			// 调用远程服务，进行价格查询
			ResultHandleT<Map<Long, ExpressSuppGoodsVO>> resultHandler = suppGoodsClientRemote
					.findSuppGoodsExpreeCost(productIdsList, provinceCode, cityCode);
			if (resultHandler != null&&resultHandler.isSuccess()) {
				Map<Long, ExpressSuppGoodsVO> map = resultHandler.getReturnContent();
				if (MapUtils.isNotEmpty(map)) {
					ExpressSuppGoodsVO item = null;
					Iterator<ExpressSuppGoodsVO> itr = map.values().iterator();
					List<ExpressSuppGoodsVO> list=new ArrayList<ExpressSuppGoodsVO>();
					while (itr.hasNext()) {
						item = itr.next();
						list.add(item);
					}
					result.setReturnContent(list);
				}
			}
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			result.setMsg("查找快递商品发生异常。");
			
		}
		
		return result;
	}
	
}
