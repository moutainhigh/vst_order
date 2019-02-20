package com.lvmama.vst.order.dao.goods;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.goods.po.SuppGoodsShareDayLimit;
import com.lvmama.vst.comm.mybatis.MyBatisOraDao;

@Repository
public class SuppGoodsShareDayLimitOraDao extends MyBatisOraDao {

	public SuppGoodsShareDayLimitOraDao() {
		super("SUPP_GOODS_SHARE_DAY_LIMIT");
	}
	
	public SuppGoodsShareDayLimit selectByPrimaryKey(Long shareDayLimitId) {
		return super.get("selectByPrimaryKey", shareDayLimitId);
	}

    public List<SuppGoodsShareDayLimit> selectByLimitGroupId(Long limitGroupId) {
        return super.queryForList("selectByLimitGroupId", limitGroupId);
    }

    public List<SuppGoodsShareDayLimit> selectInLimitGroupIdList(List<Long> limitGroupIdList) {
        return super.queryForList("selectInLimitGroupIdList", limitGroupIdList);
    }

    public List<SuppGoodsShareDayLimit> selectLimitByParams(Long shareTotalStockGroupId, List<Long> suppGoodsIds) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("shareTotalStockGroupId", shareTotalStockGroupId);
        params.put("suppGoodsIds", suppGoodsIds);
        return super.queryForList("selectLimitByParams", params);
    }

    public List<SuppGoodsShareDayLimit> selectLimitByParams(Long shareTotalStockGroupId, Long suppGoodsId, Date specDate) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("shareTotalStockGroupId", shareTotalStockGroupId);
        params.put("specDate", specDate);
        params.put("suppGoodsId", suppGoodsId);
        return super.queryForList("selectLimitByParams", params);
    }

    public List<SuppGoodsShareDayLimit> selectLimitByParams(Long shareTotalStockGroupId, List<Long> suppGoodsIdList, Date specDate) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("shareTotalStockGroupId", shareTotalStockGroupId);
        params.put("specDate", specDate);
        params.put("suppGoodsIds", suppGoodsIdList);
        return super.queryForList("selectLimitByParams", params);
    }

    public List<SuppGoodsShareDayLimit> selectLimitByParams(List<Long> shareDayLimitIds) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("shareDayLimitIds", shareDayLimitIds);
        return super.queryForList("selectLimitByParams", params);
    }

    public List<SuppGoodsShareDayLimit> selectInLimitGroupIdListBetweenDays(List<Long> limitGroupIdList, Date beginDate, Date endDate) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("limitGroupIdList", limitGroupIdList);
        params.put("beginDate", beginDate);
        params.put("endDate", endDate);
        return super.queryForList("selectBetweenDays", params);
    }

    public List<SuppGoodsShareDayLimit> selectInGroupBetweenDays(Long limitGroupId, Date beginDate, Date endDate) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("limitGroupId", limitGroupId);
        params.put("beginDate", beginDate);
        params.put("endDate", endDate);
        return super.queryForList("selectBetweenDays", params);
    }

    public Integer deleteByLimitGroupId(Long limitGroupId) {
        return super.delete("deleteByLimitGroupId", limitGroupId);
    }

    public Integer deleteInLimitGroupIdList(List<Long> limitGroupIdList) {
        return super.delete("deleteInLimitGroupIdList", limitGroupIdList);
    }

    public Integer deleteByParams(List<Long> limitGroupIdList, Date beginDate, Date endDate) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("limitGroupIdList", limitGroupIdList);
        params.put("beginDate", beginDate);
        params.put("endDate", endDate);
        return super.delete("deleteByParams", params);
    }

    public Integer insertList(Long limitGroupId, Long initCount, List<Date> dateList) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("limitGroupId", limitGroupId);
        params.put("initCount", initCount);
        params.put("dateList", dateList);
        return super.insert("insertListByParams", params);
    }
    
    public int updateDayLimitForOrder(Long shareDayLimitId, Long stock) {
    	Map<String, Object> params = new HashMap<String, Object>();
        params.put("shareDayLimitId", shareDayLimitId);
        params.put("stock", stock);
    	return super.update("updateDayLimitForOrder", params);
    }

    public int updateCountBetweenDayByLimitGroupIds(List<Long> limitGroupIdList, Long count, Date beginDate, Date endDate) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("limitGroupIdList", limitGroupIdList);
        params.put("count", count);
        params.put("beginDate", beginDate);
        params.put("endDate", endDate);
        return super.update("updateCountBetweenDayByLimitGroupIds", params);
    }
}