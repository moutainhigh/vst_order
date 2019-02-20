 <#assign isGroupLast = ( prodRouteDetailGroup_index==((prodLineRouteDetail.prodRouteDetailGroupList?size)-1))/>
 
 <li <#if isGroupLast>class="last"</#if>>
    <div class="instance-travel-xc-time">
        <b><#if prodRouteDetailGroup.getTimeType()!='全天'>${prodRouteDetailGroup.getTimeType()!''}</#if></b>
        <#if prodRouteDetailGroup.localTimeFlag == "Y"><span>当地时间</span></#if>
    </div>
    <div class="instance-travel-xc-jt">
        <i class="instance-travel-xc-icon instance-travel-xc-icon-hotel"></i>
    </div>
    
    <div class="instance-travel-xc-info">
    
        <p class="instance-travel-xc-info-single">

 			<#if prodRouteDetailGroup.prodRouteDetailHotelList?? && prodRouteDetailGroup.prodRouteDetailHotelList?size gt 0>
 				<#assign  hasHotelName=false/>
 				<#assign hotelNams=''/>
				<#list prodRouteDetailGroup.prodRouteDetailHotelList as item>
					<#if item.hotelName != '' && item.hotelName!='未指定酒店'>
						<#assign hasHotelName=true/>
						<#if hotelNams ==''>入住<#else>(或)</#if>
						
						<#if item.productId!=''>
							<a href="javascript:;" class="JS_yin_hotel_dialog" data_hotel_product_id="${item.productId}">${item.hotelName}</a>
						<#else>
							${item.hotelName}
						</#if>
						<#assign hotelNams=(hotelNams+item.hotelName)/> 
						
					</#if>
					  
				</#list>
				<#if hotelNams==''>入住酒店</#if>
			</#if>
 			
        </p>
        
    </div>
</li>