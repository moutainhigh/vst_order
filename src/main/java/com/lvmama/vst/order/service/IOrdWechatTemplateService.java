package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.OrdWechatTemplate;
import com.lvmama.vst.comm.web.BusinessException;

/**
 * 订单微信模板业务层
 * @author zhaomingzhu
 *
 */
public interface IOrdWechatTemplateService {
	
	/**
	 * 微信模板列表数量
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public int findOrdWechatTemplateCount(Map<String, Object> params) throws BusinessException;
	/**
	 * 微信模板列表
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public List<OrdWechatTemplate> findOrdWechatTemplateList(Map<String, Object> params) throws BusinessException;
	/**
	 * 微信模板信息
	 * @param id
	 * @return
	 * @throws BusinessException
	 */
	public OrdWechatTemplate findOrdWechatTemplateById(Long id) throws BusinessException;
	/**
	 * 增加微信模板
	 * @param ordSmsTemplate
	 * @return
	 * @throws BusinessException
	 */
	public int addOrdWechatTemplate(OrdWechatTemplate ordWechatTemplate) throws BusinessException;
	/**
	 * 修改微信模板
	 * @param ordSmsTemplate
	 * @return
	 * @throws BusinessException
	 */
	public int updateOrdWechatTemplate(OrdWechatTemplate ordWechatTemplate) throws BusinessException;
	/**
	 * 修改微信模板状态
	 * @param templateId
	 * @param state
	 * @return
	 * @throws BusinessException
	 */
	public int updateOrdWechatTemplateStatus(OrdWechatTemplate ordWechatTemplate) throws BusinessException;
	/**
	 * 删除微信模板
	 * @param templateId
	 * @return
	 * @throws BusinessException
	 */
	public int deleteOrdWechatTemplate(Long id) throws BusinessException;

}
