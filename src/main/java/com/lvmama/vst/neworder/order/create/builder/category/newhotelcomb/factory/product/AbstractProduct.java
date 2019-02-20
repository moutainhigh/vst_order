package com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.product;

import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.dest.api.prodrefund.interfaces.IProdRefundService;
import com.lvmama.dest.api.product.interfaces.IHotelBranchQueryApiService;
import com.lvmama.dest.api.product.interfaces.IHotelProductQueryApiService;
import com.lvmama.dest.api.vst.prod.service.IHotelProdProductPropQueryVstApiService;
import com.lvmama.vst.back.client.biz.service.BranchClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.prod.service.ProdLineRouteClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsRefund;
import com.lvmama.vst.back.newHotelcomb.po.NewHotelCombTimePrice;
import com.lvmama.vst.back.newHotelcomb.service.INewHotelCombTimePriceService;
import com.lvmama.vst.back.order.exception.OrderException;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.back.prod.po.ProdRefundRule;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.neworder.order.create.builder.category.AbstractOrderBuilder;
import com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.adapter.IHotelSysProductAdpaterService;
import com.lvmama.vst.order.service.book.NewHotelComOrderBussiness;
import com.lvmama.vst.order.service.book.OrderOrderFactory;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import net.sf.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dengcheng on 17/2/23.
 */
public abstract class AbstractProduct {

}
