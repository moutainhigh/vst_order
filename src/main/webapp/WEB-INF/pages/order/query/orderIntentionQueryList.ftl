<#import "/base/pagination.ftl" as pagination>
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>

<#include "/base/head_meta.ftl"/>
</head>
<body>
<div class="iframe_header">
        <ul class="iframe_nav">
            <li><a href="#">首页</a> &gt;</li>
            <li><a href="#">订单管理</a> &gt;</li>
            <li class="active">意向单列表</li>
        </ul>
</div>


<div class="iframe_search">
<form method="post" action='/vst_order/ord/order/queryIntentionOrderList.do' id="searchForm">
        <table class="s_table">
            <tbody>
                <tr>
                 <td class="s_label"><label>意向单编号：
                 <@s.formInput "intentionCnd.intentionId" 'class="w8"  required="true"'/></label>
                  
                 </td>
                                 
                    <td class="s_label">
                     
                     <label>提交时间：<@s.formInput "intentionCnd.createTimeBegin" 'class="w9" onClick="WdatePicker({readOnly:true})"'/>—<@s.formInput "intentionCnd.createTimeEnd" 'class="w9" onClick="WdatePicker({readOnly:true})"'/></label>
                      </td>                 
                    <td class="s_label">
                    <label>状态：<@s.formSingleSelect "intentionCnd.state" IntentionStatusMap 'class="w10"'/></label>
                    </td>                   
                </tr>
                <tr>
                <td class="s_label"><label>产品编号 ：<@s.formInput "intentionCnd.productId" 'class="w10" number="true"'/></label></td>
                <td class="s_label"><label>产品名称 ：<@s.formInput "intentionCnd.productName" 'class="w20" number="true"'/></label></td>
                <td class="s_label"><label>产品经理 ：<@s.formInput "intentionCnd.productManager" 'class="w20" number="true"'/></label></td>
                 <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<td>                   
                 <td class=" operate mt10"><a class="btn btn_cc1" id="search_button">查询</a></td>
                </tr>
                <tr>
                <td class="s_label"><label>联系人姓名 ：<@s.formInput "intentionCnd.contactName" 'class="w10" number="true"'/></label></td>
                <td class="s_label"><label>手机号码 ：<@s.formInput "intentionCnd.phoneNumber" 'class="w20" number="true"'/></label></td>
                <td class="s_label"><label>电子邮箱 ：<@s.formInput "intentionCnd.email" 'class="w20" number="true"'/></label></td>
                 <td>&nbsp;&nbsp;&nbsp;<td>                   
                 <td class=" operate mt10"><a class="btn btn_cc1" id="clear_button">清空</a></td>
                </tr>
            </tbody>
        </table>	
		</form>
    </div>
<!-- 主要内容显示区域\\ -->
<div class="iframe-content">   
    <div class="p_box">
	<table class="p_table table_center">
                <thead>
                    <tr>
                   <th> 意向单号</th>
                   <th>产品名称</th>
                    <th>下单时间</th>
                    <th>游玩时间</th>
                    <th>状态</th>
                    <th>联系人</th>
                    <th>编辑</th>
                    </tr>
                </thead>
                <tbody>
                <#if pageParam??>
                <#list pageParam.items as intentionOrder> 
                <#assign index=0>
					<tr>
						<input type="hidden" id="specDate${index}" value="${intentionOrder.travelTime?string("yyyy-MM-dd")}">
  						<input type="hidden" id="productId${index}"  value="${intentionOrder.productId}">
  						<input type="hidden" id="distributionId${index}"  value="2">
  						<input type="hidden" id="userId${index}"  value="${intentionOrder.loginId}">
  						<input type="hidden" id="intentionOrderId${index}"  value="${intentionOrder.intentionOrderId}">
  						<input type="hidden" id="childNum${index}"  value="${intentionOrder.childrenCounts}">
  						<input type="hidden" id="adultNum${index}"  value="${intentionOrder.adultCounts}">
					<td>
					<#if intentionOrder.intentionOrderId??>
						<a title="点击查看意向单详情" href="/vst_order/ord/order/toIntentionDetail.do?intentionId=${intentionOrder.intentionOrderId}" target="_blank">
							${intentionOrder.intentionOrderId}
						</a>					 
					 </#if>
					</td>
					<td>
					  <#if intentionOrder.productName??>
					   ${intentionOrder.productName} 
					 </#if>				  
					  </td>
					<td>
					 <#if intentionOrder.createTimeStr??>
					   ${intentionOrder.createTimeStr}
					 </#if>
					 </td>
					<td>
					<#if intentionOrder.travelTimeStr??>
					   ${intentionOrder.travelTimeStr}
					 </#if>
					</td>
					<td id="state_${intentionOrder.intentionOrderId}">
					<#if intentionOrder.stateView??>
					   ${intentionOrder.stateView}
					 </#if>
					</td>
					<td>
					<#if intentionOrder.contactsName??>
					   <span id="contact_${intentionOrder.intentionOrderId}">
					   联系人：${intentionOrder.contactsName!""}</br>
					   联系电话：${intentionOrder.tel!""}</br>
					   电子邮箱:${intentionOrder.email!""}
					   </span>
					 </#if>
					</td>
					<td>
					<#if intentionOrder.orderId??>后台下单 
					<#else>
					 <a href="javascript:;" onclick="infoBookInfo(${index})" >后台下单</a> 
					 </#if>
					 <a href="/vst_order/ord/order/toIntentionDetail.do?intentionId=${intentionOrder.intentionOrderId}" target="_blank">编辑</a> 
					 <a href="javascript:;" class="editCateGroup" data=${intentionOrder.intentionOrderId}>取消</a>
					 <a href="javascript:;"  data=${intentionOrder.intentionOrderId} onclick="showLogs(this)">日志</a>
					 
					 </td>
					 </tr>
					 <#assign index=index+1>
					</#list>
                </#if>				
                </tbody>
            </table>
            <#if pageParam??><@pagination.paging pageParam/></#if>			
	</div><!-- div p_box -->
</div><!-- //主要内容显示区域 -->
 <form id="bookForm" action="" method="post">
  	<input type="hidden" name="specDate" >
  	<input type="hidden" name="productId" >
  	<input type="hidden" name="distributionId" >
  	<input type="hidden" name="userId" >
  	<input type="hidden" name="intentionOrderId">
  	<input type="hidden" name="childNum" >
  	<input type="hidden" name="adultNum" >
  </form>
<#include "/base/foot.ftl"/>
</body>
</html>
<script>
   //定义动态显示窗口
	var updateIntentionDialog,showLogDialog;
	//查询
	$("#search_button").click(function(){
		$("#searchForm").submit();
	});
	//清空
		$("#clear_button").bind("click",function(){
			$(document).find("input").val("").attr("readOnly",false);
			$(document).find("select").val("");
			$(".iframe-content").html("");
		});	
	//修改意向单状态
	$("a.editCateGroup").bind("click",function(){
	   var dataArr=$(this).attr("data").split(",");	
	    var intentionOrderId  = dataArr[0];
		updateIntentionDialog = new xDialog("/vst_order/ord/order/showUpdateIntentionState.do",{"intentionOrderId":intentionOrderId},{title:"修改意向单状态",width:500,height:220});
	});
	
	//显示日志
	function showLogs(obj){
	  var thisObj=$(obj);
	  var idstr=thisObj.attr("data");
	  showLogDialog = new xDialog("/vst_order/ord/order/findIntentionComLogList.do?objectId="+idstr,{},{title:"查看日志",width:1200,iframe:true}); 
	}
	
	 function infoBookInfo(index){
	
	 $("#bookForm input[name=specDate]").val($("#specDate" + index).val());
		$("#bookForm input[name=productId]").val($("#productId"+index).val());
		$("#bookForm input[name=distributionId]").val(2);
		$("#bookForm input[name=userId]").val($("#userId"+index).val());
		$("#bookForm input[name=intentionOrderId]").val($("#intentionOrderId"+index).val());
		$("#bookForm input[name=childNum]").val($("#childNum"+index).val());
		$("#bookForm input[name=adultNum]").val($("#adultNum"+index).val());
		$("#bookForm").attr("action","/vst_order/ord/order/queryLineDetailList.do");
		$("#bookForm").submit();
	 }
	 
	 
	 $(function(){
	 	//默认提交起始时间为前一天，结束时间为今天
	 	if('${isDefaultStatus!''}'!=''){
	 		$("#createTimeBegin").val(GetDateStr(-1));
	 	}
	 	if('${isDefaultStatus!''}'!=''){
	 		$("#createTimeEnd").val(GetDateStr(0));
	 	}
	 	if('${isDefaultStatus!''}'!=''){
	 		$("#state").find("option[value='AWAIT']").attr("selected","selected");
	 	}
	 	//获取日期
 		function GetDateStr(AddDayCount) { 
			var dd = new Date(); 
			dd.setDate(dd.getDate()+AddDayCount);
			var y = dd.getFullYear(); 
			var m = toZero(dd.getMonth()+1);
			var d = toZero(dd.getDate()); 
			return y+"-"+m+"-"+d; 
		} 
		function toZero (value, length) {
            if (!length) length = 2;
            value = String(value);
            for (var i = 0, zeros = ''; i < (length - value.length); i++) {
                zeros += '0';
            }
            return zeros + value;
        };
	 	
	 });

         
</script>
