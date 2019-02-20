<#--页眉-->
<#import "/base/spring.ftl" as s/>
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
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="keywords" content="">
<meta name="description" content="">
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v5/modules/dialog.css" type="text/css">

<!--  样式外加部分 -->
<link href="http://pic.lvmama.com/min/index.php?f=/styles/v5/modules/button.css" rel="stylesheet" type="text/css">
<link rel="stylesheet" href="/vst_order/js/book/line.css" type="text/css">
<link rel="stylesheet" href="http://s2.lvjs.com.cn/styles/line/detail/product-detail.css" type="text/css">

<style> 
    .ui-calendar{ width:1200px;z-index:2!important;} 
	.ui-calendar .calmonth{ width:650px;} 
	.calmonth .caltable{ height:500px;} 
	input{height:26px!important;}
	.w8{padding:0!important;}
	.tooltip.tooltip2.bottom-left {opacity: 1!important;}
</style> 
</head>
<body>
<#--页面导航-->
	
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

	<div class="iframe_content">
		  <form id="bookForm" action="" method="post">
		  	<input type="hidden" name="fromSearch" value="false"/>
		  	<input type="hidden" name="specDate" >
		  	<input type="hidden" name="productId" >
		  	<input type="hidden" name="distributionId" >
		  	<input type="hidden" name="userId" >
		  	<input type="hidden" name="adultNum" >
		  	<input type="hidden" name="startDistrictId" >
		  	<input type="hidden" name="childNum" >
		  	<input type="hidden" name="copies" >
		  	<input type="hidden" name="additionMap[visaDocLastTime]" value="<#if visaDocLastTime??>${visaDocLastTime?string('yyyy-MM-dd')!''}</#if>" autocomplete="off"/>
            <input type="hidden" id="oughtAmount" name="oughtAmount" value="${oughtAmount}"/>
            <#--增加一键下单信息存储-->
            <input type="hidden" name="orderCreatingManner" value="${orderCreatingManner}" />
            <input type="hidden" name="originalOrderId" value="${originalOrderId}" />
            <input type="hidden" name="isTestOrder" value="${isTestOrder}" />
            <input type="hidden" name="channel_code" value="${channel_code}"/>
		  </form>
	<#--提交订单的Form-->
		<form id="orderForm" name="orderForm" action="/vst_order/ord/order/lineBackCreateOrder.do" method="POST">
			<input type="hidden" name="intentionOrderId" value="${intentionOrderId}"/>
			<input type="hidden" id="userId" name="userId" value="${userId}"/>
			<input type="hidden" id="productId" name="productId" value="${productId}"/>
			<input type="hidden" name="adultQuantity" value="${adultNum}"/>
			<input type="hidden" name="childQuantity" value="${childNum}"/>
            <input type="hidden" name="distributorCode" value="${channel_code}"/>
			<!--这里显示查询条件信息-->
			<#if (isHotel || saleCopies) && !isRoute>
				<#include "/order/orderProductQuery/line/queryDivHotel.ftl"/>  
			<#else>
				<#include "/order/orderProductQuery/line/queryDiv.ftl"/> 
			</#if>
	 	</form>    
  </div>
        
  <script type="text/javascript" src="http://pic.lvmama.com/js/youlun/jquery.birthcalendar.js"></script>
  <script type="text/javascript" src="/vst_order/js/book/coupon.js"></script>
  <script src="/vst_order/js/book/line.js"></script>
  <script src="/vst_order/js/book/express.js?2014091512345"></script>

    <#--预定失败打点统计-->
    <script src="http://s3.lvjs.com.cn/js/v5/ibm/eluminate.js"></script>
    <script src="http://s3.lvjs.com.cn/js/v5/ibm/coremetrics-initalize.js"></script>
    <script>_LVMAMA_COREMETRICS.init(document.domain);</script>

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
  function resetChildMaxQuantity(){
  <#if prodProduct.isLocalBuProduct() && !(prodProduct.getBizCategoryId() == 18 && prodProduct.getSubCategoryId() == 183) >
  	if($('#adultNum')){
		var CHILD_MIN_QUANTITY = $("#childNum option:first").val();
		var CHILD_MAX_QUANTITY = $("#childNum option:last").val();
	
		var adultQuantity = $("#adultNum").find("option:selected").text();
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
});
  	var myDatestr =${startDaeResult};
			//console.log(myDatestr);
			//myDatestr = myDatestr.replace(/-/g,"/");
			//console.log(myDatestr);
		 	var mydate = new Date(myDatestr);
		  	$(function(){
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
                 }
             });

             //点空白地方去除显示下拉
             $body.click(function (){
                 if(!$opt.is(":visible")) return;
                 $opt.hide();
                 $selectSimu.removeClass('active');
             });
		  	//设置成人数、儿童数、酒店套餐份数
	  		var adultNum=$("#adultNumValue").val();
	  		var childNum=$("#childNumValue").val();
	  		var hotelNum=$("#hotelNumValue").val();
	  		if(isNaN(hotelNum)){
	  			$("#adultNum option[value=" + parseInt(adultNum) + "]").attr('selected', 'true');
	  	  		$("#childNum option[value=" + parseInt(childNum) + "]").attr('selected', 'true');
	  		}else{
	  			$("#hotelNum option[value=" + parseInt(hotelNum) + "]").attr('selected', 'true');
	  		}
        	pandora.calendar({ 
        			date:mydate,
					trigger: ".js_youwanTime", 
					triggerClass: "js_youwanTime", 
					template: "big", //小日历  small
					mos:${mothResult},
					isTodayClick:true, //当天是否可点击 
					frequent:true,
					selectDateCallback: function(){ 
						  //requestProduct();
					},//点击选择日期后的回调函数 
					completeCallback:function(){
			
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
					$(".ui-calendar").find("td[date-map]").children("div").addClass("nodate");
					//alert(this.date.getFullYear());
					//alert(this.date.getMonth());
						var currentDate=$('.mtitle').eq(0).text();
					     var tdElement=cal.warp.find("td");
					      var productId=$("#productId").val();
					       var postData = { productId: productId,currentDate: currentDate};
	                        var jStartDistrict = $("#queryStartDistrictId");
	                        if(jStartDistrict && jStartDistrict.length>0){
	                            postData.startDistrictId = jStartDistrict.attr("data-cityid");
	                        }
					      <#if prodProduct.bizCategory.categoryCode=="category_route_group" || prodProduct.bizCategory.categoryCode=="category_route_local" >	
					          //跟团游 当地游显示出团率
					          //var postData = { productId: productId,currentDate: currentDate};
	                          postData.queryGroupRateFlag = "Y";
                        	  $.post("/vst_order/ord/order/route/CalendarJsonData.do", postData, function (data) {
						  		if(data.success){
						  			$(data.attributes.timePriceList).each(function(i,e){
						  				$(tdElement).each(function(t,v){
						  					var specDate=e.departureDate;
						  					//var routeName=e.prodLineRoute.routeName;
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
					      <#else> 
					      	 $.post("/vst_order/ord/order/route/CalendarJsonData.do",postData,function(data){
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
						  </#if>
						      
					}
				});
			});
			
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
	
	
  </script>
  <script src="/vst_order/js/payment_page.js"></script>
  <script src="/vst_order/js/order/oneKeyOrderInsurance.js"></script>