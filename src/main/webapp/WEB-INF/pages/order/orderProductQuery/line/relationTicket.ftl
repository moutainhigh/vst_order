<#if relSaleDetailMap?? && relSaleDetailMap?size &gt; 0>
	<#list relSaleDisplayOrder as categoryName>
	<#if relSaleDetailMap[categoryName] ??>
    	 <p class="listName" style="font-weight:bold;">${categoryName}</p>
    	 <#assign productMap = relSaleDetailMap[categoryName] >
    	 <#if productMap?? && productMap?size &gt; 0>
    	 	<#list productMap?keys as productId>
    	 		<#assign suppGoodsCategoryMap = productMap[productId] >
    	 		<#assign defaultGoodsList = suppGoodsCategoryMap['defaultGoodsList']>
				<#assign moreGoodsList = suppGoodsCategoryMap['moreGoodsList']>
				<#assign currProduct = suppGoodsCategoryMap['product']>
				<#assign productItemIdIndex=0 />
				<table width="100%" class="additionTable" data-type="ticket">
		    		<tbody>
    	 				<tr>
							<td colspan="6" style="font-weight:bold;">
								<#if currProduct.bizCategory.categoryCode=="category_other_ticket">
									<a class="pro_tit" href="http://ticket.lvmama.com/p-${productId}" target="_blank">${currProduct.productName}</a>
								</#if>
								<#if currProduct.bizCategory.categoryCode=="category_single_ticket">
									<a class="pro_tit" href="http://ticket.lvmama.com/scenic-${currProduct.urlId}" target="_blank">${currProduct.productName}</a>
								</#if>
								<#if currProduct.bizCategory.categoryCode=="category_comb_ticket">
									<span style="color:#0088CC;">${currProduct.productName}</span>
								</#if>
								<#if currProduct.bizCategory.categoryCode=="category_visa">
									<a class="pro_tit" href="http://www.lvmama.com/visa/${currProduct.bizDistrict.pinyin}" target="_blank">${currProduct.productName}</a>
								</#if>
							</td>
						</tr>
						<tr>
		         			<td colspan="6" style="text-align:left;">
		         			<#if defaultGoodsList?? && defaultGoodsList?size &gt; 0>
			         				<table class="tab_nav" width="100%">
			         					<tbody width="100%">
			         					<#list defaultGoodsList as currSuppGoodsMap>
			         						<#assign currSuppGoods = currSuppGoodsMap['suppGoods']>
			         						<#assign currSelectDateMap = currSuppGoodsMap['selectDateMap']>
			         						<#assign currProdProductBranch = currSuppGoodsMap['prodProductBranch']>
			         						<#assign suppGoodsBaseTimePrice = currSuppGoods.suppGoodsBaseTimePrice>
			         						<#assign reType = currSuppGoodsMap['reType']>
			         						
			         						
					        				<tr class="table_nav" width="100%">
					        					<td width="45%">
					        						&nbsp;&nbsp;<a class="showBranchDesc" href="javascript:;">${currProdProductBranch.branchName}(${currSuppGoods.goodsName})</a>
					        						<input type="hidden" name="itemMap[${currSuppGoods.suppGoodsId}].goodsId" value="${currSuppGoods.suppGoodsId}" autocomplete="off"/>
				        							<input type="hidden" name="itemMap[${currSuppGoods.suppGoodsId}].routeRelation" value="ADDITION" autocomplete="off"/>
				        							<input class="selectDateInput" type="hidden" name="itemMap[${currSuppGoods.suppGoodsId}].visitTime" value="${currSuppGoodsMap['defaultSelectDate']}" autocomplete="off"/>
					        					</td>
					        					<td  class="orange"  width="10%">单价:￥${currSuppGoodsMap['defaultSinglePrice']}</td>
					        					<td  width="20%">
					        						<select class="selectDateDropdownList" <#if currProduct.bizCategory.categoryCode == "category_visa">style="display:none;"</#if>>
							        					<#if currSelectDateMap??>
								        					<#list currSelectDateMap? keys as selectDate>
																<option value="${currSelectDateMap[selectDate]}" defaultSelectDate="${currSuppGoodsMap['defaultSelectDate']}" <#if selectDate==currSuppGoodsMap['defaultSelectDate'] >selected="selected"</#if> >${selectDate}</option>
															</#list>
							        					</#if>
							        				 </select>
					        					</td>
					        					<td  width="15%">
					        						<select childPrice="0" type="text" class="selectQunantityDropdownList" 
					        						data-reType="${reType}"
					        						data-maxQuantity="${currSuppGoods.maxQuantity}" 
					        						data-minQuantity="${currSuppGoods.minQuantity}" 
					        						data-specialTicketType="${currSuppGoods.specialTicketType}" 
					        						data-stock="<#if suppGoodsBaseTimePrice??>${suppGoodsBaseTimePrice.stock}</#if>"
					        						data-stockFlag="<#if suppGoodsBaseTimePrice??>${suppGoodsBaseTimePrice.stockFlag}</#if>" 
					        						goodsType="${currSuppGoods.goodsType}" style="width:50px;" name="itemMap[${currSuppGoods.suppGoodsId}].quantity" data-suppGoodsid="${currSuppGoods.suppGoodsId}" goodsId="${goods.suppGoodsId}" style="text-align:center" required=true number=true>
				        								<#list currSuppGoods.selectQuantityRange?split(",") as num>
															<option value="${num}" <#if num==currSuppGoods.fitQuantity>selected</#if>>${num}</option>
														</#list>
				        							</select>
				        							份
					        					</td>
					        					<td class="orange" width="10%">总价:￥${currSuppGoodsMap['defaultTotalPrice']}</td>
					        				</tr>
					        				<tr>
										 		<td colspan="5">
										 			<p class="descript">规格描述：<textarea class="textarea" value="${currSuppGoodsMap['description']}"></textarea></p>
												</td>
											</tr>
					        			</#list>
					        			</tbody>
				        			</table>
								</#if>
								<div style="display:none; width:100%;">
		         				<#if suppGoodsCategoryMap['moreGoodsList']?? && suppGoodsCategoryMap['moreGoodsList']?size &gt; 0>
		         					<#list suppGoodsCategoryMap['moreGoodsList'] as currSuppGoodsMap>
			         				<table class="tab_nav" width="100%">
			         					<tbody width="100%">
			         						<#assign currSuppGoods = currSuppGoodsMap['suppGoods']>
			         						<#assign currSelectDateMap = currSuppGoodsMap['selectDateMap']>
			         						<#assign currProdProductBranch = currSuppGoodsMap['prodProductBranch']>
			         						<#assign suppGoodsBaseTimePrice = currSuppGoods.suppGoodsBaseTimePrice>
			         						<#assign reType = currSuppGoodsMap['reType']>
					        				<tr class="table_nav" width="100%">
					        					<td width="45%">
					        						&nbsp;&nbsp;<a class="showBranchDesc" href="javascript:;">${currProdProductBranch.branchName}(${currSuppGoods.goodsName})</a>
					        						<input type="hidden" name="itemMap[${currSuppGoods.suppGoodsId}].goodsId" value="${currSuppGoods.suppGoodsId}" autocomplete="off"/>
				        							<input type="hidden" name="itemMap[${currSuppGoods.suppGoodsId}].routeRelation" value="ADDITION" autocomplete="off"/>
				        							<input class="selectDateInput" type="hidden" name="itemMap[${currSuppGoods.suppGoodsId}].visitTime" value="${currSuppGoodsMap['defaultSelectDate']}" autocomplete="off"/>
					        					</td>
					        					<td  class="orange"  width="10%">单价:￥${currSuppGoodsMap['defaultSinglePrice']}</td>
					        					<td  width="20%">
					        					
					        						<select class="selectDateDropdownList" <#if currProduct.bizCategory.categoryCode == "category_visa">style="display:none;"</#if>>
							        					<#if currSelectDateMap??>
								        					<#list currSelectDateMap? keys as selectDate>
																<option value="${currSelectDateMap[selectDate]}" defaultSelectDate="${currSuppGoodsMap['defaultSelectDate']}" <#if selectDate==currSuppGoodsMap['defaultSelectDate'] >selected="selected"</#if>>${selectDate}</option>
															</#list>
							        					</#if>
							        				 </select>
					        					</td>
					        					<td  width="15%">
					        						<select childPrice="0" type="text" class="selectQunantityDropdownList" 
					        						data-reType="${reType}"
					        						data-maxQuantity="${currSuppGoods.maxQuantity}" 
					        						data-minQuantity="${currSuppGoods.minQuantity}" 
					        						data-specialTicketType="${currSuppGoods.specialTicketType}" 
					        						data-stock="<#if suppGoodsBaseTimePrice??>${suppGoodsBaseTimePrice.stock}</#if>"
					        						data-stockFlag="<#if suppGoodsBaseTimePrice??>${suppGoodsBaseTimePrice.stockFlag}</#if>" 
					        						goodsType="${currSuppGoods.goodsType}" style="width:50px;" name="itemMap[${currSuppGoods.suppGoodsId}].quantity" data-suppGoodsid="${currSuppGoods.suppGoodsId}" goodsId="${goods.suppGoodsId}" style="text-align:center" required=true number=true>
				        								<#list currSuppGoods.selectQuantityRange?split(",") as num>
															<option value="${num}" <#if num==currSuppGoods.fitQuantity>selected</#if>>${num}</option>
														</#list>
				        							</select>
				        							份
					        					</td>
					        					<td class="orange" width="10%">总价:￥${currSuppGoodsMap['defaultTotalPrice']}</td>
					        				</tr>
					        				<tr>
										 		<td colspan="5">
										 			<p class="descript">规格描述：<textarea class="textarea" value="${currSuppGoodsMap['description']}"></textarea></p>
												</td>
											</tr>
					        			</tbody>
				        			</table>
				        			</#list>
								</#if>
								</div>
				        		<#if suppGoodsCategoryMap['moreGoodsList']?? && suppGoodsCategoryMap['moreGoodsList']?size &gt; 0>
									<div style="width:100%; text-align:right;"><span class="displayMoreGoods" style="cursor:pointer; color:#0088CC;">更多>></span></div>
								</#if>
		         			</td>
		         		</tr>
					</tbody>
		        </table> 
    	 	</#list>
    	 </#if>
   	</#if>
    </#list>
</#if>
<!--其他票JS start-->
<script>
	$(document).ready(function(){
		if($("#originalOrderId").val()!=null && $("#originalOrderId").val()!=""){
			//$("table[data-type='ticket']").find("select.selectDateDropdownList").trigger("change");
			if($("table[data-type='ticket']").find("select.selectQunantityDropdownList").length>0){
				$("table[data-type='ticket']").find("select.selectQunantityDropdownList").eq(0).trigger("change");
			}
		}
	});
</script>