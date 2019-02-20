
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
	                                            <#if detailIndex != 0 && item.logicRelateion != "">
						                        	<em class="instance-travel-xc-and-or">（<#if item.logicRelateion == "AND">和<#elseif item.logicRelateion == "OR">或</#if>）</em>
												</#if>
												<#if item.scenicNameId!='' && item.scenicNameId!=''>
                                                	<a href="javascript:;"  class="instance-travel-xc-scene-09c" data_scenic_dest_id="${item.scenicNameId}">${item.scenicName!''}</a>
                                                <#else>
                                                	${item.scenicName!''}
                                                </#if>
                                                <span>
	                                                <#if item.briefExplain=="OUTSIDE_SEE">[外观，此景点不含门票费用]
	                                                <#elseif item.briefExplain="TICKET_COST_INCLUDE">[此景点有门票景点费用]
	                                                <#elseif item.briefExplain=="TICKET_COST_EXCLUDE">[此景点无门票景点费用]
	                                                <#else>
	                                                </#if>
                                                </span>
                                                <i class="instance-travel-xc-icon instance-travel-xc-icon-scene"></i>
                                            </div>
                                            <div  <#if item.scenicNameId?? && item.scenicNameId!=''> class="instance-travel-xc-info instance-travel-xc-w630"  data_scenic_has_img_tag="${item.scenicNameId}"
                                            		<#else>class="instance-travel-xc-info"</#if> 
                                            >
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
                                                    <span class="instance-travel-xc-info-pr20">游览时间：${visitTimeStr!''}</span>
                                                    </#if>
                                                </p>
                                             </#if> 
                                                <p>
                                                	<#if item.scenicExplain=="ROUTE_INCLUDED">
                                                	<#elseif item.scenicExplain=="SELF_PAYING">自费景点:
                                                	<#elseif item.scenicExplain=="RECOMMEND">推荐景点:
                                                	<#elseif item.scenicExplain=="GIVING">赠送景点:
                                                	<#else>
                                                	</#if>
                                                	<#--<#if item.scenicExplain !='' && item.referencePrice!="">-->
                                                	<#if (item.referencePrice!='') && (item.referencePrice gt 0)>
                                                		<#--参考价格${routeDetailFormat.formatPrice(item.referencePrice)!''}元-->
	                                                	<#assign scenicFormat = routeDetailFormat.getCnPriceFormat(item.referencePrice, item.currency, 'SCENIC')!'' />
	                                                	<#if scenicFormat?? && scenicFormat!=''>
	                                                   	  <span class="instance-travel-xc-info-pr20">参考价格：${scenicFormat!''}</span>
	                                                    </#if>
                                                    </#if>
                                                	<#if item.otherFeesTip!=''>
                                                			${item.otherFeesTip}
                                                	</#if>
                                                </p>
                                                <#if item.scenicDesc != "">
                                                <p class="instance-travel-xc-det-630">
                                                   <@func.addSpace item.scenicDesc 37/>
                                                </p>
                                                </#if>
                                            </div>
                                            <#if routeShowType=='PAGE' && item.scenicNameId?? && item.scenicNameId!='' && item.scenicDesc!=''>
	                                            <div class="instance-travel-xc-pic" style="display:none"  data_scenic_dest_id="${item.scenicNameId}" data_show_scenic_tag="show">
	                                                <img src="http://placehold.it/180x120/CCCCCC" width="180" height="120"/>
	                                            </div> 
                                            </#if>                                   
                                            
                                            
</li>