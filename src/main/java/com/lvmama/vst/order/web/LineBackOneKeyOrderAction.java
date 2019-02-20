package com.lvmama.vst.order.web;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.dist.service.DistDistributorProdClientService;
import com.lvmama.vst.back.client.ord.dto.OrdPersonQueryTO;
import com.lvmama.vst.back.client.ord.po.OrderRelatedPersonsVO;
import com.lvmama.vst.back.client.ord.service.OrderRelatedPersonClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.OrdOrderTravellerConfirmService;
import com.lvmama.vst.order.web.vo.LineBackOneKeyOrderInsuranceVo;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LineBackOneKeyOrderAction extends BaseActionSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(LineBackOneKeyOrderAction.class);
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private DistDistributorProdClientService distDistributorProdClientRemote;
	
	@Autowired
	private UserUserProxyAdapter userUserProxyAdapter;

	@Autowired
	private OrderRelatedPersonClientService orderRelatedPersonClientService;
    
    @Autowired
    private IOrdItemPersonRelationService ordItemPersonRelationService;
    
    @Autowired
    private OrdOrderTravellerConfirmService ordOrderTravellerConfirmService;
    
    @Autowired
    private IOrderLocalService orderLocalService;
	
	@RequestMapping(value = "/order/orderManage/loadOrderInsurance")
	@ResponseBody
	public Object loadOrderInsurance(Model model, HttpServletRequest request, HttpServletResponse resp,String orderId){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<loadOrderInsurance>  orderId:"+orderId);
		}
		Long lOrderId=NumberUtils.toLong(orderId);
		ResultHandleT<OrdOrder> result=orderService.loadOrderWithItemByOrderId(lOrderId);
		if(result==null || result.isFail() || result.hasNull()){
			ResultMessage rm=new ResultMessage(ResultMessage.ERROR, "订单不存在！");
			return rm;
		}
		OrdOrder order=result.getReturnContent();
		List<OrdOrderItem> orderItemList=order.getOrderItemList();
		ResultMessage rm=new ResultMessage(ResultMessage.SUCCESS,"");
		List<LineBackOneKeyOrderInsuranceVo> insuranceList=new ArrayList<LineBackOneKeyOrderInsuranceVo>();
		if(orderItemList!=null && orderItemList.size()>0){
			for (OrdOrderItem ordOrderItem : orderItemList) {
				insuranceList.add(convertLineBackOneKeyOrderInsuranceVo(ordOrderItem));
			}
		}
		rm.addObject("insurance", insuranceList);
		return rm;
	}
	
	private LineBackOneKeyOrderInsuranceVo convertLineBackOneKeyOrderInsuranceVo(OrdOrderItem ordOrderItem){
		LineBackOneKeyOrderInsuranceVo insurance=new LineBackOneKeyOrderInsuranceVo();
		insurance.setSuppGoodsId(ordOrderItem.getSuppGoodsId());
		insurance.setQuantity(ordOrderItem.getQuantity());
		return insurance;
	}
	
	@RequestMapping(value = "/order/orderManage/checkPrerequisite")
	@ResponseBody
	public Object checkPrerequisite(Model model, HttpServletRequest request, HttpServletResponse resp,String orderId){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<checkPrerequisite>  orderId:"+orderId);
		}
		Long lOrderId=NumberUtils.toLong(orderId);
		ResultHandleT<OrdOrder> result=orderService.loadOrderWithItemByOrderId(lOrderId);
		if(result==null || result.isFail() || result.hasNull()){
			ResultMessage rm=new ResultMessage(ResultMessage.ERROR, "订单不存在！");
			return rm;
		}
		OrdOrder order=result.getReturnContent();
		ResultHandleT<ProdProduct> resultProduct=prodProductClientService.findRealProdProductsByOrder(order);
		if(resultProduct==null || resultProduct.isFail() || resultProduct.hasNull()){
			ResultMessage rm=new ResultMessage(ResultMessage.ERROR, "产品不存在！");
			return rm;
		}
		ProdProduct prodProduct=resultProduct.getReturnContent();
		order.setProductId(prodProduct.getProductId());
		
		//品类是自由行、子品类是景+酒，不支持一键下单
		try {
			if(prodProduct != null && prodProduct.getBizCategory() != null 
					&& prodProduct.getBizCategory().getCategoryId() != null
					&& prodProduct.getSubCategoryId() != null) {
				long categoryId = prodProduct.getBizCategory().getCategoryId().longValue();
				long subCategoryId = prodProduct.getSubCategoryId().longValue();			
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==categoryId
					&& BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().longValue() == subCategoryId){
					ResultMessage rm=new ResultMessage(ResultMessage.ERROR, "酒+景产品不支持订单一键重下！");
					return rm;
				}
			}
		} catch (Exception e) {
			LOG.error("Deal with scene_hotel product error.", e);
		}
		Boolean isSupport=orderService.isOrderSupportOneKeyRecreate(order, prodProduct);
		if(isSupport==false){
			ResultMessage rm=new ResultMessage(ResultMessage.ERROR, "非线路产品的不支持订单一键重下！");
			return rm;
		}
		String saleFlag=prodProduct.getSaleFlag();
		if(StringUtils.equalsIgnoreCase("Y", saleFlag)==false){
			ResultMessage rm=new ResultMessage(ResultMessage.ERROR, "不可售商品不能下单！");
			return rm;
		}
		boolean checkBack;
		try {
			checkBack=verfiedProdDistributor(order.getProductId(), 2L);
		} catch (Exception e) {
			LOG.error("verfiedProdDistributor error ProductId:"+order.getProductId()+" "+ExceptionFormatUtil.getTrace(e));
			checkBack=false;
		}
		if(checkBack==false){
			ResultMessage rm=new ResultMessage(ResultMessage.ERROR, "该产品不能在驴妈妈后台销售！");
			return rm;
		}
		//通过前置条件检验
		UserUser user=userUserProxyAdapter.getUserUserByPk(order.getUserNo());
		if(user==null){
			ResultMessage rm=new ResultMessage(ResultMessage.ERROR, "下单用户不存在！");
			return rm;
		}
		ResultMessage rm=new ResultMessage(ResultMessage.SUCCESS,"检验成功");
		rm.addObject("user", user);
		return rm;
	}
	
	/**
     * 验证产品的销售渠道
     * @param productId
     * @return
     * @throws Exception
     */
    protected boolean verfiedProdDistributor(Long productId, Long distributorId) throws Exception{
        Map<String, Object> prodDistributorMap = new HashMap<String, Object>();
        prodDistributorMap.put("productId", productId);
        //prodDistributorMap.put("cancelFlag", "Y");
        prodDistributorMap.put("distributorId", distributorId);
        ResultHandle resultHandle = distDistributorProdClientRemote.verfiedProdDistributor(prodDistributorMap);
        return resultHandle.isSuccess();
    }
    
    /**
     * 一键下单加载订单相关人员
     * 
     * @param model
     * @param request
     * @param resp
     * @param orderId
     * @return
     */
    @RequestMapping(value = "/ord/order/lineBackLoadOriginalOrderPersons")
    @ResponseBody
    public Object lineBackLoadOriginalOrderPersons(Model model, HttpServletRequest request, HttpServletResponse resp,
            String originalOrderId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("start method<lineBackLoadOriginalOrderPersons>  orderId:" + originalOrderId);
        }

        ResultMessage rm=new ResultMessage(ResultMessage.SUCCESS,"获取成功");

        Long orderId = NumberUtils.toLong(originalOrderId);
        OrdPersonQueryTO ordPersonQueryTO = new OrdPersonQueryTO();
        ordPersonQueryTO.setOrderId(orderId);
        ResultHandleT<OrderRelatedPersonsVO> personResult = orderRelatedPersonClientService.loadOrderRelatedPersons(ordPersonQueryTO);

        OrdOrderTravellerConfirm confirm=ordOrderTravellerConfirmService.selectSingleByOrderId(orderId);
        if(confirm!=null){
        	rm.addObject("comfirm", confirm);
        }
        if (personResult == null||personResult.isFail()) {
            LOG.error("Error get related persons for order " + originalOrderId);
            rm.setCode(ResultMessage.ERROR);
            rm.setMessage("获取失败！");
        } else {
            rm.addObject("orderRelatedPersonsVO", personResult.getReturnContent());
            HashMap<String, ArrayList<String>> goodPersonRelationMap = ordItemPersonRelationService.findPersonGoodRelationByOrderId(originalOrderId);
            rm.addObject("goodPersonRelationMap", goodPersonRelationMap);
        }
       
        return rm;
    }
    
    
    /**
     * 获取快递信息
     * @param model
     * @param request
     * @param resp
     * @param originalOrderId
     * @return
     */
    @RequestMapping(value = "/ord/order/lineBackLoadExpress")
    @ResponseBody
    public Object lineBackLoadExpress(Model model, HttpServletRequest request, HttpServletResponse resp,
            String originalOrderId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("start method<lineBackLoadExpress>  orderId:" + originalOrderId);
        }

        ResultMessage rm = new ResultMessage(ResultMessage.SUCCESS, "获取成功");

        Long orderId = NumberUtils.toLong(originalOrderId);

        OrdOrder order = orderLocalService.findExpressAddress(orderId);
        if (order == null) {
            rm.addObject("ordAddress", new OrdAddress());
            rm.addObject("addressPerson", new OrdPerson());
        } else {
            rm.addObject("ordAddress", order.getOrdAddress());
            rm.addObject("addressPerson", order.getAddressPerson());
        }

        return rm;
    }
}
