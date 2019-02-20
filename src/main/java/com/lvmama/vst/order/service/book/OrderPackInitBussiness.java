/**
 * 
 */
package com.lvmama.vst.order.service.book;

import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;

/**
 * 打包数据初始化
 * @author lancey
 *
 */
public interface OrderPackInitBussiness {

	boolean initOrderPack(OrdOrderPackDTO pack,BuyInfo.Product itemProduct);
}
