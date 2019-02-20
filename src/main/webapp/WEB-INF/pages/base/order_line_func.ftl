<#macro showSpreadPrice2 sourcePrice finalPrice>
<#assign val=(finalPrice-sourcePrice)/100.0/>
<#if val gt 0>+</#if>${val}
</#macro>
<#macro showSpreadPrice sourcePrice branch>
<#assign val=(branch.dailyLowestPrice-sourcePrice)/100.0/>
<#if val gt 0>+</#if>${val}
</#macro>
<#macro showOptionSelect productId prodProductBranch goods productItemIdIndex>

<#--一键重下，信息反填-->
<#assign key = "${goods.suppGoodsId}">
<#if addtionalQuantity??>
	<#assign quantity = "${addtionalQuantity[key]?default('')}">
<#else>
	<#assign quantity = "">
</#if>

<#if productItemIdIndex=="goods">
<select auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" goodsId="${goods.suppGoodsId}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 numText hotel_sum" name="itemMap[${goods.suppGoodsId}].adultQuantity" style="text-align:center" value="${goods.fitChildQuantity+goods.fitAdultQuantity}" required=true number=true />
<#else>
<select auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" goodsId="${goods.suppGoodsId}" style="width:60px" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" name="productMap[${productId}].itemList[${productItemIdIndex}].adultQuantity" class="numText hotel_sum">
</#if>
    <#list prodProductBranch.selectQuantityRange?split(",") as num>
        <#if num == quantity>
            <#--一键重下，信息反填-->
            <option selected="selected" value="${num}" >${num}</option>
        <#else>
            <option value="${num}" >${num}</option>
        </#if>
	</#list>
</select>
</#macro>