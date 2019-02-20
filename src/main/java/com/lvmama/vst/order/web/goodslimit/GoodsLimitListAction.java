package com.lvmama.vst.order.web.goodslimit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.lvmama.comm.utils.MiscUtils;
import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.vst.back.client.goods.service.SuppGoodsBlackListService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;

import com.lvmama.vst.back.goods.po.SuppGoodsEditPerm;
import com.lvmama.vst.back.goods.service.SuppGoodsEditPermRemoteService;

import com.lvmama.vst.back.prom.po.PromForbidBuy;
import com.lvmama.vst.back.pub.po.ComIncreament;
import com.lvmama.vst.back.pub.po.ComPush;
import com.lvmama.vst.back.pub.service.PushAdapterServiceRemote;
import com.lvmama.vst.back.supp.po.SuppGoodsBlackList;
import com.lvmama.vst.back.supp.po.SuppGoodsIDCardLimit;
import com.lvmama.vst.back.supp.po.SuppGoodsLimit;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.prom.service.PromForbidBuyRemoteService;

@Controller
@RequestMapping("/goods/goodsLimit")
public class GoodsLimitListAction extends BaseActionSupport {

	private static final long serialVersionUID = 2954141550193852185L;

	private static final Log LOG = LogFactory.getLog(GoodsLimitListAction.class);
	@Autowired
	private SuppGoodsBlackListService blackListService;

	@Resource
	private PushAdapterServiceRemote pushAdapterServiceRemote;
	
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;

	@Autowired
	private SuppGoodsEditPermRemoteService suppGoodsEditPermService;

	@Autowired
	private PromForbidBuyRemoteService promForbidBuyRemoteService;

	@Autowired
	private ComLogClientService comLogService;

	@Autowired
	private SuppGoodsClientService suppGoodsService;

	@RequestMapping(value = "/showBlackList")
	public String getBlackList(Model model,Long goodId,Long categoryId,HttpServletRequest req) throws BusinessException {
		
		SuppGoodsLimit limit = new SuppGoodsLimit();
		limit = blackListService.findDBVisitTimeLimitList(goodId);
		List<Long> blacklistNumList = new ArrayList<Long>();
		for(int i=1;i<11;i++){
			blacklistNumList.add(Long.valueOf(i));
		}
		fillShowCircusFlag(model);
		fillModelForForbidBuyInfo(model, goodId);
		if(null == categoryId){
			ResultHandleT<SuppGoods> result = suppGoodsClientService.findSuppGoodsForOrder(goodId);
			SuppGoods suppGoods = result.getReturnContent();
			categoryId = suppGoods.getCategoryId();
		}
        model.addAttribute("limit", limit);
		model.addAttribute("goodId", goodId);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("blacklistNumList", blacklistNumList);
		return "order/ticket/goodslimit/showBlackList";
	}

	private void fillModelForForbidBuyInfo(Model model, Long goodId) {
		Map<String, Object> prodGoodsParams = new HashMap<String, Object>();
		prodGoodsParams.put("objectType", "GOODS");
		prodGoodsParams.put("objectId", goodId);
		List<PromForbidBuy> promForbidBuyList = promForbidBuyRemoteService.findPromForbidBuyByParams(prodGoodsParams);
		if (promForbidBuyList != null && promForbidBuyList.size() > 0) {
			model.addAttribute("promForbidBuyFlag", "true");
			model.addAttribute("promForbidBuyId", promForbidBuyList.get(0).getPromForbidBuyId());
		} else {
			List<Long> productIds = suppGoodsClientService.findProductIds(goodId);
			if (productIds != null && !productIds.isEmpty()) {
				if (productIds.size() == 1) {
					fillModelForListSiseIsZero(model, prodGoodsParams, productIds);
				} else if (productIds.size() > 1) {
					List<Long> hasForbidBuyProductIds = promForbidBuyRemoteService.findPromForbidBuyByProductIds(productIds);
					if (hasForbidBuyProductIds!=null && hasForbidBuyProductIds.size() == 1) {
						fillModelForListSiseIsZero(model, prodGoodsParams, hasForbidBuyProductIds);
					} else if (hasForbidBuyProductIds!=null && hasForbidBuyProductIds.size() > 1) {
						model.addAttribute("hasForbidBuyProductIds", hasForbidBuyProductIds.toString());
					}
				}
			}
		}
	}

	private void fillModelForListSiseIsZero(Model model, Map<String, Object> prodGoodsParams, List<Long> hasForbidBuyProductIds) {
		prodGoodsParams.put("objectType", "PRODUCT");
		prodGoodsParams.put("objectId", hasForbidBuyProductIds.get(0));
		List<PromForbidBuy> promForbidBuyList2 = promForbidBuyRemoteService.findPromForbidBuyByParams(prodGoodsParams);
		if(CollectionUtils.isNotEmpty(promForbidBuyList2)) {
			model.addAttribute("promForbidBuyFlag", "true");
			model.addAttribute("promForbidBuyId", promForbidBuyList2.get(0).getPromForbidBuyId());
		}
	}

	@RequestMapping(value = "/showPhoneList.do")
	public String getPhoneList(Model model,Long goodId,HttpServletRequest req) throws BusinessException {
		ResultHandleT<SuppGoods> result = suppGoodsClientService.findSuppGoodsForOrder(goodId);
		SuppGoods suppGoods = result.getReturnContent();
		Long categoryId = suppGoods.getCategoryId();
		String promForbidBuyId = req.getParameter("promForbidBuyId");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("goodId", goodId);
		params.put("blacklistType", "PHONE");
		List<SuppGoodsBlackList> list = blackListService.findPhoneList(params);
		model.addAttribute("list", list);
		model.addAttribute("goodId", goodId);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("promForbidBuyId",promForbidBuyId);
		return "order/ticket/goodslimit/showPhoneList";
	}
	
	@RequestMapping(value = "/showIDCardList.do")
	public String getIDCardList(Model model,Long goodId,HttpServletRequest req) throws BusinessException {
		ResultHandleT<SuppGoods> result = suppGoodsClientService.findSuppGoodsForOrder(goodId);
		SuppGoods suppGoods = result.getReturnContent();
		Long categoryId = suppGoods.getCategoryId();
		String promForbidBuyId = req.getParameter("promForbidBuyId");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("goodId", goodId);
		params.put("blacklistType", "IDCARD");
		List<SuppGoodsBlackList> list = blackListService.findPhoneList(params);
		model.addAttribute("list", list);
		model.addAttribute("goodId", goodId);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("promForbidBuyId",promForbidBuyId);
		return "order/ticket/goodslimit/showIDCardList";
	}
	
	@RequestMapping(value = "/showIDCardAgeLimitList.do")
	public String getIDCardAgeLimitList(Model model,Long goodId,Long categoryId,HttpServletRequest req) throws BusinessException {
		LOG.info("getIDCardAgeLimitList#goodId:"+goodId);
		String promForbidBuyId = req.getParameter("promForbidBuyId");
		List<SuppGoodsIDCardLimit> suppGoodsIDCardLimitList = blackListService.findIDCardAgeLimitList(goodId);
		if(!CollectionUtils.isEmpty(suppGoodsIDCardLimitList)){
			model.addAttribute("suppGoodsIDCardLimitList", suppGoodsIDCardLimitList);
			//一个标示  Y表示页面要选中特殊规则
			model.addAttribute("hasIDCardAgeLimit", "Y");
		}
		model.addAttribute("goodId", goodId);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("promForbidBuyId",promForbidBuyId);
		return "order/ticket/goodslimit/showIDCardAgeLimitList";
	}

	@RequestMapping(value = "/addBlackList.do")
	public String addBlackList(Model model,Long goodId,String blacklistType,HttpServletRequest req) throws BusinessException {
		model.addAttribute("goodId", goodId);
		model.addAttribute("blacklistType", blacklistType);
		return "order/ticket/goodslimit/addBlackList";
	}
	
	@RequestMapping(value = "/saveBlackList.do")
	@ResponseBody
	public Object saveBlackList(Model model,Long goodId,String blacklistNum,String blacklistType,HttpServletRequest req) throws BusinessException {
		ResultMessage msg = ResultMessage.createResultMessage();
		Map<String, Object> params = new HashMap<String, Object>();
		try {
			params.put("goodId", goodId);
			params.put("blacklistType", blacklistType);
			params.put("blacklistNum", blacklistNum);
			List<SuppGoodsBlackList> list = blackListService.findPhoneList(params);
			if(list.size()==0){
				SuppGoodsBlackList blackList = new SuppGoodsBlackList();
				blackList.setGoodId(goodId);
				blackList.setBlacklistNum(String.valueOf(blacklistNum));
				blackList.setBlacklistType(blacklistType);
				blackListService.addBlackList(blackList);
				log.info("goodsId: " + goodId + "saveBlackList通知分销开始了");
				// 发送预订限制更新消息通知分销
				pushAdapterServiceRemote.push(goodId, ComPush.OBJECT_TYPE.GOODS, ComPush.PUSH_CONTENT.FX_SUPPGOODS_ORDER_LIMIT, ComPush.OPERATE_TYPE.UP, ComIncreament.DATA_SOURCE_TYPE.BUSNINESS_DATA);
				log.info("goodsId: " + goodId + "saveBlackList通知分销成功了");
				msg.setMessage("success");
			}else{
				msg.setMessage("error");
			}
		} catch (Exception e) {
			msg.setCode("error");
			msg.setMessage("保存失败:" + e.getMessage());
		}
		return msg;
	}
	
	@RequestMapping(value = "/saveLimit.do")
	@ResponseBody
	public Object saveLimitList(Model model,String limitId,Long goodId,String startTime,String endTime,Long limitNum,String limitAble,HttpServletRequest req) throws BusinessException {
		ResultMessage msg = ResultMessage.createResultMessage();
		SuppGoodsLimit limit = blackListService.findDBVisitTimeLimitList(goodId);
		//如果为限制
		if("Y".equals(limitAble)){
			if(limit==null){
				limit = new SuppGoodsLimit();
			}
			limit.setGoodId(goodId);
			limit.setEndTime(DateUtil.toDate(endTime, "yyyy-MM-dd"));
			limit.setStartTime(DateUtil.toDate(startTime, "yyyy-MM-dd"));
			limit.setLimitNum(limitNum);
			if(limit.getLimitId()!=null){
				blackListService.updateLimitList(limit);
			}else{
				blackListService.addLimitList(limit);
			}
		}else {
			//如果为不限，则删除掉原有的数据
			if(limit!=null)
				blackListService.deleteLimitListById(limit.getLimitId());
		}
		log.info("goodsId: " + goodId + "saveLimit通知分销开始");
		// 发送预订限制更新消息通知分销
		pushAdapterServiceRemote.push(goodId, ComPush.OBJECT_TYPE.GOODS, ComPush.PUSH_CONTENT.FX_SUPPGOODS_ORDER_LIMIT, ComPush.OPERATE_TYPE.UP, ComIncreament.DATA_SOURCE_TYPE.BUSNINESS_DATA);
		log.info("goodsId: " + goodId + "saveLimit通知分销结束");
		return msg;
	}
	
	@RequestMapping(value = "/saveOrUpdateIDCardAgeLimit.do")
	@ResponseBody
	public Object saveOrUpdateIDCardAgeLimit(Model model,SuppGoodsIDCardLimit suppGoodsIDCardLimit){
		LOG.info("saveOrUpdateIDCardAgeLimit#start");
		//用于写日志
		SuppGoodsParam param = new SuppGoodsParam();
		param.setProduct(false);
		SuppGoods suppGoods = MiscUtils.autoUnboxing(suppGoodsService.findSuppGoodsById(suppGoodsIDCardLimit.getSuppGoodsId(), param));

		if(null != suppGoodsIDCardLimit){
			if(null != suppGoodsIDCardLimit.getIdCardLimitId()){
				SuppGoodsIDCardLimit suppGoodsIDCardLimitold=blackListService.findSuppGoodsIdcardLimitById(suppGoodsIDCardLimit.getIdCardLimitId());
				LOG.info("saveOrUpdateIDCardAgeLimit#IdCardLimitId:"+suppGoodsIDCardLimit.getIdCardLimitId());
				int updateCount = blackListService.updateIDCardLimitByKey(suppGoodsIDCardLimit);
				if(!(updateCount > 0)){
					return ResultMessage.ADD_FAIL_RESULT;
				}
				try{
					String logContent=getIDCardAgeLimitLog(suppGoodsIDCardLimitold,suppGoodsIDCardLimit);
					comLogService.insert(ComLog.COM_LOG_OBJECT_TYPE.SUPP_GOODS_GOODS,
							suppGoods.getProductId(), suppGoodsIDCardLimit.getSuppGoodsId(),
							this.getLoginUser().getUserName(),
							"修改了商品【"+suppGoodsIDCardLimit.getSuppGoodsId()+"】的限制规则,变更内容:["+logContent+"]",
							ComLog.COM_LOG_LOG_TYPE.SUPP_GOODS_GOODS_CHANGE.name(),
							"修改商品适用限制规则",null);
				} catch (Exception e) {
					log.error("Record Log failure ！Log type:"+ ComLog.COM_LOG_LOG_TYPE.SUPP_GOODS_GOODS_CHANGE.name());
					log.error(ExceptionFormatUtil.getTrace(e));
				}
			}else{
				LOG.info("saveOrUpdateIDCardAgeLimit#add");
				int addCount = blackListService.addIDCardAgeLimit(suppGoodsIDCardLimit);
				if(!(addCount>0)){
					return ResultMessage.ADD_FAIL_RESULT;
				}
				try{
					String logContent=getIDCardAgeLimitLog(null,suppGoodsIDCardLimit);
					comLogService.insert(ComLog.COM_LOG_OBJECT_TYPE.SUPP_GOODS_GOODS,
							suppGoods.getProductId(), suppGoodsIDCardLimit.getSuppGoodsId(),
							this.getLoginUser().getUserName(),
							"新增了商品【"+suppGoodsIDCardLimit.getSuppGoodsId()+"】的限制规则,变更内容:["+logContent+"]",
							ComLog.COM_LOG_LOG_TYPE.SUPP_GOODS_GOODS_CHANGE.name(),
							"新增商品适用限制规则",null);
				} catch (Exception e) {
					log.error("Record Log failure ！Log type:"+ ComLog.COM_LOG_LOG_TYPE.SUPP_GOODS_GOODS_CHANGE.name());
					log.error(ExceptionFormatUtil.getTrace(e));
				}
			}
		}else{
			return new ResultMessage("error", "系统出现异常！");
		}
		return ResultMessage.ADD_SUCCESS_RESULT;
	}

	/**
	 * 获取修改限制年龄日志
	 * @param suppGoodsIDCardLimitOld
	 * @param suppGoodsIDCardLimitNew
	 * @return
	 */
	public  String getIDCardAgeLimitLog(SuppGoodsIDCardLimit suppGoodsIDCardLimitOld,SuppGoodsIDCardLimit suppGoodsIDCardLimitNew){
		StringBuilder logStr = new StringBuilder("");
		if(suppGoodsIDCardLimitOld !=null){
			logStr.append(ComLogUtil.getLogTxt("开始年龄",suppGoodsIDCardLimitOld.getStartAge(),suppGoodsIDCardLimitNew.getStartAge()));
			logStr.append(ComLogUtil.getLogTxt("结束年龄",suppGoodsIDCardLimitOld.getEndAge(),suppGoodsIDCardLimitNew.getEndAge()));
			return logStr.toString();

		}
		logStr.append("开始年龄为："+suppGoodsIDCardLimitNew.getStartAge()).append("结束年龄为："+suppGoodsIDCardLimitNew.getEndAge());
		return logStr.toString();
	}
	@RequestMapping(value = "/deleteIDCardAgeLimit.do")
	@ResponseBody
	public Object deleteIDCardAgeLimit(Model model,SuppGoodsIDCardLimit suppGoodsIDCardLimit){
		LOG.info("deleteIDCardAgeLimit#start");
		if(null != suppGoodsIDCardLimit && null != suppGoodsIDCardLimit.getIdCardLimitId()){
				SuppGoodsIDCardLimit suppGoodsIDCardLimitold=blackListService.findSuppGoodsIdcardLimitById(suppGoodsIDCardLimit.getIdCardLimitId());
				LOG.info("deleteIDCardAgeLimit#IdCardLimitId:"+suppGoodsIDCardLimit.getIdCardLimitId());
				int addCount = blackListService.deleteIDCardLimitByKey(suppGoodsIDCardLimit.getIdCardLimitId());
				if(!(addCount>0)){
				   return ResultMessage.DELETE_FAIL_RESULT;
				}
				 try{
					 SuppGoodsParam param = new SuppGoodsParam();
					 param.setProduct(false);
					 SuppGoods suppGoods = MiscUtils.autoUnboxing(suppGoodsService.findSuppGoodsById(suppGoodsIDCardLimit.getSuppGoodsId(), param));
					 String logContent="开始年龄为："+suppGoodsIDCardLimitold.getStartAge()+",结束年龄为："+suppGoodsIDCardLimitold.getEndAge();
					 comLogService.insert(ComLog.COM_LOG_OBJECT_TYPE.SUPP_GOODS_GOODS,
							 suppGoods.getProductId(), suppGoodsIDCardLimit.getSuppGoodsId(),
							 this.getLoginUser().getUserName(),
							 "删除了商品：【"+suppGoodsIDCardLimit.getSuppGoodsId()+"】限制规则,删除内容为:["+logContent+"]",
							 ComLog.COM_LOG_LOG_TYPE.SUPP_GOODS_GOODS_CHANGE.name(),
							 "删除商品适用限制规则",null);
				 } catch (Exception e) {
					 log.error("Record Log failure ！Log type:"+ ComLog.COM_LOG_LOG_TYPE.SUPP_GOODS_GOODS_CHANGE.name());
					 log.error(ExceptionFormatUtil.getTrace(e));
				 }
		}else{
			return new ResultMessage("error", "系统出现异常！");
		}
		return ResultMessage.DELETE_SUCCESS_RESULT;
	}
	
	@RequestMapping(value = "/deleteBlackList.do")
	public Object deleteBlackList(Model model,Long blackId,Long IDCard,HttpServletRequest req) throws BusinessException {
		log.info("blackId: " +  blackId);
		Long goodsId = blackListService.findGoodsIdByBlackId(blackId);
		blackListService.deleteBlackListById(blackId);
		log.info("goodsId: " +  goodsId);
		log.info("goodsId: " + goodsId + "deleteBlackList通知分销开始");
		// 发送预订限制更新消息通知分销
		pushAdapterServiceRemote.push(goodsId, ComPush.OBJECT_TYPE.GOODS, ComPush.PUSH_CONTENT.FX_SUPPGOODS_ORDER_LIMIT, ComPush.OPERATE_TYPE.DEL, ComIncreament.DATA_SOURCE_TYPE.BUSNINESS_DATA);
		log.info("goodsId: " + goodsId + "deleteBlackList通知分销结束");
		return null;
	}

	private void fillShowCircusFlag(Model model) {
		//LV6868 王雪梅;LV6147  童小能;  LV6785  王芬；LV13722 余国平; LV6341 刘琴；LV8105 冯晨阳; LV6396 杨梓涵；LV14663 叶盛; LV13816 郝韵 ； lv14971 嵇宁欣;lv15196 陶勇; lv10343 高颜敏
		//lv9692 王雁  ;lv6881 熊鹰；lv15318 章赛英；lv13894 刘芸

		List<SuppGoodsEditPerm> suppGoodsEditPermList = suppGoodsEditPermService.selectAllEditPermFromMemCache();
		String isShowCircusFlag = "N";
		for (SuppGoodsEditPerm suppGoodsEditPerm : suppGoodsEditPermList) {
			if( SuppGoodsEditPerm.PERM_TYPE.SUPP_LIMIT_TYPE.getCode().equals(suppGoodsEditPerm.getPermType())
					&& suppGoodsEditPerm.getUserName().equals(getLoginUserId())){
				isShowCircusFlag = "Y";
				break;
			}
		}
		model.addAttribute("isShowCircusFlag", isShowCircusFlag);
	}

}
