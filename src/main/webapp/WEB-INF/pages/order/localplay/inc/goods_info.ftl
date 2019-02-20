<table width="100%">
	<tbody>
    	<tr>
    		<input type="hidden" name="itemMap[${suppGoods.suppGoodsId}].goodsId" value="${suppGoods.suppGoodsId}" productType="${suppGoods.prodProduct.productType}"  goodsId="${suppGoods.suppGoodsId}" maxQuantity="${suppGoods.maxQuantity}" minQuantity="${suppGoods.minQuantity}" goodsType="${suppGoods.goodsType}" mainItem="true" autocomplete="off"/>
        	<td width="35%">
        		<a href="javascript:void(0);" class="J_tip" tip-content="">${suppGoods.goodsName}</a>
        	</td>
            <td width="20%">
            </td>
            <td width="15%" id="${suppGoods.suppGoodsId}singePrice" class="orange"></td>
            <td width="10%">
            	<select  id="selectgoods"  class="hotel_sum" name="itemMap[${suppGoods.suppGoodsId}].quantity" productType="${suppGoods.prodProduct.productType}" goodsId="${suppGoods.suppGoodsId}" goodsType="${suppGoods.goodsType}" mainItem="true">
                	<#list suppGoods.minQuantity..suppGoods.maxQuantity as num>
						<option value="${num}" >${num}</option>
					</#list>
                </select>
            </td>
            <td width="15%" id="${suppGoods.suppGoodsId}goodPrice" class="orange">商品总价：￥--</td>
            <td></td>
        </tr>
    </tbody>
</table>