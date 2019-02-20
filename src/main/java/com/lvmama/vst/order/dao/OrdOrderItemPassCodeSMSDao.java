package com.lvmama.vst.order.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdOrderItemPassCodeSMS;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

/**
 * Created at 20185/9/1
 * @author yangzhenzhong
 *
 */

@Repository
public class OrdOrderItemPassCodeSMSDao extends MyBatisDao{

	public OrdOrderItemPassCodeSMSDao() {
		super("ORD_ORDER_ITEM_PASS_CODE_SMS");
		// TODO Auto-generated constructor stub
	}


	public int updateStatus(Long id){
		
		return super.update("updateStatusByPrimaryKey", id);
	}

	
	public int updateStatusByOrderId(Long orderId){
		
		return super.update("updateStatusByOrderId", orderId);
	}
	
	public int insert(OrdOrderItemPassCodeSMS ordOrderItemPassCodeSMS){
		
		return super.insert("insert", ordOrderItemPassCodeSMS);
	
	}
	
	public List<OrdOrderItemPassCodeSMS> queryByOrderId(Long orderId){
		
		return super.queryForList("selectByOrderId", orderId);
	}
	
	public List<OrdOrderItemPassCodeSMS> queryByStatus(String status){
		return super.queryForList("selectByStatus", status);
	}
	
	public List<OrdOrderItemPassCodeSMS> queryYesterdayByStatus(String status){
		
		return super.queryForList("selectYesterdayByStatus", status);
	}
	
	//根据orderId查询出passCodeId为0的标识数据条数
	public Integer queryCountOfFlagData(Long orderId){
		
		return super.get("selectCountOfFlagData",orderId);

	}
	
	public OrdOrderItemPassCodeSMS queryByOrderIdPassCodeId(Long orderId,Long passCodeId){
		return null;
	}
	
	
	public List<OrdOrderItemPassCodeSMS> queryByPassCodeId(List<Long> passCodeIdList){
		
		return super.queryForList("", passCodeIdList);
	}
	
}
