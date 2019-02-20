<h5 class="hotel_tab_title">保险</h5>
<div class="hotel_tab">
<table width="100%" data-type="insurance">
	<tbody>
      <#list suppGoodsSaleReList as suppGoodsSaleRe>
        <#if suppGoodsSaleRe.insSuppGoodsList??>
	       	<#list suppGoodsSaleRe.insSuppGoodsList as sg>
                <tr suppGoodsId="${sg.suppGoodsId}">
                	<input type="hidden" name="itemMap[${sg.suppGoodsId}].goodsId" value="${sg.suppGoodsId}" autocomplete="off"/>
                    <td width="35%">
                    <a href="javascript:void(0);" class="J_tip" tip-content=""  onmouseover="goodsDetailMouseover(${sg.suppGoodsId},this);">${sg.prodProduct.productName}(${sg.prodProductBranch.branchName}-${sg.goodsName})</a>
                    <div style="display: none;">
                    ${sg.prodProductBranch.propValue['branch_desc']?html}
                    </div>
                    </td>
                   	<td width="25%"><p></p></td>
                    <td width="10%" class="orange">单价：￥${sg.suppGoodsBaseTimePrice.priceYuanStr}</td>
					<td width="10%">
            		<select class="hotel_sum" name="itemMap[${sg.suppGoodsId}].quantity" goodsId="${sg.suppGoodsId}" goodsType="${sg.goodsType}" data="${sg.productId}"  onchange="changeOnly(this)">
					<#list 0..sg.maxQuantity as num>
						<option value="${num}" <#if num==quantity>selected="selected"</#if>>${num}</option>
					</#list>
					</select>
					</td>
                    <td width="10%" class="orange" id="${sg.suppGoodsId}Td">总价：￥--</td>
            		<td></td>
               </tr>
              </#list>
           </#if>
     </#list>
    </tbody>
</table>
</div>
<script >
	
	function changeOnly(_this){
		var productId = $(_this).attr("data");
		var goodsId = $(_this).attr("goodsId");
		var value = $(_this).get(0).selectedIndex;
		var ProCategoryId = '${prodProduct.bizCategory.categoryId}';
		var subCategoryId = '${prodProduct.subCategoryId}';
		var productType = '${prodProduct.productType}';
		if(ProCategoryId == '18' && subCategoryId=='182' &&productType=='INNERLINE'){
			if(value !=0){
			//变为单选
				var selectlist = $('.hotel_sum');
				for(var i=0;i<selectlist.length;i++){
					if(selectlist.eq(i).attr("data")==productId && selectlist.eq(i).attr("goodsId")!=goodsId){
						if(selectlist.eq(i).get(0).selectedIndex !=0){
							selectlist.eq(i).eq(0).find('option').eq(0).attr("selected","selected");
							selectlist.eq(i).parent().next().html("总价：￥--");
						}
						
					}    			
				}
			}
		}
		
	}
	
	
	
</script>