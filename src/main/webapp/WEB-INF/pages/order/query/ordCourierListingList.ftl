<#--页眉-->
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-订单监控</title>
<#include "/base/head_meta.ftl"/>
</head>
<body>
<#import "/base/pagination.ftl" as pagination>
<#--页面导航-->
<div class="iframe_header">
	<i class="icon-home ihome"></i>
	<ul class="iframe_nav">
		<li><a href="#">首页</a> &gt;</li>
		<li><a href="#">订单管理</a> &gt;</li>
		<li class="active">订单列表</li>
	</ul>
</div>

<#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" action="/vst_order/ord/order/ordCourierListingList.do" method="post">
        <table class="s_table2 form-inline">
            <tbody>
                <tr>
                	<td class="s_table2_t" width="14" rowspan="3">订单信息</td>
                    <td width="" colspan="2">
                    	<label>寄件方：<@s.formSingleSelect "monitorCnd.expresstype" expresstypeMap 'class="w14"'/></label>
                    	<label>主订单号：<@s.formInput "monitorCnd.orderId" 'class="w9" number="true"'/></label>
                    	
                    	<label>子单BU：<@s.formSingleSelect "monitorCnd.belongBU" belongBUMap 'class="w14"'/></label>
                        <label>发货状态：<@s.formSingleSelect "monitorCnd.courierStatus" courierStatusMap 'class="w14"'/></label>
                        
                    	</br>
                        <label>下单时间：
                        	<input id="d4321" class="Wdate" type="text" value="${monitorCnd.createTimeBegin}" 
                        	onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:00'})" errorele="selectDate" name="createTimeBegin" readonly="readonly">
	                    	 -- 
	                    	 <input id="d4322" class="Wdate" type="text" value="${monitorCnd.createTimeEnd}" 
	                    	 onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:59',maxDate:'#F{$dp.$D(\'d4321\',{y:2});}',readOnly:true,minDate:'#F{$dp.$D(\'d4321\',{d:0});}'})"
	                    	  errorele="selectDate" name="createTimeEnd">
	                    </label> 
	                    <label>游玩时间：
                        	<@s.formInput "monitorCnd.visitTimeBegin" 'class="w10" onClick="WdatePicker({readOnly:true})"'/>
	                    	-- 
	                    	<@s.formInput "monitorCnd.visitTimeEnd" 'class="w10" onClick="WdatePicker({readOnly:true})"'/>
                        </label>
                    </td>
                 </tr>
                
            </tbody>
        </table>
            
        <div class="operate mt20" style="text-align:center">
				<a class="btn btn_cc1" id="search_button">查询</a>
				<a class="btn btn_cc1" id="clear_button">清空</a>
				<a class="btn btn_cc1" id="export_button">导出</a>
        </div>
        <div id="errorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>请至少输入一个查询条件！</div>
        <div id="errorData" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>不能导出超过200行的数据,请通过查询条件过滤！</div>
	</form>
</div>

<#--结果显示-->
<div id="result" class="iframe_content mt20">
<#if pageParam?? >
	<#if pageParam.items?size gt 0 >
			<div class="p_box">
				<table class="p_table table_center">
					<thead>
						<tr>
							<th nowrap="nowrap" style="width:10%;">订单号</th>
							<th nowrap="nowrap" style="width:10%;">子订单号</th>
							<th nowrap="nowrap" style="width:30%;">产品名称</th>
							<th nowrap="nowrap" style="width:5%;">订购份数</th>
							<th nowrap="nowrap" style="width:5%;">收件人</th>
							<th nowrap="nowrap" style="width:10%;">联系电话</th>
							<th nowrap="nowrap" style="width:30%;">寄件地址</th>
							<th nowrap="nowrap" style="width:10%;">邮编号码</th>
							<th nowrap="nowrap" style="width:10%;">子单BU</th>
							<th nowrap="nowrap" style="width:10%;">发货状态</th>
							<th nowrap="nowrap" style="width:10%;">操作</th>
						</tr>
					</thead>
					<tbody>
						
						<#list resultMap?keys as orderId>
							<#assign list = resultMap[orderId]>
							<#list list as result>
								<tr>
									<#if result_index == 0>
									<td rowspan="${list?size}" >
										<a title="点击查看订单详情" href="/vst_order/order/ordCommon/showOrderDetails.do?orderId=${result.orderId}" target="_blank">
											${result.orderId}
										</a>
									</td>
									</#if>
									<td>
									<a href="/vst_order/order/orderManage/showChildOrderStatusManage.do?orderItemId=${result.orderItemId!''}&orderType=child" target="_blank">${result.orderItemId!''}</a>
									</td>
									<td>${result.productName}</td>
									<td>${result.buyCount}</td>
									
									
									<td>${result.addressPerson.fullName!''}</td>
									<td>${result.addressPerson.mobile!''}</td>
									
									<td>${result.ordAddress.province!''}${result.ordAddress.city!''}${result.ordAddress.district!''}${result.ordAddress.street!''}</td>
									<td>
									 <#if (result.ordAddress.postalCode)?? && result.ordAddress.postalCode!='null'> 
									${result.ordAddress.postalCode!''}
									 </#if> 
									</td>
									<td>
										<#if belongBUMap?is_hash && result.belongBU != null>
											${belongBUMap[result.belongBU]!''}
										</#if>
									</td>
									<td>
										<#if result.courierStatus?exists >
											${courierStatusMap[result.courierStatus]!''}
										<#else>
											${courierStatusMap['N']!''}
										</#if> 
									<td><a href="javascript:;" class="courierStatus" courierStatus="${result.courierStatus}" orderItemId="${result.orderItemId}" orderStatus="${result.orderStatus}" ><#if result.courierStatus!=null && result.courierStatus == 'Y' >取消发货<#else>发货</#if></a></td>
								</tr>
							</#list>
						</#list>
						 
					</tbody>
				</table>
				
				<#--分页标签-->
				<@pagination.paging pageParam/>
		</div>
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关数据，请重新输入相关条件查询！</div>
	</#if>
</#if> 
</div>

<#--页脚-->
<#include "/base/foot.ftl"/>
</body>
</html>

<#--js脚本-->
<script type="text/javascript">
	$(function(){
		//查询
		$("#search_button").bind("click",function(){
		
			$("#result").empty();
			
			//表单验证
			if(!$("#searchForm").validate().form()){
				return;
			}
			
			
			//遍历所有查询条件的值
			var value = "";
			
			//input
			var textFields = $("input[type=text]");
			$.each(textFields,function(){
			 	value += this.value;
			});
			
			//select
			var selectFields = $("select");
			$.each(selectFields,function(){
			 	value += this.value;
			});
			
			
			//客户必须输入一个条件
			if(value == ""){
				$("#errorMessage").show();
				return;
			}else{
				$("#errorMessage").hide();
			}
			//假加载效果
			$("#result").empty();
			$("#result").append("<div class='loading mt20'>正在努力的加载数据中......</div>");
		
			$("#searchForm").submit();
		});
		
		//清空
		$("#clear_button").bind("click",function(){
			window.location.href = "/vst_order/ord/order/intoOrdCourierListingQuery.do";
		});
		
		
		//导出
		$("#export_button").bind("click",function(){
			
			var totalResultSize=${pageParam.totalResultSize};
			if(totalResultSize>=200){
				$("#errorData").show();
				return;
			}else{
				$("#errorData").hide();
			}
			//表单验证
			if(!$("#searchForm").validate().form()){
				return;
			}
			/**
			//遍历所有查询条件的值
			var value = "";
			
			//input
			var textFields = $("input[type=text]");
			$.each(textFields,function(){
			 	value += this.value;
			});
			
			//客户必须输入一个条件
			if(value == ""){
				$("#errorMessage").show();
				return;
			}else{
				$("#errorMessage").hide();
			}
			*/
			var url = "/vst_order/ord/order/exportOrdCourierListingExcelData.do?page="+${pageParam.currentPage};
			$("#searchForm").attr("action",url);
		  	$("#searchForm").submit();
		  	
		  	var url = "/vst_order/ord/order/ordCourierListingList.do";
		  	$("#searchForm").attr("action",url);
		});
		
		$(".courierStatus").click(function(){
			var item = $(this);
			var orderItemId = $(item).attr("orderItemId");
			var courierStatus = $(item).attr("courierStatus");
			
			var orderStatus = $(item).attr("orderStatus");
			if (courierStatus != 'Y' && orderStatus == 'CANCEL') {
				alert('操作失败,订单已取消');
				return;
			}
			
			$.ajax({
				url:"/vst_order/ord/order/finishCourier.do",
				type:'post',
				data:{
					orderItemId:orderItemId,
					courierStatus:courierStatus
				},
				dataType:'json',
				success:function(data){
					if ('success' == data.code) {
						alert('操作成功');
						console.log(data);
						if (data.message == 'Y') {
							$(item).html('取消发货');
							$(item).attr("courierStatus", "Y");
							$(item).parents("td").prev("td").html("已发货");
						} else if (data.message == 'N') {
							$(item).html('发货');
							$(item).attr("courierStatus", "N");
							$(item).parents("td").prev("td").html("未发货");
						}
						
					} else {
						alert('操作失败');
					}
				}
			});
			
		});
		
	});
	
	
</script>
