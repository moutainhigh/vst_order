package com.lvmama.vst.neworder.order.create.builder.category;

import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.neworder.order.vo.BaseBuyInfo;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * Created by dengcheng on 17/2/22.
 */
public interface IOrderDTOFactory<T> {

    /**
     * 创建数据库持久临时对象
     * @return
     */
    OrdOrderDTO buildDTO(BaseBuyInfo<T>  buyInfo);
    
    
    OrdOrderDTO buildBaseDTO(BaseBuyInfo<T>  buyInfo);
    

}
