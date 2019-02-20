<#import "/base/spring.ftl" as s/>
<#import "/base/pagination.ftl" as pagination>
<!DOCTYPE html>
<html>
	<head>
		<#include "/base/head_meta.ftl"/>
            <link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/lv/dialog.css">
            <!-- 新添加&ndash;&gt;&ndash;&gt;-->
            <link rel="stylesheet" href="http://pic.lvmama.com/styles/backstage/vst/gallery/v1/resources.css">
            <link rel="stylesheet" href="http://pic.lvmama.com/styles/backstage/v1/pandora-calendar.css"/>
            <link rel="stylesheet" href="http://pic.lvmama.com/styles/backstage/v1/sales-information-iframe.css"/>
	</head>
	<body>
	<div style="text-align:right;">
        <p>当前状态：<span id="workStatusSpan" class="cc6 f14"></span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</p>
    <div>
    	
	    <div class="iframe_content">
	    	<table class="s_table">
			<tbody>
				<tr>
                    <td class="w8 s_label"><span class="notnull">*</span>订单负责人：</td>
                    <td class="w10">
                    	<@s.formInput "monitorCnd.operatorName" 'class="w10" readonly="true"  required="true"'/>
                    </td>
                    <td  class="w9"><a href="javascript:updateOrderUser()">修改订单负责人</a> </td>
                    <td  class="w9"><a class="btn btn_cc1" id="search_button_change">切换</a></td>
                </tr>
			</tbody>
		</table>
	    	<div style="margin: 10px 0 10px 0; text-align:left;">
	    		<div class="fl operate">
                    <a class="btn btn_cc1" id="hotel" href="javascript:switchMainTab('HOTEL')">酒店（${hotelNum!0}）</a>
                    <a class="btn btn_cc1" id="main" href="javascript:switchMainTab('MAIN')">主单预订通知（${mainNum!0}）</a>
                    <br/><br/>
                    <#if mainTab!='MAIN'>
                        <a class="btn btn_cc1" id="inconfirm" href="javascript:switchTheTab('INCONFIRM_AUDIT')">已审库（${inconfirmAuditNum!0}）</a>
                        <a class="btn btn_cc1" id="inconfirmBack" href="javascript:switchTheTab('INCONFIRM_BACK_AUDIT')">已回传库（${inconfirmBackAuditNum!0}）</a>
                        <a class="btn btn_cc1" id="full" href="javascript:switchTheTab('FULL_AUDIT')">订单满房库（${fullAuditNum!0}）</a>
                        <a class="btn btn_cc1" id="peculiarFull" href="javascript:switchTheTab('PECULIAR_FULL_AUDIT')">特殊满房库（${peculiarFullAuditNum!0}）</a>
                        <a class="btn btn_cc1" id="changePrice" href="javascript:switchTheTab('CHANGE_PRICE_AUDIT')">变价库（${changePriceAuditNum!0}）</a>
                        <a class="btn btn_cc1" id="cancel" href="javascript:switchTheTab('CANCEL_CONFIRM_AUDIT')">取消确认库（${cancelAuditNum!0}）</a>
                        <a class="btn btn_cc1" id="newOrder" href="javascript:switchTheTab('NEW_ORDER_AUDIT')">新单库（${newOrderAuditNum!0}）</a>
                        <a class="btn btn_cc1" id="inquiryOrder" href="javascript:switchTheTab('INQUIRY_AUDIT')">询位库（${inquiryAuditNum!0}）</a>
                        <a class="btn btn_cc1" id="otherOrder" href="javascript:switchTheTab('CONFIRM_OTHER_AUDIT')">其它预订通知（${confirmOtherAuditNum!0}）</a>
                        <!--增加库存已审库按钮与链接 弹出新页面 -->
                    </#if>
                </div>
		    </div>
            <br>
        <#if mainTab!='MAIN'>
            <#--表单区域-->
            <#if checkedTab=='INCONFIRM_AUDIT' || checkedTab=='CONFIRM_OTHER_AUDIT' || checkedTab=='INCONFIRM_BACK_AUDIT'>
                <div class="iframe_search" style="display: none;">
            <#else>
                <div class="iframe_search">
            </#if>
            <form id="searchForm" action="/vst_order/ord/order/confirm/queryDestTaskList.do" method="post">
                <input type="hidden" id="checkedTab" name="checkedTab" value="${checkedTab!''}"/>
                <input type="hidden" id="newoperatorName" name="operatorName" value="${operatorName!''}"/>
                <input type="hidden" id="mainCheckedTab" name="mainCheckedTab" value="${mainCheckedTab!''}"/>
                <input type="hidden" id="mainTab" name="mainTab" value="${mainTab!''}"/>
                <table class="s_table">
                    <tbody>
                        <tr>
                            <td class="w8 s_label">订单号：</td>
                            <td class="w15">
                                <input type="text" name="orderItemId" id="orderItemId" value="${monitorCnd.orderItemId}" />
                            </td>
                            <td class="s_label">
                                <label>房间类型：
                                    <@s.formSingleSelect "monitorCnd.stockFlag" stockFlagMap 'class="w10"'/>
                                </label>
                            </td>
                            <td>
                                <div class="operate">
                                    &nbsp;&nbsp;<a class="btn btn_cc1" id="search_button">查询</a>
                                    <a class="btn btn_cc1" id="clear_button">清空</a>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <div id="errorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>请至少输入一个查询条件！</div>
            </form>
            </div>
            <br/><br/>
            <#if checkedTab=='INCONFIRM_AUDIT' >
                <#include "/order/confirm/library/inconfirm.ftl"/>
            <#elseif checkedTab=='INCONFIRM_BACK_AUDIT'>
                <#include "/order/confirm/library/inconfirmback.ftl"/>
            <#elseif checkedTab=='FULL_AUDIT' || checkedTab=='PECULIAR_FULL_AUDIT' || checkedTab=='CHANGE_PRICE_AUDIT'>
                <#include "/order/confirm/library/unconfirm.ftl"/>
            <#elseif checkedTab=='CANCEL_CONFIRM_AUDIT'>
                <#include "/order/confirm/library/cancel.ftl"/>
            <#elseif checkedTab=='NEW_ORDER_AUDIT'>
                <#include "/order/confirm/library/newOrder.ftl"/>
            <#elseif checkedTab=='INQUIRY_AUDIT'>
                <#include "/order/confirm/library/inquiryOrder.ftl"/>
            <#elseif checkedTab=='CONFIRM_OTHER_AUDIT'>
                <#include "/order/confirm/library/otherOrder.ftl"/>
            <#else>
                <#include "/order/confirm/library/inconfirm.ftl"/>
            </#if>
        <#else>
            <!--<input type="hidden" id="operatorName" name="operatorName" value="${operatorName!''}"/>-->
            <#include "/order/confirm/library/mainOrder.ftl"/>
        </#if>
		<#--结果显示-->
		<#include "/base/foot.ftl"/>
		<#--js脚本-->
		<script type="text/javascript" src="/vst_order/js/jquery.rotate.1-1.js"></script>
		<script type="text/javascript" src="/vst_order/js/jquery.mousewheel.js"></script>
        <script src="http://pic.lvmama.com/min/index.php?f=/js/backstage/v1/common.js,/js/lv/dialog.js,/js/lv/calendar.js"></script>
        <script src="http://pic.lvmama.com/min/index.php?f=/js/backstage/vst/gallery/v1/gallery-backstage/display.js"></script>
        <!--新添加-->
        <script src="http://pic.lvmama.com/js/backstage/vst/gallery/v1/resources.js"></script>
        <script type="text/javascript" src="/vst_order/js/lvmama-dialog.js"></script>
		
		<script>
			function loadUserWorkStatus(){
				$.ajax({
					url : "/vst_order/ord/order/queryEmployeeWorkStatus.do",
					type : "post",
					dataType : 'json',
					data : {"operatorName":$("#operatorName").val()},
					success : function(result) {
						if(!result.success){
		 					$.alert(result.message);
		 				}else{
		 					$("#workStatusSpan").html(result.attributes.workstatus);
		 				}
					}
				});
			}
			var taskListTable=document.getElementById('taskListTable');
			var trs;
			$(function(){
				//不同活动tab选择效果
				var checkedTab = '${checkedTab!''}';
				var mainTab = '${mainTab!''}';
				if(checkedTab!=""){
					if(checkedTab=="INCONFIRM_AUDIT"){
						$("#inconfirm").css("background","#ffffff none repeat scroll 0 0");
						$("#inconfirm").css("color","red");

                        //处理中和暂缓选择效果
                        var bespokeOrder = '${monitorCnd.bespokeOrder!''}';
                        if(bespokeOrder==""){
                            $("#pending").css("background","#ffffff none repeat scroll 0 0");
                            $("#pending").css("color","red");
                        }else if(bespokeOrder=="Y"){
                            $("#delay").css("background","#ffffff none repeat scroll 0 0");
                            $("#delay").css("color","red");
                        }
					}else if(checkedTab=="INCONFIRM_BACK_AUDIT"){
						$("#inconfirmBack").css("background","#ffffff none repeat scroll 0 0");
						$("#inconfirmBack").css("color","red");
					}
					else if(checkedTab=="FULL_AUDIT"){
						$("#full").css("background","#ffffff none repeat scroll 0 0");
						$("#full").css("color","red");
					}else if(checkedTab=="PECULIAR_FULL_AUDIT"){
						$("#peculiarFull").css("background","#ffffff none repeat scroll 0 0");
						$("#peculiarFull").css("color","red");
					}else if(checkedTab=="CHANGE_PRICE_AUDIT"){
						$("#changePrice").css("background","#ffffff none repeat scroll 0 0");
						$("#changePrice").css("color","red");
					}else if(checkedTab=="CANCEL_CONFIRM_AUDIT"){
						$("#cancel").css("background","#ffffff none repeat scroll 0 0");
						$("#cancel").css("color","red");
					}else if(checkedTab=="NEW_ORDER_AUDIT"){
						$("#newOrder").css("background","#ffffff none repeat scroll 0 0");
						$("#newOrder").css("color","red");
					}else if(checkedTab=="INQUIRY_AUDIT"){
                        $("#inquiryOrder").css("background","#ffffff none repeat scroll 0 0");
                        $("#inquiryOrder").css("color","red");
					}else if(checkedTab=="CONFIRM_OTHER_AUDIT"){
                        $("#otherOrder").css("background","#ffffff none repeat scroll 0 0");
                        $("#otherOrder").css("color","red");
                    }
				}
				if (mainTab != 'MAIN') {
                    $("#hotel").css("background","#ffffff none repeat scroll 0 0");
                    $("#hotel").css("color","red");
                } else {
                    $("#main").css("background","#ffffff none repeat scroll 0 0");
                    $("#main").css("color","red");
                }
				
				//设置选中行颜色变化
				if(typeof(taskListTable)!="undefined"&&taskListTable!=null){
					trs = taskListTable.getElementsByTagName('tr'); 
					for(var i=0; i<trs.length; i++){
						trs[i].onmousedown = function(){
							tronmousedown(this);
						}
					}
				}
				loadUserWorkStatus();
				$("#search_button_change").bind("click",function (){
					window.location.href = "/vst_order/ord/order/confirm/selectMainCheckedTab.do?mainCheckedTab=MYDESTTASK&operatorName="+$("#operatorName").val();
				});
			});
			
			$(function() {
				//搜索
				$("#search_button").click(function(){
					queryDestTaskList();
				});
				//清空
				$("#clear_button").bind("click",function(){
					$("#bespokeOrder").val("");
					$("#orderItemId").val("");
					$("#stockFlag").val("");
					return;
				});
			});
						
			function queryDestTaskList(){
				var checkedTab = '${checkedTab}';
				var mainCheckedTab = '${mainCheckedTab}';
				var bespokeOrder = '${monitorCnd.bespokeOrder}';
				if(checkedTab=="INCONFIRM_AUDIT"){
					window.location.href = "/vst_order/ord/order/confirm/queryDestTaskList.do?checkedTab=" + checkedTab + "&bespokeOrder=" + bespokeOrder + "&mainCheckedTab=" + mainCheckedTab+"&operatorName="+$("#operatorName").val();
				}else{
					var re = /^[1-9]+[0-9]*]*$/;
					var orderItemId = $("#orderItemId").val();
					if(orderItemId!=""){
						if(orderItemId.length>11){
							$.alert("订单号不能超过11位");
							return;
						}else if(!re.test(orderItemId)){
							$.alert("订单号只能为整数");
							return;
						}else{
							$("#searchForm").submit();
						}
					}else{
						$("#searchForm").submit();
					}
				}
			}

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
			//切换tab
			function switchTheTab(tab){
				var isDelay="N";
				var mainCheckedTab = '${mainCheckedTab}';
                var mainTab = '${mainTab}';
				if(tab!=""){
					var bespokeOrder = '${monitorCnd.bespokeOrder!''}';
                    var operatorName = $("#operatorName").val();
					window.location.href = "/vst_order/ord/order/confirm/queryDestTaskList.do?checkedTab=" + tab +"&isDelay=" + isDelay +"&bespokeOrder=" + bespokeOrder+"&mainCheckedTab=" + mainCheckedTab+"&mainTab=" + mainTab+"&operatorName="+operatorName;
				}
			}

			//切换tab
			function switchMainTab(mainTab){
				var isDelay="N";
				var mainCheckedTab = '${mainCheckedTab}';
				var tab = 'INCONFIRM_AUDIT';
				if(tab!=""){
					var bespokeOrder = '${monitorCnd.bespokeOrder!''}';
                    var operatorName =$("#operatorName").val();
					window.location.href = "/vst_order/ord/order/confirm/queryDestTaskList.do?checkedTab=" + tab +"&isDelay=" + isDelay +"&bespokeOrder=" + bespokeOrder+"&mainCheckedTab=" + mainCheckedTab+"&mainTab=" + mainTab+"&operatorName="+operatorName;
				}
			}

			//切换处理中和暂缓
			function switchDelay(bespokeOrder){
				if(bespokeOrder=="" || bespokeOrder=="Y"){
					var checkedTab = '${checkedTab}';
					var mainCheckedTab = '${mainCheckedTab}';
					var operatorName = $("#operatorName").val();
					window.location.href = "/vst_order/ord/order/confirm/queryDestTaskList.do?checkedTab=" + checkedTab+"&bespokeOrder=" + bespokeOrder+"&mainCheckedTab=" + mainCheckedTab+"&operatorName="+operatorName;
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

                var param={"orderItemId":orderItemId,"orderMemo":orderMemo,"auditId":auditId};
                retentionTimeDialog = new xDialog("/vst_order/ord/order/confirm/showUpdateRetentionTime.do?",param,{title:"修改资源保留时间",width:600});
                return;

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
            $("a[data-tag='queryHotelInfo']").off("click");
			$("a[data-tag='queryHotelInfo']").on("click",closeHouse);
			//关房操作
            var closeHouseDialog;
            function closeHouse(){
                var orderItemId=$.trim($(this).parent().parent().find("a[data-tag='orderItemId']").text());
                var url="/vst_order/ord/order/confirm/queryHotelInfo.do?orderItemId="+orderItemId+"&checkedTab=${checkedTab}";
                closeHouseDialog = new xDialog(url,{},{title:"手动关房",iframe:true,width:"1000",height:"600"});
                console.log(closeHouseDialog);
            }
            //关闭关房弹框
            function clsoeClsoeHouseDialog() {
                closeHouseDialog.close();
            }
		</script>
	</body>
</html>