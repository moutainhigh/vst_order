package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.OrdPremUserRel;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import org.springframework.stereotype.Repository;

/**
 * 客服与用户DAO
 * @author Zhang.Wei
 */
@Repository
public class OrdPremUserRelDao extends MyBatisDao {

    public OrdPremUserRelDao() {
        super("ORD_PREM_USER_REL");
    }


    public int insert(OrdPremUserRel ordPremUserRel) {
 		return super.insert("insert", ordPremUserRel);
 	}

}
