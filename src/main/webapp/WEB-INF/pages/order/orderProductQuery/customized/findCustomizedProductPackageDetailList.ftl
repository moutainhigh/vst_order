<#--页眉-->
<#import "/base/spring.ftl" as s/>
<#import "/order/orderProductQuery/line/trafficFlightInfo.ftl" as trafficFlightInfo>
<#import "/order/orderProductQuery/line/trafficTrainInfo.ftl" as trafficTrainInfo>
<#import "/order/orderProductQuery/line/trafficBusInfo.ftl" as trafficBusInfo>
<#import "/order/orderProductQuery/line/lineTransportdetail.ftl" as lineTransportdetail>
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
<link href="http://pic.lvmama.com/min/index.php?f=/styles/v5/modules/button.css,/styles/v5/modules/tip.css" rel="stylesheet" type="text/css">
<link rel="stylesheet" href="/vst_order/js/book/line.css" type="text/css">
<link rel="stylesheet" href="/vst_order/js/book/line_traffic.css" type="text/css">

<style> 
	.ui-calendar{ width:1300px;} 
	.ui-calendar .calmonth{ width:650px;} 
	.calmonth .caltable{ height:500px;} 
</style> 
</head>
<body>
	<#include "/order/orderProductQuery/customized/line_head.ftl"/>
	<#include "/order/orderProductQuery/line/line_notice.ftl"/>
<#--提交订单的Form-->
	  <form id="bookForm" action="" method="post">
	  	<input type="hidden" name="specDate" >
	  	<input type="hidden" name="productId" >
	  	<input type="hidden" name="distributionId" >
	  	<input type="hidden" name="userId" >
  	  	<input type="hidden" name="adultNum" >
	  	<input type="hidden" name="childNum" >
	  	<input type="hidden" name="copies" >
        <input type="hidden" name="startDistrictId" >
        <input type="hidden" name="additionMap[visaDocLastTime]" value="<#if visaDocLastTime??>${visaDocLastTime?string('yyyy-MM-dd')}</#if>" autocomplete="off"/>
	  </form>
		
	<#assign productItemIdIndex=0 />
	<script>
	 	var productItemIdIndex='${productItemIdIndex}';
	</script>
	<form id="orderForm" name="orderForm" action="/vst_order/ord/book/comm/createOrder.do" method="POST">
		<input type="hidden" id="userId" name="userId" value="${userId}"/>
		<input type="hidden" id="productId" name="productId" value="${productId}"/>
		<input type="hidden" name="adultQuantity" value="${adultNum}"/>
		<input type="hidden" name="childQuantity" value="${childNum}"/>
		<input type="hidden" name="intentionOrderId"  id="intentionOrderId" value="${intentionOrderId}"/>
        <input type="hidden" name="categoryId" value="${prodProduct.bizCategory.categoryId}" autocomplete="off"/>
        <input type="hidden" name="additionMap[visaDocLastTime]" value="<#if visaDocLastTime??>${visaDocLastTime?string('yyyy-MM-dd')}</#if>" autocomplete="off"/>
        <!-- 子品类添加(yangdechao) -->
        <input type="hidden" name="subCategoryId" value="${prodProduct.subCategoryId!''}" autocomplete="off"/>
		<#if prodLineRoute>
			<input type="hidden" name="lineRouteId" value="${prodLineRoute.lineRouteId}"/>
		</#if>
		<#if prodProduct.muiltDpartureFlag?? && 'Y'==prodProduct.muiltDpartureFlag>
			<#if startDistrictVo?? && startDistrictVo.startDistrict>
				<input type="hidden" id="startDistrictId" name="startDistrictId" value="${startDistrictVo.startDistrict.districtId}"/>
			</#if>
		</#if>
		
		<input type="hidden" name="travellerDelayFlag" value="${(isTravellerDelay?? && isTravellerDelay)?string('Y','N')}"/>
		
		<!--这里显示查询条件信息-->
		<#if isHotel || saleCopies>
			<#include "/order/orderProductQuery/line/queryDivHotel.ftl"/>  
		<#else>
			<#include "/order/orderProductQuery/line/queryDiv.ftl"/> 
		</#if> 
		 
		 <!--这里价格信息 -->
  		<#include "/order/orderProductQuery/line/coupon.ftl"/>  
		<!--这里显示主要产品信息-->
      	<#include "/order/orderProductQuery/line/mainlistU.ftl"/> 
      	
      	<!--这里显示主要自主打包大交通信息-->
      	<#include "/order/orderProductQuery/line/linetransport.ftl"/> 
      	
      	<!--这里显示主要自主打包酒店信息-->
	    <#include "/order/orderProductQuery/line/linehotel.ftl"/> 
	    
	    <!--这里显示主要自主打包门票信息-->
      	<#include "/order/orderProductQuery/line/lineticket.ftl"/> 
      	
      	<!--这里显示主要自主打包线路信息酒店套餐-->
	    <#include "/order/orderProductQuery/line/line_hotelcom.ftl"/>
      	
      	<!--这里显示主要自主打包线路信息当地游-->
	    <#include "/order/orderProductQuery/line/line_local.ftl"/> 
	    
	    <!--这里显示主要自主打包线路跟团游信息-->
	    <#include "/order/orderProductQuery/line/line_tour.ftl"/> 
	    
	    <!--这里显示主要自主打包线路信息自由行-->
	    <#include "/order/orderProductQuery/line/line_self.ftl"/> 
      	<!--这里显示主要更换酒店信息-->
      	<#include "/order/orderProductQuery/line/changeHotel.ftl"/>  
      	<!--这里显示主要升级产品信息-->
      	<#include "/order/orderProductQuery/line/update.ftl"/>      
      	
      	<!--这里显示附加信息-->
      	<#include "/order/orderProductQuery/line/additionU.ftl"/>  
  		
  		<!--这里显示关联销售-->
  		<#include "/order/orderProductQuery/line/relationAllU.ftl"/> 
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
  		<#if packageTourProductVo??&&packageTourProductVo.packageType=="SUPPLIER">
	  		<#if trafficInfoList??>
	  		<table width="100%">
				<tbody>
				  	<tr>
			    		<td style="font-size:16px;">
			    			<b>上车地点</b>：
			    		<td>
		                <td></td>
		                <td></td>
		                <td>
		                </td>
		                <td></td>
		                <td></td>
			    	</tr>
			    	<#list trafficInfoList as traffic>
			    		<#if traffic['trafficMap']['bus']??>
				            <#list traffic['trafficMap']['bus'] as bus>
					         <tr>
					         	<td width="10%" style="font-size:16px;">
					    		<td>
				                <td><input class="radio" name="additionMap[frontBusStop]" value="上车地点：${bus['address']};发车时间：${bus['startTime']};备注：${bus['memo']}"
							                   		<#if bus_index==0>checked="checked"</#if> type="radio" />
							                   		上车地点：${bus['address']} <br/>&nbsp;&nbsp;&nbsp;&nbsp;发车时间：${bus['startTime']}</span><span class="ts_text"> <br/>&nbsp;&nbsp;&nbsp;&nbsp;备注：${bus['memo']}<br/></td>
				                <td></td>
				                <td>
				                </td>
				                <td></td>
				                <td></td>
					        </tr>
					        </#list>
					    </#if>
			        </#list>
			    </tbody>
			</table>
			</#if>
		</#if>
		
		<!--是否存在快递信息-->
  		<div id="expressInfoDiv" hasExpress="<#if hasExpress?exists && hasExpress >1<#else>0</#if>" >
  		<#if hasExpress?exists && hasExpress >
	 	<#include "/order/express_info.ftl"/>
  		</#if>
  		</div>
  		
  		<br/>
  		<h5 class="hotel_tab_title">订单信息</h5>
 		<div class="hotel_tab">
	  		 <div id="totalOrderPriceDiv">
		    	<p><b style="font-size:14px;font-weight: bold;">订单总价：</b>产品费用0元+保险0元-优惠券0元-促销活动0元=0元</p>
		    </div>
            <div>
                <b style="font-size:14px;font-weight: bold;color:red;">*</b>是否从驴途意向单产生的订单：
                  <#if intentionOrderId?exists >
                      <select name="intentionOrderFlag" id="intentionOrderFlag">
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
  		<div class="operate mt20" style="text-align:center"><a class="syzbq">上一步</a><a class="btn btn_cc1"  id="orderSubmitA" submitFlag="true">保存订单</a></div>
   </form>    
   		
   		 <!--这里显示主要自主打包大交通信息-->
      	<#include "/order/orderProductQuery/line/linetransport_moreProduct.ftl"/> 
      	<!--这里显示主要自主打包更多酒店信息-->
      	<#include "/order/orderProductQuery/line/linehotel_moreProduct.ftl"/> 
      	<!--这里显示主要自主打包门票更多信息-->
      	<#include "/order/orderProductQuery/line/lineticket_moreProduct.ftl"/>
      	<!--这里显示主要自主打包酒店套餐更多信息-->
      	<#include "/order/orderProductQuery/line/line_hotelcom_moreProduct.ftl"/> 
      	<!--这里显示主要自主打包当地游更多信息-->
      	<#include "/order/orderProductQuery/line/line_local_moreProduct.ftl"/> 
      	<!--这里显示主要自主打包线路跟团游更多信息-->
	    <#include "/order/orderProductQuery/line/line_tour_moreProduct.ftl"/> 
	    <!--这里显示主要自主打包线路自由行更多信息-->
	    <#include "/order/orderProductQuery/line/line_self_moreProduct.ftl"/> 	
	    <!--这里显示主要自主打包线路可换酒店更多信息-->
	    <#include "/order/orderProductQuery/line/changeHotel_moreProduct.ftl"/> 

<!--弹出航班信息-->
<table class="plane_type_box">
	<tr>
    	<th>计划机型</th>
        <th>机型名称</th>
        <th>类型</th>
        <th>最少座位数</th>
        <th>最多座位数</th>
    </tr>
    <tr></tr>
</table>
	<script>
		window.lineDetail = {};
	</script>
	<script src="http://s1.lvjs.com.cn/min/index.php?f=/js/bower_components/jquery/jquery.min.js"></script>
	<#--<script src="http://s3.lvjs.com.cn/min/index.php?f=/js/line/product-detail.js"></script>-->
  <script type="text/javascript" src="http://pic.lvmama.com/js/youlun/jquery.birthcalendar.js"></script>
  <script type="text/javascript" src="/vst_order/js/book/coupon.js"></script>
  <script src="/vst_order/js/book/customized/line.js"></script>
  <script src="/vst_order/js/book/express.js?2014091512345"></script>
  <#include "/order/orderProductQuery/member_dialog.ftl"/>   
		<#include "/base/foot.ftl"/>
		
		<script src="/vst_order/js/book/order_promotion.js?version=2014102712345"></script>
		<script type="text/javascript">
			var basePath = '${rc.contextPath}';
			var rootPath = '${rc.contextPath}';
			var thisProCategoryId = '${prodProduct.bizCategory.categoryId}'; 	
		</script>
		<script src="http://s3.lvjs.com.cn/min/index.php?f=/js/ui/lvmamaUI/lvmamaUI.js,/js/new_v/top/header-air_new.js,/js/v4/login/rapidLogin.js"></script>
		<script src="http://pic.lvmama.com/min/index.php?f=/js/v4/modules/pandora-dialog.js"></script>
		<script src="http://pic.lvmama.com/min/index.php?f=/js/v6/modules/pandora-calendar.js"></script>
    	<script src="http://pic.lvmama.com/min/index.php?f=/js/v5/modules/pandora-poptip.js"></script>    	
    	<script src="http://pic.lvmama.com/js/v6/order.js"></script>
    	<script src="/vst_order/js/payment_page.js"></script>
    	<script src="/vst_order/js/book/bookCheck.js?version=2014091412345"></script>    	
		<script type="text/javascript">
			var selectUserDialog;
			var book_user_id='${userId}';
			var loading;
			var gapPrice='${gapPrice}';
			var current_product="line";
		</script>    
		<script>
			var myDatestr =${startDaeResult};
		 	var mydate = new Date(myDatestr);
			
	  		$(function(){
	        	pandora.calendar({
        			date:mydate,
					trigger: ".js_youwanTime",
					triggerClass: "js_youwanTime",
					template: "big", //小日历  small
					mos:${mothResult},
					isTodayClick:true, //当天是否可点击
					frequent: true, // 单月显示
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
                        var currentDate = $('.mtitle').eq(0).text();
                        var tdElement = cal.warp.find("td");
                        var productId = $("#productId").val();
						var postData = { productId: productId,currentDate: currentDate};
                        var jStartDistrict = $("#queryStartDistrictId");
                        if(jStartDistrict && jStartDistrict.length>0){
                            postData.startDistrictId = jStartDistrict.attr("data-cityid");
                        }
                        $.post("/vst_order/ord/order/route/CalendarJsonData.do", postData, function (data) {
                            if (data.success) {
                                $(data.attributes.timePriceList).each(function (i, e) {
                                    $(tdElement).each(function (t, v) {
                                        var specDate = e.departureDate;
                                        var date = $(v).attr("date-map");
                                        if (date == specDate) {
                                            $(v).children("div").removeClass("nodate");
                                            var price = "";
                                            if (e.lowestSaledPriceYuan != "") {
                                                price += "成:" + e.lowestSaledPriceYuan;
                                            }
                                            if (e.lowestSaledChildPriceYuan != "") {
                                                price += "童:" + e.lowestSaledChildPriceYuan;
                                            }
                                            $(v).find("div span:eq(2)").html(price);
                                            //暂时注释掉线路行程名称显示
                                            //$(v).find("div span:eq(3)").html(routeName);
                                        }
                                    });
                                });

                            } else {
                            }
                        }, "JSON");
                    }
                });
            });
		  	var isSubmit = false;
		  	//初始化查询条件信息
		  	initPage();//这里面有计算总价，第一行计算总价代码已删除
		  	
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

             //详情页多出发地事件 by dxl 2015-6-25
         (function() {
             var $body = $('body'),
                     $selectSimu = $('.selectSimu'),
                     $opt = $('.selectSimu-opt'),
                     $ipt = $('.selectSimu .select-arrow'),
                     $selectValue = $('.select-value'),
                     $con = $('.selectSimu-optCon');

             //tab切换
             $('.selectSimu-optTab>span').click(function(e){
                 e.stopPropagation();
                 var $me = $(this);
                 $me.addClass('active').siblings().removeClass('active');
                 $con.eq($me.index()).addClass('active').siblings('.selectSimu-optCon').removeClass('active');

             });

             //展开收缩下拉
             $ipt.click(function(e){
				 $(document).trigger("click");
                 if($opt.is(":hidden")) {
                     $opt.show();
                     $selectSimu.addClass('active');
                 }else {
                     $opt.hide();
                     $selectSimu.removeClass('active');
                 }
                 e.stopPropagation();
             });

             //选择索引事件
             $opt.click(function(e){
                 e.stopPropagation();
                 var target = e.target;
                 if(target.nodeName == 'A') {
                     $selectValue.html($(target).text()).attr('data-cityid',$(target).attr('data-cityid'));
                     $opt.hide();
                     $selectSimu.removeClass('active');
//                     requestProduct();
                 }
             });

             //点空白地方去除显示下拉
             $body.click(function (){
                 if(!$opt.is(":visible")) return;
                 $opt.hide();
                 $selectSimu.removeClass('active');
             });

         })(jQuery);
		
		  </script>
</body>
</html>