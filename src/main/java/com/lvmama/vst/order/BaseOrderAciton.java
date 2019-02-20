/**
 * 
 */
package com.lvmama.vst.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.dest.dock.response.ResponseBody;
import com.lvmama.dest.dock.response.order.ResponseCreditCardValidate;
import com.lvmama.dest.dock.service.interfaces.ApiCreditCardValidate;
import com.lvmama.dest.dock.utils.OrderEnum;
import com.lvmama.order.trade.vo.comm.BaseBuyInfoVo;
import com.lvmama.order.trade.vo.comm.OrdGuaranteeCreditCardVo;
import com.lvmama.order.trade.vo.comm.OrdPersonVo;
import com.lvmama.order.trade.vo.hotel.HotelBuyInfoVo;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.OrderRequiredClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.prod.vo.OrderRequiredVO;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;

/**
 * @author pengyayun
 *
 */
public class BaseOrderAciton extends BaseActionSupport {
	
	private static final Log LOG = LogFactory.getLog(BaseOrderAciton.class);

	@Autowired
	protected UserUserProxyAdapter userUserProxyAdapter;
	
	@Autowired
	protected OrderRequiredClientService orderRequiredClientService;

	/**
	 * 转换页面上提交的表单，页面上以map的方式提交数据
	 * @param info
	 * @return
	 */
	protected BuyInfo converForm(BuyInfo info){

		//身份证输入规范中的字母为大写，所以在此统一转为大写
		if(info != null){
			List<Person> travellers = info.getTravellers();
			if(travellers != null && travellers.size() > 0){
				for(Person person : travellers){
					if(person != null && !StringUtil.isEmptyString(person.getIdNo())){
						person.setIdNo(person.getIdNo().toUpperCase());
					}
				}
			}
		}
		
		if(MapUtils.isNotEmpty(info.getItemMap())){
			info.setItemList(new ArrayList(info.getItemMap().values()));
			info.getItemMap().clear();
		}
		if(MapUtils.isNotEmpty(info.getProductMap())){
			info.setProductList(new ArrayList<BuyInfo.Product>(info.getProductMap().values()));
			info.getProductMap().clear();
		}
		info.setDistributionId(Constant.DIST_BACK_END);
		if(CollectionUtils.isNotEmpty(info.getItemList())){
			info.getItemList().get(0).setMainItem("true");
		}
		return info;
	}

	/**
	 * 转换页面上提交的表单，页面上以map的方式提交数据
	 * @param info
	 * @return
	 */
	protected HotelBuyInfoVo converHotelForm(HotelBuyInfoVo info){
		
		//身份证输入规范中的字母为大写，所以在此统一转为大写
		if(info != null){
			List<OrdPersonVo> travellers = info.getTravellers();
			if(travellers != null && travellers.size() > 0){
				for(OrdPersonVo person : travellers){
					if(person != null && !StringUtil.isEmptyString(person.getIdNo())){
						person.setIdNo(person.getIdNo().toUpperCase());
					}
				}
			}
		}
		
		if(MapUtils.isNotEmpty(info.getItemMap())){
			info.setItemList(new ArrayList(info.getItemMap().values()));
			info.getItemMap().clear();
		}
		if(MapUtils.isNotEmpty(info.getProductMap())){
			Collection<BaseBuyInfoVo.Product> productCollection = info.getProductMap().values();

			LOG.info("开始转换日期-----");
			if (!CollectionUtils.isEmpty(productCollection)) {
				LOG.info("productCollection.size=" + productCollection.size());
			}
			LOG.info("完成转换日期-----");
			info.setProductList(new ArrayList<BaseBuyInfoVo.Product>(productCollection));
			info.getProductMap().clear();
		}
		info.setDistributionId(Constant.DIST_BACK_END);
		if(CollectionUtils.isNotEmpty(info.getItemList())){
			info.getItemList().get(0).setMainItem("true");
		}
		return info;
	}
	
	/**
	 * 
	 * @param buyInfo
	 */
	protected void initBooker(BuyInfo buyInfo){
		UserUser user = userUserProxyAdapter.getUserUserByUserNo(buyInfo.getUserId());
		Person person = new Person();
		person.setFullName(user.getUserName());
		person.setMobile(user.getMobileNumber());
		buyInfo.setUserNo(user.getId());
		buyInfo.setBooker(person);
	}

	/**
	 * 
	 * @param buyInfo
	 */
	protected void initHotelBooker(HotelBuyInfoVo buyInfo){
		UserUser user = userUserProxyAdapter.getUserUserByUserNo(buyInfo.getUserId());
		OrdPersonVo person = new OrdPersonVo();
		person.setFullName(user.getUserName());
		person.setMobile(user.getMobileNumber());
		buyInfo.setUserNo(user.getId());
		buyInfo.setBooker(person);
	}

    protected OrderRequiredVO queryItemInfo(OrdOrder order){
        List<Long> productIdList=null;
		List<Long> suppGoodsIdList=null;
		List<OrdOrderItem> orderItemList=order.getOrderItemList();
		if(order!=null){
			productIdList=new ArrayList<Long>();
			suppGoodsIdList=new ArrayList<Long>();
			for (OrdOrderItem ordOrderItem : orderItemList) {
				suppGoodsIdList.add(ordOrderItem.getSuppGoodsId());
				productIdList.add(ordOrderItem.getProductId());
			}
			List<OrdOrderPack> packList=order.getOrderPackList();
			if(CollectionUtils.isNotEmpty(packList)){
				for (OrdOrderPack ordOrderPack : packList) {
					productIdList.add(ordOrderPack.getProductId());
				}
			}
			
		}
        boolean isTicketBu = ProductCategoryUtil.isTicket(order.getCategoryId());
        CommEnumSet.BU_NAME buName = isTicketBu ? CommEnumSet.BU_NAME.TICKET_BU : null;
        ResultHandleT<OrderRequiredVO> orderRequiredVO = null; 
        if(order.getCategoryId().equals(41L) 
        		||order.getCategoryId().equals(43L) 
        		||order.getCategoryId().equals(44L)
        		||order.getCategoryId().equals(45L)  ){
        	
        	if (suppGoodsIdList!=null) {
				orderRequiredVO = orderRequiredClientService.findOrderRequiredListId(null, suppGoodsIdList);
			}
        	
        }
        else{
        	orderRequiredVO = orderRequiredClientService.findOrderRequiredListId(productIdList, suppGoodsIdList, buName);
        }
        OrderRequiredVO vo = orderRequiredVO.getReturnContent();
        if (vo != null) {
            //门票取消证件类型'客服联系我'的选项
            if (isTicketBu) {
                vo.setProductType("ticket");
            }
            //wifi下单必填项设置
            if (BizEnum.BIZ_CATEGORY_TYPE.category_wifi.getCategoryId().equals(order.getCategoryId())) {
                vo.setTravNumType("TRAV_NUM_ONE");
                vo.setPhoneType("TRAV_NUM_ONE");
            }
        }
		return vo;
	}

    protected Integer countTotalBx(OrdOrder order) {
        List<OrdOrderItem> list = order.getOrderItemList();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        int count = 0;
        for (OrdOrderItem item : list) {
            if (BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(item.getCategoryId())) {
                count += item.getQuantity();
            }
        }

        return count;
    }
	
	/**
	 * 检查form参数必填项是否满足
	 * @param buyInfo
	 */
	protected void checkBuyInfo(BuyInfo buyInfo){
		
	}
	

	/**
	 * (2017-02-12 add)
	 * 验证信用信息
	 * @param buyInfo
	 * @param apiCreditCardValidate
	 */
	public static void validHotelCheck(HotelBuyInfoVo hotelBuyInfoVo, ApiCreditCardValidate apiCreditCardValidate) {

		if (OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name().equals(hotelBuyInfoVo.getNeedGuarantee())) {
			String errorMsg = null;
			OrdGuaranteeCreditCardVo ordGuaranteeCreditCardVo = hotelBuyInfoVo.getGuarantee();
			if (ordGuaranteeCreditCardVo != null && ordGuaranteeCreditCardVo.getCardNo() != null) {
				LOG.info("GuaranteeCreditCardUtil.validCheck,CardNo:" + ordGuaranteeCreditCardVo.getCardNo());
				ResponseBody<ResponseCreditCardValidate> response = apiCreditCardValidate.creditCardValidate(ordGuaranteeCreditCardVo.getCardNo());
				if (response.isSuccess()) {
					ResponseCreditCardValidate validate = response.getT();
					if (validate != null) {
						if (validate.isValid()) {
							if (validate.isNeedVerifyCode()) {
								if (ordGuaranteeCreditCardVo.getCvv() == null || "".equals(ordGuaranteeCreditCardVo.getCvv().trim())) {
									errorMsg = "请提供CVV码。";
								}
							}
						} else {
							errorMsg = "信用卡无效。";
						}
					} else {
						errorMsg = "信用卡无法验证。";
					}
				} else {
					errorMsg = "信用卡验证失败。";
				}
			} else {
				errorMsg = "请填写信用卡号码等信息。";
			}

			if (errorMsg != null) {
				throw new IllegalArgumentException(errorMsg);
			}
		}

	}

}
