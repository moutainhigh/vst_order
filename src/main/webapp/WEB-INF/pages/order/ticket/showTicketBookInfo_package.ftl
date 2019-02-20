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
	            <li class="active">新建门票订单</li>
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
	        	<#if productFlag??&&productFlag>
	        		<input type="hidden" id="payTarget" name="payTarget" value="PREPAID"/>
	        		<input type="hidden" id="aperiodicFlag" name="aperiodicFlag" value="false"/>
	        		
	        		<input type="hidden" id="productIdTxt" name="productId" value="${ticketCombProductVO.product.productId}" autocomplete="off"/>
	        		<a href="http://www.lvmama.com/vst_front/scenic-${ticketCombProductVO.product.productId}" target="_blank">产品名称：${ticketCombProductVO.product.productName}</a>
	            </#if>
            </div>
            <div class="operate ml50 hotel_link">
		    	<#--<a class="btn btn_cc1" id="notifyBt">重要通知</a>-->
		    	<a id="backSearchA" class="btn btn_cc1" href="/vst_order/ord/productQuery/ticket/showTicketQueryList.do?userId=${user.userId}">返回搜索</a>
		    </div>
        </div>
		<#--表单区域-->
		<form id="orderForm" name="orderForm" method="POST" action="/vst_order/ord/book/ticket/createOrder.do">
		<input type="hidden" name="sameVisitTime" value="true" autocomplete="off"/>
		<input type="hidden" name="additionalTravel" value="false" autocomplete="off"/>
		<input type="hidden" name="userId" value="${user.userId}"/>
		<input type="hidden" name="userNo" value="${user.id}"/>
		<input type="hidden" id="categoryId" name="categoryId" value="${categoryId}"/>
        <#if productFlag?? && productFlag &&(aperiodPackageFlag?? && aperiodPackageFlag=="Y")>
            <input type="hidden" id="visitDate" name="visitTime" value="${visitTime}" autocomplete="off"/>
        </#if>
        <#if productFlag?? && productFlag &&(aperiodPackageFlag?? && aperiodPackageFlag=="N")>
		<div class="p_box mt20">
			<div class="hotel_wrap">
	        	<h5 class="hotel_tab_title">游玩日期</h5>
	            <div class="hotel_tab">
	            	请选择游玩日期：<input type="text" id="visitDate" name="visitTime" style="width:100px;" value="${ticketCombProductVO.firstSaleTimePrice.specDate?string('yyyy-MM-dd')}"
	                    	 class="iflt_date" data-type="calendar" onblur="" readonly="readonly" required/>
	            </div>
	        </div>
		</div>
		</#if>
		<div class="p_box mt20">
			<h5 class="hotel_tab_title">商品选择</h5>
            <div id="ticketDiv" class="hotel_tab">
            	<div class="table_t">主商品选择</div>
	        	<#if productFlag??&&productFlag>
	        		<input type="hidden" name="productId" value="${ticketCombProductVO.product.productId}"/>
	        		<#include "/order/ticket/inc/comb_goods_info.ftl"/>
	            </#if>
             </div>
            <h5 class="hotel_tab_title">可选服务</h5>
            <div id="baoxianDiv" class="hotel_tab">
            	
            </div>
            <!---这里是买赠信息-->
			<#include "/order/coupon/buy_present.ftl"/>
            <!--优惠模块-->
      		<#--<#include "/order/coupon/coupon.ftl"/>-->
      		<#if prepaidFalg>
             <#include "/order/payment/order_pay.ftl"/>
            </#if>
	      </div>
  			<#include "/order/ticket/inc/order_info.ftl"/>
        </div>
        </form>
	</div>
	<#include "/order/ticket/inc/ticket_common.ftl"/>
	
	<script type="text/javascript">
		var basePath = '${rc.contextPath}';
		var rootPath = '${rc.contextPath}';
		var thisProCategoryId = '${categoryId}'; 
	</script>
	<script src="http://s3.lvjs.com.cn/min/index.php?f=/js/ui/lvmamaUI/lvmamaUI.js,/js/new_v/top/header-air_new.js,/js/v5/modules/pandora-poptip.js,/js/v6/modules/pandora-calendar.js,/js/v4/modules/pandora-dialog.js,/js/v4/login/rapidLogin.js"></script>
	<script src="http://pic.lvmama.com/js/v6/order.js"></script>
    <script src="/vst_order/js/payment_page.js"></script>
	<script type="text/javascript">
		$(function(){
		//日历初始化
	    pandora.calendar.init({ area: true, 
	    input: '.iflt_date',
	    inputClass: 'iflt_date',
	    showWeek:true,
	    selectDateCallback: function(){
					calendarConfirmCallback.invoke();
				}//点击选择日期后的回调函数 
	     });
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