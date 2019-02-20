/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.route;

import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.*;
import com.lvmama.vst.back.prod.vo.ProdLineRouteVO;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo.Product;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

/**
 * 机+酒品类的线路产品
 * @author lancey
 *
 */
@Component("autoPackRouteOrderPackInitBussiness")
public class AutoPackRouteOrderPackInitBussiness extends RouteOrderPackInitBussiness{
	
	private static final Log LOG = LogFactory.getLog(AutoPackRouteOrderPackInitBussiness.class);

	@Override
	public boolean initOrderPack(OrdOrderPackDTO pack, Product itemProduct) {
		if(itemProduct.getAdultQuantity()<1){
			throwIllegalException("组合产品成人数不可以为空");
		}
		pack.putContent(OrderEnum.ORDER_TICKET_TYPE.adult_quantity.name(), itemProduct.getAdultQuantity());//记录下单的成人数
		if(itemProduct.getChildQuantity()>0){
			pack.putContent(OrderEnum.ORDER_TICKET_TYPE.child_quantity.name(), itemProduct.getChildQuantity());//下单的儿童数
		}
		ProdProductParam param = new ProdProductParam();
		param.setLineRoute(true);
		ResultHandleT<ProdProduct> product=prodProductClientService.findLineProductByProductId(itemProduct.getProductId(), param);
		if(product.getReturnContent()!=null && product.getReturnContent().getProdLineRouteList()!=null && product.getReturnContent().getProdLineRouteList().size()>0)
		{
			ProdLineRouteVO route = product.getReturnContent().getProdLineRouteList().get(0);
			//解决多行程中取具体哪一行程问题
			if (pack.getOrder()!=null && pack.getOrder().getLineRouteId() != null) {
				for (ProdLineRouteVO lineRoute : product.getReturnContent().getProdLineRouteList()) {
					if (lineRoute.getLineRouteId().equals(pack.getOrder().getLineRouteId())) {
						route = lineRoute;
						break;
					}
				}
			}
			pack.putContent(OrderEnum.ORDER_PACK_TYPE.route_days.name(), route.getRouteNum());
			pack.putContent(OrderEnum.ORDER_PACK_TYPE.route_nights.name(), route.getStayNum());
		}

//		pack.putContent(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name(),pack.getProduct().getProductType());
		pack.putContent(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name(),product.getReturnContent().getProductType());
		pack.setOwnPack(product.getReturnContent().getPackageType());
		checkProductValid(product.getReturnContent(), itemProduct,pack);

		OrdAdditionStatus ordAdditionStatus = OrderUtils.makeOrdAdditionStatus(OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.name(),
				OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.NO_UPLOAD.name());
		pack.getOrder().addOrdAdditionStatus(ordAdditionStatus);
		return true;
	}

	@Override
	protected boolean checkProductValid(ProdProduct product,
			Product itemProduct, OrdOrderPackDTO pack) {
		return true;
	}

}
