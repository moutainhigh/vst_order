package com.lvmama.vst.order.snapshot.service;

import com.lvmama.order.snapshot.api.vo.ResponseBody;
import com.lvmama.order.snapshot.comm.vo.ProdProductBranchSnapshotVo;
import com.lvmama.order.snapshot.comm.vo.ProdProductSnapshotVo;
import com.lvmama.order.snapshot.comm.vo.SuppGoodsSnapshotVo;
import com.lvmama.order.snapshot.comm.vo.param.OrderItemParamVo;
import com.lvmama.order.snapshot.comm.vo.param.OrderParamVo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;

/**
 * 获取快照对象服务
 */
public interface IVstSnapshotParseService {
	/**
	 * 获取产品
	 * @param ordOrder
	 */
	public ResponseBody<ProdProductSnapshotVo> getProdProductSnapshot(OrdOrder ordOrder);

	public ResponseBody<ProdProductSnapshotVo> getProdProductSnapshot(OrdOrderItem ordOrderItem);
	/**
	 * 获取产品规格
	 * @param ordOrderItem
	 */
	public ResponseBody<ProdProductBranchSnapshotVo> getProdProductBranchSnapshot(OrdOrderItem ordOrderItem);
	/**
	 * 获取商品
	 * @param ordOrderItem
	 */
	public ResponseBody<SuppGoodsSnapshotVo> getSuppGoodsSnapshot(OrdOrderItem ordOrderItem);
}
