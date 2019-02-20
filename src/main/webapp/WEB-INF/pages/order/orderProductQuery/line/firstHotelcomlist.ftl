  <#if comboDinnerList?? && comboDinnerList?size &gt; 0>
 		<div class="hotel_ebk_wrap">
					<#if comboDinnerList?size &gt; 1>
						<div class="table_t">必选服务</div>
                        <p class="listName"><a href="#" class="fr moreCategoryHotelCom">更多规格</a>酒店套餐</p>
					<#else>
						<div class="table_t">必选服务</div> 
					</#if> 
                        <table width="100%" class="firstHotelcomTable">
                         <tbody>
                         <tr>
        					<td>
        					<#assign hotelcomIdIndex=0 />
        					<#assign goods=comboDinnerList[0].suppGoodsList[0] />        						
        						 	<table class="tab_nav" width="100%">
        						 		<tr id="firstHotelInfoShow" class="table_nav" adult="" child="" suppGoodsId="${goods.suppGoodsId}">
		        						<td width="60%" ><a class="pro_tit" href="javascript:;" desc="${comboDinnerList[0].branchName}" prodBranchId="${comboDinnerList[0].productBranchId}">${comboDinnerList[0].branchName}</a>
		        							<input type="hidden" name="itemMap[${goods.suppGoodsId}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
											<input type="hidden" name="itemMap[${goods.suppGoodsId}].routeRelation" value="MAIN" autocomplete="off"/>
		        						</td>
		        						<td width="10%" num="${goods.fitQuantity}">
		        							<input type="hidden" readOnly class="w5" name="itemMap[${goods.suppGoodsId}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
		        							${goods.fitQuantity}份
		        						</td>
		        						<td width="10%" class="orange">总价：￥${goods.dailyLowestPriceYuan}元</td>
			        					</tr>
			        					<tr>
											<td colspan="3">
											<p class="descript">规格描述：<textarea class="textarea"></textarea></p>
											</td>
										</tr>
        						 	</table>        							        					
	        				<#assign hotelcomIdIndex=hotelcomIdIndex+1 />   
	        				 </td>
        					</tr>
        				</tbody>
                        </table>
    	</div>
</#if>