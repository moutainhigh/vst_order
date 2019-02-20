<#assign mis=JspTaglibs["/WEB-INF/pages//tld/lvmama-tags.tld"]>
<!DOCTYPE html>
<html>
<head>
<title>邮轮订单-订单处理</title>
<#include "/base/head_meta.ftl"/>
</head>
<body>

    <div class="iframe_header">
        <i class="icon-home ihome"></i>
        <ul class="iframe_nav">
            <li><a href="javaScript:">首页</a>：</li>
            <li><a href="javaScript:">订单管理</a> ></li>
            <li class="active">订单处理</li>
        </ul>
    </div>
    <form method="post" id="dataForm" onsubmit="return false;">
    <div class="order_main iframe_content mt10" id="iframeDiv">
        <div class="order_msg clearfix">
        
        		【主订单负责人】  ${orderPrincipal!''}
            	【主订单】  订单号：${order.orderId !''}
             	【包含子订单】： ${childOrderMonitorRstList?size}个
            <div class="order_seach">查询订单号：<input type="text" id="orderIdSeach" name="orderIdSeach" number="true"/><a class="btn ml10" id="orderIdSeachButton" target="_blank">查询</a> </div>
            <#if order.clientIpAddress=="180.169.51.82" && order.distributorId==3>
               	（请特别特别注意，此订单疑似驴妈妈内部成员下单）
             </#if>          
            
            
            
            </strong></span>
            <#--
            <a class="ml20" href="javaScript:">上一个关联订单++</a> |
            <a href="javaScript:">下一个关联订单++</a>
            -->
        </div>
        <div class="solid_border"></div>
        <div class="sidebar equalheight_item">
            <div class="side_setbox sidebox">
                <table class="sidebar_table" >
                    <thead>
                        <tr>
                            <th>订单状态名称</th>
                            <th>状态</th>
                            <th>相关操作</th>
                        </tr>    
                    </thead>
                    <tbody>
                    
                     <input type="hidden" name="orderId" id="orderId" value="${order.orderId!''}">
                     <input type="hidden" name="orderStatus" id="orderStatus" value="${order.orderStatus!''}">
                     <input type="hidden" name="paymentStatus" id="paymentStatus" value="${order.paymentStatus!''}">
                     <input type="hidden" name="resourceStatus" id="resourceStatus" value="${order.resourceStatus!''}">
                     <input type="hidden" name="infoStatus" id="infoStatus" value="${order.infoStatus!''}">
                     <input type="hidden" name="invoiceStatus" id="invoiceStatus" value="${order.invoiceStatus!''}">
                     <input type="hidden" name="viewOrderStatus" id="viewOrderStatus" value="${order.viewOrderStatus!''}">
                     
                     
                     
                      
                       <#if auditMap['PRETRIAL_AUDIT']==true>
	                        <tr>
	                         	 <td>信息预审</td>
	                            <td>
	                            
	                             <input type="radio" id="pretrialAudit" name="radioManage" value="pretrialAudit"/>
	                             
	                            </td>
	                             <td>
	                             </td>
	                           
	                        </tr>
	                    
	                    <#elseif isDonePretrialAudit==true>
	                         
		                    <tr>
	                            <td>信息预审</td>
	                            <td>
	                            <img src='../../img/pass.png' width='20' height='20' alt='通过'/>
	                            </td>
	                             <td>
	                             </td>
	                        </tr>
	                        
	                     </#if>
                        
                        <tr>
                            <td>审核状态</td>
                            <td>
                           
                            <#if hasInfoAndResourcePass==true>
                             
                             	<img src='../../img/pass.png' width='20' height='20' alt='通过'/>
                             
	                         <#else>
	                         	 审核中
                             </#if> 
                             
                            
                            
                            </td>
                              <td><a id="viewChildOrderList" href="javaScript:">[查看子订单状态]</a></td>
                        </tr>
                        
                        
		               <#if order.orderStatus=="NORMAL">  
		                      
	                        <#if  auditMap['PAYMENT_AUDIT']==true>
	                        
	                         <tr>
	                            <td>催支付</td>
	                            <td>
	                             <input type="radio" id="paymentAudit" name="radioManage" value="paymentAudit"/>
	                            </td>
	                            <td></td>
	                        </tr>
	                        <#elseif isDonePaymentAudit==true>
		                    <tr>
	                            <td>催支付</td>
	                            <td>
	                            <img src='../../img/pass.png' width='20' height='20' alt='通过'/>
	                            </td>
	                            <td></td>
	                        </tr>
	                        </#if>
	                        
	                        <#if  auditMap['TIME_PAYMENT_AUDIT']==true>
	                        
	                         <tr>
	                            <td>小驴分期催支付</td> 
	                            <td>
	                             <input type="radio" id="timePaymentAudit" name="radioManage" value="timePaymentAudit"/>
	                            </td>
	                            <td></td>
	                        </tr>
	                        <#elseif isDoneTimePaymentAudit==true>
		                    <tr>
	                            <td>小驴分期催支付</td>
	                            <td>
	                            <img src='../../img/pass.png' width='20' height='20' alt='通过'/>
	                            </td>
	                            <td></td>
	                        </tr>
	                        
	                        </#if>
	                       
	                        <#if   auditMap['NOTICE_REGIMENT_AUDIT']==true>
	                         
		                        <tr>
		                         	 <td>通知出团</td>
		                            <td>
		                            
		                             <input type="radio" id="noticeRegimentAudit" name="radioManage" value="noticeRegimentAudit"/>
		                             
		                            </td>
		                             <td>
		                              <a href="javascript:void(0);" class="viewSendNoticeList" data="${order.orderId}" contactEmail="${ordPersonContact.email!''}">[查出团]</a>
		                             </td>
		                           
		                        </tr>
	                        
	                        <#elseif isDoneNoticeRegimentAudit==true>
		                         
			                    <tr>
		                            <td>通知出团</td>
		                            <td>
		                            <img src='../../img/pass.png' width='20' height='20' alt='通过'/>
		                            </td>
		                             <td>
		                            <a href="javascript:void(0);" class="viewSendNoticeList" data="${order.orderId}" contactEmail="${ordPersonContact.email!''}">[查出团]</a>
		                             </td>
		                        </tr>
		                        
		                     </#if>
		                     
                        </#if>
 						<#if order.orderStatus=="CANCEL">
 						
		 						<#if auditMap['CANCEL_AUDIT']==true > 
		 						
		 						  <tr>
		                            <td>订单取消已确认</td>
		                            <td>
		                            
	                        		 <input type="radio"  id="cancleConfirmed" name="radioManage" value="cancelStatusConfim"/>
	                            
	                        		 </td>
		                         </tr>
		                         
		                         <#elseif isDoneCancleConfirmedtAudit==true>
		                         
		                         <tr>
		                            <td>订单取消已确认</td>
		                            <td>
		                            
		                             	<img src='../../img/pass.png' width='20' height='20' alt='通过'/>
	                            
	                        		 </td>
		                         </tr>
		                        
	                        	 </#if> 
		                        
                         </#if>
                    </tbody>
                </table>
            </div>
            
            <#if ordTravelContract.contractTemplate != "">
	            <div class="solid_line"></div>
	            <div class="side_setbox sidebox">
	            <h4>签约状态
	                <span style="color:red;font-size:14px;">
	                
	                </span></h4>
	                <table class="sidebar_table" >
	                    <tbody>
	                        <tr>
		                            <td>合同状态：</td>
		                            <td>
		                            
		                             ${contractStatusName!''}
		                             
		                            </td>
		                            <td>
		                            <a target="_blank" href="/vst_order/order/orderManage/showTravelContractList.do?orderId=${order.orderId!''}">[查看合同]</a>
		                            </td>
		                        </tr>
	                         
	                    </tbody>
	                </table>
	            </div>
            </#if>      
            
            <div class="solid_line"></div>
            <div class="side_setbox sidebox">
                 <h4>支付状态<span style="color:red;font-size:14px;">
                
                
                  <#if order.paymentType=="PREAUTH" > 
                  
                  		 <#if order.paymentStatus=="PAYED" > 
		                     (已预授权支付)
		                  <#else>
		                  	  (强制预授权未支付)
		                  </#if>
		                  
                  </#if>
                  
               
                
                </span></h4>
                <table class="sidebar_table" >
                    <tbody>
                        <tr>
                            <td>付款情况：</td>
                            <td>
                            ${paymentStatusStr}
                            
                        
                            <#if hasDepositsAmount==true && order.depositsAmount <=order.actualAmount> 
							(定金已收)
                             </#if> 
                            </td>
                            	<td><a id="orderPaymentInfo" href="javaScript:">[支付相关操作]</a></td>
                        </tr>
                        <tr>
                            <td>应收款：</td>
                            <td>RMB ${order.oughtAmount/100}元</td>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td>已收款：</td>
                            <td>RMB ${order.actualAmount/100}元</td>
                        </tr>
                        
                        <#if payPromotion != null>
                         <tr>
                          <td>支付立减 ：</td>
                          <td>RMB ${payPromotion/100}元</td>
                          </tr>
                          <tr>
                            <td>剩余款：</td>
                            <td>RMB ${(order.oughtAmount-order.actualAmount-payPromotion)/100}元</td>
                           </tr>
                           
                           <#else>
                            <tr>
                            <td>剩余款：</td>
                            <td>RMB ${(order.oughtAmount-order.actualAmount)/100}元</td>
                           </tr>
                           </#if>
                 
                        
                        <#if hasDepositsAmount==true> 
                        
                         <tr>
                            <td>定金应收款：</td>
                            <td>RMB ${order.depositsAmount/100}元</td>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td>定金已收款：</td>
                            <td>RMB ${depositsOughtAmount}元</td>
                            <td>
                            </td>
                        </tr>
                         <tr>
                            <td>定金剩余款：</td>
                            <td>RMB ${excessFunds}元</td>
                        </tr>
                        
                        </#if> 
                         
                         <tr>
                            <td >支付等待时间：</td>
                            <td>
                            	${waitPaymentTime!''}  
                            </td>
                            <td>
                            <#if order.normal && hasPreauthBook==false> 
                            <a href="javaScript:" id="updateWaitPaymentTime">[修改时间]</a>
                            </#if> 
                            </td>
                         </tr>
                          <tr>
                             <td>
                             
                             </td>
                             <td>
                            </td>
                         </tr>
                         
                    </tbody>
                </table>
            </div>
            <#if isPaymentFlag=='Y' >
             <div class="solid_line"></div>
             <div class="side_setbox sidebox">
                <table><tr><td><h4>支付方式</h4></td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="javaScript:" id="paymentTerm">[设置支付方式]</a></td></tr></table> 
                <table class="payment_table">
                    <tbody>
                    <#if isDownpayFlag=="Y">
                        <tr>
                            <td>${payType}：</td>
                            <td>RMB ${payAmount/100}元&nbsp;&nbsp;${payStatus}</td>
           				</tr>
           			 </#if>	
           			 <tr>
                          <td></td>
                          <td></td>
           			 </tr>
           			</tbody>
           		</table>
            </div>     
          </#if>
            <div class="solid_line"></div>
            <div class="side_setbox sidebox" >
                <h4>订单取消<span style="color:red;font-size:14px;">(订单${orderStatusStr})</span></h4>
				 <#if order.orderStatus=="COMPLETE" || order.orderStatus=="CANCEL"> 
				   <div id="cancleDiv"  disabled ="true">
				  <#else>
				   <div id="cancleDiv">
                  </#if>
                
                <table class="sidebar_table" >
                    <tbody>
                    
                    
					<#list orderCancelTypeList as cancelType> 
					
					 <#if cancelType_index%2==0 >
						</tr>
						<tr>
					 </#if>  
					 
					 	 <td>
					 	 
					 	  <label class="radio">
					 	 <#if order.orderStatus=="COMPLETE" || order.orderStatus=="CANCEL"> 
							<input type="radio" name="radioManage" value="${cancelType.dictDefId}" data="cancelOP" disabled ="true"/> 
						<#else> 
							<#if cancelType.dictDefId==order.cancelCode > 
								<input type="radio" name="radioManage" value="${cancelType.dictDefId}" data="cancelOP" onclick="orderCancelTypeChange(this)" checked="true"/> 
							<#else> 
								<input type="radio" name="radioManage" value="${cancelType.dictDefId}" data="cancelOP" onclick="orderCancelTypeChange(this)" /> 
							</#if> 
						</#if>

					 	  ${cancelType.dictDefName}
					 	 </label>
					 	 
					 	 </td>
                         
					 
					</#list>	
					
					
                    </tbody>
                </table>
                <div>
                    <span>取消原因：</span>
                    <#if order.orderStatus=="COMPLETE" || order.orderStatus=="CANCEL"> 
						<select id="cancleReason" disabled ="true"> 
							<option value="0">选择原因</option> 
						</select> 
					<#else> 
							<#if ''==order.cancelCode > 
								<select id="cancleReason"> 
									<option value="0">选择原因</option> 
								</select> 
							<#else> 
								<select id="cancleReason"> 
									<option value="0">${order.reason!''}</option> 
								</select> 
						</#if> 
					</#if>
                  
                   
                </div>
                </div>
            </div>
        </div>
        <div class="main equalheight_item">
            <div class="main_con clearfix">
                <div class="main_order_msg">
                    <div><a id="viewOrderAttachment" class="fr" href="javaScript:">附件(<b>${orderAttachmentNumber}</b>)</a>订单备注记录:</div>
                    <textarea style="width:285px; height:120px;" id="orderRemark" name="orderRemark" onkeyup="checkRemarkLength()">${order.orderMemo!''}</textarea>
                    <span class="fr" id="zsRemark">0/500字</span>
                    <div class="operate mt10">
                    <a class="btn btn_cc1" id="saveButton" >确认修改</a>
                    
                    <a class="btn btn_cc1" id="clearButton" href="javaScript:clearradio();" >清空选择</a>
                    
                    
                    
                    </div>
                    <div class="mt20">
                   
                        <ul class="supplier_list">
                          
<#--
                            <li><a id="ordSale"  target="_blank" href="/super_back/ord/sale/OrdSaleAddJump.zul?orderId=${order.orderId}&sysCode=VST" title="售后服务">售后服务</a></li>
-->
                            <li><a id="ordSale"  target="_blank" href="/sales_front/ord/sale/toOrdSale/${order.orderId}" title="售后服务">售后服务</a></li>

                            <li><a id="addMessage"   href="javaScript:" title="预订通知">预订通知</a></li>
                            <li><a id="uploadOrderAttachment" href="javaScript:" title="上传附件">上传附件</a></li>
                            <li><a id="showLog" href="javaScript:" title="查看日志">查看日志</a></li>
                            <li><a id="showInvoiceMessage" target="_blank" href="/vst_order/order/orderInvoice/showInvoiceInfo.do?orderId=${order.orderId!''}" title="发票">发票信息</a></li>
                         
                            <li><a id="findOrderVisaApprovalQueryList"   href="javaScript:" title="查看签证审核">查看签证审核</a></li>
                            <li><a id="findInsurancePolicyList"   target="_blank" href="/vst_insurant/insurancePolicy/findList.do?orderId=${order.orderId}"  title="查看投保状态">查看投保状态</a></li>
                            <#if order.orderStatus=="NORMAL" &&  order.paymentStatus == "UNPAY" >  
                              <@mis.checkPerm permCode="5920"><li><a id="changeAmount" href="javaScript:" title="修改价格">修改价格</a></li></@mis.checkPerm>
                            </#if>
                            <li><a id="showOrderSMS" href="javaScript:;" title="">查看短信</a></li>
                            <#--<li><a id="showOrderRemark" href="javaScript:;" onclick="showOrderRemark(${order.orderId});" title="">订单备注</a></li>-->
                            
                            <li><a id="showSoundRec" href="javaScript:" title="查看录音">查看录音</a></li>
							<li><a id="showUntreatedComplaintCall" href="javaScript:" title="未处理用户信息">未处理用户信息</a></li>
							<li><a id="addComplaintCall" href="javaScript:" title="新增用户信息">新增用户信息</a></li>
							<li><a id="myCreatedComplaintCall" href="javaScript:" title="我创建的用户信息">我创建的用户信息</a></li>
							<li><a id="addWork" href="javaScript:" title="新增工单">新增工单</a></li>
                            <#if showTransfer == true>
                                <li><a id="showTransfer" href="javaScript:" title="申请资金转移">申请资金转移</a></li>
                            </#if>
                            <#if showLosses == true>
                                <li><a id="showLosses" href="javaScript:" title="申请定金核损">申请定金核损</a></li>
                            </#if>
							<li><a id="showCbpInfo" href="javaScript:" title="中行支付查询">中行支付查询</a></li>
                            <@mis.checkPerm permCode="6363"><li><a id="showUserOrder" href="javaScript:" title="个人中心显示订单">个人中心显示订单</a></li></@mis.checkPerm>
                            <#if order.categoryId ==8 >
                                <li><a  target="_blank" href="http://www.lvmama.com/tnt_order/tntShipConfirmController/showShipConfirm?orderId=${order.orderId}" title="查看确认单">查看确认单</a></li>
                            </#if>
                        </ul>
                    </div>
                </div>
               
                <div class="main_order_info">
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
                            <tr>
                                <td class="e_label" id="distributorCodeTitle">渠道代码/名称：</td>
                                <td id="distributorCodeInfo">${order.distributorCode !''}/${distributionChannelName!''} </td>
                            </tr>
                             <tr>
                               <td class="e_label" id="distributionChannelTitle">分销商渠道ID：</td>
                                <td id="distributionChannelInfo">${order.distributionChannel !''} </td>
                            </tr>
                            
                            <tr>
                               <td class="e_label">CpsID：</td>
                                <td>${order.distributionCpsID !''} </td>
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
                           
                            <tr>
                                <td class="e_label">退改政策：</td>
                                <td>
                                人工退改
                                <#--
                                ${cancelStrategyTypeStr}
                                  -->
                                </td>
                            </tr>
                            
                            <tr>
                                <td class="e_label">所属公司：</td>
                                <td>${order.zhFilialeName !''}</td>
                            </tr>
                            <tr>
                                <td class="e_label">所属产品经理：</td>
                                <td>${productManager.realName!''}</td>
                            </tr> 
                        </tbody>
                    </table>
                    <div class="solid_line mt10 mb10"></div>
                    <table>
                        <tbody>
                            <tr>
                                <td class="e_label w10">产品名称：</td>                       
                                <td>
                                 <a href="http://www.lvmama.com/youlun/${ordOrderPack.productId}" target="_blank" title="点击打开前台页面">[${ordOrderPack.productName}]</a></td>
                                </td>
                            </tr>
                            
                             <!--显示团结算标识-->
                            <#if groupSettleFlag??>
                                <tr>
                                    <td class="e_label">是否团结算：</td>
                                    <td>${groupSettleFlag}</td>
                                </tr>
                            </#if>
                            
                            <tr>
                                <td class="e_label">出发日期：</td>  
                                <td><strong>[${arrivalDays}天]</strong> ${order.visitTime?string('yyyy-MM-dd') !''} 至 ${orderPackContentMap['end_sailing_date']!''}
                                
                                 </td>
                            </tr>
                            <tr>
                                <td class="e_label">游客姓名：</td>
                                <td><strong>[${travellerNum!'0'}人]</strong>${travellerName!''}</td>
                            </tr>
                           
                           
                            <tr>
                                <td class="e_label">开航日期：</td>
                                <td> ${orderPackContentMap['start_sailing_date']!''} </td>
                            </tr>
                            <tr>
                                <td class="e_label">上船地点：</td>
                                <td> ${orderPackContentMap['up_place']!''} </td>
                            </tr>
                            <tr>
                                <td class="e_label">下船地点：</td>
                                <td> ${orderPackContentMap['down_place']!''} </td>
                            </tr>
                            <tr>
                                <td class="e_label">所属航线：</td>
                                <td> ${orderPackContentMap['route']!''} </td>
                            </tr>
                            <tr>
                                <td class="e_label">用户退款总金额：</td>
                                <td>${refunds}</td>
                            </tr>
                            <tr>
                                <td class="e_label">补偿用户总金额：</td>
                                <td>${compensations}</td>
                            </tr>
                            <tr>
                                <td class="e_label" style="width:140px;">优惠券减少总金额：</td>
                                <td>
                                <#if favorUsageAmount??>
                                ${favorUsageAmount?string("#.##")} <a id="favorUsageDetail" href="javascript:void(0)">[查看明细]</a>
                                <#else>
                                    0.00
                                </#if>
                                </td>
                            </tr>
                            <tr>
                                <td class="e_label">门店优惠金额：</td>
                                <td>
                                <#if favourableO2oAmount??>
                                ${favourableO2oAmount?string("#.##")} <a id="favourableO2oAmount" href="javascript:void(0)">[查看明细]</a>
                                <#else>
                                    0.00
                                </#if>
                                </td>
                            </tr>
                            <tr>
                                <td class="e_label">促销减少总金额：</td>
                                <td>${totalOrderAmount} <a id="promotionDetail" href="javascript:void(0)">[查看明细]</a></td>
                            </tr>
                            <tr>
                                <td class="e_label">分销渠道减少总金额：</td>
                                <td>${distributionPrice!''}</td>
                            </tr>
                            <tr>
                                <td class="e_label">奖金支付的总金额：</td>
                                <td>${order.bonusAmount/100!''}</td>
                            </tr>
                            <tr>
                                <td class="e_label">白条支付总金额：</td>
                                <td>
                                <#if btOrderPaidAmountFen??>
                                ${btOrderPaidAmountFen}
                                <#else>
                                    0.00
                                </#if>
                                </td>
                            </tr>
                            <tr>
                                <td class="e_label">订单金额-价格修改：</td>
                                <td>${totalAmountChange} <a id="orderAmountChangeDetail" href="javascript:void(0)">[查看明细]</a></td>
                            </tr>
                            <tr>
                                <td class="e_label">返现金额：</td>
                                <td>${order.getRebateAmountYuan()}</td>
                            </tr>
                            
                            <tr>
                                <td class="e_label">特殊要求：</td>
                                <td>${order.remark!''}</td>
                            </tr>
                            <tr>
                                <td class="e_label">确认方式：</td>
                                <td>短信确认</td>
                            </tr>
                            <tr>
                                <td class="e_label">相关订单：</td>
                                <td>
                                <#if otherOrder==true> 
                                <a href="${rc.contextPath}/ord/order/orderMonitorList.do?userId=${order.userId}&productId=${ordOrderPack.productId}&orderStatus=NORMAL" target="_blank">[另有订单]</a>
                                </#if> 
                                <a href="${rc.contextPath}/ord/order/orderMonitorList.do?userId=${order.userId}" target="_blank">[同用户订单]</a></td>
                            </tr>
                        </tbody>
                    </table>
                    
                    
           
                </div>
            </div>
           
            <div class="solid_line mt10 mb10"></div>
             <div id="findOrdPersonBooker">
               <div class="order_msg clearfix">
	            	正在使劲加载数据中...
	            </div> 
             </div>
             <!-- <#include "/order/invoice/invoiceInfoBase.ftl"/> -->
            
             <table id="childOrderInfo" class="p_table table_center mt20">
                <thead>
                    <tr>
                        <th>客服负责人</th>
                        <th>资源审核人</th>
                        <th>子订单号</th>
                        <th>子订单状态</th>
                        <th>类型</th>
                        <th>包含商品</th>
                        <th>预订份数</th>
                        <th>销售单价</th>
		                <th>订单总价</th>
                        <th>订单实付总金额</th>
                        <!--<th>支付立减分摊总金额</th>-->
                        <th>出游日期</th>
                        <th>订单备注</th>
                    </tr>
                </thead>
                <tbody>
                
                <#list childOrderMonitorRstList as orderMonitorRst> 
                    <tr>
                        <td>
                       ${orderMonitorRst.principal!''}
                        </td>
                        <td>
                       ${orderMonitorRst.resourceApprover!''}
                        </td>
                        <td>
                        
                        <a href="/vst_order/order/orderShipManage/showChildOrderStatusManage.do?orderItemId=${orderMonitorRst.orderItemId!''}&orderType=child" target="_blank">${orderMonitorRst.orderItemId!''}</a>
						</td>
                        <td>
                        ${orderMonitorRst.currentStatus!''}
						</td>
                        <td> 
                        ${orderMonitorRst.childOrderTypeName!''}
                        </td>
                      <td>
                       ${orderMonitorRst.productName!''}
                  		</td>
                       <td> 
                      <!-- TODO -->
                        <#if orderMonitorRst.childOrderType == 'category_cruise'> 
                      	 	${orderMonitorRst.personCount!''} 人/ ${orderMonitorRst.buyCount!''} 间
						<#else>
							<#if orderMonitorRst.childBuyCount?? || orderMonitorRst.adultBuyCount??>
								成人数：${orderMonitorRst.adultBuyCount!'0'}份</br>
								儿童数：${orderMonitorRst.childBuyCount!'0'}份
							<#else>
								${orderMonitorRst.buyCount!''}份
							</#if>
						 
						</#if>
                       </td>
                       <td>${orderMonitorRst.buyItemPrice!''}</td>
		               <td>${orderMonitorRst.buyItemTotalPrice!''}</td>
                        <td>
                            <#if !(orderMonitorRst.actualPaidAmountList)?? || (orderMonitorRst.actualPaidAmountList)?size = 0>
                                0
                            </#if>
                            <#list orderMonitorRst.actualPaidAmountList as actualPaidPriceTypeVO>
                            ${actualPaidPriceTypeVO.declaration!''}<#if actualPaidPriceTypeVO.declaration?? && actualPaidPriceTypeVO.declaration != "">:</#if>${actualPaidPriceTypeVO.price/100}<br/>
                            </#list>
                        </td>
                       <!--<td>
                            <#if !(orderMonitorRst.payProAmountList)?? || (orderMonitorRst.payProAmountList)?size = 0>
                                0
                            </#if>
                            <#list orderMonitorRst.payProAmountList as payPriceTypeVO>
                            ${payPriceTypeVO.price/100}元<br/>
                            </#list>
                        </td>-->
                       <td> ${orderMonitorRst.visitTime!''} </td>
                       <td> ${orderMonitorRst.orderItemMemo!''}<#if orderMonitorRst.orderAttachmentNumber?? && orderMonitorRst.orderAttachmentNumber &gt; 0>(<a class="viewChildOrderAttachment" href="javaScript:">查看附件</a>)</#if></td>
                    </tr>
                </#list>
                </tbody>
            </table>
            
            <#if travellerDelayFlag=='Y'>
            <table class="p_table table_center mt20">
            
                <thead>
                    <tr>
                        <td colSpan ="2" align="center">
	                        <p align="center">
								<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="saveConfirm">保存</button>
							</p>
                		</td>
                 	</tr>   
                </thead>
                <tbody>
		            <tr>
		            	<td style="border-right:#fff 0px solid;text-align:left">
		            		请确认出游人是否包含70周岁（含）以上老人同行
		            	</td>
		            	<td style="border-left:#fff 0px solid;">
			            	<input type="radio" name="containOldMan" value="Y"  <#if ordOrderTravellerConfirm.containOldMan== 'Y'>checked="checked"</#if>/>有
			            	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			            	<input type="radio" name="containOldMan" value="N" <#if ordOrderTravellerConfirm.containOldMan== 'N'>checked="checked"</#if>/>没有
		            	</td>
		            </tr>
		            <tr >
		            	<td style="border-right:#fff 0px solid;text-align:left">
		            		请确认同行人中有不满6个月的婴儿
		            	</td>
		            	<td style="border-left:#fff 0px solid;">
			            	<input type="radio" name="containBaby" value="Y" <#if ordOrderTravellerConfirm.containBaby== 'Y'>checked="checked"</#if>/>有
			            	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			            	<input type="radio" name="containBaby" value="N"  <#if ordOrderTravellerConfirm.containBaby== 'N'>checked="checked"</#if>/>没有
		            	</td>
		            </tr>
		            <tr>
		            	<td style="border-right:#fff 0px solid;text-align:left">
		            		请确认同行中是否有孕妇
		            	</td>
		            	<td style="border-left:#fff 0px solid;">
			            	<input type="radio" name="containPregnantWomen" value="Y" <#if ordOrderTravellerConfirm.containPregnantWomen== 'Y'>checked="checked"</#if>/>有
			            	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			            	<input type="radio" name="containPregnantWomen" value="N" <#if ordOrderTravellerConfirm.containPregnantWomen== 'N'>checked="checked"</#if>/>没有
		            	</td>
		            </tr>
		         </tbody>
            </table>
            </#if>
            
            
            
            <table class="p_table table_center mt20">
                <thead>
                 <tr>
                        <td colSpan ="13" align="center">
                        <p align="center">
							<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="editPersonButton">修改游客信息</button>
							<#if travellerDelayFlag=='Y' && travellerLockFlag =='Y'>
								<div style="color:#FF0000;">出游人信息前台已锁定</div>
							</#if>
						</p>
                		 </td>
                 </tr>   
                    <tr>
                    	<th width="60px">入住房间</th>
                        <th width="50px">中文姓名</th>
                        <th width="40px">英文姓</th>
                        <th width="80px">英文名</th>
                        <th width="30px">性别</th>
                        <th width="50px">出生地</th>
                        <th width="60px">出生日期</th>
                        <th width="80px">证件号码</th>
                        <th width="50px">签发地</th>
                        <th width="60px">签发日期</th>
                        <th width="60px">有效日期</th>
                        <th width="60px">手机号</th>
                        <th width="30px">护照</th>
                    </tr>
                </thead>
                <tbody>
                <#if personMap?? && personMap?size &gt; 0>
                	<#list personMap?keys as cangfangkey>
                		<#assign roomMap = personMap[cangfangkey]>
                		<#if roomMap?? && roomMap?size &gt; 0>
                			<#list roomMap?keys as roomkey>
                				<#assign personList = roomMap[roomkey]>
                					<#if personList?? && personList?size &gt; 0>
                						<#list personList as person> 
						                    <tr>
						                        <#if person_index == 0> 
						                        	<td rowspan="<#if personList?? && personList?size &gt; 0>${personList?size}</#if>">第${roomkey}间： <br/>${person.checkInRoomName!''}</td>
												</#if>					                    
						                        <td>
						                      		${person.fullName!''}
						                        </td>
						                        <td>
						                        	${person.lastName!''}
												</td>
						                        <td>
						                        	${person.firstName!''}
												</td>
												 <td >
							                        <#if person.gender == "MAN"> 
							                      	 男
													<#elseif person.gender == "WOMAN">
													女
													</#if>
						                  		 </td>
						                         <td> ${person.birthPlace!''}</td>
						                         <td>
							                         <#if person.birthday?exists>
							                         ${person.birthday?string('yyyy-MM-dd')}
							                         </#if>
						                         </td>
							                     <td>
							                       <#if person.idTypeName!="客服联系我">
							                       ${person.idNo!''}
							                       </#if>
							                  	 </td>
						                  		 <td > ${person.issued!''}</td>
							                  	 <td>
							                         <#if person.issueDate?exists>
							                          ${person.issueDate?string('yyyy-MM-dd')}
							                        </#if>
							                     </td>
						                     	 <td >
							                         <#if person.expDate?exists>
							                          ${person.expDate?string('yyyy-MM-dd')}
							                         </#if>
						                         </td>
						                        <td >${person.mobile!''} </td>
						                        <td >
						                        	<#if person.passportUrl?exists>
							                         <button class="pbtn pbtn-small btn-ok viewPassPortUrl" data="${person.ordPersonId}">查验</button>
							                         <div style="display:none;">
							                         	<div id="passportUrl${person.ordPersonId}"><img style="width:100%;hright:100%;" src="${person.passportUrl}"/></div>
							                         </div>  
							                         </#if>
							                    </td>
						                    </tr>
                						</#list>
                					</#if>
                			</#list>
                		</#if>
                	</#list>
                </#if>
                </tbody>
            </table>
            
            <div id="findGoodsPersonList">
               <div class="order_msg clearfix">
	            	正在使劲加载数据中...
	           </div> 
            </div>            
            
            <#if ordPersonEmergencyList?? && ordPersonEmergencyList?size &gt; 0> 
            <table class="p_table table_center mt20">
                <thead>
                 <tr>
                        <td colSpan ="2" align="center">
                        <p align="center" style="margin-top:20px;">
							紧急联系人（<button class="pbtn pbtn-small btn-ok" style="margin-top:0px;" id="editPersonEmergencyButton">修改</button>）
						</p>
                		 </td>
                 </tr>   
                    <tr>
                        <th>中文姓名</th>
                        <th>联系人手机</th>
                    </tr>
                </thead>
                <tbody>
                <#list ordPersonEmergencyList as ordPersonEmergency> 
                   <tr>
                     <td>${ordPersonEmergency.fullName!''}</td>
                     <td>${ordPersonEmergency.mobile!''}</td>
                   </tr>
                 </#list>
                </tbody>
             </table>
			</#if>	 
        </div>
    </div>
    
    </form>
    <div id="logResult" style="display:none;margin-left:40px;margin-right:35px;">
     
     </div>
    <#include "/base/foot.ftl"/>
</body>
</html>
    <#if ordPersonEmergencyList?? && ordPersonEmergencyList?size &gt; 0> 
     <script type="text/javascript">
     	// 紧急联系人
		var editPersonEmergencyButtonDialog;
		$("#editPersonEmergencyButton").bind("click",function(){
		   editPersonEmergencyButtonDialog = new xDialog("/vst_order/order/orderShipManage/showUpdatePersonEmergency.do",{"orderId":${order.orderId!''}},{title:"修改紧急联系人",width:800});
		});
	</script>
	</#if>	
	
    <script type="text/javascript">
		//中行积分支付订单记录
		var showCbpInfoDialog;
		$("#showCbpInfo").bind("click",function(){
			var operaterName = encodeURIComponent(encodeURIComponent("${loginUserId}"));
			showCbpInfoDialog = new xDialog("/sales_front/cbp/query?orderId=" + ${order.orderId!''} + "&operaterName=" + operaterName + "&orderMoney=" + ${order.actualAmount },{},{title:"中行积分支付订单记录",width:1350,height:510,iframe:true});
		});
        //显示个人中心订单
        $("#showUserOrder").bind("click",function(){
            var orderId = ${order.orderId};

            var r = confirm("是否需要在个人中心订单列表显示该订单？");
            if(r){
                $.ajax({
                    type : "get",
                    url :"/vst_order/order/orderDelete/updateOrderDelFlag.do?deleteFlag=N&orderId="+orderId,
                    success:function(data){
                        if(data.success){
                            alert("已在个人中心列表显示该订单!");
                        }else{
                            alert(data.message);
                        }
                    }
                });
            }

        });
		//新增工单
		var addWorkDialog;
		$("#addWork").bind("click",function(){
			addWorkDialog = new xDialog("/pet_back/work/order/add.do?orderId=" + ${order.orderId!''},{},{title:"新增工单",width:1200,height:750,iframe:true});
		});
		//未处理用户信息
		var showUntreatedComplaintCallDialog;
		$("#showUntreatedComplaintCall").bind("click",function(){
    		var title = "用户 " + "${ordPersonBooker.fullName}" + " 的未处理信息";
    		var userName = encodeURIComponent(encodeURIComponent("${ordPersonBooker.fullName}"));
    		var entryPeople = encodeURIComponent(encodeURIComponent("${loginUserId}"));
    		var param = "orderId=${order.orderId}&refreshOnClose=Y&userName=" + userName + "&entryPeople=" + entryPeople;
    		showUntreatedComplaintCallDialog = new xDialog("http://super.lvmama.com/sales_front/complaintCall/orderDetail/showComplaintCallByCustomer.do?" + param,{},{title:title,width:1200,height:750,iframe:true});
	    });
		
		//新增用户信息
		var addComplaintCallDialog;
		$("#addComplaintCall").bind("click",function(){
    		var userName = encodeURIComponent(encodeURIComponent("${ordPersonBooker.fullName}"));
    		var entryPeople = encodeURIComponent(encodeURIComponent("${loginUserId}"));
    		var param = "orderId=${order.orderId}&refreshOnClose=N&userName=" + userName + "&entryPeople=" + entryPeople;
			addComplaintCallDialog = new xDialog("http://super.lvmama.com/sales_front/complaintCall/orderDetail/showExtCreateComplaintCall.do?" + param,{},{title:"客户提示信息录入",width:800,height:550,iframe:true});
	    });
		
		//我创建的用户信息
		var myCreatedComplaintCallDialog;
		$("#myCreatedComplaintCall").bind("click",function(){
    		var entryPeople = encodeURIComponent(encodeURIComponent("${loginUserId}"));
    		var param = "entryPeople=" + entryPeople;
    		myCreatedComplaintCallDialog = new xDialog("http://super.lvmama.com/sales_front/complaintCall/orderDetail/showExtMyCreatedComplaintCall.do?" + param,{},{title:"我创建的用户信息",width:1200,height:750,iframe:true});
	    });
     //发票信息
//		var showInvoiceDialg;
//		$("#showInvoiceMessage").bind("click",function(){
//			 var param={"orderId":${order.orderId!''}};
//			 showInvoiceDialg = new xDialog("/vst_order/order/orderInvoice/ord/loadInvoices.do",param,{title:"发票详情",width:950});
//         });
    	
    	//查看短信
		var showOrderSMSDialog;
		$("#showOrderSMS").bind("click",function(){
			 showOrderSMSDialog = new xDialog("/vst_order/order/ordSmsSend/showOrdSmsList.do?orderId=" + ${order.orderId!''},{},{title:"查看短信内容",width:1200,iframe:true});
         });
    	//订单备注
    	var showOrderRemarkDialog;
		function showOrderRemark(orderId){
			showOrderRemarkDialog = new xDialog("/vst_order/ord/ordRemarkLog/showOrdRemarkLogList.do?flag=N&orderId=" + ${order.orderId!''},{},{title:"订单备注",width:865,height:400,scrolling:"yes",iframe:true});
			$(".dialog-body").find(".dialog-content").find("iframe:first").attr("scrolling","yes");
		}	             
    
    var showAmountDialog;
        var  updateWaitPaymentTimeDialog;
        $("#updateWaitPaymentTime").bind("click",function(){
		
			var paymentTime= "${waitPaymentTime!''}";
			if(paymentTime!="")
			{
				var waitPaymentTime="${waitPaymentTime}";
         		updateWaitPaymentTimeDialog = new xDialog("/vst_order/order/orderShipManage/showUpdateWaitPaymentTime.do",{"orderId":"${order.orderId!''}","waitPaymentTime":waitPaymentTime},{title:"修改支付等待时间",width:620});
        
			}else{
				alert('支付等待时间为空,不可修改');
			}
		});

      	//查验护照图片
      	var showPassPortUrlDialog;
    	$(".viewPassPortUrl").click(function(){
    		var ordPersonId = $(this).attr("data");
    		showPassPortUrlDialog = pandora.dialog({
    	        width: 700,
    	        title: "护照图片",
    	        mask : true,
    	        cancel: true, 
            cancelValue: "关闭",
            	zIndex: 3000,
        cancelClassName: "pbtn pbtn-small btn-cancel",
    	        content: $("#passportUrl"+ordPersonId)
    		});
    	});
        
    	var addOrdFuncRelationDialog,messageDialog;
        $(function () {
        	//回显订单取消原因
        	var orderStatus = "${order.orderStatus }";
        	var cancelCode = "${order.cancelCode }";
        	var reason = "${order.reason }";
	        if('CANCEL' == orderStatus) {
	        	var radios = document.getElementsByName("radioManage");
	        	for (i = 0; i < radios.length; i++) {
	       	        if (radios[i].value == cancelCode) {
       	        		$("input[name='radioManage'][value="+cancelCode+"\]").attr("checked",true); 
       	        		var cancleReason = $("#cancleReason");
       	        		cancleReason.append("<option value='"+reason+"'>"+reason+"</option>");
       	        		cancleReason.val(reason);
       	        		break;
       	        	}
       	        }
       	    }
            $('.J_tip').lvtip({
                templete: 2,
                place: 'bottom-left',
                offsetX: 0,
                events: "live"  
            });
            

			if(${messageCount}>0){
			  messageDialog = new xDialog("/vst_order/order/orderShipManage/findComMessageList.do",{"orderId":${order.orderId!''},"orderType":"parent"},{title:"查看预订通知",width:1200});
		     }
     
      		if(${compliantCallsCount}>0){
        		var title = "用户 " + "${ordPersonBooker.fullName}" + " 的未处理信息";
      			var userName = encodeURIComponent(encodeURIComponent("${ordPersonBooker.fullName}"));
        		var entryPeople = encodeURIComponent(encodeURIComponent("${loginUserId}"));
        		var param = "orderId=${order.orderId}&refreshOnClose=Y&userName=" + userName + "&entryPeople=" + entryPeople;
        		showUntreatedComplaintCallDialog = new xDialog("http://super.lvmama.com/sales_front/complaintCall/orderDetail/showComplaintCallByCustomer.do?" + param,{},{title:title,width:1200,height:750,iframe:true});
      		}
      		
      		
        });
        
        function clearradio(){                           //取消radio的选择
			var x=document.getElementsByName("radioManage");   
			for(var i=0;i<x.length;i++){ //对所有结果进行遍历，如果状态是被选中的，则将其选择取消
				if (x[i].checked==true)
				{
					x[i].checked=false;
				}
			}
			$("#cancleReason").val("0");
			
		}
       /**
       function keyDown() {  
　　 　　      var keycode = event.keyCode;  
			if(13==keycode)
			{
				var orderIdSeach=$("#orderIdSeach").val();
				if($.trim(orderIdSeach)==''){
	         		alert("订单号不能为空");
	         		return；
	         	}
	         	document.getElementById("orderIdSeachButton").href="/vst_order/ord/order/orderMonitorList.do?orderId="+orderIdSeach;
	         	document.getElementById("orderIdSeachButton").focus();
	         	
	         	//document.getElementById("orderIdSeachButton").click();
	         	
	         	
			}
　　 　　     
　　    	 }  

		document.onkeydown = keyDown;
       
       */
         
		var addMessageDialog;
		$("#addMessage").bind("click",function(){

			var orderRemark=$.trim($("#orderRemark").val());
			var param={"orderId":${order.orderId!''},"orderRemark":orderRemark,"orderType":"parent"};
			addMessageDialog = new xDialog("/vst_order/order/orderShipManage/showAddMessage.do",param,{title:"创建预订通知",width:800});
         
         });
         
          var showLogDialog;
         $("#showLog").bind("click",function(){

			var param="objectType=ORD_ORDER_ORDER&objectId="+${order.orderId}+"&sysName=VST";
            showLogDialog =new xDialog("/lvmm_log/bizLog/showVersatileLogList?"+param,{},{title:"查看日志",iframe:true,width:1000,hight:300,iframeHeight:680,scrolling:"yes"});
         
         });
         
         $("#showSoundRec").bind("click",function(){

			var data="objectId="+${order.orderId}+"&page="+1;
			showLogDialog = new xDialog("/vst_order/order/orderShipManage/showSoundRecList.do",data,{title:"查看录音",width:1200});
         });
         
        $(function(){
         	//关联callid与orderid
         	var callId = "${callid}";
         	var orderId = ${order.orderId};
         	
			if (callId == "undefined" || callId.length == 0 || callId == "null"){
				return;
			}
         	
         	var options = {
					url:"/vst_order/order/orderShipManage/saveCallIdAndOrderId.do",
					type:"post",
					data:{
					    "callId":callId,
						"orderId":orderId
					},
					datatype:"json",
					success:function(jsonobj){			
						if (jsonobj.err == "Y"){
							alert("callid关联错误:"+jsonobj.msg);
						}
						else if (jsonobj.err == "N"){
						    //alert("callid:"+callId+"已与订单:"+orderId+"关联!");
						}
					}        	      
         		};
         		
         	$.ajax(options);	
         });
         
 		
     
 		$("#viewChildOrderList").bind("click",function(){
			
			 //$('html, body, .content').animate({scrollTop: $(document).height()}, 300); 
			 $('html, body, .content').animate({scrollTop: $(childOrderInfo).height()}, 300);
			 
         
         });
         
        $("#logResult div.paging a").live("click",function(){
        	var $form=$(this).parents("div.Pages");
        	var page=$(this).attr("page");
        	var url=$form.attr("url");
        	if(url==""||typeof(url)=="undefined"||page==""||typeof(page)=='undefined'){
        		return;
        	}
        	$.post(url,
			  {"page":page},
			   function(result){
			   
			  // alert(result);
			   $("#logResult").html(result);
			   
			});	
        });
        
        
		 var updatePersonDialog;
        $("#personUpdate").bind("click",function(){
			updatePersonDialog = new xDialog("/vst_order/order/orderShipManage/showUpdatePerson.do",{"ordPersonId":${ordPersonContact.ordPersonId}},{title:"修改联系人",width:400});
		});       
		        
		        
		 var faxDialog,sendOderFaxDialog;
        $("#sendOderFax").bind("click",function(){
        	var orderRemark=$.trim($("#orderRemark").val());
			sendOderFaxDialog = new xDialog("/vst_order/order/orderShipManage/showManualSendOrderFax.do",{"orderId":${order.orderId!''},"orderRemark":orderRemark,"source":"noInfoPass"},{title:"发送凭证",width:600});
		});    
		var findEbkFaxListDialog;
		$("#findEbkFaxList").bind("click",function(){
			findEbkFaxListDialog = new xDialog("/vst_certif/ebooking/task/findEbkAndFaxList.do",{"orderId":${order.orderId!''}},{title:"凭证查询",width:1300});
		});  
		
		
		var editPersonButtonDialog;
		$("#editPersonButton").bind("click",function(){
      		editPersonButtonDialog = new xDialog("/vst_order/order/orderShipManage/showUpdateTourist.do",{"orderId":${order.orderId!''}},{title:"修改游客",width:1300});
         });
         
         //检查是否数字
		function isNum(a)
		{
		    var reg = /^d+(.d+)?$/;
		    reg.test(a);
		}
              
        $("#orderIdSeachButton").bind("click",function(){
         
         	var orderIdSeach=$("#orderIdSeach").val();
         	
         	
         	if(!$("#dataForm").validate({
				rules : {
					orderIdSeach : {
						number : true
					},
					orderIdSeach : {
						required : true
					}
						}
			}).form()){
						$(this).removeAttr("disabled");
						return false;
			}
		
		
         	
         	document.getElementById("orderIdSeachButton").href="/vst_order/ord/order/orderMonitorList.do?orderId="+orderIdSeach;
         	//	window.showModalDialog("/vst_order/ord/order/orderMonitorList.do","orderId:"+orderIdSeach,"resizable:yes");
       		 // addOrdFuncRelationDialog = new xDialog("/vst_order/ord/order/orderMonitorList.do",{"orderId":orderIdSeach},{title:"订单查询列表",width:800,height:500});
         	
         	
         
         });
        
        var certificateDialog,orderSendSmsDialog,saledAppliedDialog;
        
       $("#saveButton").bind("click",function(){
       
       		var operation;
       		var cancelCode;
       		var cancleReason;
       		var orderRemark=$.trim($("#orderRemark").val());
       		var radioValue="updateOrderRemark";
       		var distributorCode = "${order.distributorCode !''}";
       		var orderStatus = "${order.orderStatus !''}";
       		var paymentStatus = "${order.paymentStatus !''}";
       		
       		
       		if($('input[name="radioManage"]:radio:checked').length<=0)
       		{
       			radioValue="updateOrderRemark";
       		}else if("${order.orderStatus}"=="CANCEL"){
       		
       			var radioVal=$('input[name="radioManage"]:radio:checked').val();
       			if( radioVal=="pretrialAudit" ||  radioVal=="INFOPASS" || radioVal=="RESOURCEPASS" || radioVal=="certificateStatus" || radioVal=="paymentAudit" || radioVal=="cancelStatusConfim")
       			{
       				radioValue=radioVal;    
       			}else{
       				radioValue="updateOrderRemark";
       				   	
       			}
       		}else{
       			radioValue=$('input[name="radioManage"]:radio:checked').val(); 
       		}
       		
       		if("updateOrderRemark"==radioValue){
       			operation="updateOrderRemark";
       		}else if("INFOPASS"==radioValue){
       			operation="infoStatus";
       			var param={"orderId":${order.orderId!''},"infoStatus":"${order.infoStatus!''}","orderRemark":orderRemark,"operation":operation};
       			
       			faxDialog = new xDialog("/vst_order/order/orderShipManage/showSendOrderFax.do",param,{title:"发送凭证",width:600});
       			return;
       		}else if("RESOURCEPASS"==radioValue){
       			operation="resourceStatus";
       			/**
       			var param={"orderId":${order.orderId!''},"resourceStatus":"${order.resourceStatus!''}","orderRemark":orderRemark,"operation":operation,mobile:"${ordPersonContact.mobile!''}"};
       			orderSendSmsDialog = new xDialog("/vst_order/order/orderShipManage/showOrderSendSms.do",param,{title:"发送短信",width:600});
       			return;
       			*/
       		}else if("certificateStatus"==radioValue){
       			operation="certificateStatus";
       			
       			/**
				var param={"orderId":"${order.orderId!''}","orderRemark":orderRemark,"operation":operation,"isSupplierOrder":"${isSupplierOrder!''}"};
       			
       			certificateDialog = new xDialog("/vst_order/order/orderShipManage/showAddCertificate.do",param,{title:"凭证确认",width:600});
				return;
       		*/
       		
       			
       		}else if("paymentAudit"==radioValue){
       			operation="paymentAudit";
       		
       		}else if("timePaymentAudit"==radioValue){
       			operation="timePaymentAudit";
       		
       		}else if("cancelStatusConfim"==radioValue){
       			operation="cancelStatusConfim";
       		}else if("noticeRegimentAudit"==radioValue){
       			operation="noticeRegimentAudit";
       		}else if("pretrialAudit"==radioValue){
       			operation="pretrialAudit";
       		}else {
	       		/**
	       		if("RESOURCE_NO_CONFIM"==radioValue || "CUSTOMER_NOTICE"==radioValue
	       			 || "INFO_NO_PASS"==radioValue || "ABANDON_ORDER_REPEAT"==radioValue || "OTHER_REASON"==radioValue){
	       		*/	
       			cancleReason=$("#cancleReason").val();
       			if(cancleReason=='0')
				{
					alert("取消原因还未选择");
					return;
				}
       			 
       			operation="cancelStatus"; 
       			cancelCode=radioValue;
       			
       			var cancleReasonText=$("#cancleReason").find("option:selected").text();
       			
       			
       			
       		}
       		//分销订单并且订单正常并且已支付${order.orderId}
       		if(operation == "cancelStatus" && isDistributor(distributorCode) && 'NORMAL' == orderStatus && 'PAYED' == paymentStatus) {
				var param = "actualAmount=" + "${order.actualAmount/100}" + "&operation="+operation+"&cancelCode="+cancelCode
					+"&cancleReasonText="+cancleReasonText+"&orderRemark="+orderRemark+"&orderId="+orderId;
				saledAppliedDialog = new xDialog("/vst_order/order/orderManage/showSaledApplied.do",param,{title:"售后申请确认",width:600});
       		}else{
	       		//遮罩层
	    		var loading = pandora.loading("正在努力保存中...");		
		
	       		var formData=$("#dataForm").serialize()+"&operation="+operation+"&cancelCode="+cancelCode+"&cancleReasonText="+cancleReasonText;
	       		//alert(formData);
	       		$.ajax({
				   url : "/vst_order/order/orderShipManage/updateOrderStatus.do",
				   data : formData,
				   type:"POST",
				   dataType:"JSON",
				   success : function(result){
				   		//var message=result.message;
				   		if(result.code=="success" ){
				   			loading.close();
				   		  alert(result.message);
				   		  // window.location.reload();
						 window.location.href=window.location.href;
				   		}else {
				   			loading.close();
				   			 alert(result.message);
				   		  	 window.location.reload();
				   		}
				   }
				});	
       		}
						
		});
	

   	function isDistributor(obj) {
   		var arr = new Array(['DISTRIBUTOR_API', 'DISTRIBUTOR_B2B', 'DISTRIBUTOR_DAOMA', 'DISTRIBUTOR_YUYUE', 'DISTRIBUTOR_SG']);  
   		return IsInArray(arr, obj);
   	}
   	function IsInArray(arr,val){  
   	　　var testStr=','+arr.join(",")+",";  
   	　　return testStr.indexOf(","+val+",")!=-1;  
   	}
	function checkRemarkLength(){
	        var orderRemark=document.getElementById('orderRemark');
	        var remarkLength=orderRemark.value.length;
	        if(remarkLength>500)
	        {
		       // alert("备注长度小于等于500!");
		        //document.getElementById("saveButton").disabled=true;
		        $("#saveButton").attr("disabled",true);
		        $("#saveButton").hide();
		        $("#zsRemark").attr("style","color:red");
	        }else{
	        	//document.getElementById("saveButton").disabled=false;
	        	$("#saveButton").removeAttr("disabled");
	        	$("#saveButton").show();
	        	$("#zsRemark").attr("style","");
	        }
	        $("#zsRemark").html(remarkLength+"/500字");
        }
        
        
         function orderCancelTypeChange(obj)
         {  
	         //alert();
	         var cancelType=obj.value;
	         var param="dictDefId="+cancelType+"&needSelect=true";
	       
	       $.ajax({
			   url : "/vst_order/order/ordCommon/findBizDictData.do",
			   data : param,
			   type:"POST",
			   dataType:"JSON",
			   success : function(data){
			   		  $("#cancleReason").html("");
			   		  $.each(data,function(i){
						var valueText=this.dictId;
						var text=this.dictName;
						if((i+1)==data.length  && (cancelType=="200" || cancelType=="201") )
						{
							$("#cancleReason").append("<option  value="+valueText+">"+text+"</option>");
						}else{
						
							$("#cancleReason").append("<option value="+valueText+">"+text+"</option>");
						}
						
			        })
			   		
			   }
			});	
			
         
         }
         
         $("#cancleReason").change(function(){
         
         		var cancleReasonText=$("#cancleReason").find("option:selected").text();
         		cancleReasonText=$.trim(cancleReasonText);
         		//var cancleReason=$("#cancleReason").val();
         		if(cancleReasonText=='其他')
         		{
         			var orderRemark=$.trim($("#orderRemark").html());
         			if(orderRemark!=''){
         				orderRemark+="  其他";
         				$("#orderRemark").html(orderRemark);
         			}else{
         				$("#orderRemark").html("其他");
         			}
         			
         			
         		}
         		
         		
         
         });


        //申请资金转移
        var showTransferDialog;
        $("#showTransfer").bind("click",function () {
            showTransferDialog = new xDialog("/vst_order/order/depositRefund/showAudit/TRANSFER/${order.orderId}.do",{},{title:"申请资金转移",width:900});
        });

        //申请定金核损
        var showLossesDialog;
        $("#showLosses").bind("click" ,function () {
            showLossesDialog = new xDialog("/vst_order/order/depositRefund/showAudit/LOSSES/${order.orderId}.do",{},{title:"申请定金核损",width:900,height:500});
        });


        //产品经理登录加载
        var showProcessTransferDialog;
        var showProcessLossesDialog;
        $(function () {
            //资产转移申请审批
            if ($("#showTransfer").html() != null) {
                $.get("/vst_order/order/depositRefund/checkShowProcessAudit/TRANSFER/${productManager.userId}/${order.orderId}.do" , function (data) {
                    if(data.code == 200){
                        //加载审批页
                        showProcessTransferDialog = new xDialog("/vst_order/order/depositRefund/showProcessAudit/TRANSFER/${order.orderId}.do",{},{title:"资金转移申请处理",width:1200});
                    }
                });
            }

            //定金核损申请审批
            if ($("#showLosses").html() != null) {
                $.get("/vst_order/order/depositRefund/checkShowProcessAudit/LOSSES/${productManager.userId}/${order.orderId}.do" , function (data) {
                    if(data.code == 200){
                        //加载审批页
                        showProcessLossesDialog = new xDialog("/vst_order/order/depositRefund/showProcessAudit/LOSSES/${order.orderId}.do",{},{title:"定金核损申请处理",width:1200});
                    }
                });
            }

        });



     var findOrderVisaApprovalQueryListDialog;
         $("#findOrderVisaApprovalQueryList").bind("click",function(){

			//var param={"searchOrderId":${order.orderId!''}};
			//findOrderVisaApprovalQueryListDialog = new xDialog("/vst_back/visa/approval/showOrderVisaApprovalQueryList.do?searchOrderId=${order.orderId}","",{title:"查看签证审核",iframe:true,width:900});
			findOrderVisaApprovalQueryListDialog = new xDialog("/visa_prod/visa/approval/showOrderVisaApprovalQueryList.do?searchOrderId=${order.orderId}",{},{title:"查看签证审核",width:900});
         
         });
         
         
         var viewSendNoticeListDialog;

		$("a.viewSendNoticeList").bind("click",function(){
			
			var orderId=$(this).attr("data");
			var contactEmail = $(this).attr("contactEmail");
			viewSendNoticeListDialog = new xDialog("/vst_order/order/orderShipManage/viewSendNoticeList.do",{"orderId":orderId,"sourceType":"notice","contactEmail":contactEmail},{title:"查看出团通知",width:900});
			
		});
		
		
		var ordPersonBookerData="orderType=parent&orderId=${order.orderId}";
     	$.post("/vst_order/order/orderManage/findOrdPersonBooker.do",
		   ordPersonBookerData,
		   function(result){
		   
		 	$("#findOrdPersonBooker").html(result);
  		});	
		
      	var goodsPersonData="orderId=${order.orderId!''}&&isChild=N";
        $.post("/vst_order/order/orderShipManage/findGoodsPersonList.do",
			   goodsPersonData,
			   function(result){
			 	$("#findGoodsPersonList").html(result);
      	});		
		
     </script>
     
<#--业务JS,added by wenzhengtao 20131220-->
<script type="text/javascript">
	//定义上传附件弹出窗口变量
    var uploadOrderAttachmentDialog;
    //定义查看附件弹出窗口变量
    var viewOrderAttachmentDialog;
    //定义发送短信弹出窗口变量
    var orderSendSmsDialog;
    //定义查看支付记录窗口变量
    var orderPaymentInfoDialog;
    var paymentTermDialog;
     var showAmountDialog;
     
     //是否锁定游玩人系统提示窗口
     var editTravellerLockFlagDialog;
    //定义全局的orderId，给引入的js使用
    var orderId = '${order.orderId}';
    var mobile = '${ordPersonContact.mobile}';
    var orderType='${RequestParameters.orderType!''}';
    var ordType='${RequestParameters.orderType!''}';
    var payPromotion='${payPromotion}'; //立减
    if(payPromotion==null || payPromotion=='' || payPromotion == undefined){
      payPromotion=0;
    }
	var orderOughtAmount='${order.oughtAmount}'; //应收款
	var actualAmount='${order.actualAmount}'; //已收款
    var oughtAmount=parseInt(orderOughtAmount) - parseInt(actualAmount) - parseInt(payPromotion);
    
    $("#saveConfirm").bind("click",function(){
    	
    	var loading = pandora.loading("正在努力保存中...");
    	var orderId = $("#orderId").val();
    	
    	var containOldMan =$(':radio[name="containOldMan"]:checked').val();
    	var containBaby =$(':radio[name="containBaby"]:checked').val();
    	var containPregnantWomen =$(':radio[name="containPregnantWomen"]:checked').val();
    	var formData="orderId="+orderId+"&containOldMan="+containOldMan+"&containBaby="+containBaby+"&containPregnantWomen="+containPregnantWomen;
    	 $.ajax({
    		   url : "/vst_order/order/orderShipManage/updateTravellerConfirm.do",
    		   data : formData,
    		   type:"POST",
    		   dataType:"JSON",
    		   success : function(result){
    		   		if(result.code=="success" ){
    		   		  loading.close();
    		   		  alert(result.message);
    		   		  window.location.reload();
    		   		}else {
    		   			loading.close();
    		   			alert(result.message);
    		   		  	window.location.reload();
    		   		}
    		   }
    		});	 
     });


    //主订单优惠券明细
    $("#favorUsageDetail").bind("click",function(){
        new xDialog("/vst_order/order/orderManage/showOrderFavorUsageDetails.do",{"orderId":${order.orderId!''}},{title:"主订单优惠券明细",width:1200});
    });

    //主订单促销明细
    $("#promotionDetail").bind("click",function(){
        new xDialog("/vst_order/order/orderShipManage/showOrderPromotionDetails.do",{"orderId":${order.orderId!''}},{title:"主订单促销明细",width:600});
    });

    //订单金额价格修改
    $("#orderAmountChangeDetail").bind("click",function(){
        new xDialog("/vst_order/order/orderShipManage/showAmountChangeQueryList.do?",{"orderId":${order.orderId!''},'objectType':'','approveStatus':'APPROVE_PASSED'},{title:"订单价格修改",width:600});
    });
</script>
<script src="/vst_order/js/order/orderAttachment.js"></script>
<script src="/vst_order/js/order/orderSendSms.js"></script>
<script src="/vst_order/js/order/orderPayment.js"></script>
<script src="/vst_order/js/order/orderAmountChange.js"></script>
<#if order&&order.distributorId==10>
	<script src="/vst_order/js/order/o2oApiInfo.js"></script>
	<script>
		getO2OUserInfo("${o2oUserName}","${o2oUserNameSign}");
	</script>
</#if>	

