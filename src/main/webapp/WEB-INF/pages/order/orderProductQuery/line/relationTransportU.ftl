<#if reTransportBranchList?? && reTransportBranchList?size &gt; 0>
                        <p class="listName">大交通</p>
                        <table width="100%" class="additionTable">
                            <tbody>
                            	<tr>
        						 <td>
		        				<#list reTransportBranchList as pb>
		        					<#list pb.suppGoodsList as goods >
		        						<table class="tab_nav" width="100%">
			        					<tr class="table_nav" adult="" child="" suppGoodsId="${goods.suppGoodsId}">
			        						<td width="50%"><a class="pro_tit" href="javascript:;" desc="${pb.branchName}" prodBranchId="${pb.productBranchId}">${pb.branchName}</a>
			        							<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
	        									<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].routeRelation" value="ADDITION" autocomplete="off"/>
	        								</a></td>
			        						<td width="20%" class="orange">成人价:￥${goods.suppGoodsBaseTimePrice.auditPriceYuan}元/儿童价:￥${goods.suppGoodsBaseTimePrice.childPriceYuan}元
						        			</td>
						        			<td width="20%">
			        							成人数<input goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 adultNumText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitAdultQuantity}" required=true number=true />
						        				儿童数<input goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 childNumText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].childQuantity" style="text-align:center" value="${goods.fitChildQuantity}" required=true number=true />
						        				<input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="0" required=true number=true />
						        			</td>
			        						<td width="10%" class="orange">总价:￥0元</td>
			        					</tr>
			        					<tr>
										 <td colspan="4">
										 <p class="descript">规格描述：<textarea class="textarea"></textarea></p>
										 </td>
										</tr>
			        					</table>
			        				<#assign productItemIdIndex=productItemIdIndex+1 />  	        					        				       				
		        				</#list>
		        				</#list>
		        				</td>
        						</tr>
                            </tbody>
                        </table>
        </#if>