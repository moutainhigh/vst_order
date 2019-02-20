<#import "/base/order_line_func.ftl" as line_func/>
 <#if moreProductList?? && moreProductList?size &gt; 0>
  <div class="hotel_ebk_wrap">
  	<div class="hotel_tab_box">
  		<div class="hotel_tab">
  			<div class="table_t">酒店套餐</div>   					
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
        						 		<#list detail.prodProductBranch.recommendSuppGoodsList as goods>
        						 		<table class="tab_nav" width="100%">
						        			<tr id="${prodProductBranch.productBranchId}_${detail.groupId}" groupId="${detail.groupId}" productId="${productId}" parentProductId="${prodProductBranch.productId}" productBranchId="${prodProductBranch.productBranchId}" goodsId="${goods.suppGoodsId}" adult="${adultQuantity}" child="${childQuantity}"  class="table_nav" >
						        				<td width="50%"><a class="pro_tit" href="javascript:;" desc="${prodProductBranch.branchName}" prodBranchId="${prodProductBranch.productBranchId}">${detail.prodProduct.productName}(${prodProductBranch.branchName})</a>
						        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].detailId" value="${detail.detailId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
                                                    <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].productCategoryId" value="${detail.prodProduct.bizCategory.categoryId}" autocomplete="off"/>
                                                    <input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
						        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].visitTime" value="<#if changeProdPackage.prodPackageGroupLine.dateList??>${changeProdPackage.prodPackageGroupLine.dateList[0]}</#if>" autocomplete="off"/>
						        				</td>
						        				<td width="20%">
						        				<#if productAddtional?? && isHotel && prodProduct.bizCategoryId == 18 && prodProduct.subCategoryId == 182 && prodProduct.packageType == "LVMAMA" && (prodProduct.productType == "INNERLINE" || prodProduct.productType == "INNERSHORTLINE" || prodProduct.productType == "INNERLONGLINE" || prodProduct.productType == "INNER_BORDER_LINE")>
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
							        					<#if (finalQuantity*2) &gt; productAddtional.adultMaxQuantity >
							        						<#assign maxHotelCombCopies=productAddtional.adultMaxQuantity />
							        					<#else>
							        						<#assign maxHotelCombCopies=(finalQuantity*2) />
							        					</#if>
														<#assign finalPrice=(goods.suppGoodsBaseTimePrice.auditPriceYuan?number)*(finalQuantity?number) />
														份数<select  isHotel="true" goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="0" type="text" class="w5 hotelAdultNumText" name="productMap[${productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitQuantity}" required=true>
												           <#list finalQuantity..maxHotelCombCopies as counter>
												              <option value="${counter}">${counter}</option>
												           </#list>
														</select> 
							        						<#assign finalPrice_=finalPrice*100.0 />
														<td width="10%" class="orange" totalAmount="${finalPrice_}" totalAmountYuan="${finalPrice?string("#.##")}">
							        					<#if prodProductBranch.productBranchId==currentProductBranchId>
							        						--
							        					<#else>
							        					差价:￥<@line_func.showSpreadPrice2 sourceTotalAmount finalPrice_/>元
							        					</#if>
							        					</td>
						        					<#else>
						        					份数<input readOnly goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="0" type="text" class="w5 hotelAdultNumText" name="productMap[${productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
						        					<td width="10%" class="orange" totalAmount="${prodProductBranch.dailyLowestPrice}" totalAmountYuan="${prodProductBranch.dailyLowestPriceYuan}">
							        					<#if prodProductBranch.productBranchId==currentProductBranchId>
							        						--
							        					<#else>
						        						差价:￥<@line_func.showSpreadPrice sourceTotalAmount prodProductBranch/>元
							        					</#if>
							        				</td>
						        					</#if>
						        				</td>
						        				<td><div class="operate" style="text-align:center;">
						        				<#if prodProductBranch.productBranchId==currentProductBranchId>
						        					<span class="btn w8 " style="margin:0;padding:5px 0;">已选择</span>
						        				<#else>
						        					<a class="btn btn_cc1 w8 " style="margin:0;padding:5px 0;" groupId="${detail.groupId}" productId="${productId}" packageProductId="${detail.prodProduct.productId}" parentProductId="${prodProductBranch.productId}" productBranchId="${prodProductBranch.productBranchId}" goodsId="${goods.suppGoodsId}" adult="${adultQuantity}" child="${childQuantity}" visitTime="<#if changeProdPackage.prodPackageGroupLine.dateList??>${changeProdPackage.prodPackageGroupLine.dateList[0]}</#if>" name="xztcBtn" >选择</a>
						        				</#if>
						        				</div>
						        					<div class="productNameLink" style="display:none;">
								        				<a class="pro_tit" href="http://dujia.lvmama.com/package/${detail.prodProduct.productId}" target="_blank">${detail.prodProduct.productName}</a>
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
						        	</#list>			        			
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