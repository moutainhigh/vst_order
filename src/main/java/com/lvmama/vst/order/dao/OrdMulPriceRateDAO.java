package com.lvmama.vst.order.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

/**
 * 
 * @author sunjian
 *
 */
@Repository
public class OrdMulPriceRateDAO extends MyBatisDao {

	public OrdMulPriceRateDAO() {
		super("ORD_MUL_PRICE_RATE");
	}

	public int deleteByPrimaryKey(Long ordMulPriceRateId) {
    	return super.delete("deleteByPrimaryKey", ordMulPriceRateId);
    }

    public int insert(OrdMulPriceRate record) {
    	record.setUpdateTime(new Date());
    	return super.insert("insert", record);
    }

    public int insertSelective(OrdMulPriceRate record) {
    	record.setUpdateTime(new Date());
    	return super.insert("insertSelective", record);
    }

    public OrdMulPriceRate selectByPrimaryKey(Long ordMulPriceRateId) {
    	return super.get("selectByPrimaryKey", ordMulPriceRateId);
    }

	public List<OrdMulPriceRate> selectByParams(Map<String, Object> params) {
		return super.queryForList("selectByParams", params);
	}
	
    public int updateByPrimaryKeySelective(OrdMulPriceRate record) {
    	record.setUpdateTime(new Date());
    	return super.update("updateByPrimaryKeySelective", record);
    }

    public int updateByPrimaryKey(OrdMulPriceRate record) {
    	record.setUpdateTime(new Date());
    	return super.update("updateByPrimaryKey", record);
    }
}
