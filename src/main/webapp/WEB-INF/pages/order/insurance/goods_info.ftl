<table width="100%">
	<tbody>
    	<tr>
        	<td width="35%">
        		${suppGoods.prodProduct.productName}(${suppGoods.goodsName})
        	</td>
            <td width="15%" class="orange">
            	单价：￥
            	${suppGoodsSimpleTimePrice.priceYuanStr}
            </td>
            <td width="10%">
            	<input type="hidden" name="itemMap[${suppGoods.suppGoodsId}].goodsId" value="${suppGoods.suppGoodsId}"/>
            	<select class="hotel_sum" name="itemMap[${suppGoods.suppGoodsId}].quantity" goodsId="${suppGoods.suppGoodsId}" goodsType="${suppGoods.goodsType}" mainItem="true">
                	<#list suppGoods.minQuantity..suppGoods.maxQuantity as num>
						<option value="${num}" >${num}</option>
					</#list>
                </select>
            </td>
            <td width="10%" class="orange" id="${suppGoods.suppGoodsId}Td">总价：￥--</td>
            <td></td>
        </tr>
        </tr>
    </tbody>
</table>