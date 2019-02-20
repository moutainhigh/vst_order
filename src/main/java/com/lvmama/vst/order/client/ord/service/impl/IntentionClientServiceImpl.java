package com.lvmama.vst.order.client.ord.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.IntentionClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.order.po.IntentionLineProdPackageGroupContainer;
import com.lvmama.vst.back.order.po.IntentionPlaneTypeInfo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderTravellerOperateDO;
import com.lvmama.vst.back.prod.curise.vo.CuriseProductVO;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageGroup;
import com.lvmama.vst.back.prod.po.ProdPackageGroup.GROUPTYPE;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.order.contract.service.IOrderTravelContractDataService;
import com.lvmama.vst.order.contract.service.impl.OrderTravelContractDataServiceFactory;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.OrdOrderTravellerConfirmService;
import com.lvmama.vst.order.service.flight.AirlineService;
import com.lvmama.vst.order.service.flight.info.PlaneTypeInfo;
import com.lvmama.vst.order.web.line.LineProdPackageGroupContainer;

/**
 * Dubbo service for Intention order
 * @author Liyupeng
 *
 */
@Component("intentionClientServiceRemote")
public class IntentionClientServiceImpl implements
		IntentionClientService {

	@Autowired
	private com.lvmama.vst.order.web.line.service.LineProdPackageGroupService lineProdPackageGroupService;
	
	@Autowired
	private OrderTravelContractDataServiceFactory orderTravelContractDataServiceFactory;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Autowired
	private AirlineService airlineService;
	
	@Resource(name = "ordOrderTravellerConfirmService")
    private OrdOrderTravellerConfirmService ordOrderTravellerConfirmService;
	
	
	@Resource(name = "complexQueryService")
	private IComplexQueryService complexQueryService;
	

	/**
	 * 初始化打包组的时间价格表
	 * 
	 * @param packageProdPackageList
	 * @param specDate
	 * @param type
	 */
	public void initPackageProductBranchList(
			List<ProdPackageGroup> packageProdPackageList, Date specDate, GROUPTYPE type, boolean isSupplier,IntentionLineProdPackageGroupContainer container) {
		LineProdPackageGroupContainer lineProdPackageGroupContainer = new LineProdPackageGroupContainer();
		BeanUtils.copyProperties(container, lineProdPackageGroupContainer);
		lineProdPackageGroupService.initPackageProductBranchList(packageProdPackageList, specDate, type, isSupplier, lineProdPackageGroupContainer);
	}
	
	/**
	 * 计算供应商产品规格的时间价格表
	 * 
	 * @param prodBranch
	 * @param specDate
	 * @param type
	 * @param packageDetail
	 */
	public void initSupplierProdBranchTimePrice(ProdProductBranch prodBranch,
			Date specDate, GROUPTYPE type) {
		lineProdPackageGroupService.initSupplierProdBranchTimePrice(prodBranch, specDate, type);

	}
	

	
	/**
	 * 计算驴妈妈产品规格的时间价格表
	 * 
	 * @param prodBranch
	 * @param specDate
	 * @param type
	 * @param packageDetail
	 */
	public void initLvmamaProdBranchTimePrice(ProdProductBranch prodBranch,
			Date specDate, GROUPTYPE type, ProdPackageDetail packageDetail) {
		
		lineProdPackageGroupService.initLvmamaProdBranchTimePrice(prodBranch, specDate, type, packageDetail);
	}
	

	/**
	 * 根据日期和线路类型计算商品的时间假期表
	 * 
	 * @param specDate
	 * @param suppGoodsId
	 * @param type
	 * @return
	 */
	public SuppGoodsBaseTimePrice getSuppGoodsBaseTimePrice(Date specDate,
			SuppGoods suppGoods, GROUPTYPE type,boolean hasAperiodic) {
		return lineProdPackageGroupService.getSuppGoodsBaseTimePrice(specDate, suppGoods, type, hasAperiodic);
	}
	
	
//	public IOrderTravelContractDataService createTravelContractDataService(OrdOrder ordOrder){
//		return orderTravelContractDataServiceFactory.createTravelContractDataService(ordOrder);
//		
//	}
	
	public ResultHandleT<CuriseProductVO> getCombCuriseProducatData(OrdOrder ordOrder, Long combCategoryId, Long combProductId) {
		
		IOrderTravelContractDataService orderTravelContractDataService = orderTravelContractDataServiceFactory.createTravelContractDataService(ordOrder);
		if(orderTravelContractDataService!=null){
			return orderTravelContractDataService.getCombCuriseProducatData(combCategoryId, combProductId);
		}else{
			ResultHandleT<CuriseProductVO> resultHandleT = new ResultHandleT<CuriseProductVO>();
			return resultHandleT;
		}
		
	}
	
	
	public int saveOrUpdateOrderTravellerConfirm(OrderTravellerOperateDO orderTravellerOperateDO){
		return ordOrderTravellerConfirmService.saveOrUpdate(orderTravellerOperateDO);
	}
	
	/**
	 * 
	 * @param orderId
	 * @return
	 */
	public OrdOrder queryOrderByOrderId(final Long orderId){
		return complexQueryService.queryOrderByOrderId(orderId);
	}
	
	
	/**
	 * 保存一个订单的
	 * @param orderId
	 * @param buyInfo
	 * @return
	 */

	public ResultHandle saveOrderPerson(Long orderId, BuyInfo buyInfo, String operatorId){
		return orderLocalService.saveOrderPerson(orderId, buyInfo, operatorId);
	}
	
	/**
	 * 后台订单人工开启流程流转
	 * @param orderId
	 * @param operatorId
	 * @return
	 */
	public ResultHandle startBackOrder(final Long orderId,String operatorId){
		return orderLocalService.startBackOrder(orderId, operatorId);
	}
	
	/**
	 * 查询机型信息
	 * @param code	机型编码
	 * @return
	 */
	public IntentionPlaneTypeInfo findPlaneTypeByCode(String code){
		IntentionPlaneTypeInfo intentionPlaneTypeInfo = new IntentionPlaneTypeInfo();
		PlaneTypeInfo planeTypeInfo =  airlineService.findPlaneTypeByCode(code);
		BeanUtils.copyProperties(planeTypeInfo, intentionPlaneTypeInfo);
		return intentionPlaneTypeInfo;
		
		
	}

	@Override
	public IntentionLineProdPackageGroupContainer initPackageProductMap(Date specDate,
			Map<String, List<ProdPackageGroup>> packageMap, boolean isSupplier) {
		IntentionLineProdPackageGroupContainer intentionLineProdPackageGroupContainer = new IntentionLineProdPackageGroupContainer();
		LineProdPackageGroupContainer lineProdPackageGroupContainer = lineProdPackageGroupService.initPackageProductMap(specDate, packageMap, isSupplier);
		BeanUtils.copyProperties(lineProdPackageGroupContainer, intentionLineProdPackageGroupContainer);
		return intentionLineProdPackageGroupContainer;
		 
	}
	

}
