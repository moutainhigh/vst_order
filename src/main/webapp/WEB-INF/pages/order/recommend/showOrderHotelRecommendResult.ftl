<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>相关酒店推荐</title>
    <#include "/base/head_meta_recommend.ftl"/>
    <link rel="stylesheet" href="http://super.lvmama.com/vst_order/css/ui-components.css">
    <link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v6/header_new.css,/styles/v6/flighthotel/hotel-facilities.css,/styles/lv/calendar.css,/styles/backstage/v1/vst/order-management/related-hotel.css">
    <style>
    	.pages_go, .page_msg, .pages span {
    		display:none;
    	}
    	
    	.pages span.PrevPage, .pages span.PageSel, .pages span.PageMore {
    		display:inline-block;
    	}
    	
    	.Pages .PrevPage, .Pages .NextPage {
    		width: 50px !important;
    	}
    </style>
</head>
<body style="min-width: 1100px;">
<#import "/base/pagination.ftl" as pagination>
    <div class="related-hotel-rec">
    	<form id="searchForm" action="/vst_order/ord/order/showOrderHotelRecommendResult.do" method="post">
	        <div class="rhr-calendar">
	            <a href="http://s.lvmama.com/hotel/C${orderRecommendHotel.startDateStr !''}O${orderRecommendHotel.endDateStr !''}U${orderRecommendHotel.districtId !''}" target="_blank" class="rhr-btn">前台搜索</a>
	            <a id="searchBtn" href="javascript:void(0);" class="rhr-btn">查询</a>
	            <div class="rhr-calendar-group">
	                入住
	                <input type="text" class="JS_rhr_calendar" name="startDate" readonly="readonly" value="${orderRecommendHotel.startDate !''}" /><em>${orderRecommendHotel.startDateWeekStr !''}</em><i></i>
	            </div>
	            <div class="rhr-calendar-group">
	                离店
	                <input type="text" class="JS_rhr_calendar" name="endDate" readonly="readonly" value="${orderRecommendHotel.endDate !''}"><em>${orderRecommendHotel.endDateWeekStr !''}</em><i></i>
	                <input type="hidden" name="starId" value="${orderRecommendHotel.starId}" />
	                <input type="hidden" name="sortType" value="${orderRecommendHotel.sortType}" />
	                <input type="hidden" name="baiduGeo" value="${orderRecommendHotel.baiduGeo}" />
	                <input type="hidden" name="mapType" value="${orderRecommendHotel.mapType}" />
	                <input type="hidden" name="districtId" value="${orderRecommendHotel.districtId}" />
	                <input type="hidden" name="suppGoodsId" value="${orderRecommendHotel.suppGoodsId}" />
	                <input type="hidden" id="pageSizeId" name="pageSize" value="${resultPage.pageSize!''}"/>
	            </div>
	        </div>
	        <div class="rhr-sort">
				<div class="rhr-sort-group">
	                <div class="rhr-sort-select">
	                    <!-- active 高亮 -->
	                    <strong>排序</strong><span class="active sort">默认<i></i></span>
	                    <ul>
	                    	<#if sortMap??>
	    						<#list sortMap?keys as key>
			                        <li>
			                            <label>${sortMap[key]!}
			                                <input type="radio" name="rhr-sort" value="${key! }" data="${sortMap[key]!}">
			                            </label>
			                        </li>
								</#list>
							</#if>
	                    </ul>
	                </div>
	            </div>
	            <div class="rhr-sort-group">
	                <div class="rhr-sort-select">
	                    <!-- active 高亮 -->
	                    <strong>数量</strong><span class="active amount">6<i></i></span>
	                    <ul>
	                    	<#if pageMap??>
	    						<#list pageMap?keys as key>
			                        <li>
			                            <label>${pageMap[key]!}
			                                <input type="radio" name="rhr-amount" value="${key! }" data="${pageMap[key]!}">
			                            </label>
			                        </li>
								</#list>
							</#if>
	                    </ul>
	                </div>
	            </div>
	            <div class="rhr-sort-group">
	                <div class="rhr-sort-select">
	                    <!-- active 高亮 -->
	                    <strong>星级</strong><span class="active star">不限<i></i></span>
	                    <ul>
	                    	<#if starMap??>
	    						<#list starMap?keys as key>
			                        <li>
			                            <label>${starMap[key]!}
			                                <input type="radio" name="rhr-star" value="${key! }" data="${starMap[key]!}">
			                            </label>
			                        </li>
								</#list>
							</#if>
	                    </ul>
	                </div>
	            </div>
	        </div>
		</form>
        <div class="rhr-hotel-list clearfix">
<#if resultPage?? >
	<#if resultPage.items?? && resultPage.items?size gt 0 >
		<div class="clearfix">
			<#list resultPage.items as result>
		            <div class="rhr-hotel">
		                <a class="rhr-hotel-img" href="http://hotels.lvmama.com/hotel/${result.hotelProduct.productId}" target="_blank"><img src="http://pic.lvmama.com${result.hotelProduct.photoUrl}" onerror='this.src="http://pic.lvmama.com/img/cmt/img_300_200.jpg"' width="240" height="130"></a>
		                <div class="rhr-hotel-main">
		                    <a class="rhr-hotel-title" href="http://hotels.lvmama.com/hotel/${result.hotelProduct.productId}" target="_blank" title="${result.hotelProduct.productName}">${result.hotelProduct.productName}</a>
		                    <div class="rhr-hotel-facilities">
		                    	<#list '${result.hotelProduct.facilities}'?split(',') as facility>
		                    		<#if facility = 460>
		                    			<span class="hotel-facilities-icon hfi-wifi-f" title="免费Wifi"></span>
		                    		<#elseif facility = 461>
		                    			<span class="hotel-facilities-icon hfi-wifi-c" title="收费Wifi"></span>
		                    		<#elseif facility = 462>
		                    			<span class="hotel-facilities-icon hfi-internet-f" title="免费宽带"></span>
		                    		<#elseif facility = 463>
		                    			<span class="hotel-facilities-icon hfi-internet-c" title="收费宽带"></span>
		                    		<#elseif facility = 464>
		                    			<span class="hotel-facilities-icon hfi-park-f" title="免费停车场"></span>
		                    		<#elseif facility = 465>
		                    			<span class="hotel-facilities-icon hfi-park-c" title="收费停车场"></span>
		                    		<#elseif facility = 466>
		                    			<span class="hotel-facilities-icon hfi-pick-f" title="免费接机服务"></span>
		                    		<#elseif facility = 467>
		                    			<span class="hotel-facilities-icon hfi-pick-c" title="收费接机服务"></span>
		                    		<#elseif facility = 468>
		                    			<span class="hotel-facilities-icon hfi-sp-in" title="室内游泳池"></span>
		                    		<#elseif facility = 469>
		                    			<span class="hotel-facilities-icon hfi-sp-out" title="室外游泳池"></span>
		                    		<#elseif facility = 470>
		                    			<span class="hotel-facilities-icon hfi-gym" title="健身房"></span>
		                    		<#elseif facility = 471>
		                    			<span class="hotel-facilities-icon hfi-bc" title="商务中心"></span>
		                    		<#elseif facility = 472>
		                    			<span class="hotel-facilities-icon hfi-mr" title="会议室"></span>
		                    		<#elseif facility = 473>
		                    			<span class="hotel-facilities-icon hfi-restaurant" title="酒店餐厅"></span>
		                    		<#else>
		                    		</#if>
		                    	</#list>
		                    </div>
		                    <div class="rhr-hotel-tag">
		                        <span class="rhr-hotel-star">${result.hotelProduct.starDesc}</span>
		                    </div>
		                    <div class="rhr-price-box">
		                        <div class="rhr-price"><i>¥</i><em>${result.hotelProduct.sellPrice}</em>起</div>
		                        <a href="/vst_order/ord/productQuery/showOrderProductQueryList.do?productId=${result.hotelProduct.productId}&startDate=${orderRecommendHotel.startDate}&endDate=${orderRecommendHotel.endDate}&days=${orderRecommendHotel.days}" target="_blank" class="rhr-btn">后台预订</a>
		                    </div>
		                </div>
		            </div>
			</#list>
		</div>
		<#--分页标签-->
		<@pagination.paging resultPage/>
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无酒店推荐，请重新输入相关条件查询！</div>
	</#if>
</#if>
    </div>
	<#include "/base/foot.ftl"/>
</body>
</html>
    <script src="http://pic.lvmama.com/min/index.php?f=/js/new_v/jquery-1.7.2.min.js,/js/lv/calendar.js,/js/backstage/v1/vst/order-management/related-hotel.js"></script>
    <script>
	    $(document).ready(function(){  
	    	var starId = "${orderRecommendHotel.starId!''}";
	    	var pageSize = "${resultPage.pageSize!''}";
	    	var sortType = "${orderRecommendHotel.sortType!''}";
			$("input[name='rhr-sort'][value='"+ sortType+"']").attr("checked",true);
			$("input[name='rhr-amount'][value='"+ pageSize+"']").attr("checked",true);
			$("input[name='rhr-star'][value='"+ starId+"']").attr("checked",true);
			var starIdStr = $('input[name="rhr-star"]:checked ').attr('data');
			var pageSizeStr = $('input[name="rhr-amount"]:checked ').attr('data');
			var sortTypeStr = $('input[name="rhr-sort"]:checked ').attr('data');
			if('' != starId && null != starId && undefined != starId){
				$('span.star').html(starIdStr + '<i></i>');
			}
			if('' != pageSize && null != pageSize && undefined != pageSize){
				$('span.amount').html(pageSizeStr + '<i></i>');
			}
			$('span.sort').html(sortTypeStr + '<i></i>');
			$("#searchBtn").bind("click", function(){
				search();
			})
	    });
		function search() {
			var starIdStr = $('input[name="rhr-star"]:checked ').val();
			var pageSizeStr = $('input[name="rhr-amount"]:checked ').val();
			var sortTypeStr = $('input[name="rhr-sort"]:checked ').val();
			$("input[name='starId']").val(starIdStr);
			$("input[name='sortType']").val(sortTypeStr);
			$("#pageSizeId").val(pageSizeStr);
			$('#searchForm').submit();
		}
	</script>