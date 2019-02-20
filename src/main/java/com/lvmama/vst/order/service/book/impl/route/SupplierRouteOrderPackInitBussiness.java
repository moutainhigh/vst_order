/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Product;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;

/**
 * @author lancey
 *
 */
@Component("supplierRouteOrderPackInitBussiness")
public class SupplierRouteOrderPackInitBussiness extends
		RouteOrderPackInitBussiness {

	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	
	/**
	 * 检查打包关系是否满足
	 * @param product
	 * @param itemProduct
	 * @return
	 */
	@Override
	protected boolean checkProductValid(ProdProduct product,Product itemProduct,OrdOrderPackDTO pack){
		List<ProdPackageGroup> list = prodPackageGroupClientService.getProdPackageGroupByProductId(product.getProductId());
		if(list.isEmpty()){
			throwNullException("打包关系不存在，不可以下单");
		}
		boolean mainBranch=false;
		Map<Long,BuyInfo.Item> map = new HashMap<Long, BuyInfo.Item>();
		if(itemProduct.getItemList()!=null){
			for(BuyInfo.Item item:itemProduct.getItemList()){
				item.setQuantity(item.getAdultQuantity()+item.getChildQuantity());
				if(BuyInfo.ItemRelation.PACK.equals(item.getRouteRelation())){
					if(item.getDetailId()==null){
						throwNullException("打包信息为空");
					}
					if(item.getQuantity()>0){
						map.put(item.getDetailId(), item);
					}
				}else if(BuyInfo.ItemRelation.MAIN.equals(item.getRouteRelation())){
					if(mainBranch){
						throwIllegalException("存在多个主规格");
					}
					mainBranch=true;
				}
			}
		}
		for(ProdPackageGroup group:list){
			//如果不是“无限制”类型时需要
			if(!ProdPackageGroup.SELECTTYPE.NOLIMIT.name().equals(group.getSelectType())){
				if(!checkPackageGroup(group, map,pack)){
					//throwIllegalException("必选行程存在没有选购商品");
				}
			}
			
		}
		ResultHandleT<List<SuppGoods>> suppGoodsList = suppGoodsClientService.findLineMainBranchSuppGoodsByProductId(product.getBizCategoryId(), product.getProductId());
		if(suppGoodsList.hasNull()||suppGoodsList.getReturnContent().isEmpty()){
			throwIllegalException("商品主规格信息不存在");
		}
		if(suppGoodsList.getReturnContent().size()>1){
			throwIllegalException("商品存在错误不可以销售");
		}
		if(!mainBranch){
			BuyInfo.Item mainItem = new BuyInfo.Item();
			mainItem.setGoodsId(suppGoodsList.getReturnContent().get(0).getSuppGoodsId());
			mainItem.setAdultQuantity(itemProduct.getAdultQuantity());
			mainItem.setChildQuantity(itemProduct.getChildQuantity());
			mainItem.setVisitTime(itemProduct.getVisitTime());
			mainItem.setQuantity(itemProduct.getAdultQuantity()+itemProduct.getChildQuantity());
			mainItem.setRouteRelation(BuyInfo.ItemRelation.MAIN);
			List<BuyInfo.Item> itemList = itemProduct.getItemList();
			if(itemList==null){
				itemList = new ArrayList<BuyInfo.Item>();
				itemProduct.setItemList(itemList);
			}
			itemList.add(0,mainItem);
		}
		return true;
	}
	
	/**
	 * 检查附加的产品关系
	 * @param mainSuppGoodsId
	 * @param product
	 * @return
	 *//*
	private boolean checkMainSuppGoodsRelation(Long mainSuppGoodsId,BuyInfo.Product product){
		List<SuppGoodsRelation> list = suppGoodsClientService.
	}*/

	private boolean checkPackageGroup(ProdPackageGroup group,
			Map<Long, BuyInfo.Item> map,OrdOrderPackDTO pack) {
		boolean flag=false;
		List<ProdPackageDetail> packageDetails = prodPackageGroupClientService.getProdPackageDetailByGroupId(group.getGroupId());
		pack.getPackageDetailList().addAll(packageDetails);
		for(ProdPackageDetail ppd:packageDetails){
			if(map.containsKey(ppd.getDetailId())){
				flag=true;
				break;
			}
		}
		return flag;
	}
	
	

}
