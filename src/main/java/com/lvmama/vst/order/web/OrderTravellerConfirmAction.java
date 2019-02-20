package com.lvmama.vst.order.web;

import com.lvmama.comm.vst.VSTEnum;
import com.lvmama.vst.back.client.ord.service.OrderTravellerConfirmClientService;
import com.lvmama.vst.back.order.po.OrdOrderTravellerConfirm;
import com.lvmama.vst.back.order.po.OrderTravellerOperateDO;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.BaseOrderAciton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Controller
public class OrderTravellerConfirmAction extends BaseOrderAciton{

	/**
	 * @author chenguangyao
	 * @since  2016.05.05
	 * 游玩人须知
	 */
	private static final long serialVersionUID = 4567936490701439555L;

	private static final Log LOG = LogFactory.getLog(OrderTravellerConfirmAction.class);
	@Autowired
	private OrderTravellerConfirmClientService orderTravellerConfirmClientService;
	
	@RequestMapping("/ord/order/update/updateTravellerConfirm.do") 
	@ResponseBody
	public Object updateTravellerConfirm(OrdOrderTravellerConfirm ordOrderTravellerConfirm,ModelMap model, HttpServletRequest request) throws BusinessException{
		
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			if(ordOrderTravellerConfirm!=null){
				ordOrderTravellerConfirm.setUpdateTime(new Date());
				int result=0;
				OrderTravellerOperateDO orderTravellerOperateDO = new  OrderTravellerOperateDO();
				orderTravellerOperateDO.setOrderTravellerConfirm(ordOrderTravellerConfirm);
				orderTravellerOperateDO.setUserCode(getLoginUserId());
				orderTravellerOperateDO.setChannelType(VSTEnum.DISTRIBUTION.LVMAMABACK.getNum()+"");
				result=orderTravellerConfirmClientService.updateOrderTravellerConfirmInfo(orderTravellerOperateDO);
				if(result>0){
					msg.setMessage("保存成功");
				}else{
					msg.setMessage("保存失败");
				}
			}		
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			msg.raise(e.getMessage());
		}
		return msg;
	}
		
	
}
