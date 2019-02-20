<#--页眉-->
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-后台下单</title>
<#include "/base/head_meta.ftl"/>


<link rel="stylesheet" href="http://pic.lvmama.com/styles/v6/order.css">

<link rel="stylesheet" href="/vst_order/js/book/calendar.css" type="text/css"/>
<link rel="stylesheet" href="/vst_order/js/tooltip/css/global.css" type="text/css" />
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/pa-base.css,/styles/v4/modules/arrow.css,/styles/v4/modules/button.css,/styles/v4/modules/forms.css,/styles/v4/modules/selectbox.css,/styles/v4/modules/step.css,/styles/v4/modules/tags.css,/styles/v4/modules/tip.css,/styles/v4/modules/dialog.css,/styles/v4/modules/tables.css" />
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/modules/dialog.css,/styles/v4/modules/calendar.css,/styles/v4/modules/tables.css,/styles/v4/modules/bank.css,/styles/v4/order-common.css,/styles/v4/order.css" />
</head>
<body>
<#--页面导航-->
<div class="iframe_header">
        <i class="icon-home ihome"></i>
        <ul class="iframe_nav">
            <li><a href="#">首页</a> &gt;</li>
            <li><a href="#">订单管理</a> &gt;</li>
            <li class="active">新建订单</li>
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
	<form id="searchForm" action="/vst_order/ord/productQuery/findOrderGoodsList.do" method="post" onsubmit="return false">
		<@s.formHiddenInput "ordOrderProductQueryVO.recommendLevel" ""/>
		<table class="s_table  form-inline">
            <tbody>
                <tr>
                	<td class="w6 s_label">入住城市：</td>
                    <td class="w6">
                    	<@s.formHiddenInput "ordOrderProductQueryVO.districtId" ""/>
                    	<input type="text" class="search" id="districtName" name="districtName" value="${ordOrderProductQueryVO.districtName}" required>
                    </td>
                    <td class="w6 s_label">入住日期：</td>
                    <td class="w6">
                    	<input type="text" id="startDate" name="startDate" style="width:100px;" value="${ordOrderProductQueryVO.startDate}" class="iflt_date" data-type="calendar" onblur="checkDay();" readonly="readonly" required/>
                    </td>
                    <td class="w6 s_label">离店日期：</td>
                    <td class="w6">
                    	<input type="text" id="endDate" name="endDate" style="width:100px;" value="${ordOrderProductQueryVO.endDate}" class="iflt_date" data-type="calendar" data-cascade="true" onblur="checkDay();" readonly="readonly" required/>
                    </td>
                    <td class="w6 s_label">入住天数：</td>
                    <td class="w6"><@s.formInput "ordOrderProductQueryVO.days" 'style="width:100px;" onblur="checkDay();" required'/></td>
                </tr>
                <tr>
                	<td class="w6 s_label">酒店名称：</td>
                    <td class="w15">
                    	<input type="hidden" id="productIdBak"/>
                    	<input type="text" class="search" style="width:300px;" id="productName" name="productName" value=<#if ordOrderProductQueryVO!=null>${ordOrderProductQueryVO.productName}</#if>>
                    </td>
                	<td class="w6 s_label">酒店ID：</td>
                    <td class="w6"><input type="text" style="width:50px;" id="productId" name="productId" value=${ordOrderProductQueryVO.productId}></td>
                </tr>
                <tr>
                    <td class="w6 s_label">支付方式：</td>
                    <td>
                    	<@s.formCheckboxes1 "ordOrderProductQueryVO.payTargets" payTargetMap "" ""/>
                    </td>
                </tr>
                <tr>
                    <td class="w6 s_label">房型要求：</td>
                    <td>
                    	<@s.formRadioButtons "ordOrderProductQueryVO.bedType" bedTypeMap "" ""/>
                    </td>
                </tr>
                <tr>
                    <td class="w6 s_label">酒店星级：</td>
                    <td>
                    <#if bizStarRate?? && bizStarRate.bizDictList?size gt 0>
                    	<#list bizStarRate.bizDictList as bizDict>
	            			<label class="checkbox mr10"><input type="checkbox" name="starRate" value="${bizDict.dictId!''}"
	            				<#if ordOrderProductQueryVO?? && ordOrderProductQueryVO.starRate??>
	            					<#list ordOrderProductQueryVO.starRate?split(",") as star>
            							<#if star == bizDict.dictId>checked</#if>
            						</#list>
	            				</#if>
	            			>${bizDict.dictName!''}</label>
	            		</#list>
                    </#if>
                    </td>
                </tr>
                <tr>
                    <td class="w6 s_label">价格区间：</td>
                    <td>
                        <@s.formRadioButtons "ordOrderProductQueryVO.priceRange" priceRangeMap "" 'onclick="showCustomPriceRange(this);"'/>
                        <span style="display:none;" id="customPriceRange"><@s.formInput "ordOrderProductQueryVO.priceBegin" 'style="width:50px;"'/>-<@s.formInput "ordOrderProductQueryVO.priceEnd" 'style="width:50px;"'/></span>
                    </td>
                </tr>
                <tr>
                    <td class="w6 s_label">设施列表：</td>
                    <td>
                    	<@s.formCheckboxes1 "ordOrderProductQueryVO.facilitieses" facilitiesMap "" ""/>
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
		<li class="active"><a id="levelAll" href="javascript:searchByRecommendLevel('all','levelAll');">全部酒店</a></li>
    </ul>
</div>
<div id="result" class="iframe_content mt20"></div>
</div>

<div>

<#include "/order/orderProductQuery/member_dialog.ftl"/>
<#include "/base/foot.ftl"/>

<script type="text/javascript">var basePath = '${rc.contextPath}';</script>
<script src="/vst_order/js/book/book.js?version=20180831"></script>
<script src="/vst_order/js/book/pandora-calendar.js"></script>
<script src="/vst_order/js/book/product_goods_search.js"></script>
<script src="/vst_order/js/book/product_goods_result.js?v=20180831"></script>
<script src="/vst_order/js/book/bookMemberLogin.js?version=2014081112345"></script><!-- 人员登陆 -->
<script type="text/javascript">
 var rootPath = '${rc.contextPath}';
 var thisProCategoryId = '${prodProduct.bizCategory.categoryId}'; 	
</script>
<script src="http://pic.lvmama.com/min/index.php?f=/js/v5/modules/pandora-poptip.js"></script>
<script src="http://pic.lvmama.com/js/v6/order.js"></script>

<script>
var updateDistOrderDialog;
var confirmOrderDialog;
var selectUserDialog;
var book_user_id='${user.userId}';
var channel_code ='';
/**
 * 下单商品信息
 */
var book_goods={};
$(function(){
	 
});
//打开填写入住信息页面
function openBookInfo(goodsId){
    $("div.searchUser").hide();
    $("div.userMobile").hide();
	//var goodsId=$('input[name="goodsId"]:checked').val();
	//var goodsId=$(obj).val();
     /** $('input[name="goodsId"]').each(function(){
         	if(goodsId==""||goodsId==undefined||$(this).attr("checked")=="checked")
      		{
      			$(this).removeAttr("disabled");
      		}else{
      			$(this).attr("disabled","disabled");
      		}
      }); */
       
      if(goodsId==""||goodsId==undefined)
      {
      	//$.alert('请选择一个房型.'); 
      	return;
      }
  	  book_goods={"goodsId":goodsId,"visitTime":$("#startDate").val(),"leaveTime":$("#endDate").val(),"quantity":"1"};
      if(book_user_id==""){
      	showQueryUserIdDialog();
      	return;
      }
      book_goods["channelCode"] =channel_code;
      showUpdateDistOrderDialog();
}

function showUpdateDistOrderDialog(){
	book_goods["userId"]=book_user_id;
	updateDistOrderDialog = new xDialog("/vst_order/ord/book/selectGoods.do",
      book_goods,
      {title:"填写入住信息",width:920,showPanel:"#bookInfoDiv"});
}
/**
** 显示会员查询对话框
**/
function showQueryUserIdDialog(){
	selectUserDialog = $.dialog({
        width: 600,
        title: "会员识别",
        content: $("#demo13H").html()
    });
}

function selectUser(){
	var $form=$("#userSelectForm");
	var user=$form.find("input[name='user_id']:checked").val();
	var userName=$form.find("input[name='user_id']:checked").attr("userName");
	if(typeof(user)=="undefined"){
		alert("请先选中会员");
		return;
	}

    if(isUserFrozen("userId",user)){
        $("div.searchUser span").html("此账号已被驴妈妈冻结，不能下单，请更换账号下单。");
        $("div.searchUser").show();
        return;
    };
	channel_code=$("input[name=channel_code]:checked").val();
	book_goods["channelCode"] =channel_code;

	closeUserDialogAndShowGoodsInfo(user,userName);
}

function closeUserDialogAndShowGoodsInfo(user,userName){
	book_user_id = user;
	$("#userInfoDiv").html(' <span style="color: #EE3388;font-size:22px;font-weight: bold;">客人姓名：'+userName+'</span>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="accountLogout();">退出当前用户</a>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="clearFindWhere();">清空查找条件</a>');
	selectUserDialog.close();
	showUpdateDistOrderDialog();
}

function searchUser(){
	var searchUser = $("input[name='searchUser']").val();
	if($.trim(searchUser)==''){
		return;
	}
	$("#mobileDiv").hide();
	$.post("${rc.contextPath}/ord/book/queryUser.do",{"key":searchUser},function(data){
		$("#userListDiv").html(data);
	});
}
function showMobileReg(){
	$("#userListDiv").empty();
	$("#mobileDiv").show();
}
function regUserAccount(){
    if(isUserFrozen("mobile",$("input[name='userMobile']").val())){
        $("div.userMobile span").html("此手机号已被驴妈妈冻结，不能下单，请更换手机号下单。");
        $("div.userMobile").show();
        return;
    };

	$("div.userMobile").hide();
	var $form = $("#userMobileForm");
	var userMobile = $form.find("input[name=userMobile]").val();
	var channelCode = $("input[name=channel_code]:checked").val();
	channel_code = channelCode;
	book_goods["channelCode"] = channel_code;
	$.post("${rc.contextPath}/ord/book/regUser.do",
		{userMobile:userMobile, channel_code:channelCode},
		function(data){
			if(data.success){
				closeUserDialogAndShowGoodsInfo(data.attributes.userId);
			}else{
				$("div.userMobile span").html(data.message);
				$("div.userMobile").show();
			}
	},"JSON");
}
//注销
function accountLogout(){
	book_user_id = "";
	//$("#userInfoDiv").hide();
	$("#userInfoDiv").html('  <span style="color: #EE3388;font-size:22px;font-weight: bold;">尚未登陆会员信息</span>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="clearFindWhere();">清空查找条件</a>');
}
//清空查找条件
function clearFindWhere(){
	 $("#searchForm")[0].reset();
}
</script>
</body>
</html>
