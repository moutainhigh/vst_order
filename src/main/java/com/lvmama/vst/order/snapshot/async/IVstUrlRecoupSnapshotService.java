package com.lvmama.vst.order.snapshot.async;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.vo.ResultHandle;

/**
 * 快照URL补偿接口
 */
public interface IVstUrlRecoupSnapshotService {
	/**
	 * 通过对象key进行补偿-主单
	 * @param ordOrder
	 * @param key
	 * @param object 对象(产品/规格/商品/...)
	 * @return
	 * @throws
	 */
	public ResultHandle orderRecoupKeyByObject(OrdOrder ordOrder, String key, Object object) throws Exception;
	/**
	 * 通过对象key进行补偿-子单
	 * @param ordOrder
	 * @param ordOrderItem
	 * @param key
	 * @param object
	 * @return
	 * @throws
	 */
	public ResultHandle orderItemRecoupKeyByObject(OrdOrderItem ordOrderItem, String key, Object object) throws Exception;

	/**
	 * 通过源object,进行key补偿-主单
	 * @param ordOrder
	 * @param key
	 * @param object
	 * @return
	 * @throws
	 */
	public ResultHandle orderMongoRecoupKey(OrdOrder ordOrder, String key, Object object) throws Exception;
	/**
	 * 通过源object,进行key补偿-子单
	 * @param ordOrderItem
	 * @param key
	 * @param object
	 * @return
	 * @throws
	 */
	public ResultHandle orderItemmongoRecoupKey(OrdOrderItem ordOrderItem, String key, Object object) throws Exception;
}
