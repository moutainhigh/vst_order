package com.lvmama.vst.order.snapshot.async;

import com.lvmama.order.snapshot.comm.enums.Snapshot_Detail_Enum;
import com.lvmama.order.snapshot.comm.factory.SnapshotVOFactory;
import com.lvmama.order.snapshot.comm.po.goods.SuppGoodsVo;
import com.lvmama.order.snapshot.comm.po.prod.ProdProductBranchVo;
import com.lvmama.order.snapshot.comm.po.prod.ProdProductVo;
import com.lvmama.order.snapshot.comm.vo.ProdProductBranchSnapshotVo;
import com.lvmama.order.snapshot.comm.vo.ProdProductSnapshotVo;
import com.lvmama.order.snapshot.comm.vo.SuppGoodsSnapshotVo;
import com.lvmama.order.snapshot.recoup.AsyncSnapshotComService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductBranchClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.snapshot.factory.SnapshotParamFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 异步确认快照公共服务
 */
@Component("asyncGatewaySnapshotComService")
public class AsyncGatewaySnapshotComService{
	@Resource
	private ProdProductClientService prodProductClientService;
	@Resource
	private ProdProductBranchClientService prodProductBranchClientService;
	@Resource
	private SuppGoodsClientService suppGoodsClientService;
	/**
	 * 加载对象
	 * @param key
	 * @param id
	 * @return
	 */
	public Object getObject(String key, Long id) throws Exception {
		if(key ==null || id ==null) {
			return null;
		}
		//加载对象
		Object object =null;
		if(Snapshot_Detail_Enum.PRODUCT_KEY.isProductKey(key)){
			object =newProdProductSnapshot(id);

		}else if(Snapshot_Detail_Enum.PRODUCT_BRANCH_KEY.isProductBranchKey(key)){
			object =newProdProductBranchSnapshot(id);

		}else if(Snapshot_Detail_Enum.SUPPGOODS_KEY.isSuppGoodsKey(key)){
			object =newSuppGoodsSnapshot(id);
		}
		return object;
	}
	/**
	 * 实例化产品PO
	 * @param id
	 * @return
	 */
	public ProdProductVo newProdProductSnapshot(Long id){
		ResultHandleT<ProdProduct> result =	prodProductClientService.findProdProductById(id);
		return SnapshotParamFactory.convertSnapshotProdProduct(result.getReturnContent());
	}
	/**
	 * 实例化产品规格PO
	 * @param id
	 * @return
	 */
	public ProdProductBranchVo newProdProductBranchSnapshot(Long id) throws Exception {
		ResultHandleT<ProdProductBranch> result =  prodProductBranchClientService.findProdProductBranchById(id);
		return SnapshotParamFactory.convertSnapshotProdProductBranch(result.getReturnContent());
	}
	/**
	 * 实例化商品PO
	 * @param id
	 * @return
	 */
	public SuppGoodsVo newSuppGoodsSnapshot(Long id){
		ResultHandleT<SuppGoods> result = suppGoodsClientService.findSuppGoodsById(id);
		return SnapshotParamFactory.convertSnapshotSuppGoods(result.getReturnContent());
	}
}
