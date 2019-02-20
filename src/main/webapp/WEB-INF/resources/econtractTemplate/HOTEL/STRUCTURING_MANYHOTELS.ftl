<!--定义group索引  groupIndex (routeDetailGroup_index )-->
 <#assign isGroupLast = ( prodRouteDetailGroup_index==((prodLineRouteDetail.prodRouteDetailGroupList?size)-1) )/>
 
 <li <#if isGroupLast>class="last"</#if>>
                            <div class="instance-travel-xc-time">
                                <b><#if prodRouteDetailGroup.getTimeType()!='全天'>${prodRouteDetailGroup.getTimeType()!''}</#if></b>
                                <#if prodRouteDetailGroup.localTimeFlag == "Y"><span>当地时间</span></#if>
                            </div>
                            <div class="instance-travel-xc-jt">
                               	 住宿
                                <i class="instance-travel-xc-icon instance-travel-xc-icon-hotel"></i>
                            </div>
                        	 <div class="instance-travel-xc-info">
                             	<p class="instance-travel-xc-info-title">
                             		<#list prodRouteDetailGroup.prodRouteDetailHotelList as item>
                             			<#if item_index = 0 && item.logicRelateion = "">
					                  		入住
										</#if>
										<#if item_index != 0 && item.logicRelateion != "">
										          或
										</#if>
										<#if item.hotelName!='' && item.hotelName!='未指定酒店'>
										 	<#if item.productId!=''>
			                                    <a data_hotel_product_id="${item.productId}" class="instance-travel-xc-hotel-09c JS_yin_hotel_dialog">${item.hotelName!''}</a>
				                            <#else>
			                                    <span class="hotelNoReg">${item.hotelName!''}</span>
			                                </#if>
										<#else>
		                                	<span class="hotelNoReg">未指定酒店(${item.starLevelName!''})</span>
	                                    </#if>
                             		</#list>
                                </p>          
                     			  <div class="instance-travel-xc-det-830">
                     			  	<#list prodRouteDetailGroup.prodRouteDetailHotelList as item>
	                     			  	<#if item.hotelDesc!="">
	                     			  		<#if item.hotelName!='' && item.hotelName!='未指定酒店'>
	                     			  			（${item.hotelName}） <@func.addSpace item.hotelDesc 37/><br/>
	                     			  		 <#else>
		                                                                                                                         （ 未指定酒店）<@func.addSpace item.hotelDesc 37/><br/>
	                                    	</#if>	
	                     			  	</#if>
                     			  	</#list>
                     			  </div>
                     			  <ul class="xc-moreHotelPic clearfix"> 
                     			  	<#list prodRouteDetailGroup.prodRouteDetailHotelList as item>
                     			  		<#if routeShowType=='PAGE' && item.hotelFirstImg!=''><!--firstHotelImg调取酒店产品第一张图片-->
	                     			  		<li>
	                                            <a href="javascript:;" class="JS_yin_hotel_dialog" data_hotel_product_id="${item.productId}">
	                                                <img src="${item.hotelFirstImg}" width="120" height="80"  />
	                                                <i class="xc-moreHotelPic-tit">${item.hotelName}</i>
	                                                <span class="xc-moreHotelPic-bg"></span>
	                                            </a>
	                                        </li>
                                        </#if>
                     			  	</#list>
                     			  </ul>
                              </div>
</li>