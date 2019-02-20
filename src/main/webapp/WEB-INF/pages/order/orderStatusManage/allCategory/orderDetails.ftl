<#assign mis=JspTaglibs["/WEB-INF/pages//tld/lvmama-tags.tld"]>
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-订单处理</title>
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
    <form id="oneKeyOrderForm" method="post" action="/vst_order/ord/order/queryLineDetailList.do">
    	<input type="hidden" name="orderCreatingManner" value="2">
    	<input type="hidden" name="specDate" value="${order.visitTime?string("yyyy-MM-dd HH:mm:ss")}">
    	<input type="hidden" id="oneKeyOrderForm_originalOrderId" name="originalOrderId" value="${order.orderId!''}">
    	<input type="hidden" name="startDistrictId" value="${order.startDistrictId!''}">
    	<input type="hidden" name="productId" value="${productId!''}">
    	<input type="hidden" name="distributionId" value="2">
    	<input type="hidden" name="userId" value="${order.userId!''}">
    </form>
    
    <form id="userSelectBaseForm" style="display:none">
		<table class="pg_d_table table_center">
		    <thead>
		        <tr>
		            <th style="width:30px;">操作</th>
		            <th>会员名称</th>
		            <th>手机号码</th>
		            <th>邮箱</th>
		            <th>会员卡号</th>
		            <th>会员状态</th>
		        </tr>
		    </thead>
		    <tbody>
		        <tr>
		            <td><input type="radio" name="user_id" userName="" value="" checked="checked"/></td>
		            <td data-type='name'></td>
		            <td data-type='mobileNumber'></td>
		            <td data-type='email'></td>
		            <td data-type='memberShipCard'></td>
		            <td data-type='zhUserStatus'></td>
		        </tr>
		    </tbody>
		</table>
		<div class="fl operate mt10"><a class="btn btn_cc1" href="javascript:selectUser()">确定</a></div>
	</form>
    
    <form method="post" id="dataForm" onsubmit="return false;">
    <div class="order_main iframe_content mt10" id="iframeDiv">
        <div class="order_msg clearfix">

        		【主订单负责人】  ${orderPrincipal!''}
            	【主订单】  订单号：${order.orderId !''}
             	【包含子订单】：<#if (order.orderItemIdList??)>${order.orderItemIdList?size}<#else>0</#if>个

            <div class="order_seach">查询订单号：<input type="text" id="orderIdSeach"/><a class="btn ml10" id="orderIdSeachButton" target="_blank">查询</a> </div>
            <#if order.clientIpAddress=="180.169.51.82" && order.distributorId==3>
               	</br><div style="color:red">（请特别特别注意，此订单疑似驴妈妈内部成员下单）</div>
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
                        
                       
                         <#if   auditMap['PRETRIAL_AUDIT']==true>
	                         
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
                        
                        <#if isDestBuFrontOrder==true>
                        	<#if order.categoryId == 33>
                             	
                            <#else>
                             	<tr>
		                            <td>凭证状态</td>
		                            <td>
		                            <#if certConfirmStatus==true>
		                             	<img src='../../img/pass.png' width='20' height='20' alt='已确认'/>
			                         <#else>
			                         	 未确认
		                             </#if> 
		                            </td>
		                            <td><a id="viewChildOrderList" href="javaScript:">[查看子订单凭证状态]</a></td>
		                        </tr>
                            </#if>
                        </#if>
                        
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
		                              <a href="javascript:void(0);" class="viewSendNoticeList" data="${order.orderId}" contactEmail="<#if ordPersonContact?exists>${ordPersonContact.email!''}</#if>">[查出团]</a>
		                             </td>
		                           
		                        </tr>
	                        
	                        <#elseif isDoneNoticeRegimentAudit==true>
		                         
			                    <tr>
		                            <td>通知出团</td>
		                            <td>
		                            <img src='../../img/pass.png' width='20' height='20' alt='通过'/>
		                            </td>
		                             <td>
		                            <a href="javascript:void(0);" class="viewSendNoticeList" data="${order.orderId}" contactEmail="<#if ordPersonContact?exists>${ordPersonContact.email!''}</#if>">[查出团]</a>
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
                     <!--是否保留房-->
                         <#if order.buCode?? && order.buCode!="OUTBOUND_BU">
                           <#if hasHotel=="Y">
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
                         </#if>
                    </tbody>
                </table>
            </div>
            <div class="solid_line"></div>
            
              <#if isExistContract>
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
			                            	<a target="_blank" href="/vst_order/order/orderManage/showTravelContractList.do?orderId=${order.orderId}">[查看合同]</a>
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
                  		<#if order.buCode=="LOCAL_BU" && (order.categoryId==15 || order.categoryId==16 || (order.categoryId==18 && order.subCategoryId==182) || order.categoryId==29)>
                  			<#if order.paymentStatus=="PAYED" > 
		                   	</#if>
                  		<#else>
                  		   <#if order.paymentStatus=="PAYED" > 
		                       (已预授权支付)
		                   <#else>
		                  	   (强制预授权未支付)
		                   </#if>
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
                            
                            <td><a href="javaScript:" id="orderPaymentInfo">[支付相关操作]</a></td>
                            
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
                            <td>RMB ${(order.oughtAmount-order.actualAmount - payPromotion)/100}元</td>
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
                            <#if order.normal && hasPreauthBook==false && order.paymentStatus!="PAYED"> 
                             <a href="javaScript:" id="updateWaitPaymentTime">[修改时间]</a>
                            </#if> 
                           
                            </td>
                         </tr order.>
                         <#if order.orderSubType=='STAMP' && order.stampPayType=='PART'>
                         <tr>
                            <td >尾款支付等待时间：</td>
                            <td>
                            	${waitRetainageTime!''}  
                            </td>
                            <td>
                            <#if order.normal && hasPreauthBook==false && order.paymentStatus!="PAYED" > 
                             <a href="javaScript:" id="updateWaitRetainageTime">[修改时间]</a>
                            </#if> 
                            </td>
                         </tr>
                         </#if>
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
                <h4><div style="float:left; margin-right: 5px;">订单取消<span style="color:red;font-size:14px;">(订单${orderStatusStr})</span></div>
                <#if canPreRefund==true>
                <div style="background:yellow;font-size:14px;width: 80px;height: 20px;-moz-border-radius: 20px / 10px; -webkit-border-radius: 20px / 10px;border-radius: 20px / 10px;float: left;"><center>提前退</center></div>
                </#if></h4>

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
            <!-- 一键重下 -->
            <div class="solid_line" div-type="backOneKeyRecreating"></div>
            <div class="side_setbox sidebox" div-type="backOneKeyRecreating" >
                <a class="btn btn_cc1" id="backOneKeyRecreatingButton">一键下单</a>
            </div>
        </div>
        <div class="main equalheight_item">
            <div class="main_con clearfix">
                <div class="main_order_msg">
                    <div><a id="viewOrderAttachment" class="fr" href="javaScript:">附件(<b>${orderAttachmentNumber}</b>)</a>订单备注记录:</div>
                    <textarea style="width:285px; height:120px;" id="orderRemark" name="orderRemark" onkeyup="checkRemarkLength()" >${order.orderMemo!''}</textarea>
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
                            <#if order.orderSubType!='STAMP'>
                            <li><a id="showInvoiceMessage" target="_blank" href="/vst_order/order/orderInvoice/showInvoiceInfo.do?orderId=${order.orderId!''}" title="发票">发票信息</a></li>
                             <#if order.orderStatus=="NORMAL" && order.paymentStatus == "UNPAY" >  
                              <@mis.checkPerm permCode="5920"><li><a id="changeAmount" href="javaScript:" title="修改价格">修改价格</a></li></@mis.checkPerm>
                            </#if>
                            </#if>
                            <li><a id="showOrderSMS" href="javaScript:;" title="">查看短信</a></li>
                            <#--<li><a id="showOrderRemark" href="javaScript:;" onclick="showOrderRemark(${order.orderId});" title="">订单备注</a></li>-->
                            <#if order.orderSubType!='STAMP'>
                            <#if order.categoryId !=32 ><li><a id="viewPassCode"  target="_blank" href="/vst_passport/passCode/findList.do?orderId=${order.orderId}"  title="查看申码">查看申码</a></li></#if>
                            <#if order.categoryId !=32 && order.orderSubType!='STAMP'><li><a id="findPassPortCodeList"  target="_blank" href="/vst_passport/passCode/findPassPortCodeList.do?orderId=${order.orderId}"  title="查看通关码">查看通关码</a></li></#if>
                            
                            
                            <#if hasVisa==true>  
                             <li><a id="findOrderVisaApprovalQueryList"   href="javaScript:" title="查看签证审核">查看签证审核</a></li>
                            </#if>
                            <#--
                            
                             -->
                             <li><a id="findInsurancePolicyList"   target="_blank" href="/vst_insurant/insurancePolicy/findList.do?orderId=${order.orderId}"  title="查看投保状态">查看投保状态</a></li>
                             
                             <#if isReFundButtonShow?? && isReFundButtonShow == true>
                             	<li><a id="partRefundApplication"   target="_blank" href="/vst_order/partRefundBackAction/toPartRefundPage.do?orderId=${order.orderId}"  title="退款申请">退款申请</a></li>
                           	 </#if>
                             
                             <#if order.isContainApiFlightTicket() && order.hasPayed() && order.hasInfoAndResourcePass()> 
                             	<li><a id="sendFlightPaymentInfo" class="sendFlightPaymentInfo" href="javascript:;"  title="机票支付通知">机票支付通知</a></li>
                             </#if>
                             <#if order.getDistributorId()=="4"> 
                                <li><a id="queryDistributor"  href="javascript:;" onclick="showDistributor(${order.distributionChannel});"  title="分销商信息">分销商信息</a></li>
                             </#if>
                             </#if>
                             
                             <li><a id="showSoundRec" href="javaScript:" title="查看录音">查看录音</a></li>
                             <li><a id="showUntreatedComplaintCall" href="javaScript:" title="未处理用户信息">未处理用户信息</a></li>
							<li><a id="addComplaintCall" href="javaScript:" title="新增用户信息">新增用户信息</a></li>
							<li><a id="myCreatedComplaintCall" href="javaScript:" title="我创建的用户信息">我创建的用户信息</a></li>
							<li><a id="addWork" href="javaScript:" title="新增工单">新增工单</a></li>

                            <#if showTransfer==true>
                                <li><a id="showTransfer" href="javaScript:" title="申请资金转移">申请资金转移</a></li>
                            </#if>
                            <#if showLosses == true>
                                <li><a id="showLosses" href="javaScript:" title="申请定金核损">申请定金核损</a></li>
                            </#if>
							<li><a id="showCbpInfo" href="javaScript:" title="中行支付查询">中行支付查询</a></li>
							<#if order.categoryId == 33>
                             	<li><a id="selectRight"   href="javaScript:" title="查看权益">查看权益</a></li>
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
                            <!--
                            <tr>
                                <td class="e_label">销售渠道ID：</td>
                                <td>${order.distributorId !''} </td>
                            </tr>
                            -->
                             <tr>
                                <td class="e_label" id="distributorCodeTitle">渠道代码/名称：</td>
                                <td id="distributorCodeInfo">${order.distributorCode !''}/${distributionChannelName!''} <#if tntOrderChannel?? && tntOrderChannel!=''>/${tntOrderChannel!''}</#if></td>
                            </tr>
                             <tr>
                                <td class="e_label" id="distributionChannelTitle">分销商渠道ID：</td>
                                <td id="distributionChannelInfo">${order.distributionChannel !''} </td>
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

                            <#if ordItemsAperiodicExpList?size gt 0>
                                <tr>
                                    <td class="e_label">有效期：</td>
                                    <td>
                                        <#list ordItemsAperiodicExpList as item>
                                            <p>${item}</p>
                                        </#list>
                                    </td>
                                </tr>
                            </#if>

                            <#if ordItemsAperiodicUnvalidList?size gt 0>
                            <tr>
                                <td class="e_label">不适用日期：</td>
                                <td>
                                    <#list ordItemsAperiodicUnvalidList as item>
                                        <p>${item}</p>
                                    </#list>
                                </td>
                            </tr>
                            </#if>

                            <tr>
                                <td class="e_label">
                                	<#if order.categoryId == 11 || order.categoryId == 12 || order.categoryId == 13 || order.categoryId == 188> 
										<b>退票政策：</b>
									<#else>
										退改政策：
									</#if>
								</td>
								<#if order.orderSubType=='STAMP'>
									<td>
										<#if order.categoryId == 11 || order.categoryId == 12 || order.categoryId == 13> 
										   <b>${cancelStrategyType}</b>
										<#else>
											${cancelStrategyType}
										</#if>
									</td>
								<#else>
									<#if realCancelStrategyTypeStr??>
										<td>
											<#if order.categoryId == 11 || order.categoryId == 12 || order.categoryId == 13> 
												<b>${realCancelStrategyTypeStr}</b>
											<#else>
												${realCancelStrategyTypeStr}
											</#if>
										</td>                            	
									<#else>
										<#if isTicket ="Y">
											<td>
												<#if order.categoryId == 11 || order.categoryId == 12 || order.categoryId == 13> 
													<b>${refoundStr!''}</b>
												 <#else>
													${refoundStr!''}
												</#if>
											</td>
                                        <#elseif order.categoryId == 29 >
                                            <td>
                                                ${order.realCancelStrategy!''}
                                            </td>
										<#else>
											<td>
												${cancelStrategyTypeStr}
												<#if order.paymentTarget=="PAY" >
													<#if order.guarantee=="GUARANTEE" >
														担保订单
													</#if>
												</#if>
												<#if order.cancelStrategy?? && order.cancelStrategy=="RETREATANDCHANGE" >
													扣款金额[(${deductAmountStr}元)]
													<#if minLastCanTime??>
														<#if isGreaterNow==1 >
															<span class="lineae_line">${minLastCanTime?string('yyyy-MM-dd HH:mm')!''} 前无损取消</span>
														<#else>
															${minLastCanTime?string('yyyy-MM-dd HH:mm')!''}  前无损取消
														</#if>
													</#if>
												</#if>	
											</td>
										</#if>    
									</#if>    
								</#if> 
								<#assign innerLvmamaPack = prodProduct.packageType=='LVMAMA' && (prodProduct.productType=='INNER_BORDER_LINE' || prodProduct.productType=='INNERLINE' || prodProduct.productType=='INNERSHORTLINE' || prodProduct.productType=='INNERLONGLINE')/>  
								<#if innerLvmamaPack || order.buCode == 'DESTINATION_BU' && (order.categoryId==32 || order.categoryId==17 || (realCancelStrategyTypeStr?? && order.subCategoryId==181))>
									<tr>
										<td></td>
										<td>${cancelStrategyRules}</td> 
									</tr>
								</#if>
                            </tr>
                            <#if ordRescheduleStatus?? && (order.categoryId == 11 || order.categoryId == 12 || order.categoryId == 13)>
								<tr>
									<td class="e_label">
										改期政策：
									</td>
									<td>
										<#if ordRescheduleStatus.ordChangeDesStrList?? && (ordRescheduleStatus.ordChangeDesStrList?size > 0) >
										<#list ordRescheduleStatus.ordChangeDesStrList as ordChangeDesStr>
											${ordChangeDesStr}<br>
										</#list>
										</#if>
									</td>
								</tr>
                            </#if>
                            <!-- 超级会员品类不可改期 -->
                            <#if order.categoryId == 188>
                            	<tr>
									<td class="e_label">
										改期政策：
									</td>
									<td>
										本商品不可改期
									</td>
								</tr>
                            </#if>
                            <tr>
                                <td class="e_label">所属公司：</td>
                                <td>${order.zhFilialeName !''}</td>
                            </tr>
                            <tr>
                                <td class="e_label">所属产品经理：</td>
                                <td>
									<#if productManager??>
										${productManager.realName!''}
									</#if>                           
                                </td>
                            </tr> 
                            <tr>
                                <td class="e_label">产品经理手机号：</td>
                                <td>
                                <#if productManager??>
                                 ${productManager.mobile!''}
                                </#if>
                                <#if productManager??>
                                	<#if productManager.mobile!=''>
                                		<a href="http://localhost:12366/ipcc/default.jsp?webcallout&webcalloutno=${productManager.mobile!''}">[呼叫]</a>
                                    </#if>
                                </#if>
                                </td>
                                	
                                </td>
                            </tr> 
                            <tr>
                                <td class="e_label w10">APP版本号：</td>
                                <td>${order.appVersion!''} </td>
                            </tr>
                            <tr>
                                <td class="e_label w10">订单流程版本：</td>
                                <td>${order.workVersion!''} </td>
                            </tr>
                        <#if groupPurchaseOrder??>
                        <tr>
                            <td class="e_label w10">是否为拼团订单：</td>
                            <td>是</td>
                        </tr>
                        <tr>
                            <td class="e_label w10">拼团订单号：</td>
                            <td><a target="_blank" href="http://super.lvmama.com/lvmm-grouppurchase-process/order/groupOrderDetail?groupOrdId=${groupPurchaseOrder.groupOrdId}">${groupPurchaseOrder.groupOrdId!''}</a></td>
                        </tr>
                        </#if>
                        </tbody>
                    </table>
                    <div class="solid_line mt10 mb10"></div>
                    <table>
                        <tbody>
                            <tr>
                                <td class="e_label">产品名称：</td>
                                   <#assign isPro="falseUrl"/>
                                   <#if order.categoryId??>
	                                   <#if order.categoryId =="15">
		                                <!--线路跟团游-->                               
		                                <td><a href="http://dujia.lvmama.com/group/${order.productId}/preview " target="_blank" title="点击打开前台页面">${order.orderProductName!''}(${order.productId!''})</a></td>
		                                <#assign isPro="isUrl"/>
		                               <#elseif order.categoryId =="18">
		                               <!--线路自由行-->
		                               <td><a href="http://dujia.lvmama.com/freetour/${order.productId}/preview " target="_blank" title="点击打开前台页面">${order.orderProductName!''}(${order.productId!''})</a></td>
		                               <#assign isPro="isUrl"/>
		                               <#elseif order.categoryId =="16">
		                               <!--当地游-->
		                               <td><a href="http://dujia.lvmama.com/local/${order.productId}/preview" target="_blank" title="点击打开前台页面">${order.orderProductName!''}(${order.productId!''})</a></td>
		                               <#assign isPro="isUrl"/>
		                               <#elseif prodProduct.bizCategory.categoryCode=="category_other_ticket">
		                               <!--其它票-->
		                               <td><a href="http://ticket.lvmama.com/p-${order.productId}" target="_blank" title="点击打开前台页面">${order.orderProductName!''}(${order.productId!''})</a></td>
		                                 <#assign isPro="isUrl"/>
		                                <!--景点门票-->
		                                <#elseif prodProduct.bizCategory.categoryCode=="category_single_ticket">
		                                 <td><a href="http://ticket.lvmama.com/scenic-${prodProduct.urlId}" target="_blank" title="点击打开前台页面">${order.orderProductName!''}(${order.productId!''})</a></td>
		                                 <#assign isPro="isUrl"/>		                               
		                                 <!--签证-->
		                                <#elseif order.categoryId =="4">		                                
		                                <td><a href="http://www.lvmama.com/visa/${prodProduct.bizDistrict.pinyin}"  target="_blank">${order.orderProductName!''}(${order.productId!''})</a></td>
		                                 <#assign isPro="isUrl"/>
		                                 <!--酒店套餐-->
		                                 <#elseif order.categoryId =="17">
		                                 <td><a href="http://dujia.lvmama.com/package/${order.productId}/preview"  target="_blank">${order.orderProductName!''}(${order.productId!''})</a></td>
		                                 <#assign isPro="isUrl"/>
		                                 <!--酒套餐-->
		                                 <#elseif order.categoryId =="32">
		                                 <td><a href="http://dujia.lvmama.com/packageNew/${order.productId}/preview"  target="_blank">${order.orderProductName!''}(${order.productId!''})</a></td>
		                                 <#assign isPro="isUrl"/>
		                                 <!--演出票-->
		                                  <#elseif order.categoryId =="31">
		                                 <td><a href="http://ticket.lvmama.com/show-${order.productId}"  target="_blank">${order.orderProductName!''}(${order.productId!''})</a></td>
		                                 <#assign isPro="isUrl"/>
		                                 <!--定制游-->
		                                <#elseif order.categoryId =="42">
			                              <td><a href="http://dujia.lvmama.com/customized/${order.productId}/preview " target="_blank" title="点击打开前台页面">${order.orderProductName!''}(${order.productId!''})</a></td>
			                              <#assign isPro="isUrl"/>
		                             </#if>		                               
                                </#if>
                                 <#if isPro=="falseUrl">
                                 <td>${order.orderProductName!''}(${order.productId!''})</td>
                                 </#if>                                       
                            </tr>
	                             <#if order.categoryId == 33>
	                            	
	                            <#else>
	                            	<#if startDistrictBo?? && startDistrictBo.districtName>
	                                <tr>
	                                    <td class="e_label">出发地：</td>
	                                    <td>${startDistrictBo.districtName}</td>
	                                </tr>
	                            </#if>
	                            </#if>                                                  
                            <!--显示团结算标识-->
                            <#if groupSettleFlag??>
                                <tr>
                                    <td class="e_label">是否团结算：</td>
                                    <td>${groupSettleFlag}</td>
                                </tr>
                            </#if>
                            <tr>
                                <td class="e_label">用户退款总金额：</td>
				<td>
				 <#if refunds??>
                                	${refunds}
                                <#else>
                                	0.00
                                </#if>
                                </td>
                            </tr>
                            <tr>
                                <td class="e_label">补偿用户总金额：</td>
				<td>
				 <#if compensations??>
                                	${compensations}
                                <#else>
                                	0.00
                                </#if>
                                </td>
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
                           <#if travDelayStatus?? && travDelayStatus=="ABANDON">
                            <tr>
                                <td class="e_label">退款金额：</td>
                                <td>${accInsRefundedAmount}   <strong>[意外险 * ${accInsQuantity}份]</strong></td>
                            </tr>
                           </#if>
                           <#if order.orderSubType=='STAMP_PROD'>
                           <#if  prodProduct.bizCategory.parentId==14|| prodProduct.bizCategory.categoryId==1>
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
                            <#if order.categoryId?? && order.categoryId =="31">
                            <td class="e_label">演出日期：</td>  
                             <#else>
                             <td class="e_label">出发日期：</td> 
                             </#if>  
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
	                       <td>
                            ${order.visitTime?string('yyyy-MM-dd')!''}
                            </td>
                            </#if>
                            
                            <#--
                                <td class="e_label">出发日期：</td>  
                                <td><strong>[${arrivalDays}天]</strong> ${order.visitTime?string('yyyy-MM-dd') !''} 至 ${orderPackContentMap['end_sailing_date']!''}
                                
                                 </td>
                                 -->
                         <#if prodProduct.productType=='FOREIGNLINE' && prodProduct.packageType=='SUPPLIER'>
						 	<#if prodProduct.bizCategory.categoryId == 15 || prodProduct.bizCategory.categoryId == 18>
	                            </tr>
	                            <td class="e_label">材料截止收取时间：</td>  
	                            <td>
	                            <#if lastDate??>${lastDate?string('yyyy-MM-dd') !''}</#if>
	                            </td>
	                            <td>
	                            <a href="javaScript:" id="updateVisaDocLastTime">[修改]</a>
	                            </td>
	                            </tr>
	                         </#if>
	                     </#if>
                            <#if frontBusStop??> 
                             <tr>
                            <td class="e_label">去程上车地点：</td>  
                            <td>${frontBusStop!''}
                            <#--
                            <#if frontBusStop?length gt 5>
                             	${frontBusStop?substring(0,5)}
                             <#else>
                             	${frontBusStop}
                             </#if>-->
                            </td>
                            </tr>
                            </#if> 
                            <#if backBusStop??> 
                             <tr>
                            <td class="e_label">返程上车地点：</td>  
                            <td>${backBusStop!''}
                            <#--
                            <#if frontBusStop?length gt 5>
                             	${frontBusStop?substring(0,5)}
                             <#else>
                             	${frontBusStop}
                             </#if>-->
                            </td>
                            </tr>
                            </#if> 
                            <#if order.categoryId==15 || order.categoryId==16 >
	                            <tr>
	                                <td class="e_label">成团率：</td>
	                                <td><a href="javaScript:" id="showGroupRate" data0="${order.productId}" data1="${order.visitTime?string('yyyy-MM-dd')!''}">[查看]</a></td>
	                            </tr>
	                        </#if>
                            <tr>
                                <td class="e_label">游客姓名：</td>
                                <td><strong>[${travellerNum!'0'}人]</strong>${travellerName!''}</td>
                            </tr>
                           
                            <tr>
                                <td class="e_label">特殊要求：</td>
                                <td>${order.remark!''}</td>
                            </tr>
                            <#-- 金融TODO -->
                            <#if order.categoryId != 33>
                            	<tr>
	                                <td class="e_label">确认方式：</td>
	                                <td>短信确认</td>
	                            </tr>
                            </#if>
                            
                            <tr>
                                <td class="e_label">相关订单：</td>
                                <td>
                                <#if otherOrder==true> 
                                <a href="${rc.contextPath}/ord/order/orderMonitorList.do?userId=${order.userId}&productId=${productId}&orderStatus=NORMAL" target="_blank">[另有订单]</a>
                                </#if> 
                                <a href="${rc.contextPath}/ord/order/orderMonitorList.do?userId=${order.userId}" target="_blank">[同用户订单]</a></td>
                            </tr>
                           <#if performStatus!=""> 
                           <tr>
                                <td class="e_label">使用状态：</td>
                                <td>${performStatus}</td>
                            </tr>
                            </#if> 
                            <#if isContainTicket==true> 
                            <tr>
                                <td class="e_label">供应商备注：</td>
                                <td>
                                    <a id="supplierMemo" href="javascript:void(0)"
                                       param='objectId=${(order.orderId)!''}&objectType=EBK_TICKET_PASS_MEMO&sysName=VST'>[查看明细]</a>
                                </td>
                            </tr>
                            </#if>
							<tr>
								<td class="e_label">公告：</td>
								<td><a id="productNotice" productId="${order.productId!''}" href="javascript:void(0)">[查看公告]</a></td>
                            <tr>
                            <#if mainTourismOrderId??>
                            <tr>
                                <td class="e_label">旅游主单：</td>
                                <td><a href="${rc.contextPath}/order/ordCommon/showOrderDetails.do?orderId=${mainTourismOrderId}" target="_blank">${mainTourismOrderId}</a></td>
                            </tr>
                            </#if>
                            <#if invoiceExpressOrderId?? >
                            <tr>
                            	<td class="e_label">发票快递订单：</td>
								<td><a href="${rc.contextPath}/order/ordCommon/showOrderDetails.do?orderId=${invoiceExpressOrderId}" target="_blank">${invoiceExpressOrderId}</a></td>
                            </tr>
                            </#if>
                            <tr>
                                <#if serviceFeeOrderFlag == "Y">
                                    <td class="e_label">改期服务费：</td>
                                    <td><a id="seeDetails" orderId="${orderId!''}" href="javascript:void(0)">[查看明细]</a></td>
                                </#if>
                            <tr>
                            <#if order.orderSubType=='STAMP'>
                            <tr>
								<td class="e_label">可兑换产品：</td>
								<td>${order.boundProduct}</td>
                            <tr>
                            <tr>
								<td class="e_label">可兑换商品：</td>
								<td><a id="boundGoods"  href="javascript:void(0)">[查看可兑换商品]</a></td>
                            <tr>
                            </#if>
                        </tbody>
                    </table>
                    <#if isDestBuFrontOrder?? && isDestBuFrontOrder==true && orderInvoiceInfoVst??>
		                 <#include "/order/invoice/invoiceInfoBase.ftl"/>
		            </#if> 
                    
           
                </div>
            </div>
           
            <div class="solid_line mt10 mb10"></div>
            
             <div id="findOrdPersonBooker">
               <div class="order_msg clearfix">
	            	正在使劲加载数据中...
	            </div> 
             </div>
             
            
            <div id="childOrderInfo">
                 <#list resultMap?keys as testKey>  
                	<div class="order_msg clearfix">
                	</br>
                	<strong>
                	  子订单--${resultMap[testKey][0].childOrderTypeName!''}
                	  </strong>
                    <#if ordRescheduleStatus?? && ("Y"==ordRescheduleStatus.ordRescheduleFlag || ordRescheduleStatus.ordChangeCount>0 ) && (order.categoryId == 11 || order.categoryId == 12 || order.categoryId == 13)>
                         <#if testKey_index==0 && OrdChangableFlag != "Y">
                        （<a class="btn <#if (ordRescheduleStatus.ordChangeCount>=ordRescheduleStatus.suppChangeCount)>changed<#else>btn_cc1</#if>" name="ordRescheduleDialog">
                             <strong><#if (ordRescheduleStatus.ordChangeCount>0)>已改期${ordRescheduleStatus.ordChangeCount}次<#else>改期</#if></strong></a>）
                         </#if>
                    </#if>
                	</div>
                	<#if showMap[testKey][0]==true>
                	 <table  class="p_table table_center mt20">
		                <thead>
		                    <#if testKey==188>
		                    	<tr>
		                    		<th>客服负责人</th>
		                        	<th>资源审核人</th>
		                        	<th>子订单号</th>
		                       		<th>子订单状态</th>
		                       		<th>类型</th>
		                       		<th>产品名称</th>
		                       		<th>预订份数</th>
			                        <th>销售单价</th>
			                        <th>订单总价</th>
	                                <th>订单实付总金额</th>
	                                <th>订单备注</th>
	                                <th>子订单价格确认状态</th>
		                    	</tr>
		                    <#else>
		                     <tr>
		                        <th>客服负责人</th>
		                        <th>资源审核人</th>
		                        <#if testKey == 11 || testKey == 12 || testKey == 13>
		                        <th>Expired Refund 💲</th>
		                        </#if>
		                        <th>子订单号</th>
		                        <th>子订单状态</th>
		                      <#--
		                        <th>产品ID</th>
		                        <th>商品ID</th>
		                        <th>商品名称</th>TODO
		                      -->
		                        <#if resultMap[testKey][0].childOrderType=='category_single_ticket'
                                            || resultMap[testKey][0].childOrderType=='category_other_ticket'
                                            || resultMap[testKey][0].childOrderType=='category_connects'
											|| resultMap[testKey][0].childOrderType=='category_show_ticket'>
		                        	<th>是否二维码对接</th>
		                        	<th>是否支持废码</th>
		                        <#else>
		                        	<#if testKey == 33>
		                        	
		                        	<#else>
		                        		<th>是否对接</th>
		                        	</#if>
		                        </#if>
		                        
		                        <#if resultMap[testKey][0].childOrderType=='category_single_ticket'
                                            || resultMap[testKey][0].childOrderType=='category_other_ticket'
                                            || resultMap[testKey][0].childOrderType=='category_comb_ticket'
                                            || resultMap[testKey][0].childOrderType=='category_connects'
											|| resultMap[testKey][0].childOrderType=='category_show_ticket'>
		                        	<th>是否是EBK订单能及时处理通关</th>
		                        </#if>
		                      	<#--
		                        <th>是否对接</th>
		                        -->
		                        
		                        <#if testKey == 33>
		                        	<th>权益发放状态</th>
	                        		<th>赠送权益金</th>
	                        		<th>类型</th>
	                        		<th>商品名称</th>
	                        		<th>预定份数</th>
	                        		<th>销售单价</th>
	                        		<th>订单实付总金额</th>
	                        		<th>子订单价格确认状态</th>
								<#else>
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
	                                  <th>
	                                    使用时间
	                                  </th>
	                                   <th>
	                                      当地酒店地址
	                                  </th>
	                                </#if>
	                                <#if testKey==43>
	                                  <th>
	                                    使用时间
	                                   </th>
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
								</#if>
		                    </tr>
		                    </#if>
		                   
		                </thead>
		                <tbody>
			                <#list resultMap[testKey]  as orderMonitorRst> 
			                  <#if testKey == 33>
			                	<tr>
			                        <td>
			                       		${orderMonitorRst.principal!''}
			                        </td>
			                        <td>
			                       		${orderMonitorRst.resourceApprover!''}
			                        </td>
			                        <td>
			                         <a href="/vst_order/order/orderManage/showChildOrderStatusManage.do?orderItemId=${orderMonitorRst.orderId!''}&orderType=child&orderId=${order.orderId}" target="_blank">${orderMonitorRst.orderId!''}</a>
									</td>
									<td>${orderMonitorRst.currentStatus!''}</td>
									
									<td>
										 <#if orderMonitorRst.rightStatus=='SUCCESS'>
										 	成功
										 </#if>
										 <#if orderMonitorRst.rightStatus=='FAIL'>
										 	失败
										 </#if>
										 <#if orderMonitorRst.rightStatus=='PROGRESSING'>
										 	处理中
										 </#if>
										 <#if orderMonitorRst.rightStatus=='CREATE'>
										 	新建
										 </#if>
									</td>
									<td>${orderMonitorRst.rightPrice}</td>
									
									<td> ${orderMonitorRst.childOrderTypeName!''}</td>
									<td> ${orderMonitorRst.productName!''}</td>
									<td> ${orderMonitorRst.buyItemCount!''}</td>
									<td> ${orderMonitorRst.buyItemPrice!''}</td>
		                            <td>${orderMonitorRst.buyItemTotalPrice!''}</td>
		                            <td ><#if orderMonitorRst.priceConfirmStatus?? && orderMonitorRst.priceConfirmStatus=='UN_CONFIRMED'>价格待确认<#else>价格已确认</#if></td>
			                    </tr>
			                
			                  <#elseif testKey == 188>
			                  	<tr>
			                  	    <!-- 超级会员 start -->
			                  		<td>${orderMonitorRst.principal!''}</td>
			                  		<td>${orderMonitorRst.resourceApprover!''}</td>
			                  		<td>
			                  			<a href="/vst_order/order/orderManage/showChildOrderStatusManage.do?orderItemId=${orderMonitorRst.orderId!''}&orderType=child&orderId=${order.orderId}" target="_blank">${orderMonitorRst.orderId!''}</a>
			                  		</td>
			                  		<td>${orderMonitorRst.currentStatus!''}</td>
			                  		<td>${orderMonitorRst.childOrderTypeName!''}</td>
			                  		<td>${orderMonitorRst.productName!''}</td>
			                  		<td>${orderMonitorRst.buyItemCount!''}</td>
			                  		<td>${orderMonitorRst.buyItemPrice!''}</td>
		                          	<td>${orderMonitorRst.buyItemTotalPrice!''}</td>
			                  		<td>
			                  			<#if !(orderMonitorRst.actualPaidAmountList)?? || (orderMonitorRst.actualPaidAmountList)?size = 0>
                                           0
                                       </#if>
                                       <#list orderMonitorRst.actualPaidAmountList as actualPaidPriceTypeVO>
                                       		${actualPaidPriceTypeVO.declaration!''}
                                       		<#if actualPaidPriceTypeVO.declaration?? && actualPaidPriceTypeVO.declaration != "">:</#if>
                                       		${actualPaidPriceTypeVO.price/100}
                                       		<br/>
                                       </#list>
			                  		</td>
			                  		<td>${orderMonitorRst.orderItemMemo!''}</td>
			                  		<td><#if orderMonitorRst.priceConfirmStatus?? && orderMonitorRst.priceConfirmStatus=='UN_CONFIRMED'>价格待确认<#else>价格已确认</#if></td>
			                  		<!-- 超级会员 end -->
			                  	</tr>
			                  <#else>
			                  	 <#if orderMonitorRst.specialTicketType != 'DISNEY_SHOW' && orderMonitorRst.specialTicketType != 'SHOW_TICKET'>
			               		<tr>
			                        <td>
			                       ${orderMonitorRst.principal!''}
			                        </td>
			                        <td>
			                       ${orderMonitorRst.resourceApprover!''}
			                        </td>
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
			                      <#--  
			                        <a href="/vst_order/order/ordCommon/showChildOrderDetails.do?orderItemId=${orderMonitorRst.orderId!''}&orderId=${order.orderId}" target="_blank">${orderMonitorRst.orderId!''}</a>
			                         -->
			                         <a href="/vst_order/order/orderManage/showChildOrderStatusManage.do?orderItemId=${orderMonitorRst.orderId!''}&orderType=child&orderId=${order.orderId}" target="_blank">${orderMonitorRst.orderId!''}</a>
						
									</td>
									<td>${orderMonitorRst.currentStatus!''}</td>
									<#--
			                        <td>${orderMonitorRst.productId!''}</td>
			                        <td>${orderMonitorRst.suppGoodsId!''}</td>
			                        <td>${orderMonitorRst.suppGoodsName!''}</td>
			                        -->
			                        <td>
			                            <#if orderMonitorRst.childOrderType == 'category_single_ticket'
                                                ||  orderMonitorRst.childOrderType == 'category_other_ticket'
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
									
									<#--<#elseif orderMonitorRst.childOrderType == 'category_single_ticket' || orderMonitorRst.childOrderType == 'category_other_ticket'||resultMap[testKey][0].childOrderType=='category_show_ticket' ||orderMonitorRst.childOrderType == 'category_comb_ticket'>
			                      	-->
			                      	
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
                                       <#list orderMonitorRst.payProAmountList as payProPriceTypeVO>
                                       ${payProPriceTypeVO.declaration!''}<#if payProPriceTypeVO.declaration?? && payProPriceTypeVO.declaration != "">:</#if>${payProPriceTypeVO.price/100}<br/>
                                       </#list>
                                   </td>-->
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
                                            <#if testKey == 11 || testKey == 12 || testKey == 13>
                                                <a class="btn <#if currentItemChangable>btn_cc1<#else>changed</#if>" name="changeDate">改期</a>
                                            </#if>
										</td>
									</#if>
			                    </tr>
			                  	</#if>
			                  </#if>
			                </#list>
	                
		                 </tbody>
	            		 </table>
	                </#if>
	                <!--迪斯尼演出票、玩乐演出票显示 -->
	                <#if showMap[testKey][1]==true>
                	 <table  class="p_table table_center mt20">
		                <thead>
		                    <tr>
		                        <th>客服负责人</th>
		                        <th>资源审核人</th>
		                        <th>子订单号</th>
		                        <th>子订单状态</th>
		                      <#--
		                        <th>产品ID</th>
		                        <th>商品ID</th>
		                        <th>商品名称</th>
		                      -->
		                        <#if resultMap[testKey][0].childOrderType=='category_single_ticket'||resultMap[testKey][0].childOrderType=='category_other_ticket'||resultMap[testKey][0].childOrderType=='category_show_ticket'>
		                        	<th>是否二维码对接</th>
		                        	<th>是否支持废码</th>
		                        <#else>
		                        	<th>是否对接</th>
		                        </#if>
		                        
		                        <#if resultMap[testKey][0].childOrderType=='category_single_ticket'||resultMap[testKey][0].childOrderType=='category_other_ticket'||resultMap[testKey][0].childOrderType=='category_show_ticket'||resultMap[testKey][0].childOrderType=='category_comb_ticket'>
		                        	<th>是否是EBK订单能及时处理通关</th>
		                        </#if>
		                      	<#--
		                        <th>是否对接</th>
		                        -->
		                        
		                        <th>类型</th>
		                        <#if resultMap[testKey][0].productType=="WIFI" >
		                        <th>产品类别</th>
		                        <th>取件类型</th>
		                        <#elseif resultMap[testKey][0].productType=="PHONE"||resultMap[testKey][0].productType=="DEPOSIT" || resultMap[testKey][0].productType=="EXPRESS">
		                        <th>产品类别</th>
		                        </#if>
		                        <th>包含商品</th>
		                        <th>演出时间</th>
		                        <th>区域详情</th>
		                        <th>座位信息</th>
		                        <th>预订份数</th>
		                        <th>销售单价</th>
		                        <th>订单总价</th>
                                <th>订单实付总金额</th>
                               <!--<th>支付立减分摊总金额</th>-->
		                        <th>出游日期</th>
		                        <#if resultMap[testKey][0].productType=="WIFI" ||resultMap[testKey][0].productType=="DEPOSIT">
		                        <th>出游截止日期</th>
		                        </#if>
		                        <th>订单备注</th>
		                    </tr>
		                </thead>
		                <tbody>
			                <#list resultMap[testKey]  as orderMonitorRst> 
			                  <#if orderMonitorRst.specialTicketType == 'DISNEY_SHOW' || orderMonitorRst.specialTicketType == 'SHOW_TICKET'>
			               		<tr>
			                        <td>
			                       ${orderMonitorRst.principal!''}
			                        </td>
			                        <td>
			                       ${orderMonitorRst.resourceApprover!''}
			                        </td>
			                        <td>
			                      <#--  
			                        <a href="/vst_order/order/ordCommon/showChildOrderDetails.do?orderItemId=${orderMonitorRst.orderId!''}&orderId=${order.orderId}" target="_blank">${orderMonitorRst.orderId!''}</a>
			                         -->
			                         <a href="/vst_order/order/orderManage/showChildOrderStatusManage.do?orderItemId=${orderMonitorRst.orderId!''}&orderType=child" target="_blank">${orderMonitorRst.orderId!''}</a>
						
									</td>
									<td>${orderMonitorRst.currentStatus!''}</td>
									<#--
			                        <td>${orderMonitorRst.productId!''}</td>
			                        <td>${orderMonitorRst.suppGoodsId!''}</td>
			                        <td>${orderMonitorRst.suppGoodsName!''}</td>
			                        -->
			                        <td>
			                            <#if orderMonitorRst.childOrderType == 'category_single_ticket'||orderMonitorRst.childOrderType == 'category_other_ticket'||resultMap[testKey][0].childOrderType=='category_show_ticket'>
			                            	<#if orderMonitorRst.apiFlag=="Y">是<#else>否</#if>
			                            <#else>
			                            	<#if orderMonitorRst.apiFlag=="Y">对接<#else>非对接</#if>
			                            </#if>
			                    	</td>
			                    	
			                    	<#if resultMap[testKey][0].childOrderType=='category_single_ticket'||resultMap[testKey][0].childOrderType=='category_other_ticket'||resultMap[testKey][0].childOrderType=='category_show_ticket'>
			                    	 	<td>
			                    	 		<#if orderMonitorRst.apiFlag=="Y">
			                    	 			<#if orderMonitorRst.isSupportDestroyCode=="1">支持废码<#else>不支持废码</#if>
			                    	 		<#else>
			                    	 		</#if>
			                    	 	</td>
			                    	</#if>
			                    	<#if resultMap[testKey][0].childOrderType=='category_single_ticket'||resultMap[testKey][0].childOrderType=='category_other_ticket'||resultMap[testKey][0].childOrderType=='category_show_ticket'||resultMap[testKey][0].childOrderType=='category_comb_ticket'>
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
|		                                    ${orderMonitorRst.childOrderTypeName!''}
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
			                       ${orderMonitorRst.productName!''}
			                  	   </td>
			                       <td> 
			                       ${orderMonitorRst.showTime!''}
			                  	   </td>
			                       <td>
			                       ${orderMonitorRst.sectionDetail!''}
			                  	   </td>
                                    <td>
                                    ${orderMonitorRst.seatsDetail!'--'}
                                    </td>
			                       <td> 
			                      
			                        <#if orderMonitorRst.childOrderType == 'category_cruise'> 
			                      	 ${orderMonitorRst.personCount!''} 人/ ${orderMonitorRst.buyCount!''} 间
									<#else>
									 ${orderMonitorRst.buyItemCount!''}
									</#if>
									
									<#--<#elseif orderMonitorRst.childOrderType == 'category_single_ticket' || orderMonitorRst.childOrderType == 'category_other_ticket' ||orderMonitorRst.childOrderType == 'category_comb_ticket'>
			                      	-->
			                      	
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
                                       <#list orderMonitorRst.payProAmountList as payProPriceTypeVO>
                                       ${payProPriceTypeVO.declaration!''}<#if payProPriceTypeVO.declaration?? && payProPriceTypeVO.declaration != "">:</#if>${payProPriceTypeVO.price/100}元<br/>
                                       </#list>
                                   </td>-->
			                       <td> ${orderMonitorRst.visitTime!''} </td> 
			                       <#if resultMap[testKey][0].productType=="WIFI" ||resultMap[testKey][0].productType=="DEPOSIT">
			                       <td>${wifiAddition.endDay}</td>
			                       </#if>
			                       <td> ${orderMonitorRst.orderItemMemo!''}<#if orderMonitorRst.orderAttachmentNumber?? && orderMonitorRst.orderAttachmentNumber &gt; 0>(<a class="viewChildOrderAttachment" href="javaScript:">查看附件</a>)</#if></td>
			                    </tr>
			                  </#if>
			                </#list>
		                 </tbody>
	            		 </table>
	                </#if>
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
	             
	            <#--自驾游儿童价 -->
				<#if selfDrivingChild?? && selfDrivingChild=="Y">
					<div class="order_msg clearfix">
                	  </br>
                	  <strong>
                	            子订单--自驾游儿童价
                	  </strong>
                	</div>
					<table class="p_table table_center mt20">
				 	<thead>
				      <th>包含商品</th>	
				      <th>预订份数</th>
				      <th>销售单价</th>
				      <th>订单总价</th>
				      <th>出游日期</th>
				      <th>订单备注</th>				      
				 	</thead>
				 	<tr>
				        <td>自驾游儿童价</td>	
				 		<td>${selfDrivingChildCount}</td>
				 		<td>${(selfDrivingChildAmount?number/100)?string('#0.00')} 元</td>
				 		<td>${(selfDrivingChildTotalAmount?number/100)?string('#0.00')} 元</td>
				 		<td>${selfDrivingChildVisitTime}</td>
				 		<td></td>
				 	</tr>
				    </table>
				</#if>
              </div>
              <#if order.orderSubType!='STAMP'>
              <div id="findBuyPresentList">
               <div class="order_msg clearfix">
	            	正在使劲加载数据中...
	            </div> 
             </div>

             
             <#if order.travellerDelayFlag=='Y'>
             <div id="findOrdOrderTravellerConfirm">
             	<div class="order_msg clearfix">
	            	正在使劲加载数据中...
	            </div> 
             </div>
             </#if>

            <#if hasOrderServiceProp?? && hasOrderServiceProp>
            <#if hasAccess?? && hasAccess>
             <div id="findOrderConnectsServiceInfo">
                    <div id="logResultList" class="divClass">
                        <div class="order_msg clearfix">
                            </br>
                            <strong>服务信息: 交通接驳-接机</strong>
                        </div>
                        <table class="p_table table_center mt20">
                            <thead>
                            <tr>
                                <th>接机航班</th>
                                <th>接机时间</th>
                                <th>送达目的地名称</th>
                                <th>送达目的地地址</th>
                                <th>用车人数</th>
                                <th>行李件数</th>
                                <th>其它备注</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>${(accessServicePropMap.flight_number)!''}</td>
                                <td>${accessServicePropMap.access_air_time!''}</td>
                                <td>${(accessServicePropMap.dest_name)!''}</td>
                                <td>${(accessServicePropMap.dest_address)!''}</td>
                                <td>${(accessServicePropMap.car_using_person_number)!''}</td>
                                <td>${(accessServicePropMap.baggage_number)!''}</td>
                                <td>${(accessServicePropMap.other_remark)!''}</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </#if>
            <#if hasGive?? && hasGive>
            	<div id="findOrderConnectsServiceInfo">
                    <div id="logResultList" class="divClass">
                        <div class="order_msg clearfix">
                            </br>
                            <strong>服务信息: 交通接驳-送机</strong>
                        </div>
                        <table class="p_table table_center mt20">
                            <thead>
                            <tr>
                                <th>送机航班</th>
                                <th>起飞时间</th>
                                <th>用车时间</th>
                                <th>出发地名称</th>
                                <th>出发地地地址</th>
                                <th>用车人数</th>
                                <th>行李件数</th>
                                <th>其它备注</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>${(giveServicePropMap.flight_number)!''}</td>
                                <td>${(giveServicePropMap.flight_start_time)!''}</td>
                                <td>${(giveServicePropMap.give_air_car_use_time)!''}</td>
                                <td>${(giveServicePropMap.starting_name)!''}</td>
                                <td>${(giveServicePropMap.starting_address)!''}</td>
                                <td>${(giveServicePropMap.car_using_person_number)!''}</td>
                                <td>${(giveServicePropMap.baggage_number)!''}</td>
                                <td>${(giveServicePropMap.other_remark)!''}</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </#if>
             <#if hasRent?? && hasRent>
                <div id="findOrderConnectsServiceInfo">
                    <div id="logResultList" class="divClass">
                        <div class="order_msg clearfix">
                            </br>
                            <strong>服务信息: 交通接驳-包车</strong>
                        </div>
                        <table class="p_table table_center mt20">
                            <thead>
                            <tr>
                                <th>预计用车开始时间</th>
                                <th>出发地名称</th>
                                <th>出发地地址</th>
                                <th>到达地名称</th>
                                <th>到达地地址</th>
                                <th>用车人数</th>
                                <th>行李件数</th>
                                <th>其它备注</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>${(rentServicePropMap.car_using_time)!''}</td>
                                <td>${(rentServicePropMap.starting_name)!''}</td>
                                <td>${(rentServicePropMap.starting_address)!''}</td>
                                <td>${(rentServicePropMap.arrival_name)!''}</td>
                                <td>${(rentServicePropMap.arrival_address)!''}</td>
                                <td>${(rentServicePropMap.car_using_person_number)!''}</td>
                                <td>${(rentServicePropMap.baggage_number)!''}</td>
                                <td>${(rentServicePropMap.other_remark)!''}</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </#if>
            </#if>

             <div id="findTravellerPersonList">
               <div class="order_msg clearfix">
	            	正在使劲加载数据中...
	            </div> 
             </div>

             <div id="findGuideInfo">
                      <div class="order_msg clearfix">
                          正在使劲加载数据中...
                      </div>
             </div>

             <div id="findGoodsPersonList">
	                    <div class="order_msg clearfix">
		            	正在使劲加载数据中...
		            	</div>
            </div> 	
            
             <div id="findEmergencyPersonList">
	                    <div class="order_msg clearfix">
		            	正在使劲加载数据中...
		            	</div> 
            </div> 	
            
            <div id="findExpressAddressList">
	                    <div class="order_msg clearfix">
		            	正在使劲加载数据中...
		            	</div> 
            </div>


              <div id="findExpressOrderList">
                  <div class="order_msg clearfix">
                      正在使劲加载数据中...
                  </div>
              </div>



            </#if>
            <#if order.orderSubType=='STAMP'>
            <div id="findPreSaleList">
	                    <div class="order_msg clearfix">
		            	正在使劲加载数据中...
		            	</div> 
            </div>
            </#if>	 	
            
            
    </div>
    
    
   
    </form>
    <div id="logResult" style="display:none;margin-left:40px;margin-right:35px;">
     
     </div>
     <input type="hidden" value="${order.travellerDelayFlag}" id="travellerDelayFlag"/>
    <#include "/order/orderProductQuery/member_dialog.ftl">
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
    	//产品重要信息提示
    	var productInfoDialog;
		$(".productInfo").bind("click",function(){
			var orderItemId = $(this).attr('data-orderItemId');
			productInfoDialog = new xDialog("/vst_order/order/orderManage/showOrderProductDetail.do?orderId=" + ${order.orderId!''} + "&orderItemId=" + orderItemId,{},{title:"重要提示",width:1000,iframe:true});
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
		//分销商信息
        var showDistributorDialog;
        function showDistributor(distributionChannel){
            var distributionChannelVal="${order.distributionChannel!''}";
            var param="";
            if(distributionChannelVal!=""){
                 param="?distributionChannel="+distributionChannelVal;
                 showDistributorDialog = new xDialog("/vst_order/order/orderManage/showDistributorInfo.do"+param,{},{title:"订单备注",width:600,height:450,scrolling:"yes",iframe:true});
            }
           
        }    
        var  updateWaitPaymentTimeDialog;
        $("#updateWaitPaymentTime").bind("click",function(){
		
			var paymentTime= "${waitPaymentTime!''}";
			if(paymentTime!="")
			{
				var waitPaymentTime="${waitPaymentTime}";
         		updateWaitPaymentTimeDialog = new xDialog("/vst_order/order/orderManage/showUpdateWaitPaymentTime.do",{"orderId":"${order.orderId!''}","waitPaymentTime":waitPaymentTime},{title:"修改支付等待时间",width:620});
        
			}else{
				alert('支付等待时间为空,不可修改');
			}
		});
		var updateWaitRetainageTimeDialog;
        $("#updateWaitRetainageTime").bind("click",function(){
		
			var paymentTime= "${waitRetainageTime!''}";
			if(paymentTime!="")
			{
				var waitRetainageTime="${waitRetainageTime}";
         		updateWaitRetainageTimeDialog = new xDialog("/vst_order/order/orderManage/showUpdateWaitRetainageTime.do",{"orderId":"${order.orderId!''}","stampId":"${order.stampId!''}","waitRetainageTime":waitRetainageTime},{title:"修改尾款支付等待时间",width:620});
        
			}else{
				alert('支付等待时间为空,不可修改');
			}
		});
		var  updateVisaDocLastTimeDialog;
        $("#updateVisaDocLastTime").bind("click",function(){
		
			var visaDocLastTime= "<#if lastDate??>${lastDate?string('yyyy-MM-dd') !''}</#if>";
         	updateVisaDocLastTimeDialog = new xDialog("/vst_order/order/orderManage/showUpdateVisaDocLastTime.do",{"orderId":"${order.orderId!''}","visaDocLastTime":visaDocLastTime},{title:"修改签证材料截止收取时间",width:620});
		});
         
       //主订单优惠券明细 
	  $("#favorUsageDetail").bind("click",function(){
		new xDialog("/vst_order/order/orderManage/showOrderFavorUsageDetails.do",{"orderId":${order.orderId!''}},{title:"主订单优惠券明细",width:1200});
      });  
         
      //主订单促销明细 
	  $("#promotionDetail").bind("click",function(){
		new xDialog("/vst_order/order/orderManage/showOrderPromotionDetails.do",{"orderId":${order.orderId!''}},{title:"主订单促销明细",width:600});
      });
      
      //订单金额价格修改
      $("#orderAmountChangeDetail").bind("click",function(){
		new xDialog("/vst_order/order/orderManage/showAmountChangeQueryList.do?",{"orderId":${order.orderId!''},'objectType':'','approveStatus':'APPROVE_PASSED'},{title:"订单价格修改",width:600});
      });
      
       //预售券抵扣总金额
      $("#stampDeductionCountAmount").bind("click",function(){
		new xDialog("/vst_order/order/orderManage/showstampDeductionList.do?",{"orderId":${order.orderId!''}},{title:"查看预售劵抵扣明细",width:800});
      });
      
      //供应商备注(门票)
      $("#supplierMemo").bind("click",function(){
		new xDialog("/vst_order/order/orderManage/showSupplierMemo.do",{"orderId":${order.orderId!''}},{title:"备注",width:650});
		//var param = $(this).attr("param");
        //new xDialog("/lvmm_log/bizLog/showVersatileLogList?"+param,{},{title:"备注",iframe:true,width:1000,hight:300,iframeHeight:680,scrolling:"yes"});
      });
       //可兑换商品
      $("#boundGoods").bind("click",function(){
		new xDialog("/vst_order/order/orderManage/boundGoods.do",{"stampId":"${order.stampId!''}"},{title:"可兑换商品",width:650});
      });
      //公告
      $("#productNotice").bind("click",function(){
		//new xDialog("/vst_order/order/orderManage/showProductNotice.do",{"productId":${order.productId!''},"orderId":${order.orderId!''}},{title:"公告",width:650});
		new xDialog("/vst_back/prod/productNotice/findProductNoticeList.do",{"productId":${order.productId!''},"noticeType":"PRODUCT_All","from":"order"},{title:"公告",width:650});
      });  
      //查看明细
      $("#seeDetails").bind("click",function(){
          new xDialog("/vst_order/order/orderManage/showServiceFeeDetails.do",{"orderId":${order.orderId!''}},{title:"改期服务费记录",width:650});
      });

      //O2O门店优惠
	  $("#favourableO2oAmount").bind("click",function(){
		new xDialog("/vst_order/order/orderManage/showOrderFavorO2oDetails.do",{"orderId":${order.orderId!''}},{title:"O2O门店优惠明细",width:650});
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
			
				var param="?orderId=${order.orderId!''}&orderType=parent";
			 // messageDialog = new xDialog("/vst_order/order/orderManage/findComMessageList.do",{"orderId":${order.orderId!''},"orderType":"parent"},{},{title:"查看预订通知",iframe:true,width:"1200"});
			  messageDialog = new xDialog("/vst_order/order/orderManage/findComMessageList.do"+param,{},{title:"查看预订通知",iframe:true,width:"1200"});
		     }
			
      		if(${compliantCallsCount}>0){
        		var title = "用户 " + "${ordPersonBooker.fullName}" + " 的未处理信息";
      			var userName = encodeURIComponent(encodeURIComponent("${ordPersonBooker.fullName}"));
        		var entryPeople = encodeURIComponent(encodeURIComponent("${loginUserId}"));
        		var param = "orderId=${order.orderId}&refreshOnClose=Y&userName=" + userName + "&entryPeople=" + entryPeople;
        		showUntreatedComplaintCallDialog = new xDialog("http://super.lvmama.com/sales_front/complaintCall/orderDetail/showComplaintCallByCustomer.do?" + param,{},{title:title,width:1200,height:750,iframe:true});
      		}
      		
     		var presentData = "orderId=${order.orderId!''}";
         	$.post("/vst_order/order/orderManage/findBuyPresentList.do",
			   presentData,
			   function(result) {
			   
			 	$("#findBuyPresentList").html(result);
     		});
     
     
     		var ordPersonBookerData="orderType=parent&orderId=${order.orderId}";
         	$.post("/vst_order/order/orderManage/findOrdPersonBooker.do",
			   ordPersonBookerData,
			   function(result){
			   
			 	$("#findOrdPersonBooker").html(result);
      		});

     		var touristData="orderType=parent&orderId="+${order.orderId!''};
         	$.post("/vst_order/order/orderManage/findTravellerPersonList.do",
			   touristData,
			   function(result){
			 	$("#findTravellerPersonList").html(result);
      		});
            //daoyou
            $.post("/vst_order/order/orderManage/findGuideInfo.do",
                    touristData,
                    function(result){
                        $("#findGuideInfo").html(result);
                    });
            //游玩人须知
         	
         	 var needConfirm =$("#travellerDelayFlag").val();
         	 if(needConfirm=='Y'){
         		var ordOrderTravellerConfirmData="orderId="+${order.orderId!''};
             	$.post("/vst_order/order/orderManage/findOrdOrderTravellerConfirm.do",
         			   ordOrderTravellerConfirmData,
         			   function(result){
         			 	$("#findOrdOrderTravellerConfirm").html(result);
              	});	
         	} 
         	
      		
      		
      		var goodsPersonData="orderId=${order.orderId!''}";
         	$.post("/vst_order/order/orderManage/findGoodsPersonList.do",
			   goodsPersonData,
			   function(result){
			   
			 	$("#findGoodsPersonList").html(result);
      		});
      		
      		var emergencyPersonData="orderType=parent&orderId=${order.orderId!''}";
         	$.post("/vst_order/order/orderManage/findEmergencyPersonList.do",
			   emergencyPersonData,
			   function(result){
			 	$("#findEmergencyPersonList").html(result);
      		});
      		var expressAddressData="orderType=parent&orderId="+${order.orderId!''};
         	$.post("/vst_order/order/orderManage/findExpressAddressList.do",
			   expressAddressData,
			   function(result){
			   
			 	$("#findExpressAddressList").html(result);
      		});


            var expressOrderData="orderType=parent&orderId="+${order.orderId!''};
            $.post("/vst_order/order/orderManage/findExpressOrderList.do",
                    expressOrderData,
                    function(result){

                        $("#findExpressOrderList").html(result);
            });


      		<#if order.orderSubType=='STAMP'>
      		var preSaleAddressData="orderType=parent&orderId="+${order.orderId!''};
         	$.post("/vst_order/order/orderManage/findPreSaleList.do",
			   preSaleAddressData,
			   function(result){
			   
			 	$("#findPreSaleList").html(result);
      		});
      		</#if>
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
         
		var addMessageDialog;
		$("#addMessage").bind("click",function(){

			var orderRemark=$.trim($("#orderRemark").val());
			var param={"orderId":${order.orderId!''},"orderRemark":orderRemark,"orderType":"parent"};
			addMessageDialog = new xDialog("/vst_order/order/orderManage/showAddMessage.do",param,{title:"创建预订通知",width:800});
         
         });
         var addMessageShorCutDialog;
         $("#addMessageShorCut").bind("click",function(){

			var orderRemark=$.trim($("#orderRemark").val());
			var param={"orderId":${order.orderId!''},"orderRemark":orderRemark,"orderType":"parent"};
			addMessageShorCutDialog = new xDialog("/vst_order/order/orderManage/showAddMessage.do",param,{title:"创建预订通知",width:800});
         
         });
         
     
     	var findOrderVisaApprovalQueryListDialog;
         $("#findOrderVisaApprovalQueryList").bind("click",function(){

			//var param={"searchOrderId":${order.orderId!''}};
			//findOrderVisaApprovalQueryListDialog = new xDialog("/vst_back/visa/approval/showOrderVisaApprovalQueryList.do?searchOrderId=${order.orderId}","",{title:"查看签证审核",iframe:true,width:900});
			findOrderVisaApprovalQueryListDialog = new xDialog("/visa_prod/visa/approval/showOrderVisaApprovalQueryList.do?searchOrderId=${order.orderId}",{},{title:"查看签证审核",width:900});
         
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
                $.get("/vst_order/order/depositRefund/checkShowProcessAudit/TRANSFER/${prodProduct.managerId}/${order.orderId}.do" , function (data) {
                   if(data.code == 200){
                       //加载审批页
                       showProcessTransferDialog = new xDialog("/vst_order/order/depositRefund/showProcessAudit/TRANSFER/${order.orderId}.do",{},{title:"资金转移申请处理",width:1200});
                   }
                });
            }

            //定金核损申请审批
            if ($("#showLosses").html() != null) {
                $.get("/vst_order/order/depositRefund/checkShowProcessAudit/LOSSES/${prodProduct.managerId}/${order.orderId}.do" , function (data) {
                    if(data.code == 200){
                        //加载审批页
                        showProcessLossesDialog = new xDialog("/vst_order/order/depositRefund/showProcessAudit/LOSSES/${order.orderId}.do",{},{title:"定金核损申请处理",width:1200});
                    }
                });
            }

        });


         /**
         var findInsurancePolicyListDialog;
          $("#findInsurancePolicyList").bind("click",function(){
			//var param={"searchOrderId":${order.orderId!''}};
			//findInsurancePolicyListDialog = new xDialog("/vst_insurant/insurancePolicy/findList.do?orderId=${order.orderId}",{},{title:"查看投保状态",iframe:true,width:"1200",height:"20000"});
         	findInsurancePolicyListDialog = new xDialog("/vst_insurant/insurancePolicy/findList.do?orderId=${order.orderId}",{},{title:"查看投保状态",width:1200});
         });
           */
          var showLogDialog;
         $("#showLog").bind("click",function(){

			var param="objectType=ORD_ORDER_ORDER&objectId="+${order.orderId}+"&sysName=VST";
           showLogDialog =new xDialog("/lvmm_log/bizLog/showVersatileLogList?"+param,{},{title:"查看日志",iframe:true,width:1000,hight:300,iframeHeight:680,scrolling:"yes"});
         });
         
         $("#showSoundRec").bind("click",function(){

			var data="objectId="+${order.orderId}+"&page="+1;
			showLogDialog = new xDialog("/vst_order/order/orderManage/showSoundRecList.do",data,{title:"查看录音",width:1200});
         
         });         
		 var selectRightDialog;
		 $("#selectRight").bind("click",function(){
			selectRightDialog = new xDialog("/vst_order/order/orderManage/findRightList.do",{"orderId":${order.orderId!''},"orderItemId":''},{title:"查看权益",width:1300});
		 }); 	
			
		$(function(){
         	//关联callid与orderid
         	var callId = "${callid}";
         	var orderId = ${order.orderId};
         	
			if (callId == "undefined" || callId.length == 0 || callId == "null"){
				return;
			}
         	
         	var options = {
					url:"/vst_order/order/orderManage/saveCallIdAndOrderId.do",
					type:"post",
					data:{
					    "callId":callId,
						"objectId":orderId
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
			 $('html, body, .content').animate({scrollTop: $("#childOrderInfo").height()}, 400);
			 
         
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
        
        
		 
		 var faxDialog,sendOderFaxDialog;
        $("#sendOderFax").bind("click",function(){
        	var orderRemark=$.trim($("#orderRemark").val());
			sendOderFaxDialog = new xDialog("/vst_order/order/orderManage/showManualSendOrderFax.do",{"orderId":${order.orderId!''},"orderRemark":orderRemark,"source":"noInfoPass"},{title:"发送凭证",width:600});
		});    
		var findEbkFaxListDialog;
		$("#findEbkFaxList").bind("click",function(){
			findEbkFaxListDialog = new xDialog("/vst_certif/ebooking/task/findEbkAndFaxList.do",{"orderId":${order.orderId!''}},{title:"凭证查询",width:1300});
		});  
		
		
		
         
         //检查是否数字
		function isNum(a)
		{
		    var reg = /^d+(.d+)?$/;
		    reg.test(a);
		}
              
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

		var changeDateDialog;
		$('[name=changeDate]').bind('click', function() {
			if ($(this).hasClass("changed")) {
				pandora.dialog({title:"改期",height: 300, width:600, content:"不能再改期"});
				return;
			}

            var param={"orderId":${order.orderId!''}, "orderItemId": $(this).siblings('input').val()};
			changeDateDialog = new xDialog("/vst_order/order/orderManage/showChangeVisitDate.do",param,{title:"改期",height: 300, width:600, zIndex: 2000});
		});

        var ordRescheduleDialog;
        $('[name=ordRescheduleDialog]').bind('click', function() {
            var param={"orderId":${order.orderId!''}};
            ordRescheduleDialog = new xDialog("/vst_order/order/orderManage/showOrdReschedule.do",param,{title:"改期",height: 400, width:800, zIndex: 2000});
        });
        <#--(function(){-->
            <#--var ordRescheduleTitle = '${ordRescheduleStatus.ordRescheduleTitle!''}';-->
            <#--if(ordRescheduleTitle && ordRescheduleTitle!=''){-->
                <#--$("a[name='ordRescheduleDialog']").attr("title",ordRescheduleTitle);-->
            <#--}-->
        <#--})();-->
        
        
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
       			if( radioVal=="pretrialAudit" ||  radioVal=="INFOPASS" || radioVal=="RESOURCEPASS" || radioVal=="certificateStatus"
                        || radioVal=="paymentAudit" || radioVal=="cancelStatusConfim" || radioVal=="onlineRefundConfirm")
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
       		}else if("pretrialAudit"==radioValue){
       			operation="pretrialAudit";
       		
       		}else if("INFOPASS"==radioValue){
       			operation="infoStatus";
       			var param={"orderId":${order.orderId!''},"infoStatus":"${order.infoStatus!''}","orderRemark":orderRemark,"operation":operation};
       			
       			faxDialog = new xDialog("/vst_order/order/orderManage/showSendOrderFax.do",param,{title:"发送凭证",width:600});
       			return;
       		}else if("RESOURCEPASS"==radioValue){
       			operation="resourceStatus";
       		
       		}else if("certificateStatus"==radioValue){
       			operation="certificateStatus";
       			
       			/**
				var param={"orderId":"${order.orderId!''}","orderRemark":orderRemark,"operation":operation,"isSupplierOrder":"${isSupplierOrder!''}"};
       			
       			certificateDialog = new xDialog("/vst_order/order/orderManage/showAddCertificate.do",param,{title:"凭证确认",width:600});
				return;
       		*/
       		
       			
       		}else if("paymentAudit"==radioValue){
       			operation="paymentAudit";
       		
       		}else if("timePaymentAudit"==radioValue){
       			operation="timePaymentAudit";
       		
       		}else if("cancelStatusConfim"==radioValue){
       			operation="cancelStatusConfim";
            }else if("onlineRefundConfirm"==radioValue){
                operation="onlineRefundConfirm";
       		}else if("noticeRegimentAudit"==radioValue){
       			operation="noticeRegimentAudit";
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
				
         		if($("#cancleReason").find("option:selected").text()=='提前退')
         		{
         		    if('${canPreRefund}' == 'true'){
         		    	if(!window.confirm("您所预订的产品现已不可退，基于您是驴妈妈的优质客户，该订单可为您直接办理全额退费， 请问您是否确认取消？"))
			       		{
			       			return;
			       		}
         		    }else{
         		    	alert("由于该订单不符合提前退的条件，故不能为用户进行退款操作。\n* 请注意只有订单上有“提前退”标记才是符合提前退的订单。");
					    return;
         		    }
         			
         		}
         		
				if('${isTicket}'=="Y"){
					var paymentTarget = '${order.paymentTarget}';
					var paymentStatusStr= '${paymentStatusStr}';
					var performStatusStr = '${performStatus}';
					var isInUse = false; //是否已使用 
				    if(paymentTarget =='PAY'){
						if(!window.confirm("你真的确定要取消订单吗？"))
		       			{
		       				return;
		       			}				
				    }
				   var jumpflag=false;
				   if(performStatusStr =='已使用'){
				       isInUse = true;
					   if(!window.confirm("订单已使用，请确认是否取消订单？"))
		       			{
		       				return;
		       			}
				    }else{
				       
				       //提交时校验使用状态
				       $.ajax({
						   url : "/vst_order/order/orderManage/queryTicketPerformStatus.do?orderId="+${order.orderId!''},
						   type:"POST",
						   async: false, //同步方式
						   success : function(result){
						   		if(result=="PERFORM" ){
						   		    isInUse = true;
						   			if(!window.confirm("订单已使用，请确认是否取消订单？"))
					       			{
					       			    jumpflag=true;
					       				return;
					       			}
								 
						   		}else if(result=="PART_PERFORM"){
						   		    isInUse = true;
						   			if(!window.confirm("订单已部分使用，请人工计算扣款金额，确认是否取消订单？"))
					       			{
					       				jumpflag=true;
					       				return;
					       			}
						   		}
						   }
			           });	
				    }
				    
				   if(jumpflag){
				      return;
				   }
				   
				   if(!isInUse){
				   	if(paymentStatusStr.trim()=="已支付"){
						if(!window.confirm("${backAmount}"))
		       			{
		       				return;
		       			}
					}else{
						if(!window.confirm("你真的确定要取消订单吗？"))
		       			{
		       				return;
		       			}
					}
				   }
				    
			}else{			
			
			   if(!window.confirm("你真的确定要取消订单吗？"))
		       	{
		       		return;
		       	}
			}
				
				
			
				
	
       		 
       			operation="cancelStatus"; 
       			cancelCode=radioValue;
       			
       			var cancleReasonText=$("#cancleReason").find("option:selected").text();
       			
       			
       			
       		}
       			
	
       		//分销订单并且订单正常并且已支付${order.orderId}
       		if(operation == "cancelStatus" && isDistributor(distributorCode) && 'NORMAL' == orderStatus && 'PAYED' == paymentStatus) {
				var param = "actualAmount=" + "${order.actualAmount/100}"+"&operation="+operation+"&cancelCode="+cancelCode
					+"&cancleReasonText="+cancleReasonText+"&orderRemark="+orderRemark+"&orderId="+orderId;
				saledAppliedDialog = new xDialog("/vst_order/order/orderManage/showSaledApplied.do",param,{title:"售后申请确认",width:600});
       		}else{
	       		
	       		//遮罩层
	    		var loading = pandora.loading("正在努力保存中...");		
	    		
           		var formData=$("#dataForm").serialize()+"&operation="+operation+"&cancelCode="+cancelCode+"&cancleReasonText="+cancleReasonText;
       			//alert(formData);
           		$.ajax({
    			   url : "/vst_order/order/orderManage/updateOrderStatus.do",
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
						if((i+1)==data.length  && (cancelType=="200" || cancelType=="201" || cancelType=="205") )
						{
							$("#cancleReason").append("<option  value="+valueText+">"+text+"</option>");
						}else{
						
							$("#cancleReason").append("<option value="+valueText+">"+text+"</option>");
						}
						
			        });
			        
			        if('${canPreRefund}' == 'true' && cancelType=="202"){
						$("#cancleReason").append("<option value='preRefund'>提前退</option>");
					}
			   		
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
     
     
      var viewSendNoticeListDialog;
  	var sendEmailDialog;

		$("a.viewSendNoticeList").bind("click",function(){
			
			var orderId=$(this).attr("data");
			var contactEmail=$(this).attr("contactEmail");
			viewSendNoticeListDialog = new xDialog("/vst_order/order/orderShipManage/viewSendNoticeList.do",{"orderId":orderId,"sourceType":"notice","contactEmail":contactEmail},{title:"查看出团通知",width:900});
			
		});
     
      //查看成团率（线路）
      $("#showGroupRate").bind("click",function(){    	
    	var productId = $(this).attr("data0");
    	var date= new Date(Date.parse($(this).attr("data1").replace(/-/g,"/")));
    	var year = date.getFullYear();
    	var month = date.getMonth();
    	var param = "productId="+productId+"&currentYear="+year+"&currentMonth="+month;
		new xDialog("/vst_back/prod/groupDateAddtional/queryGroupRate.do?"+param,{},{title:"本线路成团情况",iframe:true,width:700,hight:500,iframeHeight:680,scrolling:"yes"});
      });
 
           
           
     
     </script>
     
<#--业务JS,added by wenzhengtao 20131220-->
<script type="text/javascript">
	//定义上传附件弹出窗口变量
    var uploadOrderAttachmentDialog;
    //定义查看附件弹出窗口变量
    var viewOrderAttachmentDialog;
    
    var showAmountDialog;
    //设置支付方式
    var paymentTermDialog;
    
    //定义查看支付记录窗口变量
    var orderPaymentInfoDialog;
    //定义全局的orderId，给引入的js使用
    var orderId = '${order.orderId}';
    
    var payPromotion='${payPromotion}'; //立减
    if(payPromotion==null || payPromotion=='' || payPromotion == undefined){
      payPromotion=0;
    }
	var orderOughtAmount='${order.oughtAmount}'; //应收款
	var actualAmount='${order.actualAmount}'; //已收款
    var oughtAmount=parseInt(orderOughtAmount) - parseInt(actualAmount) - parseInt(payPromotion);
    
    var orderType='${RequestParameters.orderType!''}';
    
    var ordType='${RequestParameters.orderType!''}';
    //orderItemId='-1';
	//对接机票发送支付通知后需要等待60s才能重新发送的代码  
	$("#sendFlightPaymentInfo").on("click", function(){
		var $this = $(this);
		if($this.attr("disabled") != "disabled" && confirm("确认发送出票通知吗?")) {
		        $this.attr("disabled", true);
				$this.css("color", "#808080");
				$this.css("cursor", "default").css("text-decoration", "none");
	 			$.ajax({
					url : "/vst_order/order/orderManage/sendFlightPaymentInfo.do?orderId=" + orderId,
					type : "post",
					dataType : 'json',
					success : function(result) {
						if(result.code=="success"){
							alert("发送成功");
						}else{
							alert("操作失败:"+result.message);
						}
						setTimeout(function() {
								$this.removeAttr("disabled");
								$this.css("color", "");
								$this.css("cursor", "pointer").css("text-decoration", "underline");
							},60000);
					}
				});	
	 		}
	});

	var editPersonButtonDialogContiune;
	
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
</script>


<script src="/vst_order/js/order/orderAmountChange.js"></script>

<script src="/vst_order/js/order/orderAttachment.js"></script>

<script src="/vst_order/js/order/orderPayment.js"></script>

<script src="/vst_order/js/order/oneKeyOrder.js"></script>
	<#if o2oUserNameSign>
	<script src="/vst_order/js/order/o2oApiInfo.js"></script>
	<script>
		getO2OUserInfo("${o2oUserName}","${o2oUserNameSign}");
	</script>
	</#if>	



