<!--定义group索引  groupIndex (routeDetailGroup_index )和detail的索引值detailIndex(item_index)-->
 <#assign isGroupLast = ( prodRouteDetailGroup_index==((prodLineRouteDetail.prodRouteDetailGroupList?size)-1) && detailIndex==(detailSize-1) )/>
 
 <li <#if isGroupLast>class="last"</#if>>
<#--交通类型-->
<#assign vehicleType=item.vehicleType/>
<#if vehicleType=='PLANE'>
	<#assign vehicleTypeName = '飞机'>
<#elseif vehicleType=='TRAIN'>
	<#assign vehicleTypeName = '火车'>
<#elseif vehicleType=='BARS'>
	<#assign vehicleTypeName = '巴士'>
<#elseif vehicleType=='BOAT'>
	<#assign vehicleTypeName = '轮船'>
<#elseif vehicleType=='CRUISE'>
	<#assign vehicleTypeName = '邮轮'>
<#elseif vehicleType=='OTHERS'>
	<#assign vehicleTypeName = "其他-${item.vehicleOtherInfo!''}">
</#if>

<#--交通时间-->
<#if item.vehicleTime?? && item.vehicleTime != null>
    <#assign vehicleTime = item.vehicleTime?split(":")/>
<#else>
    <#assign vehicleTime = ['','']/>
</#if>

<#--交通显示运行类型-->
<#if vehicleType=='PLANE'>
<#assign vehicleRunType = '飞行'/>
<#else>
<#assign vehicleRunType = '行驶'/>
</#if>

        			<#if detailIndex == 0>
                                            <div class="instance-travel-xc-time">
                                                <b>${prodRouteDetailGroup.getTimeType()!''}</b>
                                                <#if prodRouteDetailGroup.localTimeFlag == "Y"><span>当地时间</span></#if>
                                            </div>
                      </#if> 
                                            <div class="instance-travel-xc-jt">
                                            	<#if detailIndex != 0 && item.logicRelation != "">
						                        	<em class="instance-travel-xc-and-or">（<#if item.logicRelation == "AND">和<#elseif item.logicRelation == "OR">或</#if>）</em>
												</#if>
                                                ${vehicleTypeName!''}                                                
                                                 <#if vehicleType=='PLANE'>
                                                <i class="instance-travel-xc-icon instance-travel-xc-icon-air"></i><!--飞机-->
								                <#elseif vehicleType=='TRAIN'>
								                	<i class="instance-travel-xc-icon instance-travel-xc-icon-train"></i><!--火车-->
												<#elseif vehicleType=='BARS'>
													<i class="instance-travel-xc-icon instance-travel-xc-icon-bus"></i><!--汽车-->
												<#elseif vehicleType=='BOAT'>
												    <i class="instance-travel-xc-icon instance-travel-xc-icon-ship"></i><!--标签中展示轮船的标签-->
												<#elseif vehicleType=='CRUISE'>
												    <i class="instance-travel-xc-icon instance-travel-xc-icon-mail"></i>
												<#elseif vehicleType=='OTHERS'>
												    <i class="instance-travel-xc-icon instance-travel-xc-icon-other"></i>
												</#if>
                                            </div>
                                            <div class="instance-travel-xc-info">
                                            	
                                                <p>
                                                	<#if vehicleTime[0] !='' || vehicleTime[1] !=''>
	                                                    <span class="instance-travel-xc-info-pr20">${vehicleRunType!''}时间：
	                                                    	 约<#if vehicleTime[0]?? &&vehicleTime[0] !=''> ${vehicleTime[0]!''}小时</#if>
										 					<#if vehicleTime[1]?? &&vehicleTime[1] !=''> ${vehicleTime[1]!''}分钟</#if>
	                                                    </span>
                                                    </#if>
                                                    <#if item.vehicleKm!=''>	
                                                    <span class="instance-travel-xc-info-pr20">${vehicleRunType!''}距离：约${item.vehicleKm!''}公里</span>
                                                    </#if>
                                                </p>
                       
                                                
                                                <p>
	                                                <#if vehicleType=='PLANE' && item.pickUpFlag == 'Y'>
											                          	  接机服务： 接机人员第${item.pickUpDay}天接机；
											        <#elseif vehicleType=='TRAIN' && item.pickUpFlag == 'Y'>
											                            接车服务：接车人员第${item.pickUpDay}天接车；
										            </#if>
                                                </p>
                                                <#if item.vehicleDesc!=''>
	                                                <p class="instance-travel-xc-det-830">
                                                         <@func.addSpace item.vehicleDesc 37/>
	                                                </p>
                                                </#if>
                                            </div>
</li>