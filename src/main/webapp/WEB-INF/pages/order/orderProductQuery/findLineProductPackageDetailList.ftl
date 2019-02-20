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
<link rel="stylesheet" href="http://pic.lvmama.com/styles/zt/bootstrap/bootstrap-3.3.7-min.css">
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
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=f=/styles/v4/modules/calendar.css,/styles/line/detail/product-detail-freetour.css,/styles/v5/modules/dialog.css">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="keywords" content="">
<meta name="description" content="">
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v5/modules/dialog.css" type="text/css">

<!--  样式外加部分 -->
<link href="http://pic.lvmama.com/min/index.php?f=/styles/v5/modules/button.css,/styles/v5/modules/tip.css" rel="stylesheet" type="text/css">
<link rel="stylesheet" href="/vst_order/js/book/line.css" type="text/css">
<link rel="stylesheet" href="/vst_order/js/book/line_traffic.css" type="text/css">
<style> 
	.ui-calendar{ width:1300px;;z-index:2!important;} 
	.ui-calendar .calmonth{ width:650px;} 
	.calmonth .caltable{ height:500px;} 
	input{height:26px!important;}
	.w8{padding:0!important;}
	.tooltip.tooltip2.bottom-left {opacity: 1!important;}
</style> 
</head>
<body>
	<#include "/order/orderProductQuery/line/line_head.ftl"/>
	<#include "/order/orderProductQuery/line/line_notice.ftl"/>
	<!-- 弹窗html开始 -->
	<div id="warnLoading" class="ft-price-loading" style="height: auto;" >
	    <div class="ft-dialog-inner clearfix">
	        <div class="ft-dialog-title"><a href="javascript:;" id="warnCancelBtn" class="ft-cancel-btn">&times;</a>温馨提示</div>
	        <div class="ft-dialog-body">
	            <div class="ft-content clearfix"><i></i><p style="padding-left:0px;background:none;display: block;height: auto;" id="warnInfo"></p></div>
	            <div class="ft-btn-group clearfix">
	                <a href="javascript:;" id="warnBtn" class="ft-knew-btn">确定</a>
	            </div>
	        </div>
	    </div>
	</div>
	<!-- 弹窗html结束 -->

    <#--下单错误打点统计-->
    <input type="hidden" id = "lineBackOrderTrackParam01" value="${lineBackOrderTrackParam01}" />
	
<#--提交订单的Form-->
	  <form id="bookForm" action="" method="post">
	  	<input type="hidden" name="fromSearch" value="false"/>
	  	<input type="hidden" name="specDate" >
	  	<input type="hidden" name="productId" >
	  	<input type="hidden" name="distributionId" >
	  	<input type="hidden" name="userId" >
  	  	<input type="hidden" name="adultNum" >
	  	<input type="hidden" name="childNum" >
	  	<input type="hidden" name="copies" >
        <input type="hidden" name="startDistrictId" >
        <input type="hidden" name="additionMap[visaDocLastTime]" value="<#if visaDocLastTime??>${visaDocLastTime?string('yyyy-MM-dd')}</#if>" autocomplete="off"/>
        <#--增加一键下单信息存储-->
		<input type="hidden" name="orderCreatingManner" value="${orderCreatingManner}" />
        <input type="hidden" name="originalOrderId" value="${originalOrderId}" />
        <input type="hidden" name="isTestOrder" value="${isTestOrder}" />
        <input type="hidden" name="channel_code" value="${channel_code}"/>
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
        <input type="hidden" name="productType" value="${prodProduct.productType!''}"/>
        <input type="hidden" name="distributorCode" value="${channel_code}"/>
		<#if prodLineRoute>
			<input type="hidden" name="lineRouteId" value="${prodLineRoute.lineRouteId}"/>
		</#if>
		<#if prodProduct.muiltDpartureFlag?? && 'Y'==prodProduct.muiltDpartureFlag>
			<#if startDistrictVo?? && startDistrictVo.startDistrict>
				<input type="hidden" id="startDistrictId" name="startDistrictId" value="${startDistrictVo.startDistrict.districtId}"/>
			</#if>
		</#if>
		
		<input type="hidden" name="travellerDelayFlag" value="${(isTravellerDelay?? && isTravellerDelay)?string('Y','N')}"/>

		<!-- 原订单信息 -->
		<input type="hidden" name="originalOrderId" id="originalOrderId" value="${originalOrderId}" />
		<input type="hidden" name="oldOughtAmount" id="oldOughtAmount" value="${oughtAmount}" data-check="${checkOldOughtAmount}" />
        <input type="hidden" name="oldDistributorId" value="${oldDistributorId}" />
        <input type="hidden" name="oldDistributorCode" value="${oldDistributorCode}" />
        <input type="hidden" name="oldDistributionChannel" value="${oldDistributionChannel}" />
		<#if unRecommendSuppGoods??>
			<input type="hidden" name="unRecommendSuppGoods" id="unRecommendSuppGoods" value="${unRecommendSuppGoods?html}"/>
		<#else>
			<input type="hidden" name="unRecommendSuppGoods" id="unRecommendSuppGoods" value=""/>
		</#if>
		<!--产品中的travellerDelayFlag标记的值，当机票切换时，只有全为非对接机票且prod_travellerDelayFlag='Y'才能为后置订单-->
		<input type="hidden" name="prod_travellerDelayFlag" value="${prodProduct.travellerDelayFlag}"/>
		<input type="hidden" name="ticketPersonNum" id="ticketPersonNum" value="0" />
		<!-- 前置锁仓 -->
        <input type="hidden" name="isPreLockSeat" value="${isPreLockSeat!''}"/>
        <input type="hidden" name="lockSetOrderId" value=""/>
        <input type="hidden" name="token" value=""/>
        <input type="hidden" name="flightTicketPrice" value=""/>
        <input type="hidden" name="newCountPrice" value="false"/>		
		<!--这里显示查询条件信息-->
		<#if (isHotel || saleCopies) && !isRoute>
			<#include "/order/orderProductQuery/line/queryDivHotel.ftl"/>  
		<#else>
			<#include "/order/orderProductQuery/line/queryDiv.ftl"/> 
		</#if> 
		<span id="changeTipInfo1" style="color: red;display: none">您的查询条件已变更，请重新查询产品信息！</span>
        <div   id="searchResultDiv">
		 <!--这里价格信息 -->
  		<#include "/order/orderProductQuery/line/coupon.ftl"/>  
		<!--这里显示主要产品信息-->
      	<#include "/order/orderProductQuery/line/mainlistU.ftl"/> 
      	
      	<!--这里显示主要自主打包大交通信息-->
      	<#include "/order/orderProductQuery/line/linetransport.ftl"/> 
      	
      	<!--这里显示主要自主打包酒店信息-->
      	<#if (prodProduct.productType == "INNERLINE" || prodProduct.productType == "INNERSHORTLINE" || prodProduct.productType == "INNERLONGLINE" || prodProduct.productType == "INNER_BORDER_LINE") && (prodProduct.getBizCategoryId() == 18 && prodProduct.getSubCategoryId() == 181 && prodProduct.packageType == "LVMAMA"  && prodProduct.bu == "DESTINATION_BU") >
	   	 <#include "/order/orderProductQuery/line/linehotel_JingJiu.ftl"/> 
	   	<#else>
	   	 <#include "/order/orderProductQuery/line/linehotel.ftl"/> 
		</#if>	
	    
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
        <div class="insuranceDiv">
            <H5 class="hotel_tab_title">以下为可选服务</H5>
        </div>    
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
  		<#include "/order/orderRemark.ftl"/>
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
            <!-- 驴途意向开始 -->
            <#include "/order/orderIntention.ftl"/>
            <!-- 驴途意向结束 -->
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
   </form>    
  		<div class="operate mt20" style="text-align:center"><a class="syzbq">上一步</a><button type="button" class="btn btn_cc1"  id="orderSubmitA" style="background-color:#2979FE;color:white;" submitFlag="true">下一步</button></div>
   		
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
</div>
	<script>
		window.lineDetail = {};
	</script>
	<script src="http://s1.lvjs.com.cn/min/index.php?f=/js/bower_components/jquery/jquery.min.js"></script>
	<#--<script src="http://s3.lvjs.com.cn/min/index.php?f=/js/line/product-detail.js"></script>-->
  <script type="text/javascript" src="http://pic.lvmama.com/js/youlun/jquery.birthcalendar.js"></script>
  <script type="text/javascript" src="/vst_order/js/book/coupon.js"></script>
  <script src="/vst_order/js/book/line.js?2018121000001"></script>
  <script src="/vst_order/js/book/express.js?2014091512345"></script>
  <script src="/vst_order/js/order/oneKeyOrderInsurance.js"></script>

  <#--预定失败打点统计-->
  <script src="http://s3.lvjs.com.cn/js/v5/ibm/eluminate.js"></script>
  <script src="http://s3.lvjs.com.cn/js/v5/ibm/coremetrics-initalize.js"></script>
  <script>_LVMAMA_COREMETRICS.init(document.domain);</script>

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
//成人数变化事件	
	$("#adultNum").change(function(){
	    $("#changeTipInfo1").css('display','block');
	    $("#searchResultDiv").css('display','none');
  	});  	
//儿童数变化事件	
  	$("#childNum").change(function(){
	    $("#changeTipInfo1").css('display','block');
	    $("#searchResultDiv").css('display','none');
  	});
//份数变化事件	
  	$("#hotelNum").change(function(){
	    $("#changeTipInfo1").css('display','block');
	    $("#searchResultDiv").css('display','none');
  	});
	
  	
function resetChildMaxQuantity(){
  <#if prodProduct.isLocalBuProduct() && !(prodProduct.getBizCategoryId() == 18 && prodProduct.getSubCategoryId() == 183) >
  	if($('#adultNum')){
		var CHILD_MIN_QUANTITY = $("#childNum option:first").val();
		var CHILD_MAX_QUANTITY = $("#childNum option:last").val();
	
		var adultQuantity = $("#adultNum").val();
		var childQuantity = $("#childNum").val();
	    if($("#childNum")){
	    	var child = $("#childNum");
	    	child.empty();
	    	var thisMaxQuantity = adultQuantity*2;
	    	if(9-adultQuantity < thisMaxQuantity ){
	        	for(var i=CHILD_MIN_QUANTITY;i<=9-adultQuantity;i++){
	        		var option = $("<option>").val(i).text(i);
					child.append(option);
	        	}
	        	if(9-adultQuantity<=0){
	        		child.empty();
	        		child.append($("<option>").val(0).text(0));
	        	}
	        	if(9-adultQuantity<childQuantity){
	        		child.val(0);
	        	}else{
	        		child.val(childQuantity);
	        	}
	    	}else{
	        	for(var i=CHILD_MIN_QUANTITY;i<=thisMaxQuantity;i++){
	        		var option = $("<option>").val(i).text(i);
					child.append(option);
	        	}
	        	if(thisMaxQuantity<childQuantity){
	        		child.val(0);
	        	}else{
	        		child.val(childQuantity);
	        	}
	   		 }
  		}
    }
</#if>
}
$(function(){
<#if prodProduct.isLocalBuProduct() && !(prodProduct.getBizCategoryId() == 18 && prodProduct.getSubCategoryId() == 183) >
	resetChildMaxQuantity();
</#if>	

<!-- 182只针对 自由行 机酒 国内 -->
<#if prodProduct.getSubCategoryId() == 182 && prodProduct.getProductType() == "INNERLINE">
	//tip提示
    $(".js_tip").poptip({
        offsetX: 0
    });
	<!-- 初始化 自由行 机酒 酒店 价格赋值 -->
	var machineWineTable = $(".machineWineTable select");
	$(machineWineTable).each(function(i) {
		var price = $(this).attr("goodsprice"); //价格
		var value = $(this).val(); // 份数
		<!-- 初始化赋值 份数 -->
		if(i == 0) {
			$(this).parent().find("input").attr("value", value);
		}
		var totalPrice= parseFloat(price) * parseInt(value) / 100;
		$(this).find("option[value="+$(this).val()+"]").attr("selected",true);
		$(this).parent().parent().find(".hoteltotalPrice").attr("totalAmountYuan",totalPrice.toFixed(2));
		$(this).parent().parent().find(".hoteltotalPrice").html("总价:￥"+totalPrice.toFixed(2)+"元");
	});
	calHotelCombPriceDiff(); //计算酒店金额
</#if>
		
		changeNextSelect(null,null);
		adultChildPrice();
	});
	
  //展开房型
    $(document).on('click','.btn_list_more',function(e){
      var $this = $(this),
          typeText = $this.attr('data-type')+'<i class="detail_icon detail_icon_jt1"></i>';

        var $thisStable = $this.siblings('table');
      if ($this.hasClass('list_more_close')) {
          $this.removeClass('list_more_close').html('更多'+typeText);
            if ($thisStable.find('th').length>0) {
                $thisStable.find('tr:gt(5)').hide();
            }else{
                //$thisStable.find('tr:gt(0)').hide();
                if ($this.attr('data-type')=='保险') {
                    var $thisTr = $thisStable.find('tr');
                    for (var i = 0; i < $thisTr.length; i++) {
                        if ($thisTr.eq(i).find('.choose_ok').length>0) {
                            $thisTr.eq(i).show().siblings('tr').hide();
                            break;
                        };
                    };    
                }else{
                    $thisStable.find('tr:gt(0)').hide();
                };
                
            };
          
            if (typeof moreCallback == 'function') {
                moreCallback($this);
            };
      }else{
          $this.addClass('list_more_close').html('收起'+typeText);
          $this.siblings('table').find('tr').show();
          $(".room_info").hide();
      };
    }); 
  
  $(document).on('click','.btn_hotel_list_more',function(e){
      var $this = $(this),
          typeText = $this.attr('data-type')+'<i class="detail_icon detail_icon_jt1"></i>';
      var $thisStable = $this.siblings('table');
      if ($this.hasClass('list_more_close')) {
          $this.removeClass('list_more_close').html('展开'+typeText);
          $this.prev().find(".machineWineTable tr").hide();
          $this.prev().find(".machineWineTable #selectedHotel").closest("tr").show();
      }else{
          $this.addClass('list_more_close').html('收起'+typeText);
          $this.prev().find(".machineWineTable tr").show();
          $this.prev().find(".room_info").hide();
      };
    });
    
  /**
 * 计算酒店套餐的差价
 */
function calHotelCombPriceDiff() {
    var $container = $(".machineWineTable");
    $container.each(function(index, e) {
        var $this = $(e);
        var $default = $this.find(".lvmama-price-flag");
        var selectedPrice = $default.attr("data-price-"+$default.val());
        $this.find(".room_num").each(function(index2, e2){
            var $current = $(e2);
            var currentPrice = $current.attr("data-price-"+$current.val());
            var price = currentPrice-selectedPrice;
            if(price%100 == 0){
                price = (price/100).toFixed(0);
            }else{
                price = (price/100).toFixed(2);
            }
            if(price>0){
                price = "差价￥ +"+price;
            }else if(price==0){
                price = "--";
            }else {
                price = "差价￥ "+price;
            }
            $current.closest("tr").find(".room_price").text(price);
        });
    });
}

  function adultChildPrice(){
     if($("#adultChildPrice_duijie").size()>0){
        $("#adultChildPrice_duijie").each(function(i,e){
           var toadultPrice = $(e).attr("toadultPrice");
		   var tochildPrice = $(e).attr("tochildPrice");
		   var backadultPrice = $(e).attr("backadultPrice");
		   var backchildPrice = $(e).attr("backchildPrice");
		   adultPrice = dealPrice(parseInt(toadultPrice)+parseInt(backadultPrice));
		   childPrice = dealPrice(parseInt(tochildPrice)+parseInt(backchildPrice));
		   var priceString = "成人：￥"+adultPrice+" X "+$(e).attr("adultNum")
		                        +"<br>儿童：￥"+childPrice+" X "+$(e).attr("childNum");
		   $(e).html(priceString);
        });
		   
	}
  }
  function dealPrice(price){
	if (price % 100 == 0) {
		price = (price / 100).toFixed(0);
	} else {
		price = (price / 100).toFixed(2);
	}
	return price;
  }
  function changeNextSelect(selectDate,hotel){
		var bizCategoryId = "${prodProduct.bizCategoryId}";
		var prodProductBu = "${prodProduct.bu}";
		var prodPackageType = "${prodProduct.packageType}";
		var prodProductType = "${prodProduct.productType}";
		var prodSubCategoryId = "${prodProduct.subCategoryId!''}"; 
		if(prodProductType == "INNERLINE" || prodProductType == "INNERSHORTLINE" || prodProductType == "INNERLONGLINE" || prodProductType == "INNER_BORDER_LINE"){
			if(bizCategoryId == "18" && prodSubCategoryId == "181" && prodPackageType == "LVMAMA"  && prodProductBu == "DESTINATION_BU" ){
				setTicketPersonNum();
				var hotelMaxNum = 0;
				$(".hotelSelect").each(function(){
					var select_value = $(this).val();
			        if (parseInt(select_value) > parseInt(hotelMaxNum)) {
			            hotelMaxNum = select_value;
			        }
				});
				var hotelComMaxNum = 0;
				if(hotel && hotel != null){
					var suppGoodsId="#"+$(hotel).attr("goodsId");
					if($(hotel).siblings(suppGoodsId+"adultQuantity")){
						var nameOfTotalNum = $(hotel).siblings(suppGoodsId+"adultQuantity").attr('name');
						if(nameOfTotalNum){
							nameOfTotalNum = nameOfTotalNum.replace('adultQuantity', 'quantity');
							$("[name='" + nameOfTotalNum + "']").val($(hotel).val());
							$(hotel).siblings(suppGoodsId+"adultQuantity").val($(hotel).val());
						}
					}
				}
				$(".hotelAdultNumText").each(function(){
					var select_value = $(this).val();
			        if (parseInt(select_value) > parseInt(hotelComMaxNum)) {
			            hotelComMaxNum = select_value;
			        }
				});
				if(parseInt(hotelComMaxNum) > parseInt(hotelMaxNum)){
					hotelMaxNum = hotelComMaxNum;
				}
				if(selectDate && selectDate !=null){
					if($(selectDate).attr("class") == "lineSelectDate"){
						var thisSelectQunan =$(selectDate).next(".selectQunantityDropdownListTicket");
						getTicketNumListFromAjax(thisSelectQunan,hotelMaxNum);
					}else if($(selectDate).attr("class") == "moreLineSelectDate"){
						var thisSelectQunan =$(selectDate).next(".selectmoreQunantityDropdownListTicket");
						return getmoreTicketNumListFromAjax(thisSelectQunan,hotelMaxNum);
					}else{
						var specDate = $(selectDate).find("option:selected").text();
						var thisSelectQunan =$(selectDate).parent().next().find(".selectQunantityDropdownList");
						getNumListFromAjax(thisSelectQunan,specDate,hotelMaxNum);
					}
				}else{
				    if($(".selectQunantityDropdownList")){
						$(".selectQunantityDropdownList").each(function(){
							var thisSelectQunan =  $(this);
							var specDate = thisSelectQunan.parent().prev().find(".selectDateDropdownList option:selected").text();
							getNumListFromAjax(thisSelectQunan,specDate,hotelMaxNum);
						});
					}
				    if($(".selectQunantityDropdownListTicket")){
						$(".selectQunantityDropdownListTicket").each(function(){
							var thisSelectQunan =  $(this);
							getTicketNumListFromAjax(thisSelectQunan,hotelMaxNum);
						});
					}
				    if($(".selectmoreQunantityDropdownListTicket")){
						$(".selectmoreQunantityDropdownListTicket").each(function(){
							var thisSelectQunan =  $(this);
							getmoreTicketNumListFromAjax(thisSelectQunan,hotelMaxNum);
						});
					}
				}
			}
		}
		return true;
	}
	
	function getmoreTicketNumListFromAjax(thisSelectQunan,hotelMaxNum){
		var goodsId = thisSelectQunan.attr("data-suppGoodsid");
		var specDateSelect = thisSelectQunan.prev(".moreLineSelectDate");
		var specDate = specDateSelect.find("option:selected").text();
		$.ajax({
		type: "POST",
		cache: false,
		async: false,
		dataType:"json",
	    url:"/vst_order/ord/order/changeStockSelectOption.do",
	    data:{suppGoodsId:goodsId,specDate:specDate,ticketMax:hotelMaxNum*8},
	    success:function(data){
	  		if(data.success){
				var thisValue = thisSelectQunan.parent().parent().find("#"+goodsId+"TotalNum").val();
				//处理关联销售的门票，如果默认是从0开始，则更新列表后，也是从0开始
				var firstOption = thisSelectQunan.find("option:first");
				var numList = [];
				if(data.attributes){
					if(data.attributes.selectNumList){
						numList = data.attributes.selectNumList;
					}
				}
				if(numList.length > 0){
					thisSelectQunan.empty();
					if(parseInt($(firstOption).val()) == 0 && parseInt(numList[0]) > 0){
						thisSelectQunan.append(firstOption);
					}
					if(thisSelectQunan.attr("data-reType") == "OPTIONAL"){
						for(var i=0;i<numList.length;i++){
				    		var option = $("<option>").val(numList[i]).text(numList[i]);
							thisSelectQunan.append(option);
				    	}
					}else{
						var option = $("<option>").val(numList[numList.length-1]).text(numList[numList.length-1]);
						thisSelectQunan.append(option);
					}
			    	if(parseInt(thisValue) > parseInt(numList[numList.length-1])){
			    		thisSelectQunan.val($(firstOption).val());
			    		thisSelectQunan.parent().parent().find("#"+goodsId_+"TotalNum").val($(firstOption).val());
			    	}else{
			    		thisSelectQunan.val(thisValue);
			    	}
		    		changeMoreSelectDateForSceneHotel(specDateSelect);
		    		return true;
				}
	  		}
		}
	    });
	    return false;
	}
	
	function getTicketNumListFromAjax(thisSelectQunan,hotelMaxNum){
		var goodsId = thisSelectQunan.attr("data-suppGoodsid");
		var specDateSelect = thisSelectQunan.prev(".lineSelectDate");
		var specDate = specDateSelect.find("option:selected").text();
		$.post("/vst_order/ord/order/changeStockSelectOption.do",{suppGoodsId:goodsId,specDate:specDate,ticketMax:hotelMaxNum*8},function(data){
	  		if(data.success){
				var thisValue = thisSelectQunan.parent().parent().find("#"+goodsId+"TotalNum").val();
				//处理关联销售的门票，如果默认是从0开始，则更新列表后，也是从0开始
				var firstOption = thisSelectQunan.find("option:first");
				var numList = [];
				if(data.attributes){
					if(data.attributes.selectNumList){
						numList = data.attributes.selectNumList;
					}
				}
				if(numList.length > 0){
					thisSelectQunan.empty();
					if(parseInt($(firstOption).val()) == 0 && parseInt(numList[0]) > 0){
						thisSelectQunan.append(firstOption);
					}
					if(thisSelectQunan.attr("data-reType") == "OPTIONAL"){
						for(var i=0;i<numList.length;i++){
				    		var option = $("<option>").val(numList[i]).text(numList[i]);
							thisSelectQunan.append(option);
				    	}
					}else{
						var option = $("<option>").val(numList[numList.length-1]).text(numList[numList.length-1]);
						thisSelectQunan.append(option);
					}
			    	if(parseInt(thisValue) > parseInt(numList[numList.length-1])){
			    		thisSelectQunan.val($(firstOption).val());
			    		thisSelectQunan.parent().parent().find("#"+goodsId_+"TotalNum").val($(firstOption).val());
			    	}else{
			    		thisSelectQunan.val(thisValue);
			    		setTicketPersonNum();
			    	}
		    		lineSelectDateChangeForSceneHotel(specDateSelect);
				}
	  		}
  		},"JSON");
	}
	
	function getNumListFromAjax(thisSelectQunan,specDate,hotelMaxNum){
		var goodsId = thisSelectQunan.attr("data-suppGoodsid");
		$.post("/vst_order/ord/order/changeStockSelectOption.do",{suppGoodsId:goodsId,specDate:specDate,ticketMax:hotelMaxNum*8},function(data){
	  		if(data.success){
				var thisValue = thisSelectQunan.val();
				//处理关联销售的门票，如果默认是从0开始，则更新列表后，也是从0开始
				var firstOption = thisSelectQunan.find("option:first");
				var numList = [];
				if(data.attributes){
					if(data.attributes.selectNumList){
						numList = data.attributes.selectNumList;
					}
				}
				if(numList.length > 0){
					thisSelectQunan.empty();
					if(parseInt($(firstOption).val()) == 0 && parseInt(numList[0]) > 0){
						thisSelectQunan.append(firstOption);
					}
					if(thisSelectQunan.attr("data-reType") == "OPTIONAL"){
						for(var i=0;i<numList.length;i++){
				    		var option = $("<option>").val(numList[i]).text(numList[i]);
							thisSelectQunan.append(option);
				    	}
					}else{
						var option = $("<option>").val(numList[numList.length-1]).text(numList[numList.length-1]);
						thisSelectQunan.append(option);
					}
			    	if(parseInt(thisValue) > parseInt(numList[numList.length-1])){
			    		thisSelectQunan.val(0);
			    	}else{
			    		thisSelectQunan.val(thisValue);
			    	}
		    		//$(thisSelectQunan).change();
		    		var copies =$(thisSelectQunan).find("option:selected").val();
					var singlePrice = $(thisSelectQunan).parent().prev().find("select").eq(0).find("option:selected").val();
					$(thisSelectQunan).parent().parent().find("td").eq(4).html("总价:￥"+(changeTwoDecimal_f_(singlePrice * copies)));
				}
	  		}
  		},"JSON");
	}
	
	function changeTwoDecimal_f_(x) {
		  var f_x = parseFloat(x);
		  if (isNaN(f_x)) {
		      alert('function:changeTwoDecimal->parameter error');
		      return false;
		  }
		  var f_x = Math.round(x * 100) / 100;
		  var s_x = f_x.toString();
		  var pos_decimal = s_x.indexOf('.');
		  if (pos_decimal < 0) {
		      pos_decimal = s_x.length;
		      s_x += '.';
		  }
		  while (s_x.length <= pos_decimal + 2) {
		      s_x += '0';
		  }
	
		  return s_x;
	}
		   
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
						 //requestProduct();
						 $("#changeTipInfo1").css('display','block');
						  $("#searchResultDiv").css('display','none');
					},//点击选择日期后的回调函数
					completeCallback:function(){
						var date=$("#visitTime").val();
						$(".ui-calendar").find("td[date-map]").children("div").removeClass("today");
						$(".ui-calendar").find("td[date-map='"+date+"']").children("div").addClass("today");
						<#if prodProduct.bizCategory.categoryCode=="category_route_group" || prodProduct.bizCategory.categoryCode=="category_route_local" >
							$(".ui-calendar").find("td[date-map]").find("div span:eq(1)").addClass("J_tip");
							$('.J_tip').lvtip({
				                templete: 2,
				                place: 'bottom-left',
				                offsetX: 0,
				                events: "live"  
				            });
						</#if>
					},
					sourceFn:function (cal){
					
//为了解决日历悬浮框js问题  	start				
					$.fn.lvtip = function(option){
    var opt = {
        templete : 1, 
        place : "bottom", 
        offsetX : 0, 
        offsetY : 0, 
        trigger : "mouseenter",  // other is "click"
        event : "bind",  // other is "live" live方法有问题，后添加元素ui不被调用
        hovershow : undefined  //time is 200
    }
    opt = $.extend(opt,option);
    var templeteStr = "";
    switch(opt.templete){
        case 1:
            templeteStr = '<div class="tooltip tooltip1" style="display:none;" id="lvtip'+opt.templete+'">'+
                        '    <div class="tooltip-arrow"></div>'+
                        '       <div class="tooltip-outer">'+
                        '       <div class="tooltip-shadow"></div>'+
                        '       <div class="tooltip-inner">'+
                        '           <h5 class="tooltip-title"></h5>'+
                        '           <div class="tooltip-content">'+
                        '               <p></p>'+
                        '           </div>'+
                        '           </div>'+
                        '       </div>'+
                        '   </div>';
            break;
        case 2:
            templeteStr = '<div class="tooltip tooltip2" style="display:none;" id="lvtip'+opt.templete+'">'+
                        '       <div class="tooltip2-arrow"><em></em><i></i></div>'+
                        '       <div class="tooltip-outer">'+
                        '           <h5 class="tooltip-title"></h5>'+
                        '           <div class="tooltip-content">'+
                        '               <p></p>'+
                        '           </div>'+
                        '       </div>'+
                        '   </div>';
            break;
        case 3:
            templeteStr = '<div class="tooltip tooltip2" style="display:none;" id="lvtip' + opt.templete + '">' +
                        '       <div class="tooltip-outer">' +
                        '           <h5 class="tooltip-title"></h5>' +
                        '           <div class="tooltip-content">' +
                        '               <p></p>' +
                        '           </div>' +
                        '       </div>' +
                        '   </div>';
            break;
        case 4:
            templeteStr = '<div class="tooltip tooltip2" style="display:none;width:500px;" id="lvtip' + opt.templete + '">' +
                        '       <div class="tooltip-outer" style="width:500px;">' +
                        '           <h5 class="tooltip-title"></h5>' +
                        '           <div class="tooltip-content">' +
                        '               <p></p>' +
                        '           </div>' +
                        '       </div>' +
                        '   </div>';
            break;
        default :
            break;
    }
    if($("#lvtip"+opt.templete).length==0){
        $("body").append(templeteStr);
    }
    $(this)[opt.event](opt.trigger,function(){
        var title = $(this).attr("tip-title");
        var content = $(this).attr("tip-content");
        var obj = $("#lvtip"+opt.templete);
        clearTimeout(obj.data("timeId"));
        if(title){
            obj.find(".tooltip-title").html(title).show();
        }else{
            obj.find(".tooltip-title").html("").hide();
        }
        if(content){
            obj.find(".tooltip-content p").html(content);
        }else{
            obj.hide();
            return;
        }
        var left = $(this).offset().left;
        var top = $(this).offset().top;
        switch(opt.place){
            case "top":
                left-=(obj.outerWidth()-$(this).outerWidth())/2;
                top-=$(obj).outerHeight();
                break;
            case "bottom":
                left-=(obj.outerWidth()-$(this).outerWidth())/2;
                top+=$(this).outerHeight();
                break;
            case "left":
                left-=$(obj).outerWidth();
                top-=($(obj).outerHeight()-$(this).outerHeight())/2;
                break;
            case "right":
                left+=$(this).outerWidth();
                top-=($(obj).outerHeight()-$(this).outerHeight())/2;
                break;
            case "top-left":
                top-=$(obj).outerHeight();
                break;
            case "top-right":
                left-=$(obj).outerWidth()-$(this).outerWidth();
                top-=$(obj).outerHeight();
                break;
            case "bottom-left":
                top+=$(this).outerHeight();
                break;
            case "bottom-right":
                left-=$(obj).outerWidth()-$(this).outerWidth();
                top+=$(this).outerHeight();
                break;
        }
        obj.addClass(opt.place).css({
            left : left + opt.offsetX,
            top : top + opt.offsetY
        }).fadeIn(200);
    })[opt.event]("mouseleave",function(){
        
        if(opt.trigger!='click'){
        	this.timeId = setTimeout(function(){
                $("#lvtip"+opt.templete).hide();
            },opt.hovershow);
            $("#lvtip"+opt.templete).data("timeId",this.timeId);
        }
    });
    var obj = this;
    $("#lvtip"+opt.templete).mouseenter(function(){
        clearTimeout(obj.timeId);
    }).mouseleave(function(){
        $(this).hide();
    });
}
//为了解决日历悬浮框js问题  	end

						$(".ui-calendar").find("td[date-map]").children("div").addClass("nodate");
                        var currentDate = $('.mtitle').eq(0).text();
                        var tdElement = cal.warp.find("td");
                        var productId = $("#productId").val();
						var postData = { productId: productId,currentDate: currentDate};
                        var jStartDistrict = $("#queryStartDistrictId");
                        if(jStartDistrict && jStartDistrict.length>0){
                            postData.startDistrictId = jStartDistrict.attr("data-cityid");
                        }
                        <#if prodProduct.bizCategory.categoryCode=="category_route_group" || prodProduct.bizCategory.categoryCode=="category_route_local" >	
					    //跟团游 当地游显示团率
                        	 postData.queryGroupRateFlag = "Y";
                        	  $.post("/vst_order/ord/order/route/CalendarJsonData.do", postData, function (data) {
						  		if(data.success){
						  			$(data.attributes.timePriceList).each(function(i,e){
						  				$(tdElement).each(function(t,v){
						  					var specDate=e.departureDate;
						  					var date=$(v).attr("date-map");;
							  				if(date==specDate){							  				    
							  					
							  					//成团率
							  					var str1,str2;
							  					var travellerNum = '';// 收客人数
							  					var minGroupNum = '';// 最少成团人数
							  					var groupRateRecord = data.attributes.groupRateMap[date];
							  					if(groupRateRecord!=null){
							  						var groupRate = groupRateRecord.groupRate;//成团率
								  					travellerNum = groupRateRecord.travellerNum==null?'':groupRateRecord.travellerNum;
								  					minGroupNum = groupRateRecord.leastClusterPerson==null?'':groupRateRecord.leastClusterPerson;
								  					if(groupRate == "" || groupRate ==null || groupRate ==0){
								  						str1 ="未确认";
								  						str2 ="未确认";
								  					}else if(groupRate >= 1){
								  						str1 ="已成团";
								  						str2 ="已成团";
								  					}else{
								  						str1 ="成团"+groupRate*100+"%";
								  						str2 ="成团率"+groupRate*100+"%";
								  					}
							  					}else{
							  						str1 ="未确认";
								  					str2 ="未确认";
							  					}
								  				$(v).find("div span:eq(1)").html(str1);
								  				$(v).find("div span:eq(1)").attr("tip-content","<span style='color:#1B1'>"+str2+"</span><br>"+"收客人数："+travellerNum+"<br>"+"最少成团人数："+minGroupNum);
							  				
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
						  			
						  		}else{
						  		}
						  	},"JSON");
                        <#else>
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
                        </#if>
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
					$("#changeTipInfo1").css('display','block');//显示提示信息
					$("#searchResultDiv").css('display','none');
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