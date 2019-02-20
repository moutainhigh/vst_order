/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.route;

import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.client.prod.service.ProdPackageGroupClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductBranchClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.vo.ProdLineRouteVO;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo.Product;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderPackInitBussiness;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;

/**
 * 线路的初始化,
 * 供应商提供的线路
 * @author lancey
 *
 */
public abstract class RouteOrderPackInitBussiness extends AbstractBookService implements OrderPackInitBussiness{
	/**
	 * 自动打包交通产品的属性键
	 * */
	protected static final String autoPackTrafficProductKey = "auto_pack_traffic";
	
	@Autowired
	protected ProdProductClientService prodProductClientService;
	
	@Autowired
	protected ProdPackageGroupClientService prodPackageGroupClientService;
	
	@Autowired
	protected ProdProductBranchClientService prodProductBranchClientRemote;
	
	@Autowired
	protected ProdProductClientService prodProductClientRemote;

	@Autowired
	protected SuppGoodsClientService suppGoodsClientService;

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
		if(product.getReturnContent()!=null && CollectionUtils.isNotEmpty(product.getReturnContent().getProdLineRouteList()))
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
		if(product.getReturnContent() != null && product.getReturnContent().getProducTourtType() != null){
			pack.putContent(OrderEnum.ORDER_ROUTE_TYPE.route_tour_type.name(), product.getReturnContent().getProducTourtType());
		}
		pack.setOwnPack(product.getReturnContent().getPackageType());
		checkProductValid(product.getReturnContent(), itemProduct,pack);
		
		OrdAdditionStatus ordAdditionStatus = OrderUtils.makeOrdAdditionStatus(OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.name(), 
				OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.NO_UPLOAD.name());
		pack.getOrder().addOrdAdditionStatus(ordAdditionStatus);
		return true;
	}
	
	protected abstract boolean checkProductValid(ProdProduct product,Product itemProduct,OrdOrderPackDTO pack);

	
}
