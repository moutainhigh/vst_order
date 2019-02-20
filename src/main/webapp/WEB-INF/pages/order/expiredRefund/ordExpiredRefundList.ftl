<#assign voa=JspTaglibs["/WEB-INF/pages/tld/vstOrgAuthentication-tags.tld"]>
<#--页眉-->
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-门票过期退</title>
<#include "/base/head_meta.ftl"/>
</head>
<body>
<#import "/base/paginationMonitor.ftl" as pagination>
<#--页面导航-->
<div class="iframe_header">
	<i class="icon-home ihome"></i>
	<ul class="iframe_nav">
		<li><a href="#">首页</a> &gt;</li>
		<li><a href="#">订单管理</a> &gt;</li>
		<li class="active">门票过期退列表</li>
	</ul>
</div>

<#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" action="/vst_order/order/expiredRefund/list.do" method="post">
        <table class="s_table2 form-inline">
            <tbody> 
                <tr>
                    <td colspan="2">
                        <label>&nbsp;产品名称：<@s.formInput "erCnd.productName" 'class="14"'/></label>
                        <label>产品ID：<@s.formInput "erCnd.productId" 'class="w14" number="true"'/></label>
                    	<label>销售渠道：<@s.formSingleSelect "erCnd.distChnlId" distChnlMap 'class="w17"'/></label>
                    	<label>订单号：<@s.formInput "erCnd.orderId" 'class="w14" number="true"'/></label>
                    </td>
                 </tr>  
                 <tr> 
                    　                             <td colspan="2">
	                    <label>供应商名称：<@s.formInput "erCnd.supplierName" 'class="14"'/></label> 
	                    <label>商品ID：	<@s.formInput "erCnd.suppGoodsId" 'class="w14" number="true"'/></label> 
	                    <label>处理状态：<@s.formSingleSelect "erCnd.processStatus" processStatusMap 'class="w17"'/></label>
                    </td>
                </tr> 
            </tbody>
        </table>
               
        <div class="operate mt20" style="text-align:right; margin-right: 180px;">
	        <a class="btn btn_cc1" id="search_button">查询</a>
		    <a class="btn btn_cc1" id="refresh_button">刷新</a>
        </div>
	</form>
</div>

<#--结果显示-->
<div id="result" class="iframe_content mt20">
<#if resultPage?? >
	<#if resultPage.items?size gt 0 >
			<div class="p_box">
				<table class="p_table table_center">
					<thead>
						<tr>
							<th nowrap="nowrap" style="width:5%;">主订单号</th>
							<th nowrap="nowrap" style="width:5%;">子订单号</th>
							<th nowrap="nowrap" style="width:5%;">产品ID</th>
							<th nowrap="nowrap" style="width:16%;">产品名称</th>
							<th nowrap="nowrap" style="width:5%;">商品ID</th>
							<th nowrap="nowrap" style="width:16%;">商品名称</th>
							<th nowrap="nowrap" style="width:10%;">供应商</th>
							<th nowrap="nowrap" style="width:8%;">出游日期</th>
							<th nowrap="nowrap" style="width:8%;">下单渠道</th>
							<th nowrap="nowrap" style="width:8%;">过期退状态</th>
							<th nowrap="nowrap" style="width:3%;">废码日志</th>
							<th nowrap="nowrap" style="width:8%;">操作</th>
						</tr>
					</thead>
					<tbody>
						<#list resultPage.items as result>
							<tr>
								<td>
								    <a title="点击查看订单详情" href="/vst_order/order/ordCommon/showOrderDetails.do?orderId=${result.orderId}" target="_blank">
										${result.orderId}
									</a>
								</td>
								<td>${result.orderItemId}</td>
								<td>${result.productId}</td>
								<td>
								    ${result.productName} 
								    <!--<a title="点击查看产品详情" href="/scenic_back/singleTicket/prod/product/showProductMaintain.do?categoryId=11&productId=2987718&categoryName=%20%20%20%20&isView=Y" target="_blank"></a> -->
								</td>
								<td>${result.suppGoodsId}</td>
								<td>${result.suppGoodsName}</td>
								<td>${result.supplierName}</td>
								<td>${result.visitTime}</td>
								<td>${result.distChnlView}</td>
								<td>${result.processStatusView}</td>
								<td>
								    <#if result.codeId != -1>
								        <a href="javascript:void(0);" class="showERLog" data=${result.codeId} param='objectId=${result.codeId}&objectType=PASS_CODE&sysName=VST'>查看日志</a>
                                   </#if>
								<td>
								    <#if result.processStatus == 4>
								        <a class="mr10" id="desCode${result.orderItemId!''}" href="javascript:destroyCodeNew(${result.orderItemId!''});" data="${result.orderItemId!''}">人工废码</a>
								    </#if>
								</td>
							</tr>
						</#list>
					</tbody>
				</table>
				
				<#--分页标签-->
				<@pagination.paging resultPage/>
		</div>
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关过期退数据，请重新输入相关条件查询！</div>
	</#if>
</#if> 
</div>

<#--页脚-->
<#include "/base/foot.ftl"/>
</body>
</html>

<#--js脚本-->
<script type="text/javascript">
	$(function() {
	
		// 查询
		$("#search_button").bind("click",function() {
			if($(this).attr("disabled")) {
				return;
			}
			
			$("#result").empty();
			
			// 去除input空格
			var tempTextFields = $("input[type=text]");
			$.each(tempTextFields,function(){
			 	this.value = $.trim(this.value);
			});
			
			//表单验证
			if(!$("#searchForm").validate().form()){
				return;
			}
			
			//加载数据中
			$("#result").append("<div class='loading mt20'>正在努力的加载数据中......</div>");
		
			$(this).attr("disabled", true);
			$(this).css("border","1px solid #aaa").css("background-color","#bbb").css("color", "#000").css("pointer","");
			$("#searchForm").submit();
		});
		
		// 刷新
		$("#refresh_button").bind("click",function() {
			if ($(this).attr("disabled")) {
				return;
			} else {
			    $.ajax({
				    url: "/vst_order/order/expiredRefund/refresh.do",
				    data: null,
				    type: "POST",
				    dataType: "JSON",
				    beforeSend: function () {
				    	$("#refresh_button").css({"background":"#888888"});
				        $("#refresh_button").attr("disabled", true);
				    },
				    success : function(data) {
				    	if (data == 1) {
				    		$.alert("刷新成功！");
				    	} else if (data == 0) {
				    		$.alert("后台刷新处理中, 请稍后重试！");  
				    	} else {
					    	$.alert("系统内部错误：刷新异常，请稍后重试！");
				    	}
				    },
				    complete: function () {
				    	$("#refresh_button").css({"background":"#4D90FE"});
				        $("#refresh_button").removeAttr("disabled");
				    },
				    error: function(XMLHttpRequest, textStatus, errorThrown) {
				    	$(this).attr("disabled", false);
				    	$(this).css({"background":"#4D90FE"});
				    	$.alert("异常：请检查网络连接或稍后尝试！");
				    }
				});
			}
		});
		
		// 查看日志
		$("a.showERLog").live("click", function() {
		    var param = $(this).attr("param");
			new xDialog("/lvmm_log/bizLog/showVersatileLogList?"+param,{},{title:"查看日志",iframe:true,width:1000,hight:300,iframeHeight:680,scrolling:"yes"});
		});
		
	});
	
	// 按enter键提交查询
	$(document).keyup(function(event) {
	    if(event.keyCode ==13) {
	        $("#search_button").trigger("click");
	    }
	});
	
	// 人工废码NEW
	function destroyCodeNew(orderItemId) {
        if (confirm("确定人工废码？")) {
			$.ajax({
	        	url: "/vst_order/order/expiredRefund/destroyCode.do?orderItemId=" + orderItemId,
	            type: "post",
	            async: true,
	            success: function (result) {
	                if (result == "DESTROYED_SUCCESS") {
	                    pandora.dialog({wrapClass: "dialog-mini", content: "废码成功！", okValue: "确定", ok: function () {
	                	    $("#searchForm").submit();
                        }});
	                } else if (result == "DESTROYED_FAILED") {
	                	$.alert("废码失败！");
	                } else if (result == "DESTROYED_AUDIT") {
	                	$.alert("废码审核中！");
	                } else if (result == "-1") {
	            		$.alert("废码子订单号有误，无法废码！");
	            	} else if (result == "-2") {
	            		$.alert("废码失败，请稍后再试！");
	            	} else if (result == "-3") {
	            		$.alert("废码成功，但更新意向单状态失败！");
	            	} else if (result == "" || result == null) {
	            		$.alert("未查询到申码记录或状态不对不能废码！");
	            	} else {
	            		$.alert("系统未知错误！");
	            	}
	            }
        	});
		}
	}
	
	// 人工废码
	function destroyCode(codeId) {
        if (confirm("确认人工废码?")) {
			$.ajax({
	        	url: "/vst_passport/passCode/destroyCode.do?codeId="+codeId,
	            type: "post",
	            async: false,
	            success: function (result) {
	            	if (result.code=="SUCCESS") {
	            		alert("废码处理中："+result.message);
	            	} else {
	            		alert("废码失败："+result.message);
	            		return;
	            	}
	            }
        	});
		}
	}
</script>
