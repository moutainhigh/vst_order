<#if changeAdditionList?? && changeAdditionList?size &gt; 0>
							<#assign changeAdditionIdIndex=0 />
		        				<#list changeAdditionList as pb>
		        					<#assign goods=pb.suppGoodsList[0] />
		        						<table class="tab_nav ${goods.productId}" width="100%">
			        					<tr class="table_nav " parentProductId="${goods.productId}" adult="" child=""  suppGoodsId="${goods.suppGoodsId}">
			        						<td width="50%"><a class="pro_tit" href="javascript:;" desc="${pb.branchName}" prodBranchId="${pb.productBranchId}">${pb.branchName}</a>
			        							<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
	        									<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].routeRelation" value="ADDITION" autocomplete="off"/>
	        									<input class="${goods.productId}visitTime" type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].visitTime" value="${goods.suppGoodsBaseTimePrice.specDate?string('yyyy-MM-dd')}" autocomplete="off"/>
	        								</a></td>
			        						<td width="20%" class="orange"
			        						<#if pb.selectPriceMap??>
			        						<#assign selectPriceMap=pb.selectPriceMap />
			        						<#list selectPriceMap?keys as key>
			        						${key}="${selectPriceMap[key]}"
			        						</#list>
			        						</#if>
			        						>单价:￥${goods.suppGoodsBaseTimePrice.auditPriceYuan}元</td>
			        						<#if isLvmamaProduct??&&isLvmamaProduct >
			        							<td width="20%">
			        							<#if goods.relationType??&&goods.relationType='AMOUNT'>
			        								<input auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 numText" readOnly name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />等量
			        							<#else>
			        								<#if goods.relationType??&&goods.relationType='OPTION'>
			        								<input auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 numText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />必选
			        								<#else>
			        								<input auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 numText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />可选
			        								</#if>
			        							</#if>	        							
			        						</td>
			        						<#else>
			        							<td width="20%">
			        							<#if goods.parentGoodsRelation?? && goods.parentGoodsRelation.relationType='AMOUNT'>
			        								<input auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 numText" readOnly name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />等量
			        							<#else>
			        								<#if goods.parentGoodsRelation?? && goods.parentGoodsRelation.relationType='OPTION'>
			        								<input auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 numText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />必选
			        								<#else>
			        								<input auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 numText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />可选
			        								</#if>
			        							</#if>	        							
			        						</td>
			        						</#if>
			        						
			        						<td width="10%" class="orange">总价:￥${goods.dailyLowestPriceYuan}元</td>
			        					</tr>
			        					<tr>
										 <td colspan="3">
										 <p class="descript">规格描述：<textarea class="textarea"></textarea></p>
										 </td>
										</tr>
			        					</table>
			        				<#assign changeAdditionIdIndex=changeAdditionIdIndex+1 /> 
			        				<#assign productItemIdIndex=productItemIdIndex+1 />  	        					        				       				
								</#list>
</#if>     
 <script>
 	 productItemIdIndex='${productItemIdIndex}';
 </script>   