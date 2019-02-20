package com.lvmama.vst.order.web.apportion;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BaseActionSupport;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyanqun on 2017/6/1.
 */
@Controller
public class SomeManualAction extends BaseActionSupport {
    @Resource
    SuppGoodsHotelAdapterClientService suppGoodsHotelAdapterClientService;

    @RequestMapping("/suppGoodsHotelAdapter")
    public String querySuppGoodsWithProduct(Model model, String suppGoodsIds){
        List<Long> suppGoodsIdList = new ArrayList<>();
        if (StringUtils.isBlank(suppGoodsIds)) {
            model.addAttribute("resultMsg", "没有商品");
            return "/order/apportion/apportionResult";
        }
        String[] suppGoodsIdArray = suppGoodsIds.split(",");
        if (ArrayUtils.isEmpty(suppGoodsIdArray)) {
            model.addAttribute("resultMsg", "没有商品");
            return "/order/apportion/apportionResult";
        }
        for (String suppGoodsIdStr : suppGoodsIdArray) {
            if (StringUtils.isBlank(suppGoodsIdStr)) {
                continue;
            }
            Long suppGoodsId = Long.valueOf(suppGoodsIdStr);
            if (NumberUtils.isAboveZero(suppGoodsId)) {
                suppGoodsIdList.add(suppGoodsId);
            }
        }
        ResultHandleT<List<SuppGoods>> suppGoodsListWithProductResultHandle = suppGoodsHotelAdapterClientService.findSuppGoodsListWithProduct(suppGoodsIdList);
        List<Long> resultSuppGoodsId = new ArrayList<>();
        for (SuppGoods suppGoods : suppGoodsListWithProductResultHandle.getReturnContent()) {
            resultSuppGoodsId.add(suppGoods.getSuppGoodsId());
        }
        String resultMsg = GsonUtils.toJson(resultSuppGoodsId);
        model.addAttribute("resultMsg", resultMsg);
        return "/order/apportion/apportionResult";
    }
}
