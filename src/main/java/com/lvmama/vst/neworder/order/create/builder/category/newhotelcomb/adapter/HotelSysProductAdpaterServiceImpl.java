package com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.adapter;

import com.google.common.base.Preconditions;
import com.lvmama.comm.bee.po.prod.ProdProduct;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombOrderService;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombProductService;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombBuyInfoVo;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombSuppGoodsTimePriceVo;
import com.lvmama.dest.api.hotelcomb.vo.TimePriceQueryVo;
import com.lvmama.dest.api.order.enums.OrderEnum;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombBuyInfoVo.GoodsItem;
import com.lvmama.dest.api.product.po.HotelBranchPo;
import com.lvmama.dest.api.product.vo.HotelBranchVo;
import com.lvmama.vst.back.biz.po.BizBranch;
import com.lvmama.vst.back.goods.dao.SuppGoodsNotimeTimePriceDao;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.goods.interfaces.IHotelGoodsQueryApiService;
import com.lvmama.dest.api.goods.vo.HotelGoodsBaseVo;
import com.lvmama.dest.api.product.interfaces.IHotelBranchQueryApiService;
import com.lvmama.dest.api.product.vo.HotelProductBranchBaseVo;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductBranchClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.neworder.order.OrderUtils;
import com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.NewHotelCombOrderFactory;
import com.lvmama.vst.neworder.order.vo.BaseTimePrice;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dengcheng on 17/3/21.
 */
@Component
public class HotelSysProductAdpaterServiceImpl implements IHotelSysProductAdpaterService {

    @Resource
    IHotelGoodsQueryApiService hotelGoodsQueryApiService;


    @Resource
    ProdProductBranchClientService prodProductBranchClientService;

    @Resource
    private SuppGoodsClientService suppGoodsClientService;

    @Resource(name="orderTicketNoTimePriceService")
    OrderTimePriceService orderTimePriceService;

    @Resource
    protected IHotelBranchQueryApiService hotelBranchQueryApiService;


    @Resource
    IHotelCombProductService hotelCombProductService;
    
	@Resource
	IHotelCombOrderService    hotelCombOrderService ;

	private static final Logger LOG = LoggerFactory
            .getLogger(HotelSysProductAdpaterServiceImpl.class);

    @Override
    public SuppGoods routerToSuppGoods(Long id, Long subCategoryId) {
        SuppGoods suppGoods = new SuppGoods();
        Preconditions.checkArgument(subCategoryId!=null,"subCategoryId not bean null");
        if(OrderUtils.isHotelComProduct(subCategoryId)){
            ResponseBody<HotelGoodsBaseVo> hotelGoodsBaseResponse = hotelGoodsQueryApiService.findSuppGoodsByGoodsId(new RequestBody<Long>().setTFlowStyle(id, NewOrderConstant.VST_ORDER_TOKEN));
            HotelGoodsBaseVo hotelGoodsBaseVo = hotelGoodsBaseResponse.getT();

            Preconditions.checkArgument(hotelGoodsBaseResponse.getT()!=null,"商品ID:"+id+"Not Found");


            HotelBranchPo hotelBranchPo = new HotelBranchPo();

            hotelBranchPo.setProductBranchId(hotelGoodsBaseResponse.getT().getProductBranchId());

            ResponseBody<HotelBranchVo> hotelBranchVoResponseBody = hotelBranchQueryApiService.findProdProductBranchById(new RequestBody<HotelBranchPo>().setTFlowStyle(hotelBranchPo,NewOrderConstant.VST_ORDER_TOKEN));

            Preconditions.checkNotNull(hotelBranchVoResponseBody.getT(),"ResponseBody<HotelBranchVo> not bean null");

            /**
             * begin
             * 将酒店子系统  HotelGoodsBaseVo  转化成vst 系统 SuppGoods
             */
            EnhanceBeanUtils.copyProperties(hotelGoodsBaseVo,suppGoods);

            ResponseBody<HotelProductBranchBaseVo>  hotelProductBranchBaseVoResponseBody =  hotelBranchQueryApiService.getProductBranchById(new RequestBody<Long>().setTFlowStyle(hotelGoodsBaseResponse.getT().getProductBranchId(),NewOrderConstant.VST_ORDER_TOKEN));

            Preconditions.checkArgument(hotelProductBranchBaseVoResponseBody.getT()!=null,"规格 %s Not Found",hotelGoodsBaseResponse.getT().getProductBranchId()+"");

            Preconditions.checkArgument(hotelProductBranchBaseVoResponseBody.getT().getHotelBranchVo()!=null,"getHotelBranchVo is null");


            ProdProductBranch  prodProductBranch = new ProdProductBranch();

            EnhanceBeanUtils.copyProperties(hotelProductBranchBaseVoResponseBody.getT(),prodProductBranch);

            suppGoods.setProdProductBranch(prodProductBranch);

//            HotelBranchVo hotelBranchVo = hotelGoodsBaseVo.getProductBranchBaseVo().getHotelBranchVo();

            BizBranch bizBranch = new BizBranch();

            EnhanceBeanUtils.copyProperties(hotelBranchVoResponseBody.getT(),bizBranch);

            suppGoods.getProdProductBranch().setBizBranch(bizBranch);


        } else {
            ResultHandleT<SuppGoods> handler =  suppGoodsClientService.findSuppGoodsById(id);
            Preconditions.checkNotNull(handler);
            Preconditions.checkNotNull(handler.getReturnContent(),"handler.getReturnContent() is null");
            ResultHandleT<ProdProductBranch> prodProductBranchResultHandleT = null;
            try {
                prodProductBranchResultHandleT = prodProductBranchClientService.findProdProductBranchById(handler.getReturnContent().getProductBranchId());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ProdProductBranch  prodProductBranch = prodProductBranchResultHandleT.getReturnContent();


            Preconditions.checkArgument(prodProductBranch!=null,"规格 %s Not Found",handler.getReturnContent().getProductBranchId()+"");

            Preconditions.checkArgument(prodProductBranch.getBizBranch()!=null,"prodProductBranch.getBizBranch() is null");

            suppGoods = handler.getReturnContent();
            suppGoods.setProdProductBranch(prodProductBranch);

        }
        return suppGoods;
    }

    @Override
    public BaseTimePrice routerToGoodsTimePrice(Long id,Long planId,Date specDate, Long subCategoryId,Long quantity) {
        BaseTimePrice btp = new BaseTimePrice();
        Preconditions.checkArgument(subCategoryId!=null,"subCategoryId not bean null");

        if(OrderUtils.isHotelComProduct(subCategoryId)){

            TimePriceQueryVo tpQ = new TimePriceQueryVo();
            tpQ.setGoodsId(id);

            tpQ.setPricePlanId(planId);
            tpQ.setCheckInDate(specDate);

            ResponseBody<List<HotelCombSuppGoodsTimePriceVo>> hotelCombSuppGoodsTimePriceVoResponseBody = hotelCombProductService.getHotelCombTimePrice(new RequestBody<TimePriceQueryVo>().setTFlowStyle(tpQ,NewOrderConstant.VST_ORDER_TOKEN));

            List<HotelCombSuppGoodsTimePriceVo> hotelCombSuppGoodsTimePriceVoList= hotelCombSuppGoodsTimePriceVoResponseBody.getT();

            Preconditions.checkArgument(!hotelCombSuppGoodsTimePriceVoList.isEmpty(),"商品%s无可售信息",id+"");
            HotelCombSuppGoodsTimePriceVo hotelCombPrice = hotelCombSuppGoodsTimePriceVoList.get(0);
            RequestBody  request  = new RequestBody();
            HotelCombBuyInfoVo hotelCombBuyInfoVo  = new HotelCombBuyInfoVo();
            List<GoodsItem> goodsList  = new ArrayList<GoodsItem>();
            HotelCombBuyInfoVo.GoodsItem goods = new HotelCombBuyInfoVo().new GoodsItem();
            goods.setCheckInDate(specDate);
            goods.setGoodsId(id);
            goods.setPricePlanId(planId);
            goods.setQuantity(quantity);
            goodsList.add(goods);
            
            hotelCombBuyInfoVo.setGoodsList(goodsList);
            request.setT(hotelCombBuyInfoVo);
            RequestBody<List<String>> response =hotelCombOrderService.calOrderResource(request) ;
            String statusResource = OrderEnum.RESOURCE_STATUS.AMPLE.name() ;
            for(String str:response.getT()){
               if(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(str)){
               	statusResource =OrderEnum.RESOURCE_STATUS.UNVERIFIED.name();
               	break ;
               }
            }
            LOG.info("====  OrderEnum.RESOURCE_STATUS.UNVERIFIED.name()statusResource====="+statusResource);
            btp.setResrouseStatus(statusResource);
            btp.setGoodsId(hotelCombPrice.getSuppGoodsId());
            btp.setSpecDate(hotelCombPrice.getSpecDate());
            btp.setPricePlanId(hotelCombPrice.getPricePlanId());
            btp.setSalePrice(hotelCombPrice.getSalePrice());
            btp.setAheadBookTime(hotelCombPrice.getAheadBookTime());
            btp.setSettmentPrice(hotelCombPrice.getSettlementPrice());


        } else {
            ResultHandleT<SuppGoodsBaseTimePrice> timePriceResultHandleT =  orderTimePriceService.getTimePrice(id,specDate,true);
            SuppGoodsBaseTimePrice timePrice = timePriceResultHandleT.getReturnContent();

            Preconditions.checkArgument(timePriceResultHandleT!=null,"商品%s无可售信息",id+"");
            Preconditions.checkArgument(timePriceResultHandleT.getReturnContent()!=null,"商品%s无可售信息",id+"");

            SuppGoodsNotimeTimePrice notimeTimePrice=(SuppGoodsNotimeTimePrice)timePrice;
           
            btp.setGoodsId(notimeTimePrice.getSuppGoodsId());
            btp.setSettmentPrice(notimeTimePrice.getSettlementPrice());
            btp.setSalePrice(notimeTimePrice.getPrice());
            btp.setAheadBookTime(notimeTimePrice.getAheadBookTime());
            btp.setSpecDate(notimeTimePrice.getSpecDate());

        }
        return btp;
    }


    @Override
    public ProdProduct findProductById(Long id, Long categoryId) {
        return null;
    }

    @Override
    public SuppGoodsTimePrice findGoodsTimePriceByParams(Long id, Date specDate, Long categoryId) {
        return null;
    }
}
