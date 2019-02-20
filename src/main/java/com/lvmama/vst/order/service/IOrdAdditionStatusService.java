package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdOrderPack;


/**
 * @author 张伟
 *
 */
public interface IOrdAdditionStatusService {

	
	public int addOrdAdditionStatus(OrdAdditionStatus ordAdditionStatus);
	
	public OrdAdditionStatus findOrdAdditionStatusById(Long id);
	
	public List<OrdAdditionStatus> findOrdAdditionStatusList(Map<String, Object> params);


	public int updateByPrimaryKeySelective(OrdAdditionStatus ordAdditionStatus);



	
	/**
	 * 上传出团通知书
	 * @param fileId
	 * @param fileName
	 * @param memo
	 * @param orderId
	 */
	public boolean saveNoticeRegiment(Long fileId, String fileName,String memo,
			Long orderId,String loginUserId);
	
	/**
	 * 发送出团通知书
	 * @param orderId
	 * @param email
	 * @return
	 */
	public boolean updateSendNoticeRegiment(Long orderId, String email,String loginUserId);
	
	
	/**
	 * 上传且立即发送
	 * @param fileId
	 * @param fileName
	 * @param memo
	 * @param orderId
	 * @param email
	 */
	public  boolean addUploadAndSendNoticeRegiment(Long fileId, String fileName,String memo, Long orderId, String email,String loginUserId) ;
	
	/**
	 * 出团通知短信发送
	 * @param fileId
	 * @param fileName
	 * @param memo
	 * @param orderId
	 * @param email
	 */
	public  boolean addSMSNoticeRegiment( Long orderId, String smsContent,String mobile,String loginUserId) ;
	
	/**
	 * 根据订单号查询出团通知书状态
	 * @param orderId
	 * @return
	 */
	public OrdAdditionStatus selectByOrderIdKey(Long orderId);
	
}
