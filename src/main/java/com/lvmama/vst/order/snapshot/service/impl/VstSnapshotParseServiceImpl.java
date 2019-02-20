package com.lvmama.vst.order.snapshot.service.impl;

import com.lvmama.order.snapshot.api.service.ISnapshotParseClientService;
import com.lvmama.order.snapshot.api.vo.ResponseBody;
import com.lvmama.order.snapshot.comm.vo.ProdProductBranchSnapshotVo;
import com.lvmama.order.snapshot.comm.vo.ProdProductSnapshotVo;
import com.lvmama.order.snapshot.comm.vo.SuppGoodsSnapshotVo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.snapshot.service.IVstSnapshotParseService;
import com.lvmama.vst.order.snapshot.factory.SnapshotParamFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class VstSnapshotParseServiceImpl implements IVstSnapshotParseService {
	@Resource
	private ISnapshotParseClientService snapshotParseClientService;

	@Override
	public ResponseBody<ProdProductSnapshotVo> getProdProductSnapshot(OrdOrder ordOrder) {
		return snapshotParseClientService.getProdProductSnapshot(SnapshotParamFactory.convertOrderParamVo(ordOrder));

	}

	@Override
	public ResponseBody<ProdProductSnapshotVo> getProdProductSnapshot(OrdOrderItem ordOrderItem) {
		return snapshotParseClientService.getProdProductSnapshot(SnapshotParamFactory.convertOrdOrderItem(ordOrderItem));
	}

	@Override
	public ResponseBody<ProdProductBranchSnapshotVo> getProdProductBranchSnapshot(OrdOrderItem ordOrderItem) {
		return snapshotParseClientService.getProdProductBranchSnapshot(SnapshotParamFactory.convertOrdOrderItem(ordOrderItem));
	}

	@Override
	public ResponseBody<SuppGoodsSnapshotVo> getSuppGoodsSnapshot(OrdOrderItem ordOrderItem) {
		return snapshotParseClientService.getSuppGoodsSnapshot(SnapshotParamFactory.convertOrdOrderItem(ordOrderItem));
	}
}
