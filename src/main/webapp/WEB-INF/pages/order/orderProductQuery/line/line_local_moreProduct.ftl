<#import "/base/order_line_func.ftl" as line_func/>
 <script>
 	$(".moreLineSelectDate").bind("change",function(){
		changeMoreSelectDate($(this));
	});
</script>
 <#if moreProductList?? && moreProductList?size &gt; 0>
  <div class="hotel_ebk_wrap">
  	<div class="hotel_tab_box">
  		<div class="hotel_tab">
  			<div class="table_t">当地游</div>   					
  					<#assign changeProdPackage=moreProductList[0] />
  						<p class="listName">${changeProdPackage.startDay}日到${changeProdPackage.endDay}日行程</p>
  						<#if changeProdPackage??&&changeProdPackage.prodPackageDetails?size &gt; 0>
  							<#list changeProdPackage.prodPackageDetails as detail>
		  					<#if detail.prodProductBranch?? && detail.prodProductBranch.recommendSuppGoodsList??&& detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0 >
		  					<table width="100%" class="updateChangeTable">
			  				   <tbody>
			  					<tr>
        						 <td>	
        						 	<#assign prodProductBranch=detail.prodProductBranch />
        						 	 <#if detail.prodProductBranch.recommendSuppGoodsList??&&detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0>
        						 	   <#assign goods=detail.prodProductBranch.recommendSuppGoodsList[0] />
        						 		<table class="tab_nav" width="100%">
						        			<tr id="${prodProductBranch.productBranchId}_${detail.groupId}" groupId="${detail.groupId}" productId="${productId}" parentProductId="${prodProductBranch.productId}" productBranchId="${prodProductBranch.productBranchId}" goodsId="${goods.suppGoodsId}" adult="${adultQuantity}" child="${childQuantity}"  class="table_nav" >
						        				<td width="50%"><a class="pro_tit" href="javascript:;" desc="${prodProductBranch.branchName}" prodBranchId="${prodProductBranch.productBranchId}">${detail.prodProduct.productName}(${prodProductBranch.branchName})</a>
						        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].detailId" value="${detail.detailId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
						        					<input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
						        					<input type="hidden"  goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" class="w5 adultNumText" name="productMap[${productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitAdultQuantity}" required=true number=true />
						        					<input type="hidden"  goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" class="w5 childNumText" name="productMap[${productId}].itemList[${productItemIdIndex}].childQuantity" style="text-align:center" value="${goods.fitChildQuantity}" required=true number=true />
						        					<input class="itemLineSelectDate" type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].visitTime" value="${prodProductBranch.selectDateList[0]}" autocomplete="off"/>
						        				</td>
						        				<td width="20%" class="orange">
						        					<select class="adultPriceMapSelect" style="display:none;">
						        						<#if prodProductBranch.adultPriceMap?? && prodProductBranch.adultPriceMap?size &gt; 0>
    	 													<#list prodProductBranch.adultPriceMap?keys as adultDate>
    	 														<option value="${prodProductBranch.adultPriceMap[adultDate]}">${adultDate}</option>
    	 													</#list>
    	 												</#if>
						        					</select>
						        					<select class="childPriceMapSelect" style="display:none;">
						        						<#if prodProductBranch.childPriceMap?? && prodProductBranch.childPriceMap?size &gt; 0>
    	 													<#list prodProductBranch.childPriceMap?keys as childDate>
    	 														<option value="${prodProductBranch.childPriceMap[childDate]}">${childDate}</option>
    	 													</#list>
    	 												</#if>
						        					</select>
						        					<span class="adultChildPriceSpan">成人价:￥${goods.suppGoodsBaseTimePrice.auditPriceYuan}元/儿童价:￥${goods.suppGoodsBaseTimePrice.childPriceYuan}元</span>
						        				</td>
						        				<td width="20%">
						        					<#if prodProductBranch.productBranchId==currentProductBranchId>
						        						<select class="parentSelectedDateList" style="display:none;">
							        						<#if changeProdPackage.prodPackageGroupLine?? && changeProdPackage.prodPackageGroupLine.dateList??>
								        					<#assign index=0 />
								        					<#list changeProdPackage.prodPackageGroupLine.dateList as date>
								        						<#if selectedDate == date>
								        							<option value="${prodProductBranch.selectPriceMap[date]}" parentProductId="${goods.productId}" selected="selected">${date}</option>
																<#else>
																	<option value="${prodProductBranch.selectPriceMap[date]}" parentProductId="${goods.productId}">${date}</option>
																</#if>
																<#assign index=index+1 />	
															</#list>
															<#else>														
								        						<option value="${prodProductBranch.selectPriceMap[specDate]}" parentProductId="${goods.productId}">${specDate}</option>
								        					</#if>
							        					</select>
						        					</#if>
						        					<select class="moreLineSelectDate">
						        						<#if prodProductBranch?? && prodProductBranch.selectDateList??>
							        					<#assign index=0 />
							        					<#list prodProductBranch.selectDateList as date>
							        						<#if selectedDate == date>
							        							<option value="${prodProductBranch.selectPriceMap[date]}" parentProductId="${goods.productId}" selected="selected">${date}</option>
															<#else>
																<option value="${prodProductBranch.selectPriceMap[date]}" parentProductId="${goods.productId}">${date}</option>
															</#if>
															<#assign index=index+1 />	
														</#list>
														<#else>														
							        						<option value="${prodProductBranch.selectPriceMap[specDate]}" parentProductId="${goods.productId}">${specDate}</option>
							        					</#if>
						        					</select>
												</td>
						        				<td width="10%" class="itemLineSelectDatePrice" totalAmount="${prodProductBranch.dailyLowestPrice}" totalAmountYuan="${prodProductBranch.dailyLowestPriceYuan}">
						        					<#if prodProductBranch.productBranchId==currentProductBranchId>
						        						--
						        					<#else>
						        					差价:￥<@line_func.showSpreadPrice sourceTotalAmount prodProductBranch/>元
						        				</#if>
						        				</td>
						        				<td><div class="operate" style="text-align:center;">
						        				<#if prodProductBranch.productBranchId==currentProductBranchId>
						        					<span class="btn w8 " style="margin:0;padding:5px 0;">已选择</span>
						        				<#else>
						        				<a class="btn btn_cc1 w8 " style="margin:0;padding:5px 0;" groupId="${detail.groupId}" detailId="${detail.detailId}" productId="${productId}" packageProductId="${detail.prodProduct.productId}" parentProductId="${prodProductBranch.productId}" productBranchId="${prodProductBranch.productBranchId}" goodsId="${goods.suppGoodsId}" adult="${adultQuantity}" child="${childQuantity}" visitTime="<#if changeProdPackage.prodPackageGroupLine.dateList??>${changeProdPackage.prodPackageGroupLine.dateList[0]}</#if>" name="xztcBtn" >选择</a>
						        				</#if>
						        				</div>
						        				<div class="productNameLink" style="display:none;">
													<a class="pro_tit" href="http://dujia.lvmama.com/local/${detail.prodProduct.productId}" target="_blank">${detail.prodProduct.productName}</a>
					        					</div>
						        				</td>
						        			</tr>
						        			<tr>
												<td colspan="4">
												<p class="descript">规格描述：<textarea class="textarea"></textarea></p>
												</td>
											</tr>
				        				</table> 
				        			 <#assign productItemIdIndex=productItemIdIndex+1 /> 
				        			 </#if> 			        			
				        		 </td>
        					   </tr>	  					
	  						  </tbody>
        					</table>
        				</#if>
        				</#list>        				
  					</#if> 
  		</div>
  	</div>
  </div>
 <script>
 	 productItemIdIndex='${productItemIdIndex}';
 </script>
 </#if> 