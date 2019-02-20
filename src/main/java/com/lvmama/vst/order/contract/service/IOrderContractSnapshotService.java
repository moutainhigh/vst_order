package com.lvmama.vst.order.contract.service;

import java.util.Map;

import com.lvmama.vst.back.order.po.OrdContractSnapshotData;

/**
 * @author jswangxiaowei
 *
 */
public interface IOrderContractSnapshotService {

	public int saveContractSnapshot(OrdContractSnapshotData ordContractSnapshotData, String operatorName);

	public OrdContractSnapshotData selectByParam(Map<String, Object> params);

}
