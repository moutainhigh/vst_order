<#import "/base/spring.ftl" as s/>
<#import "/base/pagination.ftl" as pagination>
<!DOCTYPE html>
<html>
	<head>
		<#include "/base/head_meta.ftl"/>
	</head>
	<script type="text/javascript" src="/vst_order/js/jquery.mousewheel.js"></script>
	<body>
<div class="fl operate">
<div id="result" class="iframe_content mt20">
	<div class="fl operate" style="margin: 10px 0 10px;">
		 <form id="searchForm" action="/vst_order/ord/order/confirm/queryDestHotelTaskList.do" method="post">
        <input type="hidden" name="checkedTab" value="MYTASK"/>
        <table class="s_table">
            <tbody>
                <tr>
                	<td class="w8 s_label">主订单号：</td>
                            <td class="w15">
                                <input type="text" name="orderId" id="orderId" value="${monitorCnd.orderId}" />
                            </td>
                    <td class="w8 s_label">子订单号：</td>
                            <td class="w15">
                                <input type="text" name="orderItemId" id="orderItemId" value="${monitorCnd.orderItemId}" />
                            </td>
                    <td class="w8 s_label">入住时间：</td>
                    <td class="w15">
                        <@s.formInput "monitorCnd.visitTimeBegin" 'class="w9" onClick="WdatePicker({readOnly:true})"'/>
                    </td>
                    <td class="w8 s_label">资源审核:</td>
                    <td class="w15">
                        <select name="resourceStatus">
									<option value="" <#if monitorCnd.resourceStatus == ''>selected="selected"</#if>>全部</option>
									<option value="UNVERIFIED" <#if monitorCnd.resourceStatus == 'UNVERIFIED'>selected="selected"</#if>>未审核</option>
									<option value="AMPLE" <#if monitorCnd.resourceStatus == 'AMPLE'>selected="selected"</#if>>资源满足</option>
									<option value="LOCK" <#if monitorCnd.resourceStatus == 'LOCK'>selected="selected"</#if>>资源不满足</option>
						</select>
                    </td>
                </tr>
                <tr>
                    <td class="s_label">供应商：</td>
                    <td>
                    	<input type="hidden" name="supplierId" id="supplierId" value="${monitorCnd.supplierId!''}" />
                    	<input type="text" class="searchInput" name="supplierName" id="supplierName" value="${monitorCnd.supplierName!''}" />
                    </td>
                    <td class="s_label">渠道：</td>
                    <td>
                        <input type="checkbox" name="orderChannel" value="neither" <#if orderChannel && orderChannel?index_of('neither') !=-1>checked</#if>/>主站
                        	<input type="checkbox" name="orderChannel" value="other" <#if orderChannel && orderChannel?index_of('other') !=-1>checked</#if>/>分销(不含淘宝)
                        	<input type="checkbox" name="orderChannel" value="taobao" <#if orderChannel && orderChannel?index_of('taobao') !=-1>checked</#if>/>分销(淘宝)
                    </td>
                    
                </tr>
            </tbody>
        </table>
        <input type="hidden" name="orderFilter" id="orderFilter" value="${orderFilter!'Y'}" />
        <input type="hidden" name="activityName" id="activityName" value="" />
        <input type="hidden" name="activityDetail" id="activityDetail" value="" />
        
        <div class="operate mt20" style="text-align:center">
            <a class="btn btn_cc1" id="search_button">查询</a>
        </div>
        
        <div id="errorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>请至少输入一个查询条件！</div>
    </form>
	</div>
	<#if resultPage?? >
	    <#if resultPage.items?size gt 0 >
	            <div class="p_box" style="float: left; width: 100%;">
	                <table class="p_table table_center" id="taskListTable">
	                    <thead>
	                        <tr>
	                            <th>订单号</th>
	                            <th>商品信息</th>
	                            <th>供应商</th>
	                            <th>入离时间</th>
	                            <th>客人</th>
	                            <th>房价</th>
	                            <th>客人备注</th>
	                            <th>入库时长</th>
	                            <th>通知</th>
	                            <th>备注信息</th>
							    <#if monitorCnd.bespokeOrder !='Y'>	                            
	                            <th>操作</th>
	                            </#if>
	                        </tr>
	                    </thead>
	                    <tbody>
	                        <#list resultPage.items as result>
	                                <tr>
	                                    <td>
	                                    	<#if result.orderMonitorRst.stockFlag=='all'>
	                                            		保留<br>
	                                        <#elseif result.orderMonitorRst.stockFlag=='part'>
	                                        			部分保留<br>
	                                        <#else>
	                                            		非保留<br>
	                                        </#if>
	                                        <a title="点击查看订单详情" href="/vst_order/order/ordCommon/showOrderDetails.do?objectId=${result.orderMonitorRst.orderId}&objectType=ORDER<#if result.auditTypeName=='催支付'>&isReminderPayment=cuizhifu</#if>" target="_blank">
	                                            ${result.orderMonitorRst.orderId}
	                                        </a>/
	                                        <a title="点击查看订单详情" href="/vst_order/order/ordCommon/showOrderDetails.do?objectId=${result.objectId}&objectType=${result.objectType}<#if result.auditTypeName=='催支付'>&isReminderPayment=cuizhifu</#if>" target="_blank">
	                                            ${result.orderMonitorRst.orderItemId}
	                                        </a>
	                                        <#if result.orderMonitorRst.guarantee == 'GUARANTEE'>
	                                            <a title="该订单需要担保">#</a>
	                                        </#if>
	                                        <#if result.orderMonitorRst.distributionChannel=='taobao'>
	                                        	<br>淘宝
	                                        </#if>
	                                        <#if result.orderMonitorRst.distributionChannel=='O2O'>
	                                        	<br>O2O
	                                        </#if>
	                                        <#if result.orderMonitorRst.distributionChannel=='other'>
	                                        	<br>分销（不含淘宝）
	                                        </#if>
	                                    </td>
	                                    <td>${result.orderMonitorRst.productName!''} ${result.orderMonitorRst.suppGoodsName!''}</td>
	                                    <td>
	                                    	${result.orderMonitorRst.belongBU!''} <br> 
	                                    	<a  href="javaScript:void(0)" id="supplierName" onclick="viewSupplier('${result.orderMonitorRst.supplierId}','${result.orderMonitorRst.suppGoodsId}');">${result.orderMonitorRst.supplierName!''}</a>
	                                    </td>
	                                    <td>${result.orderMonitorRst.visitTime}</td>
	                                    <td>${result.orderMonitorRst.contactName}</td>
	                                    <#if result.orderMonitorRst.settlementPrice?? && result.orderMonitorRst.settlementPrice !=''>
	                                    <td>￥${(result.orderMonitorRst.settlementPrice?number/100)?string('#0.00')}
                                        <#if result.orderMonitorRst.categoryType == 'category_hotel'>
										<br/>${result.orderMonitorRst.quantity}间，${result.orderMonitorRst.arrivalDays}晚
                                        </#if>
                                        <#if result.orderMonitorRst.categoryType == 'category_route_hotelcomb'>
												<br/>${result.orderMonitorRst.quantity}份
                                        	</#if>
                                        </td>
	                                    <#else>
	                                    <td></td>
	                                    </#if>
	                                    <td>
	                                    	<#if result.orderMonitorRst.remark ??>
	                                            <a href="javaScript:showCustomRemark('${result.orderMonitorRst.remark}')" title="${result.orderMonitorRst.remark}">查看</a>
	                                        </#if>
	                                    </td>
	                                    <td>${result.auditCreateTime}</td>
	                                    <td>
	                                    	<a href="javaScript:viewNotice('${result.orderMonitorRst.orderId}',${result.orderMonitorRst.orderItemId},'${result.orderMonitorRst.isSupplierOrderItem}');" title="查看">查看</a>
											<#if result.orderMonitorRst.isSupplierOrderItem == "N" && result.orderMonitorRst.orderSubType != "STAMP" && result.orderMonitorRst.ebkFaxCount gt 0>
	                                    	<a href="javaScript:showFaxRecvBynew('${result.orderMonitorRst.orderId}','${result.orderMonitorRst.orderItemId}','${result.orderMonitorRst.certifId}',${result.orderMonitorRst.ebkFaxCount!0});" title="查看回传">查看回传(${result.orderMonitorRst.ebkFaxCount!0})</a>
	                                    	</#if>
                                            <#if result.orderMonitorRst.isSupplierOrderItem == "N" && result.orderMonitorRst.orderSubType != "STAMP" && result.orderMonitorRst.ebkMailCount gt 0>
                                                <a href="javaScript:showMailRecv('${result.orderMonitorRst.orderId}','${result.orderMonitorRst.orderItemId}','${result.orderMonitorRst.certifId}');" title="查看邮件回传">查看邮件回传(${result.orderMonitorRst.ebkMailCount!0})</a>
                                            </#if>
	                                    </td>
	                                	<td width="280">
	                                    	<form action="/vst_order/ord/order/updateOrderItemConfirmStatus.do" method="post">
	                                    		<input type="hidden" name="orderItemId" value=${result.objectId}>
	                                    		<table class="e_table form-inline ">
										            <tbody>
			                                    		<div style="float: left;">
			                                    			<#if result.orderMonitorRst.orderItemConfirmStatus=='SUCCESS'>
			                                    			<div style="display: inline;">
			                                    			<#else>
			                                    			<div style="display: none;">
			                                    			</#if>
			                                    				<input type="text" autocomplete="off" name="confirmId" placeholder="确认号" style="margin: 0 0 3px;"><br>
			                                    			</div>
			                                    			<textarea class="textarea" autocomplete="off" placeholder="备注" name="orderMemo" rows="8" cols="15" maxlength="2000" >${result.orderMonitorRst.orderItemMemo!''}</textarea>
			                                    		</div>
		                                    			<div style="float: left;">
		                                    				<select name="confirmStatus" autocomplete="off" class="w10" style="margin: 0 0 5px;" value="${result.orderMonitorRst.orderItemConfirmStatus!''}" onchange="confirmIdDisplay(this);" >
																<option value="INCONFIRM" <#if result.orderMonitorRst.orderItemConfirmStatus=='INCONFIRM'>selected="selected"</#if>>已审</option>
																<option value="FULL" <#if result.orderMonitorRst.orderItemConfirmStatus=='FULL'>selected="selected"</#if>>订单满房</option>
																<option value="PECULIAR_FULL" <#if result.orderMonitorRst.orderItemConfirmStatus=='PECULIAR_FULL'>selected="selected"</#if>>特殊满房</option>
																<option value="CHANGE_PRICE" <#if result.orderMonitorRst.orderItemConfirmStatus=='CHANGE_PRICE'>selected="selected"</#if>>订单变价</option>
																<option value="SUCCESS" <#if result.orderMonitorRst.orderItemConfirmStatus=='SUCCESS'>selected="selected"</#if>>确认成功</option>
															</select><br>
															<div class="fl operate">
								                				&nbsp;&nbsp;&nbsp;<a class="btn btn_cc1" href="javascript:void(0)" onclick="javascript:updateConfirmStatus(this, '${result.orderMonitorRst.orderItemConfirmStatus!''}')" style="text-decoration:none;cursor:pointer;">保存</a>
								                			</div>	
		                                    			</div>
										            </tbody>
										         </table>
	                                    	</form>
	                                    </td>
                                       	<#if monitorCnd.bespokeOrder !='Y'>
                                        	<td width="210">
	                                        	<div class="fl operate"">                                        		
	                                        		<a class="btn btn_cc1" href="javascript:void(0)" onclick="javascript:showDelayRemindTime('${result.auditId}')" style="text-decoration:none;cursor:pointer;">暂缓</a>
	                                        		<a class="btn btn_cc1" href="javascript:void(0)" onclick="javascript:showCancelOrder('${result.objectId}')" style="text-decoration:none;cursor:pointer;">取消订单</a>
	                                        	</div>
	                                        	<div style="float: left;margin-top: 5px;">
<#--
		                                        		<a class="btn btn_cc1" target="_blank" href="/super_back/ord/sale/OrdSaleAddJump.zul?orderId=${result.orderMonitorRst.orderId}&sysCode=VST" title="售后服务">售后</a>
-->
                                                    <a class="btn btn_cc1" target="_blank" href="/sales_front/ord/sale/toOrdSale/${result.orderMonitorRst.orderId}" title="售后服务">售后</a>
                                                    <a class="btn btn_cc1" href="javaScript:uploadOrderAttachment('${result.orderMonitorRst.orderId}','${result.orderMonitorRst.orderItemId}')" title="上传附件">上传</a>
		                                        		<a class="btn btn_cc1" href="javaScript:showLog('${result.orderMonitorRst.orderItemId}')" title="查看日志">日志</a>
	                                        	</div>
                                            <#if result.orderMonitorRst.categoryType == 'category_hotel' || result.orderMonitorRst.categoryType == 'category_route_hotelcomb'>
                                                <div class="" style="float: left;margin-top: 5px;margin-right: 5px;">
                                                    <a class="btn btn_cc1" title="发送满房邮件" onclick="javaScript:updateConfirmStatusAndSendEmail(this, 'FULL')">发送满房邮件</a>
                                                </div>
                                                <div class="" style="float: left;margin-top: 5px;margin-right: 5px;">
                                                    <a class="btn btn_cc1" title="发送变价邮件" onclick="javaScript:updateConfirmStatusAndSendEmail(this, 'CHANGE_PRICE')">发送变价邮件</a>
                                                </div>
                                            </#if>
                                            <#if (result.orderMonitorRst.categoryType == 'category_hotel' || result.orderMonitorRst.categoryType == 'category_route_hotelcomb') && result.orderMonitorRst.stockFlag != 'all'>
                                                <div style="float: left;margin-top: 5px;">
                                                    <a class="btn btn_cc1" title="电话确认" <#if result.orderMonitorRst.isHandleSupplier == 'Y'>disabled="true"<#else> onclick="javaScript:handleSupplier(this)"</#if>>电话确认</a>
                                                </div>
                                            </#if>
                                            </td>
										</#if>
	                                </tr>
	                        </#list>
	                    </tbody>
	                </table>
	                <#--分页标签-->
	                <@pagination.paging resultPage/>
	        </div>
	    <#else>
	        <div class="no_data mt20"><i class="icon-warn32"></i>暂无相关订单，请重新输入相关条件查询！</div>
	    </#if>
	</#if>
</div>
</div>
<#include "/base/foot.ftl"/>
</body>
<script>
vst_pet_util.commListSuggest("#supplierName", "#supplierId",'/vst_order/ord/order/querySupplierList.do');
$(function(){
	$("#search_button").bind("click",function(){
		if($(this).attr("disabled")) {
				return;
		}
			
		//表单验证
		if(!$("#searchForm").validate().form()){
				return;
		}
		if($("#orderId").val()==""&&$("#supplierId").val()==""&&$("#orderItemId").val()==""){
			alert("必须填写订单号或供应商");
			return;
		}
		if($("#orderId").val()!=""&&!$.isNumeric($("#orderId").val())){
			alert("订单号必须为数字");
			return;
		}
		if($("#orderItemId").val()!=""&&!$.isNumeric($("#orderItemId").val())){
			alert("子订单号必须为数字");
			return;
		}
		$("#result").append("<div class='loading mt20'>正在努力的加载数据中......</div>");
		$(this).attr("disabled", true);
		$(this).css("border","1px solid #aaa").css("background-color","#bbb").css("color", "#000").css("pointer","");
		$("#searchForm").submit();
	});
});
var sendOderFaxDialog;
            $(".sendOderFax").bind("click",function(){
                var form = $(this).parent().parent().prev();
                var orderId = form.find("[name=orderId]").val();
                var orderItemId = form.find("[name=orderItemId]").val();
                var orderMemo = form.find("[name=orderMemo]").val();
            	sendOderFaxDialog = new xDialog("/vst_order/order/orderManage/showManualSendOrderFax.do",{"orderId":orderId,"orderItemId":orderItemId,"orderRemark":orderMemo,"source":"noInfoPass"},{title:"发送凭证",width:600});
            });
            $(".hotel_sendOderFax").bind("click",function(){
                var form = $(this).parent().parent().prev();
				var orderId = form.find("[name=orderId]").val();
				var orderMemo = form.find("[name=orderMemo]").val();
                sendOderFaxDialog = new xDialog("/vst_order/order/orderStatusManage/showManualSendOrderFax.do",{"orderId":orderId,"orderRemark":orderMemo,"source":"noInfoPass"},{title:"发送凭证",width:600});
            });

            //查看ebk、fax、和对接的通知
			var findEbkFaxListDialog;
			function viewNotice(orderId, orderItemId, isSupplierOrderItem){
				if(isSupplierOrderItem=="Y"){
					var param="objectType=ORD_ORDER_ITEM&objectId="+orderItemId+"&sysName=VST";
		            new xDialog("/lvmm_log/bizLog/showVersatileLogList?"+param,{},{title:"查看日志",iframe:true,width:1000,hight:300,iframeHeight:680,scrolling:"yes"});
				}else{
					findEbkFaxListDialog = new xDialog("/vst_certif/ebooking/task/findEbkAndFaxList.do",{"orderId":orderId},{title:"凭证查询",width:1300});
				}
			}
			
			//显示暂缓对话框
			var showDelayRemindTimeDialog;
			function showDelayRemindTime(auditId){
				if(auditId==""){
					alert("参数不能为空");
				}else{
					url="/vst_order/ord/order/confirm/showDelayRemindTimeDialog.do?auditId=" + auditId;
					showDelayRemindTimeDialog = 
						new xDialog(url,
						{},
						{title:"暂缓",width:500,hight:500,scrolling:"yes"});
				}
			}


			//更新子订单确认状态
			function updateCancelMemo(obj){
				var form = $(obj).parents("form").eq(0);
				var orderItemId = form.find("[name=orderItemId]").val();
				var orderMemo = form.find("[name=orderMemo]").val();
				var r=confirm("确定修改订单"+orderItemId+"备注？");
				if (r==true){
					//遮罩层
					var loading = pandora.loading("正在努力保存中...");
					var formData="orderItemId="+orderItemId+"&orderMemo="+orderMemo;
					$.ajax({
						   url : "/vst_order/ord/order/confirm/updateOrderMemo.do",
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

            //其它预订通知-审核通过-订单从工作台消失
            function orderPassOtherAudit(obj, auditId){
                var form = $(obj).parents("td").prev().find("form").eq(0);
                var orderItemId = form.find("[name=orderItemId]").val();
                var orderMemo = form.find("[name=orderMemo]").val();
                var r=confirm("确定订单"+orderItemId+"审核通过？");
                if (r==true){
                    //遮罩层
                    var loading = pandora.loading("正在努力保存中...");
                    var formData="orderItemId="+orderItemId+"&auditId="+auditId+"&orderMemo="+orderMemo;
                    $.ajax({
                        url : "/vst_order/ord/order/confirm/orderPassOtherAudit.do",
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

            //主单预订通知-审核通过-订单从工作台消失
            function orderPassMainAudit(obj, auditId){
                var form = $(obj).parents("td").prev().find("form").eq(0);
                var orderId = form.find("[name=orderId]").val();
                var orderMemo = form.find("[name=orderMemo]").val();
                var r=confirm("确定订单"+orderId+"审核通过？");
                if (r==true){
                    //遮罩层
                    var loading = pandora.loading("正在努力保存中...");
                    var formData="orderId="+orderId+"&orderMemo="+orderMemo+"&auditId="+auditId;
                    $.ajax({
                        url : "/vst_order/ord/order/confirm/orderPassMainAudit.do",
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


            /**
             * 更新子订单确认状态
             * @param obj
             * @param initStatus 页面初始化状态
             */
			function updateConfirmStatus(obj, initStatus){
				var form = $(obj).parents("form").eq(0);
				var orderItemId = form.find("[name=orderItemId]").val();
				var orderMemo = form.find("[name=orderMemo]").val();
                //表单提交时状态
				var updateStatus = form.find("[name=confirmStatus]").val();

                //更新订单备注，状态不变
                if (initStatus == updateStatus) {
                    var r=confirm("确定修改订单"+orderItemId+"备注？");
                    if (r==true){
                        //遮罩层
                        var loading = pandora.loading("正在努力保存中...");
                        var formData="orderItemId="+orderItemId+"&orderMemo="+orderMemo;
                        $.ajax({
                            url : "/vst_order/ord/order/confirm/updateOrderMemo.do",
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
                            var formData="orderItemId="+orderItemId+"&initStatus="+initStatus+"&updateStatus="+updateStatus+"&orderMemo="+orderMemo+"&confirmId="+confirmId;
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

            /**
             * 更新子订单确认状态
             * @param obj
             * @param initStatus 页面初始化状态
             */
            function updateMainOrderMemo(obj) {
                var form = $(obj).parents("form").eq(0);
                var orderId = form.find("[name=orderId]").val();
                var orderMemo = form.find("[name=orderMemo]").val();
                //表单提交时状态
                var updateStatus = form.find("[name=confirmStatus]").val();
                var r = confirm("确定修改订单" + orderId + "备注？");
                if (r == true) {
                    //遮罩层
                    var loading = pandora.loading("正在努力保存中...");
                    var formData = "orderId=" + orderId + "&orderMemo=" + orderMemo;
                    $.ajax({
                        url: "/vst_order/ord/order/confirm/updateMainOrderMemo.do",
                        data: formData,
                        type: "POST",
                        dataType: "JSON",
                        success: function (result) {
                            if (result.code == "success") {
                                loading.close();
                                alert(result.message);
                                document.location.reload();
                            } else {
                                loading.close();
                                alert(result.message);
                                document.location.reload();
                            }
                        },
                        error: function (XMLHttpRequest, textStatus, errorThrown) {
                            loading.close();
                            if (textStatus == 'timeout') {
                                alert("程序运行超时");
                                document.location.reload();
                            } else {
                                alert("程序运行出现异常");
                                document.location.reload();
                            }
                        }
                    });
                }
            }
            
            //取消确认
			function orderCancelConfirm(orderItemId,auditId){
				if(auditId=="" || orderItemId==""){
					alert("参数不能为空");
				}
				var r=confirm("通过订单"+orderItemId+"取消确认？");
				if (r==true){
					//遮罩层
					var loading = pandora.loading("正在努力保存中...");
					var formData="orderItemId=" + orderItemId + "&auditId="+auditId;
					$.ajax({
						   url : "/vst_order/ord/order/confirm/orderCancelConfirm.do",
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
			
			//重新发送通知
			function resendNotification(orderItemId){
				if(orderItemId==""){
					alert("订单号不能为空");
					return;
				}
				var r=confirm("确定订单"+orderItemId+"审核通过？");
				if (r==true){
					//遮罩层
					var loading = pandora.loading("正在努力保存中...");
					var formData="orderItemId="+orderItemId;
					$.ajax({
						   url : "/vst_order/ord/order/confirm/resendNotification.do",
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
			

			//询位库 审核通过
			function orderPassInquiryAudit(obj, auditId){
                var form = $(obj).parents("td").prev().find("form").eq(0);
                var orderItemId = form.find("[name=orderItemId]").val();
                var orderMemo = form.find("[name=orderMemo]").val();

				if(orderItemId==""){
					alert("订单号不能为空");
					return;
				}
				var r=confirm("确定订单"+orderItemId+"审核通过？");
				if (r==true){
					//遮罩层
					var loading = pandora.loading("正在努力保存中...");
					var formData="orderItemId="+orderItemId+"&orderMemo="+orderMemo+"&auditId="+auditId;
					$.ajax({
						   url : "/vst_order/ord/order/confirm/orderPassInquiryAudit.do",
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
			
			//取消订单
			var showCancelOrderDialog;
			function showCancelOrder(orderItemId){
				if(orderItemId==""){
					alert("订单号不能为空");
				}else{
					url="/vst_order/ord/order/confirm/showCancelOrderDialog.do?orderItemId=" + orderItemId;
					showCancelOrderDialog = 
						new xDialog(url,
						{},
						{title:"取消订单",width:500,hight:800,scrolling:"yes"});
				}
			}
			
			var showFaxRecvDialog;
			function showFaxRecv(orderId, orderItemId, certifId){
				if(orderId==""||orderItemId==""||certifId==""){
					alert("订单号不能为空");
				}else{
					url="/vst_order/ord/order/confirm/showFaxRecvDialog.do?orderId=" + orderId + "&orderItemId=" + orderItemId+ "&certifId=" + certifId;
					showFaxRecvDialog = 
						new xDialog(url,
						{},
						{title:"查看回传",width:1120,hight:800,scrolling:"yes"});
				}
			}
			
			function showFaxRecvBynew(orderId, orderItemId, certifId,num){
				if(orderId==""||orderItemId==""||certifId==""){
					alert("订单号不能为空");
				}else{
					if(num>1){
						url="/vst_certif/ebooking/faxRecv/findEbookingFaxRecvList.do?orderId="+orderId+"&readUserStatus=Y&source=orderHotelDetails";
						showFaxRecvDialog = 
						new xDialog(url,
						{},
						{title:"查看回传",width:600,scrolling:"yes"});
					}else{
						url="/vst_order/ord/order/confirm/showFaxRecvDialog.do?orderId=" + orderId + "&orderItemId=" + orderItemId+ "&certifId=" + certifId;
						showFaxRecvDialog = 
						new xDialog(url,
						{},
						{title:"查看回传",width:1120,hight:800,scrolling:"yes"});
					}
					
				}
			}
			
            function showMailRecv(orderId, orderItemId, certifId){
                window.open("/vst_certif/ebooking/mailRecv/findEbookingMailRecvList.do?orderId="+orderId);
            }
			//显示客人备注
			function showCustomRemark(remark){
				$.alert(remark);
			}
			
			//查看供应商
			var viewSupplierDialog;
			function viewSupplier(supplierId, suppGoodsId){
				viewSupplierDialog = new xDialog("/vst_back/supp/supplier/showViewDistrict.do",{"supplierId":supplierId,"suppGoodsId":suppGoodsId},{title:"查看供应商",width:800});
			}
			
			//查看日志
			function showLog(orderItemId){
				var param="objectType=ORD_ORDER_ITEM&objectId="+orderItemId+"&sysName=VST";
	            new xDialog("/lvmm_log/bizLog/showVersatileLogList?"+param,{},{title:"查看日志",iframe:true,width:1000,hight:300,iframeHeight:680,scrolling:"yes"});
			}
			//定义上传附件弹出窗口变量
		    var uploadOrderAttachmentDialog;
		    //定义查看附件弹出窗口变量
		    var viewOrderAttachmentDialog;
			//上传附件
			function uploadOrderAttachment(orderId, orderItemId){
				data={"orderId":orderId,"orderItemId":orderItemId,orderType:"child"};
				uploadOrderAttachmentDialog = new xDialog(
						"/vst_order/ord/order/intoUploadOrderAttachmentPage.do",//进入上传附件页面
						data,//传递订单ID
						{title:"上传订单普通附件",width:600}//设置弹出窗口样式
						);
			}
			
			//设置选中行颜色变化
			function tronmousedown(obj){
				if(typeof(trs)!="undefined"&&trs!=null){
					for(var o=0; o<trs.length; o++){
					     if(trs[o] == obj){
					      trs[o].style.backgroundColor = '#fce6a2';
					     }else{
					      trs[o].style.backgroundColor = '';
					     }
				    }
				}
   			}

   			function handleSupplier(obj) {
                var form = $(obj).parents("td").prev().find("form").eq(0);
                var orderItemId = form.find("[name=orderItemId]").val();
                var orderMemo = form.find("[name=orderMemo]").val();
                var r=confirm("订单"+orderItemId+"电话已确认？");
                if (r==false){
                    return;
                }
                var formData="orderItemId="+orderItemId+"&orderMemo="+orderMemo;
                //遮罩层
                var loading = pandora.loading("正在努力保存中...");
				$.ajax({
                    url : "/vst_order/ord/order/confirm/handleSupplier.do",
					type:"POST",
					dataType:"JSON",
                    data: formData,
					success : function(result){
                        if(result.code=="success" ){
                            loading.close();
                            document.location.reload();
                        }else {
                            loading.close();
                            alert(result.message);
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

   			function handleSupplierForNewOrder(orderItemId) {
                var r=confirm("订单"+orderItemId+"电话已确认？");
                if (r==false){
                    return;
                }
                var formData="orderItemId="+orderItemId;
                //遮罩层
                var loading = pandora.loading("正在努力保存中...");
				$.ajax({
                    url : "/vst_order/ord/order/confirm/handleSupplier.do",
					type:"POST",
					dataType:"JSON",
                    data: formData,
					success : function(result){
                        if(result.code=="success" ){
                            loading.close();
                            document.location.reload();
                        }else {
                            loading.close();
                            alert(result.message);
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

   			function updateConfirmStatusAndSendEmail(obj, confirmStatus) {
                var form = $(obj).parents("td").prev().find("form").eq(0);
                var orderItemId = form.find("[name=orderItemId]").val();
                var orderMemo = form.find("[name=orderMemo]").val();
                var confirmId = form.find("[name=confirmId]").val();
                var re = /^[0-9A-Za-z]*$/;
                var isOk=true; //参数验证是否ok
                if(orderItemId==""||confirmStatus==""){
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
                    var r=confirm("是否确认发送？");
                    if (r==true){
                        //遮罩层
                        var loading = pandora.loading("正在努力保存中...");
                        var formData="orderItemId="+orderItemId+"&confirmStatus="+confirmStatus+"&orderMemo="+orderMemo+"&confirmId="+confirmId;
                        $.ajax({
                            url : "/vst_order/ord/order/confirm/updateConfirmStatusAndSendEmail.do",
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

   			function sendEmail(obj, confirmStatus) {
                var form = $(obj).parents("td").prev().find("form").eq(0);
                var orderItemId = form.find("[name=orderItemId]").val();
                var orderMemo = form.find("[name=orderMemo]").val();
                var r=confirm("是否确认发送？");
                if (r==true){
                    //遮罩层
                    var loading = pandora.loading("正在努力保存中...");
                    var formData="orderItemId="+orderItemId+"&confirmStatus="+confirmStatus+"&orderMemo="+orderMemo;
                    $.ajax({
                        url : "/vst_order/ord/order/confirm/notifyManager.do",
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
</script>
</html>