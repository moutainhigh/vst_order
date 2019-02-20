<#import "/base/order_line_func.ftl" as line_func/>
<#import "/order/orderProductQuery/line/trafficFlightInfo.ftl" as trafficFlightInfo>
<#import "/order/orderProductQuery/line/trafficTrainInfo.ftl" as trafficTrainInfo>
<#import "/order/orderProductQuery/line/trafficBusInfo.ftl" as trafficBusInfo>
 <#if moreProductList?? && moreProductList?size &gt; 0>
	<#if isFlight == 'true'> 
		<div class="operate">
			<#if transportType=='TOBACK'>
		    	<a id="wftjTransport" class="btn btn_cc1 w8" style=" margin:0;padding:5px 0;">往返推荐 </a><a id="zyzhTransport" class="btn w8" style="margin:0;padding:5px 0;">自由组合</a>
			<#else>
		    	<a id="zyzhTransport" class="btn btn_cc1 w8" style="margin:0;padding:5px 0;">自由组合</a>
			</#if>
		</div>
	</#if>
	
<div class="hotel_tab">
	<div class="table_t">大交通</div>   					
	<#assign changeProdPackage=moreProductList[0] />
	<p class="listName">
		<#if changeProdPackage.prodPackageGroupTransport??&&changeProdPackage.prodPackageGroupTransport.toStartDate??>去程(${changeProdPackage.prodPackageGroupTransport.toStartPointDistrict.districtName}-${changeProdPackage.prodPackageGroupTransport.toDestinationDistrict.districtName})${changeProdPackage.prodPackageGroupTransport.toStartDate}日</#if>
		<#if changeProdPackage.prodPackageGroupTransport??&&changeProdPackage.prodPackageGroupTransport.backStartDate??>返程(${changeProdPackage.prodPackageGroupTransport.backStartPointDistrict.districtName}-${changeProdPackage.prodPackageGroupTransport.backDestinationDistrict.districtName})${changeProdPackage.prodPackageGroupTransport.backStartDate}日</#if>
	</p> 
	<#if changeProdPackage??&&changeProdPackage.prodPackageDetails?size &gt; 0>
		<#list changeProdPackage.prodPackageDetails as detail>
			<div id="trafficInfoDiv${detail.groupId}" style="display:none">
				<#if detail.prodProduct.prodTrafficVO??&&detail.prodProduct.prodTrafficVO.prodTraffic.toType??
					&&detail.prodProduct.prodTrafficVO.prodTraffic.toType=="FLIGHT">
	 				<@trafficFlightInfo.trafficFlightInfo detail changeProdPackage.prodPackageGroupTransport.toStartDate changeProdPackage.prodPackageGroupTransport.backStartDate />
				</#if>	
				<#if detail.prodProduct.prodTrafficVO??&&detail.prodProduct.prodTrafficVO.prodTraffic.toType??
					&&detail.prodProduct.prodTrafficVO.prodTraffic.toType=="TRAIN">
					<@trafficTrainInfo.trafficTrainInfo detail changeProdPackage.prodPackageGroupTransport.toStartDate changeProdPackage.prodPackageGroupTransport.backStartDate />
				</#if>
				<#if detail.prodProduct.prodTrafficVO??&&detail.prodProduct.prodTrafficVO.prodTraffic.toType??
					&&detail.prodProduct.prodTrafficVO.prodTraffic.toType=="BUS">
					<@trafficBusInfo.trafficBusInfo detail changeProdPackage.prodPackageGroupTransport.toStartDate changeProdPackage.prodPackageGroupTransport.backStartDate />
				</#if>
				<#if detail.prodProduct.prodTrafficVO??&&detail.prodProduct.prodTrafficVO.prodTraffic.toType??
					&&detail.prodProduct.prodTrafficVO.prodTraffic.toType=="SHIP">
					<@trafficBusInfo.trafficBusInfo detail changeProdPackage.prodPackageGroupTransport.toStartDate changeProdPackage.prodPackageGroupTransport.backStartDate />
				</#if>
			</div>
		
			<#if detail.prodProductBranch?? && detail.prodProductBranch.recommendSuppGoodsList??&& detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0 >
			<table width="100%" class="updateChangeTable">
			   <tbody>
				<tr>
				 <td>	
				 	<#assign prodProductBranch=detail.prodProductBranch />
				 	  <#if detail.prodProductBranch.recommendSuppGoodsList?? &&detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0>
				 	    <#assign goods=detail.prodProductBranch.recommendSuppGoodsList[0] />
				 	    
				 	    <#assign prodTrafficGroup=detail.prodProduct.prodTrafficVO.prodTrafficGroupList[0] />
				 	    <#if prodTrafficGroup??&&prodTrafficGroup.prodTrafficFlightList?size == 2>
				 	    	<#assign toFlight = prodTrafficGroup.prodTrafficFlightList[0].bizFlight />
				 	    	<#assign backFlight = prodTrafficGroup.prodTrafficFlightList[1].bizFlight />
				 	    <#elseif prodTrafficGroup??&&prodTrafficGroup.prodTrafficFlightList?size == 3 || prodTrafficGroup??&&prodTrafficGroup.prodTrafficFlightList?size == 4>
				 	    	<#assign toFlight = prodTrafficGroup.prodTrafficFlightList[0].bizFlight />
				 	    	<#assign backFlight = prodTrafficGroup.prodTrafficFlightList[2].bizFlight />
				 	    </#if>
				 	    
				 		<table class="tab_nav" width="100%">
		        			<tr id="${prodProductBranch.productBranchId}_${detail.groupId}" groupId="${detail.groupId}" productBranchId="${prodProductBranch.productBranchId}" adult="${adultQuantity}" child="${childQuantity}"  class="table_nav" >
		        				<td width="50%">
		        					<a class="pro_tit" href="javascript:;" desc="${prodProductBranch.branchName}" prodBranchId="${prodProductBranch.productBranchId}"
										onclick="openProduct(${detail.prodProduct.productId!''},
											${detail.prodProduct.bizCategory.categoryId!''},
										'${detail.prodProduct.bizCategory.categoryName!''}')">${detail.prodProduct.productName}</a>
		        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
		        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].detailId" value="${detail.detailId}" autocomplete="off"/>
		        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
		        					<input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
		        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].visitTime" value="<#if changeProdPackage.prodPackageGroupTransport.toStartDate??>${changeProdPackage.prodPackageGroupTransport.toStartDate}<#else><#if changeProdPackage.prodPackageGroupTransport.backStartDate??>${changeProdPackage.prodPackageGroupTransport.backStartDate}</#if></#if>" autocomplete="off"/>
                                    <!--增加机票短信模板相关的信息(往返)--起-->
									<#assign prodTrafficGroup=detail.prodProduct.prodTrafficVO.prodTrafficGroupList[0] />
									<#if prodTrafficGroup??&&prodTrafficGroup.prodTrafficFlightList?size &gt; 0>
                                        <!--设定交通的去程和返程信息-->
                                        <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].toDate" value="${changeProdPackage.prodPackageGroupTransport.toStartDate}"/>
                                        <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].backDate" value="${changeProdPackage.prodPackageGroupTransport.backStartDate}"/>
										<#list prodTrafficGroup.prodTrafficFlightList as prodTrafficFlight>
											<#assign bizFlight=prodTrafficFlight.bizFlight/>
										<#--1代表去程，2代表返程，刚好可以用索引来表示-->
											<#assign roundTripFlightType=prodTrafficFlight_index+1/>
                                            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].companyName" value="${bizFlight.airlineString}"/>
                                            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].flightNo" value="${bizFlight.flightNo}"/>
                                            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].planeCode" value="${bizFlight.airplaneString}"/>
                                            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].flightType" value="${roundTripFlightType}"/>
                                            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].flightTime" value="${bizFlight.flightTime}"/>
                                            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].fromAirPort" value="${bizFlight.startAirportString}"/>
                                            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].toAirPort" value="${bizFlight.arriveAirportString}"/>
                                            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].fromCityName" value="${bizFlight.startDistrictString}"/>
                                            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].toCityName" value="${bizFlight.arriveDistrictString}"/>
                                            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].startTerminal" value="${bizFlight.startTerminal}"/>
                                            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].arriveTerminal" value="${bizFlight.arriveTerminal}"/>
                                            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].goTimeStr" value="${bizFlight.startTime}"/>
                                            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].arriveTimeStr" value="${bizFlight.arriveTime}"/>
										</#list>
									</#if>
                                    <!--增加机票短信模板相关的信息(非对接往返)--止-->
		        				</td>
		        				<td width="20%">
		        					成人数<input readOnly goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 adultNumText" name="productMap[${productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitAdultQuantity}" required=true number=true /><br/>
		        					儿童数<input readOnly goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 childNumText" name="productMap[${productId}].itemList[${productItemIdIndex}].childQuantity" style="text-align:center" value="${goods.fitChildQuantity}" required=true number=true />
		        					<input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
		        				</td>
		        				<td width="10%" class="orange" totalAmount="${prodProductBranch.dailyLowestPrice}" totalAmountYuan="${prodProductBranch.dailyLowestPriceYuan}">
		        					<#if prodProductBranch.productBranchId==currentProductBranchId>
		        						--
		        					<#else>
		        					差价:￥<@line_func.showSpreadPrice sourceTotalAmount prodProductBranch/>元
		        				</#if>
		        				</td>
		        				<td>
		        					<div class="operate" style="text-align:center;">
				        				<#if prodProductBranch.productBranchId==currentProductBranchId>
				        					<span class="btn w8 " style="margin:0;padding:5px 0;">已选择</span>
				        				<#else>
				        					<a class="btn btn_cc1 w8 " style="margin:0;padding:5px 0;" 
				        									groupId="${detail.groupId}" 
				        									productBranchId="${prodProductBranch.productBranchId}" 
				        									adult="${adultQuantity}" child="${childQuantity}"  
				        									packageProductId="${detail.prodProduct.productId}" 
				        									categoryId = "${detail.prodProduct.bizCategory.categoryId!''}"
				        									<#if toFlight??>
					        									to_startTime = "${toFlight.startTime}"
					        									to_startAirportString ="${toFlight.startAirportString}"
					        									to_airlineString="${toFlight.airlineString}"
					        									to_arriveTime="${toFlight.arriveTime}"
					        									to_arriveAirportString="${toFlight.arriveAirportString}"
					        									to_flightNo="${toFlight.flightNo}"
				        									</#if>
				        									<#if backFlight??>
					        									back_startTime = "${backFlight.startTime}"
					        									back_startAirportString="${backFlight.startAirportString}"
					        									back_airlineString="${backFlight.airlineString}"
					        									back_arriveTime="${backFlight.arriveTime}"
					        									back_arriveAirportString="${backFlight.arriveAirportString}"
					        									back_flightNo="${backFlight.flightNo}"
					        								</#if>
				        					name="xztcBtn" >选择</a>
				        				</#if>
			        				</div>
		        				</td>
		        			</tr>
		        			<tr style="display:none">
								<td colspan="4">
								<p class="descript">规格描述：<textarea class="textarea"></textarea></p>
								</td>
							</tr>
        				</table> 
        			  
		        	</#if>			        			
        		 </td>
			   </tr>	  					
			  </tbody>
			</table>
			</#if>
		</#list>        				
	</#if>
</div> 
<#assign productItemIdIndex = productItemIdIndex+1 />
 <script>
 	 productItemIdIndex='${productItemIdIndex}';
 	 var transType = '${transportType}';
 	 $(function(){
 	 	$("#wftjTransport").click(function(){
 	 		$(".dialog-close").click();
 	 		if(transType == 'TOBACK'){
				openChangeDivTransport($('.moreCategoryLineTransport'), "linetransport");
 	 		}else{
 	 			openChangeDivTransport($('.apiChangeTransprot'), "linetransport");
 	 		}
 	 	});
 	 	
 	 	$("#zyzhTransport").click(function(){
 	 		$(".dialog-close").click();
 	 		if(transType == 'TOBACK'){
				openChangeDivTransport($('.moreCategoryLineTransport'), "apilinetransport");
 	 		}else{
 	 			openChangeDivTransport($('.apiChangeTransprot'), "apilinetransport");
 	 		}
 	 	});
 	 });
 </script>
 </#if> 