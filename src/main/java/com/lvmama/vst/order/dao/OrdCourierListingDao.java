package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdCourierListing;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdCourierListingDao extends MyBatisDao {

	public OrdCourierListingDao() {
		super("ORD_COURIER_LISTING");
	}

	public int deleteByPrimaryKey(Long OrdCourierListingId) {
		return super.delete("deleteByPrimaryKey", OrdCourierListingId);
	}

	public int insert(OrdCourierListing ordCourierListing) {
		return super.insert("insert", ordCourierListing);
	}

	public int insertSelective(OrdCourierListing ordCourierListing) {
		return super.insert("insertSelective", ordCourierListing);
	}

	public OrdCourierListing selectByPrimaryKey(Long ordCourierListingId) {
		return super.get("selectByPrimaryKey", ordCourierListingId);
	}

	public int updateByPrimaryKeySelective(OrdCourierListing ordCourierListing) {
		return super.update("updateByPrimaryKeySelective", ordCourierListing);
	}

	public int updateByPrimaryKey(OrdCourierListing ordCourierListing) {
		return super.update("updateByPrimaryKey", ordCourierListing);
	}

	public List<OrdCourierListing> findOrdCourierListingList(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}	
	
}