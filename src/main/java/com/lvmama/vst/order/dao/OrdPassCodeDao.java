/**
 * 
 */
package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdPassCode;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

/**
 * @author lancey
 *
 */
@Repository
public class OrdPassCodeDao extends MyBatisDao {

	public OrdPassCodeDao() {
		super("ORD_PASS_CODE");
	}

	public void insert(OrdPassCode record){
		super.insert("insert", record);
	}
	
	
	public int selectCountByOrderItemId(final Long orderItemId){
		Integer result = super.get("selectCountByOrderItemId",orderItemId);
		if(result==null){
			return 0;
		}else{
			return result;
		}
	}
	
	public OrdPassCode getOrdPassCodeByOrderItemId(final Long orderItemId){
		List<OrdPassCode> ordPassCodeList =  super.queryForList("getOrdPassCodeByOrderItemId",orderItemId);
		if(CollectionUtils.isEmpty(ordPassCodeList)){
			return null;
		}
		return ordPassCodeList.get(0);
	}
	
	/**
	 * 根据通关点和辅助码查询
	 * @param orderItemId
	 * @param code
	 * @return
	 */
	public  List<OrdPassCode>  getOrdPassCodeByCheckInAndCode(Long checkInId,String code){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("checkingId", checkInId);
		params.put("addCode", code);
		return super.queryForList("getOrdPassCodeByCheckInAndCode",params);
	}
	
	/**
	 * 查询
	 * @param params
	 * @return
	 */
	public  List<OrdPassCode>  findByParams(Map<String,Object> params){
		return super.queryForList("selectByParams",params);
	}
	
	public int update(final OrdPassCode record){
		return super.update("update", record);
	}
	public int updatePicFilePath(final OrdPassCode record){
		return super.update("updatePicFilePath", record);
	}
	
	
}
