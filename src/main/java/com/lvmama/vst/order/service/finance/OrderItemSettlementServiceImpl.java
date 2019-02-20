package com.lvmama.vst.order.service.finance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lvmama.order.snapshot.comm.enums.Snapshot_Detail_Enum.SUPPGOODS_KEY;
import com.lvmama.vst.back.client.supp.service.SuppSettlementEntityClientService;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.supp.po.SuppSettlementEntities;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrderItemSettlementService;
import com.lvmama.vst.order.vo.OrderItemSettlementVo;

@Service("orderItemSettlementService")
public class OrderItemSettlementServiceImpl implements
		IOrderItemSettlementService {
	
	@Autowired
	protected IOrdOrderItemService OrdOrderItemService;
	
	@Autowired
	private SuppSettlementEntityClientService settlementEntityService;
	
	
	@Override
	public ResultHandleT<OrderItemSettlementVo> findSettlementCodeByOrderItemId(
			Long orderItemId) {
		ResultHandleT<OrderItemSettlementVo> resultHandle = new ResultHandleT<>();
		if (null == orderItemId) {
			resultHandle.setMsg("参数不能为空！");
			return resultHandle;
		}
		OrdOrderItem orderItem = OrdOrderItemService.selectOrderItemByOrderItemId(orderItemId);
		if (null == orderItem) {
			resultHandle.setMsg("不存在子订单！");
			return resultHandle;
		}
		try {
			OrderItemSettlementVo orderItemSettlement = new OrderItemSettlementVo();
			String settlementEntityCode = orderItem.getContentStringByKey(SUPPGOODS_KEY.settlementCode.name());
			String buyoutSettlementEntityCode = orderItem.getContentStringByKey(SUPPGOODS_KEY.buyoutSettlementCode.name());
			if (null != settlementEntityCode) {
				ResultHandleT<SuppSettlementEntities> result = settlementEntityService
						.findSuppSettlementEntityByCode(settlementEntityCode);
				if (result.isFail() || result.hasNull()) {
					resultHandle.setMsg("结算实体对象获取为null！");
				} else {
					SuppSettlementEntities suppSettlementEntities = result.getReturnContent();
					if (null != suppSettlementEntities.getId()) {
						settlementEntityCode = suppSettlementEntities.getId() + "_" + settlementEntityCode;
					} else {
						resultHandle.setMsg("结算实体对象id为null！");
					}

				}
			}
			if (null != buyoutSettlementEntityCode) {
				ResultHandleT<SuppSettlementEntities> result = settlementEntityService
						.findSuppSettlementEntityByCode(buyoutSettlementEntityCode);
				if (result.isFail() || result.hasNull()) {
					resultHandle.setMsg("买断结算实体对象获取为null！");
				} else {
					SuppSettlementEntities suppSettlementEntities = result.getReturnContent();
					if (null != suppSettlementEntities.getId()) {
						buyoutSettlementEntityCode = suppSettlementEntities.getId() + "_" + buyoutSettlementEntityCode;
					} else {
						resultHandle.setMsg("买断结算实体对象id为null！");
					}

				}
			}
			orderItemSettlement.setBuyoutSettlementEntityCode(buyoutSettlementEntityCode);
			orderItemSettlement.setSettlementEntityCode(settlementEntityCode);
			orderItemSettlement.setOrderItemId(orderItemId);
			resultHandle.setReturnContent(orderItemSettlement);
		} catch (BusinessException ex) {
			resultHandle.setMsg(ex);
		}
		return resultHandle;
	}

	
	
	
	
}
