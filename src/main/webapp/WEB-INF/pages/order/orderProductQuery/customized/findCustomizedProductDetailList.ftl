<#--页眉-->
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-后台线路下单</title>
<#include "/base/head_meta.ftl"/>
<link href="http://pic.lvmama.com/styles/youlun/ui-cruise.css" rel="stylesheet" /> 
<link href="http://pic.lvmama.com/styles/youlun/ui-components.css" rel="stylesheet" /> 
<link href="http://pic.lvmama.com/styles/youlun/iframe.css" rel="stylesheet" /> 

<link rel="stylesheet" href="/vst_order/css/order_pay.css" type="text/css"/>
<link rel="stylesheet" href="http://pic.lvmama.com/styles/v6/order.css">

<link rel="stylesheet" href="/vst_order/js/book/calendar.css" type="text/css"/>
<link rel="stylesheet" href="/vst_order/js/tooltip/css/global.css" type="text/css" />
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/pa-base.css,/styles/v4/modules/arrow.css,/styles/v4/modules/button.css,/styles/v4/modules/forms.css,/styles/v4/modules/selectbox.css,/styles/v4/modules/step.css,/styles/v4/modules/tags.css,/styles/v4/modules/tip.css,/styles/v4/modules/dialog.css,/styles/v4/modules/tables.css" />
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/modules/dialog.css,/styles/v4/modules/calendar.css,/styles/v4/modules/tables.css,/styles/v4/modules/bank.css,/styles/v4/order-common.css,/styles/v4/order.css" />
<link rel="shortcut icon" type="image/x-icon" href="http://www.lvmama.com/favicon.ico">
<link rel="stylesheet" href="/vst_order/css/ticketOrder.css" type="text/css" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="keywords" content="">
<meta name="description" content="">
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v5/modules/dialog.css" type="text/css">

<!--  样式外加部分 -->
<link href="http://pic.lvmama.com/min/index.php?f=/styles/v5/modules/button.css" rel="stylesheet" type="text/css">
<link rel="stylesheet" href="/vst_order/js/book/line.css" type="text/css">
<link rel="stylesheet" href="http://s2.lvjs.com.cn/styles/line/detail/product-detail.css" type="text/css">

<style> 
	.ui-calendar{ width:1200px;} 
	.ui-calendar .calmonth{ width:650px;} 
	.calmonth .caltable{ height:500px;} 
</style> 
</head>
<body>
<#--页面导航-->
	
	<#include "/order/orderProductQuery/customized/line_head.ftl"/>
	<#include "/order/orderProductQuery/line/line_notice.ftl"/>
	<div class="iframe_content">
		  <form id="bookForm" action="" method="post">
		  	<input type="hidden" name="specDate" >
		  	<input type="hidden" name="productId" >
		  	<input type="hidden" name="distributionId" >
		  	<input type="hidden" name="userId" >
		  	<input type="hidden" name="adultNum" >
		  	<input type="hidden" name="childNum" >
		  	<input type="hidden" name="copies" >
		  	<input type="hidden" name="additionMap[visaDocLastTime]" value="${visaDocLastTime?string('yyyy-MM-dd')!''}" autocomplete="off"/>
		  </form>
	<#--提交订单的Form-->
		<form id="orderForm" name="orderForm" action="/vst_order/ord/order/lineBackCreateOrder.do" method="POST">
			<input type="hidden" name="intentionOrderId" value="${intentionOrderId}"/>
			<input type="hidden" id="userId" name="userId" value="${userId}"/>
			<input type="hidden" id="productId" name="productId" value="${productId}"/>
			<input type="hidden" name="adultQuantity" value="${adultNum}"/>
			<input type="hidden" name="childQuantity" value="${childNum}"/>
            <input type="hidden" name="categoryId" value="${prodProduct.bizCategory.categoryId}" autocomplete="off"/>
            <input type="hidden" name="subCategoryId" value="${prodProduct.subCategoryId!''}" autocomplete="off"/>
            <input type="hidden" name="additionMap[visaDocLastTime]" value="${visaDocLastTime?string('yyyy-MM-dd')!''}" autocomplete="off"/>
            <#if prodLineRoute>
             <input type="hidden" name="lineRouteId" value="${prodLineRoute.lineRouteId}"/>
            </#if>
           
            
		
			<input type="hidden" name="travellerDelayFlag" value="${(isTravellerDelay?? && isTravellerDelay)?string('Y','N')}"/>
		
			<!--这里显示查询条件信息-->
			<#if isHotel || saleCopies>
				<#include "/order/orderProductQuery/line/queryDivHotel.ftl"/>  
			<#else>
				<#include "/order/orderProductQuery/line/queryDiv.ftl"/> 
			</#if>
			<!--这里显示库存不足的提示信息-->
			<#if !(prodLineRoute??)>
			  <span class="error" style="margin-left:100px;font-size:14px;margin-top:10px;float:left;">库存不足，请重新选择日期!</span>
			    <!--如果库存为空，则加入不请求优惠券的标志值-->
			  <input type="hidden" id="notInsertCouponFlag"  value="Y"/>
			</#if>
			<!--这里价格信息 -->
	  		<#include "/order/orderProductQuery/line/coupon.ftl"/>  
			 <!--这里显示主要产品信息-->
	      	<#include "/order/orderProductQuery/line/mainlist.ftl"/>    
	      	 <!--这里显示酒店套餐产品信息-->      		
	      	<#include "/order/orderProductQuery/line/firstHotelcomlist.ftl"/>    
	      	
	      	<!--这里显示附加信息-->
	      	<#include "/order/orderProductQuery/line/addition.ftl"/> 
	      	
	      	<!--这里显示关联销售-->
	  		<#include "/order/orderProductQuery/line/relationAll.ftl"/> 
	  		<#if existsInsurance>
	    	<div id="insuranceDiv">
	  		<#include "/order/ticket/inc/insurance.ftl"/>
	  		</div>	
	  		</#if>
	  		
	  		<!---这里是买赠信息-->
			<#include "/order/coupon/buy_present.ftl"/>
	    	<!--优惠模块-->
	    	<div>
	    	
      		<#--<#include "/order/coupon/coupon.ftl"/>-->
      		<#include "/order/payment/order_pay.ftl"/>
	    	</div>
	    	<!--这里显示备注信息-->
	  		<#include "/order/orderProductQuery/customized/orderRemark.ftl"/>
	  		
	  		<!--是否存在快递信息-->
	  		<div id="expressInfoDiv" hasExpress="<#if hasExpress?exists && hasExpress >1<#else>0</#if>" >
	  		<#if hasExpress?exists && hasExpress >
		 	<#include "/order/express_info.ftl"/>
	  		</#if>
	  		</div>
	  		
	  		<br/>
	  		<h5 class="hotel_tab_title">订单信息</h5>
	 		<div class="hotel_tab">
		  		 <div id="totalOrderPriceDiv" style="color:red;">
			    	<p><b style="font-size:14px;font-weight: bold;">订单总价：</b>产品费用0元+保险0元-优惠券0元-促销活动0元=0元</p>
			    </div>
			    <br/>
                <div>
                    <b style="font-size:14px;font-weight: bold;color:red;">*</b>是否从驴途意向单产生的订单：
                    <#if intentionOrderId?exists >
                      <select name="intentionOrderFlag" id="intentionOrderFlag" >
                        <option selected="selected" value="Y">是</option>
                    </select>
                    <#else>
                    <select name="intentionOrderFlag" id="intentionOrderFlag">
                        <option value="NONE">请选择</option>
                        <option value="Y">是</option>
                        <option selected="selected" value="N">否</option>
                    </select>
                    </#if>
                    <span id="iOrderFlag"></span>
                </div>
			 	<table width="100%">
		    	<tbody>
		        	<tr>
		            	<td width="55%"><span class="table_t" style="font-weight: bold;">退改规则</span>:<span id="cancelStrategyTd"></span></td>
		                <td width="5%"></td>
		                <td width="10%"class="orange"></td>
		                <td width="10%">

		                </td>
		                <td width="10%" class="orange"></td>
		                <td></td>
		            </tr>
		        </tbody>
		    </table>
			 </div>
	  		<div class="operate mt20" style="text-align:center"><a class="syzbq">上一步</a><a class="btn btn_cc1" id="orderSubmitA" submitFlag="true">保存订单</a></div>
	 	</form>    
            
 	    <!--这里显示酒店套餐产品信息-->
      	<#include "/order/orderProductQuery/line/hotelcomlist.ftl"/> 
  </div>
        
  <script type="text/javascript" src="http://pic.lvmama.com/js/youlun/jquery.birthcalendar.js"></script>
  <script type="text/javascript" src="/vst_order/js/book/coupon.js"></script>
  <script src="/vst_order/js/book/customized/line.js"></script>
  <script src="/vst_order/js/book/express.js?2014091512345"></script>
  <#include "/order/orderProductQuery/member_dialog.ftl"/> 
</body>
</html>
<#include "/base/foot.ftl"/>
<script src="/vst_order/js/book/bookMemberLogin.js?version=2014081112345"></script>
<script type="text/javascript">
	var basePath = '${rc.contextPath}';
	var rootPath = '${rc.contextPath}';
	var thisProCategoryId = '${prodProduct.bizCategory.categoryId}'; 
</script>
<script src="/vst_order/js/book/bookCheck.js?version=2014091412345"></script>
<script src="/vst_order/js/book/order_promotion.js?version=2014102712345"></script>
<script src="http://s3.lvjs.com.cn/min/index.php?f=/js/ui/lvmamaUI/lvmamaUI.js,/js/new_v/top/header-air_new.js,/js/v4/login/rapidLogin.js"></script>
<script src="http://pic.lvmama.com/min/index.php?f=/js/v4/modules/pandora-dialog.js,/js/v6/modules/pandora-calendar.js"></script>
<script src="http://pic.lvmama.com/min/index.php?f=/js/v5/modules/pandora-poptip.js"></script>
<script>
var selectUserDialog;
var book_user_id='${userId}';
var loading;
var gapPrice='${gapPrice}';
var current_product="line";
</script>   

	<script src="http://pic.lvmama.com/js/v6/order.js"></script>
    
  <script>
  
  
  	 countTotalPrice();//总价计算
			var myDatestr =${startDaeResult};
			//console.log(myDatestr);
			//myDatestr = myDatestr.replace(/-/g,"/");
			//console.log(myDatestr);
		 	var mydate = new Date(myDatestr);
		  	$(function(){
        	pandora.calendar({ 
        			date:mydate,
					trigger: ".js_youwanTime", 
					triggerClass: "js_youwanTime", 
					template: "big", //小日历  small
					mos:${mothResult},
					isTodayClick:true, //当天是否可点击 
					frequent:true,
					selectDateCallback: function(){ 
						  requestProduct();
					},//点击选择日期后的回调函数 
					completeCallback:function(){
						var date=$("#visitTime").val();
						$(".ui-calendar").find("td[date-map]").children("div").removeClass("today");
						$(".ui-calendar").find("td[date-map='"+date+"']").children("div").addClass("today");
					},
					sourceFn:function (cal){
					$(".ui-calendar").find("td[date-map]").children("div").addClass("nodate");
					//alert(this.date.getFullYear());
					//alert(this.date.getMonth());
						var currentDate=$('.mtitle').eq(0).text();
					     var tdElement=cal.warp.find("td");
					      var productId=$("#productId").val();
					      
					      $.post("/vst_order/ord/order/route/CalendarJsonData.do",{productId:productId,currentDate:currentDate},function(data){
					  		if(data.success){
					  			$(data.attributes.timePriceList).each(function(i,e){
					  				$(tdElement).each(function(t,v){
					  					var specDate=e.departureDate;
					  					//var routeName=e.prodLineRoute.routeName;
					  					var date=$(v).attr("date-map");;
						  				if(date==specDate){
						  				/*
					  						if(e.stock>=10){
					  							$(v).find("div span:eq(1)").html("充足");
					  						}else{
					  							if(e.stock>0){
					  								$(v).find("div span:eq(1)").html(e.stock);
					  							}else if(e.stock==-1){
					  								$(v).find("div span:eq(1)").html("紧张");
					  							}
					  						}*/
					  						$(v).children("div").removeClass("nodate");
					  						var price="";
					  						if(e.lowestSaledPriceYuan != ""){
					  								price+="成:"+e.lowestSaledPriceYuan;
								            }
								            if(e.lowestSaledChildPriceYuan != ""){
								                   price+="童:"+e.lowestSaledChildPriceYuan;
								            }
						  					$(v).find("div span:eq(2)").html(price);
						  					//暂时注释掉线路行程名称显示
						  					//$(v).find("div span:eq(3)").html(routeName);
						  				}
						  			});
					  			});
					  			
					  		}else{
					  		}
					  	},"JSON");
					}
				});
			});
  	var isSubmit = false;
  	//初始化查询条件信息
  	initPage();  
  	
  	
  	//上一步事件  	
  	$(".syzbq").bind("click",function(){
  		window.history.back(-1);
  	});  	
	// dialog全局配置
	(function (d) {
		d["skin"] = "dialog-blue";
		d["mask"] = false;
		d["okClassName"] = "btn-ok";
		d["drag"] = true;
		d["cancelClassName"] = "btn-cancel";
	}(pandora.dialog.defaults));
	
	
	//是否使用优惠券
	$('input[type=radio][name=youhui]').bind('click',function(){
		var v=$(this).val();
		$("#couponChecked").val(v);
		if(v=="false"){
			 
		}
		if(v=="bonus"){
			$("#bonus_number").val("0.00");
			$("#couponCode").val("");
			$("#couponInfoMsg").html("");
		}
		if(v=="coupon"){
			$("#bonus_number").val("");
			$("#bonusInfoMsg").html("");
		}
		lineBackCheckStock();
	});
	
  </script>
  <script src="/vst_order/js/payment_page.js"></script>