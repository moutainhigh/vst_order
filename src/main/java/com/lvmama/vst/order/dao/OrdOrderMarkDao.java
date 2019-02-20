package com.lvmama.vst.order.dao;


import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdOrderMark;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
@Repository
public class OrdOrderMarkDao extends MyBatisDao {
    public OrdOrderMarkDao() {
        super("ORD_ORDER_MARK");
    }

    public int saveOrdOrderMark(OrdOrderMark ordOrderMark){
        return super.insert("saveOrdOrderMark", ordOrderMark);
    }
    
    public int updateOrdOrderMark(OrdOrderMark ordOrderMark){
       return super.update("updateOrdOrderMark", ordOrderMark);
    }

    public OrdOrderMark findOrdOrderMarkByOrderId(Long orderId){
    	List<OrdOrderMark> markList = super.getList("findOrdOrderMarkByOrderId", orderId);
    	if(CollectionUtils.isEmpty(markList))
    		return null;
        return markList.get(0);
    }
}
