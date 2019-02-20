<#--页眉-->
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-后台线路下单</title>
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
            <li class="active">新建线路订单</li>
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
	<select id='sel_children' name ="subCategoryId" style="<#if categoryIds != 18>display: none;</#if>" id="childrenIds">
		<option value="">全部</option>
	 	<#if bizFreedomList?? >
	   		<#list bizFreedomList as bizCategory>
	   	 		<option <#if bizCategory.categoryId == subIds>selected ='selected'</#if> value=${bizCategory.categoryId}>${bizCategory.categoryName!''}</option>
	   		</#list>
	 	 </#if>
	</select>
	<form id="searchForm" action="/vst_order/ord/productQuery/findLineOrderProductList.do" method="post" onsubmit="return false">
		<input type="hidden" id="packagesTypes" name="packagesTypes">
		<input type="hidden" id="searchType" name="searchType" value="ROUTE_LMM"/>
		<input type="hidden" id="traffic" name="traffic">
		<@s.formHiddenInput "ordOrderProductQueryVO.recommendLevel" ""/>
		
		<table class="s_table  form-inline">
            <tbody>
                <tr>
                	<td class="w6 s_label">出发城市：</td>
                    <td class="w6">
                    	<@s.formHiddenInput "ordOrderProductQueryVO.districtId" "" />
                    	<input type="text" class="search" id="districtName" name="districtName" value="${ordOrderProductQueryVO.districtName}" >
                    </td>
                	<td colspan="3">产品ID/名称/目的地</td>
                    <td ><@s.formInput "ordOrderProductQueryVO.keyWords" 'style="width:200px;" required'/></td>
              		<td class="w6 s_label">游玩天数：</td>
                    <td class="w6"><@s.formInput "ordOrderProductQueryVO.days" 'style="width:100px;" '/></td>
             	</tr>
             	<tr>
                    <td class="w6 s_label">价格区间：</td>
                    <td>
                        <@s.formRadioButtons "ordOrderProductQueryVO.priceRange" priceRangeMap "" 'onclick="showCustomPriceRange(this);"'/>
                        <span style="display:none;" id="customPriceRange"><@s.formInput "ordOrderProductQueryVO.priceBegin" 'style="width:50px;"'/>-<@s.formInput "ordOrderProductQueryVO.priceEnd" 'style="width:50px;"'/></span>
                    </td>
                </tr>
                <tr>
                    <td class="w6 s_label" style="vertical-align:top;">产品品类：</td>
                    <td class="category_raido">
                    	<@s.formRadioButtons "ordOrderProductQueryVO.categoryIds" categoryMap "" />
                    	<div class="procla js_procla"></div>
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
<div class="f14" id="recommendDiv" style="display:none;margin-left:20px;margin-right:20px;">
	<ul class="ui_tab_1">
		<li><a id="level1" href="javascript:searchByRecommendLevel('1','level1');">不建议(1)</a></li>
		<li><a id="level2" href="javascript:searchByRecommendLevel('2','level2');">不推(2)</a></li>
		<li><a id="level3" href="javascript:searchByRecommendLevel('3','level3');">可订(3)</a></li>
		<li><a id="level4" href="javascript:searchByRecommendLevel('4','level4');">可推(4)</a></li>
		<li><a id="level5" href="javascript:searchByRecommendLevel('5','level5');">特推(5)</a></li>
		<li class="active"><a id="levelAll" href="javascript:searchByRecommendLevel('all','levelAll');">全部线路</a></li>
    </ul>
</div>
<div id="result" class="iframe_content mt20"></div>
<div id="detailResult" class="iframe_content mt20 mod_xx"></div>
    	
    </div>
    
<div class="PopBox"> 	
   
    </div>
</div>
<div class="pop_body_bg"></div>
	
    
<#include "/order/orderProductQuery/member_dialog.ftl"/>   
</body>
</html>
<#include "/base/foot.ftl"/>

<script type="text/javascript">var basePath = '${rc.contextPath}';</script>
<script src="/vst_order/js/book/book.js?version=20180831"></script>
<script src="/vst_order/js/book/pandora-calendar.js"></script>
<script src="/vst_order/js/book/product_goods_search_line.js"></script>
<script src="/vst_order/js/book/bookMemberLogin.js?version=2014081112345"></script>
<script>
<#if isExit?? >
	$(function(){
	accountLogout();
	});
</#if>
var updateDistOrderDialog;
var confirmOrderDialog;
var selectUserDialog;
var book_user_id='${user.userId}';
var loading;
//清空查找条件
function clearFindWhere(){
	 $("#searchForm")[0].reset();
};

	//遍历数据，填充html，data：json数据，box传入div对象；
	function innerHtml(data,box){
		var html = "";
		for(var n in data){
		   var list="";
		   for(var a in data[n]){
				list+='<a href="javascript:;" type="'+ n +'" name="'+ data[n][a].name +'">'+ a+'('+ data[n][a].num +')' +'</a>';
			}
			html+='<p>'+ n +':'+ list +'</p>';
		}
		box.html(html);
	} 
	$(function(){
	$("input[name='keyWords']").jsonSuggest({ 
		url:"/vst_order/ord/productQuery/searchComplete.do", 
		maxResults: 20, 
		minCharacters:1, 
		param:["#searchType"],//扩展参数
		/*onSelect:function(item){ 
			$("#keyWords").val(item.text); 
		} */
	});
	});
	$('.js_procla a').live('click',function(){
		var type = $(this).attr("type");
		var name = $(this).attr("name");
		
		
		if(type=='包含项目'){
			$("#packagesTypes").val(name);
		}
		if(type=='往返交通'){
			$("#traffic").val(name);
		}
		
		//如果酒店ID不为空，则城市非必填
		if($.trim($("#productId").val())!=""){
			$("#districtName").nextAll(".e_error").remove();
			$("#districtName").removeAttr("required");
		}else{
			$("#districtName").nextAll(".e_error").remove();
		}
		
		if(!$("#searchForm").validate().form()){
				return false;
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
			
		//checkbox
		var checkboxFileds = $("input[type=checkbox]");
		$.each(checkboxFileds,function(){
			if(this.checked){
		 		value += this.value;
			}
		});
			
		//客户必须输入一个条件
		if(value == ""){
			$("#errorMessage").show();
			return false;
		}else{
			$("#errorMessage").hide();
		}
			
		//加载效果
		$("#result").empty();
		if($("#detailResult")){
			$("#detailResult").empty();
		}		
		$("#result").empty();
		$("#result").append("<div class='loading mt20'><img src='../../img/loading.gif' width='32' height='32' alt='加载中'>正在努力的加载数据中......</div>");

		//给productId强制赋值
		var productId = $("#productIdBak").val();
		if(productId!=""){
			$("#productId").val(productId);
		}
		//显示推荐级别区域
		$("#recommendDiv").show();
		//ajax加载查询结果
		var $form=$(this).parents("form");
		
		innerHtml("",$('.js_procla'));
		$.ajax({
			type:"post",
			url:$form.attr("action"),
			data:$form.serialize(),
			success:function(result){				
				$("#result").html(result);
				var filterData=$("#filter").html();
				innerHtml(jQuery.parseJSON(filterData),$('.js_procla'));
			}
		});		
	});
	
	$(".category_raido").find("input[type=radio]").bind("click",function(){
		$("#packagesTypes").val("");
		$("#traffic").val("");
		innerHtml("",$('.js_procla'));
		//等29号与搜索组一同上线
		var radio = $(this);
		if (document.getElementById('sel_children_clone')) {
			$("#sel_children_clone").remove();
		}
		if (radio[0].value=='FREETOUR') {
			radio.next().next().before($("#sel_children").clone().attr("id", "sel_children_clone"));
			$("#sel_children_clone").show();
		} else {
			$("#sel_children_clone").remove();
		}
	});
	
</script>
