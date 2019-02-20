package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import com.lvmama.vst.back.order.po.OrdOrderWifiTimeRate;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrdOrderWifiTimeRateDao extends MyBatisDao {

	public OrdOrderWifiTimeRateDao() {
		super("ORD_ORDER_WIFI_TIME_RATE");
	}

	public int deleteByPrimaryKey(Long wifiTimeRateId) {
		return super.delete("deleteByPrimaryKey", wifiTimeRateId);
	}

	public int insert(OrdOrderWifiTimeRate ordOrderWifiTimeRate) {
		return super.insert("insert", ordOrderWifiTimeRate);
	}

	public int insertSelective(OrdOrderWifiTimeRate ordOrderWifiTimeRate) {
		return super.insert("insertSelective", ordOrderWifiTimeRate);
	}

	public OrdOrderWifiTimeRate selectByPrimaryKey(Long wifiTimeRateId) {
		return super.get("selectByPrimaryKey", wifiTimeRateId);
	}

	public int updateByPrimaryKeySelective(OrdOrderWifiTimeRate ordOrderWifiTimeRate) {
		return super.update("updateByPrimaryKeySelective", ordOrderWifiTimeRate);
	}

	public int updateByPrimaryKey(OrdOrderWifiTimeRate ordOrderWifiTimeRate) {
		return super.update("updateByPrimaryKey", ordOrderWifiTimeRate);
	}
	
  public List<OrdOrderWifiTimeRate> selectByParam(Map<String, Object> params) {
    	return super.queryForList("selectByParams", params);
    }
}