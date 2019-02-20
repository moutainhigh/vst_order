<!--默认用餐点模块-->
<!--定义group索引  groupIndex (routeDetailGroup_index )和detail的索引值detailIndex(item_index)-->
 <#assign isGroupLast = ( prodRouteDetailGroup_index==((prodLineRouteDetail.prodRouteDetailGroupList?size)-1) && detailIndex==(detailSize-1) )/>
 <#if item.mealType?? && item.mealType!=''>
 	<#if (item.mealType?index_of("BREAKFAST")!=-1) || (item.mealType?index_of("LUNCH")!=-1) || (item.mealType?index_of("DINNER")!=-1) >
 		<#assign isShowTotalDay=true /> 
 	</#if>
</#if>
 
 <li <#if isGroupLast>class="last"</#if>>	
				<#if detailIndex == 0>
					                        
                                            <div class="instance-travel-xc-time">
                                                <b>${prodRouteDetailGroup.getTimeType()!''}</b>
                                                <#if prodRouteDetailGroup.localTimeFlag == "Y"><span>当地时间</span></#if>
                                            </div>
                 </#if>
                
                                            <div class="instance-travel-xc-jt">
               								<#assign mealTypeCnName = routeDetailFormat.getMealTypeCnName(item.mealType)!'' />
				
                                            <#if detailIndex != 0 && item.logicRelation != "">
					                        	<em class="instance-travel-xc-and-or">（<#if item.logicRelation == "AND">和<#elseif item.logicRelation == "OR">或</#if>）</em>
											</#if>
                                               	${(mealTypeCnName=='')?string('用餐',mealTypeCnName)}	                                                
                                                <i class="instance-travel-xc-icon instance-travel-xc-icon-restaurant"></i>
                                            </div>
                                            <div class="instance-travel-xc-info">	                                            
	                                            <#if (item.mealTime?exists) || (  (isShowTotalDay?exists) && isShowTotalDay) &&  ( (item.breakfastMealTime?exists) || (item.lunchMealTime?exists) || (item.dinnerMealTime?exists)) >
								                   			<p>用餐时间
								                   			<#if item.mealTime?exists>
								                   			<#if item.mealTime?split(':')?size gt 1>
							                   					<#if item.mealTime?split(':')[0]!=''>${item.mealTime?split(':')[0]}小时</#if>
							                   					<#if item.mealTime?split(':')[1]!=''>${item.mealTime?split(':')[1]}分钟</#if>
								                   			<#else>
								                   				<#if item.mealTime?split(':')[0]!=''>${item.mealTime?split(':')[0]}小时</#if>
								                   			</#if>
								                   			</#if>
								                   			<#if  (item.mealType?index_of("BREAKFAST")!=-1) && item.breakfastMealTime?exists>
								                   				早餐
									                   			<#if item.breakfastMealTime?split(':')?size gt 1>
									                   				<#if item.breakfastMealTime?split(':')[0]!=''>${item.breakfastMealTime?split(':')[0]}小时</#if>
									                   				<#if item.breakfastMealTime?split(':')[1]!=''>${item.breakfastMealTime?split(':')[1]}分钟</#if>
									                   			<#else>	
									                   				<#if item.breakfastMealTime?split(':')[0]!=''>${item.breakfastMealTime?split(':')[0]}小时</#if>
									                   			</#if>
								                   			</#if>
								                   			<#if (item.mealType?index_of("LUNCH")!=-1) && item.lunchMealTime?exists>
								                   				中餐
									                   			<#if item.lunchMealTime?split(':')?size gt 1>
									                   				<#if item.lunchMealTime?split(':')[0]!=''>${item.lunchMealTime?split(':')[0]}小时</#if>
									                   				<#if item.lunchMealTime?split(':')[1]!=''>${item.lunchMealTime?split(':')[1]}分钟</#if>
									                   			<#else>	
									                   				<#if item.lunchMealTime?split(':')[0]!=''>${item.lunchMealTime?split(':')[0]}小时</#if>
									                   			</#if>
								                   			</#if>
								                   			<#if (item.mealType?index_of("DINNER")!=-1)  && item.dinnerMealTime?exists>
								                   				晚餐
									                   			<#if item.dinnerMealTime?split(':')?size gt 1>
									                   				<#if item.dinnerMealTime?split(':')[0]!=''>${item.dinnerMealTime?split(':')[0]}小时</#if>
									                   				<#if item.dinnerMealTime?split(':')[1]!=''>${item.dinnerMealTime?split(':')[1]}分钟</#if>
									                   			<#else>	
									                   				<#if item.dinnerMealTime?split(':')[0]!=''>
									                   				${item.dinnerMealTime?split(':')[0]}小时
									                   				</#if>
									                   			</#if>
								                   			</#if>
								                   			</p>
								                   		</#if>
	                                            
	                                            
	                                            <#if (prodRouteDetailGroup.getTimeType()!='') >
			                                            <#if prodRouteDetailGroup.getTimeType() == '全天'><!--全天-->
					                                            	<p>餐费标准：
					                                            		<#if (item.mealType?index_of("BREAKFAST")!=-1) && item.breakfastPrice!=''>
					                                            			<#assign mealPriceFormat = routeDetailFormat.getCnPriceFormat(item.breakfastPrice, item.breakfastCurrency, 'MEAL')!'' />
					                                            			<#if mealPriceFormat!''>早餐&nbsp;&nbsp;&nbsp;${mealPriceFormat!''}/人/餐 </#if>
					                                            		</#if>
					                                            		<#if (item.mealType?index_of("LUNCH")!=-1) && item.lunchPrice!=''>
					                                            			<#assign mealPriceFormat = routeDetailFormat.getCnPriceFormat(item.lunchPrice, item.lunchCurrency, 'MEAL')!'' />
					                                            			<#if mealPriceFormat!''>中餐&nbsp;&nbsp;&nbsp;${mealPriceFormat!''}/人/餐</#if>
					                                            		</#if>
					                                            		<#if (item.mealType?index_of("DINNER")!=-1) && item.dinnerPrice!=''>
					                                            			<#assign mealPriceFormat = routeDetailFormat.getCnPriceFormat(item.dinnerPrice, item.dinnerCurrency, 'MEAL')!'' />
					                                            			<#if mealPriceFormat!''>晚餐&nbsp;&nbsp;&nbsp;${mealPriceFormat!''}/人/餐</#if>
					                                            		</#if>
					                                            	</p>
					        							<#else><!--不是全天-->
						        							<#if item.price!=''>
				                                            	<#assign mealPriceFormat = routeDetailFormat.getCnPriceFormat(item.price, item.currency, 'MEAL')!'' />
			                                                	<#if mealPriceFormat!''><p>餐费标准：${mealPriceFormat!''}/人/餐</p></#if>
			                                                </#if>
					        							</#if>	
	                                            </#if>
	                        
                                                <#if item.mealPlace == 1>
								                    <#assign mealPlace = "酒店内"/>
								                <#else>
								                    <#assign mealPlace = item.mealPlaceOther/>
								                </#if>
                                                <#if mealPlace!''>
                                                <p>用餐地点：${mealPlace!''}</p>
                                                
                                                </#if>
                                                <#if item.mealDesc!''>
                                                <p class="instance-travel-xc-det-830">
                                                    	${item.mealDesc?replace("\n", "<br/>")!''}
                                                </p>
                                                </#if>
                                                
                                            </div>
</li>