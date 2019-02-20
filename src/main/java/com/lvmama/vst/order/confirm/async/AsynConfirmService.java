package com.lvmama.vst.order.confirm.async;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.vo.ResultHandleT;

/**
 * 异步确认服务
 * @version 1.0
 */
public interface AsynConfirmService {
	/**
	 * 异步确认接口(api)
	 * @param resultHandel<OrdOrderItem>
	 */
	public void apiAsynConfirmOrder(ResultHandleT<OrdOrderItem> resultHandel);

}
