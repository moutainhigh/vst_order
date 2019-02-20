<!--线路详情页产品详情-产品概要-->
<#if product.prodLineRouteList?? && product.prodLineRouteList?size gt 0>

<div class="product-summary">
        <h5>产品概要</h5>
        <div class="instance_list2_box">    
        <#list product.prodLineRouteList as prodLineRouteFrontVo>            
         <div class="instance_list2" style="display:block;">           	
	        <ul class="clearfix" >
	        	<#if prodLineRouteFrontVo.staticsProductInfos?exists>
	        		<#assign staticsProductInfos = prodLineRouteFrontVo.staticsProductInfos />
	        			<!--住宿--->
	        			<#assign countHotel = staticsProductInfos['countHotel'] />
	        				<#if staticsProductInfos['hotelLevelList']??>
	        					<#assign levelList=staticsProductInfos['hotelLevelList'] />
	        				</#if>
	        				
	        				<#if  levelList?exists || countHotel gt 0 >
	        					<li class="product-summary-li-first">
	        						<i class="icon-product-summary-hotel"><img src="http://pic.lvmama.com/img/v6/line/forpdf/hotel.png" width="24" height="24"></i>
		        					含${countHotel}晚住宿
		        					<#if staticsProductInfos['hotelLevelList']??>
		        						：
			        					<#list levelList as hotelStar>
			        						${hotelStar.value}晚${hotelStar.key}星
			        						<#if (hotelStar_index+1) != levelList?size>、</#if>
			        					</#list>
		        					</#if>
	        					</li>
	        				<#else>
	        					<li class="product-summary-li-first">
	        						<i class="icon-product-summary-hotel"><img src="http://pic.lvmama.com/img/v6/line/forpdf/hotel.png" width="24" height="24"></i>住宿详见行程介绍
	        					</li>
	        				</#if>
	        			
	        			<!--景点-->
	        				<#assign scenicCount=staticsProductInfos['scenicCount']/>
	        				<li class="product-summary-li-sec">
	        				    <i class="icon-product-summary-view-port"><img src="http://pic.lvmama.com/img/v6/line/forpdf/viewport.png" width="24" height="24"></i>
	        				    <#if scenicCount?? && scenicCount gt 0 >含${scenicCount}个景点<#else>景点详见行程介绍</#if>
	        				</li>

	        				
	        			
	        			<!--交通-->
	        				<#assign vehiclesCount=staticsProductInfos['vehiclesCount']/>
	        				<li><i class="icon-product-summary-traffic"><img src="http://pic.lvmama.com/img/v6/line/forpdf/traffic.png" width="24" height="24"></i>
	        				    <#if vehiclesCount?? && vehiclesCount?size gt 0>
	        						<#assign vehicleSize=vehiclesCount?size/>
	        						含
	        						<#list vehiclesCount as v>
	        							${v}<#if v_index lt vehicleSize-1>、</#if>
	        						</#list>
	        					<#else>
	        						交通详见行程介绍	
	        					</#if>
	        				</li>
	        				
	        				
	        			<!--用餐-->
	        				<#assign mealMap=staticsProductInfos['mealMap'] />
	        				<li class="product-summary-li-first">
	        				<i class="icon-product-summary-restaurant"><img src="http://pic.lvmama.com/img/v6/line/forpdf/restaurant.png" width="24" height="24"></i>
		        				<#if mealMap?exists &&  mealMap?size gt 0>
		        					
		        					<#assign breakfastCount=mealMap['breakfastCount']/>
		        					<#assign lunchCount=mealMap['lunchCount']/>
		        					<#assign dinnerCount=mealMap['dinnerCount']/>
		        					<#assign oneSelfCount=mealMap['oneSelfCount']/>
		        					<#assign eatCount=0/>
		        					
		        					<#assign hasMeal=false />
		        					<#if lunchCount?? && lunchCount gt 0>
		        						<#assign eatCount=(eatCount+lunchCount)/>
		        					</#if>
		        					<#if dinnerCount?? && dinnerCount gt 0>
		        						<#assign eatCount=(eatCount+dinnerCount)/>
		        					</#if>
		        					<#if (breakfastCount?? && breakfastCount gt 0) || (eatCount?? && eatCount gt 0) >
		        						含
			        					<#if breakfastCount?? && breakfastCount gt 0>
			        						<#assign hasMeal=true />
			        						${breakfastCount}早餐
			        						<#if eatCount?? && eatCount gt 0>、</#if>
			        					</#if>
			        					<#if eatCount?? && eatCount gt 0>
			        						<#assign hasMeal=true />
			        						${eatCount}正餐
			        					</#if>
			        				<#else>
			        					用餐详见行程介绍	
		        					</#if>
		        				<#else>
		        					用餐详见行程介绍
		        				</#if>
	        				</li>
	        			
	        			<!--购物点-->
	        				<#assign shopCount=staticsProductInfos['shopCount']/>
	        				<#assign recommendCount=staticsProductInfos['recommendCount']/>
	        				<li class="product-summary-li-sec">
	        				<i class="icon-product-summary-shop"><img src="http://pic.lvmama.com/img/v6/line/forpdf/shop.png" width="24" height="24"></i>
	        					<#if shopCount?? && shopCount gt 0 >
	        						含购物点
	        					<#elseif recommendCount?? && recommendCount gt 0 >
	        						含${recommendCount}个推荐项目
	        					<#else>推荐详见行程介绍</#if>
	        				</li>
	        				
	        			
	        			<!-自由活动-->
	        				<#assign freeActivityCount=staticsProductInfos['freeActivityCount']/>
	        				<li><i class="icon-product-summary-free-time"><img src="http://pic.lvmama.com/img/v6/line/forpdf/freetime.png" width="24" height="24"></i>
	        					<#if freeActivityCount?? && freeActivityCount gt 0 >含自由活动${freeActivityCount}次<#else>自由活动详见行程介绍</#if>
	        				</li>
	        	</#if>
	        </ul>
        </div>
	
      </#list>
      </div>
</div>
</#if>

