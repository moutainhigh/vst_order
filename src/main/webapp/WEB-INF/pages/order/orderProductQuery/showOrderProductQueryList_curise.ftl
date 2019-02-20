<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title></title>
    <#include "/base/head_meta.ftl"/>
   <link href="http://pic.lvmama.com/styles/youlun/ui-cruise.css" rel="stylesheet" /> 
   <link href="http://pic.lvmama.com/styles/youlun/ui-components.css" rel="stylesheet" /> 
   <link href="http://pic.lvmama.com/styles/youlun/iframe.css" rel="stylesheet" /> 
   <link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/pa-base.css,/styles/v4/modules/arrow.css,/styles/v4/modules/button.css,/styles/v4/modules/forms.css,/styles/v4/modules/selectbox.css,/styles/v4/modules/step.css,/styles/v4/modules/tags.css,/styles/v4/modules/tip.css,/styles/v4/modules/dialog.css,/styles/v4/modules/tables.css" /> 
   <link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/modules/dialog.css,/styles/v4/modules/calendar.css,/styles/v4/modules/tables.css,/styles/v4/modules/bank.css,/styles/v4/order-common.css,/styles/v4/order.css" /> 
	<script>
		var updateDistOrderDialog;
		var confirmOrderDialog;
		var selectUserDialog;
		var book_user_id='${user.userId}';
		var loading;
	</script>
</head>
<body>
    <div class="iframe_header">
        <i class="icon-home ihome"></i>
        <ul class="iframe_nav">
            <li><a href="#">首页</a> ></li>
            <li><a href="#">订单管理</a> ></li>
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
    <div class="iframe_search">
    	<form id="searchForm">
    	<table class="s_table  form-inline">
            <tbody>
                <tr>
                    <td class="w8 s_label"><i class="red">*</i>出游时间段：</td>
                    <td class="w28"><input  class="J_calendar input-text" errorEle="selectDate" type="text" autocomplete="off" name="beginDate" id="beginDate" required=true readonly=readonly/>~ <input data-cascade="true" class="J_calendar input-text mr10" errorEle="selectDate" type="text" autocomplete="off" name="endDate" id="endDate" required=true readonly="readonly"/></td>
                    <td class="w6 s_label">产品名称：</td>
                    <td class="w8"><input type="text" class="w8" name="productName" id="productName"/></td>
                    <td class="w6 s_label">产品ID：</td>
                    <td class="w8"><input type="text" class="w8" name="productId" id="productId" number=true/></td>
                    <td class="w8 s_label"><i class="red">*</i>下单渠道：</td>
                    <td class="w12">
                        <select class="w12" id="distributionId">
                            <option value="2">驴妈妈后台</option>
                            <option value="5">兴旅同业中心</option>
                        </select>
                    </td>
                    <td><div id="selectDateError" style="display:inline"></div></td>
                </tr>
            </tbody>
        </table>
        </form>
        <div class="operate mt20" style="text-align:center"><a class="btn btn_cc1 cxBtn" id="productSearch">查询</a></div>
    </div>
    
    <form id="baseDataForm">
    <input type="hidden" name="productId" id="combProductId"/>
    <input type="hidden" name="productName" id="combProductName"/>
    <input type="hidden" name="distributionId"/>
    <input type="hidden" name="userId" />
    <input type="hidden" name="orderTotalPrice" />
    <input type="hidden" name="ordResult" />
    <input type="hidden" name="specDate">
    </form>
    <div class="iframe_content mt20 mod_cp">
    	
    </div>
    
    
     <div class="iframe_content mt20 mod_xx">
    	
    </div>
    
<div class="PopBox"> 	
   
    </div>
</div>
<div class="pop_body_bg"></div>
	
    
<#include "/order/orderProductQuery/member_dialog.ftl"/>   
</body>
</html>
<#include "/base/foot.ftl"/>
<script src="http://pic.lvmama.com/min/index.php?f=/js/ui/lvmamaUI/lvmamaUI.js"></script>
<script type="text/javascript" src="/vst_order/js/pandora-calendar.js"></script>
<script src="http://pic.lvmama.com/js/v4/modules/pandora-select.js"></script> 
<script src="http://pic.lvmama.com/js/v4/hotel-order.js"></script> 
<script src="http://pic.lvmama.com/js/youlun/cruise.js"></script>
 <script>
 var tempSelectProduct;
    pandora.calendar({
        trigger: ".J_calendar",
        triggerClass: "J_calendar",
        startDelayDays: 5, // 在开始的基础上叠加天数 配合fatalism一起使用
        //fatalism: 365,
        mos:24,
        template: {
            warp: '<div class="ui-calendar ui-calendar-mini"></div>',
            calControl: '<span class="month-prev" {{stylePrev}} title="上一月">‹</span><span class="month-next" {{styleNext}} title="下一月">›</span>',
            calWarp: '<div class="calwarp clearfix">{{content}}</div>',
            calMonth: '<div class="calmonth">{{content}}</div>',
            calTitle: '<div class="caltitle"><span class="mtitle">{{month}}</span></div>',
            calBody: '<div class="calbox">' +
                        '<i class="monthbg">{{month}}</i>' +
                        '<table cellspacing="0" cellpadding="0" border="0" class="caltable">' +
                            '<thead>' +
                                '<tr>' +
                                    '<th class="sun">日</th>' +
                                    '<th class="mon">一</th>' +
                                    '<th class="tue">二</th>' +
                                    '<th class="wed">三</th>' +
                                    '<th class="thu">四</th>' +
                                    '<th class="fri">五</th>' +
                                    '<th class="sat">六</th>' +
                                '</tr>' +
                            '</thead>' +
                            '<tbody>' +
                                '{{date}}' +
                            '</tbody>' +
                        '</table>' +
                    '</div>',
            weekWarp: '<tr>{{week}}</tr>',
            day: '<td {{week}} {{dateMap}} >' +
                    '<div {{className}}>{{day}}</div>' +
                 '</td>'
        }
    });
 
 	//查询产品
 	function searchProduct(){
 		if(!$("#searchForm").validate({
			rules : {
				beginDate : {
					required : true
				}
				,
				endDate : {
					required : true
				}
			},
			messages : {
				beginDate : '请选择开始日期',
				endDate : '请选择结束日期',
			}
		}).form()){
				return;
			}
		
		var beginDate = $("#beginDate").val();
		var endDate = $("#endDate").val();
		if(Date.parse(endDate)-Date.parse(beginDate)<0){
			$.alert("出游时间段：开始日期不能大于结束日期!");
			return;
		}	
		loading	= pandora.loading("正在努力加载...");
		var productName = $("#productName").val();
		var productId = $("#productId").val();
		var distributionId = $("#distributionId").val();
		$("input[name=distributionId]").val(distributionId);
		$.get("${rc.contextPath}/ord/order/queryShipProductList.do?beginDate="+beginDate+"&endDate="+endDate+"&productName="+productName+"&productId="+productId+"&distributionId="+distributionId,function(result){
			$(".mod_cp").html(result);
			//清空之前产品的数据
			$(".mod_xx").empty();
			loading.close();
		});
 	}
 
	$(document).ready(function(){
		$("#productSearch").click(function(){
			searchProduct();
		});
	});
	
/**
** 显示会员查询对话框
**/
function showQueryUserIdDialog(){
	selectUserDialog = pandora.dialog({
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

	closeUserDialogAndShowGoodsInfo(user,userName);
}

function closeUserDialogAndShowGoodsInfo(user,userName){
	book_user_id = user;
	$("#userInfoDiv").html(' <span style="color: #EE3388;font-size:22px;font-weight: bold;">客人姓名：'+userName+'</span>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="accountLogout();">退出当前用户</a>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="clearFindWhere();">清空查找条件</a>');
	selectUserDialog.close();
	if(tempSelectProduct!=null){
		$('#'+tempSelectProduct+'Btn').click();
	}
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
	$("div.userMobile").hide();
	var $form = $("#userMobileForm");
	$.post("${rc.contextPath}/ord/book/regUser.do",
		$form.serialize(),
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
    