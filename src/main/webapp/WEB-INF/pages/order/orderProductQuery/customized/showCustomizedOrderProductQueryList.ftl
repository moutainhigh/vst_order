<#--页眉-->
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-后台定制游下单</title>
<style>
.procla a,.procla span { padding-left: 10px; color: #333; text-decoration: none; }
</style>
<#include "/base/head_meta.ftl"/>
   <link href="http://pic.lvmama.com/styles/youlun/ui-cruise.css" rel="stylesheet" /> 
   <link href="http://pic.lvmama.com/styles/youlun/ui-components.css" rel="stylesheet" /> 
   <link href="http://pic.lvmama.com/styles/youlun/iframe.css" rel="stylesheet" /> 

<link rel="stylesheet" href="/vst_order/js/book/calendar.css" type="text/css"/>
<link rel="stylesheet" href="/vst_order/js/tooltip/css/global.css" type="text/css" />
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/pa-base.css,/styles/v4/modules/arrow.css,/styles/v4/modules/button.css,/styles/v4/modules/forms.css,/styles/v4/modules/selectbox.css,/styles/v4/modules/step.css,/styles/v4/modules/tags.css,/styles/v4/modules/tip.css,/styles/v4/modules/dialog.css,/styles/v4/modules/tables.css" />
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/modules/dialog.css,/styles/v4/modules/calendar.css,/styles/v4/modules/tables.css,/styles/v4/modules/bank.css,/styles/v4/order-common.css,/styles/v4/order.css" />
<link rel="stylesheet" href="/vst_order/js/book/line.css" type="text/css">
	<script>
		var updateDistOrderDialog;
		var confirmOrderDialog;
		var selectUserDialog;
		var book_user_id='${user.userId}';
		var loading;
	</script>
</head>
<body>
<#--页面导航-->
<div class="iframe_header">
        <i class="icon-home ihome"></i>
        <ul class="iframe_nav">
            <li><a href="#">首页</a> &gt;</li>
            <li><a href="#">订单管理</a> &gt;</li>
            <li class="active">新建定制游订单</li>
        </ul>
</div>

<div id="userInfoDiv" style="text-align:center;position:fixed;margin-left:500px;margin-top:-30px;background:#F9FAFB;"> 
	<#if user.userId?? >
	    <span style="color: #EE3388;font-size:22px;font-weight: bold;">客人姓名：${user.userName}</span>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="accountLogout();">退出当前用户</a>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="clearFindWhere();">清空查找条件</a>
	<#else>
		<span style="color: #EE3388;font-size:22px;font-weight: bold;">尚未登陆会员信息</span>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="clearFindWhere();">清空查找条件</a>
	</#if>
</div>

<div>
<#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" action="/vst_order/ord/productQuery/customized/findCustomizedProductList.do" method="post" onsubmit="return false">
		<table class="s_table  form-inline">
            <tbody>
                <tr>
                	<td class="w14 s_label">产品ID/名称</td>
                    <td class="">
                    	<input type="hidden"  id="productId" name="productId"/>
                    	<input  type="text" class="search" id="productName" name="productName" value="" required>
                    </td>
             	</tr>
             	<#--<tr>
                    <td class="w14 s_label">价格区间：</td>
                    <td >
	                	<input type="radio" name="priceRange" value="" >不限</input>
	                	<input type="radio" name="priceRange" value="1" />1000以下</input>
	                	<input type="radio" name="priceRange" value="2" />1000-3000</input>
	                	<input type="radio" name="priceRange" value="3" />3000-8000</input>
	                	<input type="radio" name="priceRange" value="4" />8000以上</input>
	                </td>
                </tr>-->
            </tbody>            
        </table>
        <div class="operate mt20" style="text-align:center">
        	<a class="btn btn_cc1" id="search_button">查询</a>
        	<a class="btn btn_cc1" id="clear_button">清空</a>
        </div>
        <div id="errorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>请至少输入一个查询条件！</div>
	</form>
</div>
<div id="result" class="iframe_content mt20"></div>
	
    
<#include "/order/orderProductQuery/member_dialog.ftl"/>   
</body>
</html>
<#include "/base/foot.ftl"/>

<script type="text/javascript">var basePath = '${rc.contextPath}';</script>
<script src="/vst_order/js/book/book.js?version=20180831"></script>
<script src="/vst_order/js/book/pandora-calendar.js"></script>
<script src="/vst_order/js/book/bookMemberLogin.js?version=2014081112345"></script>
<script>
var updateDistOrderDialog;
var confirmOrderDialog;
var selectUserDialog;
var book_user_id='${user.userId}';
var loading;
	//清空查找条件
	function clearFindWhere(){
		 $("#searchForm")[0].reset();
	};

	$(function(){
		$("#productName").jsonSuggest({
			url:basePath+"/ord/productQuery/customized/queryCustomizedProductList.do",
			maxResults: 20,
			minCharacters:1,
			onSelect:function(item){
				$("#productId").val(item.id);
			}
		});
	});
	/**
	 * 绑定查询按钮事件
	 */
	$("#search_button").bind("click",function(){
		//验证
		if(!$("#searchForm").validate().form()){
			return false;
		}
		//加载效果
		$("#result").empty();
		$("#result").append("<div class='loading mt20'><img src='../../img/loading.gif' width='32' height='32' alt='加载中'>正在努力的加载数据中......</div>");
		//ajax加载查询结果
		var $form=$(this).parents("form");
		 
		$.ajax({
			type:"post",
			url:$form.attr("action"),
			data:$form.serialize(),
			success:function(result){				
				$("#result").html(result);
			}
		});		
	});
</script>
