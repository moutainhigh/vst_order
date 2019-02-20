<#--页眉-->
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-后台下单</title>
<#include "/base/head_meta.ftl"/>
<link rel="stylesheet" href="/vst_order/js/book/calendar.css" type="text/css"/>
<link rel="stylesheet" href="/vst_order/js/tooltip/css/global.css" type="text/css" />
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/pa-base.css,/styles/v4/modules/arrow.css,/styles/v4/modules/button.css,/styles/v4/modules/forms.css,/styles/v4/modules/selectbox.css,/styles/v4/modules/step.css,/styles/v4/modules/tags.css,/styles/v4/modules/tip.css,/styles/v4/modules/dialog.css,/styles/v4/modules/tables.css" />
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/modules/dialog.css,/styles/v4/modules/calendar.css,/styles/v4/modules/tables.css,/styles/v4/modules/bank.css,/styles/v4/order-common.css,/styles/v4/order.css" />
<link href="http://pic.lvmama.com/min/index.php?f=/styles/v5/modules/button.css" rel="stylesheet" type="text/css">
<link rel="stylesheet" href="/vst_order/css/ticketOrder.css" type="text/css" />
<link rel="stylesheet" href="http://pic.lvmama.com/styles/v6/wifi/wifi.css">
<link rel="stylesheet" href="/vst_order/css/order_pay.css" type="text/css"/>
<link rel="stylesheet" href="http://pic.lvmama.com/styles/v6/order.css">
</head>
<body>
	<#--页面导航-->
	<div class="iframe_header">
	        <i class="icon-home ihome"></i>
	        <ul class="iframe_nav">
	            <li><a href="#">首页</a> &gt;</li>
	            <li><a href="#">订单管理</a> &gt;</li>
	            <li class="active">新建wifi订单</li>
	        </ul>
	</div>
	<div id="userInfoDiv" style="text-align:center;position:fixed;margin-left:500px;margin-top:-30px;background:#F9FAFB;"> 
		<#if user.userId?? >
		    <span style="color: #EE3388;font-size:22px;font-weight: bold;">客人姓名：${user.userName}</span>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="accountLogout();">退出当前用户</a>
		<#else>
			<span style="color: #EE3388;font-size:22px;font-weight: bold;">尚未登陆会员信息</span>
		</#if>
	</div>
	
	<div class="iframe_content">
        <div class="hotel_title">
        	<div class="hotel_t">
            		<input type="hidden" id="payTarget" name="payTarget" value="${suppGoods.payTarget}"/>
            		<input type="hidden" id="productIdTxt" name="productId" value="${suppGoods.prodProduct.productId}" autocomplete="off"/>
	        		<a>产品名称：${suppGoods.prodProduct.productName}</a>
            </div>
            <div class="operate ml50 hotel_link">
		    	<a id="backSearchA" class="btn btn_cc1" href="/vst_order/ord/productQuery/wifi/showWifiQueryList.do?userId=${user.userId}">返回搜索</a>
		    </div>
        </div>
		<#--表单区域-->
		<form id="orderForm" name="orderForm" method="POST" action="/vst_order/ord/book/wifi/createOrder.do">
		<input type="hidden" name="additionalTravel" value="true" autocomplete="off"/>
		<input type="hidden" name="userId" value="${user.userId}"/>
		<input type="hidden" name="userNo" value="${user.id}"/>
		<input type="hidden" id="productIdTxt" name="productId" value="${suppGoods.prodProduct.productId}" autocomplete="off"/>
		<div class="p_box mt20">
			<div class="hotel_wrap">
	        	<h5 class="hotel_tab_title">游玩日期</h5>
	            <div class="hotel_tab">
	            	请选择游玩日期：
	            	<input id="visitTimeInput" type="text" data-check="checkIn" autocomplete="off" readonly name="itemMap[${suppGoods.suppGoodsId}].visitTime"  value="${startDate?string('yyyy-MM-dd')!''}" class="J_calendar js_youwanTime  cal_style" placeholder="请选择开始日期" required>
	            </div>
	        </div>
		</div>
		<div class="p_box mt20">
			<h5 class="hotel_tab_title">商品选择</h5>
            <div id="ticketDiv" class="hotel_tab">
            	<div class="table_t">主商品选择</div>
	 			<#include "/order/wifi/inc/goods_info.ftl"/>
             </div>
             <#if suppGoods.goodsType =="EXPRESSTYPE_DISPLAY">
             <#include "/order/wifi/inc/mail_wifi.ftl"/>
             </#if>
      		<#include "/order/wifi/inc/oder_pay.ftl"/>
	        </div>
  			 <#include "/order/wifi/inc/order_info.ftl"/>
        </div>
        </form>
	</div>
	<#include "/order/orderProductQuery/member_dialog.ftl"/>  
	<#include "/base/foot.ftl"/>
	<script src="http://s3.lvjs.com.cn/min/index.php?f=/js/ui/lvmamaUI/lvmamaUI.js,/js/new_v/top/header-air_new.js,/js/v4/modules/pandora-dialog.js,/js/v4/login/rapidLogin.js"></script>
	<script src="http://pic.lvmama.com/min/index.php?f=/js/v5/modules/pandora-poptip.js"></script>
	<script type="text/javascript" src="http://pic.lvmama.com/min/index.php?f=/js/v6/modules/pandora-calendar.js"></script>
	<script src="http://pic.lvmama.com/js/v6/order.js"></script>
	<script src="/vst_order/js/book/wifi/bookWifi.js"></script>
	<script src="/vst_order/js/tooltip/js/jtip.js"></script>
	<script src="/vst_order/js/book/bookMemberLogin.js?version=2014081112345"></script>
	<script src="/vst_order/js/book/wifi/bookCheck.js?version=2014091412345"></script>
	<script src="/vst_order/js/book/order_promotion.js?version=2014091412345"></script>
	<script src="/vst_order/js/book/coupon_allCategroy.js?version=2014091412345"></script>
	<script src="/vst_order/js/book/express.js?2014091512345"></script>
	<script src="/vst_order/js/book/wifi/payment_page.js"></script>
    
<script>
	var basePath = '${rc.contextPath}';
	var selectUserDialog;
	var book_user_id='${user.userId}';
	var basepath = '${rc.contextpath}';
	var rootpath = '${rc.contextpath}';
	var thisProCategoryId = '${suppgoods.prodproduct.bizcategory.categoryid}';
	var orderInfoDialog;
	$(function(){
			//日历初始化
	          var jsonData = { 
					trigger: ".J_calendar",
					mos:12,//传递日历显示日期 
					template: "small", //小日历  small
					isTodayClick:true, //当天是否可点击 
					triggerEvent: "click",
					cascade: {
               			days:1,
                		trigger: ".J_calendar"
            		},
					showPrev: true, // 控制上个月翻月按钮
            		showNext: true, // 控制下个月翻月按钮
					selectDateCallback:selectDateCallback,
					completeCallback: function(){
					},
					sourceFn:souceFnCallBack
					
				}
			pandora.calendar(jsonData);
    		BACK.BOOK.CHECK.checkStock();
    		VST.WIFI.ORDER.bindHotelSum();
    		Express.showExpressageInfo();
    		loadJtip();
			bookNumOnchangeCallback.pushFun(BACK.BOOK.CHECK.checkStock);
			bookNumOnchangeCallback.pushFun(Express.showExpressageInfo);
			calendarConfirmCallback.pushFun(Express.showExpressageInfo);
			calendarConfirmCallback.pushFun(BACK.BOOK.CHECK.checkStock);
			submitFormCallback.pushFun(submitOrder);
			toMyOrder();
    });
    
    
    function toMyOrder(){
        $(".myOrder").each(function(index){
            //alert(index);
            $(this).html('您可在<a href="http://www.lvmama.com/myspace/order.do" rel="nofollow" hidefocus="false">“<u>我的订单</u>”</a>中申请。');
        })
    }
</script>
</body>
</html>