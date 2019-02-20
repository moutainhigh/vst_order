package com.lvmama.vst.neworder.order.router;

import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;

import java.util.Date;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by dengcheng on 17/4/26.
 * 系统接口定位服务 子系统拆分后适用
 */
@Component("systemLookUpService")
public class LookUpServiceImpl implements ILookUpService,ApplicationContextAware {

    ApplicationContext applicationContext;

	@Autowired
	private ResPreControlService resControlBudgetRemote;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public IGoodsRouterService lookUpGoodsService(Long categoryId) {
        String beanName =NewOrderConstant.SYSTEM_GOODS_SERVICE_MAPPING.lookupBeanNameByCategoryId(categoryId);
        return (IGoodsRouterService) applicationContext.getBean(beanName);
    }

    @Override
    public ITimePriceRouterService lookUptTimePriceService(Long categoryId,Long goodsId,Date visitDate,boolean withBuyOutPrice) {
    	GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
		//如果能找到该有效预控的资源
    	boolean hasControled=false;
		hasControled = goodsResPrecontrolPolicyVO != null && goodsResPrecontrolPolicyVO.isControl() && withBuyOutPrice;
        String beanName = NewOrderConstant.SYSTEM_TIMEPRICE_SERVICE_MAPPING.lookupBeanNameByCategoryId(categoryId, hasControled);
        return (ITimePriceRouterService) applicationContext.getBean(beanName);
    }

    @Override
    public IProductRouterService lookUpProductService(Long categoryId) {
        return null;
    }


	@Override
	public OrderTimePriceService lookupTicketTimePrice(Long categoryId) {
        String beanName =NewOrderConstant.VSTTIKET_TIMEPRICE_SERVICE.lookupBeanNameByCategoryId(categoryId);
        return (OrderTimePriceService) applicationContext.getBean(beanName);
	}


}
