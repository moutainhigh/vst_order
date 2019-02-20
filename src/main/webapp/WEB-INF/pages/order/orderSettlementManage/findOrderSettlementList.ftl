<#assign mis=JspTaglibs["/WEB-INF/pages//tld/lvmama-tags.tld"]>
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
		<li class="active">订单-结算价修改</li>
	</ul>
</div>
<input type="hidden" value="true" id="valiatePam"/>
<#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" action="/vst_order/order/orderSettlementChange/findOrderSettlementList.do" method="post">
        <table class="s_table2 form-inline">
            <tbody>
            <tr>
                <td width="">
                    <label>主订单号：<@s.formInput "monitorCnd.orderId" 'class="w9" maxlength="18" number="true"'/></label>
                </td>

                <td width="">
                    <label>价格确认状态：<@s.formSingleSelect "monitorCnd.priceConfirmStatus"  priceConfirmMap  'class="w10"'/></label>
                </td>

                <td width="">
                    <label>供应商名称：
					<@s.formHiddenInput "monitorCnd.supplierId"/>
					<@s.formInput "monitorCnd.supplierName" 'class="search"'/>
                    </label>
                </td>
                <td width="">
					<div style="display:none;">
                    <label>游玩日期：
                        <input id="d4321" class="Wdate" type="text" value="${monitorCnd.startVisitTime}"
                               onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:00'})"
                               errorele="selectDate" name="startVisitTime" >
                        --
                        <input id="d4322" class="Wdate" type="text" value="${monitorCnd.endVisitTime}"
                               onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:59',maxDate:'#F{$dp.$D(\'d4321\',{y:2});}',minDate:'#F{$dp.$D(\'d4321\',{d:0});}'})"
                               errorele="selectDate" name="endVisitTime">
                    </label>
                    </div>
                    <label>下单时间：
                        <input id="createTimeBegin" class="Wdate" type="text" value="${monitorCnd.createTimeBegin}"
                               onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:00'})"
                               errorele="selectDate" name="createTimeBegin" >
                        --

                        <input id="createTimeEnd" class="Wdate" type="text" value="${monitorCnd.createTimeEnd}"
                               onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:59',maxDate:'#F{$dp.$D(\'d4321\',{y:2});}',minDate:'#F{$dp.$D(\'d4321\',{d:0});}'})"
                               errorele="selectDate" name="createTimeEnd">
                    </label>
				</td>

            </tr>
            <tr>
                <td width="">
                    <label>子订单号：<@s.formInput "monitorCnd.orderItemId" 'class="w9" maxlength="18" number="true"'/></label>
                </td>

                <td width="" colspan="2">
                    <label>支付时间：
					<@s.formInput "monitorCnd.paymentTimeBegin" 'class="w7" onClick="WdatePicker({})"'/>
                        --
					<@s.formInput "monitorCnd.paymentTimeEnd" 'class="w7" onClick="WdatePicker({})"'/>
                    </label>
                </td>
                <td width="">
                    <label>商品ID：	<@s.formInput "monitorCnd.suppGoodsId" 'class="14" number="true"'/></label>
                </td>

            </tr>
            </tbody>
        </table>        
        <div class="operate mt20" style="text-align:center">
				<a class="btn btn_cc1" id="search_button">查询</a>
				<a class="btn btn_cc1" id="clear_button">清空</a>
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
							<th nowrap="nowrap" style="width:10%;">主订单号</th>
							<th nowrap="nowrap" style="width:10%;">子订单号</th>
							<th nowrap="nowrap" style="width:10%;">子订单价格<br/>确认状态</th>
							<th nowrap="nowrap" style="width:10%;">子订单结算状态</th>
							<th nowrap="nowrap" style="width:10%;">供应商ID</th>
							<th nowrap="nowrap" style="width:10%;">供应商名称</th>
							<th nowrap="nowrap" style="width:10%;">产品ID</th>
							<th nowrap="nowrap" style="width:10%;">产品名称</th>
							<th nowrap="nowrap" style="width:10%;">商品ID</th>
							<th nowrap="nowrap" style="width:10%;">商品名称</th>
							<th nowrap="nowrap" style="width:10%;">游玩日期</th>
							<th nowrap="nowrap" style="width:10%;">价格类型</th>
                            <th nowrap="nowrap" style="width:10%;">币种</th>
							<th nowrap="nowrap" style="width:10%;">份数/间夜</th>
							<th nowrap="nowrap" style="width:10%;">原结算单价</th>
							<th nowrap="nowrap" style="width:5%;">实际结算单价</th>
							<th nowrap="nowrap" style="width:10%;">原结算总价</th>
							<th nowrap="nowrap" style="width:10%;">实际结算总价</th>
							<th nowrap="nowrap" style="width:10%;">操作</th>
						</tr>
					</thead>
					<tbody>
						<#list resultList as result>
							<tr>
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
                                <td><#if result.priceConfirmStatus?? && result.priceConfirmStatus=='UN_CONFIRMED'>价格待确认<#else>价格已确认</#if></td>
								<td>${result.settlementStatus}</td>
								<td>${result.supplierId}</td>
								<td>${result.supplierName}</td>
								<td>${result.productId}</td>
								<td>${result.productName}</td>
								<td>${result.suppGoodsId}</td>
								<td>${result.suppGoodsName}</td>
								<td>${result.visitTime}</td>
								<td>${result.priceType}</td>
                                <td><#if result.currencyName??>${result.currencyName}<#else>人民币</#if></td>
								<td>${result.buyItemCount}</td>
								<td>${result.oldSettlementPrice}</td>
								<td>${result.actualSettlementPrice}</td>
								<td>${result.yuanOrderTotalSettlementPrice}</td>
								<td>${result.actualTotalSettlementPrice}</td>
								
								<td class="oper">
								<#-- 只有买断，则隐藏修改非买断按钮-->
								<#if  (result.isBuyoutFlag != "Y" ) >
									<#--外币结算修改按钮-->
									<#if  result.isCurrency != 'Y'>
										<@mis.checkPerm permCode="5894"><a href="javascript:void(0);" class="settlementPrice" data=${result.orderItemId}>改结算单价</a></@mis.checkPerm>
										<@mis.checkPerm permCode="5895"><a href="javascript:void(0);" class="settlementTotalPrice" data=${result.orderItemId}>改结算总价</a></@mis.checkPerm>
									<#else>
										<@mis.checkPerm permCode="5894"><a href="javascript:void(0);" class="currencySettlementPrice" data=${result.orderItemId} dataOrderId=${result.orderId}>改外币结算单价</a></@mis.checkPerm>
										<@mis.checkPerm permCode="5895"><a href="javascript:void(0);" class="currencySettlementTotalPrice" data=${result.orderItemId} dataOrderId=${result.orderId}>改外币结算总价</a></@mis.checkPerm>
									</#if>

								<#else>
									<#if  result.buyoutTotalPrice == result.totalSettlementPrice && result.buyoutQuantity==result.quantity >
									<#else>
									</#if>
								</#if>

								<#if  result.isBuyoutFlag == "Y">
                                	<a href="javascript:void(0);" data-ismultprice="${result.isMultPrice}" class="settlementBudgetPrice" data=${result.orderItemId}>改买断结算价</a>
								</#if>
								<a href="javascript:void(0);" class="maulSettlment" data=${result.orderItemId} data2=${result.orderId}>生成结算子项</a>
                                <#if result.priceConfirmStatus?? && result.priceConfirmStatus=='UN_CONFIRMED'><a href="javascript:void(0);" class="confirmPrice" dataOrdItem=${result.orderItemId} dataOrd=${result.orderId}>确认价格无问题</a></#if>
                                <@mis.checkPerm permCode="5896">
									<#--<a href="/super_back/ord/sale/OrdSaleAddJump.zul?orderId=${result.orderId}&sysCode=VST" target="_blank">查看退改记录</a>-->
                                    <a href="/sales_front/ord/sale/toOrdSale/${result.orderId}" target="_blank">查看退改记录</a>
								</@mis.checkPerm>
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
			//验证选择
			if($("#valiatePam").val()=="true" && isEmpty($("#orderId").val()) && isEmpty($("#orderItemId").val()) && (isEmpty($("input[name=createTimeBegin]").val()) || isEmpty($("input[name=createTimeEnd]").val())) && (isEmpty($("input[name=paymentTimeBegin]").val()) ||  isEmpty($("input[name=paymentTimeEnd]").val()))){
				alert("主订单号、子订单号、下单日期和支付日期 请至少选择一个搜索条件");
				return;
			}
			if(valiateMonth($("input[name=createTimeBegin]").val(),$("input[name=createTimeEnd]").val())){
				alert("下单日期不能超过3个月");
				return;

			}
            if(valiateMonth($("input[name=paymentTimeBegin]").val(),$("input[name=paymentTimeEnd]").val())){
                alert("支付日期不能超过3个月");
				return;
            }

			if($(this).attr("disabled")) {
				return;
			}
			//表单验证
			if(!$("#searchForm").validate().form()){
				return;
			}
			//假加载效果
			$("#result").empty();
			$("#result").append("<div class='loading mt20'>正在努力的加载数据中......</div>");
			$(this).attr("disabled", true);
			$("#searchForm").submit();
		});
		
		//清空
		$("#clear_button").bind("click",function(){
			window.location.href = "/vst_order/order/orderSettlementChange/showOrderSettlementList.do";
		});

        $("#supplierName").jsonSuggest({
            url:"${rc.contextPath}/ord/order/querySupplierList.do",
            maxResults: 20,
            minCharacters:1,
            onSelect:function(item){
                $("#supplierId").val(item.id);
            }
        });

		
	});
	
	var showAmountDialog;
	 $("a.settlementPrice").bind("click",function(){

		var orderItemId = $(this).attr("data");
		var param="orderItemId="+orderItemId;

   		showAmountDialog = new xDialog("/vst_order/order/orderSettlementChange/showOrderSettlementChange.do",param,{title:"修改结算单价格",width:500});
         
     });
     
	 $("a.settlementTotalPrice").bind("click",function(){

		var orderItemId = $(this).attr("data");
		var param="orderItemId="+orderItemId;
   		showAmountDialog = new xDialog("/vst_order/order/orderSettlementChange/showOrderSettlementTotalChange.do",param,{title:"修改结算总价格",width:500});
         
     });

    $("a.currencySettlementPrice").bind("click",function(){

        var orderItemId = $(this).attr("data");
        var orderId = $(this).attr("dataOrderId");

        var param="orderItemId=" + orderItemId + "&orderId=" + orderId;

        showAmountDialog = new xDialog("/vst_order/order/orderSettlementChange/showOrderCurrencySettlementChange.do",param,{title:"修改外币结算单价格",width:500});

    });

    $("a.currencySettlementTotalPrice").bind("click",function(){

        var orderItemId = $(this).attr("data");
        var orderId = $(this).attr("dataOrderId");

        var param="orderItemId=" + orderItemId + "&orderId=" + orderId;
        showAmountDialog = new xDialog("/vst_order/order/orderSettlementChange/showOrderCurrencySettlementTotalChange.do",param,{title:"修改外币结算总价格",width:500});

    });

    $("a.settlementBudgetPrice").bind("click",function(){

        var orderItemId = $(this).attr("data");
        var param="orderItemId="+orderItemId;
		
		var isMult = $(this).attr("data-ismultprice");
		var openUrl = "/vst_order/order/orderSettlementChange/showOrderSettlementBudgetChange.do";
		
		/*
        $.ajax({
            url : "/vst_order/order/orderSettlementChange/checkUpdateBudgetPrice.do",
            data : param,
            type:"POST",
            dataType:"JSON",
            success : function(result){
                if(result.code=="success"){
                    //alert(result.message);
                    showAmountDialog = new xDialog(openUrl,param,{title:"修改买断结算价格",width:500});
                }else {
                    alert(result.message);
                }
            }
        });
        */
		
		showAmountDialog = new xDialog(openUrl,param,{title:"修改买断结算价格",width:500});
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

	//确认子订单价格状态
	$("a.confirmPrice").on("click",function () {
		var orderId=$(this).attr("dataOrd");
        var orderItemId = $(this).attr("dataOrdItem");
        $.ajax({
            url : "/vst_order/order/orderSettlementChange/updateConfirmPrice.do",
            data : {
                orderId:orderId,
                orderItemId:orderItemId
			},
            type:"POST",
            dataType:"JSON",
            success : function(result){
                if(result.code=="success"){
                    alert(result.message);
                    $("#searchForm").submit();
                }else {
                    alert(result.message);
                }
            }
        });

    });

    function isEmpty(value) {
        if (value == null || value == "" || typeof(value) == "undefined") {
            return true;
        }
        return false;
    }

	function valiateMonth(startTime,endTime) {
		if(isEmpty(startTime) || isEmpty(endTime)){
            return false;
		}
		var startDate=strToDate(startTime);
		var endDate=strToDate(endTime);
		var day=parseInt((endDate.getTime()-startDate.getTime())/(24 * 60 * 60 * 1000));
		if(day>90){
			return true;
		}
		return false;
		
    }

    function strToDate(str) {
        var tempStrs = str.split(" ");
        var dateStrs = tempStrs[0].split("-");
        var year = parseInt(dateStrs[0], 10);
        var month = parseInt(dateStrs[1], 10) - 1;
        var day = parseInt(dateStrs[2], 10);
        var date=null;
		if(tempStrs.indexOf(":")!=-1) {
            var timeStrs = tempStrs[1].split(":");
            var hour = parseInt(timeStrs [0], 10);
            var minute = parseInt(timeStrs[1], 10) - 1;
            var second = parseInt(timeStrs[2], 10);
            date= new Date(year, month, day, hour, minute, second);
        }else{
			date=new Date(year, month, day);
		}
        return date;
    }

</script>
