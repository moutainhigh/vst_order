 <#if freedomPackageList?? && freedomPackageList?size &gt; 0>
  <div class="hotel_ebk_wrap">
  	<div class="hotel_tab_box">
  		<div class="hotel_tab">
  			<div class="table_t">自由行</div>
  					<#list freedomPackageList as changeProdPackage>
  						<#if changeProdPackage??&&changeProdPackage.prodPackageDetails?size &gt; 0>
  							<#assign detail=changeProdPackage.prodPackageDetails[0] />
	  						<#if changeProdPackage.prodPackageDetails?size gt 1>
	  							 <p class="listName"><a href="#" class="fr moreCategoryLineSelf" adultNum="${adultNum}" childNum="${childNum}" outProductId="${productId}" packageGroupId="${detail.groupId}" packageProductId="${detail.prodProduct.productId}" packageProductBranchId="${detail.prodProductBranch.productBranchId}">更多自由行</a>${changeProdPackage.startDay}日到${changeProdPackage.endDay}日行程</p>
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
        						 		    <tr>
        						 		       <td colspan="3">
        						 		           <a class="pro_tit" href="http://dujia.lvmama.com/freetour/${detail.prodProduct.productId}" target="_blank">${detail.prodProduct.productName}</a>
        						 		       </td>
        						 		    </tr>
						        			<tr id="firstLineSelfShow${detail.groupId}" groupId="${detail.groupId}" productId="${productId}" parentProductId="${prodProductBranch.productId}" productBranchId="${prodProductBranch.productBranchId}" goodsId="${goods.suppGoodsId}" adult="${adultNum}" child="${childNum}" class="table_nav" >
						        				<td width="50%"><a class="pro_tit" href="javascript:;" desc="${prodProductBranch.branchName}" prodBranchId="${prodProductBranch.productBranchId}">${detail.prodProduct.productName}(${prodProductBranch.branchName})</a>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].detailId" value="${detail.detailId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
						        					<input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].visitTime" value="<#if changeProdPackage.prodPackageGroupLine.dateList??>${changeProdPackage.prodPackageGroupLine.dateList[0]}</#if>" autocomplete="off"/>
						        				</td>
						        				<td width="20%" class="orange">成人价:￥${goods.suppGoodsBaseTimePrice.auditPriceYuan}元/儿童价:￥${goods.suppGoodsBaseTimePrice.childPriceYuan}元
						        				</td>
						        				<td width="20%">
						        					成人数<input readOnly goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 adultNumText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitAdultQuantity}" required=true number=true />
						        					儿童数<input readOnly goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 childNumText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].childQuantity" style="text-align:center" value="${goods.fitChildQuantity}" required=true number=true />
						        					<input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
						        				</td>
						        				<td width="10%" class="orange">总价:￥${prodProductBranch.dailyLowestPriceYuan}元</td>
						        			</tr>
						        			<tr>
												<td colspan="4">
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