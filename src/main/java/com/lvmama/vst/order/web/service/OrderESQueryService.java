package com.lvmama.vst.order.web.service;

import java.util.List;
import com.lvmama.order.search.vo.LvoOrderVo;
import com.lvmama.order.search.vo.PageVo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;

public interface OrderESQueryService {

	
	PageVo<LvoOrderVo> queryOrderListFromES(Integer page, Integer pageSize, OrderMonitorCnd monitorCnd);
	
	void saveOrderList(List<OrdOrder> orderList);
	
	List<OrdOrder> copyList(List<LvoOrderVo> lvoOrderVoList);
	
}
