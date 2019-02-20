<!DOCTYPE html>
<html>
<head>

<#include "/base/head_meta.ftl"/>
<link rel="stylesheet" href="/vst_order/js/book/calendar.css" type="text/css"/>
<link rel="stylesheet" href="/vst_order/js/tooltip/css/global.css" type="text/css" />
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/pa-base.css,/styles/v4/modules/arrow.css,/styles/v4/modules/button.css,/styles/v4/modules/forms.css,/styles/v4/modules/selectbox.css,/styles/v4/modules/step.css,/styles/v4/modules/tags.css,/styles/v4/modules/tip.css,/styles/v4/modules/dialog.css,/styles/v4/modules/tables.css" />
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/modules/dialog.css,/styles/v4/modules/calendar.css,/styles/v4/modules/tables.css,/styles/v4/modules/bank.css,/styles/v4/order-common.css,/styles/v4/order.css" />
</head>
<body>
<div class="iframe_header">
        <ul class="iframe_nav">
            <li><a href="#">短信管理</a> &gt;</li>
            <li class="active">订单短信查询</li>
        </ul>
</div>

<div class="iframe_search">
<form method="post" action="/vst_order/order/ordSmsTemplate/findOrdSmsTemplateList.do" id="searchForm">
        <table class="s_table  form-inline">
            <tbody>
                <tr>
                    <td class="w6 s_label">产品类型：</td>
                    <td class="w2">
                     	<select name="categoryId" >
                     				<option value="">请选择</option>
                    	 	        <option value="">全部</option>
                     		<#list bizCategoryList as bizCategory> 
                    	 			<option value="${bizCategory.categoryId!''}" <#if ordSmsTemplate.categoryId == bizCategory.categoryId>selected</#if>>${bizCategory.categoryName!''}</option>
                    	 	</#list>	
			        	</select>
			        </td>
			        <td class="w6 s_label">发送节点：</td>
                    <td class="w46">
                     	<select name="sendNode">
                    	 			<option value="">请选择</option>
                    	 	<#list sendNodeList as sendNode>
                    		<option value="${sendNode.code!''}" <#if ordSmsTemplate.sendNode = sendNode.code>selected</#if>>${sendNode.cnName!''}</option>
                    		</#list>	
			        	</select>
			        </td>
			     </tr>
			     <tr>
			     	<td class="w6 s_label">规则名称：</td>
                    <td class="w18"><input type="text" name="templateName" value="${ordSmsTemplate.templateName}"></td>
		            <td class="w6 s_label">供应商：</td>  
		            <td>
		            <input type="text" errorEle="searchValidate" class="searchInput" name="supplierName" id="supplierName" value="${ordSmsTemplate.supplierName}" >
                	<input type="hidden" value="${ordSmsTemplate.supplierId}" name="suplierId" id="suplierId" >
                	</td>
                 </tr>
                 <tr>
                 	<td class="w6 s_label">下单时间：</td>
                    <td class="w18">
                     	<select name="orderTime">
                     				<option value="">请选择</option>
                     		<#list orderTimeList as orderTime>
                    		<option value="${orderTime.code!''}" <#if ordSmsTemplate.orderTime == orderTime.code>selected</#if>>${orderTime.cnName!''}</option>
                    		</#list>	
			        	</select>
			        </td>
                	<td class="w6 s_label"><span class="notnull">*</span>规则类型：</td>
                    <td>
                    	<input type="radio" name="rule" value="Y" required=true errorEle="rule" <#if rule == "Y">checked</#if>>发送规则 &nbsp&nbsp
                		<input type="radio" name="rule" value="N" required=true errorEle="rule" <#if rule == "N">checked</#if>>不发送规则
                		<div  id="ruleError" style="display:inline"></div>
                    </td>
                </tr>
                <tr>
                    <td class=" operate mt10"><a class="btn btn_cc1" id="search_button">查询</a> </td>
                </tr>    
            </tbody>
        </table>	
		</form>
    </div>
	
<!-- 主要内容显示区域\\ -->
<div class="iframe-content">   
    
    <#if pageParam?? && pageParam.items?? &&  pageParam.items?size &gt; 0>
    <div class="p_box">
    <#if rule?? && rule=="Y" ><span>发送规则查询结果</span>
    <#else>
    <span>不发送规则查询结果</span>
    </#if>
    <table class="p_table table_center">
                <thead>
                    <tr>
                	<th>产品类型</th>
                    <th>规则名称</th>
                    <th>规则类型</th>
                    <th>发送节点</th>
                    <th>创建时间</th>
                    <th>操作</th>
                    </tr>
                </thead>
                <tbody>
					<#list pageParam.items as ordSmsTemplate> 
					<tr>
					<td><#if ordSmsTemplate.bizCategory??>${ordSmsTemplate.bizCategory.categoryName!''}<#else>全部</#if></td>
					<td><#if rule?? && rule=="Y" >${ordSmsTemplate.templateName!''}
					<#else>${ordSmsTemplate.ruleName!''}</a></#if></td>
					<td><#if rule?? && rule=="Y" >发送规则<#else>不发送规则</#if></td>
					<td>
						<#list sendNodeList as sendNode> 
						<#if sendNode = ordSmsTemplate.sendNode>${sendNode.cnName!''}</#if>
						</#list>
					</td>
					<td>${ordSmsTemplate.creataTime?string("yyyy-MM-dd HH:mm:ss")}</td>
					<td class="oper"> 
						<#if rule?? && rule=="Y" >                      
                            <#if ordSmsTemplate.valid == "Y"> 
                            <a href="javascript:void(0);" class="cancel" data="N" data1=${ordSmsTemplate.templateId!''}>禁用</a>
                            <#else>
                            <a href="javascript:void(0);" class="cancel" data="Y" data1=${ordSmsTemplate.templateId!''}>启用</a>
                            </#if>
                            <a href="javascript:void(0);" class="edit" data=${ordSmsTemplate.templateId!''}>编辑</a>
                            <#elseif rule=="N">
                            <a href="javascript:void(0);" class="sendEdit" data=${ordSmsTemplate.ruleId!''}>编辑</a>
                         </#if>           
                        </td>   
					</tr>
					</#list>
                </tbody>
            </table>
				<#if pageParam.items?exists> 
					<div class="paging" > 
					${pageParam.getPagination()}
						</div> 
				</#if>
        
	</div><!-- div p_box -->
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关短信模板，重新输入条件查询！</div>
    </#if>
</div><!-- //主要内容显示区域 -->

<#include "/base/foot.ftl"/>
</body>
</html>
<script>
var addDialog ,updateDialog;

vst_pet_util.commListSuggest("#supplierName", "input[name=suplierId]",'/vst_back/supp/supplier/searchSupplierList.do');

$(function(){

//查询
	$("#search_button").bind("click",function(){
		if(!$("#searchForm").validate().form()){
				return false;
			}
		$(".iframe-content").empty();
		$(".iframe-content").append("<div class='loading mt20'><img src='../../img/loading.gif' width='32' height='32' alt='加载中'> 加载中...</div>");
		$("#searchForm").submit();
	});
});	

//创建
$("#new_button").bind("click",function(){
		if(!$("#searchForm").validate().form()){
				return false;
			}
		var rule = $("input[name='rule']:checked").val();
		message = rule ==="Y" ? "短信模板创建" : "不发送规则创建";
		addDialog = new xDialog("/vst_order/order/ordSmsTemplate/showAddOrdSmsTemplate.do",{"rule":rule}, {title: message,width:1000})
});


//发送短信修改
	$("a.edit").bind("click",function(){
		var templateId = $(this).attr("data");
		updateDialog = new xDialog("/vst_order/order/ordSmsTemplate/showUpdateOrdSmsTemplate.do",{"templateId":templateId }, {title:"修改短信发送模板",width:1000});
});

//不发送短信修改
	$("a.sendEdit").bind("click",function(){
		var templateId = $(this).attr("data");
		updateDialog = new xDialog("/vst_order/order/ordSmsTemplate/showAddOrdSmsTemplate.do",{"templateId":templateId}, {title: "修改短信不发送模板",width:1000});
});

//禁用/启用
	$("a.cancel").bind("click",function(){
	 var templateId=$(this).attr("data1");
	 var valid = $(this).attr("data");
	 msg = valid === "Y" ? "确认启用短信模板的发送 ？" : "确认禁用短信模板的发送？";
	 $.confirm(msg, function () {
	  $.get("/vst_order/order/ordSmsTemplate/editFlag.do?templateId="+templateId+"&valid="+valid, function(result){
       confirmAndRefresh(result);
       });
       });
});

//删除
	$("a.delete").bind("click",function(){
	 var templateId=$(this).attr("data");
	 var rule = $("input[name='rule']:checked").val();
	 msg = " 确认删除该模板 ？";
	 $.confirm(msg, function () {
	  $.get("/vst_order/order/ordSmsTemplate/deleteOrdSmsTemplate.do?templateId="+templateId+"&rule="+rule, function(result){
       confirmAndRefresh(result);
       });
       });
});
	
//预览

	$("a.show").bind("click",function(){
		var templateId=$(this).attr("data1");
	 	var rule = $("input[name='rule']:checked").val();
		new xDialog("/vst_order/order/ordSmsTemplate//showOrdSmsTemplate.do",{"templateId":templateId ,"rule":rule}, {title:"预览",width:1000});
});



	//确定并刷新
	function confirmAndRefresh(result){
		if (result.code == "success") {
			pandora.dialog({wrapClass: "dialog-mini", content:result.message, okValue:"确定",ok:function(){
				var rule = $("input[name='rule']:checked").val();
				window.location.href="/vst_order/order/ordSmsTemplate/findOrdSmsTemplateList.do?rule="+rule;
			}});
		}else {
			pandora.dialog({wrapClass: "dialog-mini", content:result.message, okValue:"确定",ok:function(){
				$.alert(result.message);
			}});
		}
	}
	
</script>


