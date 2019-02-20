package com.lvmama.vst.order.web.line;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoods.GOODSTYPE;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.comm.utils.order.PriceUtil;

public class LineUtils {

	/**
	 * 改变日期价格的价格单位
	 * @param branchSelectPriceMap
	 * @return
	 */
	public static Map change(Map<String,Long> branchSelectPriceMap){
		Map newMap=new TreeMap();
		if(MapUtils.isNotEmpty(branchSelectPriceMap)){
			Iterator<String> itr=branchSelectPriceMap.keySet().iterator();
			String key="";
			Long value=null;
			while(itr.hasNext()){
				key=itr.next();
				value=branchSelectPriceMap.get(key);
				if(value==null){
					value=0L;
				}
				newMap.put(key, PriceUtil.trans2YuanStr(value));
			}
		}		
		return newMap;
	}

	/**
	 * 根据规格得到需要快递的商品列表
	 * @param prodProductBranchList
	 * @return
	 */
	public static List<Long> getNeedExpressGoodsListForBranch(List<ProdProductBranch> prodProductBranchList){
		List<Long> needExpressGoodsList=new ArrayList<Long>();
		if(CollectionUtils.isNotEmpty(prodProductBranchList)){
			for(ProdProductBranch branch:prodProductBranchList){
				if(branch != null && CollectionUtils.isNotEmpty(branch.getSuppGoodsList())){
					for(SuppGoods goods:branch.getSuppGoodsList()){
						if(StringUtils.isNotEmpty(goods.getGoodsType())&&goods.getGoodsType().equals(GOODSTYPE.EXPRESSTYPE_DISPLAY.name())){
							needExpressGoodsList.add(goods.getSuppGoodsId());
						}
					}
				}
			}
		}
		return needExpressGoodsList;
	}
	/**
	 * 根据打包组得到需要快递的商品列表
	 * @param prodProductBranchList
	 * @return
	 */
	public static List<Long> getNeedExpressGoodsListForGroup(
			List<ProdPackageGroup> prodPackageGroupList, boolean firstDetail) {
		List<Long> needExpressGoodsList = new ArrayList<Long>();
		List<ProdProductBranch> tempList = new ArrayList<ProdProductBranch>();
		if (CollectionUtils.isNotEmpty(prodPackageGroupList)) {
			for (ProdPackageGroup group : prodPackageGroupList) {
				if (CollectionUtils.isNotEmpty(group.getProdPackageDetails())) {
					if (firstDetail) {
						ProdPackageDetail detail = group
								.getProdPackageDetails().get(0);
						tempList.add(detail.getProdProductBranch());
					} else {
						for (ProdPackageDetail detail : group
								.getProdPackageDetails()) {
							tempList.add(detail.getProdProductBranch());
						}
					}
				}
			}
		}
		needExpressGoodsList = getNeedExpressGoodsListForBranch(tempList);
		return needExpressGoodsList;
	}
}
