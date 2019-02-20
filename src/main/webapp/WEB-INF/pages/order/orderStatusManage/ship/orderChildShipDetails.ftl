<#assign mis=JspTaglibs["/WEB-INF/pages//tld/lvmama-tags.tld"]>
<#import "/base/spring.ftl" as spring/>

<!DOCTYPE html>
<html>
<head>
<title>订单管理-订单处理</title>
<#include "/base/head_meta.ftl"/>
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
            【此为子订单】  订单号：${orderItem.orderItemId!''}  	【主订单】  订单号：${order.orderId !''}
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
        
        <input type="hidden" name="orderId" id="orderId" value="${order.orderId!''}">
         <input type="hidden" name="orderItemId" id="orderItemId" value="${orderItem.orderItemId!''}">
         <input type="hidden" name="orderStatus" id="orderStatus" value="${order.orderStatus!''}">
         <input type="hidden" name="resourceStatus" id="resourceStatus" value="${orderItem.resourceStatus!''}">
         <input type="hidden" name="infoStatus" id="infoStatus" value="${orderItem.infoStatus!''}">
         <input type="hidden" name="viewOrderStatus" id="viewOrderStatus" value="${order.viewOrderStatus!''}">
         <input type="hidden" name="isSupplierOrder" id="isSupplierOrder" value="${isSupplierOrder!''}">
        
        
            
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
                                  <a title="点击查看订单详情" href="/vst_order/order/orderShipManage/showOrderStatusManage.do?orderId=${order.orderId!''}&orderType=parent" target="_blank">
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
                        
                    </tbody>
                </table>
            </div>
            
            <#if order.orderStatus=="NORMAL">
            
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
                            <td>
                               <#--
                            <a id="findEbookingFaxRecvList" href="javaScript:">[--查回传(${ebkCount})]</a>
                              -->
                            </td>
                        </tr>
                         
                      
                       
                       <#--
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
                         -->
                         
                    </tbody>
                </table>
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
                        <#--
                            <li><a id="sendOderFax"   href="javaScript:" title="发送凭证">发送凭证</a></li>
                            <li><a id="findEbkFaxList"  href="javaScript:" title="凭证查询">凭证查询</a></li>
                            -->
<#--
                            <li><a id="ordSale"  target="_blank" href="/super_back/ord/sale/OrdSaleAddJump.zul?orderId=${order.orderId}&sysCode=VST" title="售后服务">售后服务</a></li>
-->
                            <li><a id="ordSale"  target="_blank" href="/sales_front/ord/sale/toOrdSale/${order.orderId}" title="售后服务">售后服务</a></li>

                            <li><a id="addMessage"   href="javaScript:" title="预订通知">预订通知</a></li>
                            <li><a id="uploadOrderAttachment" href="javaScript:" title="上传附件">上传附件</a></li>
                            <li><a id="showLog" href="javaScript:" title="查看日志">查看日志</a></li>
                            <li><a id="findInsurancePolicyList"   target="_blank" href="/vst_insurant/insurancePolicy/findList.do?orderId=${order.orderId}"  title="查看投保状态">查看投保状态</a></li>                           	 
                             <#--
                             <#if order.orderStatus=="NORMAL" &&  order.paymentStatus == "UNPAY" >  
                              <@mis.checkPerm permCode="6220"><li><a id="changeAmount" href="javaScript:" title="修改价格">修改价格</a></li></@mis.checkPerm>
                            </#if>
                             -->
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
                    <strong>
                    下单人相关信息
                    </strong>
                    </br>
            		<div id="findOrdPersonBookerList">
            		   <div class="order_msg clearfix">
		            	正在使劲加载数据中...
		            	</div> 
            		</div>
              </div>
             <div id="findTouristList">
               <div class="order_msg clearfix">
	            	正在使劲加载数据中...
	            </div> 
             </div>
            <div id="findGoodsPersonList">
               <div class="order_msg clearfix">
	            	正在使劲加载数据中...
	           </div> 
            </div>                   
				 
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
            
            
            var baseInfoData="orderItemId="+${RequestParameters.orderItemId};
         	$.post("/vst_order/order/orderShipManage/findOrderBaseInfo.do",
			   baseInfoData,
			   function(result){
			   
			 	$("#findOrderBaseInfo").html(result);
      		});	
            
             var ordPersonBookerData="orderItemId="+${RequestParameters.orderItemId};
         	$.post("/vst_order/order/orderShipManage/findOrdPersonBooker.do",
			   ordPersonBookerData,
			   function(result){
			   
			 	$("#findOrdPersonBookerList").html(result);
      		});	
      		
		  	var goodsPersonData="orderId=${order.orderId!''}&orderItemId=${RequestParameters.orderItemId}&isChild=Y";
		    $.post("/vst_order/order/orderShipManage/findGoodsPersonList.do",
				   goodsPersonData,
				   function(result){
				 	$("#findGoodsPersonList").html(result);
		  	});		      		
      		
      		var suppGoodsInfoData="orderItemId="+${RequestParameters.orderItemId};
         	$.post("/vst_order/order/orderShipManage/findOrderSuppGoodsInfo.do",
			   suppGoodsInfoData,
			   function(result){
			   
			 	$("#findOrderSuppGoodsInfo").html(result);
      		});	
		
            var touristData="orderItemId="+${RequestParameters.orderItemId};
         	
         	$.post("/vst_order/order/orderShipManage/findTouristList.do",
			   touristData,
			   function(result){
			   
			 	$("#findTouristList").html(result);
      		});	
      		
			if(${messageCount}>0){
			  messageDialog = new xDialog("/vst_order/order/orderShipManage/findComMessageList.do",{"orderId":${order.orderId!''},"orderItemId":"${RequestParameters.orderItemId}","orderType":"child"},{title:"查看预订通知",width:1200});
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
      
		var addMessageDialog;
		$("#addMessage").bind("click",function(){

			var orderRemark=$.trim($("#orderRemark").val());
			var param={"orderId":${order.orderId!''},"orderItemId":${orderItem.orderItemId!''},"orderRemark":orderRemark,"orderType":"child"};
			addMessageDialog = new xDialog("/vst_order/order/orderShipManage/showAddMessage.do",param,{title:"创建预订通知",width:800});
         
         });
          var showLogDialog;
         $("#showLog").bind("click",function(){

			var param="objectType=ORD_ORDER_ITEM&objectId="+${orderItem.orderItemId!}+"&sysName=VST";
            showLogDialog =new xDialog("/lvmm_log/bizLog/showVersatileLogList?"+param,{},{title:"查看日志",iframe:true,width:1000,hight:300,iframeHeight:680,scrolling:"yes"});
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
		
		var findEbookingFaxRecvListDialog;
		$("#findEbookingFaxRecvList").bind("click",function(){
			findEbookingFaxRecvListDialog = new xDialog("/vst_certif/ebooking/faxRecv/findEbookingFaxRecvList.do",{"orderId":${order.orderId!''},"readUserStatus":"Y","source":"orderHotelDetails"},{title:"查回传",width:600});
		});  
         
              
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
       			faxDialog = new xDialog("/vst_order/order/orderShipManage/showSendOrderFax.do",param,{title:"发送凭证",width:600});
       			return;
       			*/
       		}else if("RESOURCEPASS"==radioValue){
       			operation="resourceStatus";
       			
       			var param={"orderCatType":"curise","orderId":${order.orderId!''},"orderItemId":"${orderItem.orderItemId!''}","resourceStatus":"${orderItem.resourceStatus!''}","orderRemark":orderRemark,"operation":operation};
       			retentionTimeDialog = new xDialog("/vst_order/order/orderShipManage/showUpdateRetentionTime.do",param,{title:"修改资源保留时间",width:600});
       			return;
       			
       			
       		}else if("certificateStatus"==radioValue){
       			operation="certificateStatus";
				var param={"orderItemId":"${orderItem.orderItemId!''}","orderRemark":orderRemark,"operation":operation};
       			certificateDialog = new xDialog("/vst_order/order/orderShipManage/showAddCertificate.do",param,{title:"凭证确认",width:600});
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
       			operation="cancelStatus"; 
       			cancelCode=radioValue;
       			var cancleReasonText=$("#cancleReason").find("option:selected").text();
       		}
       		
       		//遮罩层
    		var loading = pandora.loading("正在努力保存中...");		
	
       		var formData=$("#dataForm").serialize()+"&operation="+operation+"&cancelCode="+cancelCode+"&cancleReasonText="+cancleReasonText;
       		//alert(formData);
       		$.ajax({
			   url : "/vst_order/order/orderShipManage/updateChildOrderStatus.do",
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
     
     
     </script>
     
<#--业务JS,added by wenzhengtao 20131220-->
<script type="text/javascript">
	//定义上传附件弹出窗口变量
    var uploadOrderAttachmentDialog;
    //定义查看附件弹出窗口变量
    var viewOrderAttachmentDialog;
   
    //定义查看支付记录窗口变量
    var orderPaymentInfoDialog;
    
 	var showAmountDialog;
 	
    //定义全局的orderId，给引入的js使用
    var orderId = '${order.orderId}';
    
    var orderItemId = '${orderItem.orderItemId}';
    
    var orderType='${RequestParameters.orderType!''}';
    
    var ordType='${RequestParameters.orderType!''}';
</script>
<script src="/vst_order/js/order/orderAttachment.js"></script>

<script src="/vst_order/js/order/orderPayment.js"></script>

<script src="/vst_order/js/order/orderAmountChange.js"></script>