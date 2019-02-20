 <#assign isGroupLast = ( prodRouteDetailGroup_index==((prodLineRouteDetail.prodRouteDetailGroupList?size)-1) && detailIndex==(detailSize-1) )/>
<li <#if isGroupLast>class="last"</#if>>
	<#if detailIndex == 0>
                                            <div class="instance-travel-xc-time">
                                             <#if prodRouteDetailGroup.getTimeType()!='全天'><b>${prodRouteDetailGroup.getTimeType()}</b></#if>
                                               <#-- <b>${prodRouteDetailGroup.getTimeType()!''}</b>-->
                                                <#if prodRouteDetailGroup.localTimeFlag == "Y"><span>当地时间</span></#if>
                                            </div>
   </#if>                                      
                                            <div class="instance-travel-xc-jt">
                                                <i class="instance-travel-xc-icon instance-travel-xc-icon-recom"></i>
                                               ${item.recommendName!''}
                                            </div>
                                            <div class="instance-travel-xc-info">
                                                <p>	
                                                	<#if (item.referencePrice!='') && (item.referencePrice gt 0)>
	                                                	<#assign mealPriceFormat = routeDetailFormat.getCnPriceFormat(item.referencePrice, item.currency, 'RECOMMEND')!'' />
	                                                	
	                                                	<#if mealPriceFormat?? && mealPriceFormat!=''>
	                                                    <span class="instance-travel-xc-info-pr20">参考价格：${mealPriceFormat!''}</span>
	                                                    </#if>
                                                    </#if>
                                                    <#if (item.visitTime!='') && (item.visitTime gt 0)>
                                                    <span class="instance-travel-xc-info-pr20">项目时间：${item.visitTime!''}分钟</span>
                                                    </#if>
                                                    
                                                </p>
                                                <p>地址：${item.address!''}</p>
                                                <#if item.recommendDesc!''>
                                                <p class="instance-travel-xc-det-830">
                                                     <@func.addSpace item.recommendDesc 37/>
                                                </p>
                                                </#if>
                                            </div>

                                            <!-- 分界线 -->
</li>
