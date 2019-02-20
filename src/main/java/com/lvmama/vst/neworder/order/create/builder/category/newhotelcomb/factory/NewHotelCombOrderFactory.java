package com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory;

import com.google.common.base.Throwables;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.neworder.order.create.builder.category.IOrderDTOFactory;
import com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.product.AbstractDTOProduct;
import com.lvmama.vst.neworder.order.vo.BaseBuyInfo;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by dengcheng on 17/2/22.
 */
@Component("newHotelCombOrderFactory")
public class NewHotelCombOrderFactory implements IOrderDTOFactory<OrderHotelCombBuyInfo> {
	private static final Log LOG = LogFactory.getLog(NewHotelCombOrderFactory.class);
	@Resource(name = "hotelCombDTOProductService")
	AbstractDTOProduct dtoProductService;

	@Override
	public OrdOrderDTO buildDTO(BaseBuyInfo<OrderHotelCombBuyInfo> baseBuyInfo) {
		OrdOrderDTO order = null;
		LOG.info("NewHotelCombOrderFactory----buildDTO--start");
		try {
			OrderHotelCombBuyInfo buyInfo = baseBuyInfo.getT();
			 order = this.buildBaseDTO(baseBuyInfo);
			dtoProductService.buildOrderPaymentType(order, buyInfo);
			dtoProductService.buildOrderResourceConfirmStatus(order, buyInfo);
			dtoProductService.buildBonus(order, buyInfo);
			dtoProductService.buildPromotion(order, buyInfo);
			dtoProductService.buildRebate(order, buyInfo);
			dtoProductService.buildtravelContract(order, buyInfo);
			dtoProductService.buildManagerId(order, buyInfo);
			dtoProductService.buildOrderViewStatus(order, buyInfo);
			dtoProductService.buildMainWorkFlow(order, buyInfo);
			dtoProductService.checkTestOrder(order, buyInfo);
			LOG.info("NewHotelCombOrderFactory----buildDTO--end");
		} catch (Throwable t) {
			Throwables.propagate(t);
		} finally {
			LOG.info("=== remove queryThreadCache");
			AbstractDTOProduct.queryThreadCache.remove();
		}
		return order;
	}

	@Override
	public OrdOrderDTO buildBaseDTO(BaseBuyInfo<OrderHotelCombBuyInfo> baseBuyInfo) {
		OrdOrderDTO order = new OrdOrderDTO();
		LOG.info("NewHotelCombOrderFactory----buildBaseDTO--start");
		try {
			OrderHotelCombBuyInfo buyInfo = baseBuyInfo.getT();
			LOG.info("NewHotelCombOrderFactory----dbLoader--start");
			dtoProductService.dbLoader(buyInfo);
			LOG.info("NewHotelCombOrderFactory----dbLoader--end");
			
			dtoProductService.buildOrderHeader(order, buyInfo);
			LOG.info("NewHotelCombOrderFactory----buildOrderHeader--end");
			dtoProductService.buildOrderItem(order, buyInfo);
			LOG.info("NewHotelCombOrderFactory----buildOrderItem--end");
			dtoProductService.buildOrderPerson(order, buyInfo);
			LOG.info("NewHotelCombOrderFactory----buildOrderPerson--end");
			dtoProductService.buildOrderVisitTime(order, buyInfo);
			LOG.info("NewHotelCombOrderFactory----buildOrderVisitTime--end");
			dtoProductService.buildOrderCancelStrategy(order, buyInfo);
			LOG.info("NewHotelCombOrderFactory----buildOrderCancelStrategy--end");
			dtoProductService.buildOrderAmmount(order, buyInfo);
			LOG.info("NewHotelCombOrderFactory----buildOrderCancelStrategy--buildOrderAmmount");
			dtoProductService.buildOrderLatestPayedForWait(order, buyInfo);
			LOG.info("NewHotelCombOrderFactory----buildBaseDTO--end");
		} catch (Throwable t) {
			Throwables.propagate(t);
		} finally {
			LOG.info("=== remove queryThreadCache");
			AbstractDTOProduct.queryThreadCache.remove();
		}
		return order;
	}

}
