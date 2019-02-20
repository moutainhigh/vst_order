package com.lvmama.vst.order.dao;

import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.comm.vo.pass.EbkTicketPassVO;
import com.lvmama.vst.comm.vo.pass.EbkTicketPostVO;
import com.lvmama.vst.comm.vo.pass.EbkTicketStatisVO;
import com.lvmama.vst.ebooking.ebk.po.EbkOrdTransfer;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * EbkPassDao,持久层类 通关
 * 
 * @version 1.0
 * @since 1.0
 */
@Repository
public class EbkConnectsPassDao extends MyBatisDao {

	public EbkConnectsPassDao() {
		super("EBK_CONNECTS_PASS");
	}

	/**
	 * 根据条件查询EBK通关信息
	 * 
	 * @param params
	 *            查询条件
	 * @return 返回EBK通关对象集合
	 * @author
	 */
	public List<EbkTicketStatisVO> selectEbkPassStatisListByPrams(Map<String, Object> params) {
		return super.queryForList("selectEbkPassStatisListByPrams", params);
	}
	
	public Integer selectEbkPassStatisCount(Map<String, Object> params) {
		return super.get("selectEbkPassStatisCount", params);
	}
	
	/**
	 * 根据条件查询EBK通关信息
	 * 
	 * @param params
	 *            查询条件
	 * @return 返回EBK通关对象集合
	 * @author
	 */
	public List<EbkTicketPassVO> selectEbkPassListByPrams(Map<String, Object> params) {
		return super.queryForList("selectEbkPassListByPrams", params);
	}
	
	public Integer selectEbkPassListCount(Map<String, Object> params) {
		return super.get("selectEbkPassListCount", params);
	}
	
	public List<EbkTicketPassVO> selectEbkPassListForReportByPrams(Map<String, Object> params) {
		return super.queryForListForReport("selectEbkPassListByPrams", params);
	}
	
	/**
	 * 根据条件查询EBK中间商订单信息
	 * 
	 * @param params
	 *            查询条件
	 * @return 返回EBK中间商订单信息
	 * @author
	 */
	public List<EbkTicketPassVO> selectEbkMiddleListByPrams(Map<String, Object> params) {
		return super.queryForList("selectEbkMiddleListByPrams", params);
	}
	
	public Integer selectEbkMiddleListCount(Map<String, Object> params) {
		return super.get("selectEbkMiddleListCount", params);
	}

	public List<EbkTicketPassVO> selectEbkMiddleListForReportByPrams(Map<String, Object> params) {
		return super.queryForListForReport("selectEbkMiddleListByPrams", params);
	}
		
	/**
	 * 根据条件查询EBK邮寄订单信息
	 * 
	 * @param params
	 *            查询条件
	 * @return 返回EBK通关对象集合
	 * @author
	 */
	public List<EbkTicketPostVO> selectEbkPostListByPrams(Map<String, Object> params) {
		return super.queryForList("selectEbkPostListByPrams", params);
	}
	
	public Integer selectEbkPostListCount(Map<String, Object> params) {
		return super.get("selectEbkPostListCount", params);
	}
	
	public List<EbkTicketPostVO> selectEbkPostListForReportByPrams(Map<String, Object> params) {
		return super.queryForListForReport("selectEbkPostListByPrams", params);
	}

	public int transferOrder(Map<String, Object> params) {
		return super.insert("insertTransferOrder", params);
	}
		
	public List<EbkOrdTransfer> queryEbkOrdTransferList(Map<String, Object> params) {
		return super.queryForList("queryEbkOrdTransferList", params);
	}
	
	public void updateTransferOrder(Map<String, Object> params) {
		super.update("updateTransferOrder", params);
	}
}