package com.lvmama.vst.neworder.order.router.hotel;

import com.google.common.base.Preconditions;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.goods.interfaces.IHotelGoodsQueryApiService;
import com.lvmama.dest.api.goods.vo.HotelGoodsBaseVo;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombOrderService;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombProductService;
import com.lvmama.dest.api.product.interfaces.IHotelBranchQueryApiService;
import com.lvmama.dest.api.product.interfaces.IHotelProductQueryApiService;
import com.lvmama.dest.api.product.po.HotelBranchPo;
import com.lvmama.dest.api.product.vo.HotelBranchVo;
import com.lvmama.dest.api.product.vo.HotelProductBranchBaseVo;
import com.lvmama.dest.api.product.vo.HotelProductVo;
import com.lvmama.vst.back.biz.po.BizBranch;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductBranchClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.neworder.order.router.IGoodsRouterService;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

/**
 * Created by dengcheng on 17/4/26.
 */
@Component("HotelSystemGoodsService")
public class HotelGoodsServiceImpl implements IGoodsRouterService {

    @Resource
    IHotelGoodsQueryApiService hotelGoodsQueryApiService;


    @Resource
    ProdProductBranchClientService prodProductBranchClientService;


     @Resource
     IHotelProductQueryApiService  hotelProductQueryApiService;
    @Resource
    protected IHotelBranchQueryApiService hotelBranchQueryApiService;


    @Resource
    IHotelCombProductService hotelCombProductService;

    @Resource
    IHotelCombOrderService hotelCombOrderService;

    @Override
    public SuppGoods findGoodsById(Long id) {
        SuppGoods suppGoods = new SuppGoods();
        ResponseBody<HotelGoodsBaseVo> hotelGoodsBaseResponse = hotelGoodsQueryApiService.findSuppGoodsByGoodsId(new RequestBody<Long>().setTFlowStyle(id, NewOrderConstant.VST_ORDER_TOKEN));
        HotelGoodsBaseVo hotelGoodsBaseVo = hotelGoodsBaseResponse.getT();

        Preconditions.checkArgument(hotelGoodsBaseResponse.getT()!=null,"商品ID:"+id+"Not Found");


        HotelBranchPo hotelBranchPo = new HotelBranchPo();

        hotelBranchPo.setProductBranchId(hotelGoodsBaseResponse.getT().getProductBranchId());

      //  ResponseBody<HotelBranchVo> hotelBranchVoResponseBody = hotelBranchQueryApiService.findProdProductBranchById(new RequestBody<HotelBranchPo>().setTFlowStyle(hotelBranchPo,NewOrderConstant.VST_ORDER_TOKEN));

      //  Preconditions.checkNotNull(hotelBranchVoResponseBody.getT(),"ResponseBody<HotelBranchVo> not bean null");

        /**
         * begin
         * 将酒店子系统  HotelGoodsBaseVo  转化成vst 系统 SuppGoods
         */
        EnhanceBeanUtils.copyProperties(hotelGoodsBaseVo,suppGoods);

        ResponseBody<HotelProductBranchBaseVo>  hotelProductBranchBaseVoResponseBody =  hotelBranchQueryApiService.getProductBranchById(new RequestBody<Long>().setTFlowStyle(hotelGoodsBaseResponse.getT().getProductBranchId(),NewOrderConstant.VST_ORDER_TOKEN));

        Preconditions.checkArgument(hotelProductBranchBaseVoResponseBody.getT()!=null,"规格 %s Not Found",hotelGoodsBaseResponse.getT().getProductBranchId()+"");

        Preconditions.checkArgument(hotelProductBranchBaseVoResponseBody.getT().getHotelBranchVo()!=null,"getHotelBranchVo is null");


        ProdProductBranch prodProductBranch = new ProdProductBranch();

        EnhanceBeanUtils.copyProperties(hotelProductBranchBaseVoResponseBody.getT(),prodProductBranch);
        ResponseBody<HotelProductVo>  hotelProductRequestBaseVoResponseBody = hotelProductQueryApiService.findProductDetail(new RequestBody<Long>().setTFlowStyle(hotelGoodsBaseResponse.getT().getProductId(),NewOrderConstant.VST_ORDER_TOKEN));
        Preconditions.checkArgument(hotelProductRequestBaseVoResponseBody.getT()!=null,"hotelProductVo is null");
        ProdProduct prodProduct = new ProdProduct();
        BizCategory  bizCategory = new  BizCategory();
    	Map<String, String> prodProductPropMap = new HashMap<String,String>();	
        suppGoods.setProdProductBranch(prodProductBranch);
        EnhanceBeanUtils.copyProperties(hotelProductRequestBaseVoResponseBody.getT(),prodProduct);
        EnhanceBeanUtils.copyProperties(hotelProductRequestBaseVoResponseBody.getT().getBizCategory(),bizCategory);
   //     EnhanceBeanUtils.copyProperties(hotelProductRequestBaseVoResponseBody.getT().getProdProductPropMap(),prodProductPropMap);
        prodProduct.setBizCategory(bizCategory);
  //      prodProduct.setProdProductPropMap(prodProductPropMap);
//            HotelBranchVo hotelBranchVo = hotelGoodsBaseVo.getProductBranchBaseVo().getHotelBranchVo();
        prodProduct.setPropValue(hotelProductRequestBaseVoResponseBody.getT().getPropValueMap());
        
        BizBranch bizBranch = new BizBranch();
        bizBranch.setAttachFlag("Y");
        bizBranch.setBranchCode("product_branch");
        bizBranch.setBranchName("产品规格");
        bizBranch.setCancelFlag("Y");
   //     EnhanceBeanUtils.copyProperties(hotelBranchVoResponseBody.getT(),bizBranch);
        suppGoods.setProdProduct(prodProduct);
        suppGoods.getProdProductBranch().setBizBranch(bizBranch);
        return  suppGoods;
    }
}
