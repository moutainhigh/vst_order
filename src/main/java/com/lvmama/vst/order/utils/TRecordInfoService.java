package com.lvmama.vst.order.utils;

import java.util.List;

import com.lvmama.comm.pet.vo.Page;
import com.lvmama.lvccweb.po.TRecordInfo;

public interface TRecordInfoService {
	//给定日期,查询最近10天的通话记录
	public Page<TRecordInfo> getNearest10RecordInfos(String currDateTime, String currCallerNo, Integer pageNo, Boolean isOnlyGetTotalRec);
	public List<TRecordInfo> getRecordInfosWithCallid(String callid, String createDateofCallid);
}
