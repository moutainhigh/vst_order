<#assign mis=JspTaglibs["/WEB-INF/pages//tld/lvmama-tags.tld"]>
<#import "/base/spring.ftl" as spring/>

<!DOCTYPE html>
<html>
<head>
<title>酒店订单-订单处理</title>
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
            <div class="order_seach">查询订单号：<input type="text" id="orderIdSeach"/><a class="btn ml10" id="orderIdSeachButton" target="_blank">查询</a> </div>
            <span class="f16 fl">订单号：<strong>${order.orderId !''}
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
                     <input type="hidden" name="buCode" id="buCode" value="${order.buCode!''}">
                     <input type="hidden" name="resourceStatus" id="resourceStatus" value="${order.resourceStatus!''}">
                     <input type="hidden" name="infoStatus" id="infoStatus" value="${order.infoStatus!''}">
                     <input type="hidden" name="invoiceStatus" id="invoiceStatus" value="${order.invoiceStatus!''}">
                     <input type="hidden" name="viewOrderStatus" id="viewOrderStatus" value="${order.viewOrderStatus!''}">
                     <input type="hidden" name="performStatus" id="performStatus" value="${orderItem.performStatus!''}">
                     <input type="hidden" name="orderItemId" id="orderItemId" value="${orderItem.orderItemId!''}">
                     
                     <input type="hidden" name="isSupplierOrder" id="isSupplierOrder" value="${isSupplierOrder!''}">
                     
                     
                      <#if order.orderStatus=="NORMAL">
                      
                        <tr>
                            <td>信息审核</td>
                            <td>
                            <#-- 
                             order.infoStatus=="UNVERIFIED"  
                             -->
                             <#if order.infoStatus=="INFOPASS">
                             
                             	<img src='../../img/pass.png' width='20' height='20' alt='通过'/>
                             
	                         <#else>
	                         	 <#if  auditMap['INFO_AUDIT']==true>
	                         	 	 <input type="radio" id="infoStatus" name="radioManage" value="INFOPASS"/>
	                         	 <#else>
	                         		 <input type="radio" id="infoStatus" name="radioManage" value="INFOPASS" disabled="true"/>  	   
	                         	 </#if> 
                             </#if> 
                             
                         
                            
                            </td>
                            <td></td>
                        </tr>
                        
                        <#if isDestBuFrontOrder==false>
                        <tr>
                            <td>资源审核</td>
                            <td>
                            <#-- 
                             order.resourceStatus=="UNVERIFIED"  
                             -->
                            <#if order.resourceStatus=="AMPLE">
                             
                             	<img src='../../img/pass.png' width='20' height='20' alt='通过'/>
                             
	                         <#else>
	                         	 <#if  auditMap['RESOURCE_AUDIT']==true>
	                         	 	 <input type="radio" id="resourceStatus" name="radioManage"  value="RESOURCEPASS"/>
	                         	 <#else>
	                         	 	 <input type="radio" id="resourceStatus" name="radioManage"  value="RESOURCEPASS" disabled="true"/>
	                         	 </#if> 
                             </#if> 
                             
                            
                            
                            </td>
                        </tr>
                        <#else>
                       	<input type="hidden"  name="hidden_resourceStatus" value="${order.resourceStatus}_${auditMap['RESOURCE_AUDIT']}"/>
                       </#if> 
                        
                        <tr>
                            <td>凭证确认</td>
                            <td>
                             
                            <#if isDoneCertificate==true>
                             
                             	<img src='../../img/pass.png' width='20' height='20' alt='通过'/>
                             
	                         <#else>
	                         	 <#if  auditMap['CERTIFICATE_AUDIT']==true>
	                         	 	 <input type="radio" id="certificate" name="radioManage" value="certificateStatus"/>
	                         	 <#else>
	                         	 	<input type="radio" id="certificate" name="radioManage" value="certificateStatus"  disabled="true"/>
	                         	 </#if> 
                             </#if> 
                             
                             
                            </td>
                            <td><a id="findEbookingFaxRecvList" href="javaScript:">[查回传(${ebkCount})]</a></td>
                        </tr>
                        
                        
	                        <#if  order.paymentStatus!="PAYED" &&  auditMap['PAYMENT_AUDIT']==true>
	                        
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
	                        
	                        <#if  order.paymentStatus!="PAYED" && auditMap['TIME_PAYMENT_AUDIT']==true>
	                        
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
                         
 						<#if auditMap['ONLINE_REFUND_AUDIT']==true > 
 						
 						  <tr>
                            <td>在线自动退款</td>
                            <td>
                            
                       		 <input type="radio"  id="onlineRefund" name="radioManage" value="onlineRefundConfirm"/>
                           
                       		 </td>
                         </tr>

                         <#elseif isDoneOnlineRefundAudit==true>
                         
                         <tr>
                            <td>在线自动退款</td>
                            <td>
                            
                             	<img src='../../img/pass.png' width='20' height='20' alt='通过'/>
                           
                       		 </td>
                         </tr>
                        
                       	 </#if> 
                    

                        <!--目的地酒店 酒店预定号 Added by yangzhenzhong Update by xiachengliang-->

                        <#if order.buCode?? && (order.buCode =="DESTINATION_BU" || order.buCode =="OUTBOUND_BU")>
                         <#if reservationNo??>
                         <tr>
                             <td>酒店预定号</td>
                             <td>${reservationNo}</td>
                         </tr>
                         <#else>
                         <tr>
                             <td>酒店预定号</td>
                             <td>${supplierNo}</td>
                         </tr>
                         </#if>


                     </#if>

                     <#if order.buCode?? && (order.buCode =="DESTINATION_BU" || order.buCode =="OUTBOUND_BU")>
                         <#if suppOrderId??>
                         <tr>
                             <td>供应商订单号</td>
                             <td>${suppOrderId}</td>
                         </tr>
                         </#if>


                     </#if>
                         <!--是否保留房-->
                         <#if order.buCode?? && order.buCode!="OUTBOUND_BU">
		                      <tr>
		                         <td>酒店类型</td>		                            
		                         <td>
		                         	<#if isPartStockFlag=="all">保留房
		                         	<#elseif isPartStockFlag == "part">部分保留房
		                         	<#elseif isPartStockFlag == "non">非保留房
		                         	<#else>未知类型</#if>
		                         </td>
		                      </tr>
                         </#if>
                    </tbody>
                </table>
            </div>
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
                            <td>${paymentStatusStr}</td>
                        </tr>
                        <tr>
                            <td>应收款：</td>
                            <td>RMB ${order.oughtAmount/100}元</td>
                        </tr>
                        <tr>
                            <td>已收款：</td>
                            <td>RMB ${order.actualAmount/100}元</td>
                            <td><a href="javaScript:" id="orderPaymentInfo">[查看记录]</a></td>
                        </tr>
                        
                        
                        <#if payPromotion != null>
                         <tr>
                          <td>支付立减 ：</td>
                          <td>RMB ${payPromotion/100}元</td>
                          </tr>
                        </#if>
                        <#if isNeedShowConfirmStatus?? && isNeedShowConfirmStatus == true>
	                           <tr>
	                             <td>订单状态 </td>
			                     <td>
			                   		    <select name="confirmStatus" autocomplete="off" class="w10" style="margin: 0 0 5px;" value="${orderItem.confirmStatus!''}" onchange="confirmIdDisplay(this);" >
										   <option value="INCONFIRM" <#if orderItem.confirmStatus=='INCONFIRM'>selected="selected"</#if>>已审</option>
										   <option value="FULL" <#if orderItem.confirmStatus=='FULL'>selected="selected"</#if>>订单满房</option>
										   <option value="PECULIAR_FULL" <#if orderItem.confirmStatus=='PECULIAR_FULL'>selected="selected"</#if>>特殊满房</option>
										   <option value="CHANGE_PRICE" <#if orderItem.confirmStatus=='CHANGE_PRICE'>selected="selected"</#if>>订单变价</option>
										   <option value="SUCCESS" <#if orderItem.confirmStatus=='SUCCESS'>selected="selected"</#if>>确认成功</option>
									    </select>
			                     </td>
			                     <td>
			                            <a class="btn ml10" <#if orderItem.confirmStatus=='SUCCESS'>style="background:#D3D3D3" </#if> <#if orderItem.confirmStatus!='SUCCESS'>onclick="javascript:updateConfirmStatus(this, '${orderItem.confirmStatus!''}')"</#if> href="javascript:void(0)" >保存</a>
			                     </td>
			                   </tr>
                         </#if>
                    </tbody>
                </table>
                </div>
                <#if isNeedShowConfirmStatus?? && isNeedShowConfirmStatus == true>
				 <div style="display: none;">
					&nbsp;&nbsp;<input type="text" autocomplete="off" name="confirmId" placeholder="确认号" style="margin: 0 0 3px;"><br>
				 </div>
			  </#if>
            <div class="solid_line"></div>
            <div class="side_setbox sidebox" >
                <h4>订单取消<span style="color:red;font-size:14px;">(订单${orderStatusStr}/${settlementStatusStr})</span></h4>
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
					 
					 	 <td style="width:140px;">
					 	 
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
                  
                   <#if isSupplierOrder=="true" >
                     <span class="fr" style="color:red">此订单为供应商订单，取消操纵成功后，需要等待供应商确认后，才会真正把订单取消。</span>
                   	
                   </#if>  
                   
                </div>
                </div>
            </div>
			<div id="hotelRecommend"  class="btn" style="border-radius: 5px;margin: 10px 0 0 70px;color: #06c;">相关酒店推荐</div>
        </div>
        <div class="main equalheight_item">
            <div class="main_con clearfix">
                <div class="main_order_msg">
                    <div><a id="viewOrderAttachment" class="fr" href="javaScript:">附件(<b>${orderAttachmentNumber}</b>)</a>订单备注记录:</div>
                    <#if order.categoryId==1 && (order.workVersion=="3.0" || order.workVersion=="3.1")>
                    	<textarea style="width:285px; height:120px;" id="orderItemRemark" name="orderItemRemark" onkeyup="checkItemRemarkLength()">${orderItem.orderMemo!''}</textarea>
                    </#if>
                    <#if order.categoryId==1 && (order.workVersion=="3.0" || order.workVersion=="3.1")>
                    	<div><a id="viewOrderAttachment" class="fr" href="javaScript:">附件(<b>${orderAttachmentNumber}</b>)</a>主订单备注记录:</div>
                    </#if>
                    <textarea style="width:285px; height:120px;" id="orderRemark" name="orderRemark" onkeyup="checkRemarkLength()" <#if order.categoryId==1 && (order.workVersion=="3.0" || order.workVersion=="3.1")>disabled="true"</#if>>${order.orderMemo!''}</textarea>
                    <span class="fr" id="zsRemark">0/500字</span>
                    <div class="operate mt10">
                    <a class="btn btn_cc1" id="saveButton" >确认修改</a>
                    <a class="btn btn_cc1" id="clearButton" href="javaScript:clearradio();" >清空选择</a>
                    
                    </div>
                    <div class="mt20">
                    <#--
                        <p>供应商：</p>
                          -->
                        <ul class="supplier_list">
                            <li><a id="sendOderFax"   href="javaScript:" title="发送凭证">发送凭证</a></li>
                            
                            <li><a id="findEbkFaxList"  href="javaScript:" title="凭证查询">凭证查询</a></li>
<#--
                            <li><a id="ordSale"  target="_blank" href="/super_back/ord/sale/OrdSaleAddJump.zul?orderId=${order.orderId}&sysCode=VST" title="售后服务">售后服务</a></li>
-->
                            <li><a id="ordSale"  target="_blank" href="/sales_front/ord/sale/toOrdSale/${order.orderId}" title="售后服务">售后服务</a></li>

                        <#--
                        <li>复制订单++</li>
                        <li>资金转移++</li>
                        -->
                            <li><a id="addMessage"   href="javaScript:" title="预订通知">预订通知</a></li>
                            <li><a id="uploadOrderAttachment" href="javaScript:" title="上传附件">上传附件</a></li>
                            <li><a id="showLogDialog" href="javaScript:" title="查看日志">查看日志</a></li>
                            <li><a id="showInvoiceMessage" target="_blank" href="/vst_order/order/orderInvoice/showInvoiceInfo.do?orderId=${order.orderId!''}" title="发票">发票信息</a></li>
                             <#if orderItem.supplierId=="1" > 
                              <li><a id="yiLongDeduct" href="javaScript:" title="艺龙扣款">艺龙扣款</a></li>
                             </#if>
                             
                             <#if order.orderStatus=="NORMAL" &&  order.paymentStatus == "UNPAY" >  
                              <@mis.checkPerm permCode="5920"><li><a id="changeAmount" href="javaScript:" title="修改价格">修改价格</a></li></@mis.checkPerm>
                            </#if>
                            <li><a id="showOrderSMS" href="javaScript:;" title="">查看短信</a></li>
						<#--<li><a id="showOrderRemark" href="javaScript:;" onclick="showOrderRemark(${order.orderId});" title="">订单备注</a></li>-->
                            <#if isReCancelBtn?? && isReCancelBtn == 'REJECT'>
                                <li><a id="reSendCancelApply" href="javaScript:"  title="重新取消">重新取消</a></li>
                            </#if>
                            
                            <li><a id="showSoundRec" href="javaScript:;" title="查看录音">查看录音</a></li>
                            <li><a id="showOrderItemLogDialog" href="javaScript:" title="子单日志">子单日志</a></li>
                            <li><a id="showUntreatedComplaintCall" href="javaScript:" title="未处理用户信息">未处理用户信息</a></li>
							<li><a id="addComplaintCall" href="javaScript:" title="新增用户信息">新增用户信息</a></li>
							<li><a id="myCreatedComplaintCall" href="javaScript:" title="我创建的用户信息">我创建的用户信息</a></li>
							<li><a id="addWork" href="javaScript:" title="新增工单">新增工单</a></li>
							<li><a id="showCbpInfo" href="javaScript:" title="中行支付查询">中行支付查询</a></li>
							<#if "${isContainInsurance}"==true>
								<li><a id="findInsurancePolicyList"   target="_blank" href="/vst_insurant/insurancePolicy/findList.do?orderId=${order.orderId}"  title="查看投保状态">查看投保状态</a></li>
                            </#if>
                            <@mis.checkPerm permCode="6363"><li><a id="showUserOrder" href="javaScript:" title="个人中心显示订单">个人中心显示订单</a></li></@mis.checkPerm>
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
                                <td class="e_label">渠道代码/名称：</td>
                                <td>${order.distributorCode !''}/${distributionChannelName!''} <#if tntOrderChannel?? && tntOrderChannel!=''>/${tntOrderChannel}</#if></td>
                            </tr>
                             <tr>
                                <td class="e_label">分销商渠道ID：</td>
                                <td>${order.distributionChannel !''} </td>
                            </tr>
                            
                            <tr>
                                <td class="e_label">付款方式：</td>
                                <td>
                                <#if order.paymentTarget=="PREPAID" > 
                            	 网站预付
				                  <#else>
				                  	 酒店现付
				                  </#if>
				                  
                                </td>
                            </tr>
                           
                            <tr>
                                <td class="e_label">退改政策：</td>
                                <td>
                                	<strong>酒店</strong>
                                </td>
                            </tr>
                            <tr>
                                <td></td>
                                <td>
                                <#if orderItem.cancelStrategy=="UNRETREATANDCHANGE" > 
                                	当前订单不退不改
                                <#else>
	                                <#if order.paymentTarget=="PAY" > 
	                                 	 <#if order.guarantee=="GUARANTEE" > 
	                                 	 担保订单
	                                 	 </#if>
	                                 </#if>
	                                扣款类型[${deductTypeStr}(${deductAmountStr}元)]
	                                 <#if order.lastCancelTime??>
	                                	<#if isGreaterNow==1 > 
		                            	 	<span class="lineae_line">${order.lastCancelTime?string('yyyy-MM-dd HH:mm')!''}  前无损取消</span>
						                  <#else>
						                  	 ${order.lastCancelTime?string('yyyy-MM-dd HH:mm')!''}  前无损取消
						                  </#if>
						             </#if>
						        </#if>     
								<#if "${isContainInsurance}"==true>
									<br/><strong>保险</strong><br/>
									${insuranceRules}
								</#if> 
                                </td>
                            </tr>
                            
                            <#--支付等待时间如果有就显示-->
                            <#if order.waitPaymentTime??>
	                            <tr>
	                                <td class="e_label">支付等待时间：</td>
	                                <td>
	                                	${order.waitPaymentTime?string('yyyy-MM-dd HH:mm')!''}
	                                </td>
	                            </tr>
                            </#if>
                            
                            <tr>
                                <td class="e_label">所属公司：</td>
                                <td>${order.zhFilialeName !''}</td>
                            </tr>
                            <tr>
                                <td class="e_label">所属产品经理：</td>
                                <td>${productManager!''}</td>
                            </tr>
                        </tbody>
                    </table>
                    <div class="solid_line mt10 mb10"></div>
                    <table>
                        <tbody>
                            <tr>
                                <td class="e_label w10">酒店名称：</td>
                                <td>
                                 <#if city!=""> 
                                 	[${city}] 
                                 <#else> 
                                 
                                 </#if> 
                                
                                <a href="http://hotels.lvmama.com/hotel/${orderItem.productId}" target="_blank" title="点击打开前台页面">[${orderItem.productName}]</a></td>
                            </tr>
                             <tr>
                                <td class="e_label">酒店电话号码：</td>
                                <td>${order.hotelTel}</td>
                            </tr>
                            <tr>
                                <td class="e_label">供应商：</td>
                                <td><a  href="javaScript:" id="supplierName" >[  ${suppSupplier.supplierName}  ]</a></td>
                            </tr>
                            <tr>
                            	<td class="e_label">供应商电话：</td>
                                <td><a href="http://localhost:12366/ipcc/default.jsp?webcallout&webcalloutno=${suppSupplier.tel}" title="点击号码外呼">[  ${suppSupplier.tel}  ]</a></td>
                                
                            </tr>
                            <tr>
                                <td class="e_label">客人姓名：</td>
                                <td><strong>[${travellerNum!'0'}人]</strong>${travellerName!''}</td>
                            </tr>
                            <tr>
                                <td class="e_label">入住天数：</td>  
                                <td><strong>[${arrivalDays}天]</strong> ${orderItem.visitTime?string('yyyy-MM-dd') !''} 至 ${lastOrderHotelTimeRate.visitTime ?string('yyyy-MM-dd') !''} </td>
                            </tr>
                            <tr id="branchName"><#--房型后面需要加上携程促销显示-->
                                <td class="e_label td_top">房型：</td>
                                <td>
                                    <strong>[${orderItem.quantity}间]</strong>  [${orderItem.contentMap['branchName']}] [${orderItem.suppGoodsName}]
									${bedType} 
									<#if addValue?? && addValue!=""> 
									（${addValue!''}） 
									</#if> 
                                    
                                     <#--<a  class="J_tip order_tip" tip-content="礼包包含：" href="javaScript:">--礼包</a>-->
                                     <a class="J_tip order_tip" tip-content="${broadband!''}" href="javaScript:">宽带</a> 
                                    <#--<p>-->
                                    
                                    <#if isRoomReservations=="true"> 
                                    	<span class="cc6">[房型保留]</span> 
                                    </#if> 
                                     <#if suppSupplier.apiFlag=="N"> 
                                    	<span class="cc6"><a id="roomStatus" href="javaScript:" title="满房通知"  >[满房通知]</a></span> 
                                    </#if> 
                                </td>
                            </tr>
                            <tr>
                                <td class="e_label">商品ID：</td>
                                <td>${orderItem.suppGoodsId}</td>
                            </tr>
                            <tr>
                                <td class="e_label">间夜数：</td>
                                <td>${orderItem.quantity*arrivalDays}</td>
                            </tr>
                            <tr>
                                <td class="e_label">信用住订单：</td>
                                <td>${orderItem.creditTagStr}</td>
                            </tr>
                            <tr>
                                <td class="e_label">到店时间：</td>
                                <td>最晚${lastTime}</td>
                            </tr>
                            <tr>
                                <td class="e_label">返现金额：</td>
                                <td>${order.getRebateAmountYuan()}</td>
                            </tr>
                            
                            <tr>
                                <td class="e_label">优惠券减少总金额：</td>
                                <td>
                                <#if favorUsageAmount??>
                                	${favorUsageAmount?string("#.##")} <a id="favorUsageDetail" href="javascript:void(0)">[查看明细]</a>
                                <#else>
                                	0.00
                                </#if>
                                </td>
                            </tr>
                            <tr>
                                <td class="e_label">促销减少总金额：</td>
                                <td>
                                <#if totalOrderAmount??>
                                	${totalOrderAmount} <a id="promotionDetail" href="javascript:void(0)">[查看明细]</a>
                                <#else>
                                	0.00
                                </#if>
                                </td>
                            </tr>
                            <tr>
                                <td class="e_label">分销渠道减少总金额：</td>
                                <td>${distributionPrice!''}</td>
                            </tr>
                        <#if order.orderSubType=='STAMP_PROD'>
                          <#if order.categoryId==1>
                            <tr>
                                <td class="e_label">预售券抵扣总金额：</td>
                              <#if order.stampDeductionCountAmount??>
                                <td>${(order.stampDeductionCountAmount/100)?string('#0.00')} <a id="stampDeductionCountAmount" href="javascript:void(0)">[查看明细]</a></td>
                              <#else>
                                 <td>0.00</td>
                              </#if>
                            </tr>
                            </#if>
                        </#if>
                            
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
                                <a href="${rc.contextPath}/ord/order/orderMonitorList.do?userId=${order.userId}&productId=${orderItem.productId}&orderStatus=NORMAL" target="_blank">[另有订单]</a>
                                </#if> 
                                <a href="${rc.contextPath}/ord/order/orderMonitorList.do?userId=${order.userId}" target="_blank">[同用户订单]</a></td>
                            </tr>
                            <tr>
                                <td class="e_label">使用状态：</td>
                                <td>${performStatus}</td>
                            </tr>
                            <#if invoiceExpressOrderId?? >
                            <tr>
                            	<td class="e_label">发票快递订单：</td>
								<td><a href="${rc.contextPath}/order/ordCommon/showOrderDetails.do?orderId=${invoiceExpressOrderId}" target="_blank">${invoiceExpressOrderId}</a></td>
                            </tr>
                            </#if>
                        </tbody>
                    </table>
                        <#if isDestBuSingleHotelFrontOrder?? && isDestBuSingleHotelFrontOrder==true>
                    
                             <#include "/order/invoice/invoiceInfoBase.ftl"/>
	                 </#if>        
                </div>
            </div>
            <table class="p_table table_center mt20">
                <thead>
                    <tr>
                        <th>房型</th>
                        <th>日期</th>
                        <#if currencyCode?? &&  currencyCode != "" && currencyCode != null>
                            <th>汇率</th>
                        </#if>
                        <th>房价</th>
                        <th>结算价</th>
                        <th>早餐</th>
                        <th>担保时间</th>
                        <th>最晚预定</th>
                        <th>子订单实付金额</th>
                        <th>优惠分摊总金额</th>
                        <th>促销分摊总金额</th>
                        <th>分销渠道减少分摊总金额</th>
                        <th>订单金额减少分摊总金额</th>
                         <th>支付立减分摊总金额</th>
                        <th>子订单退款总金额</th>
                        <th>退款间数</th>
                    </tr>
                </thead>
                <tbody>
                
                <#list hotelTimeRateInfoList as hotelTimeRateInfo>
                    <tr>
                        <td>
                        <#if hotelTimeRateInfo_index == 0>
                        	<a class="productInfo" href="javaScript:">${orderItem.contentMap['branchName']}( ${orderItem.suppGoodsName} )</a>
                        <#else>${orderItem.contentMap['branchName']}( ${orderItem.suppGoodsName} )
                        </#if>
                        </td>
                        <td>${hotelTimeRateInfo.visitTime?string('yyyy-MM-dd') !''}</td>
                        
                        <#if currencyCode?? &&  currencyCode != "" && currencyCode != null>
                            <td>${currencyCode!''}( ${cashSellRate} )</td>
                        </#if>
                        
                        <td>RMB ${((hotelTimeRateInfo.price)!0)/100!''}</td>
                        <!-- 结算价 -->
                        <td>RMB ${((hotelTimeRateInfo.settlementPrice)!0)/100}</td>
                        <td>
                            <#if hotelTimeRateInfo.breakfastTicket==0>
                                无
                            <#elseif hotelTimeRateInfo.breakfastTicket==1>
                                单早
                            <#elseif hotelTimeRateInfo.breakfastTicket==2>
                                双早
                            <#elseif hotelTimeRateInfo.breakfastTicket==3>
                                三早
                            <#else>
                            ${hotelTimeRateInfo.breakfastTicket}早
                            </#if>
                        </td>
                        <td>
                            <#if hotelTimeRateInfo.guaranteeTime!=null && hotelTimeRateInfo.guaranteeTime!='null' >
                            ${hotelTimeRateInfo.guaranteeTime!''}:00
                            </#if>
                        </td>
                        <td>${hotelTimeRateInfo.lastTime!''}</td>
                        <td>RMB ${((hotelTimeRateInfo.actualPaidAmount)!0)/100}</td>
                        <td>RMB ${((hotelTimeRateInfo.couponApportionAmount)!0)/100}</td>
                        <td>RMB ${((hotelTimeRateInfo.promotionApportionAmount)!0)/100}</td>
                        <td>RMB ${((hotelTimeRateInfo.distributorApportionAmount)!0)/100}</td>
                        <td>RMB ${((hotelTimeRateInfo.manualChangeApportionAmount)!0)/100}</td>
                        <td>RMB ${((hotelTimeRateInfo.payAmountReductTotalAmount)!0)/100}</td>
                        <td>RMB ${((hotelTimeRateInfo.refundAmount)!0)/100}</td>
                        <td>${(hotelTimeRateInfo.refundQuantity)!0}间</td>
                    </tr>
                </#list>
                </tbody>
            </table>

            
			<div id="childOrderInfo">
				<#if resultMap??>
					<#list resultMap?keys as testKey>  
						<div class="order_msg clearfix">
							</br>
							<strong>子订单--${resultMap[testKey][0].childOrderTypeName!''}</strong>
						</div>
						<table  class="p_table table_center mt20">
								<thead>
									<tr>
										<th>客服负责人</th>
										<th>资源审核人</th>
										<#if testKey == 11 || testKey == 12 || testKey == 13>
											<th>Expired Refund 💲</th>
										</#if>
										<th>子订单号</th>
										<th>子订单状态</th>
										<#if resultMap[testKey][0].childOrderType=='category_single_ticket'
	                                          	|| resultMap[testKey][0].childOrderType=='category_other_ticket'
	                                          	|| resultMap[testKey][0].childOrderType=='category_connects'
												|| resultMap[testKey][0].childOrderType=='category_show_ticket'>
											<th>是否二维码对接</th>
											<th>是否支持废码</th>
										<#else>
											<th>是否对接</th>
										</#if>
										<#if resultMap[testKey][0].childOrderType=='category_single_ticket'
												|| resultMap[testKey][0].childOrderType=='category_other_ticket'
												|| resultMap[testKey][0].childOrderType=='category_comb_ticket'
												|| resultMap[testKey][0].childOrderType=='category_connects'
												|| resultMap[testKey][0].childOrderType=='category_show_ticket'>
											<th>是否是EBK订单能及时处理通关</th>
										</#if>
										<th>类型</th>
										<#if resultMap[testKey][0].productType=="WIFI" >
											<th>产品类别</th>
											<th>取件类型</th>
										<#elseif resultMap[testKey][0].productType=="PHONE"||resultMap[testKey][0].productType=="DEPOSIT" || resultMap[testKey][0].productType=="EXPRESS">
											<th>产品类别</th>
										</#if>
										<th>包含商品</th>
										<th>预订份数</th>
										<th>销售单价</th>
										<th>订单总价</th>
										<th>订单实付总金额</th>                              
										<!--<th>支付立减分摊总金额</th> -->
										<#if testKey!=45>
											<th>出游日期</th>
											<#if testKey == 13 && resultMap[testKey][0].certValidDay??>
												<th>有效期</th>
											</#if>
										</#if>
										<#if testKey==44>
											<th>使用时间</th>
											<th>当地酒店地址</th>
										</#if>
										<#if testKey==43>
											<th> 使用时间</th>
										</#if>
										<#if resultMap[testKey][0].productType=="WIFI" ||resultMap[testKey][0].productType=="DEPOSIT">
											<th>出游截止日期</th>
										</#if>
										<th>订单备注</th>
										<#if resultMap[testKey][0].childOrderType=='category_presale'>
											<th>补贴金额单价</th>
										</#if>
										<th>子订单价格确认状态</th>
										<#if OrdChangableFlag == "Y">
											<th>操作</th>
										</#if>
									</tr>
								</thead>
								<tbody>
									<#list resultMap[testKey]  as orderMonitorRst> 
										<#if orderMonitorRst.specialTicketType != 'DISNEY_SHOW' && orderMonitorRst.specialTicketType != 'SHOW_TICKET'>
											<tr>
												<td>${orderMonitorRst.principal!''}</td>
												<td>${orderMonitorRst.resourceApprover!''}</td>
												<#if testKey == 11 || testKey == 12 || testKey == 13>
													<td>
														<#if orderMonitorRst.expiredRefundFlag == 'P'>
															in process
														<#else>
															${orderMonitorRst.expiredRefundFlag!''}
														</#if>
													</td>
												</#if>			                        
												<td>
													<a href="/vst_order/order/orderManage/showChildOrderStatusManage.do?orderItemId=${orderMonitorRst.orderId!''}&orderType=child&orderId=${order.orderId}" target="_blank">${orderMonitorRst.orderId!''}</a>
												</td>
												<td>${orderMonitorRst.currentStatus!''}</td>
												<td>
													<#if orderMonitorRst.childOrderType == 'category_single_ticket'
															|| orderMonitorRst.childOrderType == 'category_other_ticket'
															|| resultMap[testKey][0].childOrderType=='category_connects'
															|| resultMap[testKey][0].childOrderType=='category_show_ticket'>
														<#if orderMonitorRst.apiFlag=="Y">是<#else>否</#if>
				                            		<#else>
														<#if orderMonitorRst.apiFlag=="Y">对接<#else>非对接</#if>
													</#if>
												</td>
												<#if resultMap[testKey][0].childOrderType=='category_single_ticket'
														|| resultMap[testKey][0].childOrderType=='category_other_ticket'
														|| resultMap[testKey][0].childOrderType=='category_connects'
														|| resultMap[testKey][0].childOrderType=='category_show_ticket'>
													<td>
														<#if orderMonitorRst.apiFlag=="Y">
															<#if orderMonitorRst.isSupportDestroyCode=="1">支持废码<#else>不支持废码</#if>
														<#else>
														</#if>
													</td>
												</#if>
												<#if resultMap[testKey][0].childOrderType=='category_single_ticket'
														|| resultMap[testKey][0].childOrderType=='category_other_ticket'
														|| resultMap[testKey][0].childOrderType=='category_comb_ticket'
														|| resultMap[testKey][0].childOrderType=='category_connects'
														|| resultMap[testKey][0].childOrderType=='category_show_ticket'>
													<td>
														<#if orderMonitorRst.isEbkAndEnterInTime=='Y'>
				                    	 					是
														<#else>
				                    	 					否
														</#if>
													</td>
												</#if>		                        
												<td> 
													<#if orderMonitorRst.childOrderTypeName=="当地游" || orderMonitorRst.childOrderTypeName=="跟团游">
														<#if orderMonitorRst.producTourtType=="ONEDAYTOUR">
															${orderMonitorRst.childOrderTypeName!''}-一日游
														<#elseif orderMonitorRst.producTourtType=="MULTIDAYTOUR"> 
															${orderMonitorRst.childOrderTypeName!''}-多日游
														<#else>
															${orderMonitorRst.childOrderTypeName!''}
														</#if>
													<#else>
														${orderMonitorRst.childOrderTypeName!''}
													</#if>
												</td>
												<#if orderMonitorRst.productType=="WIFI" >
													<td> ${orderMonitorRst.productType!''}</td>
													<td><#if wifiAddition.pickingType=="NOTICETYPE_DISPLAY">自取<#else>邮寄</#if></td>
												<#elseif orderMonitorRst.productType=="PHONE">
													<td>电话卡</td>
												<#elseif orderMonitorRst.productType=="DEPOSIT">
													<td>押金</td>
												<#elseif orderMonitorRst.productType=="EXPRESS">
													<td>快递</td>
												</#if>
												<td>
													<#if resultMap[testKey][0].childOrderType=='category_single_ticket'
															|| resultMap[testKey][0].childOrderType=='category_other_ticket'
															|| resultMap[testKey][0].childOrderType=='category_comb_ticket'
															|| resultMap[testKey][0].childOrderType=='category_food'
															|| resultMap[testKey][0].childOrderType=='category_sport'>
														<a class="productInfo" href="javaScript:" data-orderItemId="${orderMonitorRst.orderId!''}">${orderMonitorRst.productName!''}</a>
													<#else>
														${orderMonitorRst.productName!''}
													</#if>
												</td>
												<td>
													<#if orderMonitorRst.childOrderType == 'category_cruise'>
														${orderMonitorRst.personCount!''} 人/ ${orderMonitorRst.buyCount!''} 间
													<#else>
														${orderMonitorRst.buyItemCount!''}
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
												<#if listDates ??>
													<td>
														<#assign flag = true/>
														<#list listDates as itemDate>
															<#if flag==true>
																${itemDate?string('yyyy-MM-dd')} -
																<#assign flag = false/>
															<#else>
																${itemDate?string('yyyy-MM-dd')}
															</#if>
														</#list>
													</td>
												<#else>
													<#if testKey!=45>
														<td> ${orderMonitorRst.visitTime!''} </td>
														<#if testKey == 13 && orderMonitorRst.certValidDay??>
															<td>${orderMonitorRst.certValidDay!''}</td>
														</#if>
													</#if>
			                           			</#if>
			                           			<#if testKey==44>
			                           				<td>${(orderMonitorRst.useTime)!''}</td>
			                           				<td>${(orderMonitorRst.localHotelAddress)!''}</td>
			                           			</#if>
			                           			<#if testKey==43>
			                           				<td>${(orderMonitorRst.useTime)!''}</td>
			                           			</#if>
			                           			<#if resultMap[testKey][0].productType=="WIFI" ||resultMap[testKey][0].productType=="DEPOSIT">
			                           				<td>${wifiAddition.endDay}</td>
			                           			</#if>
			                           			<td> ${orderMonitorRst.orderItemMemo!''}<#if orderMonitorRst.orderAttachmentNumber?? && orderMonitorRst.orderAttachmentNumber &gt; 0>(<a class="viewChildOrderAttachment" href="javaScript:">查看附件</a>)</#if></td>
			                           				<#if resultMap[testKey][0].childOrderType=='category_presale'>
														<td>${subsidyAmount}</td>
													</#if>
												<td ><#if orderMonitorRst.priceConfirmStatus?? && orderMonitorRst.priceConfirmStatus=='UN_CONFIRMED'>价格待确认<#else>价格已确认</#if></td>
												<#if OrdChangableFlag == "Y">
													<td>
														<input type="hidden" name="orderItemId" value="${orderMonitorRst.orderId}">
														<#assign currentItemChangable = false/>
														<#list changableItemIdList as changableItemId>
															<#if changableItemId == orderMonitorRst.orderId>
																<#assign currentItemChangable = true/>
																<#break>
															</#if>
														</#list>
														<a class="btn <#if currentItemChangable>btn_cc1<#else>changed</#if>" name="changeDate">改期</a>
													</td>
												</#if>
											</tr>
										</#if>
									</#list>
								</tbody>
							</table>
						
						<#if resultMap[testKey][0].productType=="WIFI">
							<table class="p_table table_center mt20">
								<thead>
									<#if wifiAddition.rentDays!=null>
										<th>租赁天数</th>
									</#if>
									<th>取件城市</th>
									<th>取件网点</th>
									<th>还件城市</th>
									<th>还件网点</th>
								</thead>
								<tbody>
								
								</tbody>
									<tr>
			               		<#if wifiAddition.rentDays!=null>
			                        <td>${wifiAddition.rentDays}</td>	
			                    </#if>
			               		<td><#if wifiAddition.pickingType=="NOTICETYPE_DISPLAY">${wifiAddition.tackCityName}<#else>邮寄</#if></td>
			               		<td><#if wifiAddition.pickingType=="NOTICETYPE_DISPLAY">${wifiAddition.takePickingPoint}</#if></td>
			               		<td>${wifiAddition.backCityName}</td>
			               		<td>${wifiAddition.backPickingPoint}</td>
			               	</tr>
	                    </table>
	                    </#if>
					</#list>
				</#if>
			</div>
            
				<#if "${ishowInsurance}"==true>
				<div class="order_msg clearfix">
					<br>
					<strong>
					  游客信息(<a class="btn btn_cc1" id="editInsurePersonButton" href="javaScript:" >修改</a>)
					</strong>
				</div>
				<table class="p_table table_center mt20">
					<thead>
						<tr>
							<th>类型	</th>
							<th>中文姓名</th>
							<th>英文姓</th>
							<th>英文名</th>
							<th>证件类型</th>
							<th>证件号码</th>
							<th>签发地</th>
							<th>有效期</th>
							<th>出生日期</th>
							<th>人群</th>
							<th>性别</th>
							<th>联系电话</th>
							<th>邮箱地址</th>
							<th>关联的商品</th>
						</tr>
					</thead>
					<tbody>
						<#list insurePersonList as person> 
							<tr>
								<td>游客</td>
								<td>${person.fullName!''}</td>
								<td>${person.lastName!''}</td>
								<td>${person.firstName!''}</td>
								<td>${person.idTypeName!''}</td>
								<td>
									<#if person.idTypeName!="客服联系我">
										${person.idNo!''}
									</#if>
								</td>
								<td>${person.issued!''}</td>
								<td>
									<#if person.expDate?exists>
										${person.expDate?string('yyyy-MM-dd')}
									</#if>
								</td>
								<td>
									<#if person.birthday?exists>
										${person.birthday?string('yyyy-MM-dd')}
									</#if>
								</td>
								<td>
									<#if person.peopleType == "PEOPLE_TYPE_ADULT"> 
										成人
									<#elseif person.peopleType == "PEOPLE_TYPE_CHILD">
										儿童
									<#elseif person.peopleType == "PEOPLE_TYPE_OLDER">
										老人
									</#if>
								</td>
								<td>
									<#if person.gender == "MAN"> 
										男
									<#elseif person.gender == "WOMAN">
										女
									</#if>
								</td>
								<td>${(person.mobile)!''}</td>
								<td>${(person.email)!''} </td>
								<td>${(person.checkInRoomName)!'无'}</td>
							</tr>
						</#list>
					</tbody>
				</table>
			</#if>

            <div id="findBuyPresentList">
               <div class="order_msg clearfix">
	            	正在使劲加载数据中...
	            </div> 
             </div>
            <table class="mt20 e_table form-inline" style="width:400px;">
                <tbody>
                    <tr>
                        <td class="e_label">登录用户：</td>
                        <td><a href="../../../crm-srv-web/hotline/maintained/index/${order.userNo}" target="_blank">${ordPersonBooker.fullName!''}</a></td>
                    </tr>
                    <tr>
                        <td class="e_label">联系人：</td>
                        <td style="word-break: break-all;word-wrap: break-word;">
                         <#if order.distributorId==3 > 
                        	 <#if order.contactPerson.fullName!='' > 
                        	 	${order.contactPerson.fullName!''}
                        	 <#else>
                        	 	 ${order.contactPerson.mobile?substring(0,3)}****${order.contactPerson.mobile?substring(7,order.contactPerson.mobile?length)}
                        	 </#if>
                       
	                  	 <#else>
	                  	 	 <#if order.contactPerson?? > 
	                  	 	  ${order.contactPerson.fullName!''}
	                  	 	 </#if>
	                  	
	                  	</#if>
                        
                        </td>
                        <td><a href="javaScript:" id="personUpdate">[修改]</a></td>
                    </tr>
                    <tr>
                        <td class="e_label">联系人手机：</td>
                        <td>${order.contactPerson.mobile!''}</td>
                        <td><nobr><a href="javaScript:" id="orderSendSms">[发送短信]</a> <a href="http://localhost:12366/ipcc/default.jsp?webcallout&webcalloutno=${order.contactPerson.mobile!''}">[呼叫]</a></td>
                    </tr>
                    <tr>
                        <td class="e_label">电子邮件：</td>
                        <td style="word-break: break-all;word-wrap: break-word;">${order.contactPerson.email!''}</td>
                    </tr>
                    <tr>
		            	<td class="e_label">会员类型：</td>
			            <td>
							${userTypeStr!''}
			            </td>
			        </tr>
                    <tr>
                        <td class="e_label">是否为超级会员：</td>
                        <td>
                            <#if userSuperVip == true>
                               <span>是</span>
                            <#else>
                                 <span>否</span>
                            </#if>
                        </td>
                    </tr>

                </tbody>
            </table> 
        </div>
    </div>
    </form>
    <div id="logResult" style="display:none;margin-left:40px;margin-right:35px;">
     
     </div>
    <#include "/base/foot.ftl"/>
</body>
</html>
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
    	//酒店推荐
    	var hotelRecommendDialog;
    	$("#hotelRecommend").bind("click",function(){
    		var startDate = "${orderItem.visitTime?string('yyyy-MM-dd') !''}";
    		var endDate = "${lastOrderHotelTimeRate.visitTime ?string('yyyy-MM-dd') !''}";
    		var mapType = '${mapType !''}';//地图类型
    		var baiduGeo = '${baiduGeo !''}';//经纬度
    		var starId = '${starId !''}';//酒店星级
    		var suppGoodsId = "${orderItem.suppGoodsId !''}";
    		var param = "startDate=" + startDate + "&endDate=" + endDate + "&mapType=" + mapType + "&baiduGeo=" + baiduGeo + "&starId=" + starId + "&suppGoodsId=" + suppGoodsId;
    		hotelRecommendDialog = new xDialog("/vst_order/ord/order/newOrderRecommendHotelResult.do?" + param,{},{title:"相关酒店推荐",width:1200,iframe:true});
		});
		//产品重要信息提示
		var productInfoDialog;
		$(".productInfo").bind("click",function(){
			productInfoDialog = new xDialog("/vst_order/order/orderStatusManage/showOrderHotelProductDetail.do?orderId=" + ${order.orderId!''},{},{title:"重要提示",width:900,height:200,iframe:true});
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
		         
    	var addOrdFuncRelationDialog,messageDialog;
        var certificateDialog,orderSendSmsDialog,retentionTimeDialog,saledAppliedDialog;
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
            
          //人工申请，更新发票信息状态  
	      $("#invoiceApply").bind("click",function(){    
	  		var id="${(orderInvoiceInfoVst.id)!''}";
	  		var param="id="+id;
	  		$.ajax({
			   url : "/vst_order/order/ordCommon/updateInvoiceApplyStatus.do",
			   data : param,
			   type:"POST",
			   dataType:"JSON",
			   success : function(result){
			   		if(result.success=='true'){
			   		     window.location.reload();
			   		}else {
			   			 alert(result.msg);
			   			 window.location.reload();
			   		}
			   }
			});	
			
		 });
            
            $("#saveButton").bind("click",function(){
            
            		var operation;
            		var cancelCode;
            		var cancleReason;
            		var orderRemark=$.trim($("#orderRemark").val());
            		var radioValue="updateOrderRemark";
               		var distributorCode = "${order.distributorCode !''}";
               		var orderStatus = "${order.orderStatus !''}";
               		var paymentStatus = "${order.paymentStatus !''}";
            		
            		
            		if($('input:radio:checked').length<=0)
            		{
            			radioValue="updateOrderRemark";
            		}else if("${order.orderStatus}"=="CANCEL"){
            		
            			var radioVal=$('input:radio:checked').val();
            			if(radioVal=="INFOPASS" || radioVal=="RESOURCEPASS" || radioVal=="certificateStatus" || radioVal=="paymentAudit"
            					|| radioVal=="timepPaymentAudit" || radioVal=="cancelStatusConfim" || radioVal=="onlineRefundConfirm")
            			{
            				radioValue=radioVal;    
            			}else{
            				radioValue="updateOrderRemark";
            				   	
            			}
            		}else{
            			radioValue=$('input:radio:checked').val(); 
            		}
            		
            		if("updateOrderRemark"==radioValue){
            			operation="updateOrderRemark";
            		}else if("INFOPASS"==radioValue){
            			operation="infoStatus";
            			var param={"orderId":${order.orderId!''},"infoStatus":"${order.infoStatus!''}","orderRemark":orderRemark,"operation":operation};
            			
            			faxDialog = new xDialog("/vst_order/order/orderStatusManage/showSendOrderFax.do",param,{title:"发送凭证",width:600});
            			return;
            		}else if("RESOURCEPASS"==radioValue){
            			operation="resourceStatus";
            			
            			var param={"orderCatType":"hotel","orderId":${order.orderId!''},"orderItemId":"${orderItem.orderItemId!''}","resourceStatus":"${orderItem.resourceStatus!''}","orderRemark":orderRemark,"operation":operation};
            			retentionTimeDialog = new xDialog("/vst_order/order/orderShipManage/showUpdateRetentionTime.do",param,{title:"修改资源保留时间",width:600});
            			return;
            		}else if("certificateStatus"==radioValue){
            			operation="certificateStatus";
            			
            			
     				var param={"orderId":"${order.orderId!''}","orderRemark":orderRemark,"operation":operation,"isSupplierOrder":"${isSupplierOrder!''}"};
            			
            			certificateDialog = new xDialog("/vst_order/order/orderStatusManage/showAddCertificate.do",param,{title:"凭证确认",width:600});
     				return;
            		
            		
            			
            		}else if("paymentAudit"==radioValue){
            			operation="paymentAudit";
            		
            		}else if("timePaymentAudit"==radioValue){
            			operation="timePaymentAudit";
            		
            		}else if("cancelStatusConfim"==radioValue){
            			operation="cancelStatusConfim";
            		}else if("onlineRefundConfirm"==radioValue){
            			operation="onlineRefundConfirm";
            		}else {
     	       		/**
     	       		if("RESOURCE_NO_CONFIM"==radioValue || "CUSTOMER_NOTICE"==radioValue
     	       			 || "INFO_NO_PASS"==radioValue || "ABANDON_ORDER_REPEAT"==radioValue || "OTHER_REASON"==radioValue){
     	       		*/	
            			cancleReason=$("#cancleReason").val();
            			if(cancleReason=='0')
     				{
     					$.alert("取消原因还未选择");
     					return;
     				}
            			 
            			operation="cancelStatus"; 
            			cancelCode=radioValue;
            			
            			var cancleReasonText=$("#cancleReason").find("option:selected").text();
            			console.log('click');
            			//分销订单并且订单正常并且已支付${order.orderId}
                   		if(operation == "cancelStatus" && isDistributor(distributorCode) && 'NORMAL' == orderStatus && 'PAYED' == paymentStatus) {
            				var param = "actualAmount=" + "${order.actualAmount/100}" + "&operation="+operation+"&cancelCode="+cancelCode
            					+"&cancleReasonText="+cancleReasonText+"&orderRemark="+orderRemark+"&orderId="+orderId;
            				saledAppliedDialog = new xDialog("/vst_order/order/orderManage/showSaledApplied.do",param,{title:"售后申请确认",width:600});
                   		}else{
            				confirmUpdate(operation, cancelCode, cancleReasonText);
                   		}

            			return;
            			
            		}
            		
            		confirmUpdate(operation, cancelCode, cancleReasonText);
     			
     						
     		});

			if(${messageCount}>0){
			  messageDialog = new xDialog("/vst_order/order/orderStatusManage/findComMessageList.do",{"orderId":${order.orderId!''}},{title:"查看预订通知",width:1200});
		     }
     
      		if(${compliantCallsCount}>0){
        		var title = "用户 " + "${ordPersonBooker.fullName}" + " 的未处理信息";
      			var userName = encodeURIComponent(encodeURIComponent("${ordPersonBooker.fullName}"));
        		var entryPeople = encodeURIComponent(encodeURIComponent("${loginUserId}"));
        		var param = "orderId=${order.orderId}&refreshOnClose=Y&userName=" + userName + "&entryPeople=" + entryPeople;
        		showUntreatedComplaintCallDialog = new xDialog("http://super.lvmama.com/sales_front/complaintCall/orderDetail/showComplaintCallByCustomer.do?" + param,{},{title:title,width:1200,height:750,iframe:true});
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
		var ordPersonBookerData="orderType=parent&orderId=${order.orderId}";
     	$.post("/vst_order/order/orderManage/findOrdPersonBooker.do",
		   ordPersonBookerData,
		   function(result){
		   
		 	//$("#findOrdPersonBooker").html(result);
  		});	
  		*/
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
			var param={"orderId":${order.orderId!''},"orderItemId":${orderItem.orderItemId!''},"orderRemark":orderRemark,"categoryId":${order.categoryId!''},"buCode":"${order.buCode!''}"};
			addMessageDialog = new xDialog("/vst_order/order/orderStatusManage/showAddMessage.do",param,{title:"创建预订通知",width:800});
         
         });
         
        var yiLongDeductDialog;
		$("#yiLongDeduct").bind("click",function(){

			var orderRemark=$.trim($("#orderRemark").val());
			var param={"orderId":${order.orderId!''},"orderItemId":${orderItem.orderItemId!''}};
			yiLongDeductDialog = new xDialog("/vst_order/order/orderStatusManage/showYLDeduct.do",param,{title:"艺龙扣款",width:400});
         
         });
           

    var showLogDialog;
    $("#showLogDialog").bind("click",function(){
        var param = "parentType=ORD_ORDER&parentId="+${order.orderId}+"&sysName=VST";
        showLogDialog = new xDialog("/lvmm_log/bizLog/showVersatileLogList?"+param,{},{title:"日志",iframe:true,width:1000,hight:500,iframeHeight:680,scrolling:"yes"});
    });

    //子单日志
    var showOrderItemLogDialog;
    $("#showOrderItemLogDialog").bind("click",function(){
        var param = "objectType=ORD_ORDER_ITEM&objectId="+${orderItem.orderItemId!''}+"&sysName=VST";
        showOrderItemLogDialog = new xDialog("/lvmm_log/bizLog/showVersatileLogList.do?"+param,{},{title:"子单日志",iframe:true,width:1000,hight:500,iframeHeight:680,scrolling:"yes"});
    });
    
    
         $("#showSoundRec").bind("click",function(){

			var data="orderId="+${order.orderId}+"&page="+1;
			showLogDialog = new xDialog("/vst_order/order/orderStatusManage/showSoundRecList.do",data,{title:"查看录音",width:1200});
         
         });
    
         $(function(){
         	//关联callid与orderid
         	var callId = "${callid}";
         	var orderId = ${order.orderId};
         	
			if (callId == "undefined" || callId.length == 0 || callId == "null"){
				return;
			}
         	
         	var options = {
					url:"/vst_order/order/orderStatusManage/saveCallIdAndOrderId.do",
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
    
           
       var presentData = "orderId=${order.orderId!''}";
         	$.post("/vst_order/order/orderManage/findBuyPresentList.do",
			   presentData,
			   function(result) {
			   
			 	$("#findBuyPresentList").html(result);
     		});
       
       /**
 		$("#showLog").bind("click",function(){
         
         	$("#logResult").html("");
         	$("#logResult").css("display","block");
         	//var page=$("#page").val();
         	//alert(page);
         	var data="orderId="+${order.orderId}+"&page="+1;
         	
         	$.post("/vst_order/order/orderStatusManage/logList.do",
			   data,
			   function(result){
			   
			  // alert(result);
			   $("#logResult").html(result);
			   
			  
			});	
			
			 $('html, body, .content').animate({scrollTop: $(document).height()}, 300); 
         
         });
         */
         
         var houseStatusDialog;
		$("#roomStatus").bind("click",function(){

			var param={"source":"orderHotelDetails",suppGoodsId:"${orderItem.suppGoodsId}",productId:"${orderItem.productId}",supplierId:"${suppSupplier.supplierId}","productName":'${orderItem.productName}',"supplierName":'${suppSupplier.supplierName}',districtName:'${city}'};
			houseStatusDialog =new xDialog("${showHouseStatus}",param,{title:"确认房态",width:800});
			//houseStatusDialog =new xDialog("/vst_order/goods/house/showHouseStatus.do",{suppGoodsId:suppGoodsId,productId:${orderItem.productId},supplierId:${suppSupplier.supplierId},productName:productName,supplierName:supplierName,districtName:districtName},{title:"确认房态",width:800});
         
         });
         var viewSupplierDialog;
        $("#supplierName").bind("click",function(){
			viewSupplierDialog = new xDialog("/vst_back/supp/supplier/showViewDistrict.do",{"supplierId":"${suppSupplier.supplierId}","suppGoodsId":"${orderItem.suppGoodsId}"},{title:"查看供应商",width:800});
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
        	<#if order.contactPerson?? && order.contactPerson.ordPersonId??>
			updatePersonDialog = new xDialog("/vst_order/order/orderStatusManage/showUpdatePerson.do",{"ordPersonId":${order.contactPerson.ordPersonId}},{title:"修改联系人",width:400});
			<#else>
			alert("联系人信息不全");
			</#if>
		});       
		        
		        
		 var faxDialog,sendOderFaxDialog;
        $("#sendOderFax").bind("click",function(){
        	var orderRemark=$.trim($("#orderRemark").val());
            var cancelCertConfirmStatus = '${cancelCertConfirmStatus!''}';
            if (cancelCertConfirmStatus && cancelCertConfirmStatus != 'REJECT' && "${faxFlag!''}"!="Y") {
                sendOderFaxDialog = new xDialog("/vst_order/order/orderManage/showOrderCertifDialog.do",{"cancelCertConfirmStatus":cancelCertConfirmStatus,"orderItemId":${orderItem.orderItemId!''}},{title:"发送凭证",width:600});
            } else {
                sendOderFaxDialog = new xDialog("/vst_order/order/orderStatusManage/showManualSendOrderFax.do",{"orderId":${order.orderId!''},"orderRemark":orderRemark,"source":"noInfoPass","cancelCertConfirmStatus":cancelCertConfirmStatus},{title:"发送凭证",width:600});
            }

		});    
		var findEbkFaxListDialog;
		// 添加分销邮件标识 by xiexun
		var distributionmailflag;
		if('${orderItem.content}'!=null){
   				distributionmailflag ='${orderItem.contentMap["distribution_mail_flag"]}';
		}
		$("#findEbkFaxList").bind("click",function(){
			findEbkFaxListDialog = new xDialog("/vst_certif/ebooking/task/findEbkAndFaxList.do",{"orderId":${order.orderId!''},"distributionmailflag":distributionmailflag},{title:"凭证查询",width:1300});
		});  
		
		var findEbookingFaxRecvListDialog;
		$("#findEbookingFaxRecvList").bind("click",function(){
			findEbookingFaxRecvListDialog = new xDialog("/vst_certif/ebooking/faxRecv/findEbookingFaxRecvList.do",{"orderId":${order.orderId!''},"readUserStatus":"Y","source":"orderHotelDetails"},{title:"查回传",width:600});
		});  
		
		
		     
        $("#orderIdSeachButton").bind("click",function(){
         
         	var orderIdSeach=$("#orderIdSeach").val();
         	if($.trim(orderIdSeach)==''){
         		alert("订单号不能为空");
         		return false;
         	}
         	document.getElementById("orderIdSeachButton").href="/vst_order/ord/order/orderMonitorList.do?orderId="+orderIdSeach;
         	//	window.showModalDialog("/vst_order/ord/order/orderMonitorList.do","orderId:"+orderIdSeach,"resizable:yes");
       		 // addOrdFuncRelationDialog = new xDialog("/vst_order/ord/order/orderMonitorList.do",{"orderId":orderIdSeach},{title:"订单查询列表",width:800,height:500});
         	
         	
         
         });
        
        
	
   function confirmUpdate(operation, cancelCode, cancleReasonText) {
	 	//遮罩层
		var loading = pandora.loading("正在努力保存中...");		

  		var formData=$("#dataForm").serialize()+"&operation="+operation+"&cancelCode="+cancelCode+"&cancleReasonText="+cancleReasonText;
  		//alert(formData);
  		$.ajax({
		   url : "/vst_order/order/orderStatusManage/updateOrderStatus.do",
		   data : formData,
		   type:"POST",
		   dataType:"JSON",
		   success : function(result){
		   		//var message=result.message;
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
   }
	
	/**
     * 更新子订单确认状态
     * @param obj
     * @param initStatus 页面初始化状态
     */
	function updateConfirmStatus(obj, initStatus){
		var form = $(obj).parents("form").eq(0);
		var orderItemId = form.find("[name=orderItemId]").val();
        //表单提交时状态
		var updateStatus = form.find("[name=confirmStatus]").val();

		var orderItemRemark=$.trim($("#orderItemRemark").val());

        //更新订单备注，状态不变
        if (initStatus == updateStatus) {
            alert("确认状态未做修改！");
        } else {
            //修改订单备注和状态
            var confirmId = form.find("[name=confirmId]").val();
            var re = /^[0-9A-Za-z]*$/;
            var isOk=true; //参数验证是否ok
            if(orderItemId==""||updateStatus==""){
                alert("未选择确认状态！");
                isOk=false;
            }
            if(typeof(confirmId)!="undefined"&&confirmId!=""){
                confirmId=$.trim(confirmId);
                if(!re.test(confirmId)){
                    alert("确认号不符合要求");
                    isOk=false;
                }
            }

            if(isOk==true){
	            var r=confirm("确定修改订单"+orderItemId+"备注？");
	            if (r==true){
	                //遮罩层
	                var loading = pandora.loading("正在努力保存中...");

	                var formData="orderItemId="+orderItemId+"&initStatus="+initStatus+"&updateStatus="+updateStatus+"&confirmId="+confirmId+"&orderMemo="+orderItemRemark;
	                $.ajax({
	                    url : "/vst_order/ord/order/confirm/updateConfirmStatus.do",
	                    data : formData,
	                    type:"POST",
	                    dataType:"JSON",
	                    success : function(result){
	                        if(result.code=="success" ){
	                            loading.close();
	                            alert(result.message);
	                            document.location.reload();
	                        }else {
	                            loading.close();
	                            alert(result.message);
	                            document.location.reload();
	                        }
	                    },
	                    error: function(XMLHttpRequest, textStatus, errorThrown) {
	                        loading.close();
	                        if(textStatus=='timeout'){
	                            alert("程序运行超时");
	                            document.location.reload();
	                        }else{
	                            alert("程序运行出现异常");
	                            document.location.reload();
	                        }
	                    }
	                });
	            
	            }
	        }    
        }
	}
	
	//确认号隐藏
	function confirmIdDisplay(obj){
		var selected = $(obj).val();
		var form = $(obj).parents("form").eq(0);
		var confirmIdInput = form.find("[name=confirmId]");
		var theDiv = confirmIdInput.parent("div");
		if(selected == "SUCCESS"){
			theDiv.show();
		}else{
			confirmIdInput.val("");
			theDiv.hide();
		}
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
        
        function checkItemRemarkLength(){
	        var orderRemark=document.getElementById('orderItemRemark');
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

        //Added by yangzhenzhong 目的地酒店新增重发取消按钮
        $("#reSendCancelApply").bind("click",function(){
            if(window.confirm("确定重新取消？")){
                //遮罩层
                var loading = pandora.loading("正在努力保存中...");
                $.ajax({
                    url : "/vst_order/order/orderStatusManage/reSendCancelApply.do",
                    data : {orderItemId:${orderItem.orderItemId!''}},
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
            }
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
    
     var showAmountDialog;
     
    //定义全局的orderId，给引入的js使用
    var orderId = '${order.orderId}';
    var mobile = '${order.contactPerson.mobile}';
    
    var orderType='';
    
    var ordType='parent';
    
       //主订单优惠券明细 
	  $("#favorUsageDetail").bind("click",function(){
		new xDialog("/vst_order/order/orderManage/showOrderFavorUsageDetails.do",{"orderId":${order.orderId!''}},{title:"主订单优惠券明细",width:1200});
      });  
         
      //主订单促销明细 
	  $("#promotionDetail").bind("click",function(){
		new xDialog("/vst_order/order/orderManage/showOrderPromotionDetails.do",{"orderId":${order.orderId!''}},{title:"主订单促销明细",width:600});
      });

     //预售券抵扣总金额
      $("#stampDeductionCountAmount").bind("click",function(){
        new xDialog("/vst_order/order/orderManage/showstampDeductionList.do?",{"orderId":${order.orderId!''}},{title:"查看预售劵抵扣明细",width:800});
      });
    
</script>
<script>
	var editInsurePersonButtonDialog;
	$("#editInsurePersonButton").bind("click",function(){
		editInsurePersonButtonDialog = new xDialog("/vst_order/ord/order/update/showUpdateInsurePersonList.do",{"orderId":${RequestParameters.orderId!''}},{title:"修改保险人",width:800,dialogAutoStop: true});
	});
</script>
<script src="/vst_order/js/order/orderAttachment.js"></script>
<script src="/vst_order/js/order/orderSendSms.js"></script>
<script src="/vst_order/js/order/orderPayment.js"></script>
<script src="/vst_order/js/order/orderAmountChange.js"></script>
<#--携程促销处理-->
<script src="${rc.contextPath}/js/book/supplier_promotion.js"></script>
<script>buildOfferDescriptionForStr('${snapshotCtripHotelPromotion?js_string}');</script>
