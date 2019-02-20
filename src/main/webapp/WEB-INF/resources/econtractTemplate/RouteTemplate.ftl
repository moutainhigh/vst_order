<!--行程发送子模板-->
<#macro routeTemplate lineRoute>
<#if lineRoute.routeDetailFormat??>
<#assign routeDetailFormat = lineRoute.routeDetailFormat />
</#if>
<!--按nDay升序遍历行程明细列表 开始-->
	<#list lineRoute.prodLineRouteDetailList?sort_by("nDay")  as prodLineRouteDetail> 
    <!-- 第N天标题 -->
    <tr>
        <td style="padding-left: 10px;height: 40px;font-size: 14px;line-height: 40px;background: #E51785;color: #fff;">
        	第${prodLineRouteDetail.nDay!''}天&nbsp;&nbsp;&nbsp;&nbsp;${prodLineRouteDetail.title!''}
        </td>
    </tr>   
    <!-- 第N天内容 -->
    <tr>
        <td style="padding:5px;">
            <table border="0" cellspacing="0" cellpadding="0" style="border-collapse:collapse;font-size: 12px;line-height:22px;color: #666;">
                <tbody>
                    <!--如果行程明细里存在行程明细组列表，则遍历组列表并展示 开始-->
				    <#if prodLineRouteDetail.prodRouteDetailGroupList??>
				    	<!--按sortValue升序遍历行程明细组列表 开始--> 
				    	<#list prodLineRouteDetail.prodRouteDetailGroupList  as routeDetailGroup>
				    		<!--如果模块类型存在，则遍历此类型列表并展示相关信息 开始-->
							<#if routeDetailGroup.moduleType??>
								<#if routeDetailGroup.moduleType='SCENIC'><!--景点-->
									<#assign scenicSize = routeDetailGroup.prodRouteDetailScenicList?size />
									<#list routeDetailGroup.prodRouteDetailScenicList as scenic>
										<#assign indexPlus = scenic_index+1 />
										<!-- 如果下一项是“和”或者“或”  tr不加 border-bottom:1px dashed #ccc; -->
										<!-- 逻辑关系只有景点，购物点与酒店有 -->
							            <tr <#if indexPlus lt scenicSize && routeDetailGroup.prodRouteDetailScenicList[indexPlus].logicRelateion??><#else>style="border-bottom:1px dashed #ccc;"</#if> >
							                <!--如果当前景点不是第一条且存在逻辑关系，则显示逻辑关系 和/或，否则显示时间-->
							                <#if scenic.logicRelateion?? && scenic_index!='0'>
							                	<td valign="top" width="100px;" style="padding:13px 0;">${scenic.logicRelateionName}</td>
							                <#else>
							                   <td valign="top" width="100px;" style="padding:13px 0;">
							                   		<#if routeDetailGroup.timeType!='全天'>${routeDetailGroup.timeType}</#if><br>
							                       <#if routeDetailGroup.localTimeFlag?? && routeDetailGroup.localTimeFlag='Y'>当地时间<#else></#if>
							                   </td>
							                </#if>
							               <!--如果使用话术模板-->
											<#if scenic.useTemplateFlag="Y">
												<td style="padding:10px 0;">
													<#if scenic.templateText??>
								                   		<@func.addSpace scenic.templateText 42/>
							                   		</#if>
												</td>
											<#else>
								               <td style="padding:0 0 10px 0;">
								                   <!-- 灰色字体 加 color:#999;-->
								                   <p style="color:#999; margin-bottom:10px;">
								                   		<#if scenic.scenicName?exists>前往景点：${scenic.scenicName} &nbsp;&nbsp; 
                                                             <#if scenic.briefExplain?? && scenic.briefExplain == 'OUTSIDE_SEE'>
                                                                                                                                                                                                      外观，此景点不含门票费用
                                                             <#elseif scenic.briefExplain?? && scenic.briefExplain == 'TICKET_COST_INCLUDE'>
                                                                                                                                                                                                      此景点有门票景点费用         
                                                             <#elseif scenic.briefExplain?? && scenic.briefExplain == 'TICKET_COST_EXCLUDE'>  
                                                                                                                                                                                                        此景点无门票景点费用                                                                                                                                                                                                                                                                    
                                                             </#if>                                             
                                                        </#if>  
								                   		<#if scenic.travelTime?exists>
								                   			行驶时间：
								                   			<#if scenic.travelTime?split(':')?size gt 1>
								                   				${scenic.travelTime?split(':')[0]}小时<#if scenic.travelTime?split(':')[1]!=''>${scenic.travelTime?split(':')[1]}分钟</#if>
								                   			<#else>
								                   				${scenic.travelTime?split(':')[0]}小时
								                   			</#if>
								                   			&nbsp;&nbsp;&nbsp;&nbsp;
								                   		</#if>
								                   		<#if scenic.distanceKM?exists>行驶距离：${scenic.distanceKM}公里&nbsp;&nbsp;&nbsp;&nbsp;</#if>
								                   		<#if scenic.scenicName?exists || scenic.travelTime?exists || scenic.distanceKM?exists>
								                   			<br/>
								                   		</#if>	
								                   		<#if scenic.visitTime?exists>
								                   			游览时间：
								                   			<#if scenic.visitTime?split(':')?size gt 1>
								                   				${scenic.visitTime?split(':')[0]}小时<#if scenic.visitTime?split(':')[1]!=''>${scenic.visitTime?split(':')[1]}分钟</#if>
								                   			<#else>
								                   				${scenic.visitTime?split(':')[0]}小时
								                   			</#if>
								                   			&nbsp;&nbsp;&nbsp;&nbsp;
								                   		</#if>
								                   		<#if scenic.scenicExplain!='ROUTE_INCLUDED' && scenic.scenicExplainName?exists>${scenic.scenicExplainName}&nbsp;&nbsp;&nbsp;&nbsp;</#if>
								                   		<#--<#if scenic.referencePrice?exists>参考价格${scenic.referencePrice/100}元&nbsp;&nbsp;&nbsp;&nbsp;</#if>-->
								                   		<#if scenic.referencePrice?exists>
								                   			<#if routeDetailFormat?exists>
							                   					<#assign referencePriceFormat = routeDetailFormat.getCnPriceFormat(scenic.referencePrice, scenic.currency, 'SCENIC')!'' />
							                   				</#if>
							                   				<#if referencePriceFormat!''>${referencePriceFormat!''}<#else>参考价格${scenic.referencePrice/100}元</#if>
								                   			&nbsp;&nbsp;&nbsp;&nbsp;
								                   		</#if>
								                   		${scenic.otherFeesTip}
								                   </p>
								                   <p style="margin-bottom:10px;">
								                   		<!--景点描述-->
								                   		<#if scenic.scenicDesc??>
									                   		<@func.addSpace scenic.scenicDesc 42/>
								                   		</#if>
								                   </p>
								               </td>
									        </#if>
							            </tr>
									</#list>
								<#elseif routeDetailGroup.moduleType='HOTEL'><!--酒店-->
										<!--如果组内话术模板不为空，则表示酒店使用话术模板-->
										<#if routeDetailGroup.hotelTemplateText?? && routeDetailGroup.hotelTemplateText!="">
											<tr style="border-bottom:1px dashed #ccc;">
												<td valign="top" width="100px;" style="padding:13px 0;">
							                   		<#if routeDetailGroup.timeType!='全天'>${routeDetailGroup.timeType}</#if><br>
							                       <#if routeDetailGroup.localTimeFlag?? && routeDetailGroup.localTimeFlag='Y'>当地时间<#else></#if>
							                   </td>
							                   <td style="padding:10px 0;">
							                   		<#if routeDetailGroup.hotelTemplateText??>
								                   		<@func.addSpace routeDetailGroup.hotelTemplateText 42/>
							                   		</#if>
							                   </td>
											</tr>
										<#else>
											<#if routeDetailGroup.prodRouteDetailHotelList?? && routeDetailGroup.prodRouteDetailHotelList?size = 1>
												<#assign hotelSize = routeDetailGroup.prodRouteDetailHotelList?size />
												<#list routeDetailGroup.prodRouteDetailHotelList as hotel>
													<#assign hotelIndexPlus = hotel_index+1 />
													<!-- 如果下一项是“和”或者“或”  tr不加 border-bottom:1px dashed #ccc; -->
										            <tr <#if hotelIndexPlus lt hotelSize && routeDetailGroup.prodRouteDetailHotelList[hotelIndexPlus].logicRelateion??><#else>style="border-bottom:1px dashed #ccc;"</#if> >
										                <!--如果当前酒店不是第一条且存在逻辑关系，则显示逻辑关系 和/或，否则显示时间-->	
										                <#if hotel.logicRelateion?? && hotel_index!='0'>
										                	<td valign="top" width="100px;" style="padding:13px 0;">${hotel.logicRelateionName}</td>
										                <#else>
										                   <td valign="top" width="100px;" style="padding:13px 0;">
										                   		<#if routeDetailGroup.timeType!='全天'>${routeDetailGroup.timeType}</#if><br>
										                       <#if routeDetailGroup.localTimeFlag?? && routeDetailGroup.localTimeFlag='Y'>当地时间<#else></#if>
										                   </td>
										                </#if>
										               <td style="padding:0 0 10px 0;">
										                   <!-- 灰色字体 加 color:#999;-->
										                   <p style="color:#999; margin-bottom:10px;">入住酒店&nbsp;&nbsp;&nbsp;&nbsp;
										                   		<#if hotel.travelTime?exists>
										                   			行驶时间：
										                   			<#if hotel.travelTime?split(':')?size gt 1>
									                   					${hotel.travelTime?split(':')[0]}小时<#if hotel.travelTime?split(':')[1]!=''>${hotel.travelTime?split(':')[1]}分钟</#if>
										                   			<#else>
										                   				${hotel.travelTime?split(':')[0]}小时
										                   			</#if>
										                   			&nbsp;&nbsp;&nbsp;&nbsp;
										                   		</#if>
										                   		<#if hotel.distanceKM?exists>行驶距离：${hotel.distanceKM}公里&nbsp;&nbsp;&nbsp;&nbsp;</#if>
										                   		<br/>
										                   		${hotel.belongToPlace}&nbsp;&nbsp;&nbsp;&nbsp;
										                   		${hotel.hotelName}&nbsp;&nbsp;&nbsp;&nbsp;
										                   		${hotel.roomType}
										                   </p>
										                   <p style="margin-bottom:10px;">
										                   		<#if hotel.hotelDesc??>
											                   		<@func.addSpace hotel.hotelDesc 42/>
										                   		</#if>
										                   </p>
										               </td>
										            </tr>
												</#list>
											</#if>	
											<#if routeDetailGroup.prodRouteDetailHotelList?? && routeDetailGroup.prodRouteDetailHotelList?size gt 1>
												<#assign hotelSize = routeDetailGroup.prodRouteDetailHotelList?size />
												<tr style="border-bottom:1px dashed #ccc;">
								                   <td valign="top" width="100px;" style="padding:13px 0;">
								                   	   <#if routeDetailGroup.timeType!='全天'>${routeDetailGroup.timeType}</#if><br>
								                       <#if routeDetailGroup.localTimeFlag?? && routeDetailGroup.localTimeFlag='Y'>当地时间<#else></#if>
								                   </td>
									               <td style="padding:0 0 10px 0;">
									                   <!-- 灰色字体 加 color:#999;-->
									                   <p style="color:#999; margin-bottom:10px;">入住&nbsp;&nbsp;
										                   <#list routeDetailGroup.prodRouteDetailHotelList as hotel>
										                  	    <#assign hotelIndexPlus = hotel_index+1 />
										                  	    <#if hotel.hotelName!='' && hotel.hotelName!='未指定酒店'>
						                     			  			${hotel.hotelName}&nbsp;&nbsp;
						                     			  		<#else>
							                                                                                                                          未指定酒店(${hotel.starLevelName})&nbsp;&nbsp;
						                                    	</#if>	
										                   		<#if hotelIndexPlus lt hotelSize>
										                   			或&nbsp;&nbsp;
										                   		</#if>
										                   </#list>	
									                   </p>
									                   <p style="margin-bottom:10px;">
									                   	    <#list routeDetailGroup.prodRouteDetailHotelList as hotel>
										                   		<#if hotel.hotelDesc??>
										                   			<#if hotel.hotelName!='' && hotel.hotelName!='未指定酒店'>
							                     			  			（${hotel.hotelName}）<@func.addSpace hotel.hotelDesc 42/><br/>
							                     			  		 <#else>
								                                                                                                                         （ 未指定酒店）<@func.addSpace hotel.hotelDesc 42/><br/>
							                                    	</#if>	
										                   		</#if>
									                   		</#list>	
									                   </p>
									               </td>
									            </tr>
											</#if>
								       </#if>
								<#elseif routeDetailGroup.moduleType='SHOPPING'><!--购物点-->
									<#assign shoppingSize = routeDetailGroup.prodRouteDetailShoppingList?size />
									<#list routeDetailGroup.prodRouteDetailShoppingList as shopping>
										<#assign shoppingIndexPlus = shopping_index+1 />
										<!-- 如果下一项是“和”或者“或”  tr不加 border-bottom:1px dashed #ccc; -->
							            <tr <#if shoppingIndexPlus lt shoppingSize && routeDetailGroup.prodRouteDetailShoppingList[shoppingIndexPlus].logicRelateion??><#else>style="border-bottom:1px dashed #ccc;"</#if> >
							                <!--如果当前购物点不是第一条且存在逻辑关系，则显示逻辑关系 和/或，否则显示时间-->	
							                <#if shopping.logicRelation?? && shopping_index!='0'>
							                	<td valign="top" width="100px;" style="padding:13px 0;">${shopping.logicRelationName}</td>
							                <#else>
							                   <td valign="top" width="100px;" style="padding:13px 0;">
							                   		<#if routeDetailGroup.timeType!='全天'>${routeDetailGroup.timeType}</#if><br>
							                       <#if routeDetailGroup.localTimeFlag?? && routeDetailGroup.localTimeFlag='Y'>当地时间<#else></#if>
							                   </td>
							                </#if>
							               
							               <!--如果使用话术模板-->
											<#if shopping.useTemplateFlag="Y">
												<td style="padding:10px 0;">
													<#if shopping.templateText??>
								                   		<@func.addSpace shopping.templateText 42/>
							                   		</#if>
												</td>
											<#else>
								               <td style="padding:0 0 10px 0;">
								                   <!-- 灰色字体 加 color:#999;-->
								                   <p style="color:#999; margin-bottom:10px;">
								                   		<#if shopping.shoppingName?exists>购物点：${shopping.shoppingName}<#if shopping.recommendFlag == "Y">[推荐购物点]</#if><br/></#if>
								                   		<#if shopping.travelTime?exists>
								                   			行驶时间：
								                   			<#if shopping.travelTime?split(':')?size gt 1>
							                   					${shopping.travelTime?split(':')[0]}小时<#if shopping.travelTime?split(':')[1]!=''>${shopping.travelTime?split(':')[1]}分钟</#if>
								                   			<#else>
								                   				${shopping.travelTime?split(':')[0]}小时
								                   			</#if>
								                   			&nbsp;&nbsp;&nbsp;&nbsp;
								                   		</#if>
								                   		<#if shopping.distanceKM?exists>行驶距离：${shopping.distanceKM}公里&nbsp;&nbsp;&nbsp;&nbsp;</#if>
								                   		<#if shopping.visitTime?exists>
								                   			游览时间：
								                   			<#if shopping.visitTime?split(':')?size gt 1>
							                   					${shopping.visitTime?split(':')[0]}小时<#if shopping.visitTime?split(':')[1]!=''>${shopping.visitTime?split(':')[1]}分钟</#if>
								                   			<#else>
								                   				${shopping.visitTime?split(':')[0]}小时
								                   			</#if>
								                   		</#if> 
								                   </p>
								                   <#if shopping.address?exists>
								                   		<p style="color:#999; margin-bottom:10px;">地址：${shopping.address}</p>	
								                   </#if>
								                   <#if shopping.mainProducts?exists>
								                   		<p style="color:#999; margin-bottom:10px;">主营产品：${shopping.mainProducts}</p>
								                   </#if>
								                   <#if shopping.subjoinProducts?exists>
								                   		<p style="color:#999; margin-bottom:10px;">兼营产品：${shopping.subjoinProducts}</p>
								                   </#if>
								                   
								                   <!--购物点说明-->
								                   <p style="margin-bottom:10px;">
								                   		<#if shopping.shoppingDesc??>
									                   		<@func.addSpace shopping.shoppingDesc 42/><br/>
								                   		</#if>
								                   </p>
								               </td>
											</#if>
							            </tr>
									</#list>
								<#elseif routeDetailGroup.moduleType='VEHICLE'><!--交通-->
									<#list routeDetailGroup.prodRouteDetailVehicleList as vehicle>
							            <!-- 如果下一项是“和”或者“或”  tr不加 border-bottom:1px dashed #ccc; --><!--交通组-->
							            <tr>
							               <td valign="top" width="100px;" style="padding:13px 0;">
							               		${routeDetailGroup.timeType}<br>
							                   <#if routeDetailGroup.localTimeFlag?? && routeDetailGroup.localTimeFlag='Y'>当地时间<#else></#if>
							               </td>
							               <td style="padding:0 0 10px 0;">
							                   <!-- 灰色字体 加 color:#999;-->
							                   <p style="color:#999; margin-bottom:10px;">
							                   		<#if vehicle.vehicleName?exists>交通：${vehicle.vehicleName}&nbsp;&nbsp;&nbsp;&nbsp;</#if>
							                   		<#if vehicle.vehicleTime?exists>
							                   			行驶时间：
							                   			<#if vehicle.vehicleTime?split(':')?size gt 1>
						                   					${vehicle.vehicleTime?split(':')[0]}小时<#if vehicle.vehicleTime?split(':')[1]!=''>${vehicle.vehicleTime?split(':')[1]}分钟</#if>
							                   			<#else>
							                   				${vehicle.vehicleTime?split(':')[0]}小时
							                   			</#if>
							                   			&nbsp;&nbsp;&nbsp;&nbsp;
							                   		</#if>
							                   		<#if vehicle.vehicleKm?exists>行驶距离：${vehicle.vehicleKm}公里</#if>
							                   </p>
							                   <p>
	                                                <#if vehicle.vehicleType=='PLANE' && vehicle.pickUpFlag == 'Y'>
											                          	  接机服务： 接机人员第${vehicle.pickUpDay}天接机；
											        <#elseif vehicle.vehicleType=='TRAIN' && vehicle.pickUpFlag == 'Y'>
											                            接车服务：接车人员第${vehicle.pickUpDay}天接车；
										            </#if>
                                               </p>
							                   <!--交通说明-->
							                   <p style="margin-bottom:10px;">
							                   		<#if vehicle.vehicleDesc??>
								                   		<@func.addSpace vehicle.vehicleDesc 42/><br/>
							                   		</#if>
							                   </p>
							               </td>
							            </tr>
									</#list>
								<#elseif routeDetailGroup.moduleType='MEAL'><!--用餐-->
									<#list routeDetailGroup.prodRouteDetailMealList as meal>
											<#if meal.mealType?? && meal.mealType!=''>
										 	<#if (meal.mealType?index_of("BREAKFAST")!=-1) || (meal.mealType?index_of("LUNCH")!=-1) || (meal.mealType?index_of("DINNER")!=-1) >
										 		<#assign isShowTotalDay=true /> 
										 	</#if>
											</#if>
								
							             <!-- 如果下一项是“和”或者“或”  tr不加 border-bottom:1px dashed #ccc; -->
							            <tr style="border-bottom:1px dashed #ccc;">
							               <td valign="top" width="100px;" style="padding:13px 0;">
							               		${routeDetailGroup.timeType}<br>
							                   <#if routeDetailGroup.localTimeFlag?? && routeDetailGroup.localTimeFlag='Y'>当地时间<#else></#if>
							               </td>
							               
							               <!--如果使用话术模板-->
											<#if meal.useTemplateFlag="Y">
												<td style="padding:10px 0;">
													<#if meal.templateText??>
								                   		<@func.addSpace meal.templateText 42/>
							                   		</#if>
												</td>
											<#else>
								               <td style="padding:0 0 10px 0;">
								                   <!-- 灰色字体 加 color:#999;-->
								                   <p style="color:#999; margin-bottom:10px;">
								                   		<#if meal.mealName?exists>用餐：${meal.mealName}&nbsp;&nbsp;&nbsp;&nbsp;</#if>
								                   		<#if (meal.mealTime?exists) || ((  (isShowTotalDay?exists) && isShowTotalDay) &&  ( (meal.breakfastMealTime?exists) || (meal.lunchMealTime?exists) || (meal.dinnerMealTime?exists))) >
								                   			用餐时间
								                   			<#if meal.mealTime?exists>
								                   			<#if meal.mealTime?split(':')?size gt 1>
							                   					${meal.mealTime?split(':')[0]}小时<#if meal.mealTime?split(':')[1]!=''>${meal.mealTime?split(':')[1]}分钟</#if>
								                   			<#else>
								                   				${meal.mealTime?split(':')[0]}小时
								                   			</#if>
								                   			</#if>
								                   			
								                   			
								                   			<#if   (meal.mealType?index_of("BREAKFAST")!=-1) && meal.breakfastMealTime?exists>
								                   				&nbsp;&nbsp;早餐
									                   			<#if meal.breakfastMealTime?split(':')?size gt 1>
									                   				${meal.breakfastMealTime?split(':')[0]}小时<#if meal.breakfastMealTime?split(':')[1]!=''>${meal.breakfastMealTime?split(':')[1]}分钟</#if>
									                   			<#else>	
									                   				${meal.breakfastMealTime?split(':')[0]}小时
									                   			</#if>
								                   			</#if>
								                   			<#if (meal.mealType?index_of("LUNCH")!=-1) && meal.lunchMealTime?exists>
								                   				&nbsp;&nbsp;中餐
									                   			<#if meal.lunchMealTime?split(':')?size gt 1>
									                   				${meal.lunchMealTime?split(':')[0]}小时<#if meal.lunchMealTime?split(':')[1]!=''>${meal.lunchMealTime?split(':')[1]}分钟</#if>
									                   			<#else>	
									                   				${meal.lunchMealTime?split(':')[0]}小时
									                   			</#if>
								                   			</#if>
								                   			<#if (meal.mealType?index_of("DINNER")!=-1) &&  meal.dinnerMealTime?exists>
								                   				&nbsp;&nbsp;晚餐
									                   			<#if meal.dinnerMealTime?split(':')?size gt 1>
									                   				${meal.dinnerMealTime?split(':')[0]}小时<#if meal.dinnerMealTime?split(':')[1]!=''>${meal.dinnerMealTime?split(':')[1]}分钟</#if>
									                   			<#else>	
									                   				${meal.dinnerMealTime?split(':')[0]}小时
									                   			</#if>
								                   			</#if>
								                   			
								                   			&nbsp;&nbsp;&nbsp;&nbsp;
								                   		</#if>
							
								                   		<#if meal.mealPlace?exists>用餐地点：<#if meal.mealPlace=="1">酒店内<#elseif meal.mealPlace=="2"&& meal.mealPlaceOther??>${meal.mealPlaceOther}</#if>&nbsp;&nbsp;&nbsp;&nbsp;</#if>
								                   		<#if meal.mealName?exists || meal.mealTime?exists || meal.mealPlace?exists>
								                   			<br/>
								                   		</#if>
								                   		<#if routeDetailGroup.timeType=='全天'>
								                   			<#if ((isShowTotalDay?exists) && isShowTotalDay)  && ( (meal.breakfastPrice?exists )|| (meal.lunchPrice?exists) || (meal.dinnerPrice?exists))>
								                   				用餐餐标：
								                   				<#if (meal.mealType?index_of("BREAKFAST")!=-1) && meal.breakfastPrice?exists>
								                   					
								                   					<#if routeDetailFormat?exists>
								                   						<#assign mealPriceFormat = routeDetailFormat.getCnPriceFormat(meal.breakfastPrice, meal.breakfastCurrency, 'MEAL')!'' />
								                   					</#if>
					                                            	<#if mealPriceFormat!''>早餐${mealPriceFormat!''}<#else>早餐${meal.breakfastPrice/100}元</#if>
					                                            	&nbsp;
								                   				</#if>
								                   				<#if (meal.mealType?index_of("LUNCH")!=-1) && meal.lunchPrice?exists>
								                   					<#if routeDetailFormat?exists>
								                   						<#assign lunchMealPriceFormat = routeDetailFormat.getCnPriceFormat(meal.lunchPrice, meal.lunchCurrency, 'MEAL')!'' />
								                   					</#if>
					                                            	<#if lunchMealPriceFormat!''>中餐${lunchMealPriceFormat!''}<#else>中餐${meal.lunchPrice/100}元</#if>
								                   					&nbsp;
								                   				</#if>
								                   				<#if (meal.mealType?index_of("DINNER")!=-1) && meal.dinnerPrice?exists>
								                   					<#if routeDetailFormat?exists>
								                   						<#assign dinnerMealPriceFormat = routeDetailFormat.getCnPriceFormat(meal.dinnerPrice, meal.dinnerCurrency, 'MEAL')!'' />
								                   					</#if>
					                                            	<#if dinnerMealPriceFormat!''>晚餐${dinnerMealPriceFormat!''}<#else>晚餐${meal.dinnerPrice/100}元</#if>
					                                            	&nbsp;
								                   				</#if>
								                   				
								                   			</#if>
								                   		<#else>
								                   			<#if meal.price?exists>
								                   				用餐餐标：
								                   				<#if routeDetailFormat?exists>
								                   						<#assign mealPriceFormat = routeDetailFormat.getCnPriceFormat(meal.price, meal.currency, 'MEAL')!'' />
								                   				</#if>
								                   				<#if mealPriceFormat!''>${mealPriceFormat!''}<#else>${meal.price/100}元</#if>
								                   				&nbsp;&nbsp&nbsp;&nbsp
								                   			</#if>	
								                   		</#if>
								                   		<#if meal.referencePrice?exists>
								                   			参考价格：
								                   			<#if routeDetailFormat?exists>
							                   					<#assign referencePriceFormat = routeDetailFormat.getCnPriceFormat(meal.referencePrice, meal.currency, 'MEAL')!'' />
							                   				</#if>
							                   				<#if referencePriceFormat!''>${referencePriceFormat!''}<#else>${meal.referencePrice/100}元</#if>
							                   				&nbsp;&nbsp;&nbsp;&nbsp;
								                   		</#if>
								                   		${meal.otherFeesTip}
								                   </p>
								                   <!--用餐说明-->
								                   <p style="margin-bottom:10px;">
								                   		<#if meal.mealDesc??>
									                   		<@func.addSpace meal.mealDesc 42/><br/>
								                   		</#if>
								                   </p>
								               </td>
											</#if>
							            </tr>
									</#list>
								<#elseif routeDetailGroup.moduleType='FREE_ACTIVITY'><!--自由活动-->
									<#list routeDetailGroup.prodRouteDetailActivityList as activity>
										<!-- 如果下一项是“和”或者“或”  tr不加 border-bottom:1px dashed #ccc; -->
							            <tr style="border-bottom:1px dashed #ccc;">
							               <td valign="top" width="100px;" style="padding:13px 0;">
							               		<#if routeDetailGroup.timeType!='全天'>${routeDetailGroup.timeType}</#if><br>
							                   <#if routeDetailGroup.localTimeFlag?? && routeDetailGroup.localTimeFlag='Y'>当地时间<#else></#if>
							               </td>
							               <td style="padding:0 0 10px 0;">
							                   <!-- 灰色字体 加 color:#999;-->
							                   <p style="color:#999; margin-bottom:10px;">
							                   		自由活动&nbsp;&nbsp;&nbsp;&nbsp;	
							                   		<#if activity.travelTime?exists>
							                   			行驶时间：
							                   			<#if activity.travelTime?split(':')?size gt 1>
						                   					${activity.travelTime?split(':')[0]}小时<#if activity.travelTime?split(':')[1]!=''>${activity.travelTime?split(':')[1]}分钟</#if>
							                   			<#else>
							                   				${activity.travelTime?split(':')[0]}小时
							                   			</#if>
							                   			&nbsp;&nbsp;&nbsp;&nbsp;
							                   		</#if>
							                   		<#if activity.distanceKm?exists>行驶距离：${activity.distanceKm}公里&nbsp;&nbsp;&nbsp;&nbsp;</#if>
							                   		<#if activity.visitTime?exists>
							                   			活动时间：
							                   			<#if activity.visitTime?split(':')?size gt 1>
						                   					${activity.visitTime?split(':')[0]}小时<#if activity.visitTime?split(':')[1]!=''>${activity.visitTime?split(':')[1]}分钟</#if>
							                   			<#else>
							                   				${activity.visitTime?split(':')[0]}小时
							                   			</#if>
							                   		</#if>
							                   </p>
							                   <!--自由活动说明-->
							                   <p style="margin-bottom:10px;">
							                   		<#if activity.activityDesc??>
								                   		<@func.addSpace activity.activityDesc 42/><br/>
							                   		</#if>
							                   </p>
							               </td>
							            </tr>
									</#list>
								<#elseif routeDetailGroup.moduleType='OTHER_ACTIVITY'><!--其他活动-->
									<#list routeDetailGroup.prodRouteDetailActivityList as activity>
							            <!-- 如果下一项是“和”或者“或”  tr不加 border-bottom:1px dashed #ccc; -->
							            <tr style="border-bottom:1px dashed #ccc;">
							               <td valign="top" width="100px;" style="padding:13px 0;">
							               		<#if routeDetailGroup.timeType!='全天'>${routeDetailGroup.timeType}</#if><br>
							                   <#if routeDetailGroup.localTimeFlag?? && routeDetailGroup.localTimeFlag='Y'>当地时间<#else></#if>
							               </td>
							               <td style="padding:0 0 10px 0;">
							                   <!-- 灰色字体 加 color:#999;-->
							                   <p style="color:#999; margin-bottom:10px;">
							                   		<#if activity.activityName?exists>${activity.activityName}&nbsp;&nbsp;&nbsp;&nbsp;</#if>
							                   		<#if activity.travelTime?exists>
							                   			行驶时间：
							                   			<#if activity.travelTime?split(':')?size gt 1>
						                   					${activity.travelTime?split(':')[0]}小时<#if activity.travelTime?split(':')[1]!=''>${activity.travelTime?split(':')[1]}分钟</#if>
							                   			<#else>
							                   				${activity.travelTime?split(':')[0]}小时
							                   			</#if>
							                   			&nbsp;&nbsp;&nbsp;&nbsp;
							                   		</#if>
							                   		<#if activity.distanceKm?exists>行驶距离：${activity.distanceKm}公里&nbsp;&nbsp;&nbsp;&nbsp;</#if>
							                   		<#if activity.visitTime?exists>
							                   			活动时间：
							                   			<#if activity.visitTime?split(':')?size gt 1>
						                   					${activity.visitTime?split(':')[0]}小时<#if activity.visitTime?split(':')[1]!=''>${activity.visitTime?split(':')[1]}分钟</#if>
							                   			<#else>
							                   				${activity.visitTime?split(':')[0]}小时
							                   			</#if>
							                   		</#if>
							                   </p>
							                   <!--其他活动说明-->
							                   <p style="margin-bottom:10px;">
							                   		<#if activity.activityDesc??>
								                   		<@func.addSpace activity.activityDesc 42/><br/>
							                   		</#if>
							                   </p>
							               </td>
							            </tr>
									</#list>
									<!--其他活动结束-->
									<#elseif routeDetailGroup.moduleType='RECOMMEND'><!--推荐模块-->
									<!--推荐项目开始-->
									<#list routeDetailGroup.prodRouteDetailRecommendList as recommend>
										<tr style="border-bottom:1px dashed #ccc;">
							               <td valign="top" width="100px;" style="padding:13px 0;">
							               		<#--${routeDetailGroup.timeType}<br>-->
							               		<#if routeDetailGroup.timeType!='全天'>${routeDetailGroup.timeType}</#if><br>
							                   <#if routeDetailGroup.localTimeFlag?? && routeDetailGroup.localTimeFlag='Y'>当地时间<#else></#if>
							               </td>
							               <td style="padding:0 0 10px 0;">
							                   <!-- 灰色字体 加 color:#999;-->
							                   <p style="color:#999; margin-bottom:10px;">
							                   		<#if recommend.recommendName?exists>${recommend.recommendName}&nbsp;&nbsp;&nbsp;&nbsp;</#if>
                                                    <#--<#if recommend.referencePrice?exists>参考价格${recommend.referencePrice/100}元&nbsp;&nbsp;&nbsp;&nbsp;</#if>-->
                                                     <#if (recommend.referencePrice?exists) && (recommend.referencePrice gt 0)>
                                                    	参考价格
                                                    	<#if routeDetailFormat?exists>
					                   						<#assign referencePriceFormat = routeDetailFormat.getCnPriceFormat(recommend.referencePrice, recommend.currency, 'RECOMMEND')!'' />
					                   					</#if>
		                                            	<#if referencePriceFormat!''>${referencePriceFormat!''}<#else>${recommend.referencePrice/100}元</#if>
		                                            	&nbsp;&nbsp;&nbsp;&nbsp;
                                                    </#if>
                                                  <#if (recommend.visitTime != "") && (recommend.visitTime gt 0)>
                                                    	项目时间：${recommend.visitTime!''}分钟
                                                    </#if>
							                   </p>
							                   <p>地址：${recommend.address!''}</p>
							                   <!--推荐项目说明-->
							                   <p style="margin-bottom:10px;">
							                   		<#if recommend.recommendDesc??>
								                   		<@func.addSpace recommend.recommendDesc 42/><br/>
							                   		</#if>
							                   </p>
							               </td>
							            </tr>	
									</#list>
									<!--推荐项目结束-->
									
								</#if>
							</#if>
							<!--如果模块类型存在，则遍历此类型列表并展示相关信息 结束-->
						</#list>
						<!--按sortValue升序遍历行程明细组列表 结束--> 
				    </#if>
				    <!--如果行程明细里存在行程明细组列表，则遍历组列表并展示 结束-->
                </tbody>
            </table>
        </td>
    </tr>
    <!--第N天内容结束-->
  </#list>
<!--按nDay升序遍历行程明细列表 结束-->
</#macro>  
                            