package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdTravAdditionConf;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
/**
 *目的地出游人补全表单配置数据库访问层
 * 
 * @author CHENHAO
 *
 */
@Repository("ordTravAdditionConfDAO")
public class OrdTravAdditionConfDAO extends MyBatisDao{
    
	public OrdTravAdditionConfDAO() {
		super("ORD_TRAV_ADDITION_CONF");
	}

	public int deleteByPrimaryKey(Long travAdditionConfId){
		return super.delete("deleteByPrimaryKey", travAdditionConfId);
	}

    public int insert(OrdTravAdditionConf record){
    	return super.insert("insert", record);
    }

    public OrdTravAdditionConf selectByPrimaryKey(Long travAdditionConfId){
    	return super.get("selectByPrimaryKey", travAdditionConfId);
    }
    
    public List<OrdTravAdditionConf> selectByParam(Map<String,Object> param){
        return super.queryForList("selectByParam", param);
    }

    public int updateByPrimaryKey(OrdTravAdditionConf record){
    	return super.update("updateByPrimaryKey", record);
    }
    
}