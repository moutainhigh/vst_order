   <#if visaBranchList?? && visaBranchList?size &gt; 0>
                        <p class="table_t">签证</p>
                        <table width="100%" class="additionTable">
                            <tbody>
                            	<tr>
        						 <td>
		        				<#list visaBranchList as pb>
		        					<#list pb.suppGoodsList as goods >
		        						<table class="tab_nav" width="100%">
			        					<tr class="table_nav" adult="" child="" suppGoodsId="${goods.suppGoodsId}">
			        						<td width="50%"><a class="pro_tit" href="javascript:;" desc="${pb.branchName}" prodBranchId="${pb.productBranchId}">${pb.branchName}</a>
			        							<input type="hidden" name="itemMap[${goods.suppGoodsId}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
	        									<input type="hidden" name="itemMap[${goods.suppGoodsId}].routeRelation" value="ADDITION" autocomplete="off"/>
	        								</a></td>
			        						<td width="20%" class="orange">单价:￥${goods.suppGoodsBaseTimePrice.priceYuan}元</td>
			        						<td width="20%">
			        							<select auditPrice="${goods.suppGoodsBaseTimePrice.priceYuan}" childPrice="0" type="text" class="w5 numText" name="itemMap[${goods.suppGoodsId}].quantity" goodsId="${goods.suppGoodsId}" style="text-align:center" required=true number=true>
			        								<#list goods.selectQuantityRange?split(",") as num>
														<option value="${num}" <#if num==goods.fitQuantity>selected</#if>>${num}</option>
													</#list>
			        							</select>
			        						</td>
			        						<td width="10%" class="orange" id="${goods.suppGoodsId}Td">总价:￥0元</td>
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