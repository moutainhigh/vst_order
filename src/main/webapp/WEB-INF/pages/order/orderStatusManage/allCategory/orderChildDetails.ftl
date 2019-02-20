<#assign mis=JspTaglibs["/WEB-INF/pages//tld/lvmama-tags.tld"]>
<#import "/base/spring.ftl" as spring/>

<!DOCTYPE html>
<html>
<head>
<title>订单管理-订单处理</title>
<#include "/base/head_meta.ftl"/>
    <style type="text/css">
        tbody tr td#confirmCancelOrder{
            color: #06c;
            cursor: pointer;
        }
    </style>
</head>
<body class="orderChildDetailsBody">

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
        【子订单负责人】  ${orderPrincipal!''}
            【此为子订单】  子订单号：${orderItem.orderItemId!''}  	【主订单】  主订单号：${order.orderId !''}
            <div class="order_seach">查询订单号：<input type="text" id="orderIdSeach" number="true"/><a class="btn ml10" id="orderIdSeachButton" target="_blank">查询</a> </div>
            <#if order.clientIpAddress=="180.169.51.82" && order.distributorId==3>
               	</br><div style="color:red">（请特别特别注意，此订单疑似驴妈妈内部成员下单）</div>
             </#if>          
            
            
            
            </strong></span>
            <#--
            <a class="ml20" href="javaScript:">上一个关联订单++</a> |
            <a href="javaScript:">下一个关联订单++</a>
            -->
        </div>
        
        <input type="hidden" name="orderId" id="orderId" value="${order.orderId!''}">
         <input type="hidden" name="orderItemId" id="orderItemId" value="${orderItem.orderItemId!''}">
         <input type="hidden" name="orderStatus" id="orderStatus" value="${order.orderStatus!''}">
         <input type="hidden" name="resourceStatus" id="resourceStatus" value="${orderItem.resourceStatus!''}">
         <input type="hidden" name="infoStatus" id="infoStatus" value="${orderItem.infoStatus!''}">
         <input type="hidden" name="viewOrderStatus" id="viewOrderStatus" value="${order.viewOrderStatus!''}">
         <input type="hidden" name="isSupplierOrder" id="isSupplierOrder" value="${isSupplierOrder!''}">
         <input type="hidden" name="auditId" id="auditId" value="${auditId!''}">
        <input type="hidden" name="showStatusChange" id="showStatusChange" value="${showStatusChange!''}">
        <input type="hidden" name="showStatusChangeProcessed" id="showStatusChangeProcessed" value="${showStatusChangeProcessed!''}">
        
            
        <div class="solid_border"></div>
        
        <div class="sidebar equalheight_item">
            <div class="side_setbox sidebox">
                <table class="sidebar_table" >
                    <thead>
                        <tr>
                            <th>主订单状态名称</th>
                            <th>状态</th>
                            <th>相关操作</th>
                        </tr>    
                    </thead>
                    <tbody>
	                      <tr>
                            <td>信息预审</td>
                            <td>
                           
                            <#if isDonePretrialAudit==true>
                             
                             	<img src='../../img/pass.png' width='20' height='20' alt='通过'/>
                             
	                         <#else>
	                         	 
                             </#if> 
                             
                            
                            
                            </td>
                        </tr>
                        <tr>
                            <td>审核状态</td>
                            <td>
	                            <#if hasInfoAndResourcePass==true>
	                             
	                             	<img src='../../img/pass.png' width='20' height='20' alt='通过'/>
	                             
		                         <#else>
		                         	 审核中
	                             </#if> 
                            </td>
                                  <td>
                                  <a title="点击查看订单详情" href="/vst_order/order/orderManage/showOrderStatusManage.do?orderId=${order.orderId!''}&orderType=parent" target="_blank">
								  [查看主订单]
								  </a>
								  </td>
                        </tr>
                         <tr>
                            <td>支付状态  </td>
                            <td>
                            	${paymentStatusStr}
                            </td>
                            <td></td>
                        </tr>
                       <#--
                        <tr>
                            <td>合同状态</td>
                            <td>
                            	${contractStatusName!''}
                            </td>
                            <td>
                             <#if ordTravelContract.fileId!=null>
                            	<a href="/vst_back/pet/ajax/file/downLoad.do?fileId=${ordTravelContract.fileId}">[查看合同]</a>
                             </#if> 
                            </td>
                        </tr>
                        -->
                    </tbody>
                </table>
            </div>
            
            
            
            <div class="solid_line"></div>
            
        <div class="side_setbox sidebox">
                
                
                <table class="sidebar_table" >
                    <thead>
                        <tr>
                            <th>子订单状态名称</th>
                            <th>状态</th>
                            <th>相关操作</th>
                        </tr>    
                    </thead>
                    <tbody>
                      
                        
                        <#if order.orderStatus=="NORMAL">
                        
                        <tr>
                            <td>信息审核</td>
                            <td>
                             <#if orderItem.infoStatus=="INFOPASS">
                             
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
                        
                       <#if isDestBuFrontHotelOrderItem==false>
                       <tr>
                            <td>资源审核</td>
                            <td>
                           
                            <#if orderItem.resourceStatus=="AMPLE">
                             
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
                       	<input type="hidden"  name="hidden_resourceStatus" value="${orderItem.resourceStatus}_${auditMap['RESOURCE_AUDIT']}"/>
                       </#if> 
                        
                           <#if orderItem.categoryId=='33'>
                              
                           <#else>
                              <tr>
	                            <td>凭证确认</td>
	                            <td>
	                            <#if isDoneCertificate==true>
	                             	<img src='../../img/pass.png' width='20' height='20' alt='通过'/>
		                         <#else>
		                         	 <#if  auditMap['CERTIFICATE_AUDIT']==true>
		                         	 	 <input type="radio" id="certificate" name="radioManage" value="certificateStatus" <#if order.orderSubType=='STAMP' > disabled="true"</#if> />
		                         	 <#else>
		                         	 	<input type="radio" id="certificate" name="radioManage" value="certificateStatus"  disabled="true"/>
		                         	 </#if> 
	                             </#if> 
	                            </td>
	                            <td>
	                            <a id="findEbookingFaxRecvList"  <#if order.orderSubType!='STAMP' > href="javaScript:"</#if> >[查回传(${ebkCount})]</a>
	                            </td>
	                        </tr>
                           </#if>
                        
                       </#if>

 						<#if order.orderStatus=="CANCEL">
                                <#if auditMap['CANCEL_CONFIRM_AUDIT']==true >
                                    <tr>
                                        <td>订单确认取消</td>
                                        <td>
                                            <input type="radio"  id="confirmCancelOrder" name="radioManage" value="cancelStatusConfim"/>
                                        </td>
                                        <#--<td id="confirmCancelOrder">[确认]</td>-->
                                    </tr>
		 						<#elseif auditMap['CANCEL_AUDIT']==true >
		 						  <tr>
		                            <td>订单确认取消</td>
		                            <td>
	                        		 <input type="radio"  id="cancleConfirmed" name="radioManage" value="cancelStatusConfim"/>
	                        		 </td>
		                         </tr>
		                         <#elseif isDoneChildCancleConfirmedAudit==true>
		                         <tr>
		                            <td>订单取消已确认</td>
		                            <td>
		                             	<img src='../../img/pass.png' width='20' height='20' alt='通过'/>
	                        		 </td>
		                         </tr>
	                        	 </#if> 
                         </#if>

                        <!--目的地酒店套餐 酒店预定号 Added by yangzhenzhong-->
                        <#if order.buCode?? && (order.buCode=="DESTINATION_BU" || order.buCode=="OUTBOUND_BU")>
                            <#if orderItem.categoryId=='17' || orderItem.categoryId=='32' || (order.subCategoryId?? && order.subCategoryId=='181' && orderItem.categoryId=='1')>
                              <#if suppOrderId??>
                              <tr>
                              <td>供应商订单号</td>
                              <td>${suppOrderId}</td>
                              </tr>
                           <#else>
                              <tr>
                              <td>供应商订单号</td>
                              <td>${supplierNo}</td>
                              </tr>
                           </#if>
                           </#if>
                        </#if>
						<!--国内酒店子单预定号-->
                        <#if order.buCode?? && (order.buCode=="LOCAL_BU" || order.buCode=="OUTBOUND_BU")>
                        <#if (order.categoryId?? && order.categoryId=='15') || (order.subCategoryId?? && order.subCategoryId=='182' && orderItem.categoryId=='1')>
                          <#if suppOrderId??>
                              <tr>
                              <td>酒店预定号</td>
                              <td>${suppOrderId}</td>
                              </tr>
                           <#else>
                              <tr>
                              <td>酒店预定号</td>
                              <td>${supplierNo}</td>
                              </tr>
                           </#if>
						  </#if>
                        </#if>
                         <!--是否保留房-->
                         <#if order.buCode?? && order.buCode!="OUTBOUND_BU">
                         <#if orderItem.categoryId=='17' || orderItem.categoryId=='32'|| orderItem.categoryId=='1'>
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
                         <#if isNeedShowConfirmStatus?? && isNeedShowConfirmStatus == true>
	                           <tr>
	                             <td>订单状态 </td>
			                     <td>
			                   		    <select name="confirmStatus" autocomplete="off" class="w10"  value="${orderItem.confirmStatus!''}" onchange="confirmIdDisplay(this);" >
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
                    <div><a id="viewParentOrderAttachment" class="fr" href="javaScript:">附件(<b>${parentOrderAttachmentNumber}</b>)</a>主订单备注记录:</div>
                    <textarea style="width:285px; height:120px;" id="parentOrderRemark" name="parentOrderRemark" readOnly="true">${order.orderMemo!''}</textarea>
                    <div><a id="viewOrderAttachment" class="fr" href="javaScript:">附件(<b>${orderAttachmentNumber}</b>)</a>订单备注记录:</div>
                    <textarea style="width:285px; height:120px;" id="orderRemark" name="orderRemark" onkeyup="checkRemarkLength()">${orderItem.orderMemo!''}</textarea>
                    <span class="fr" id="zsRemark">0/500字</span>
                    <div class="operate mt10">
                    <a class="btn btn_cc1" id="saveButton" >确认修改</a>
                    <a class="btn btn_cc1" id="clearButton" href="javaScript:clearradio();" >清空选择</a>
                    </div>
                    <div class="mt20">
                        <ul class="supplier_list">
                           <#if order.orderSubType!='STAMP'> <li><a id="sendOderFax"   href="javaScript:" title="发送凭证">发送凭证</a></li></#if>
                           <#if order.orderSubType!='STAMP'> <li><a id="findEbkFaxList"  href="javaScript:" title="凭证查询">凭证查询</a></li></#if>
<#--
                            <li><a id="ordSale"  target="_blank" href="/super_back/ord/sale/OrdSaleAddJump.zul?orderId=${order.orderId}&sysCode=VST" title="售后服务">售后服务</a></li>
-->
                               <li><a id="ordSale"  target="_blank" href="/sales_front/ord/sale/toOrdSale/${order.orderId}" title="售后服务">售后服务</a></li>

                               <li><a id="addMessage"   href="javaScript:" title="预订通知">预订通知</a></li>
                            <li><a id="uploadOrderAttachment" href="javaScript:" title="上传附件">上传附件</a></li>
                            <li><a id="showLog" href="javaScript:" title="查看日志">查看日志</a></li>
                            
                            <#if order.orderSubType!='STAMP'>
                            <#if order.orderStatus=="NORMAL" &&  order.paymentStatus == "UNPAY" >
                             	<@mis.checkPerm permCode="6220"><li><a id="changeAmount" href="javaScript:" title="修改价格">修改价格</a></li></@mis.checkPerm>
                            </#if>


                             <#if orderItem.categoryId != '32'>
                             <li><a id="viewPassCode"  target="_blank" href="/vst_passport/passCode/findList.do?orderId=${order.orderId}"  title="查看通关码">查看通关码</a></li>
                             </#if>
                             <#if hasVisa==true>

                             <li><a id="findOrderVisaApprovalQueryList"   href="javaScript:" title="查看签证审核">查看签证审核</a></li>
                            </#if>
                             <li><a id="findInsurancePolicyList"   target="_blank" href="/vst_insurant/insurancePolicy/findList.do?orderId=${order.orderId}"  title="查看投保状态">查看投保状态</a></li>

                             <#if isReFundButtonShow?? && isReFundButtonShow == true>
                             	<li><a id="partRefundApplication"   target="_blank" href="/vst_order/partRefundBackAction/toPartRefundPage.do?orderId=${order.orderId}"  title="退款申请">退款申请</a></li>
                           	 </#if>

                            <#if isReCancelBtn?? && isReCancelBtn == 'REJECT'>
                            	<li><a id="reSendCancelApply" href="javaScript:"  title="重新取消">重新取消</a></li>
                            </#if>
                            </#if>
                            
                             <#if orderItem.categoryId == '33'>
                             	<li><a id="selectRight"   href="javaScript:" title="查看权益">查看权益</a></li>
                             </#if>
                        </ul>
                    </div>
                </div>
                <div class="main_order_info">
                    <div id="findOrderBaseInfo">
	                    <div class="order_msg clearfix">
		            	正在使劲加载数据中...
		            	</div> 
                    </div> 
                    <div class="solid_line mt10 mb10"></div>
                    <div id="findOrderSuppGoodsInfo">
	                    <div class="order_msg clearfix">
		            	正在使劲加载数据中...
		            	</div>
                    </div>
                   </br>
                    <div class="solid_line mt10 mb10"></div>
            		<div id="findOrdPersonBooker">
            		   <div class="order_msg clearfix">
		            	正在使劲加载数据中...
		            	</div>
            		</div>
              </div>
               <#if order.orderSubType!='STAMP'>	 
             <div id="findTravellerPersonList">
               <div class="order_msg clearfix">
	            	正在使劲加载数据中...
	            </div> 
             </div>

			<!-- 服务信息 -->
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
            <div class="solid_line"></div>
            <!--机票子订单详情页-->
			<div>
				<#if orderItem.isApiFlightTicket() && flightOrderDetailUrl?exists >
				<br><h1 id="flightOrderItemDetai" style="text-decoration:underline;cursor:pointer;">机票子订单详情页：</h1><br>
				<iframe id="flightOrderItemDetailContent" style="scrolling2:no;frameborder:1;width:100%;height:1000px;display:block;" src="${flightOrderDetailUrl?if_exists}"></iframe>
				<script>
					$("#flightOrderItemDetai").click(function(){
					  $("#flightOrderItemDetailContent").toggle();
					});
				</script>
				</#if>
            </div>
            
            <#if order.orderSubType=='STAMP'>
            <div id="findPreSaleList">
	                    <div class="order_msg clearfix">
		            	正在使劲加载数据中...
		            	</div> 
            </div>
            </#if>
        </div>
    </div>
    
    
   
    </form>
    <div id="logResult" style="display:none;margin-left:40px;margin-right:35px;">
     
     </div>
    <#include "/base/foot.ftl"/>
</body>
</html>
    <script type="text/javascript">
       
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
            	var param="?orderId=${order.orderId!''}&orderItemId=${RequestParameters.orderItemId}&orderType=child";
			  messageDialog = new xDialog("/vst_order/order/orderManage/findComMessageList.do"+param,{},{title:"查看预订通知",iframe:true,width:"1200"});
		    
			//  messageDialog = new xDialog("/vst_order/order/orderManage/findComMessageList.do",{"orderId":${order.orderId!''},"orderItemId":"${RequestParameters.orderItemId}","orderType":"child"},{title:"查看预订通知",width:1200});
		     }
		     		   
            
            var baseInfoData="orderItemId="+${RequestParameters.orderItemId};
         	$.post("/vst_order/order/orderManage/findOrderBaseInfo.do",
			   baseInfoData,
			   function(result){
			   
			 	$("#findOrderBaseInfo").html(result);
      		});	
            
             var ordPersonBookerData="orderId=${order.orderId}&orderItemId="+${RequestParameters.orderItemId};
         	$.post("/vst_order/order/orderManage/findOrdPersonBooker.do",
			   ordPersonBookerData,
			   function(result){
			   
			 	$("#findOrdPersonBooker").html(result);
      		});	
      		
      		var suppGoodsInfoData="orderItemId="+${RequestParameters.orderItemId}+"&orderId=${order.orderId}";
         	$.post("/vst_order/order/orderManage/findOrderSuppGoodsInfo.do",
			   suppGoodsInfoData,
			   function(result){
			   
			 	$("#findOrderSuppGoodsInfo").html(result);
      		});	
		
            var touristData="orderItemId="+${RequestParameters.orderItemId}+"&orderId="+${order.orderId!''};
         	
         	$.post("/vst_order/order/orderManage/findTravellerPersonList.do",
			   touristData,
			   function(result){
			   
			 	$("#findTravellerPersonList").html(result);
      		});	
      		
      		var goodsPersonData="orderId=${order.orderId!''}";
         	$.post("/vst_order/order/orderManage/findGoodsPersonList.do",
			   goodsPersonData,
			   function(result){
			   
			 	$("#findGoodsPersonList").html(result);
      		});
      		
			var emergencyPersonData="orderId=${order.orderId!''}";
         	$.post("/vst_order/order/orderManage/findEmergencyPersonList.do",
			   emergencyPersonData,
			   function(result){
			 	$("#findEmergencyPersonList").html(result);
      		});
      		
			var expressAddressData="orderId="+${order.orderId!''};
         	$.post("/vst_order/order/orderManage/findExpressAddressList.do",
			   expressAddressData,
			   function(result){
			   
			 	$("#findExpressAddressList").html(result);
      		});


            var expressOrderData="orderId="+${order.orderId!''};
            /*需要添加findExpressOrderList请求*/
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
			var param={"orderId":${order.orderId!''},"orderItemId":${orderItem.orderItemId!''},"orderRemark":orderRemark,"orderType":"child"};
			addMessageDialog = new xDialog("/vst_order/order/orderManage/showAddMessage.do",param,{title:"创建预订通知",width:800});
         
         });
         
         var selectRightDialog;
		$("#selectRight").bind("click",function(){
			selectRightDialog = new xDialog("/vst_order/order/orderManage/findRightList.do",{"orderId":${order.orderId!''},"orderItemId":${orderItem.orderItemId!''}},{title:"查看权益",width:1300});
		}); 
         
         $("#reSendCancelApply").bind("click",function(){
             if(window.confirm("确定重新取消？")){
                 //遮罩层
                 var loading = pandora.loading("正在努力保存中...");
                 $.ajax({
                     url : "/vst_order/order/orderManage/reSendCancelApply.do",
                     data : {orderItemId:orderItemId},
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
         
         
          var showLogDialog;
         $("#showLog").bind("click",function(){
			var param="objectType=ORD_ORDER_ITEM&objectId="+${orderItem.orderItemId}+"&sysName=VST";
            showLogDialog =new xDialog("/lvmm_log/bizLog/showVersatileLogList?"+param,{},{title:"查看日志",iframe:true,width:1000,hight:300,iframeHeight:680,scrolling:"yes"});
         
         });
		        
		 var faxDialog,sendOderFaxDialog;
        $("#sendOderFax").bind("click",function(){
        	var orderRemark=$.trim($("#orderRemark").val());
            var cancelCertConfirmStatus = '${cancelCertConfirmStatus!''}';
            if (cancelCertConfirmStatus && cancelCertConfirmStatus != 'REJECT' && "${faxFlag!''}"!="Y") {
                sendOderFaxDialog = new xDialog("/vst_order/order/orderManage/showOrderCertifDialog.do",{"cancelCertConfirmStatus":cancelCertConfirmStatus},{title:"发送凭证",width:600});
            } else {
                sendOderFaxDialog = new xDialog("/vst_order/order/orderManage/showManualSendOrderFax.do",{"orderId":${order.orderId!''},"orderItemId":${orderItem.orderItemId!''},"orderRemark":orderRemark,"source":"noInfoPass","cancelCertConfirmStatus":cancelCertConfirmStatus},{title:"发送凭证",width:600});
            }
		});
		var findEbkFaxListDialog;
		$("#findEbkFaxList").bind("click",function(){
			findEbkFaxListDialog = new xDialog("/vst_certif/ebooking/task/findEbkAndFaxList.do",{"orderId":${order.orderId!''},"orderItemId":${orderItem.orderItemId!''}},{title:"凭证查询",width:1300});
		});  
		
		var findEbookingFaxRecvListDialog;
		<#if order.orderSubType!='STAMP'>
		$("#findEbookingFaxRecvList").bind("click",function(){
			findEbookingFaxRecvListDialog = new xDialog("/vst_certif/ebooking/faxRecv/findEbookingFaxRecvList.do",{"orderId":${order.orderId!''},"readUserStatus":"Y","source":"orderHotelDetails"},{title:"查回传",width:600});
		});  
        </#if>
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

        $("td#confirmCancelOrder").bind("click", function(){
            var orderItemId = $('input[name="orderItemId"]').val();
            var auditId = $('input[name="auditId"]').val();
            $.ajax({
                url: "/vst_order/order/orderManage/orderCancelConfirm.do",
                data: {"orderItemId":orderItemId,"auditId":auditId},
                type: "get",
                dataType: "JSON",
                contentType: "application/json",
                success: function(result){
                    alert(result.message);
                    window.location.reload();
                }
            });

        });
        
        var certificateDialog,orderSendSmsDialog,retentionTimeDialog ;
        
       $("#saveButton").bind("click",function(){
      
       		var operation;
       		var cancelCode;
       		var cancleReason;
       		var orderRemark=$.trim($("#orderRemark").val());
       		var radioValue="updateOrderRemark";
       		
       		
       		if($('input:radio:checked').length<=0)
       		{
       			radioValue="updateOrderRemark";
       		}else if("${order.orderStatus}"=="CANCEL"){
       		
       			var radioVal=$('input:radio:checked').val();
       			if(radioVal=="INFOPASS" || radioVal=="RESOURCEPASS" || radioVal=="certificateStatus" || radioVal=="paymentAudit" || radioVal=="timePaymentAudit" || radioVal=="cancelStatusConfim")
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
       			/**
       			var param={"orderId":${order.orderId!''},"infoStatus":"${order.infoStatus!''}","orderRemark":orderRemark,"operation":operation};
       			faxDialog = new xDialog("/vst_order/order/orderManage/showSendOrderFax.do",param,{title:"发送凭证",width:600});
       			return;
       			*/
       		}else if("RESOURCEPASS"==radioValue){
       			operation="resourceStatus";
       			
       			var param={"orderCatType":"curise","orderId":${order.orderId!''},"orderItemId":"${orderItem.orderItemId!''}","resourceStatus":"${orderItem.resourceStatus!''}","orderRemark":orderRemark,"operation":operation};
       			retentionTimeDialog = new xDialog("/vst_order/order/orderManage/showUpdateRetentionTime.do",param,{title:"修改资源保留时间",width:600});
       			return;
       			
       			
       		}else if("certificateStatus"==radioValue){
       			operation="certificateStatus";
				var param={"orderItemId":"${orderItem.orderItemId!''}","orderRemark":orderRemark,"operation":operation};
       			certificateDialog = new xDialog("/vst_order/order/orderManage/showAddCertificate.do",param,{title:"凭证确认",width:600});
				return;
       		}else if("cancelStatusConfim"==radioValue){
       			operation="cancelStatusConfim";
       		}else {
       			cancleReason=$("#cancleReason").val();
       			if(cancleReason=='0')
				{
					alert("取消原因还未选择");
					return;
				}
				var isTicket = $("#isTicketType").val();
                var backAmount = $("#backAmount").val();
                var performStatus = $("#performStatus").val();
                var paymentStatusStr= '${paymentStatusStr}';
                if(isTicket=="Y")
                {
                     if(performStatus =='已使用'){
					   if(!window.confirm("订单已使用，请人工计算扣款金额，确认取消?"))
		       			{
		       				return;
		       			}
				     }
				     
	                 if(paymentStatusStr.trim()=='已支付'){
					  if(!window.confirm(backAmount))
	       			  {
	       				return;
	       			  }
					}else{
						if(!window.confirm("你真的确定要取消订单吗？"))
		       			{
		       				return;
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

            var auditId = $('input[name="auditId"]').val();
       		var showStatusChange = $('input[name="showStatusChange"]').val();
       		var showStatusChangeProcessed = $('input[name="showStatusChangeProcessed"]').val();
       		
       		//遮罩层
    		var loading = pandora.loading("正在努力保存中...");		
	
       		var formData=$("#dataForm").serialize()+"&operation="+operation+"&cancelCode="+cancelCode+"&cancleReasonText="+cancleReasonText+"&showStatusChange="+showStatusChange+"&auditId="+auditId+"showStatusChangeProcessed="+showStatusChangeProcessed;
       		//alert(formData);
       		$.ajax({
			   url : "/vst_order/order/orderManage/updateChildOrderStatus.do",
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
			
						
		});
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
		var orderMemo = form.find("[name=orderRemark]").val();

		var orderRemark=$.trim($("#orderRemark").val());
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

	                var formData="orderItemId="+orderItemId+"&initStatus="+initStatus+"&updateStatus="+updateStatus+"&confirmId="+confirmId+"&orderMemo="+orderRemark;
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
     
     	var findOrderVisaApprovalQueryListDialog;
         $("#findOrderVisaApprovalQueryList").bind("click",function(){

			//var param={"searchOrderId":${order.orderId!''}};
			//findOrderVisaApprovalQueryListDialog = new xDialog("/vst_back/visa/approval/showOrderVisaApprovalQueryList.do?searchOrderId=${order.orderId}","",{title:"查看签证审核",iframe:true,width:900});
			findOrderVisaApprovalQueryListDialog = new xDialog("/visa_prod/visa/approval/showOrderVisaApprovalQueryList.do?searchOrderId=${order.orderId}",{},{title:"查看签证审核",width:900});
         
         });
     
     </script>
     
<#--业务JS,added by wenzhengtao 20131220-->
<script type="text/javascript">
	//定义上传附件弹出窗口变量
    var uploadOrderAttachmentDialog;
    //定义查看附件弹出窗口变量
    var viewOrderAttachmentDialog;
   
    var showAmountDialog;
    
    //定义查看支付记录窗口变量
    var orderPaymentInfoDialog;
    //定义全局的orderId，给引入的js使用
    var orderId = '${order.orderId}';
    
    var orderItemId = '${orderItem.orderItemId}';
    
    var orderType='${RequestParameters.orderType!''}';
    
    var ordType='${RequestParameters.orderType!''}';
</script>
<script src="/vst_order/js/order/orderAttachment.js"></script>

<script src="/vst_order/js/order/orderPayment.js"></script>

<script src="/vst_order/js/order/orderAmountChange.js"></script>
