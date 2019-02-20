package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.order.vo.OrdOrderMarkVo;

@Repository
public class OrdOrderMarkReDao extends MyBatisDao {

    public OrdOrderMarkReDao() {
        super("ORD_ORDER_MARK_RE");
    }

    public List<OrdOrderMarkVo> findOrdOrderMarkResByParams(Map<String, Object> params) {
        return super.queryForList("findOrdOrderMarkResByParams", params);
    }

    public Integer getTotalCount(Map<String, Object> params) {
        return super.get("getTotalCount", params);
    }
}
