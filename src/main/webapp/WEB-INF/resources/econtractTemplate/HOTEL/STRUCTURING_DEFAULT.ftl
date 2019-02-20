<!--定义group索引  groupIndex (routeDetailGroup_index )和detail的索引值detailIndex(item_index)-->
 <#assign isGroupLast = ( prodRouteDetailGroup_index==((prodLineRouteDetail.prodRouteDetailGroupList?size)-1) && detailIndex==(detailSize-1) )/>
 
 <li <#if isGroupLast>class="last"</#if>>
        			<#if detailIndex == 0>
                                            <div class="instance-travel-xc-time">
                                                <b><#if prodRouteDetailGroup.getTimeType()!='全天'>${prodRouteDetailGroup.getTimeType()!''}</#if></b>
                                                <#if prodRouteDetailGroup.localTimeFlag == "Y"><span>当地时间</span></#if>
                                            </div>
                      </#if>  
                            <div class="instance-travel-xc-jt">
                            
                               <#if detailIndex == 0> 住宿</#if>
                                <i class="instance-travel-xc-icon instance-travel-xc-icon-hotel"></i>
                            </div>
                             <div  <#if item.hotelFirstImg?? && item.hotelFirstImg!=''> class="instance-travel-xc-info instance-travel-xc-w630"
                                            		<#else>class="instance-travel-xc-info"</#if> 
                             >
                            
                            
                                <p class="instance-travel-xc-info-title">
                                	<#if detailIndex != 0 && item.logicRelateion != "">
						                  <em class="instance-travel-xc-and-or">（<#if item.logicRelateion == "AND">和<#elseif item.logicRelateion == "OR">或</#if>）</em>
									</#if>
									<#if item.hotelName!='' && item.hotelName!='未指定酒店'>
	                                    <#if item.productId!=''>
	                                    	<a href="javascript:;"  data_hotel_product_id="${item.productId}" class="instance-travel-xc-hotel-09c">${item.hotelName!''}</a>
	                                    <#else>
	                                    	${item.hotelName!''}
	                                    </#if>
                                    </#if>
                                    <#if item.belongToPlace!''>
                                    <span class="instance-travel-xc-hotel-span">
                                        <i class="instance-travel-xc-icon instance-travel-xc-icon-address"></i>
                                        ${item.belongToPlace!''}
                                    </span>
                                    </#if>
                                </p>
                                <#if item.travelTime!='' || item.distanceKM!''>
                                	<#assign travelTypeStr = routeDetailFormat.getTravelTypeCnName(item.travelType)/>
						            <#assign travelTimeStr = routeDetailFormat.getTimeStr(item.travelTime)/>
                                	<p>
                                		<#if travelTypeStr != "" && travelTimeStr != "">
                                        <span class="instance-travel-xc-info-pr20"> ${travelTypeStr!''}时间：${travelTimeStr!''}</span>
                                        </#if>
                                        <#if travelTypeStr != "" && item.distanceKM != "">
                                        <span class="instance-travel-xc-info-pr20"> ${travelTypeStr!''}距离：约${item.distanceKM!''}公里</span>
                                        </#if>
                                	</p>
                                </#if>
                               
                               <#if (item.roomType!='') || item.starLevelName!=''>
	                                <p>
					                	<#if item.roomType!=''><span class="instance-travel-xc-info-pr20">房型：${item.roomType}</span></#if>
					                	
	                                    <#if item.starLevelName?? && item.starLevelName!='' && item.starLevel!=-1>
	                                    <span class="instance-travel-xc-info-pr20">星级：${item.starLevelName!''}</span>
	                                    </#if>
	                                </p>
                                </#if>
                                
                                <#if item.hotelDesc!''>
                                <p class="instance-travel-xc-det-630">
                                    <@func.addSpace item.hotelDesc 37/>
                                </p>
                                </#if>
                            </div>

                           <ul class="xc-moreHotelPic clearfix"> 
                     			  	<#if routeShowType=='PAGE' && item.hotelFirstImg!=''><!--firstHotelImg调取酒店产品第一张图片-->
	                     			  		<li>
	                                            <img src="" width="120" height="80" data_hotel_product_id="${item.productId}" />
	                                        </li>
                                        </#if>
                     			  </ul>
                         
</li>