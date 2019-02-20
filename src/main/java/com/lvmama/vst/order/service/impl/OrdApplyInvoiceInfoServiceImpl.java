package com.lvmama.vst.order.service.impl;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.finance.service.InvoiceRemoteService;
import com.lvmama.finance.vo.*;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.vo.OrdAppInvInfoQueryVo;
import com.lvmama.vst.back.prod.po.ProdProduct.COMPANY_TYPE_DIC;
import com.lvmama.vst.comm.utils.Pair;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.order.ApplyInvoiceInfoResult;
import com.lvmama.vst.comm.vo.order.OrderInvoiceInfoVst;
import com.lvmama.vst.order.dao.OrdAddressDao;
import com.lvmama.vst.order.dao.OrdApplyInvoiceInfoDao;
import com.lvmama.vst.order.dao.OrdPersonDao;
import com.lvmama.vst.order.service.IOrdInvoiceService;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.OrdApplyInvoiceInfoService;
import com.lvmama.vst.pet.adapter.IReceiverUserServiceAdapter;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Wangsizhi
 */
@Service("ordApplyInvoiceInfoService")
public class OrdApplyInvoiceInfoServiceImpl implements OrdApplyInvoiceInfoService {    
 
    private Logger logger = Logger.getLogger(OrdApplyInvoiceInfoServiceImpl.class);
    private static final String objectType = "ORD_APPLY_INVOICE_INFO";
    
    
    @Autowired
    private OrdApplyInvoiceInfoDao ordApplyInvoiceInfoDao;
    
    @Autowired
    private OrdPersonDao ordPersonDao;
  
    @Autowired
    private OrdAddressDao ordAddressDao;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private IOrdInvoiceService ordInvoiceService;
    
    @Autowired
    private IOrderUpdateService orderUpdateService;
    
    @Autowired
	private UserUserProxyAdapter userUserProxyService;
    
    @Autowired
	private IOrdTravelContractService ordTravelContractService;
    @Autowired
    private InvoiceRemoteService invoiceRemoteService;

    /**
     * 保存发票申请信息，用于入住24小时后申请发票。
     * @param orderId 订单ID
     * @param map 发票申请的相关信息
     */
    @Override
    public void saveOrdApplyInvoiceInfo(String orderId, Map<String, String> map) {
        
        if (StringUtils.isNotBlank(orderId)) {
            orderId = orderId.trim();
            JSONObject jsonMap = JSONObject.fromObject(map);
            logger.info("saveOrdApplyInvoiceInfo orderId=" + orderId + "---map=" + jsonMap.toString());
         }
        
    	//发票信息
    	String title = map.get("title");
    	String content = map.get("content");
    	String deliveryType = map.get("deliveryType");
    	deliveryType = StringUtils.isBlank(deliveryType) ? "EXPRESS" : deliveryType;
    	String userId = map.get("userId");
    	String errormsg = map.get("errormsg"); 
    	String purchaseWay = map.get("purchaseWay");
        String taxNumber = map.get("taxNumber");
        String buyerAddress = map.get("buyerAddress");
        String buyerTelephone = map.get("buyerTelephone"); 
        String bankAccount = map.get("bankAccount");
        String accountBankAccount = map.get("accountBankAccount");
        String elecInvoiceStr = map.get("elecInvoice");
        String insIncludedStr = map.get("insIncluded");
        
        Integer elecInvoice = null;
        String receiverEmail = map.get("receiverEmail");
        
        if (StringUtils.isBlank(elecInvoiceStr)) {
            //传参的elecInvoice为空，根据配置（sweet）的isDefaultEle判断，默认设置成电子还是纸质
            String isDefaultEle = Constant.getInstance().getProperty("isDefaultEle");
            logger.info("saveOrdApplyInvoiceInfo orderId=" + orderId + "---isDefaultEle=" + isDefaultEle.trim());
            if (StringUtils.isBlank(isDefaultEle)) {
                return;
            }
            int isDefaultEleInt = Integer.parseInt(isDefaultEle.trim());
            if (1 == isDefaultEleInt) {
                logger.info("saveOrdApplyInvoiceInfo orderId=" + orderId + "---isDefaultEleInt=" + isDefaultEleInt + "---默认电子发票");
                elecInvoice = isDefaultEleInt;
            }
            if (0 == isDefaultEleInt) {
                logger.info("saveOrdApplyInvoiceInfo orderId=" + orderId + "---isDefaultEleInt=" + isDefaultEleInt + "---默认纸质发票");
                elecInvoice = isDefaultEleInt;
            }
        }else {
            elecInvoice = Integer.parseInt(elecInvoiceStr.trim());
        }

    	//电子发票时，快递方式改为自取,若包含保险开单改为快递
    	if (1 == elecInvoice) {
    	    deliveryType = "SELF";

            //Y表示包含保险发票，N表示不包含
    	    if ("Y".equals(insIncludedStr)){
                deliveryType = "EXPRESS";
            }
        }
        
    	OrdApplyInvoiceInfo  ordApplyInvoiceInfo = new OrdApplyInvoiceInfo();
    	ordApplyInvoiceInfo.setTitle(title);
    	ordApplyInvoiceInfo.setContent(content);
    	ordApplyInvoiceInfo.setDeliveryType(deliveryType);
    	ordApplyInvoiceInfo.setUserId(userId);
    	ordApplyInvoiceInfo.setOrderId(Long.parseLong(orderId));
    	ordApplyInvoiceInfo.setUpdateTime(new Date());
    	ordApplyInvoiceInfo.setApplyTimes(0);
    	ordApplyInvoiceInfo.setErrormsg(errormsg);
    	ordApplyInvoiceInfo.setPurchaseWay(purchaseWay);
    	ordApplyInvoiceInfo.setElecInvoice(elecInvoice);
    	ordApplyInvoiceInfo.setReceiverEmail(receiverEmail);
    	
        if (StringUtils.isBlank(purchaseWay)) {
            ordApplyInvoiceInfo.setPurchaseWay("personal");//personal 个人
            ordApplyInvoiceInfo.setTaxNumber(null);
        }else if (StringUtils.isNotBlank(purchaseWay)) {
            if (purchaseWay.equalsIgnoreCase("personal")) {
                ordApplyInvoiceInfo.setPurchaseWay("personal");//personal 个人
                ordApplyInvoiceInfo.setTaxNumber(null);
            }else if (purchaseWay.equalsIgnoreCase("company")) {
                ordApplyInvoiceInfo.setPurchaseWay("company");//company 公司
                ordApplyInvoiceInfo.setTaxNumber(taxNumber);
                ordApplyInvoiceInfo.setBuyerAddress(buyerAddress);
                ordApplyInvoiceInfo.setBuyerTelephone(buyerTelephone);
                ordApplyInvoiceInfo.setBankAccount(bankAccount);
                ordApplyInvoiceInfo.setAccountBankAccount(accountBankAccount);
            }
        }
    	
    	logger.info("------单酒店 前台下单  保存发票申请信息----开始,订单ID-----------OrdApplyInvoiceInfoServiceImpl-----begin----:" + orderId);
    	UserUser user = userUserProxyService.getUserUserByUserNo(userId);
    	
        if(user != null && StringUtils.isNotBlank(user.getUserName())){
        	ordApplyInvoiceInfo.setUserName(user.getUserName());
    	}
        if(user != null && StringUtils.isNotBlank(user.getMobileNumber())){
        	ordApplyInvoiceInfo.setMobileNumber(user.getMobileNumber());
    	}
        logger.info("------单酒店 前台下单  保存发票申请信息----开始,订单ID-----------OrdApplyInvoiceInfoServiceImpl-----end----:" + orderId);

        ordApplyInvoiceInfo.setStatus("PENDING");
        ordApplyInvoiceInfo.setCreateTime(new Date());
        if (null != ordApplyInvoiceInfo) {
            logger.info("add ordApplyInvoiceInfo orderId=" + ordApplyInvoiceInfo.getOrderId() + "    " + ordApplyInvoiceInfo.toString());
        }

        if (StringUtils.isBlank(ordApplyInvoiceInfo.getTitle())) {
            logger.info("add ordApplyInvoiceInfo title is null orderId=" + ordApplyInvoiceInfo.getOrderId());
            return;
        }
        
        
    	ordApplyInvoiceInfoDao.insertSelective(ordApplyInvoiceInfo);
    	
    	if (deliveryType.equalsIgnoreCase("EXPRESS")) {
    	  //发票联系人信息
            String fullName = map.get("fullName");
            String mobile = map.get("mobile");
            
            OrdPerson ordPerson = new OrdPerson();
            ordPerson.setFullName(fullName);
            ordPerson.setMobile(mobile);
            ordPerson.setObjectId(Long.parseLong(orderId));
            ordPerson.setObjectType(objectType);
            ordPerson.setPersonType(IReceiverUserServiceAdapter.RECEIVERS_TYPE.CONTACT.name());
            ordPersonDao.insertSelective(ordPerson);
            
            logger.info("saveOrdApplyInvoiceInfo ordPerson insert success orderId=" + orderId + "---" + ordPerson.toString());
            
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("orderId", Long.parseLong(orderId));
            List<OrdPerson> ordPersons = ordPersonDao.getOrdApplyInvoicePersonByOrderId(params);
             Long ordPersonId = null; 
                
             if (CollectionUtils.isNotEmpty(ordPersons)) {
                    OrdPerson  orderPerson= ordPersons.get(0);
                    ordPersonId = orderPerson.getOrdPersonId();
                    
                    //发票寄送地址信息
                    String province = map.get("province");
                    String city = map.get("city");
                    String street = map.get("street");
                    String postcode = map.get("postcode");
                    String district = map.get("district");
                    
                    OrdAddress ordAddress = new OrdAddress();
                    ordAddress.setProvince(province);
                    ordAddress.setCity(city);
                    ordAddress.setStreet(street);
                    ordAddress.setPostalCode(postcode);
                    ordAddress.setOrdPersonId(ordPersonId);
                    ordAddress.setDistrict(district);
                    ordAddressDao.insertSelective(ordAddress);
                    
                    logger.info("saveOrdApplyInvoiceInfo ordAddress insert success orderId=" + orderId + "---" + ordPerson.toString());
              }
        }
      		 
    }

    
    /**
    * 根据用户ID获取发票填充信息
    * @param  userId
    */
   @Override
   public List<OrderpersonInvoiceInfoAddress> findInvoicePersonInfo(Map<String, Object> map){
       //订单重构时优化入参不用map
       //List<OrderpersonInvoiceInfoAddress> ordPersonList  = ordPersonDao.findOrdPersonListInvoice(map);
       List<OrderpersonInvoiceInfoAddress> ordPersonList  = new ArrayList<OrderpersonInvoiceInfoAddress>();
       
       if (null == map || map.size() == 0) {
           logger.info("OrdApplyInvoiceInfoServiceImpl findInvoicePersonInfo map is null");
           return null;
       }
       String userId = (String) map.get("userId");
       
       long startTime = System.currentTimeMillis();
       logger.info("OrdApplyInvoiceInfoServiceImpl findInvoicePersonInfo userId=" + userId + "---startTime=" + startTime);
       if (StringUtils.isBlank(userId)) {
           logger.info("OrdApplyInvoiceInfoServiceImpl findInvoicePersonInfo userId is null");
           return null;
       }
       OrdApplyInvoiceInfo latestApplyInvoice = ordApplyInvoiceInfoDao.selectLatestApplyInvoiceByUserId(userId);
       if (null == latestApplyInvoice) {
           logger.info("OrdApplyInvoiceInfoServiceImpl findInvoicePersonInfo no applyInvoice userId=" + userId);
           return null;
       }
       
      Long orderId = latestApplyInvoice.getOrderId();
       if (null == orderId) {
           logger.info("OrdApplyInvoiceInfoServiceImpl findInvoicePersonInfo  latestApplyInvoice orderId is null userId=" + userId);
           return null;
       }
       
       OrderpersonInvoiceInfoAddress opia = new OrderpersonInvoiceInfoAddress();
       OrdApplyInvoicePersonAddress fullApplyInvoiceInfo = fillFullApplyInvoiceInfo(latestApplyInvoice);
       
       //重构优化后会使用OrdApplyInvoicePersonAddress，删除OrderpersonInvoiceInfoAddress
       opia.setTitle(fullApplyInvoiceInfo.getTitle());
       opia.setContent(fullApplyInvoiceInfo.getContent());
       opia.setAmount(fullApplyInvoiceInfo.getAmount());
       opia.setPurchaseWay(fullApplyInvoiceInfo.getPurchaseWay());
       opia.setTaxNumber(fullApplyInvoiceInfo.getTaxNumber());
       opia.setBuyerAddress(fullApplyInvoiceInfo.getBuyerAddress());
       opia.setBuyerTelephone(fullApplyInvoiceInfo.getBuyerTelephone());
       opia.setBankAccount(fullApplyInvoiceInfo.getBankAccount());
       opia.setAccountBankAccount(fullApplyInvoiceInfo.getAccountBankAccount());
       
       opia.setElecInvoice(fullApplyInvoiceInfo.getElecInvoice());
       opia.setReceiverEmail(fullApplyInvoiceInfo.getReceiverEmail());
       
       opia.setContactName(fullApplyInvoiceInfo.getFullName());
       opia.setMobile(fullApplyInvoiceInfo.getMobile());
       
       opia.setDeliveryType(fullApplyInvoiceInfo.getDeliveryType());
       opia.setProvince(fullApplyInvoiceInfo.getProvince());
       opia.setCity(fullApplyInvoiceInfo.getCity());
       opia.setDistrict(fullApplyInvoiceInfo.getDistrict());
       opia.setStreet(fullApplyInvoiceInfo.getStreet());
       opia.setPostalCode(fullApplyInvoiceInfo.getPostcode());
       
       logger.info("OrdApplyInvoiceInfoServiceImpl findInvoicePersonInfo userId=" + userId + "---opia=" + opia.toString());
       
       ordPersonList.add(opia);
       long endTime = System.currentTimeMillis();
       logger.info("OrdApplyInvoiceInfoServiceImpl findInvoicePersonInfo userId=" + userId + "---endTime=" + endTime);
       logger.info("OrdApplyInvoiceInfoServiceImpl findInvoicePersonInfo userId=" + userId + "---costTime=" + (endTime-startTime));
       return ordPersonList;
   }

	
    /**
     * 查询vst订单详情页   发票补偿信息
     */
    @Override
    public List<ApplyInvoiceInfoResult> getPreparyApplyInvoiceInfo(Map<String, Object> map) {
    	map.put("unApplied",true);
    	List<ApplyInvoiceInfoResult> preparyApplyInvoiceInfo  = ordApplyInvoiceInfoDao.getPreparyApplyInvoiceInfo(map);
		return preparyApplyInvoiceInfo;
    }

    /**
     * 查询vst订单详情页    发票补偿信息显示条数
     */
    @Override
    public Integer getApplyInvoiceInfoCount(Map<String, Object> map) {
    	map.put("unApplied",true);
        List<ApplyInvoiceInfoResult> preparyApplyInvoiceInfo  = ordApplyInvoiceInfoDao.getPreparyApplyInvoiceInfo(map);
		return preparyApplyInvoiceInfo.size();
    }
    
    /**
     * 查询vst订单详情页    发票补偿信息显示条数
     */
    @Override
    public void updateApplyInvoiceList(Map<String, Object> map) {
        List<ApplyInvoiceInfoResult> preparyApplyInvoiceInfo  = ordApplyInvoiceInfoDao.getPreparyApplyInvoiceInfo(map);
        logger.info("更新前size====="+preparyApplyInvoiceInfo.size());
        preparyApplyInvoiceInfo = filterAppliedInvioces(preparyApplyInvoiceInfo);
    }
    /**
     * 查询vst 后台订单详情页   发票信息模块
     */
    @Override
	public OrderInvoiceInfoVst getVstOrderInvoiceInfo(Long orderId){
        logger.info("OrdApplyInvoiceInfoServiceImpl getVstOrderInvoiceInfo orderId=" + orderId);
    	
    	List<OrdApplyInvoiceInfo> oaiiList = ordApplyInvoiceInfoDao.selectByOrderId(orderId);
    	//默认取最新一条
    	if (null == oaiiList || oaiiList.size() <=0) {
    	    logger.info("OrdApplyInvoiceInfoServiceImpl getVstOrderInvoiceInfo oaiiList is null orderId=" + orderId);
    	    return null;
        }
    	OrdApplyInvoiceInfo ordApplyInvoiceInfo = oaiiList.get(0);
    	
    	OrdApplyInvoicePersonAddress oapa = fillFullApplyInvoiceInfo(ordApplyInvoiceInfo);
    	OrderInvoiceInfoVst oiVst = new OrderInvoiceInfoVst();
    	
    	oiVst.setAccountBankAccount(oapa.getAccountBankAccount());
    	oiVst.setAmount(oapa.getAmount());
    	oiVst.setBankAccount(oapa.getBankAccount());
    	oiVst.setBuyerAddress(oapa.getBuyerAddress());
    	oiVst.setBuyerTelephone(oapa.getBuyerTelephone());
    	oiVst.setCity(oapa.getCity());
    	oiVst.setContactMobile(oapa.getMobile());
    	oiVst.setContactName(oapa.getFullName());
    	oiVst.setContent(oapa.getContent());
    	oiVst.setCreateTime(oapa.getCreateTime());
    	oiVst.setDeliveryType(oapa.getDeliveryType());
    	oiVst.setId(oapa.getId());
    	oiVst.setPostcode(oapa.getPostcode());
    	oiVst.setProvince(oapa.getProvince());
    	oiVst.setPurchaseWay(oapa.getPurchaseWay());
    	oiVst.setStatus(oapa.getStatus());
    	oiVst.setStreet(oapa.getStreet());
    	oiVst.setTaxNumber(oapa.getTaxNumber());
    	oiVst.setTitle(oapa.getTitle());
    	oiVst.setDistrict(oapa.getDistrict());
    	oiVst.setElecInvoice(oapa.getElecInvoice());
    	oiVst.setReceiverEmail(oapa.getReceiverEmail());
    	
    	logger.info("OrdApplyInvoiceInfoServiceImpl getVstOrderInvoiceInfo orderId=" + orderId + "---" + oiVst.toString());
    	
		return oiVst;
    	
    }
	 
    /**
     * @Description: 自动申请发票 
     * @author Administrator 
     * @date 2016-10-17 上午11:27:06
     */
    public void autoApplyInvoice() {
        /*尝试三次申请发票； 如三次失败，则客服人工申请；每尝试一次，则申请次数加一。*/
        int maxTryTimes = 3;
        for (int i = 0; i < maxTryTimes; i++) {
            applyInvoiceByApplyTimes(i);
        }
    }

    /**
     * @Description: 根据申请发票的次数（每申请一次，失败，申请次数加一）获取待申请 
     * @author jszhagnhe plus 
     * @date 2016-10-17 下午8:46:01
     */
    private void applyInvoiceByApplyTimes(final int applyTimes) {
        logger.info("applyInvoiceByApplyTimes applyTimes = " + applyTimes);
        List<OrdApplyInvoiceInfo> pendingList = ordApplyInvoiceInfoDao.getPendingApplyInfoListByParam(applyTimes);
        /*每次根据条件查询符合条件的前100条处理，直到查询数量为0时结束*/

        logger.info("获取待申请发票记录   pendingList.size() = " + pendingList.size());
        logger.info("applyInvoiceByApplyTimes  pendingList:");
        for (OrdApplyInvoiceInfo ordApplyInvoiceInfo : pendingList) {
            logger.info(ordApplyInvoiceInfo.toString());
        }

        while (null != pendingList && pendingList.size() > 0)
        {
            for (OrdApplyInvoiceInfo ordApplyInvoiceInfo : pendingList)
            {
                Long id = ordApplyInvoiceInfo.getId();
                Long orderId = ordApplyInvoiceInfo.getOrderId();
                OrdOrder ordorder = orderUpdateService.queryOrdOrderByOrderId(orderId);

                //发票主单未支付情况过滤不往财务推送发票
                Long expressOrderId = ordApplyInvoiceInfo.getExpressOrderId();
                if (expressOrderId != null) {
                    OrdOrder expressOrder = orderUpdateService.queryOrdOrderByOrderId(expressOrderId);
                    String orderStatus = expressOrder.getPaymentStatus();
                    if (isUnPayOrder(orderStatus)) {
                        logger.info("订单 orderId = " + orderId + " expressOrderId : " + expressOrderId + "发票主单未支付不推送发票信息");
                        continue;
                    }
                }


                if (null != ordorder)
                {
                	/*设置订单子项*/
                    List<OrdOrderItem> orderItems = orderUpdateService.queryOrderItemByOrderId(orderId);
                    ordorder.setOrderItemList(orderItems);
                    
                    String orderStatus = ordorder.getOrderStatus();
                    if (StringUtils.isBlank(orderStatus)) {
                        logger.info("获取order状态异常   orderId = " + orderId);
                    }
                    if (StringUtils.isNotBlank(orderStatus))
                    {
                        /*若订单在入住前已取消，则更新发票申请信息表中记录为已取消*/
                        if (isCancelOrder(orderStatus))
                        {
                            ordApplyInvoiceInfo.setStatus(OrdInvoiceApplyEnum.STATUS_TYPE.CANCEL.name());
                            logger.info("订单 orderId = " + orderId + " 已取消， 更新订单申请信息表信息为已取消");
                            ordApplyInvoiceInfoDao.updateByPrimaryKeySelective(ordApplyInvoiceInfo);
                        }
                        else
                        {
                        	InvoiceResponseVO<Boolean> checkOrderInvoicedResp =  invoiceRemoteService.checkOrderInvoiced(orderId);
                        	boolean invoicedFlag = checkOrderInvoicedResp.getBody();

                            /*若已申请发票，则更新发票申请信息表中记录为已申请*/
                            if (invoicedFlag)
                            {
                                logger.info("订单 orderId = " + orderId + " 已申请发票， 更新订单申请信息表信息已申请发票");
                                ordApplyInvoiceInfo.setStatus(OrdInvoiceApplyEnum.STATUS_TYPE.APPLIED.name());
                                ordApplyInvoiceInfoDao.updateByPrimaryKeySelective(ordApplyInvoiceInfo);
                            }

                            if(!invoicedFlag)
                            {
                                //计算发票金额
//                                long amount = calcInvoiceAmount(orderId);
                                InvoiceResponseVO<Long> orderInvoiceableAmtResp =  invoiceRemoteService.getInvoiceableAmountByOrderId(orderId);
                                if (orderInvoiceableAmtResp != null && orderInvoiceableAmtResp.isSuccess()) {
                                    long amount = orderInvoiceableAmtResp.getBody();
                                    logger.info("订单 orderId = " + orderId + " 开票金额 ："+amount);
                                    //申请发票
                                    applyInvoice(applyTimes, ordApplyInvoiceInfo,orderId, amount);
                                    //将发票金额回写到ORD_APPLY_INVOICE_INFO表中,注：申请成功后才更新金额
                                    OrdApplyInvoiceInfo ordApplyInvoiceInfo2 = ordApplyInvoiceInfoDao.selectByPrimaryKey(id);
                                    String applyInvoiceStatus = ordApplyInvoiceInfo2.getStatus();
                                    if (StringUtils.isNotBlank(applyInvoiceStatus)
                                            && applyInvoiceStatus.equals(OrdInvoiceApplyEnum.STATUS_TYPE.APPLIED.name())) {
                                        ordApplyInvoiceInfo.setAmount(amount);
                                        ordApplyInvoiceInfoDao.updateByPrimaryKeySelective(ordApplyInvoiceInfo);
                                    }
                                } else {
                                    logger.info("订单 orderId = " + orderId + "第" + ordApplyInvoiceInfo.getApplyTimes() + "次申请发票失败,调用财务开票金额接口失败");
                                    ordApplyInvoiceInfo.setApplyTimes(ordApplyInvoiceInfo.getApplyTimes() + 1);
                                    ordApplyInvoiceInfoDao.updateByPrimaryKeySelective(ordApplyInvoiceInfo);
                                    /*失败则将申请次数加一, 若申请次数等于3，则更新发票申请信息表中记录为已申请*/
                                    if (ordApplyInvoiceInfo.getApplyTimes() == 3) {
                                        ordApplyInvoiceInfo.setStatus(OrdInvoiceApplyEnum.STATUS_TYPE.MANUAL.name());
                                        ordApplyInvoiceInfo.setErrormsg("自动申请超过三次,需人工申请");
                                        ordApplyInvoiceInfoDao.updateByPrimaryKeySelective(ordApplyInvoiceInfo);
                                    }
                                }
                            }
                        }
                        
                    }
                } else{
                    logger.info("获取order信息为null orderId = " + orderId);
                }
                
        }

        pendingList = ordApplyInvoiceInfoDao.getPendingApplyInfoListByParam(applyTimes);

        }
    }
    
    /**
     * applyTimes 申请次数；ordApplyInvoiceInfo 发票申请信息；orderId 订单ID；amount 发票金额
     * @Description: 调用接口申请发票
     * @author Wangsizhi
     * @date 2016-10-21 下午5:55:13
     */
    private void applyInvoice(final int applyTimes,
            OrdApplyInvoiceInfo ordApplyInvoiceInfo, final Long orderId,
            final long amount) {

        if (amount < 100)
        {
            logger.info("订单 orderId = " + orderId + " 发票金额小于1元， 更新订单申请信息表信息无效");
            ordApplyInvoiceInfo.setStatus(OrdInvoiceApplyEnum.STATUS_TYPE.INVALID.name());
            ordApplyInvoiceInfo.setErrormsg("发票金额小于1元");
        }
        else
        {
            //组装调用发票申请接口所需的map
            OrdPerson ordPerson = new OrdPerson();
            String deliveryType = ordApplyInvoiceInfo.getDeliveryType();
            if (StringUtils.isNotBlank(deliveryType) && !deliveryType.equals("SELF")) {
                ordPerson = adaptAddress(orderId);
            }
            
            Integer elecInvoice = ordApplyInvoiceInfo.getElecInvoice();
            String receiverEmail = ordApplyInvoiceInfo.getReceiverEmail();
            
            OrdInvoice ordInvoice = new OrdInvoice();
            String content = ordApplyInvoiceInfo.getContent();
            String title = ordApplyInvoiceInfo.getTitle();
            String purchaseWay = ordApplyInvoiceInfo.getPurchaseWay();
            String taxNumber = ordApplyInvoiceInfo.getTaxNumber();

        	OrdOrder order = orderService.queryOrdorderByOrderId(orderId);
        	Map<String,Object> map = new HashMap<String,Object>();
        	map.put("orderId", orderId);
        	List<OrdTravelContract> ordTravelContracts = ordTravelContractService.findOrdTravelContractList(map);
        	
            ordInvoice.setContent(content);
            ordInvoice.setTitle(title);

            ordInvoice.setAmount(amount);
            ordInvoice.setDeliveryType(ordApplyInvoiceInfo.getDeliveryType());
            ordInvoice.setUserId(ordApplyInvoiceInfo.getUserId());
            ordInvoice.setBuyerAddress(ordApplyInvoiceInfo.getBuyerAddress());
            ordInvoice.setBuyerTelephone(ordApplyInvoiceInfo.getBuyerTelephone());
            ordInvoice.setBankAccount(ordApplyInvoiceInfo.getBankAccount());
            ordInvoice.setAccountBankAccount(ordApplyInvoiceInfo.getAccountBankAccount());

            if (StringUtils.isBlank(purchaseWay)) {
                ordInvoice.setPurchaseWay("personal");//company 公司
                ordInvoice.setTaxNumber(null);
            }else if (StringUtils.isNotBlank(purchaseWay)) {
                if (purchaseWay.equalsIgnoreCase("personal")) {
                    ordInvoice.setPurchaseWay("personal");//company 公司
                    ordInvoice.setTaxNumber(null);
                }else if (purchaseWay.equalsIgnoreCase("company")) {
                    ordInvoice.setPurchaseWay("company");//company 公司
                    ordInvoice.setTaxNumber(taxNumber);
                }
            }
            
            // 只处理 “国旅” （分公司）
    		if (ordTravelContracts != null && ordTravelContracts.size()>0 && order.getCompanyType() != null 
    				&& COMPANY_TYPE_DIC.GUOLV.name().equals(order.getCompanyType().trim())
    				&& OrdApplyInvoiceInfo.GUOLV_contractTemplate.contains(ordTravelContracts.get(0).getContractTemplate().toUpperCase())) {
    			ordInvoice.setCompanyType(COMPANY_TYPE_DIC.GUOLV.getTitle());
    		}else{
    			ordInvoice.setCompanyType(COMPANY_TYPE_DIC.XINGLV.getTitle());
            	logger.info("开票公司为空 ,default xinglv ---------- 订单编号 ---------- " + orderId);
    		}
        
            
            Pair<OrdInvoice, OrdPerson> kv = Pair.make_pair(ordInvoice, ordPerson);
            List<Pair<OrdInvoice, OrdPerson>> invoices = new ArrayList<Pair<OrdInvoice,OrdPerson>>();
            invoices.add(kv);

            try
            {
            	InvoiceOrderMergeVO invoiceMergeVO = new InvoiceOrderMergeVO();

            	List<InvoiceVO> invoiceVOList = new ArrayList<InvoiceVO>();
            	InvoiceVO invoiceVO = new InvoiceVO();
            	BeanUtils.copyProperties(ordInvoice, invoiceVO);

            	InvoiceAddressVO invoiceAddressVO = new InvoiceAddressVO();
            	boolean hasAddress = false;

            	if(CollectionUtils.isNotEmpty(ordPerson.getAddressList()))
            	{
            		OrdAddress ordAddress = ordPerson.getAddressList().get(0);
            		
            		if(ordAddress != null)
            		{
            			invoiceAddressVO.setCity(ordAddress.getCity());
            			invoiceAddressVO.setProvince(ordAddress.getProvince());
            			invoiceAddressVO.setDistrict(ordAddress.getDistrict());
            			invoiceAddressVO.setStreet(ordAddress.getStreet());
            			invoiceAddressVO.setPostalCode(ordAddress.getPostalCode());
            			invoiceAddressVO.setReceiverName(ordPerson.getFullName());
            			invoiceAddressVO.setReceiverPhone(ordPerson.getMobile());
                        hasAddress = true;
            		}
            	}

                if (null != elecInvoice && elecInvoice == 1) {
                    //电子发票时，设置接收电子发票的邮箱
                    invoiceAddressVO.setReceiverEmail(receiverEmail);
                    //电子发票包含保险（保险目前要邮寄）
                    if (hasAddress){
                        //1表示包含保险发票，0表示不包含
                        invoiceMergeVO.setInsIncluded(1);
                    } else {
                        invoiceMergeVO.setInsIncluded(0);
                    }
                }

            	invoiceVO.setInvoiceAddress(invoiceAddressVO);
            	
            	invoiceVOList.add(invoiceVO);

            	List<InvoiceOrderVO> invoiceOrderVOList = new ArrayList<>();
            	InvoiceOrderVO invoiceOrderVO = new InvoiceOrderVO();
            	invoiceOrderVO.setOrderId(orderId);
            	invoiceOrderVOList.add(invoiceOrderVO);

            	invoiceMergeVO.setInvoices(invoiceVOList);
            	invoiceMergeVO.setInvoiceOrders(invoiceOrderVOList);
            	
            	UserUser user = userUserProxyService.getUserUserByUserNo(ordApplyInvoiceInfo.getUserId());
            	invoiceMergeVO.setOperatorId(user.getUserName());
            	invoiceMergeVO.setOperatorName(user.getRealName());
            	
            	//设置是否为电子发票
            	invoiceMergeVO.setElecInvoice(elecInvoice);
            	
            	invoiceRemoteService.saveInvoiceForLvmamaFront(invoiceMergeVO);
            	
                ordApplyInvoiceInfo.setApplyTimes(applyTimes + 1);
                ordApplyInvoiceInfo.setStatus(OrdInvoiceApplyEnum.STATUS_TYPE.APPLIED.name());
            }
            catch (Exception e)
            {
                logger.info("订单 orderId = " + orderId + "第" + applyTimes + "次申请发票失败" + "\n"
                            + "exception = " , e);
                ordApplyInvoiceInfo.setApplyTimes(applyTimes + 1);
                /*失败则将申请次数加一, 若申请次数等于3，则更新发票申请信息表中记录为已申请*/
                if (ordApplyInvoiceInfo.getApplyTimes() == 3) {
                    ordApplyInvoiceInfo.setStatus(OrdInvoiceApplyEnum.STATUS_TYPE.MANUAL.name());
                    ordApplyInvoiceInfo.setErrormsg("自动申请超过三次,需人工申请");
                }
            }
        }
        ordApplyInvoiceInfoDao.updateByPrimaryKeySelective(ordApplyInvoiceInfo);
    }
    
    /**
     * @Description: 判断订单是否已取消（已取消：true；未取消：false） 
     * @author Wangsizhi
     * @date 2016-10-20 下午8:07:27
     */
    private boolean isCancelOrder(String orderStatus) {
        return orderStatus.equals(OrderEnum.ORDER_STATUS.CANCEL.name());
    }

    /**
     * @Description: 判断订单是否未支付（已取消：true；未取消：false）
     * @author Wangsizhi
     * @date 2016-10-20 下午8:07:27
     */
    private boolean isUnPayOrder(String paymentStatus) {
        return OrderEnum.PAYMENT_STATUS.UNPAY.name().equals(paymentStatus);
    }
    
    /**
     * @Description: 开票金额计算 
     * @author Wangsizhi
     * @date 2016-10-19 下午3:42:08
     */
/*    private long calcInvoiceAmount(Long orderId) {
        //TODO 现阶段按照原有后台开票计算金额的方式计算发票金额，后续再减去 储值卡支付金额 再做优化
    	 VstInvoiceAmountVo vstInvoiceAmountVo = orderService.getInvoiceAmount(orderId);
    	 Long invoiceAmountLong = vstInvoiceAmountVo.getInvoiceAmount();        
        return invoiceAmountLong;
    }*/
    
    /**
     * orderId 订单ID
     * @Description: 将发票申请的联系人和地址信息转换成发票的联系人和地址信息 
     * @author Wangsizhi
     * @date 2016-10-21 下午3:31:00
     */
    private OrdPerson adaptAddress(final Long orderId) {
        /*转换联系人*/
        OrdPerson applyInvoiceOrdPerson = getApplyInvoiceOrdPersonByOrderId(orderId);
        OrdPerson ordPerson = new OrdPerson();
        if (null != applyInvoiceOrdPerson) {
            ordPerson.setFullName(applyInvoiceOrdPerson.getFullName());
            ordPerson.setMobile(applyInvoiceOrdPerson.getMobile());
            ordPerson.setObjectType("ORD_INVOICE");
            ordPerson.setPersonType(IReceiverUserServiceAdapter.RECEIVERS_TYPE.ADDRESS.name());
            
            /*转换地址*/
            List<OrdAddress> applyInvoiceAddressList = applyInvoiceOrdPerson.getAddressList();
            List<OrdAddress> addressList = new ArrayList<OrdAddress>();
            
            OrdAddress applyInvoiceAddress = new OrdAddress();
            OrdAddress address = new OrdAddress();
            
            if (CollectionUtils.isNotEmpty(applyInvoiceAddressList)) {
                applyInvoiceAddress = applyInvoiceAddressList.get(0);
            }
            if (null != applyInvoiceAddress) {
                address.setProvince(applyInvoiceAddress.getProvince());
                address.setCity(applyInvoiceAddress.getCity());
                address.setStreet(applyInvoiceAddress.getStreet());
                address.setDistrict(applyInvoiceAddress.getDistrict());
                address.setPostalCode(applyInvoiceAddress.getPostalCode());
                addressList.add(address);
            }
            ordPerson.setAddressList(addressList);
        }
        
        return ordPerson;
    }
    
    /**
     * @Description: 根据orderId获取申请发票联系人信息和地址信息 
     * @author Wangsizhi
     * @date 2016-10-21 下午3:09:08
     */
    private OrdPerson getApplyInvoiceOrdPersonByOrderId(final Long orderId) {
        OrdPerson applyInvoiceOrdPerson = null;
        /*查询发票申请联系人信息*/
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("orderId", orderId);
        List<OrdPerson> ordPersonList = ordPersonDao.getOrdApplyInvoicePersonByOrderId(params);
        
        if (CollectionUtils.isNotEmpty(ordPersonList)) {
            applyInvoiceOrdPerson = ordPersonList.get(0);
            /*查询发票申请联系人地址信息信息*/
            Map<String, Object> ordAddressParams = new HashMap<String, Object>();
            Long ordPersonId = applyInvoiceOrdPerson.getOrdPersonId();
            ordAddressParams.put("ordPersonId", ordPersonId);
            List<OrdAddress> ordAddressList = ordAddressDao.findOrdAddressList(ordAddressParams);
            if (CollectionUtils.isNotEmpty(ordAddressList)) {
                /*设置联系人地址*/
                applyInvoiceOrdPerson.setAddressList(ordAddressList);
            }else {
                logger.info("订单 orderId = " + orderId + " ordPersonId" + ordPersonId 
                            + " 申请发票， 获取联系人地址为空");
            }
        }else {
            logger.info("订单 orderId = " + orderId + " 申请发票， 获取联系人为空");
        }
        return applyInvoiceOrdPerson;
    }

    private List<ApplyInvoiceInfoResult> filterAppliedInvioces(List<ApplyInvoiceInfoResult> preparyApplyInvoiceInfo) {
		if(preparyApplyInvoiceInfo != null && preparyApplyInvoiceInfo.size() > 0){
			Iterator ite = preparyApplyInvoiceInfo.iterator();
    		while (ite.hasNext()) {
    			ApplyInvoiceInfoResult info = (ApplyInvoiceInfoResult)ite.next();
                Long orderId = info.getOrderId();
                Long id = info.getId();
                OrdOrder ordorder = orderUpdateService.queryOrdOrderByOrderId(orderId);
                if (StringUtils.isNotBlank(ordorder.getNeedInvoice()) && "true".equals(ordorder.getNeedInvoice())) {
                    OrdApplyInvoiceInfo ordApplyInvoiceInfo = ordApplyInvoiceInfoDao.selectByPrimaryKey(id);
                    if(ordApplyInvoiceInfo != null){
                        logger.info("订单 orderId = " + orderId + " 已申请发票， 更新订单申请信息表信息已申请发票");
                        ordApplyInvoiceInfo.setStatus(OrdInvoiceApplyEnum.STATUS_TYPE.APPLIED.name());
                        ordApplyInvoiceInfoDao.updateByPrimaryKeySelective(ordApplyInvoiceInfo);
//                        ite.remove();
                    }
                }
    		}
    	}
		return preparyApplyInvoiceInfo;
	}

	
	 /**
     * 后台订单详情页 点击手动申请按钮后   根据orderId 更新发票信息表信息
	 * @return 
     */
	@Override
	public int updateApplyInfoStatus(Long id) {
	    int count = 0;
	    OrdApplyInvoiceInfo oaii = ordApplyInvoiceInfoDao.selectByPrimaryKey(id);
	    Long orderId = oaii.getOrderId();
	    OrdOrder ordorder = orderUpdateService.queryOrdOrderByOrderId(orderId);
        if (null == ordorder) {
            logger.info("获取order信息为null orderId = " + orderId);
        }
        else
        {
        	InvoiceResponseVO<Boolean> checkOrderInvoicedResp =  invoiceRemoteService.checkOrderInvoiced(orderId);
        	boolean invoicedFlag = checkOrderInvoicedResp.getBody();

            /*若已申请发票，则更新发票申请信息表中记录为已申请*/
            if (invoicedFlag)
            {
            	OrdApplyInvoiceInfo ordApplyInvoiceInfo = ordApplyInvoiceInfoDao.selectByPrimaryKey(id);
                //判空
                int updatecount = 0;
                if(ordApplyInvoiceInfo != null)
                {
                    logger.info("发票id = " + id + " 已申请发票， 更新订单申请信息表信息已申请发票");
                    ordApplyInvoiceInfo.setStatus(OrdInvoiceApplyEnum.STATUS_TYPE.APPLIED.name());
                    updatecount = ordApplyInvoiceInfoDao.updateByPrimaryKeySelective(ordApplyInvoiceInfo);
                }
                count = updatecount;
            }
        }
        return count;
	
	}

	
	 /**
     * 发票补偿页面 跳转到后台订单详情页 ,若订单状态已取消，更新发票信息表状态  已取消
	 * @return 
     */
	@Override
	public void updateApplyInfoStatusByOrderStatus(Long orderId) {
	    OrdOrder ordorder = orderUpdateService.queryOrdOrderByOrderId(orderId);
        if (null == ordorder) {
            logger.info("获取order信息为null orderId = " + orderId);
        }else {
                List<OrdApplyInvoiceInfo> oaiiList = ordApplyInvoiceInfoDao.selectByOrderId(orderId);
                for (OrdApplyInvoiceInfo ordApplyInvoiceInfo : oaiiList) {
                  //判空
                    if(ordApplyInvoiceInfo != null){
                        logger.info("订单 orderId = " + orderId + " 状态已取消， 更新订单申请信息表信息已取消");
                        ordApplyInvoiceInfo.setStatus(OrdInvoiceApplyEnum.STATUS_TYPE.CANCEL.name());
                        ordApplyInvoiceInfoDao.updateByPrimaryKeySelective(ordApplyInvoiceInfo);
                    }
                }

            }
           
	}

	@Override
	public OrdApplyInvoiceInfo selectAppliedInvoiceByParams(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return ordApplyInvoiceInfoDao.selectAppliedInvoiceByParams(param);
	}
	
    /**
     * @Description: 填充发票申请信息的联系人和地址信息 
     * @author Wangsizhi
     * @date 2017-12-4 下午2:51:31
     */
    private OrdApplyInvoicePersonAddress fillFullApplyInvoiceInfo(OrdApplyInvoiceInfo applyInvoice) {
        Long orderId = applyInvoice.getOrderId();
        
        if (null == orderId) {
            logger.info("OrdApplyInvoiceInfoServiceImpl fillFullApplyInvoiceInfo  orderId is null");
            return null;
        }
        
        logger.info("OrdApplyInvoiceInfoServiceImpl fillFullApplyInvoiceInfo  orderId=" + applyInvoice.getOrderId() + "---id=" + applyInvoice.getId() + "---" + applyInvoice.toString());
        
        OrdApplyInvoicePersonAddress ordApplyInvoicePersonAddress = new OrdApplyInvoicePersonAddress();
        //设置发票信息
        ordApplyInvoicePersonAddress.setId(applyInvoice.getId());
        ordApplyInvoicePersonAddress.setOrderId(applyInvoice.getOrderId());
        ordApplyInvoicePersonAddress.setTitle(applyInvoice.getTitle());
        ordApplyInvoicePersonAddress.setContent(applyInvoice.getContent());
        ordApplyInvoicePersonAddress.setAmount(applyInvoice.getAmount());
        ordApplyInvoicePersonAddress.setStatus(applyInvoice.getStatus());
        ordApplyInvoicePersonAddress.setUserId(applyInvoice.getUserId());
        ordApplyInvoicePersonAddress.setDeliveryType(applyInvoice.getDeliveryType());
        ordApplyInvoicePersonAddress.setPurchaseWay(applyInvoice.getPurchaseWay());
        ordApplyInvoicePersonAddress.setTaxNumber(applyInvoice.getTaxNumber());
        ordApplyInvoicePersonAddress.setBuyerAddress(applyInvoice.getBuyerAddress());
        ordApplyInvoicePersonAddress.setBuyerTelephone(applyInvoice.getBuyerTelephone());
        ordApplyInvoicePersonAddress.setBankAccount(applyInvoice.getBankAccount());
        ordApplyInvoicePersonAddress.setAccountBankAccount(applyInvoice.getAccountBankAccount());
        
        Integer elecInvoice = applyInvoice.getElecInvoice();
        if (null == elecInvoice) {
            logger.info("OrdApplyInvoiceInfoServiceImpl fillFullApplyInvoiceInfo  orderId=" + applyInvoice.getOrderId() + "---id=" + applyInvoice.getId() + "---applyInvoice-getElecInvoice is null");
            elecInvoice = 0;
        }
        ordApplyInvoicePersonAddress.setElecInvoice(elecInvoice);
        ordApplyInvoicePersonAddress.setReceiverEmail(applyInvoice.getReceiverEmail());
        
        Date createTime = applyInvoice.getCreateTime();
        createTime = (null == createTime ? applyInvoice.getUpdateTime() : createTime);
        ordApplyInvoicePersonAddress.setCreateTime(createTime);
        ordApplyInvoicePersonAddress.setElecInvoice(applyInvoice.getElecInvoice());
        ordApplyInvoicePersonAddress.setReceiverEmail(applyInvoice.getReceiverEmail());
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("orderId", orderId);
        List<OrdPerson> ordPersons = ordPersonDao.getOrdApplyInvoicePersonByOrderId(params);
        if (null == ordPersons || ordPersons.size() == 0) {
            logger.info("OrdApplyInvoiceInfoServiceImpl findInvoicePersonInfo  ordPersons is null orderId=" + orderId);
        }else {
            OrdPerson ordPerson = ordPersons.get(0);
            logger.info("OrdApplyInvoiceInfoServiceImpl findInvoicePersonInfo  ordPersons orderId=" + orderId + "---" + ordPerson.toString());
            //设置联系人信息
            ordApplyInvoicePersonAddress.setFullName(ordPerson.getFullName());
            ordApplyInvoicePersonAddress.setMobile(ordPerson.getMobile());
            
            Long ordPersonId = ordPerson.getOrdPersonId();
            Map<String, Object> params1 = new HashMap<String, Object>();
            params1.put("ordPersonId", ordPersonId);
            List<OrdAddress> ordAddressList = ordAddressDao.findOrdAddressList(params1);
            if (null == ordAddressList || ordAddressList.size() == 0) {
                logger.info("OrdApplyInvoiceInfoServiceImpl findInvoicePersonInfo  ordAddressList is null orderId=" + orderId + "---ordPersonId=" + ordPersonId);
            }else {
                OrdAddress ordAddress = ordAddressList.get(0);
                logger.info("OrdApplyInvoiceInfoServiceImpl findInvoicePersonInfo  ordAddressList orderId=" + orderId + "---ordPersonId=" + ordPersonId + "---" + ordAddress.toString());
                //设置地址
                ordApplyInvoicePersonAddress.setProvince(ordAddress.getProvince());
                ordApplyInvoicePersonAddress.setCity(ordAddress.getCity());
                ordApplyInvoicePersonAddress.setStreet(ordAddress.getStreet());
                ordApplyInvoicePersonAddress.setDistrict(ordAddress.getDistrict());
                ordApplyInvoicePersonAddress.setPostcode(ordAddress.getPostalCode());
            }
            
        }
        
        logger.info("OrdApplyInvoiceInfoServiceImpl findInvoicePersonInfo  ordAddressList orderId=" + orderId + "---id=" + ordApplyInvoicePersonAddress.getId() + "---" + ordApplyInvoicePersonAddress.toString());
        
        return ordApplyInvoicePersonAddress;
    }
    
    /**
     * @Description: 发票取消, 用户取消发票申请 
     */
    @Override
    public int revokeApplyInvoice(Long id){
        int count = 0;
        if (null == id) {
            logger.info("OrdApplyInvoiceInfoServiceImpl revokeApplyInvoice id=[" + id + "]");
        }else{
            logger.info("OrdApplyInvoiceInfoServiceImpl revokeApplyInvoice id=" + id);
            OrdApplyInvoiceInfo ordApplyInvoiceInfo = ordApplyInvoiceInfoDao.selectByPrimaryKey(id);
            //判空
            if(ordApplyInvoiceInfo != null){
                logger.info("订单 id = " + id + "订单 orderId = " + ordApplyInvoiceInfo.getOrderId() + "更新订单申请信息表信息取消发票");
                ordApplyInvoiceInfo.setStatus(OrdInvoiceApplyEnum.STATUS_TYPE.REVOKE.name());
                count = ordApplyInvoiceInfoDao.updateStatusByPrimaryKey(ordApplyInvoiceInfo);
                logger.info("OrdApplyInvoiceInfoServiceImpl revokeApplyInvoice success id=" + id);
            }
        }
        return count;
    }

    /**
     * @Description: 更新发票申请全部信息（发票、地址，关联订单），更新发票申请信息(状态除外)
     * @author Wangsizhi
     * @date 2017-12-4 下午2:56:55
     */
    @Override
    public void updateApplyInvoiceInfo(OrdApplyInvoicePersonAddress oapa) {
        if (null == oapa) {
            logger.info("OrdApplyInvoiceInfoServiceImpl updateApplyInvoiceInfo param OrdApplyInvoicePersonAddress is null");
            return;
        }
        Long orderId = oapa.getOrderId();
        logger.info("OrdApplyInvoiceInfoServiceImpl updateApplyInvoiceInfo orderId=" + orderId + "---" + oapa.toString());
        Long id = oapa.getId();
        
        logger.info("OrdApplyInvoiceInfoServiceImpl updateApplyInvoiceInfo id=" + id + "---" + oapa.toString());
        
        //设置发票申请信息
        OrdApplyInvoiceInfo ordApplyInvoiceInfo = ordApplyInvoiceInfoDao.selectByPrimaryKey(id);
        
        if (null == ordApplyInvoiceInfo) {
            logger.info("OrdApplyInvoiceInfoServiceImpl updateApplyInvoiceInfo select OrdApplyInvoiceInfo is null orderId=" + orderId);
            return;
        }
        
        OrdApplyInvoicePersonAddress beforeUpdate = fillFullApplyInvoiceInfo(ordApplyInvoiceInfo);
        
        logger.info("OrdApplyInvoiceInfoServiceImpl updateApplyInvoiceInfo before update id=" + id + "---" + beforeUpdate.toString());

        ordApplyInvoiceInfo.setTitle(oapa.getTitle());
        ordApplyInvoiceInfo.setContent(oapa.getContent());
        ordApplyInvoiceInfo.setDeliveryType(oapa.getDeliveryType());

        String purchaseWay = oapa.getPurchaseWay();
        String taxNumber = oapa.getTaxNumber();
        String buyerAddress = oapa.getBuyerAddress();
        String buyerTelephone = oapa.getBuyerTelephone();
        String bankAccount = oapa.getBankAccount();
        String accountBankAccount = oapa.getAccountBankAccount();
        Integer elecInvoice = oapa.getElecInvoice();
        String receiverEmail = oapa.getReceiverEmail();
        elecInvoice = null == elecInvoice ? 1 : elecInvoice;

        //是否包含电子发票带保险地址
        boolean hasInsIncluded = false;
        if (1 == elecInvoice) {
            ordApplyInvoiceInfo.setElecInvoice(elecInvoice);
            ordApplyInvoiceInfo.setReceiverEmail(receiverEmail);
            //电子发票时，快递方式改为自取
            ordApplyInvoiceInfo.setDeliveryType("SELF");
            //判断电子发票是否包含保险
            if (oapa.getStreet() != null && oapa.getFullName() != null && oapa.getMobile() != null){
                hasInsIncluded = true;
                ordApplyInvoiceInfo.setDeliveryType("EXPRESS");
            }
        }
        
        if (StringUtils.isBlank(purchaseWay)) {
            ordApplyInvoiceInfo.setPurchaseWay("personal");//personal 个人
            ordApplyInvoiceInfo.setTaxNumber(null);
        }else if (StringUtils.isNotBlank(purchaseWay)) {
            if (purchaseWay.equalsIgnoreCase("personal")) {
                ordApplyInvoiceInfo.setPurchaseWay("personal");//personal 个人
                ordApplyInvoiceInfo.setTaxNumber(null);
                ordApplyInvoiceInfo.setBuyerAddress(null);
                ordApplyInvoiceInfo.setBuyerTelephone(null);
                ordApplyInvoiceInfo.setBankAccount(null);
                ordApplyInvoiceInfo.setAccountBankAccount(null);
            }else if (purchaseWay.equalsIgnoreCase("company")) {
                ordApplyInvoiceInfo.setPurchaseWay("company");//company 公司
                ordApplyInvoiceInfo.setTaxNumber(taxNumber);
                ordApplyInvoiceInfo.setBuyerAddress(buyerAddress);
                ordApplyInvoiceInfo.setBuyerTelephone(buyerTelephone);
                ordApplyInvoiceInfo.setBankAccount(bankAccount);
                ordApplyInvoiceInfo.setAccountBankAccount(accountBankAccount);
            }
        }
        
        ordApplyInvoiceInfoDao.updateByPrimaryKey(ordApplyInvoiceInfo);
        logger.info("OrdApplyInvoiceInfoServiceImpl update ApplyInvoiceInfo success orderId=" + orderId);
        //发票联系人信息
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("orderId", orderId);
        List<OrdPerson> ordPersons = ordPersonDao.getOrdApplyInvoicePersonByOrderId(params);
        if (null == ordPersons || ordPersons.size() == 0) {
            logger.info("OrdApplyInvoiceInfoServiceImpl updateApplyInvoiceInfo findInvoicePersonInfo  ordPersons is null orderId=" + orderId);
            //无联系人说明是之前是电子发票，如本次改为纸质或电子带保险开票则新增联系人以及地址
            if (elecInvoice == 0 || hasInsIncluded){
                OrdPerson ordPerson = new OrdPerson();
                ordPerson.setFullName(oapa.getFullName());
                ordPerson.setMobile(oapa.getMobile());
                ordPerson.setObjectId(orderId);
                ordPerson.setObjectType(objectType);
                ordPerson.setPersonType(IReceiverUserServiceAdapter.RECEIVERS_TYPE.CONTACT.name());
                ordPersonDao.insertSelective(ordPerson);
                logger.info("OrdApplyInvoiceInfoServiceImpl updateApplyInvoiceInfo insert ordPerson success orderId=" + orderId + "---" + ordPerson.toString());

                Map<String, Object> perSonParams = new HashMap<>();
                params.put("orderId", orderId);
                List<OrdPerson> ordPersonList = ordPersonDao.getOrdApplyInvoicePersonByOrderId(perSonParams);
                Long ordPersonId = null;

                if (CollectionUtils.isNotEmpty(ordPersonList)) {
                    OrdPerson  orderPerson= ordPersons.get(0);
                    ordPersonId = orderPerson.getOrdPersonId();

                    //发票寄送地址信息
                    OrdAddress ordAddress = new OrdAddress();
                    ordAddress.setProvince(oapa.getProvince());
                    ordAddress.setCity(oapa.getCity());
                    ordAddress.setStreet(oapa.getStreet());
                    ordAddress.setPostalCode(oapa.getPostcode());
                    ordAddress.setOrdPersonId(ordPersonId);
                    ordAddress.setDistrict(oapa.getDistrict());
                    ordAddressDao.insertSelective(ordAddress);

                    logger.info("OrdApplyInvoiceInfoServiceImpl updateApplyInvoiceInfo ordAddress insert success orderId=" + orderId + "---" + ordPerson.toString());
                }

            }

        }else {
            OrdPerson orderPerson= ordPersons.get(0);
            Long ordPersonId = orderPerson.getOrdPersonId();
            Map<String, Object> params1 = new HashMap<>();
            params1.put("ordPersonId", ordPersonId);
            List<OrdAddress> ordAddressList = ordAddressDao.findOrdAddressList(params1);

            //有联系人说明是纸质或电子带保险，如本次改为电子则删除联系人以及地址，否则更新
            if (elecInvoice == 1 && !hasInsIncluded){
                ordPersonDao.deleteByPrimaryKey(orderPerson.getOrdPersonId());
                if (ordAddressList != null && ordAddressList.size() > 0){
                    ordAddressDao.deleteByPrimaryKey(ordAddressList.get(0).getOrdAddressId());
                }
            }else {
                String fullName = oapa.getFullName();
                String mobile = oapa.getMobile();

                orderPerson.setFullName(StringUtils.isNotBlank(fullName) ? fullName : orderPerson.getFullName());
                orderPerson.setMobile(StringUtils.isNotBlank(mobile) ? mobile : orderPerson.getMobile());
                //更新联系人
                ordPersonDao.updateByPrimaryKeySelective(orderPerson);
                logger.info("OrdApplyInvoiceInfoServiceImpl update ordPerson success orderId=" + orderId);

                if (null == ordAddressList || ordAddressList.size() == 0) {
                    logger.info("OrdApplyInvoiceInfoServiceImpl findInvoicePersonInfo updateApplyInvoiceInfo ordAddressList is null orderId=" + orderId + "---ordPersonId=" + ordPersonId);
                }else {
                    OrdAddress ordAddress = ordAddressList.get(0);
                    ordAddress.setProvince(StringUtils.isNotBlank(oapa.getProvince()) ? oapa.getProvince() : ordAddress.getProvince());
                    ordAddress.setCity(StringUtils.isNotBlank(oapa.getCity()) ? oapa.getCity() : ordAddress.getCity());
                    ordAddress.setDistrict(StringUtils.isNotBlank(oapa.getDistrict()) ? oapa.getDistrict() : ordAddress.getDistrict());
                    ordAddress.setStreet(StringUtils.isNotBlank(oapa.getStreet()) ? oapa.getStreet() : ordAddress.getStreet());
                    ordAddress.setPostalCode(StringUtils.isNotBlank(oapa.getPostcode()) ? oapa.getPostcode() : ordAddress.getPostalCode());
                    //更新地址
                    ordAddressDao.updateByPrimaryKeySelective(ordAddress);
                    logger.info("OrdApplyInvoiceInfoServiceImpl update ordAddress success orderId=" + orderId);
                }
            }

        }
        
        OrdApplyInvoicePersonAddress afterUpdate = fillFullApplyInvoiceInfo(ordApplyInvoiceInfo);
        logger.info("OrdApplyInvoiceInfoServiceImpl updateApplyInvoiceInfo success id=" + id + "---orderId=" + orderId);
        logger.info("OrdApplyInvoiceInfoServiceImpl updateApplyInvoiceInfo after update id=" + id + "---" + afterUpdate.toString());
    }

    /**
     * @Description: 根据订单Id查询  发票申请全部信息（发票、地址，关联订单）
     */
    @Override
    public List<OrdApplyInvoicePersonAddress> findAppInvFullInfoByOrderId(Long orderId){
        if (null == orderId) {
            logger.info("OrdApplyInvoiceInfoServiceImpl findAppInvFullInfoByOrderId orderId is null");
            return null;
        }
        logger.info("OrdApplyInvoiceInfoServiceImpl findAppInvFullInfoByOrderId orderId="+ orderId);
        List<OrdApplyInvoiceInfo> ordApplyInvoiceInfoList = ordApplyInvoiceInfoDao.selectByOrderId(orderId);
        if (null == ordApplyInvoiceInfoList || ordApplyInvoiceInfoList.size() <= 0) {
            logger.info("OrdApplyInvoiceInfoServiceImpl findAppInvFullInfoByOrderId no applyInvoice orderId=" + orderId);
            return null;
        }
        
        List<OrdApplyInvoicePersonAddress> oapaList = new ArrayList<OrdApplyInvoicePersonAddress>();
        logger.info("OrdApplyInvoiceInfoServiceImpl findAppInvFullInfoByOrderId orderId="+ orderId + "---ordApplyInvoiceInfoList.size=" + ordApplyInvoiceInfoList.size());
        for (OrdApplyInvoiceInfo oaii : ordApplyInvoiceInfoList) {
            OrdApplyInvoicePersonAddress ordApplyInvoicePersonAddress = fillFullApplyInvoiceInfo(oaii);
            oapaList.add(ordApplyInvoicePersonAddress);
        }
        logger.info("OrdApplyInvoiceInfoServiceImpl findAppInvFullInfoByOrderId orderId="+ orderId + "---oapaList.size=" + oapaList.size());
        logger.info("OrdApplyInvoiceInfoServiceImpl findAppInvFullInfoByOrderId orderId="+ orderId + "---oapaList:");
        for (OrdApplyInvoicePersonAddress oapa : oapaList) {
            logger.info(oapa.toString());
        }
        
        return oapaList;
    }
    
    
    
    
    
    /**
    * @Description:根据用户Id查询  发票申请全部信息（发票、地址，关联订单） 列表的总条数
    * 只返回状态为： PENDING("待申请"), CANCEL("订单取消"),(随订单取消发票申请), REVOKE("发票取消"),(用户取消发票申请)的发票申请记录
    */
    @Override
    public long findSpecAppInvFullInfoListByUserIdCount(String userId){
        long totalCount = ordApplyInvoiceInfoDao.findSpecAppInvFullInfoListByUserIdCount(userId);
        logger.info("OrdApplyInvoiceInfoServiceImpl findSpecAppInvFullInfoListByUserIdCount userId=" + userId + "---totalCount=" + totalCount);
        return totalCount;
    }
    
    /**
    * @Description:根据用户Id查询  发票申请全部信息（发票、地址，关联订单） 分页列表
    * 只返回状态为： PENDING("待申请"), CANCEL("订单取消"),(随订单取消发票申请), REVOKE("发票取消"),(用户取消发票申请)的发票申请记录
    */
    @Override
    public List<OrdApplyInvoicePersonAddress> findSpecAppInvFullInfoListByUserId(Map<String, Object> map){
        Object userId = (String) map.get("userId");
        Object _start = (long)  map.get("_start");
        Object _end = (long)  map.get("_end");
        
        if (null == userId || null == _start || null == _end) {
            logger.info("OrdApplyInvoiceInfoServiceImpl findSpecAppInvFullInfoListByUserId 入参不合法");
            return null;
        }
        logger.info("OrdApplyInvoiceInfoServiceImpl findSpecAppInvFullInfoListByUserId userId=" + userId 
                + "---_start=" + _start + "---_end=" + _end);
        List<OrdApplyInvoiceInfo> specAppInvFullInfoList = ordApplyInvoiceInfoDao.findSpecPageAppInvFullInfoListByUserId(map);
        if (null == specAppInvFullInfoList || specAppInvFullInfoList.size() <= 0) {
            logger.info("OrdApplyInvoiceInfoServiceImpl findSpecAppInvFullInfoListByUserId 查询结果为空 userId=" + userId.toString());
            return null;
        }
        ArrayList<OrdApplyInvoicePersonAddress> oapaList = new ArrayList<OrdApplyInvoicePersonAddress>();
        for (OrdApplyInvoiceInfo ordApplyInvoiceInfo : specAppInvFullInfoList) {
            OrdApplyInvoicePersonAddress fullApplyInvoiceInfo = fillFullApplyInvoiceInfo(ordApplyInvoiceInfo);
            oapaList.add(fullApplyInvoiceInfo);
        }
        logger.info("OrdApplyInvoiceInfoServiceImpl findSpecAppInvFullInfoListByUserId userId=" + userId + "---oapaList.size" + oapaList.size());
        
        logger.info("OrdApplyInvoiceInfoServiceImpl findSpecAppInvFullInfoListByUserId userId=" + userId + "---oapaList:");
        for (OrdApplyInvoicePersonAddress oapa : oapaList) {
            logger.info(oapa.toString());
        }
        
        return oapaList;
    }
    
    /**
    * @Description:根据用户Id、订单Id查询  发票申请全部信息（发票、地址，关联订单） 列表的总条数
    * 只返回状态为： PENDING("待申请"), CANCEL("订单取消"),(随订单取消发票申请), REVOKE("发票取消"),(用户取消发票申请)的发票申请记录
    */
    @Override
    public long listAppInvFullInfoByConditionCount(OrdAppInvInfoQueryVo oiiqv){
        if (null == oiiqv) {
            logger.info("OrdApplyInvoiceInfoServiceImpl listAppInvFullInfoByConditionCount oiiqv is null");
            return 0;
        }
        
        logger.info("OrdApplyInvoiceInfoServiceImpl listAppInvFullInfoByConditionCount oiiqv=" + oiiqv.toString());
        
        Long orderId = oiiqv.getOrderId();
        String userId = (oiiqv.getUserId()).trim();
        
        Map<String, Object> map = new HashMap<String, Object>();
        if (null != orderId) {
            map.put("orderId", orderId);
        }
        if (StringUtils.isNotEmpty(userId)) {
            map.put("userId", userId);
        }
        
        logger.info("OrdApplyInvoiceInfoServiceImpl listAppInvFullInfoByConditionCount map=" + JSONObject.fromObject(map).toString());
        
        long totalCount = ordApplyInvoiceInfoDao.listAppInvFullInfoByConditionCount(map);
        logger.info("OrdApplyInvoiceInfoServiceImpl listAppInvFullInfoByConditionCount orderId=" + orderId + "---userId=" + userId + "---totalCount=" + totalCount);
        return totalCount;
    }
    
    /**
    * @Description:根据用户Id、订单Id查询  发票申请全部信息（发票、地址，关联订单） 分页列表
    * 只返回状态为： PENDING("待申请"), CANCEL("订单取消"),(随订单取消发票申请), REVOKE("发票取消"),(用户取消发票申请)的发票申请记录
    */
    @Override
    public List<OrdApplyInvoicePersonAddress> listAppInvFullInfoByCondition(Map<String, Object> map){
        logger.info("OrdApplyInvoiceInfoServiceImpl listAppInvFullInfoByCondition map=" + JSONObject.fromObject(map).toString());
       
        Object _start = (long)  map.get("_start");
        Object _end = (long)  map.get("_end");
        
        if (null == _start || null == _end) {
            logger.info("OrdApplyInvoiceInfoServiceImpl listAppInvFullInfoByCondition 入参不合法");
            return null;
        }
        
        List<OrdApplyInvoiceInfo> specAppInvFullInfoList = ordApplyInvoiceInfoDao.listAppInvFullInfoByCondition(map);
        if (null == specAppInvFullInfoList || specAppInvFullInfoList.size() <= 0) {
            logger.info("OrdApplyInvoiceInfoServiceImpl listAppInvFullInfoByCondition 查询结果为空 ");
            return null;
        }
        ArrayList<OrdApplyInvoicePersonAddress> oapaList = new ArrayList<OrdApplyInvoicePersonAddress>();
        for (OrdApplyInvoiceInfo ordApplyInvoiceInfo : specAppInvFullInfoList) {
            OrdApplyInvoicePersonAddress fullApplyInvoiceInfo = fillFullApplyInvoiceInfo(ordApplyInvoiceInfo);
            oapaList.add(fullApplyInvoiceInfo);
        }
        logger.info("OrdApplyInvoiceInfoServiceImpl listAppInvFullInfoByCondition map=" + JSONObject.fromObject(map).toString() + "---oapaList.size" + oapaList.size());
        
        logger.info("OrdApplyInvoiceInfoServiceImpl listAppInvFullInfoByCondition map=" + JSONObject.fromObject(map).toString() + "---oapaList:");
        for (OrdApplyInvoicePersonAddress oapa : oapaList) {
            logger.info(oapa.toString());
        }
        
        return oapaList;
    }
    
    
    /**
     * @Description: 根据发票Id查询  发票申请全部信息（发票、地址，关联订单）
     */
    public OrdApplyInvoicePersonAddress findAppInvFullInfoById(Long id){
        if (null == id) {
            logger.info("OrdApplyInvoiceInfoServiceImpl findAppInvFullInfoById id is null");
            return null;
        }
        logger.info("OrdApplyInvoiceInfoServiceImpl findAppInvFullInfoById id=" + id);
        OrdApplyInvoiceInfo ordApplyInvoiceInfo = ordApplyInvoiceInfoDao.selectByPrimaryKey(id);
        if (null == ordApplyInvoiceInfo) {
            logger.info("OrdApplyInvoiceInfoServiceImpl findAppInvFullInfoById no applyInvoice id=" + id);
            return null;
        }
        OrdApplyInvoicePersonAddress oapa = fillFullApplyInvoiceInfo(ordApplyInvoiceInfo);
        logger.info("OrdApplyInvoiceInfoServiceImpl findAppInvFullInfoById id=" + id + "---" + oapa.toString());
        return oapa;
    }

    /**
     * 根据 订单id查询 是否可申请发票 可以申请发票，则返回：true; 不可以申请发票, 则返回：false
     * 如果订单存在一条 状态为【PENDING("待申请")、* APPLIED("已申请")、 MANUAL("人工申请"),】,则不可以申请发票, 其余为可以申请发票。
     * @param orderId 订单orderId
     * @return
     */
    public Boolean checkIfApplyInvoiceByOrderId(Long orderId){
        if (null == orderId) {
            logger.info("OrdApplyInvoiceInfoServiceImpl checkIfApplyInvoiceByOrderId orderId is null");
            return null;
        }
        logger.info("OrdApplyInvoiceInfoServiceImpl checkIfApplyInvoiceByOrderId orderId=" + orderId);
        List<OrdApplyInvoiceInfo> oaList = ordApplyInvoiceInfoDao.findSpecStatusApplyInvoiceByOrderId(orderId);
        
        boolean result = true;//默认true为可以申请发票
        if (null != oaList && oaList.size() > 0) {
            //已存在处于 状态为【PENDING("待申请")、* APPLIED("已申请")、 MANUAL("人工申请")】的发票申请
            result = false;
            logger.info("OrdApplyInvoiceInfoServiceImpl checkIfApplyInvoiceByOrderId 已存在申请发票记录，不可以申请发票 orderId=" + orderId);
        }
        logger.info("OrdApplyInvoiceInfoServiceImpl checkIfApplyInvoiceByOrderId orderId=" + orderId + "---result=" + result);
        return new Boolean(result);
    }
}
