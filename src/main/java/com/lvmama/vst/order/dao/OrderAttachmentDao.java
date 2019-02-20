package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrderAttachment;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

/**
 * 订单附件业务数据库访问层
 * 
 * @author wenzhengtao
 *
 */
@Repository("orderAttachmentDao")
public class OrderAttachmentDao extends MyBatisDao{

	public OrderAttachmentDao() {
		super("ORD_ATTACHMENT");
	}

	public int deleteByPrimaryKey(Long ordAttachmentId){
		 return super.delete("deleteByPrimaryKey", ordAttachmentId);
	 }

    public int insert(OrderAttachment record){
    	return super.insert("insert", record);
    }

    public int insertSelective(OrderAttachment record){
    	return super.insert("insertSelective", record);
    }

    public OrderAttachment selectByPrimaryKey(Long ordAttachmentId){
    	return super.get("selectByPrimaryKey", ordAttachmentId);
    }

    public int updateByPrimaryKeySelective(OrderAttachment record){
    	return super.update("updateByPrimaryKeySelective", record);
    }

    public int updateByPrimaryKey(OrderAttachment record){
    	return super.update("updateByPrimaryKey", record);
    }
    
    /**
     * 根据订单ID查询附件记录
     * 
     * @param orderId
     * @return
     */
    public List<OrderAttachment> selectByOrderId(Long orderId){
    	return super.queryForList("selectByOrderId", orderId);
    }
    
    /**
	 * 根据订单ID统计附件数量
	 * @param orderId
	 * @return
	 */
    public Integer countByOrderId(Long orderId){
    	return super.get("countByOrderId", orderId);
    }
    
    /**
     * 通用查询方法
     * 
     * @param params
     * @return
     */
    public List<OrderAttachment> findOrderAttachmentByCondition(Map<String, Object> params) {
		return super.queryForList("findOrderAttachmentByCondition", params);
	}

    /**
     * 通用统计方法
     * 
     * @param params
     * @return
     */
	public Integer countOrderAttachmentByCondition(Map<String, Object> params) {
		return super.get("countOrderAttachmentByCondition", params);
	}
	
	/**
	 * @desc 订单附件有效状态更改
	 * @param param
	 * @return
	 */
	public int updateOrderAttachmentFlag(Map<String, Object> param){
		return super.update("updateOrderAttachmentFlag", param);
	}
}
