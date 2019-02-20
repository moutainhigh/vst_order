package com.lvmama.vst.order.snapshot.async;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.vo.ResultHandle;

/**
 * 快照消息补偿接口
 */
public interface IVstMsgRecoupSnapshotService {
	/**
	 * 产品补偿
	 * @param ordOrder
	 * @return
	 * @throws Exception
	 */
	public ResultHandle recoupProdProduct(OrdOrder ordOrder) throws Exception ;

	/**
	 * 产品补偿
	 * @param ordOrder
	 * @return
	 * @throws Exception
	 */
	public ResultHandle recoupSuppGoods(OrdOrder ordOrder) throws Exception ;
}
