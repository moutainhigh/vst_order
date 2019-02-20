  <#if changeProdPackageList?? && changeProdPackageList?size &gt; 0>
  <div class="hotel_ebk_wrap">
  	<div class="hotel_tab_box">
  		<div class="hotel_tab">
  			<div class="table_t">更换酒店</div>
  				<#assign updateChangeIdIndex=0 />   
  					<#list changeProdPackageList as changeProdPackage>
  						<#if changeProdPackage??&&changeProdPackage.prodPackageDetails?size &gt; 0>
	  						<#if changeProdPackage.prodPackageDetails?size gt 1>
	  							 <p class="listName"><a href="#" class="fr moreCategory">更多酒店</a>${changeProdPackage.startDay}日到${changeProdPackage.endDay}日行程</p>
	  						<#else>
	  							<p class="listName">${changeProdPackage.startDay}日到${changeProdPackage.endDay}日行程</p> 
	  						</#if>
              			 	<#assign detail=changeProdPackage.prodPackageDetails[0] />
		  					<#if detail.prodProductBranch?? && detail.prodProductBranch.suppGoodsList??&& detail.prodProductBranch.suppGoodsList?size &gt; 0 >
		  					<table width="100%" class="updateChangeTable">
			  				   <tbody>
			  					<tr>
        						 <td>	
				  					<#list detail.prodProductBranch.suppGoodsList as goods>
				  						<table class="tab_nav" width="100%">
						        			<tr class="table_nav" adult="" child="" suppGoodsId="${goods.suppGoodsId}">
						        				<td width="50%"><a class="pro_tit" href="javascript:;" desc="${detail.prodProductBranch.branchName}" prodBranchId="${detail.prodProductBranch.productBranchId}">${detail.prodProductBranch.branchName}</a>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].detailId" value="${detail.detailId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].visitTime" value="<#if changeProdPackage.prodPackageGroupHotel.dateList??>${changeProdPackage.prodPackageGroupHotel.dateList[0]}</#if>" autocomplete="off"/>
						        				</td>
						        				<td width="20%" class="orange">成人价:￥${goods.suppGoodsBaseTimePrice.auditPriceYuan}元/儿童价:￥${goods.suppGoodsBaseTimePrice.childPriceYuan}元
						        				</td>
						        				<td width="20%">
						        					成人数<input readOnly goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 adultNumText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitAdultQuantity}" required=true number=true /><br/>
						        					儿童数<input readOnly goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 childNumText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].childQuantity" style="text-align:center" value="${goods.fitChildQuantity}" required=true number=true />
						        					<input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
						        				</td>
						        				<td width="10%" class="orange">总价:￥${goods.dailyLowestPriceYuan}元</td>
						        			</tr>
						        			<tr>
												<td colspan="3">
												<p class="descript">规格描述：<textarea class="textarea"></textarea></p>
												</td>
											</tr>
				        				</table>
					        			<#assign updateChangeIdIndex=updateChangeIdIndex+1 />  
					        			<#assign productItemIdIndex=productItemIdIndex+1 />  
					        		</#list>				        			
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