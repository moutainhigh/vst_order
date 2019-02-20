<!--默认购物点模块-->
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
                                             <#if detailIndex != 0 && item.logicRelation != "">
					                        	<em class="instance-travel-xc-and-or">（<#if item.logicRelation == "AND">和<#elseif item.logicRelation == "OR">或</#if>）</em>
											</#if>
                                               	${item.shoppingName!''}	
                                               	 <#if item.recommendFlag == "Y">
							                  		<span>[推荐购物点]</span>
												</#if>
                                                
                                                <i class="instance-travel-xc-icon instance-travel-xc-icon-shopping"></i>
                                            </div>
                                            <div class="instance-travel-xc-info">
                                            	<#if item.travelTime != "" || item.distanceKM != "" || item.visitTime != "">
                                            		<#assign travelTypeStr = routeDetailFormat.getTravelTypeCnName(item.travelType)/>
						                        	<#assign travelTimeStr = routeDetailFormat.getTimeStr(item.travelTime)/>
						                        	
	                                                <p>
	                                                	<#if travelTypeStr != "" && travelTimeStr != "">
	                                                    <span class="instance-travel-xc-info-pr20"> ${travelTypeStr!''}时间：${travelTimeStr!''}</span>
	                                                    </#if>
	                                                    <#if travelTypeStr != "" && item.distanceKM != "">
	                                                    <span class="instance-travel-xc-info-pr20"> ${travelTypeStr!''}距离：约${item.distanceKM!''}公里</span>
	                                                    </#if>
	                                                    <#assign visitTimeStr = routeDetailFormat.getTimeStr(item.visitTime)/>
						                        		<#if visitTimeStr != "">
	                                                    <span class="instance-travel-xc-info-pr20">参观时间：${visitTimeStr!''}</span>
	                                                    </#if>
	                                                </p>
                                                </#if>
                                                <#if item.address != ""><p>地址：${item.address!''}</p></#if>
                                                <#if item.mainProducts != ""><p>主营产品：<@func.addSpace item.mainProducts 37/>  </p></#if>
                                                <#if item.subjoinProducts != ""><p>兼营产品：<@func.addSpace item.subjoinProducts 37/></p></#if>
                                                <#if item.shoppingDesc != "">
                                                <p class="instance-travel-xc-det-830">
                                                   <@func.addSpace item.shoppingDesc 37/>
                                                </p>
                                                </#if>
                                            </div>
</li>