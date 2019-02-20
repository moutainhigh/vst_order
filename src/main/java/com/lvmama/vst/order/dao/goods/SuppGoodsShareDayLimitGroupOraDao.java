package com.lvmama.vst.order.dao.goods;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.goods.po.SuppGoodsShareDayLimitGroup;
import com.lvmama.vst.comm.mybatis.MyBatisOraDao;

@Repository
public class SuppGoodsShareDayLimitGroupOraDao extends MyBatisOraDao {

	public SuppGoodsShareDayLimitGroupOraDao() {
		super("SG_SHARE_DAY_LIMIT_GROUP");
	}

    public List<SuppGoodsShareDayLimitGroup> selectBySuppGoodsIdList(List<Long> suppGoodsIdList) {
        return super.queryForList("selectBySuppGoodsIdList", suppGoodsIdList);
    }

    public List<SuppGoodsShareDayLimitGroup> selectByLimitGroupIdList(List<Long> limitGroupIdList) {
        return super.queryForList("selectByLimitGroupIdList", limitGroupIdList);
    }

    public List<SuppGoodsShareDayLimitGroup> selectByLimitGroupId(Long limitGroupId) {
        return super.queryForList("selectByLimitGroupId", limitGroupId);
    }
    
    public List<SuppGoodsShareDayLimitGroup> selectByCondition(Map<String, Object> params) {
        return super.queryForList("selectByCondition", params);
    }

	/**
	 * 根据主键删除
	 * @param limitGroupId 日限制组ID
	 * @return 删除
	 */
	public Integer deleteByLimitGroupId(Long limitGroupId) {
		return super.delete("deleteByLimitGroupId", limitGroupId);
	}

    public Integer deleteBySuppGoodsIds(List<Long> suppGoodsIds) {
        return super.delete("deleteBySuppGoodsIds", suppGoodsIds);
    }

    public Integer deleteInLimitGroupIdList(List<Long> limitGroupIdList) {
        return super.delete("deleteInLimitGroupIdList", limitGroupIdList);
    }
	public Integer insertList(Long limitGroupId, List<Long> suppGoodsIdList) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("limitGroupId", limitGroupId);
        params.put("suppGoodsIdList", suppGoodsIdList);
		return super.insert("insertList", params);
	}
}