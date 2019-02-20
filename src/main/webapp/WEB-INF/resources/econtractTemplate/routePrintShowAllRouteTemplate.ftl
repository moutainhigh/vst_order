<div class="instance-travel-xc-gb">

<#if product.prodLineRouteList??>
	<div class="instance-travel-xc-gb-border">
	  <#list product.prodLineRouteList as prodLineRoute>
		<#if prodLineRoute.lineRouteId == "${routeId}">
			<#list prodLineRoute.prodLineRouteDetailList as prodLineRouteDetail>
				<p class="instance-travel-xc-gb-p" id="travel-day-${prodLineRoute.lineRouteId}-${prodLineRouteDetail_index+1}">
				        <b>第${prodLineRouteDetail_index+1}天</b>
				        <#if prodLineRouteDetail.title?? && prodLineRouteDetail.title!=''>
				             <#assign titleArray=(prodLineRouteDetail.title)?split("-")>
				        </#if>
				        <#if titleArray??>
				        <#assign titleArraySize=titleArray?size />
				        <em>
				        	<#list titleArray as one>
				        	    ${one}
								<#if one_index != titleArraySize-1>
									<span class="instance-travel-xc-gb-p-span"></span>
								</#if>
							</#list>	
				        </em>
				        </#if>
				</p>
				<ul class="instance-travel-xc-gb_ul instance-travel-xc-gb_ul_old">
				<!--循环每一天行程-->
					<#if prodLineRouteDetail.prodRouteDetailGroupList?? && prodLineRouteDetail.prodRouteDetailGroupList?size gt 0 >
						<#list prodLineRouteDetail.prodRouteDetailGroupList as prodRouteDetailGroup>
								
											<!--定义group索引  groupIndex (routeDetailGroup_index )-->
												<#assign groupIndex=prodRouteDetailGroup_index/>
												<#if prodRouteDetailGroup.moduleType == 'SHOPPING'>
													<#if prodRouteDetailGroup.prodRouteDetailShoppingList?? && prodRouteDetailGroup.prodRouteDetailShoppingList?size gt 0>
														<#list prodRouteDetailGroup.prodRouteDetailShoppingList as item>
														<!--定义detail的索引值detailIndex(item_index)-->
															<#assign detailIndex=item_index/>
															<#assign detailSize=prodRouteDetailGroup.prodRouteDetailShoppingList?size/>
															<#include "/SHOPPING/"+((item.useTemplateFlag=='Y')?string(item.templateCode,'STRUCTURING_DEFAULT'))+".ftl"/>    
														</#list>
													</#if>
												</#if>
												
												<#if prodRouteDetailGroup.moduleType == 'SCENIC'>
													<#if prodRouteDetailGroup.prodRouteDetailScenicList?? && prodRouteDetailGroup.prodRouteDetailScenicList?size gt 0>
														<#list prodRouteDetailGroup.prodRouteDetailScenicList as item>
															<!--定义detail的索引值detailIndex(item_index)-->
															<#assign detailIndex=item_index/>
															<#assign detailSize=prodRouteDetailGroup.prodRouteDetailScenicList?size/>
															<#include "/SCENIC/"+((item.useTemplateFlag=='Y')?string('CODE_DEFAULT','STRUCTURING_DEFAULT'))+".ftl"/>    
														</#list>
													</#if>
												</#if>
												
												<#if prodRouteDetailGroup.moduleType == 'HOTEL'>
													<#if prodRouteDetailGroup.prodRouteDetailHotelList?? && prodRouteDetailGroup.prodRouteDetailHotelList?size gt 0>
														<#list prodRouteDetailGroup.prodRouteDetailHotelList as item>
															<#if item_index == 0>
																<#assign useTemplateFlag = item.useTemplateFlag />
															</#if>	
														</#list>
													</#if>
													<#if useTemplateFlag =='Y'>
														<#include "/HOTEL/CODE_DEFAULT.ftl"/>
													<#else>
														<#if prodRouteDetailGroup.prodRouteDetailHotelList?? && prodRouteDetailGroup.prodRouteDetailHotelList?size = 1>
															<#list prodRouteDetailGroup.prodRouteDetailHotelList as item>
																<!--定义detail的索引值detailIndex(item_index)-->
																<#assign detailIndex=item_index/>
																<#assign detailSize=prodRouteDetailGroup.prodRouteDetailHotelList?size/>
																<#include "/HOTEL/STRUCTURING_DEFAULT.ftl"/>    
															</#list>
														</#if>
														<#if prodRouteDetailGroup.prodRouteDetailHotelList?? && prodRouteDetailGroup.prodRouteDetailHotelList?size gt 1>
																<!--定义detail的索引值detailIndex(item_index)-->
																<#assign detailSize=prodRouteDetailGroup.prodRouteDetailHotelList?size/>
																<#include "/HOTEL/STRUCTURING_MANYHOTELS.ftl"/>    
														</#if>
													</#if>
												</#if>
												
												<#if prodRouteDetailGroup.moduleType == 'MEAL'>
													<#if prodRouteDetailGroup.prodRouteDetailMealList?? && prodRouteDetailGroup.prodRouteDetailMealList?size gt 0>
														<#list prodRouteDetailGroup.prodRouteDetailMealList as item>
															<!--定义detail的索引值detailIndex(item_index)-->
															<#assign detailIndex=item_index/>
															<#assign detailSize=prodRouteDetailGroup.prodRouteDetailMealList?size/>
															<#include "/MEAL/"+((item.useTemplateFlag=='Y')?string(item.templateCode,'STRUCTURING_DEFAULT'))+".ftl"/>    
														</#list>
													</#if>
												</#if>
												
												<#if prodRouteDetailGroup.moduleType == 'FREE_ACTIVITY'>
													<#if prodRouteDetailGroup.prodRouteDetailActivityList?? && prodRouteDetailGroup.prodRouteDetailActivityList?size gt 0>
														<#list prodRouteDetailGroup.prodRouteDetailActivityList as item>
															<!--定义detail的索引值detailIndex(item_index)-->
															<#assign detailIndex=item_index/>
															<#assign detailSize=prodRouteDetailGroup.prodRouteDetailActivityList?size/>
															<#include "/FREE_ACTIVITY/STRUCTURING_DEFAULT.ftl"/>    
														</#list>
													</#if>
												</#if>
												
												<#if prodRouteDetailGroup.moduleType == 'OTHER_ACTIVITY'>
													<#if prodRouteDetailGroup.prodRouteDetailActivityList?? && prodRouteDetailGroup.prodRouteDetailActivityList?size gt 0>
														<#list prodRouteDetailGroup.prodRouteDetailActivityList as item>
															<!--定义detail的索引值detailIndex(item_index)-->
															<#assign detailIndex=item_index/>
															<#assign detailSize=prodRouteDetailGroup.prodRouteDetailActivityList?size/>
															<#include "/OTHER_ACTIVITY/STRUCTURING_DEFAULT.ftl"/>    
														</#list>
													</#if>
												</#if>
												
												<#if prodRouteDetailGroup.moduleType == 'VEHICLE'>
													<#if prodRouteDetailGroup.prodRouteDetailVehicleList?? && prodRouteDetailGroup.prodRouteDetailVehicleList?size gt 0>
														<#list prodRouteDetailGroup.prodRouteDetailVehicleList as item>
															<!--定义detail的索引值detailIndex(item_index)-->
															<#assign detailIndex=item_index/>
															<#assign detailSize=prodRouteDetailGroup.prodRouteDetailVehicleList?size/>
															<#include "/VEHICLE/STRUCTURING_DEFAULT.ftl"/>    
														</#list>
													</#if>
												</#if>	
												
												<#if prodRouteDetailGroup.moduleType == 'RECOMMEND'>
													<#if prodRouteDetailGroup.prodRouteDetailRecommendList?? && prodRouteDetailGroup.prodRouteDetailRecommendList?size gt 0>
														<#list prodRouteDetailGroup.prodRouteDetailRecommendList as item>
															<!--定义detail的索引值detailIndex(item_index)-->
															<#assign detailIndex=item_index/>
															<#assign detailSize=prodRouteDetailGroup.prodRouteDetailRecommendList?size/>
															<#include "/RECOMMEND/STRUCTURING_DEFAULT.ftl"/>    
														</#list>
													</#if>
												</#if>				
								
							</#list>
						</#if>
					</ul>
	 			</#list>	
		</#if>
  </#list>
	</div>
</#if>
	
</div>