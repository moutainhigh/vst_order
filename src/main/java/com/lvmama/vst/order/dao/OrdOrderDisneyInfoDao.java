package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.OrdOrderDisneyInfo;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by luoweiyi on 2016/3/4.
 */
@Repository
public class OrdOrderDisneyInfoDao extends MyBatisDao{

    public OrdOrderDisneyInfoDao(){
        super("ORD_ORDER_DISNEY_INFO");
    }

    public List<OrdOrderDisneyInfo> selectByParams(Map<String,Object> params){
        return super.getList("selectByParams",params);
    }

    public Integer insert(OrdOrderDisneyInfo ordOrderDisneyInfo){
        return super.insert("insert",ordOrderDisneyInfo);
    }
}
