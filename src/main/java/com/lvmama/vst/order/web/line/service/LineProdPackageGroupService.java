package com.lvmama.vst.order.web.line.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;
import com.lvmama.vst.back.prod.po.ProdPackageGroup.GROUPTYPE;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.order.web.line.LineProdPackageGroupContainer;

public interface LineProdPackageGroupService {

	/**
	 * 计算打包产品所有组的时间价格表
	 */
	public LineProdPackageGroupContainer initPackageProductMap(Date specDate,
			Map<String, List<ProdPackageGroup>> packageMap, boolean isSupplier);

	/**
	 * 计算一个行程的时间价格表
	 * 
	 * @param packageProdPackageList
	 * @param specDate
	 * @param type
	 */
	public void initPackageProductBranchList(
			List<ProdPackageGroup> packageProdPackageList, Date specDate,
			GROUPTYPE type, boolean isSupplier,LineProdPackageGroupContainer container);

	/**
	 * 计算一个供应商规格下所有产品的时间价格表
	 * 
	 * @param prodBranch
	 * @param specDate
	 * @param type
	 */
	public void initSupplierProdBranchTimePrice(ProdProductBranch prodBranch,
			Date specDate, GROUPTYPE type);

	/**
	 * 计算自主打包的规格下所有产品的时间价格表
	 * 
	 * @param prodBranch
	 * @param specDate
	 * @param type
	 * @param packageDetail
	 */
	public void initLvmamaProdBranchTimePrice(ProdProductBranch prodBranch,
			Date specDate, GROUPTYPE type, ProdPackageDetail packageDetail);
	

	/**
	 * 计算线路商品的时间价格表
	 * 
	 * @param specDate
	 * @param suppGoodsId
	 * @param type
	 * @return
	 */
	public SuppGoodsBaseTimePrice getSuppGoodsBaseTimePrice(Date specDate,
			SuppGoods suppGoods, GROUPTYPE type,boolean hasAperiodic);

	/**
	 * 得到交通产品的详细信息
	 * 
	 * @param tracfficPackageGroupList
	 */
	public void initTrafficBizInfo(
			List<ProdPackageGroup> tracfficPackageGroupList);

}
