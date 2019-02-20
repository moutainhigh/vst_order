package com.lvmama.vst.order.service.book.util;

import org.apache.commons.lang3.ArrayUtils;

import com.lvmama.comm.utils.StringUtil;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.redis.JedisTemplate2;

public class ConfirmEnum {
	
	static{
		initTicketEnabled();
	}
	/**
	 * 初始化门票工作流开启状态(true/false)
	 */
	private static void initTicketEnabled(){
		setTicketByRedis("PC","true");
		setTicketByRedis("APP","true");
		setTicketByRedis("TNT","true");
	}
	
	/**
	 * 获取工作流状态
	 * @param type
	 * @return true/false
	 */
	public static String getTicketWorkEnabledStatus(String type){
		if("PC".equalsIgnoreCase(type)){
			return queryTicketByRedis(Constant.SCENIC_WORKFLOW_STATUS.scenic_ticket_workflow_pc_type_status.name());
		}else if("APP".equalsIgnoreCase(type)){
			return queryTicketByRedis(Constant.SCENIC_WORKFLOW_STATUS.scenic_ticket_workflow_app_type_status.name());
		}else if("TNT".equalsIgnoreCase(type)){
			return queryTicketByRedis(Constant.SCENIC_WORKFLOW_STATUS.scenic_ticket_workflow_tnt_type_status.name());
		}else{
			throw new RuntimeException("==error==type get value is null ==="+type+"===");
		}
		
	}
	
	/**
	 * 获取工作流状态
	 * @param type
	 * @return true/false
	 */
	public static String getTickeNewtWorkEnabledStatus(String type){
		if("PC".equalsIgnoreCase(type)){
			return queryTicketByRedis(Constant.SCENIC_WORKFLOW_STATUS.scenic_new_ticket_workflow_pc_type_status.name());
		}else if("APP".equalsIgnoreCase(type)){
			return queryTicketByRedis(Constant.SCENIC_WORKFLOW_STATUS.scenic_new_ticket_workflow_app_type_status.name());
		}else if("TNT".equalsIgnoreCase(type)){
			return queryTicketByRedis(Constant.SCENIC_WORKFLOW_STATUS.scenic_new_ticket_workflow_tnt_type_status.name());
		}else{
			throw new RuntimeException("==error==type get value is null ==="+type+"===");
		}
		
	}
	
	/**
	 * 通过redis获取门票工作流切换状态
	 * @param key
	 * @return
	 */
	public static String queryTicketByRedis(String key){
		JedisTemplate2 jedis=JedisTemplate2.getReaderInstance();
		return jedis.get(key);
	}
	/**
	 * 通过redis修改门票工作流切换状态
	 * @param key
	 * @return
	 */
	public static void setTicketByRedis(String type,String status){
		String key=null;
		if("PC".equalsIgnoreCase(type)){
			key=Constant.SCENIC_WORKFLOW_STATUS.scenic_new_ticket_workflow_pc_type_status.name();
		}else if("APP".equalsIgnoreCase(type)){
			key=Constant.SCENIC_WORKFLOW_STATUS.scenic_new_ticket_workflow_app_type_status.name();
		}else if("TNT".equalsIgnoreCase(type)){
			key=Constant.SCENIC_WORKFLOW_STATUS.scenic_new_ticket_workflow_tnt_type_status.name();
		}
		if(key==null){
			throw new RuntimeException("====key is null===");
		}
		JedisTemplate2 jedis=JedisTemplate2.getReaderInstance();
		jedis.set(key, status);
	}
	
	/**
	 * 判断是否进入门票工作流
	 * @param order
	 * @return
	 */
	public static boolean isTicketOrderNew(final OrdOrder order){
		if (isNewTicketWorkflowStart(order) && checkTicketOrder(order)) {
            return true;
        }
        return false;
	}
	
	/**
     * 门票 总开关是否开启
     * @return
     */
    public static boolean isNewTicketWorkflowStart(final OrdOrder order){
    	String statusEnabled="true";
    	Long distributionChannel = order.getDistributionChannel();
		if(distributionChannel == null){
			distributionChannel = 0L;
		}
		Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
		//DIST_FRONT_END，DIST_BACK_END，DIST_O2O_SELL，DIST_O2O_APP_SELL----pc
		if(Constant.DIST_FRONT_END == order.getDistributorId() 
			    ||Constant.DIST_BACK_END == order.getDistributorId()
			    ||Constant.DIST_O2O_SELL == order.getDistributorId()
			    ||Constant.DIST_O2O_APP_SELL == order.getDistributorId()){
			statusEnabled=getTickeNewtWorkEnabledStatus("PC");
		}else if(ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, distributionChannel.longValue())){
			//10000L,107L,108L,110L,10001L,10002L---app
			statusEnabled=getTickeNewtWorkEnabledStatus("APP");
		}else{
			//其他分销
			statusEnabled=getTickeNewtWorkEnabledStatus("TNT");
		}
		if(StringUtil.isEmptyString(statusEnabled)){
			statusEnabled="true";
		}
		return "true".equalsIgnoreCase(statusEnabled);
    }
    

    /**
     * 校验门票订单信息
     * @param order
     * @return
     */
    private static boolean checkTicketOrder(OrdOrder order){
        //门票,品类
        if (Constant.BU_NAME.TICKET_BU.getCode().equalsIgnoreCase(order.getBuCode())
                && (BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId() == order.getCategoryId()
                || BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId() == order.getCategoryId()
                || BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId() == order.getCategoryId())
                ) {
            return true;
        }
        return false;
    }
}
