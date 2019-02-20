package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.prod.adapter.ProdProductHotelAdapterClientService;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.enumeration.CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.contract.service.IOrderElectricContactService;
import com.lvmama.vst.order.contract.service.IOrderElectricService;
import com.lvmama.vst.order.dao.OrdTravelContractDAO;
import com.lvmama.vst.order.service.IComplexQueryService;

/**
 * 
 * @author sunjian
 *
 */
@Service
public class OrderEcontractGeneratorService {
	private static final Logger LOG = LoggerFactory.getLogger(OrderEcontractGeneratorService.class);
	@Resource(name="orderTravelElectricContactService")
	private IOrderElectricContactService orderTravelElectricContactService;
	
	@Resource(name="orderCommissionedServiceAgreementService")
	private IOrderElectricContactService orderCommissionedServiceAgreementService;
	
	@Resource(name="teamOutboundTourismContractService")
	private IOrderElectricService teamOutboundTourismContractService;
	
	@Resource(name="teamWithInTerritoryContractService")
	private IOrderElectricService teamWithInTerritoryContractService;
	
	@Resource(name="teamDonggangZhejiangContractService")
	private IOrderElectricService teamDonggangZhejiangContractService;
	
	@Resource(name="commissionedServiceAgreementService")
	private IOrderElectricContactService commissionedServiceAgreementService;
	
	@Resource(name="beijingDayTourContractService")
	private IOrderElectricContactService beijingDayTourContractService;
	
	@Resource(name="advanceProductAgreementContractService")
	private IOrderElectricContactService advanceProductAgreementContractService;
	
	@Resource(name="preSalesAgreementContractService")
	private IOrderElectricContactService preSalesAgreementContractService;
	
	@Resource(name="taiwanTravelContractService")
	private IOrderElectricContactService taiwanTravelContractService;
	@Resource(name="destCommissionedServiceAgreementService")
	private IOrderElectricContactService destCommissionedServiceAgreementService;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private OrdTravelContractDAO ordTravelContractDAO;
	
	@Resource(name="cruiseTourismContractService")
	private IOrderElectricContactService cruiseTourismContractService;
	
	@Resource(name="financeContractService")
	private IOrderElectricContactService financeContractService;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	
	@Autowired
	private ProdProductHotelAdapterClientService prodProductHotelAdapterClientService;
	
	public ResultHandle generateEcontract(Long orderId, String operatorName) {
		Log.info("===========合同生成orderId:"+orderId+"================");
		ResultHandle resultHandle = new ResultHandle();
		if (orderId != null) {
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);//订单
			ResultHandleT<SuppGoods> resultHandlesuppGoods =suppGoodsClientService.findSuppGoodsById(order.getMainOrderItem().getSuppGoodsId());	
			if(resultHandlesuppGoods.getReturnContent()!=null){
				SuppGoods SuppGoods =resultHandlesuppGoods.getReturnContent();
				Log.info("进入合同SuppGoods.getProductId()============"+SuppGoods.getProductId());
				if(SuppGoods!=null){
					if(null!=SuppGoods.getProductId() && !"".equals(SuppGoods.getProductId())){
						ResultHandleT<ProdProduct> resultHandleT= prodProductHotelAdapterClientService.findProductById(SuppGoods.getProductId());
						ProdProduct prodProduct=resultHandleT.getReturnContent();
						LOG.info("=====1taiwan,jingyu,CategoryId："+order.getCategoryId()+"======CompanyType："+order.getMainOrderItem().getCompanyType()+"=====BizDistrictId："+prodProduct.getBizDistrictId());
						//如果订单是台湾签证，公司主体是景域的，不生成不发送任何合同    8=行政区域是中国
						if(BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCategoryId().equals(order.getCategoryId()) && 
								(ProdProduct.COMPANY_TYPE_DIC.JOYU.name()).equals(order.getMainOrderItem().getCompanyType()) && 
								(new Long(8L)).equals(prodProduct.getBizDistrictId())){
							resultHandle.setMsg("订单ID=" + orderId + ",不生成不发送任何合同。");
							return resultHandle;
						}
					}
				}
				
			}
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderId", orderId);
			List<OrdTravelContract> ordTravelContractList = ordTravelContractDAO.selectByParam(params);
			
			boolean isTravellerFlag = false;
			boolean isNeedSendEmail=true;
			//后置订单，未锁定游玩人
			if(("Y").equals(order.getTravellerDelayFlag()) && ("N").equals(order.getTravellerLockFlag())){
				isTravellerFlag = true;
				isNeedSendEmail = false;
			}
			if (ordTravelContractList != null && !ordTravelContractList.isEmpty()) {
				for (OrdTravelContract ordTravelContract : ordTravelContractList) {
					if (ordTravelContract != null) {
						//邮轮出境旅游合同
						if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.OUTBOUND_TOURISM.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
							resultHandle = orderTravelElectricContactService.saveTravelContact(ordTravelContract, operatorName);//油轮对应合同
							isNeedSendEmail=false;
						//邮轮委托服务协议
						} else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
							resultHandle = orderCommissionedServiceAgreementService.saveTravelContact(ordTravelContract, operatorName);//油轮对应合同
							isNeedSendEmail=false;
						//团队出境旅游合同
						} else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.TEAM_OUTBOUND_TOURISM.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
							resultHandle = teamOutboundTourismContractService.saveTravelContact(ordTravelContract, operatorName);
						//浙江东港合同
						} else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.DONGGANG_ZHEJIANG_CONTRACT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
							resultHandle = teamDonggangZhejiangContractService.saveTravelContact(ordTravelContract, operatorName);
						//团队境内旅游合同
						}else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
							Log.info("境内合同生成orderId:"+orderId+"start");
							resultHandle = teamWithInTerritoryContractService.saveTravelContact(ordTravelContract, operatorName);
							Log.info("境内合同生成orderId:"+orderId+"end");
						//预付款协议
						} else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
							resultHandle = advanceProductAgreementContractService.saveTravelContact(ordTravelContract, operatorName);
							isNeedSendEmail=true;
						//委托服务协议
						} else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
							Log.info("委托服务协议合同生成orderId:"+orderId+"start");
							resultHandle = commissionedServiceAgreementService.saveTravelContact(ordTravelContract, operatorName);
							Log.info("委托服务协议合同生成orderId:"+orderId+"end");
				 		//目的地委托服务协议
						}else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {							
							LOG.info("----根据订单渠道下单判断目的地委托服务协议合同是否生成---");
							//目的地后台下单
							if(OrdOrderUtils.isDestBuBackOrder(order)){
								isNeedSendEmail = false;
								LOG.info("----目的地后台下单合同此处不生成---");
							}else{
								resultHandle = destCommissionedServiceAgreementService.saveTravelContact(ordTravelContract, operatorName);
								isNeedSendEmail=true;
							}						
				 		}//旅游产品预售协议
						else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.PRESALE_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
							resultHandle = preSalesAgreementContractService.saveTravelContact(ordTravelContract, operatorName);
						//赴台旅游预订须知
						} else if(CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.TAIWAN_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())){
							resultHandle = taiwanTravelContractService.saveTravelContact(ordTravelContract, operatorName);
						//上海邮轮旅游合同
						} else if(CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.CRUISE_TOURISM_SHANGHAI.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())){
							resultHandle = cruiseTourismContractService.saveTravelContact(ordTravelContract, operatorName);
						}else {
							resultHandle.setMsg("订单ID=" + orderId + ",合同模板(" + ordTravelContract.getContractTemplate() + ")不存在。");
						}
					} else {
						resultHandle.setMsg("订单ID=" + orderId + ",合同信息不存在。");
					}
				}
				LOG.info("---------1-------------合同3==="+resultHandle.getMsg());
				
				if ( isNeedSendEmail && resultHandle.isSuccess()) {//非油轮对应合同统一调用发送邮件功能，油轮内部产生合同有自己发送邮件代码
					if(order.getDistributorId()!=null&&!order.getDistributorId().equals(Constant.DIST_O2O_SELL)  &&  !order.getDistributorId().equals(Constant.DIST_O2O_APP_SELL)){//o2o门店所有门店默认不发送邮件
						LOG.info("---------2-------------合同3==="+resultHandle.getMsg());
						resultHandle=commissionedServiceAgreementService.sendOrderEcontractEmail(order,"system");
					}
				}
				
				
			} else {
				resultHandle.setMsg("订单ID=" + orderId + ",不需生成合同。");
			}
		} else {
			resultHandle.setMsg("订单ID为null");
		}
		LOG.info("---------3-------------合同3==="+resultHandle.getMsg());
		return resultHandle;
	}
	
	//TODO
	public ResultHandle generateFinanceEcontract(Long orderId, String operatorName) {
		Log.info("===========合同生成orderId:"+orderId+"================");
		ResultHandle resultHandle = new ResultHandle();
		if (orderId != null) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderId", orderId);
			List<OrdTravelContract> ordTravelContractList = ordTravelContractDAO.selectByParam(params);
		
			if (ordTravelContractList != null && !ordTravelContractList.isEmpty()) {
				for (OrdTravelContract ordTravelContract : ordTravelContractList) {
					if (ordTravelContract != null) {
						if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.FINANCE_CONTRACT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
							resultHandle = financeContractService.saveTravelContact(ordTravelContract, operatorName);
						} else {
							resultHandle.setMsg("订单ID=" + orderId + ",合同模板(" + ordTravelContract.getContractTemplate() + ")不存在。");
						}
					} else {
						resultHandle.setMsg("订单ID=" + orderId + ",合同信息不存在。");
					}
				}
			} else {
				resultHandle.setMsg("订单ID=" + orderId + ",不需生成合同。");
			}
		} else {
			resultHandle.setMsg("订单ID为null");
		}
		
		return resultHandle;
	}
	
	public ResultHandleT<String> getContractTemplateHtml(String templateCode){
		ResultHandleT<String> resultHandle = new ResultHandleT<String>();
		if(StringUtils.isNotEmpty(templateCode)){
			//委托服务协议
			if(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode().equals(templateCode)){
				resultHandle = commissionedServiceAgreementService.getContractTemplateHtml();
			}
			//目的地委托服务协议
			if(ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode().equals(templateCode)){
				resultHandle = destCommissionedServiceAgreementService.getContractTemplateHtml();
			}
			//北京一日游合同
			if(ELECTRONIC_CONTRACT_TEMPLATE.BEIJING_DAY_TOUR.getCode().equals(templateCode)){
				resultHandle = beijingDayTourContractService.getContractTemplateHtml();
			}
			//预付款协议
			if(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode().equals(templateCode)){
				resultHandle = advanceProductAgreementContractService.getContractTemplateHtml();
			}
			//团队出境旅游合同
			if(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_OUTBOUND_TOURISM.getCode().equals(templateCode)){
				resultHandle = teamOutboundTourismContractService.getContractTemplateHtml();
			}
			//团队境内旅游合同
			if(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode().equals(templateCode)){
				resultHandle = teamWithInTerritoryContractService.getContractTemplateHtml();
			}
			//浙江东港旅游合同
			if(ELECTRONIC_CONTRACT_TEMPLATE.DONGGANG_ZHEJIANG_CONTRACT.getCode().equals(templateCode)){
				resultHandle = teamDonggangZhejiangContractService.getContractTemplateHtml();
			}
			//产品预售协议
			if(ELECTRONIC_CONTRACT_TEMPLATE.PRESALE_AGREEMENT.getCode().equals(templateCode)){
				resultHandle = preSalesAgreementContractService.getContractTemplateHtml();
			}
			//台湾旅游协议
			if(ELECTRONIC_CONTRACT_TEMPLATE.TAIWAN_AGREEMENT.getCode().equals(templateCode)){
				resultHandle = taiwanTravelContractService.getContractTemplateHtml();
			}
			//上海邮轮旅游合同
			if(ELECTRONIC_CONTRACT_TEMPLATE.CRUISE_TOURISM_SHANGHAI.getCode().equals(templateCode)){
				resultHandle = cruiseTourismContractService.getContractTemplateHtml();
			}
			
			//康旅产品包合同
			if(ELECTRONIC_CONTRACT_TEMPLATE.FINANCE_CONTRACT.getCode().equals(templateCode)){
				resultHandle = financeContractService.getContractTemplateHtml();
			}
		}else{
			resultHandle.setMsg("OrderEcontractGeneratorService.getContractTemplateHtml: templateCode is null");
		}
		return resultHandle;
	}
	
	/**
	 * 部分合同添加产品内容
	 * @param templateCode
	 * @param productId
	 * @return
	 */
	public ResultHandleT<String> getContractTemplateHtml(String templateCode,Long productId){
		ResultHandleT<String> resultHandle = new ResultHandleT<String>();
		if(StringUtils.isNotEmpty(templateCode)){
			//委托服务协议
			if(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode().equals(templateCode)){
				resultHandle = commissionedServiceAgreementService.getContractTemplateHtml();
			}
			//目的地委托服务协议
			if(ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode().equals(templateCode)){
				resultHandle = destCommissionedServiceAgreementService.getContractTemplateHtml();
			}
			//北京一日游合同
			if(ELECTRONIC_CONTRACT_TEMPLATE.BEIJING_DAY_TOUR.getCode().equals(templateCode)){
				resultHandle = beijingDayTourContractService.getContractTemplateHtml();
			}
			//预付款协议
			if(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode().equals(templateCode)){
				resultHandle = advanceProductAgreementContractService.getContractTemplateHtml();
			}
			//团队出境旅游合同
			if(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_OUTBOUND_TOURISM.getCode().equals(templateCode)){
				resultHandle = teamOutboundTourismContractService.getContractTemplateHtml(productId);
			}
			//浙江东港旅游合同
			if(ELECTRONIC_CONTRACT_TEMPLATE.DONGGANG_ZHEJIANG_CONTRACT.getCode().equals(templateCode)){
				resultHandle = teamDonggangZhejiangContractService.getContractTemplateHtml(productId);
			}
			
			//团队境内旅游合同
			if(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode().equals(templateCode)){
				resultHandle = teamWithInTerritoryContractService.getContractTemplateHtml(productId);
			}
			//产品预售协议
			if(ELECTRONIC_CONTRACT_TEMPLATE.PRESALE_AGREEMENT.getCode().equals(templateCode)){
				resultHandle = preSalesAgreementContractService.getContractTemplateHtml();
			}
			//台湾旅游协议
			if(ELECTRONIC_CONTRACT_TEMPLATE.TAIWAN_AGREEMENT.getCode().equals(templateCode)){
				resultHandle = taiwanTravelContractService.getContractTemplateHtml();
			}
			//上海邮轮旅游合同
			if(ELECTRONIC_CONTRACT_TEMPLATE.CRUISE_TOURISM_SHANGHAI.getCode().equals(templateCode)){
				resultHandle = cruiseTourismContractService.getContractTemplateHtml();
			}
		}else{
			resultHandle.setMsg("OrderEcontractGeneratorService.getContractTemplateHtml: templateCode is null");
		}
		return resultHandle;
	}
	
}
