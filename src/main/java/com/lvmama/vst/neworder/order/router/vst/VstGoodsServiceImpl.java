package com.lvmama.vst.neworder.order.router.vst;

import com.google.common.base.Preconditions;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductBranchClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.neworder.order.router.IGoodsRouterService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by dengcheng on 17/4/26.
 */
@Component("vstSystemGoodsService")
public class VstGoodsServiceImpl implements IGoodsRouterService {

    @Resource
    ProdProductBranchClientService prodProductBranchClientService;


    @Resource
    private SuppGoodsClientService suppGoodsClientService;
    @Resource
    private ProdProductClientService  prodProductClientService;

    @Override
    public SuppGoods findGoodsById(Long id) {
        SuppGoods suppGoods = new SuppGoods();

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
        //增加查询产品 返回product 修改bug
        ResultHandleT<ProdProduct>    productResultHandleT =  prodProductClientService.findProdProductById(handler.getReturnContent().getProductId());
        
        ProdProduct prodProduct  = productResultHandleT.getReturnContent();
        Preconditions.checkArgument(prodProduct!=null,"产品 %s Not Found",handler.getReturnContent().getProductId()+"");
      
        suppGoods = handler.getReturnContent();
        suppGoods.setProdProductBranch(prodProductBranch);
        suppGoods.setProdProduct(prodProduct);
        return  suppGoods;
    }
}
