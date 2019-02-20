package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdNoUploadNotice;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdNoUploadNoticeDao extends MyBatisDao {
	
	public OrdNoUploadNoticeDao() {
		super("ORD_NO_UPLOAD_NOTICE");
	}

	public int insertSelective(OrdNoUploadNotice notice) {
		return super.insert("insertSelective", notice);

	}
 
	
	public List<OrdNoUploadNotice> selectByParams(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
	
	public int deleteOrdNoUploadNotices (Map<String, Object> params){
		return super.delete("deleteOrdNoUploadNotices", params);
	}

}