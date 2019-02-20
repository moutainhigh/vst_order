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
		<li class="active">订单-批量生成结算子项</li>
	</ul>
</div>

<#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" action="/vst_order/order/orderSettlementChange/findOrderSettlementListForBatch.do" method="post">
        <table class="s_table2 form-inline">
            <tbody>
                <tr>
                    <td width="" >
	                    <label>支付时间：
	                      <@s.formInput "monitorCnd.paymentTimeBegin" 'class="w7" onClick="WdatePicker({readOnly:true})"  required=true'  />
		                   -- 
		                  <@s.formInput "monitorCnd.paymentTimeEnd" 'class="w7" onClick="WdatePicker({readOnly:true})"  required=true'/>
	                    </label>
	                    <label>（以下查询出的订单资源和信息审核通过，且已完成支付的订单。）</label>
                    </td>
                 </tr>
            </tbody>
        </table>        
        <div class="operate mt20" style="text-align:center">
				<a class="btn btn_cc1" id="search_button">查询</a>
				<a class="btn btn_cc1" id="clear_button">清空</a>
				<a class="btn btn_cc1" id="batch_button">批量生成选中订单结算子项</a>
        </div>
        <div id="errorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>请至少输入一个查询条件！</div>
	</form>
</div>

<#--结果显示-->
<div id="result" class="iframe_content mt20">

			<div class="p_box">
			<@pagination.paging pageParam>
				</@pagination.paging>
				<table class="p_table table_center">
					<thead>
						<tr>
							<th nowrap="nowrap" style="width:10%;">
							全选
							<input TYPE="checkbox" id="chkAll" name="chkAll" onclick="selectAll();">
							</th>
							<th nowrap="nowrap" style="width:10%;">主订单号</th>
							<th nowrap="nowrap" style="width:10%;">子订单号</th>
							<th nowrap="nowrap" style="width:10%;">子订单结算状态</th>
							<th nowrap="nowrap" style="width:10%;">供应商ID</th>
							<th nowrap="nowrap" style="width:10%;">供应商名称</th>
							<th nowrap="nowrap" style="width:10%;">产品ID</th>
							<th nowrap="nowrap" style="width:10%;">产品名称</th>
							<th nowrap="nowrap" style="width:10%;">商品ID</th>
							<th nowrap="nowrap" style="width:10%;">商品名称</th>
							<th nowrap="nowrap" style="width:10%;">游玩日期</th>
							<th nowrap="nowrap" style="width:10%;">价格类型</th>
							<th nowrap="nowrap" style="width:10%;">份数/间夜</th>
							<th nowrap="nowrap" style="width:10%;">原结算单价</th>
							<th nowrap="nowrap" style="width:5%;">实际结算单价</th>
							<th nowrap="nowrap" style="width:10%;">实际结算总价</th>
							<th nowrap="nowrap" style="width:10%;">操作</th>
						</tr>
					</thead>
					<tbody>
						<#list resultList as result>
							<tr>
								<td>
								<input type="checkbox" name="orderItemObj" value="${result.orderId}_${result.orderItemId}">
								</td>
								<td>
									<a title="点击查看订单详情" href="/vst_order/order/ordCommon/showOrderDetails.do?objectId=${result.orderId}&objectType=ORDER" target="_blank">
										${result.orderId}
									</a>
								</td>
								<td>
									<a title="点击查看订单详情" href="/vst_order/order/ordCommon/showOrderDetails.do?objectId=${result.orderItemId}&objectType=ORDER_ITEM" target="_blank">
										${result.orderItemId}
									</a>
								</td>
								<td>${result.settlementStatus}</td>
								<td>${result.supplierId}</td>
								<td>${result.supplierName}</td>
								<td>${result.productId}</td>
								<td>${result.productName}</td>
								<td>${result.suppGoodsId}</td>
								<td>${result.suppGoodsName}</td>
								<td>${result.visitTime}</td>
								<td>${result.priceType}</td>
								<td>${result.buyItemCount}</td>
								
								<td>${result.oldSettlementPrice}</td>
								<td>${result.actualSettlementPrice}</td>
								<td>${result.actualTotalSettlementPrice}</td>
								
								<td class="oper">
								<a href="javascript:void(0);" class="settlementPrice" data=${result.orderItemId}>改结算单价</a>
								
								
								<a href="javascript:void(0);" class="maulSettlment" data=${result.orderItemId} data2=${result.orderId}>生成结算子项</a>
								<#--
								<a href="javascript:void(0);" class="showRecommend" data=${product.productId}>修改团期</a>
								-->
		                        </td>
							</tr>
						</#list>
					</tbody>
				</table>
					<@pagination.paging pageParam>
				</@pagination.paging>
<#--页脚-->
<#include "/base/foot.ftl"/>
		</div>
	
</div>

</body>
</html>

<#--js脚本-->
<script type="text/javascript">
	$(function(){
		//查询
		$("#search_button").bind("click",function(){
			
			//表单验证
			if(!$("#searchForm").validate().form()){
				return;
			}
			//假加载效果
			$("#result").empty();
			$("#result").append("<div class='loading mt20'>正在努力的加载数据中......</div>");
		
			$("#searchForm").submit();
		});
		
		//清空
		$("#clear_button").bind("click",function(){
			window.location.href = "/vst_order/order/orderSettlementChange/showOrderSettlementListForBatch.do";
		});
		
		$("#batch_button").bind("click",function(){
			batch();
		});
		
	});
	
	// 全选
	function selectAll(){
		 var checklist = document.getElementsByName("orderItemObj");
		 if(document.getElementById("chkAll").checked){
			 for(var i=0;i<checklist.length;i++){
			    checklist[i].checked = 1;
			 }
		 }else{
		  	for(var j=0;j<checklist.length;j++){
		     	checklist[j].checked = 0;
		  	}
		 }
	}
	
	// 批量生成结算子项
	function batch(){
		var orderItemJson = getOrderItemJson();
		if(orderItemJson!=null&&orderItemJson!=""){
		    var param = {'orderItemJson':getOrderItemJson()};
			$.ajax({
			   url : "/vst_order/order/orderSettlementChange/batchManualSettlmente.do",
			   data : param,
			   type:"POST",
			   dataType:"JSON",
			   success : function(result){
		   		if(result.code=="success"){
		   			alert(result.message);
			   		 //parent.window.location.reload();
		   		}else {
		   		  	alert(result.message);
		   		}
			   }
			});
		}else{
			alert("需选择生成结算子项的订单！");
		}
	}
	
	function getOrderItemJson(){
		var s = document.getElementsByName("orderItemObj");
		var s2 = "";
		var indexNum = 0;
		for( var i = 0; i < s.length; i++ )
		{
			if (s[i].checked ){
				var itemValue = s[i].value.split("_");
				var orderId = itemValue[0];
				var orderItemId = itemValue[1];
				if(indexNum==0){
					s2 = '{"orderItemList":[{"orderId":'+orderId+',"orderItemId":'+orderItemId+'}';
					indexNum++;
				}
				else{
					s2 += ',{"orderId":'+orderId+',"orderItemId":'+orderItemId+'}';
				}
			}
			if(i == s.length-1 && indexNum > 0){
				s2 += "]}"
			}
			
		}
		// alert(s2);
		return s2;
	}
	
	var showAmountDialog;
	 $("a.settlementPrice").bind("click",function(){

		var orderItemId = $(this).attr("data");
		var param="orderItemId="+orderItemId;
   		showAmountDialog = new xDialog("/vst_order/order/orderSettlementChange/showOrderSettlementChange.do",param,{title:"修改结算价格",width:500});
         
      });
      
       $("a.maulSettlment").bind("click",function(){

		var orderItemId = $(this).attr("data");
		var orderId = $(this).attr("data2");
		var param="orderId="+orderId+"&orderItemId="+orderItemId;
		
		
		$.ajax({
		   url : "/vst_order/order/orderSettlementChange/manualSettlmente.do",
		   data : param,
		   type:"POST",
		   dataType:"JSON",
		   success : function(result){
	   		if(result.code=="success"){
	   			alert(result.message);
		   		 //parent.window.location.reload();
	   		}else {
	   		  	alert(result.message);
	   		}
		   }
		});	
		
         
      });
      
      
      
</script>
