<table width="100%">
	<tbody>
    	<tr>
    		<input type="hidden" name="itemMap[${suppGoods.suppGoodsId}].goodsId" value="${suppGoods.suppGoodsId}" productType="${suppGoods.prodProduct.productType}"  goodsId="${suppGoods.suppGoodsId}" maxQuantity="${suppGoods.maxQuantity}" minQuantity="${suppGoods.minQuantity}" goodsType="${suppGoods.goodsType}" mainItem="true" autocomplete="off"/>
        	<td width="35%">
        		<a href="javascript:void(0);" class="J_tip" tip-content="">${suppGoods.goodsName}</a>
        	</td>
            <td width="20%">
            </td>
            <td width="15%" <#if  product.productType == 'PHONE'>id="${suppGoods.suppGoodsId}singePrice"</#if> class="orange"><#if product.productType == 'PHONE'></#if></td>
            <td width="10%">
            	<select class="hotel_sum" name="itemMap[${suppGoods.suppGoodsId}].quantity" productType="${suppGoods.prodProduct.productType}" goodsId="${suppGoods.suppGoodsId}" goodsType="${suppGoods.goodsType}" mainItem="true">
                	<#list suppGoods.minQuantity..suppGoods.maxQuantity as num>
						<option value="${num}" >${num}</option>
					</#list>
                </select>
            </td>
            <td width="15%" id="${suppGoods.suppGoodsId}goodPrice" class="orange">商品总价：￥--</td>
            <td></td>
        </tr>
        <#if depositGood??>
        <tr>
    		<td width="35%">
        		<a href="javascript:void(0);" class="J_tip" tip-content="">押金</a>
        		(<span class="orange">￥${depositGood.suppGoodsNotimeTimePrice.price/100}</span>
        		/台)
        	</td>
            <td width="20%">
            </td>
            <td width="15%" class="orange"></td>
            <td width="10%">
            	<input type="hidden"  name="itemMap[${depositGood.suppGoodsId}].goodsId" value="${depositGood.suppGoodsId}">
                <input type="hidden" id="depositVisitime"   name="itemMap[${depositGood.suppGoodsId}].visitTime" value="${startDate?string('yyyy-MM-dd')!''}"/>
                <input id="depositGoodNumInput" class="hotel_sum" type="text" style="width:67px;" readonly="readonly" name="itemMap[${depositGood.suppGoodsId}].quantity"  value="${suppGoods.minQuantity}" productType="${suppGoods.prodProduct.productType}" goodsId="${suppGoods.suppGoodsId}" goodsType="${suppGoods.goodsType}" mainItem="true" >
                
            </td>
            <td id="${suppGoods.suppGoodsId}deposit" width="10%" class="orange">押金总价：￥--</td>
            <td></td>
        </tr>
        <#elseif product.productType == WIFI>
           <tr>
    		<td width="35%">
        		<a href="javascript:void(0);" class="J_tip" tip-content="">押金</a>
        		(<span class="orange">到付</span>)
        	</td>
            <td width="20%">
            </td>
            <td width="15%" class="orange"></td>
            <td width="10%">
            </td>
            <td width="10%" class="orange"></td>
            <td></td>
        </tr>
        </#if>
        
    </tbody>
</table>