<#--页眉-->
<#import "/base/spring.ftl" as spring/>
<!DOCTYPE html>
<html>
<head>
<title></title>
</head>
<body>
<#--页面导航-->
<div id="logResultList" class="divClass">
<table>
                        <tbody>
                            <tr>
                                <td class="e_label w10">下单时间：</td>
                                <td>${order.createTime?string('yyyy-MM-dd HH:mm') !''} </td>
                            </tr>
                            <tr>
                                <td class="e_label">订单来源：</td>
                                <td>${distributorName !''} 
                                
                                  <#if order.distributorId=="2" > 
	                                <strong>[${order.backUserId!''}]
	                                </strong>
                                 <#else>
				                  	 
				                  </#if>
                                
                                </td>
                            </tr>
                              <!--
                              <tr>
                                <td class="e_label">销售渠道ID：</td>
                                <td>${order.distributorId !''} </td>
                            </tr>
                            -->
                             <tr>
                                <td class="e_label">渠道代码/名称：</td>
                                <td>${order.distributorCode !''}/${distributionChannelName!''} </td>
                            </tr>
                             <tr>
                                <td class="e_label">分销商渠道ID：</td>
                                <td>${order.distributionChannel !''} </td>
                            </tr>
                            <tr>
                                <td class="e_label">付款方式：</td>
                                <td>
                                <#if order.paymentTarget=="PREPAID" > 
                            	 预付
				                  <#else>
				                  	 现付
				                  </#if>
				                  
                                </td>
                            </tr>

                            <#if ordItemAperiodicExp?? && ordItemAperiodicExp?length gt 0>
                                <tr>
                                    <td class="e_label">有效期：</td>
                                    <td>
                                        ${ordItemAperiodicExp}
                                    </td>
                                </tr>
                            </#if>


                           <tr>
                           <input type="hidden" name="isTicketType" id="isTicketType" value="${isTicket!''}">
                            <input type="hidden" name="backAmount" id="backAmount" value="${backAmount}">
                                <td class="e_label">退改政策：</td>
                                <#if order.orderSubType=='STAMP'>
                                 <td> ${cancelStrategyType}</td>
                                <#else>
							    <#if isFreedomNewRule == "Y" ||  isTicket == "Y">
								   <td>
                                       <#if order.buCode == "DESTINATION_BU"&& order.subCategoryId =="181"><table><td>此商品已被打包&nbsp;&nbsp;</td><td></#if>
                                        ${refoundStr}
                                       <#if order.buCode == "DESTINATION_BU"&& order.subCategoryId =="181"></td></table></#if>
                                   </td>
								<#else>
									   <td>
                                           <#if order.buCode == "DESTINATION_BU"&& order.subCategoryId =="181">此商品已被打包&nbsp;&nbsp;</#if>
                                       ${cancelStrategyTypeStr}
										   <#if orderItem.cancelStrategy?? && orderItem.cancelStrategy=="RETREATANDCHANGE" >
											   扣款金额[(${deductAmountStr}元)]
											   <#if orderItem.lastCancelTime??>
												   <#if isGreaterNow==1 >
													   <span class="lineae_line">${orderItem.lastCancelTime?string('yyyy-MM-dd HH:mm')!''}
														   前无损取消</span>
												   <#else>
												   ${orderItem.lastCancelTime?string('yyyy-MM-dd HH:mm')!''}  前无损取消
												   </#if>
											   </#if>
										   </#if>
									   </td>
								   
							   </#if>
							    </#if>
							   
							   
                            </tr>
                           
                            <tr>
                                <td class="e_label">所属公司：</td>
                                <td>${filialeName!''}</td>
                            </tr>
                            <tr>
                                <td class="e_label">所属产品经理：</td>
                                <td>
                                <#if productManager.realName??>
                                	${productManager.realName!''}
                                 </#if>
                                </td>
                            </tr>
                        </tbody>
                    </table>
</div>
<#--页脚-->
<#include "/base/foot.ftl"/>
</body>
</html>
<script type="text/javascript">

         
 </script>
