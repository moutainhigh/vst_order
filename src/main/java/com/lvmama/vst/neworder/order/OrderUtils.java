package com.lvmama.vst.neworder.order;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.vo.ActivitiKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dengcheng on 17/2/21.
 */
public class OrderUtils {

    private static final Logger LOG = LoggerFactory
            .getLogger(OrderUtils.class);

    final static Long hotelComCategoryId = 32L;
    final static Long hotelCategoryId = 1L;


    public static boolean isHotelProduct(Long categoryId){
    	if(categoryId == null){
    		LOG.info("OrderUtils.isHotelProduct categoryId is null");
    		return false;
    	}
        return  categoryId.longValue()==categoryId.longValue();
    }

    public static boolean isHotelComProduct(Long subCategoryId){
    	if(subCategoryId == null){
    		LOG.info("OrderUtils.isHotelComProduct subCategoryId is null");
    		return false;
    	}
        return    hotelComCategoryId.longValue()==subCategoryId.longValue();
    }

}
