 <#if hotelCombPackageList?? && hotelCombPackageList?size &gt; 0>
  <div class="hotel_ebk_wrap">
  	<div class="hotel_tab_box">
  		<div class="hotel_tab">
  			<div class="table_t">酒店套餐</div>
  					<#list hotelCombPackageList as changeProdPackage>
  						<#if changeProdPackage??&&changeProdPackage.prodPackageDetails?size &gt; 0>
  							<#assign detail=changeProdPackage.prodPackageDetails[0] />
	  						<#if changeProdPackage.prodPackageDetails?size gt 1>
	  							 <p class="listName"><a href="javascript:void(0);" class="fr moreCategoryLineHotelCom" adultNum="${adultNum}" childNum="${childNum}" outProductId="${productId}" packageGroupId="${detail.groupId}" packageProductId="${detail.prodProduct.productId}" packageProductBranchId="${detail.prodProductBranch.productBranchId}">更多酒店套餐</a>${changeProdPackage.startDay}日到${changeProdPackage.endDay}日行程</p>
	  						<#else>
	  							 <p class="listName">${changeProdPackage.startDay}日到${changeProdPackage.endDay}日行程</p> 
	  						</#if>              			 	
		  					<#if detail.prodProductBranch?? && detail.prodProductBranch.recommendSuppGoodsList??&& detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0 >
		  					<table width="100%" class="updateChangeTable">
			  				   <tbody>
			  					<tr>
        						 <td>
        						 	<#assign prodProductBranch=detail.prodProductBranch />
        						 		<#if detail.prodProductBranch.recommendSuppGoodsList?? &&detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0>
        						 	    <#assign goods=detail.prodProductBranch.recommendSuppGoodsList[0] />
        						 		<table class="tab_nav" width="100%">
        						 		    <tr id="firstLineHotelComShow${detail.groupId}_productNameLink">
        						 		        <td colspan="3">
        						 		           <a class="pro_tit" href="http://dujia.lvmama.com/package/${detail.prodProduct.productId}" target="_blank">${detail.prodProduct.productName}</a>
        						 		        </td>
        						 		    </tr>
        						 		    
					        						<#assign fitQuantity=goods.fitQuantity />
						        					<#if packageTourProductVo.productAddtional?? && isHotel && prodProduct.bizCategoryId == 18 && prodProduct.subCategoryId == 182 && prodProduct.packageType == "LVMAMA" && (prodProduct.productType == "INNERLINE" || prodProduct.productType == "INNERSHORTLINE" || prodProduct.productType == "INNERLONGLINE" || prodProduct.productType == "INNER_BORDER_LINE")>
							        					<#assign baseQuantity=baseChildNum + baseAdultNum />
							        					<#assign totalQuantity=adultNum + childNum />
							        					<#if (totalQuantity/baseQuantity) &gt; (totalQuantity%baseQuantity) >
							        						<#assign mixedQuantity=(totalQuantity%baseQuantity)+1 />
							        					<#else>
							        						<#assign mixedQuantity=totalQuantity%baseQuantity />
							        					</#if>
														<#assign fitQuantity=goods.fitQuantity />
							        					<#if fitQuantity &gt; mixedQuantity >
							        						<#assign finalQuantity=fitQuantity />
							        					<#else>
							        						<#assign finalQuantity=mixedQuantity />
							        					</#if>
							        					<#if (finalQuantity*2) &gt; packageTourProductVo.productAddtional.adultMaxQuantity >
							        						<#assign maxHotelCombCopies=packageTourProductVo.productAddtional.adultMaxQuantity />
							        					<#else>
							        						<#assign maxHotelCombCopies=(finalQuantity*2) />
							        					</#if>
														<#assign finalPrice=(goods.suppGoodsBaseTimePrice.auditPriceYuan?number)*(finalQuantity?number) />
											<tr id="firstLineHotelComShow${detail.groupId}" isroute="true" groupId="${detail.groupId}" productId="${productId}" totalAmount="${finalPrice*100.0}" parentProductId="${prodProductBranch.productId}" productBranchId="${prodProductBranch.productBranchId}" goodsId="${goods.suppGoodsId}" adult="${adultNum}" child="${childNum}" class="table_nav" >
						        				<td width="50%"><a class="pro_tit" href="javascript:void(0);" desc="${prodProductBranch.branchName}" prodBranchId="${prodProductBranch.productBranchId}">${detail.prodProduct.productName}(${prodProductBranch.branchName})</a>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].detailId" value="${detail.detailId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
                                                    <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].productCategoryId" value="${packageTourProductVo.bizCategory.categoryId}" autocomplete="off"/>
                                                    <input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].visitTime" value="<#if changeProdPackage.prodPackageGroupLine.dateList??>${changeProdPackage.prodPackageGroupLine.dateList[0]}</#if>" autocomplete="off"/>

						        				</td>
						        				<td width="20%">
														份数<select isHotel="true" goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="0" type="text" class="w5 hotelAdultNumText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity"  required=true>
												           <#list finalQuantity..maxHotelCombCopies as counter>
												              <option value="${counter}">${counter}</option>
												           </#list>
														</select> 
						        				</td>
						        				<td width="10%" class="orange">总价:￥${finalPrice?string("#.##")}元</td>
						        					<#else>
				        					<tr id="firstLineHotelComShow${detail.groupId}" groupId="${detail.groupId}" productId="${productId}" totalAmount="${prodProductBranch.dailyLowestPrice}" parentProductId="${prodProductBranch.productId}" productBranchId="${prodProductBranch.productBranchId}" goodsId="${goods.suppGoodsId}" adult="${adultNum}" child="${childNum}" class="table_nav" >
						        				<td width="50%"><a class="pro_tit" href="javascript:void(0);" desc="${prodProductBranch.branchName}" prodBranchId="${prodProductBranch.productBranchId}">${detail.prodProduct.productName}(${prodProductBranch.branchName})</a>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].detailId" value="${detail.detailId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
                                                    <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].productCategoryId" value="${packageTourProductVo.bizCategory.categoryId}" autocomplete="off"/>
                                                    <input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].visitTime" value="<#if changeProdPackage.prodPackageGroupLine.dateList??>${changeProdPackage.prodPackageGroupLine.dateList[0]}</#if>" autocomplete="off"/>

						        				</td>
						        				<td width="20%">
						        					份数<input readOnly goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="0" type="text" class="w5 hotelAdultNumText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
						        				</td>
						        				<td width="10%" class="orange">总价:￥${prodProductBranch.dailyLowestPriceYuan}元</td>
						        					</#if>
						        			</tr>
						        			<tr>
												<td colspan="3">
												<p class="descript">规格描述：<textarea class="textarea"></textarea></p>
												</td>
											</tr>
				        			</table> 
				        			<#assign productItemIdIndex=productItemIdIndex+1 />
				        			<script>
										productItemIdIndex='${productItemIdIndex}';	       
			    					</script>  
						        	</#if>     			
				        		 </td>
        					   </tr>	  					
	  						  </tbody>
        					</table>
        					</#if>
  					</#if>   						
  				</#list>
  		</div>
  	</div>
  </div>
 </#if> 