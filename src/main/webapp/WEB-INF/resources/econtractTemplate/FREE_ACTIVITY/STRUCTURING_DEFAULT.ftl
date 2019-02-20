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
                           	 自由活动
                            <i class="instance-travel-xc-icon instance-travel-xc-icon-activity"></i>
                        </div>
                        <div class="instance-travel-xc-info">
                        <#if item.visitTime!='' ||item.travelTime!='' ||item.distanceKm>
                        	<#assign visitTimeDesc = routeDetailFormat.getTimeStr(item.visitTime)/>
                        	<#assign travelTimeDesc = routeDetailFormat.getCnTimeFormat(item.travelType,item.travelTime)/>
                        	<#assign distanceKmDesc = routeDetailFormat.getCnDistanceFormat(item.travelType,item.distanceKm)/>
                        	<p>
                                <#if visitTimeDesc!=''><span class="instance-travel-xc-info-pr20">活动时间： ${visitTimeDesc!''}</span></#if>
                                 <#if travelTimeDesc!=''><span class="instance-travel-xc-info-pr20"> <@func.addSpace travelTimeDesc 37/></span></#if>
                                <#if distanceKmDesc!=''><span class="instance-travel-xc-info-pr20"><@func.addSpace distanceKmDesc 37/> </span></#if>
                            </p>
                        	
                        </#if>
                        
                        <#if item.activityDesc != "">
                            <p class="instance-travel-xc-det-830">
                                <@func.addSpace item.activityDesc 37/>
                            </p>
                        </#if>
                        </div>
</li>