package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.comm.po.ComFileMap;

@Repository
public class ComFileMapDAO extends MyBatisDao{

    public ComFileMapDAO() {
	super("COM_FILE_MAP");
	}

	public int deleteByPrimaryKey(Long comFileId) {
    	return super.delete("deleteByPrimaryKey", comFileId);
    }

    public int insert(ComFileMap record) {
    	return super.insert("insert", record);
    }

    public int insertSelective(ComFileMap record) {
    	return super.insert("insertSelective", record);
    }

    public ComFileMap selectByPrimaryKey(Long comFileId) {
    	return super.get("selectByPrimaryKey", comFileId);
    }

    public int updateByPrimaryKeySelective(ComFileMap record) {
    	return super.update("updateByPrimaryKeySelective", record);
    }

    public int updateByPrimaryKey(ComFileMap record) {
    	return super.update("updateByPrimaryKeySelective", record);
    }
    
    public List<ComFileMap> selectByParams(Map<String, Object> params) {
    	return super.queryForList("selectByParams", params);
    }
    
    public ComFileMap getByFileName(String fileName) {
    	Map<String, Object> params =new HashMap<String, Object>();
    	params.put("fileName", fileName);
    	return super.get("selectByParams", params);
    }
    
    public ComFileMap getByFileId(Long fileId) {
    	Map<String, Object> params =new HashMap<String, Object>();
    	params.put("fileId", fileId);
    	return super.get("selectByParams", params);
    }
}