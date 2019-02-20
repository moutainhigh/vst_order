<table width="100%">
	<tbody>
    	<tr>
    		<input type="hidden" name="itemMap[${suppGoods.suppGoodsId}].goodsId" value="${suppGoods.suppGoodsId}" autocomplete="off"/>
    		<input type="hidden" name="itemMap[${suppGoods.suppGoodsId}].goodType" value="${suppGoods.goodsType}" autocomplete="off"/>
        	<td width="35%">
        		<!--<a href="javascript:void(0);" >-->${suppGoods.prodProductBranch.branchName}-${suppGoods.goodsName}<!--</a>-->
        	</td>
        	<td width="25%"><p></p></td>
            <td width="10%" class="orange">单价：￥${suppGoodsSimpleTimePrice.priceYuan}</td>
            <td width="10%">
            	<select class="hotel_sum mainSum" name="itemMap[${suppGoods.suppGoodsId}].quantity" goodsId="${suppGoods.suppGoodsId}" goodsType="${suppGoods.goodsType}" mainItem="true">
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