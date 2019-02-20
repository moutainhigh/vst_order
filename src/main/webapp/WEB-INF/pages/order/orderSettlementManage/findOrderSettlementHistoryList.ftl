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
		<li class="active">订单-结算价修改-历史</li>
	</ul>
</div>

<#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" action="/vst_order/order/orderSettlementChange/findOrderSettlementHistoryList.do" method="post">
        <table class="s_table2 form-inline">
            <tbody>
                <tr>
                    <td width="" >
                    	<label>主订单号：<@s.formInput "monitorCnd.orderId" 'class="w9" maxlength="18"  number="true"'/></label>
                    	
                    	<label>商品ID：<@s.formInput "monitorCnd.suppGoodsId" 'class="w9" number="true"'/></label>
                    	
                    	 <label>游玩时间(开始)：
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
	</form>
</div>

<#--结果显示-->
<div id="result" class="iframe_content mt20">

			<div class="p_box">
				<table class="p_table table_center">
					<thead>
						<tr>
							<th nowrap="nowrap" style="width:10%;">审核状态</th>
							<th nowrap="nowrap" style="width:10%;">审核人</th>
							<th nowrap="nowrap" style="width:10%;">审核时间</th>
							<th nowrap="nowrap" style="width:10%;">审核备注</th>
							<th nowrap="nowrap" style="width:10%;">修改申请人</th>
							<th nowrap="nowrap" style="width:10%;">修改申请时间</th>
							<th nowrap="nowrap" style="width:10%;">修改原因</th>
							<th nowrap="nowrap" style="width:10%;">修改备注</th>
							<th nowrap="nowrap" style="width:10%;">主订单号</th>
							<th nowrap="nowrap" style="width:10%;">子订单号</th>
							
							<th nowrap="nowrap" style="width:10%;">供应商ID</th>
							<th nowrap="nowrap" style="width:10%;">供应商名称</th>
							<th nowrap="nowrap" style="width:10%;">产品ID</th>
							<th nowrap="nowrap" style="width:10%;">产品名称</th>
							<th nowrap="nowrap" style="width:10%;">商品ID</th>
							<th nowrap="nowrap" style="width:10%;">商品名称</th>
							<th nowrap="nowrap" style="width:10%;">游玩日期</th>
							<th nowrap="nowrap" style="width:10%;">价格类型</th>
							<th nowrap="nowrap" style="width:10%;">份数/间夜</th>
							<#--
							
							<th nowrap="nowrap" style="width:10%;">原价格类型的实际结算单价</th>
							<th nowrap="nowrap" style="width:5%;">修改后子订单结算单价</th>
							<th nowrap="nowrap" style="width:10%;">原子订单的实际结算总价</th>
							<th nowrap="nowrap" style="width:10%;">修改后子订单的结算总价</th>
							-->
							
							<th nowrap="nowrap" style="width:10%;">原价格类型的</br>实际结算单价</th>
							<th nowrap="nowrap" style="width:5%;">修改后价格类型的</br>结算单价</th>
							<th nowrap="nowrap" style="width:10%;">原价格类型的</br>实际结算总价</th>
							<th nowrap="nowrap" style="width:10%;">修改后价格类型的</br>结算总价</th>
							
							
							<th nowrap="nowrap" style="width:10%;">原毛利(总)</th>
							<th nowrap="nowrap" style="width:10%;">修改后毛利(总)</th>
						</tr>
					</thead>
					<tbody>
						<#list resultList as result>
							<tr>
								<td>${result.statusName}</td>
								<td>${result.operatorApprove}</td>
								<td>
								 <#if result.updateTime??>
									 	 ${result.updateTime?string('yyyy-MM-dd HH:mm:ss') !''}
								</#if>
								</td>
								<td>${result.approveRemark}</td>
								<td>${result.operator}</td>
								<td>${result.createTime?string('yyyy-MM-dd HH:mm:ss') !''}</td>
								<td>${result.reasonName}</td>
								<td>${result.remark}</td>
								<td>${result.orderId}</td>
								<td>${result.orderItemId}</td>
								
								<td>${result.supplierId}</td>
								<td>${result.supplierName}</td>
								<td>${result.productId}</td>
								<td>${result.productName}</td>
								<td>${result.suppGoodsId}</td>
								<td>${result.suppGoodsName}</td>
								<td>${result.visitTimeStr!''}</td>
								<td>${result.priceTypeName}</td>
								<td>${result.buyItemCount}</td>
								
								<td>${result.oldActualSettlementPriceYuan}</td>
								<td>${result.newActualSettlementPriceYuan}</td>
								<td>${result.oldTotalSettlementPriceYuan}</td>
								<td>${result.newTotalSettlementPriceYuan}</td>
								<td>
								<#if result.oldMaoLi??>
									 	${result.oldMaoLi/100.0}
								</#if>
								</td>
								<td>
								<#if result.newMaoLi??>
									 	${result.newMaoLi/100.0}
								</#if>
								</td>
								
								
								
								
							</tr>
						</#list>
					</tbody>
				</table>
				
				<#--分页标签-->
				<@pagination.paging pageParam/>
				
		</div>
	
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
			window.location.href = "/vst_order/order/orderSettlementChange/showOrderSettlementHistoryList.do";
		});
		
		
		
		
		//导出
		$("#export_button").bind("click",function(){
			
			
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
			
			//客户必须输入一个条件
			if(value == ""){
				$("#errorMessage").show();
				return;
			}else{
				$("#errorMessage").hide();
			}
			
			
			var url = "/vst_order/order/orderSettlementChange/exportExcelData.do?page="+${pageParam.currentPage};
			$("#searchForm").attr("action",url);
		  	$("#searchForm").submit();
		  	
		  	var url = "/vst_order/order/orderSettlementChange/findOrderSettlementHistoryList.do";
		  	$("#searchForm").attr("action",url);
		});
		
	});
	
</script>
